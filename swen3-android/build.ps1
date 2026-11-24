param(
    [string]$Sdk = $env:ANDROID_HOME,
    [string]$Jdk = $env:JAVA_HOME
)
$ErrorActionPreference = 'Stop'
$PSNativeCommandUseErrorActionPreference = $true
if (-not $Sdk) { $Sdk = Join-Path $env:LOCALAPPDATA 'Android\Sdk' }
if (-not $Jdk) { throw 'Set JAVA_HOME or pass -Jdk with the path to a JDK (17 or newer).' }
$tools = Join-Path $Sdk 'build-tools\36.0.0'
$androidJar = Join-Path $Sdk 'platforms\android-33\android.jar'
$env:JAVA_HOME = $Jdk
$env:Path = "$(Join-Path $Jdk 'bin');$env:Path"
$build = Join-Path $PSScriptRoot 'build'
$classes = Join-Path $build 'classes'
New-Item -Force -ItemType Directory $build,$classes | Out-Null
$manifest = Join-Path $PSScriptRoot 'AndroidManifest.xml'
$unsigned = Join-Path $build 'unsigned.apk'
$aligned = Join-Path $build 'aligned.apk'
$apk = Join-Path $build 'Paperless-SWEN3.apk'
$key = Join-Path $build 'debug.keystore'

function Invoke-BuildTool([string]$Exe, [string[]]$ToolArgs) {
    & $Exe @ToolArgs
    if ($LASTEXITCODE -ne 0) { throw "$Exe exited with code $LASTEXITCODE" }
}

$sources = Get-ChildItem (Join-Path $PSScriptRoot 'src') -Filter '*.java' -Recurse
Invoke-BuildTool 'javac.exe' (@('--release','8','-encoding','UTF-8','-classpath',$androidJar,'-d',$classes) + $sources.FullName)
$bytecode = Get-ChildItem $classes -Filter '*.class' -Recurse
Invoke-BuildTool (Join-Path $tools 'd8.bat') (@('--min-api','24','--lib',$androidJar,'--output',$build) + $bytecode.FullName)
$resources = Join-Path $build 'resources.zip'
$aapt2 = Join-Path $tools 'aapt2.exe'
Invoke-BuildTool $aapt2 @('compile','--dir',(Join-Path $PSScriptRoot 'res'),'-o',$resources)
Invoke-BuildTool $aapt2 @('link','-I',$androidJar,'--manifest',$manifest,'-o',$unsigned,$resources)
Push-Location $build
try {
    Invoke-BuildTool (Join-Path $tools 'aapt.exe') @('add',$unsigned,'classes.dex')
} finally {
    Pop-Location
}
Invoke-BuildTool (Join-Path $tools 'zipalign.exe') @('-f','4',$unsigned,$aligned)
if (-not (Test-Path $key)) {
    Invoke-BuildTool 'keytool.exe' @('-genkeypair','-noprompt','-keystore',$key,'-storepass','android',
        '-keypass','android','-alias','androiddebugkey','-keyalg','RSA','-keysize','2048','-validity','10000',
        '-dname','CN=SWEN3 Debug')
}
Invoke-BuildTool (Join-Path $tools 'apksigner.bat') @('sign','--ks',$key,'--ks-pass','pass:android','--out',$apk,$aligned)
Invoke-BuildTool (Join-Path $tools 'apksigner.bat') @('verify',$apk)
Write-Output $apk
