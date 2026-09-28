param (
    [string]$BaseUrl = "http://localhost:8080"
)

Write-Host "======================================================================" -ForegroundColor Cyan
Write-Host "   SPRING BOOT + REDIS CACHING DEMONSTRATION & BENCHMARK SUITE       " -ForegroundColor Cyan
Write-Host "======================================================================" -ForegroundColor Cyan

# 1. Clear Cache
Write-Host "`n[Step 0] Resetting cache state (POST $BaseUrl/api/employees/cache/clear)..." -ForegroundColor Yellow
try {
    $clearResp = Invoke-RestMethod -Uri "$BaseUrl/api/employees/cache/clear" -Method Post
    Write-Host "Cache cleared successfully: $($clearResp.message)" -ForegroundColor Green
} catch {
    Write-Host "Error connecting to application at $BaseUrl. Ensure employee-redis-app is running." -ForegroundColor Red
    exit 1
}

# 2. Cache Miss Demonstration
Write-Host "`n[Step 1] First Read: Demonstrating @Cacheable CACHE MISS (Hits Database)..." -ForegroundColor Yellow
$sw1 = [System.Diagnostics.Stopwatch]::StartNew()
$read1 = Invoke-RestMethod -Uri "$BaseUrl/api/employees/1" -Method Get
$sw1.Stop()
$missTime = $sw1.Elapsed.TotalMilliseconds

Write-Host "  Response: $($read1.data.fullName) | Department: $($read1.data.department) | Salary: `$$($read1.data.salary)" -ForegroundColor White
Write-Host "  Latency (Cache Miss / Database Query): $($missTime.ToString('F2')) ms" -ForegroundColor Magenta

# 3. Cache Hit Demonstration
Write-Host "`n[Step 2] Second Read: Demonstrating @Cacheable CACHE HIT (Served directly from Redis)..." -ForegroundColor Yellow
$sw2 = [System.Diagnostics.Stopwatch]::StartNew()
$read2 = Invoke-RestMethod -Uri "$BaseUrl/api/employees/1" -Method Get
$sw2.Stop()
$hitTime = $sw2.Elapsed.TotalMilliseconds

Write-Host "  Response: $($read2.data.fullName) | Department: $($read2.data.department) | Salary: `$$($read2.data.salary)" -ForegroundColor White
Write-Host "  Latency (Cache Hit / Redis): $($hitTime.ToString('F2')) ms" -ForegroundColor Green

if ($hitTime -gt 0) {
    $speedup = [math]::Round($missTime / $hitTime, 2)
    Write-Host "  Acceleration: Redis cache was $speedup x faster!" -ForegroundColor Cyan
}

# 4. Inspect Redis Key & TTL
Write-Host "`n[Step 3] Inspecting Redis Key and Time-To-Live (TTL)..." -ForegroundColor Yellow
$inspect1 = Invoke-RestMethod -Uri "$BaseUrl/api/employees/cache/inspect/1" -Method Get
Write-Host "  Redis Key: $($inspect1.data.cacheKey)" -ForegroundColor White
Write-Host "  Is Cached in Redis: $($inspect1.data.isCachedInRedis)" -ForegroundColor Green
Write-Host "  Remaining TTL: $($inspect1.data.ttlRemainingSeconds) seconds" -ForegroundColor Cyan

# 5. Demonstrate @CachePut
Write-Host "`n[Step 4] Demonstrating @CachePut: Updating salary in DB and refreshing Redis cache..." -ForegroundColor Yellow
$updatePayload = @{
    firstName = "Alice"
    lastName = "Johnson"
    email = "alice.johnson@nashtechglobal.com"
    department = "ENGINEERING"
    salary = 125000.0
    status = "ACTIVE"
    joiningDate = "2023-01-15"
} | ConvertTo-Json

$putResp = Invoke-RestMethod -Uri "$BaseUrl/api/employees/1" -Method Put -Body $updatePayload -ContentType "application/json"
Write-Host "  Updated Employee: $($putResp.data.fullName) | New Salary: `$$($putResp.data.salary)" -ForegroundColor Green

# 6. Verify Updated Value from Cache
Write-Host "`n[Step 5] Verifying cache refreshed immediately via @CachePut..." -ForegroundColor Yellow
$read3 = Invoke-RestMethod -Uri "$BaseUrl/api/employees/1" -Method Get
Write-Host "  Cached Salary immediately after update: `$$($read3.data.salary)" -ForegroundColor Green
if ($read3.data.salary -eq 125000.0) {
    Write-Host "  SUCCESS: @CachePut kept Redis and Database in perfect sync!" -ForegroundColor Green
}

# 7. Demonstrate @CacheEvict
Write-Host "`n[Step 6] Demonstrating @CacheEvict: Deleting temporary employee..." -ForegroundColor Yellow
# Create temp employee to delete
$tempEmp = @{
    firstName = "Temp"
    lastName = "User"
    email = "temp.user@nashtechglobal.com"
    department = "OPERATIONS"
    salary = 50000.0
    status = "ACTIVE"
} | ConvertTo-Json
$createdTemp = Invoke-RestMethod -Uri "$BaseUrl/api/employees" -Method Post -Body $tempEmp -ContentType "application/json"
$tempId = $createdTemp.data.id

# Read to cache it
$null = Invoke-RestMethod -Uri "$BaseUrl/api/employees/$tempId" -Method Get
$inspectBefore = Invoke-RestMethod -Uri "$BaseUrl/api/employees/cache/inspect/$tempId" -Method Get
Write-Host "  Temp Employee ID $tempId cached in Redis: $($inspectBefore.data.isCachedInRedis)" -ForegroundColor White

# Delete
$delResp = Invoke-RestMethod -Uri "$BaseUrl/api/employees/$tempId" -Method Delete
Write-Host "  Delete Response: $($delResp.message)" -ForegroundColor Yellow

# Inspect after eviction
$inspectAfter = Invoke-RestMethod -Uri "$BaseUrl/api/employees/cache/inspect/$tempId" -Method Get
Write-Host "  Temp Employee ID $tempId in Redis after @CacheEvict: $($inspectAfter.data.isCachedInRedis)" -ForegroundColor Green
if (-not $inspectAfter.data.isCachedInRedis) {
    Write-Host "  SUCCESS: @CacheEvict purged the key from Redis!" -ForegroundColor Green
}

Write-Host "`n======================================================================" -ForegroundColor Cyan
Write-Host "   DEMONSTRATION COMPLETED SUCCESSFULLY!                              " -ForegroundColor Cyan
Write-Host "======================================================================" -ForegroundColor Cyan