$ErrorActionPreference = "Stop"

$minecraftRoot = (Resolve-Path (Join-Path $PSScriptRoot "..\..")).Path
$builder = Join-Path $PSScriptRoot "build-curseforge-pack.ps1"
$packVersion = (Select-String -LiteralPath (Join-Path $PSScriptRoot "..\gradle.properties") -Pattern '^mod_version=').Line.Split('=')[1]
$customModName = "exodus-$packVersion.jar"
$output = Join-Path $minecraftRoot "dist\Project-Exodus-$packVersion-CurseForge.zip"

if (-not (Test-Path -LiteralPath $builder)) {
    throw "Missing CurseForge pack builder: $builder"
}

& $builder -MinecraftRoot $minecraftRoot -OutputPath $output

Add-Type -AssemblyName System.IO.Compression.FileSystem
$verificationRoot = Join-Path $minecraftRoot "exodus-mod\build\curseforge-pack-test"
if (Test-Path -LiteralPath $verificationRoot) {
    Remove-Item -LiteralPath $verificationRoot -Recurse -Force
}
[System.IO.Compression.ZipFile]::ExtractToDirectory($output, $verificationRoot)

$manifestPath = Join-Path $verificationRoot "manifest.json"
if (-not (Test-Path -LiteralPath $manifestPath)) {
    throw "manifest.json is missing from the archive root."
}

$manifest = Get-Content -LiteralPath $manifestPath -Raw | ConvertFrom-Json
if ($manifest.manifestType -ne "minecraftModpack" -or $manifest.manifestVersion -ne 1) {
    throw "Unexpected CurseForge manifest type or version."
}
if ($manifest.minecraft.version -ne "1.20.1") {
    throw "Unexpected Minecraft version."
}
if ($manifest.minecraft.modLoaders[0].id -ne "forge-47.4.10" -or -not $manifest.minecraft.modLoaders[0].primary) {
    throw "Unexpected Forge loader."
}
$expectedPublicMods = @(Get-ChildItem -LiteralPath (Join-Path $minecraftRoot "mods") -File -Filter "*.jar" |
    Where-Object { $_.Name -notin @($customModName, "jei-1.20.1-forge-15.56.0.205.jar") })
if (@($manifest.files).Count -ne $expectedPublicMods.Count) {
    throw "Expected $($expectedPublicMods.Count) CurseForge file references, found $(@($manifest.files).Count)."
}

$duplicates = @($manifest.files | Group-Object projectID | Where-Object Count -gt 1)
if ($duplicates.Count -ne 0) {
    throw "The manifest contains duplicate CurseForge projects."
}

$lostCities = @($manifest.files | Where-Object { $_.projectID -eq 269024 -and $_.fileID -eq 8862717 -and $_.required })
if ($lostCities.Count -ne 1) {
    throw "The manifest must contain exactly one required Lost Cities 1.20-7.5.5 reference."
}

$overrideMods = @(Get-ChildItem -LiteralPath (Join-Path $verificationRoot "overrides\mods") -File -Filter "*.jar")
if ($overrideMods.Count -ne 1 -or $overrideMods[0].Name -ne $customModName) {
    throw "Only $customModName may be bundled in overrides/mods."
}
if (@($overrideMods | Where-Object Name -Match "lostcities").Count -ne 0) {
    throw "Lost Cities must be a public CurseForge reference, not a bundled override."
}

if (Test-Path -LiteralPath (Join-Path $verificationRoot "overrides\mods\jei-1.20.1-forge-15.56.0.205.jar")) {
    throw "The old JEI version must not be bundled."
}

if (Test-Path -LiteralPath (Join-Path $verificationRoot "overrides\tacz")) {
    throw "Extracted TACZ content must not be bundled because it is already present in the CurseForge mods."
}

Write-Output "PASS: CurseForge manifest has $($expectedPublicMods.Count) unique public mods and only Exodus is bundled."
