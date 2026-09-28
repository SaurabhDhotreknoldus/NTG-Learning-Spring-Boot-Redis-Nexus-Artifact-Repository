#!/usr/bin/env bash
# Nexus Verification & Helper Script for Linux / macOS / WSL

set -e

NEXUS_URL="${1:-http://localhost:8081}"
CONTAINER_NAME="${2:-ntg-nexus}"

echo "=================================================="
echo "   NTG Nexus Hosted Repository Setup Helper       "
echo "=================================================="

echo ""
echo "[1/3] Checking Nexus Docker container ($CONTAINER_NAME)..."
if docker ps --filter "name=$CONTAINER_NAME" --format '{{.Names}}' | grep -q "$CONTAINER_NAME"; then
    echo "Nexus container is running."
else
    echo "Nexus container is NOT running."
    echo "To start: docker compose up -d nexus"
fi

echo ""
echo "[2/3] Checking initial admin password..."
if docker exec "$CONTAINER_NAME" test -f /nexus-data/admin.password 2>/dev/null; then
    ADMIN_PASS=$(docker exec "$CONTAINER_NAME" cat /nexus-data/admin.password)
    echo "Found initial admin password: $ADMIN_PASS"
    echo "Use this password to login at $NEXUS_URL and complete setup."
else
    echo "Initial password file no longer exists (password already changed)."
fi

echo ""
echo "[3/3] Checking Nexus HTTP endpoint ($NEXUS_URL)..."
HTTP_STATUS=$(curl -s -o /dev/null -w "%{http_code}" "$NEXUS_URL/service/rest/v1/status" || echo "000")
if [ "$HTTP_STATUS" = "200" ]; then
    echo "Nexus is UP and HEALTHY! (Status: 200 OK)"
else
    echo "Nexus HTTP endpoint returned status: $HTTP_STATUS"
    echo "Nexus may still be initializing. Wait ~60 seconds."
fi

echo ""
echo "Useful commands:"
echo "  - Deploy library: (cd employee-library && mvn clean deploy -s ../nexus/settings.xml)"
echo "  - View Nexus logs: docker logs -f $CONTAINER_NAME"
echo "  - Browse repositories: $NEXUS_URL/#browse/browse:maven-releases"
