# Nexus Verification & Helper Script for Windows PowerShell
param (
    [string]$NexusUrl = "http://localhost:8081",
    [string]$ContainerName = "ntg-nexus"
)

Write-Host "==================================================" -ForegroundColor Cyan
Write-Host "   NTG Nexus Hosted Repository Setup Helper       " -ForegroundColor Cyan
Write-Host "==================================================" -ForegroundColor Cyan

# 1. Check if Docker container is running
Write-Host "`n[1/3] Checking Nexus Docker container ($ContainerName)..." -ForegroundColor Yellow
$containerStatus = docker ps --filter "name=$ContainerName" --format "{{.Status}}" 2>$null

if ($containerStatus) {
    Write-Host "Nexus container is running: $containerStatus" -ForegroundColor Green
} else {
    Write-Host "Nexus container is NOT running." -ForegroundColor Red
    Write-Host "To start: docker compose up -d nexus" -ForegroundColor Yellow
}

# 2. Check initial admin password if file exists
Write-Host "`n[2/3] Checking initial admin password..." -ForegroundColor Yellow
try {
    $adminPassword = docker exec $ContainerName cat /nexus-data/admin.password 2>$null
    if ($adminPassword) {
        Write-Host "Found initial admin password: $adminPassword" -ForegroundColor Green
        Write-Host "Use this password to login at $NexusUrl and complete setup." -ForegroundColor Cyan
    } else {
        Write-Host "Initial password file no longer exists (password was already configured)." -ForegroundColor Gray
    }
} catch {
    Write-Host "Could not query admin password from container." -ForegroundColor Gray
}

# 3. Check Nexus HTTP API health
Write-Host "`n[3/3] Checking Nexus HTTP endpoint ($NexusUrl)..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "$NexusUrl/service/rest/v1/status" -TimeoutSec 5 -UseBasicParsing 2>$null
    if ($response.StatusCode -eq 200) {
        Write-Host "Nexus is UP and HEALTHY! (Status: 200 OK)" -ForegroundColor Green
    } else {
        Write-Host "Nexus returned HTTP status: $($response.StatusCode)" -ForegroundColor Yellow
    }
} catch {
    Write-Host "Nexus HTTP endpoint is not reachable yet at $NexusUrl." -ForegroundColor Red
    Write-Host "Nexus may still be starting up. Please allow 60-90 seconds." -ForegroundColor Gray
}

Write-Host "`nUseful commands:" -ForegroundColor Cyan
Write-Host "  - Deploy library: cd employee-library; mvn clean deploy -s ../nexus/settings.xml" -ForegroundColor White
Write-Host "  - View Nexus logs: docker logs -f $ContainerName" -ForegroundColor White
Write-Host "  - Browse repositories: $NexusUrl/#browse/browse:maven-releases" -ForegroundColor White
