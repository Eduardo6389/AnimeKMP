$ErrorActionPreference = "Stop"
$destino = Join-Path $PSScriptRoot "..\shared\src\commonTest\resources"
New-Item -ItemType Directory -Force $destino | Out-Null

$respuestas = @(
    @{ Nombre = "top_anime_1.json"; Url = "https://api.jikan.moe/v4/top/anime?page=1" },
    @{ Nombre = "top_anime_2.json"; Url = "https://api.jikan.moe/v4/top/anime?page=2" },
    @{ Nombre = "anime_1_full.json"; Url = "https://api.jikan.moe/v4/anime/1/full" },
    @{ Nombre = "buscar_naruto.json"; Url = "https://api.jikan.moe/v4/anime?q=Naruto&page=1" },
    @{ Nombre = "anime_5114_full.json"; Url = "https://api.jikan.moe/v4/anime/5114/full" }
)

foreach ($respuesta in $respuestas) {
    $archivo = Join-Path $destino $respuesta.Nombre
    Invoke-WebRequest -Uri $respuesta.Url -OutFile $archivo
    Start-Sleep -Milliseconds 600
}

Write-Host "5 respuestas de Jikan guardadas en shared/src/commonTest/resources"
