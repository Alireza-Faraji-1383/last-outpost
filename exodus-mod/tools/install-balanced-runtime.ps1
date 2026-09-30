param([Parameter(Mandatory=$true)][string]$MinecraftRoot)
$ErrorActionPreference = 'Stop'
$instanceRoot = (Resolve-Path -LiteralPath $MinecraftRoot).Path
$projectRoot = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot '..')).Path
$versionLine = Select-String -LiteralPath (Join-Path $projectRoot 'gradle.properties') -Pattern '^mod_version='
$version = $versionLine.Line.Split('=')[1]
$jar = Join-Path $projectRoot "build\libs\exodus-$version.jar"
if (!(Test-Path -LiteralPath $jar)) { throw 'Run the complete Gradle test/build gates before installing.' }
$modsRoot = (Resolve-Path -LiteralPath (Join-Path $instanceRoot 'mods')).Path
$configPath = Join-Path $instanceRoot 'config\sem-common.toml'
$config = Get-Content -Raw -LiteralPath $configPath
$settings = @{detectionRange='80.0'; maxShootDistance='70.0'; enableCustomDrops='true'; gunDropChance='0.02'; ammoDropChance='1.0'}
foreach ($key in $settings.Keys) {
    $pattern = '(?m)^([\t ]*)' + [regex]::Escape($key) + '[\t ]*=.*$'
    if ([regex]::Matches($config,$pattern).Count -ne 1) { throw "Expected exactly one SEM setting: $key" }
    $config = [regex]::Replace($config,$pattern,('${1}'+$key+' = '+$settings[$key]))
}
$stamp = Get-Date -Format 'yyyyMMdd-HHmmss'
Copy-Item -LiteralPath $configPath -Destination "$configPath.pre-balance-$stamp.bak"
foreach ($old in Get-ChildItem -LiteralPath $modsRoot -Filter 'exodus-*.jar' -File) {
    # Each resolved target is verified inside the explicitly named mods folder.
    if ([System.IO.Path]::GetDirectoryName($old.FullName) -ne $modsRoot) { throw 'Unsafe mod target.' }
    Move-Item -LiteralPath $old.FullName -Destination ($old.FullName+".pre-balance-$stamp.bak")
}
Copy-Item -LiteralPath $jar -Destination (Join-Path $modsRoot "exodus-$version.jar")
Set-Content -LiteralPath $configPath -Value $config -Encoding utf8
$builtHash=(Get-FileHash -LiteralPath $jar -Algorithm SHA256).Hash
$installedHash=(Get-FileHash -LiteralPath (Join-Path $modsRoot "exodus-$version.jar") -Algorithm SHA256).Hash
if ($builtHash -ne $installedHash) { throw 'Installed JAR hash mismatch.' }
Write-Output "Installed Exodus $version; SEM detection=80, shoot=70, gun=.02, ammo=1. Restart Minecraft to load. SHA256=$installedHash"
