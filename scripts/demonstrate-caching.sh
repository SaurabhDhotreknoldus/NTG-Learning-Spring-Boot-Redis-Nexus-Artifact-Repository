#!/usr/bin/env bash
set -e

BASE_URL="${1:-http://localhost:8080}"

echo "======================================================================"
echo "   SPRING BOOT + REDIS CACHING DEMONSTRATION & BENCHMARK SUITE       "
echo "======================================================================"

echo ""
echo "[Step 0] Resetting cache state (POST $BASE_URL/api/employees/cache/clear)..."
curl -s -X POST "$BASE_URL/api/employees/cache/clear" | grep -o '"message":"[^"]*"' || true

echo ""
echo "[Step 1] First Read: Demonstrating @Cacheable CACHE MISS (Hits Database)..."
START_1=$(date +%s%N)
RESP_1=$(curl -s "$BASE_URL/api/employees/1")
END_1=$(date +%s%N)
DIFF_1=$(( (END_1 - START_1) / 1000000 ))
echo "  Response: $RESP_1"
echo "  Latency (Cache Miss / Database Query): ${DIFF_1} ms"

echo ""
echo "[Step 2] Second Read: Demonstrating @Cacheable CACHE HIT (Served from Redis)..."
START_2=$(date +%s%N)
RESP_2=$(curl -s "$BASE_URL/api/employees/1")
END_2=$(date +%s%N)
DIFF_2=$(( (END_2 - START_2) / 1000000 ))
echo "  Response: $RESP_2"
echo "  Latency (Cache Hit / Redis): ${DIFF_2} ms"

echo ""
echo "[Step 3] Inspecting Redis Key and Time-To-Live (TTL)..."
curl -s "$BASE_URL/api/employees/cache/inspect/1"
echo ""

echo ""
echo "[Step 4] Demonstrating @CachePut: Updating salary in DB and refreshing Redis cache..."
curl -s -X PUT "$BASE_URL/api/employees/1" \
  -H "Content-Type: application/json" \
  -d '{"firstName":"Alice","lastName":"Johnson","email":"alice.johnson@nashtechglobal.com","department":"ENGINEERING","salary":125000.0,"status":"ACTIVE","joiningDate":"2023-01-15"}'
echo ""

echo ""
echo "[Step 5] Verifying cache refreshed immediately via @CachePut..."
curl -s "$BASE_URL/api/employees/1"
echo ""

echo ""
echo "======================================================================"
echo "   DEMONSTRATION COMPLETED SUCCESSFULLY!                              "
echo "======================================================================"