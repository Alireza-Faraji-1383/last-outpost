[CmdletBinding()]
param(
    [string]$MinecraftRoot = (Resolve-Path (Join-Path $PSScriptRoot "..\..")).Path,
    [string]$OutputPath = ""
)

$ErrorActionPreference = "Stop"

$packName = "Project Exodus"
$packVersion = (Select-String -LiteralPath (Join-Path $PSScriptRoot "..\gradle.properties") -Pattern '^mod_version=').Line.Split('=')[1]
if (!$OutputPath) { $OutputPath = Join-Path $MinecraftRoot "dist\Project-Exodus-$packVersion-CurseForge.zip" }
$customModName = "exodus-$packVersion.jar"
$excludedMods = @("jei-1.20.1-forge-15.56.0.205.jar")
$overrideDirectories = @("config", "defaultconfigs", "kubejs", "recruits")
$buildRoot = Join-Path $MinecraftRoot "exodus-mod\build\curseforge-pack"
$stagingRoot = Join-Path $buildRoot "staging"
$reportPath = Join-Path (Split-Path $OutputPath -Parent) "Project-Exodus-$packVersion-CurseForge-build-report.json"

$curseForgeFiles = @(
    @{ name = "[1.20.1] SecurityCraft v1.10.2.1.jar"; fingerprint = 2421776137; projectID = 64760; fileID = 8336490 }
    @{ name = "appleskin-forge-mc1.20.1-2.5.1.jar"; fingerprint = 1671637789; projectID = 248787; fileID = 4770828 }
    @{ name = "architectury-9.2.14-forge.jar"; fingerprint = 98480002; projectID = 419699; fileID = 5137938 }
    @{ name = "cloth-config-11.1.136-forge.jar"; fingerprint = 635589968; projectID = 348521; fileID = 5729105 }
    @{ name = "curios-forge-5.14.1+1.20.1.jar"; fingerprint = 1288931317; projectID = 309927; fileID = 6418456 }
    @{ name = "daffas_arsenal-3.7.1.2.jar"; fingerprint = 3212679312; projectID = 1254350; fileID = 8979524 }
    @{ name = "geckolib-forge-1.20.1-4.8.4.jar"; fingerprint = 3690314662; projectID = 388172; fileID = 8285794 }
    @{ name = "incontrol-1.20-9.5.0.jar"; fingerprint = 1045902606; projectID = 257356; fileID = 8790496 }
    @{ name = "Jade-1.20.1-Forge-11.13.3.jar"; fingerprint = 3261507386; projectID = 324717; fileID = 8479276 }
    @{ name = "jei-1.20.1-forge-15.62.0.216.jar"; fingerprint = 2884808425; projectID = 238222; fileID = 8964933 }
    @{ name = "kotlinforforge-4.12.0-all.jar"; fingerprint = 2392977662; projectID = 351264; fileID = 7291067 }
    @{ name = "kubejs-forge-2001.6.5-build.26.jar"; fingerprint = 2994552797; projectID = 238086; fileID = 8020595 }
    @{ name = "lanserverproperties-1.11.1-forge.jar"; fingerprint = 3682653448; projectID = 387365; fileID = 4776657 }
    @{ name = "lostcities-1.20-7.5.5.jar"; fingerprint = 1128582096; projectID = 269024; fileID = 8862717 }
    @{ name = "lrarmor-1.20.1-0.1.4.4.jar"; fingerprint = 3071182315; projectID = 1021131; fileID = 7343690 }
    @{ name = "lrtactical-1.20.1-0.4.3.jar"; fingerprint = 3473687940; projectID = 1273094; fileID = 8652673 }
    @{ name = "mezz_config-1.20.1-forge-0.6.5.jar"; fingerprint = 1448958754; projectID = 1689768; fileID = 8987763 }
    @{ name = "mobplayeranimator-forge-1.20.1-1.3.3-all.jar"; fingerprint = 3320375087; projectID = 1058791; fileID = 6125584 }
    @{ name = "MouseTweaks-forge-mc1.20.1-2.25.1.jar"; fingerprint = 3001740257; projectID = 60089; fileID = 5338457 }
    @{ name = "player-animation-lib-forge-1.0.2-rc1+1.20.jar"; fingerprint = 2854309810; projectID = 658587; fileID = 4587214 }
    @{ name = "rhino-forge-2001.2.3-build.10.jar"; fingerprint = 1432081092; projectID = 416294; fileID = 6186971 }
    @{ name = "simpleenemymod-1.20.1-0.1.6-beta-hotfix.jar"; fingerprint = 900752687; projectID = 1471691; fileID = 8880362 }
    @{ name = "sophisticatedbackpacks-1.20.1-3.26.3.2167.jar"; fingerprint = 3348443960; projectID = 422301; fileID = 8985926 }
    @{ name = "sophisticatedcore-1.20.1-1.5.2.2346.jar"; fingerprint = 4247952056; projectID = 618298; fileID = 8985880 }
    @{ name = "tacz-1.20.1-1.1.8-hotfix.jar"; fingerprint = 969230376; projectID = 1028108; fileID = 8141310 }
    @{ name = "taczadditions-1.20.1-1.3.5.jar"; fingerprint = 3198549257; projectID = 1356005; fileID = 8717646 }
    @{ name = "ZeroContact-main-build-72-36c6a8f.jar"; fingerprint = 832760669; projectID = 1581744; fileID = 8668188 }
)

if (-not ("CurseForgePackFingerprint" -as [type])) {
    Add-Type -TypeDefinition @'
using System;
using System.IO;
using System.Collections.Generic;

public static class CurseForgePackFingerprint
{
    public static uint Compute(string path)
    {
        byte[] input = File.ReadAllBytes(path);
        var normalized = new List<byte>(input.Length);
        foreach (byte value in input)
        {
            if (value != 9 && value != 10 && value != 13 && value != 32)
                normalized.Add(value);
        }

        byte[] data = normalized.ToArray();
        const uint multiplier = 0x5bd1e995;
        uint hash = 1u ^ (uint)data.Length;
        int offset = 0;
        int remaining = data.Length;

        while (remaining >= 4)
        {
            uint value = (uint)(data[offset] | data[offset + 1] << 8 | data[offset + 2] << 16 | data[offset + 3] << 24);
            value *= multiplier;
            value ^= value >> 24;
            value *= multiplier;
            hash *= multiplier;
            hash ^= value;
            offset += 4;
            remaining -= 4;
        }

        if (remaining == 3) hash ^= (uint)data[offset + 2] << 16;
        if (remaining >= 2) hash ^= (uint)data[offset + 1] << 8;
        if (remaining >= 1)
        {
            hash ^= data[offset];
            hash *= multiplier;
        }

        hash ^= hash >> 13;
        hash *= multiplier;
        hash ^= hash >> 15;
        return hash;
    }
}
'@
}

function Copy-DirectoryContents {
    param([string]$Source, [string]$Destination)

    if (-not (Test-Path -LiteralPath $Source)) {
        return
    }
    New-Item -ItemType Directory -Path $Destination -Force | Out-Null
    Get-ChildItem -LiteralPath $Source -Force | Copy-Item -Destination $Destination -Recurse -Force
}

$modsRoot = Join-Path $MinecraftRoot "mods"
$publicMods = @(Get-ChildItem -LiteralPath $modsRoot -File -Filter "*.jar" |
    Where-Object { $_.Name -ne $customModName -and $_.Name -notin $excludedMods } |
    Sort-Object Name)

if ($publicMods.Count -ne $curseForgeFiles.Count) {
    throw "Expected $($curseForgeFiles.Count) public mods, found $($publicMods.Count). Resolve CurseForge metadata before rebuilding."
}

foreach ($mod in $publicMods) {
    $mapping = $curseForgeFiles | Where-Object name -eq $mod.Name | Select-Object -First 1
    if ($null -eq $mapping) {
        throw "Missing CurseForge mapping for $($mod.Name)."
    }
    $actualFingerprint = [uint64][CurseForgePackFingerprint]::Compute($mod.FullName)
    if ($actualFingerprint -ne [uint64]$mapping.fingerprint) {
        throw "CurseForge fingerprint changed for $($mod.Name): expected $($mapping.fingerprint), actual $actualFingerprint."
    }
}

$duplicateProjects = @($curseForgeFiles | Group-Object projectID | Where-Object Count -gt 1)
if ($duplicateProjects.Count -ne 0) {
    throw "CurseForge mappings contain duplicate project IDs."
}

if (Test-Path -LiteralPath $buildRoot) {
    Remove-Item -LiteralPath $buildRoot -Recurse -Force
}
New-Item -ItemType Directory -Path $stagingRoot -Force | Out-Null
New-Item -ItemType Directory -Path (Split-Path $OutputPath -Parent) -Force | Out-Null

foreach ($directory in $overrideDirectories) {
    Copy-DirectoryContents `
        -Source (Join-Path $MinecraftRoot $directory) `
        -Destination (Join-Path $stagingRoot "overrides\$directory")
}

$customModPath = Join-Path $modsRoot $customModName
if (-not (Test-Path -LiteralPath $customModPath)) {
    throw "Missing custom mod: $customModPath"
}
$overrideModsRoot = Join-Path $stagingRoot "overrides\mods"
New-Item -ItemType Directory -Path $overrideModsRoot -Force | Out-Null
Copy-Item -LiteralPath $customModPath -Destination (Join-Path $overrideModsRoot $customModName) -Force

$manifest = [ordered]@{
    minecraft = [ordered]@{
        version = "1.20.1"
        modLoaders = @([ordered]@{
            id = "forge-47.4.10"
            primary = $true
        })
    }
    manifestType = "minecraftModpack"
    manifestVersion = 1
    name = $packName
    version = $packVersion
    author = "Alireza"
    files = @($curseForgeFiles | ForEach-Object {
        [ordered]@{
            projectID = [int64]$_.projectID
            fileID = [int64]$_.fileID
            required = $true
        }
    })
    overrides = "overrides"
}

$manifest | ConvertTo-Json -Depth 8 | Set-Content -LiteralPath (Join-Path $stagingRoot "manifest.json") -Encoding utf8

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

$report = [ordered]@{
    outputPath = $OutputPath
    archiveSize = (Get-Item -LiteralPath $OutputPath).Length
    minecraft = "1.20.1"
    forge = "47.4.10"
    curseForgeMods = $curseForgeFiles.Count
    bundledMods = @($customModName)
    excludedMods = $excludedMods
    includedOverrideDirectories = $overrideDirectories
    generatedAtUtc = [DateTime]::UtcNow.ToString("o")
}
$report | ConvertTo-Json -Depth 6 | Set-Content -LiteralPath $reportPath -Encoding utf8
$report | ConvertTo-Json -Depth 6
