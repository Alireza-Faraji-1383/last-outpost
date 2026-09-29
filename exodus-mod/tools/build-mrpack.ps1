[CmdletBinding()]
param(
    [string]$MinecraftRoot = (Resolve-Path (Join-Path $PSScriptRoot "..\..")).Path,
    [string]$OutputPath = (Join-Path (Resolve-Path (Join-Path $PSScriptRoot "..\..")).Path "dist\Project-Exodus-0.2.0.mrpack"),
    [switch]$SkipDownloadVerification
)

$ErrorActionPreference = "Stop"
$ProgressPreference = "SilentlyContinue"

$packName = "Project Exodus"
$packVersion = "0.2.0"
$excludedMods = @("jei-1.20.1-forge-15.56.0.205.jar")
$overrideDirectories = @("config", "defaultconfigs", "kubejs", "recruits", "tacz")
$buildRoot = Join-Path $MinecraftRoot "exodus-mod\build\mrpack"
$stagingRoot = Join-Path $buildRoot "staging"
$verificationRoot = Join-Path $buildRoot "verification"
$reportPath = Join-Path (Split-Path $OutputPath -Parent) "Project-Exodus-0.2.0-build-report.json"

function Get-FileDigestRecord {
    param([System.IO.FileInfo]$File)

    [pscustomobject]@{
        Name = $File.Name
        FullName = $File.FullName
        Size = $File.Length
        Sha1 = (Get-FileHash -LiteralPath $File.FullName -Algorithm SHA1).Hash.ToLowerInvariant()
        Sha512 = (Get-FileHash -LiteralPath $File.FullName -Algorithm SHA512).Hash.ToLowerInvariant()
    }
}

function Copy-DirectoryContents {
    param([string]$Source, [string]$Destination)

    if (-not (Test-Path -LiteralPath $Source)) {
        return
    }

    New-Item -ItemType Directory -Path $Destination -Force | Out-Null
    Get-ChildItem -LiteralPath $Source -Force | Copy-Item -Destination $Destination -Recurse -Force
}

if (Test-Path -LiteralPath $buildRoot) {
    Remove-Item -LiteralPath $buildRoot -Recurse -Force
}
New-Item -ItemType Directory -Path $stagingRoot -Force | Out-Null
New-Item -ItemType Directory -Path (Split-Path $OutputPath -Parent) -Force | Out-Null

$modsRoot = Join-Path $MinecraftRoot "mods"
$modFiles = Get-ChildItem -LiteralPath $modsRoot -File -Filter "*.jar" |
    Where-Object { $_.Name -notin $excludedMods } |
    Sort-Object Name
$records = @($modFiles | ForEach-Object { Get-FileDigestRecord -File $_ })

$requestBody = @{
    hashes = @($records | ForEach-Object { $_.Sha512 })
    algorithm = "sha512"
} | ConvertTo-Json -Depth 4

$lookup = Invoke-RestMethod `
    -Method Post `
    -Uri "https://api.modrinth.com/v2/version_files" `
    -ContentType "application/json" `
    -Headers @{ "User-Agent" = "Project-Exodus-Mrpack-Builder/0.2.0" } `
    -Body $requestBody

$manifestFiles = [System.Collections.Generic.List[object]]::new()
$downloadable = [System.Collections.Generic.List[string]]::new()
$bundled = [System.Collections.Generic.List[string]]::new()
$overrideModsRoot = Join-Path $stagingRoot "overrides\mods"

foreach ($record in $records) {
    $property = $lookup.PSObject.Properties[$record.Sha512]
    $version = if ($null -ne $property) { $property.Value } else { $null }
    $matchingFile = if ($null -ne $version) {
        $version.files | Where-Object { $_.hashes.sha512 -eq $record.Sha512 } | Select-Object -First 1
    } else {
        $null
    }

    $isCompatible = $null -ne $version -and
        $version.game_versions -contains "1.20.1" -and
        $version.loaders -contains "forge"

    if ($isCompatible -and $null -ne $matchingFile -and -not [string]::IsNullOrWhiteSpace($matchingFile.url)) {
        $manifestFiles.Add([ordered]@{
            path = "mods/$($record.Name)"
            hashes = [ordered]@{
                sha1 = $record.Sha1
                sha512 = $record.Sha512
            }
            env = [ordered]@{
                client = "required"
                server = "required"
            }
            downloads = @($matchingFile.url)
            fileSize = $record.Size
        })
        $downloadable.Add($record.Name)
    } else {
        New-Item -ItemType Directory -Path $overrideModsRoot -Force | Out-Null
        Copy-Item -LiteralPath $record.FullName -Destination (Join-Path $overrideModsRoot $record.Name) -Force
        $bundled.Add($record.Name)
    }
}

foreach ($directory in $overrideDirectories) {
    Copy-DirectoryContents `
        -Source (Join-Path $MinecraftRoot $directory) `
        -Destination (Join-Path $stagingRoot "overrides\$directory")
}

$manifest = [ordered]@{
    formatVersion = 1
    game = "minecraft"
    versionId = $packVersion
    name = $packName
    summary = "Minecraft 1.20.1 Forge minigame pack with Project Exodus."
    files = @($manifestFiles)
    dependencies = [ordered]@{
        minecraft = "1.20.1"
        forge = "47.4.10"
    }
}

$manifestPath = Join-Path $stagingRoot "modrinth.index.json"
$manifest | ConvertTo-Json -Depth 10 | Set-Content -LiteralPath $manifestPath -Encoding utf8

if (Test-Path -LiteralPath $OutputPath) {
    Remove-Item -LiteralPath $OutputPath -Force
}
Add-Type -AssemblyName System.IO.Compression.FileSystem
[System.IO.Compression.ZipFile]::CreateFromDirectory(
    $stagingRoot,
    $OutputPath,
    [System.IO.Compression.CompressionLevel]::Optimal,
    $false
)

if (Test-Path -LiteralPath $verificationRoot) {
    Remove-Item -LiteralPath $verificationRoot -Recurse -Force
}
New-Item -ItemType Directory -Path $verificationRoot -Force | Out-Null
[System.IO.Compression.ZipFile]::ExtractToDirectory($OutputPath, $verificationRoot)

$verifiedManifest = Get-Content -LiteralPath (Join-Path $verificationRoot "modrinth.index.json") -Raw | ConvertFrom-Json
if ($verifiedManifest.formatVersion -ne 1 -or $verifiedManifest.game -ne "minecraft") {
    throw "The generated Modrinth manifest is invalid."
}
if ($verifiedManifest.dependencies.minecraft -ne "1.20.1" -or $verifiedManifest.dependencies.forge -ne "47.4.10") {
    throw "The generated loader dependencies are invalid."
}

$unsafeEntries = [System.IO.Compression.ZipFile]::OpenRead($OutputPath)
try {
    foreach ($entry in $unsafeEntries.Entries) {
        if ($entry.FullName -match '(^|/)\.\.(/|$)' -or [System.IO.Path]::IsPathRooted($entry.FullName)) {
            throw "Unsafe archive path: $($entry.FullName)"
        }
    }
} finally {
    $unsafeEntries.Dispose()
}

if (-not $SkipDownloadVerification) {
    foreach ($entry in $verifiedManifest.files) {
        $destination = Join-Path $verificationRoot ($entry.path -replace '/', [System.IO.Path]::DirectorySeparatorChar)
        New-Item -ItemType Directory -Path (Split-Path $destination -Parent) -Force | Out-Null
        Invoke-WebRequest -Uri $entry.downloads[0] -OutFile $destination -Headers @{ "User-Agent" = "Project-Exodus-Mrpack-Builder/0.2.0" }
        $actualSha1 = (Get-FileHash -LiteralPath $destination -Algorithm SHA1).Hash.ToLowerInvariant()
        $actualSha512 = (Get-FileHash -LiteralPath $destination -Algorithm SHA512).Hash.ToLowerInvariant()
        if ($actualSha1 -ne $entry.hashes.sha1 -or $actualSha512 -ne $entry.hashes.sha512) {
            throw "Downloaded file hash mismatch: $($entry.path)"
        }
    }
}

$resultingMods = Get-ChildItem -LiteralPath (Join-Path $verificationRoot "overrides\mods") -File -Filter "*.jar" -ErrorAction SilentlyContinue
$resultingModNames = @($resultingMods.Name) + @($verifiedManifest.files.path | ForEach-Object { Split-Path $_ -Leaf })
if ($resultingModNames.Count -ne $records.Count) {
    throw "Expected $($records.Count) mods after installation simulation, found $($resultingModNames.Count)."
}
if (($resultingModNames | Where-Object { $_ -like "jei-*.jar" }).Count -ne 1) {
    throw "The generated pack does not contain exactly one JEI version."
}

$report = [ordered]@{
    outputPath = $OutputPath
    archiveSize = (Get-Item -LiteralPath $OutputPath).Length
    minecraft = "1.20.1"
    forge = "47.4.10"
    totalMods = $records.Count
    downloadableMods = @($downloadable)
    bundledMods = @($bundled)
    excludedMods = $excludedMods
    includedOverrideDirectories = $overrideDirectories
    downloadVerification = -not $SkipDownloadVerification
    verifiedAtUtc = [DateTime]::UtcNow.ToString("o")
}
$report | ConvertTo-Json -Depth 6 | Set-Content -LiteralPath $reportPath -Encoding utf8
$report | ConvertTo-Json -Depth 6
