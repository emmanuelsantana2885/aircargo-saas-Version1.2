#!/usr/bin/env bash
set -euo pipefail

# =============================================================================
# 🛑 STOP EVERYTHING RUNNING — force an exec clean slate
# =============================================================================
echo "[🛑 Stopping any existing Java/Vite processes...]"
pkill -f "java -jar.*aircargo.*service" 2>/dev/null || true
pkill -f "vite" 2>/dev/null || true
sleep 2

# =============================================================================
# 🚀 BUILD every backend module
# =============================================================================
echo "[🏗️  Building all backend modules ...]"

for dir in backend/aircargo-gateway \
           backend/aircargo-auth-service \
           backend/aircargo-flight-service \
           backend/aircargo-booking-service \
           backend/aircargo-mawb-service \
           backend/aircargo-warehouse-service \
           backend/aircargo-uld-service \
           backend/aircargo-load-planning-service \
           backend/aircargo-export-service \
           backend/aircargo-notification-service; do
    echo "  → $dir"
    (cd "$dir" && mvn clean compile -DskipTests -q) || {
        echo "❌ Maven build failed in $dir. Aborting."
        exit 1
    }
done

# =============================================================================
# 🎮 START all backend services in background
# =============================================================================
echo "[▶️  Launching all backend services ...]"

for dir in backend/aircargo-gateway \
           backend/aircargo-auth-service \
           backend/aircargo-flight-service \
           backend/aircargo-booking-service \
           backend/aircargo-mawb-service \
           backend/aircargo-warehouse-service \
           backend/aircargo-uld-service \
           backend/aircargo-load-planning-service \
           backend/aircargo-export-service \
           backend/aircargo-notification-service; do

    name=$(basename "$dir")
    echo "  → Starting $name"
    cd "$dir" && java -jar "target/${name}-1.2.0-SNAPSHOT.jar" >> "/tmp/${name}.log" 2>&1 &
    echo "    [PID $(pgrep -f "${name}.*jar" | head -1 || echo '??')] -> /tmp/${name}.log"
done

# =============================================================================
# ⌛ WAIT for Gateway actuator to report UP
# =============================================================================
echo "[⏳ Waiting for Gateway actuator /actuator/health (timeout 120s) ...]"

start_time=$(date +%s)
while ! curl -s http://localhost:8080/actuator/health | grep -q '"status":"UP"'; do
    elapsed=$(( $(date +%s) - start_time ))
    if [ $elapsed -gt 120 ]; then
        echo "❌ ERROR: Gateway did not become healthy after 120s."
        exit 1
    fi
    sleep 1
done

echo "✅ All backend services are healthy (Gateway took ${elapsed}s)."
