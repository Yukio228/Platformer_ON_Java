$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$dist = Join-Path $root "dist"
$build = Join-Path $dist "build"
$classes = Join-Path $build "classes"
$jarDir = Join-Path $build "jar"
$jarPath = Join-Path $jarDir "PirateHat.jar"
$portableParent = Join-Path $dist "portable"
$portableApp = Join-Path $portableParent "PirateHat"
$zipPath = Join-Path $dist "PirateHat-portable.zip"
$sourceRoot = Join-Path $root "PirateHat\src"
$resourceRoot = Join-Path $root "PirateHat\res"
$sourceList = Join-Path $build "sources.txt"
$iconPath = Join-Path $dist "PirateHat.ico"
$oldIconPath = Join-Path $portableApp "PirateHat.ico"

New-Item -ItemType Directory -Force $dist | Out-Null
if ((Test-Path $oldIconPath) -and !(Test-Path $iconPath)) {
    Copy-Item -LiteralPath $oldIconPath -Destination $iconPath -Force
}

foreach ($path in @($build, $portableParent, $zipPath)) {
    if (Test-Path $path) {
        $resolved = (Resolve-Path $path).Path
        if (!$resolved.StartsWith($dist, [System.StringComparison]::OrdinalIgnoreCase)) {
            throw "Refusing to delete outside dist: $resolved"
        }
        Remove-Item -LiteralPath $resolved -Recurse -Force
    }
}

New-Item -ItemType Directory -Force $classes, $jarDir, $portableParent | Out-Null

Get-ChildItem -Path $sourceRoot -Recurse -Filter *.java |
    ForEach-Object { $_.FullName } |
    Out-File -Encoding ascii $sourceList

javac -encoding UTF-8 -d $classes "@$sourceList"
if ($LASTEXITCODE -ne 0) {
    throw "javac failed with exit code $LASTEXITCODE"
}

Copy-Item -Path (Join-Path $resourceRoot "*") -Destination $classes -Recurse -Force

jar --create --file $jarPath --main-class main.MainClass -C $classes .
if ($LASTEXITCODE -ne 0) {
    throw "jar failed with exit code $LASTEXITCODE"
}

$jpackageArgs = @(
    "--type", "app-image",
    "--name", "PirateHat",
    "--input", $jarDir,
    "--main-jar", "PirateHat.jar",
    "--main-class", "main.MainClass",
    "--dest", $portableParent,
    "--vendor", "Yukio",
    "--app-version", "1.0.0"
)

if (Test-Path $iconPath) {
    $jpackageArgs += @("--icon", $iconPath)
}

jpackage @jpackageArgs
if ($LASTEXITCODE -ne 0) {
    throw "jpackage failed with exit code $LASTEXITCODE"
}

Compress-Archive -Path $portableApp -DestinationPath $zipPath -Force

Write-Output "Built executable: $portableApp\PirateHat.exe"
Write-Output "Built archive: $zipPath"
