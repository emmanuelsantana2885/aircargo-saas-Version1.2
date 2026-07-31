#!/usr/bin/env bash
set -euo pipefail

# ── Load secrets from gitignored .env ───────────────────────────
AIR_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
. "$AIR_ROOT/aircargo-env.sh"

echo "🛑 Deteniendo procesos existentes..."
pkill -f "java -jar.*aircargo.*service" 2>/dev/null || true
pkill -f "vite" 2>/dev/null || true
sleep 2

echo "🏗️ Construyendo backend..."
declare -a services=(
    backend/aircargo-auth-service
    backend/aircargo-flight-service
    backend/aircargo-booking-service
    backend/aircargo-mawb-service
    backend/aircargo-warehouse-service
    backend/aircargo-uld-service
    backend/aircargo-load-planning-service
    backend/aircargo-export-service
    backend/aircargo-notification-service
    backend/aircargo-gateway
)

for dir in "${services[@]}"; do
    [ -d "$dir" ] || { echo "❌ El directorio $dir no existe"; exit 1; }
    name=$(basename "$dir")
    echo "  → Construyendo $name"
    (cd "$dir" && mvn clean package -DskipTests -q) || {
        echo "❌ La compilación de $name ha fallado."
        exit 1
    }
done

echo "🚀 Iniciando todos los servicios backend..."
for dir in "${services[@]}"; do
    name=$(basename "$dir")
    echo "  → Iniciando $name"
    (cd "$dir" && \
         java -jar "target/${name}-1.2.0-SNAPSHOT.jar" >> "/tmp/${name}.log" 2>&1) &
    echo "    [PID $(pgrep -f "${name}.*jar" | head -1 || echo '??')] -> /tmp/${name}.log"
done

echo "⏳ Esperando Gateway..."
start_time=$(date +%s)
until curl -s http://localhost:8080/actuator/health | grep -q '"status":"UP"'; do
    sleep 2
    now=$(date +%s)
    if (( now - start_time > 90 )); then
        echo "❌ ERROR: Gateway no responde tras 90s."
        tail -n 30 /tmp/aircargo-gateway.log 2>/dev/null || echo "   (no logs disponibles)"
        exit 1
    fi
    echo -n "."
done
echo ""
elapsed=$(($(date +%s) - start_time))
echo "✅ Gateway UP (${elapsed}s)"

echo "🌐 Iniciando frontend (Vite)..."
cd /home/manolov/Desktop/Projects/Rannik/aircargo-saas-Version1.2/frontend && npm run dev &
echo "   → Vite corriendo (puerto 5173)"

echo ""
echo "🎉 ✅ TODO EN MARCHE !"
echo "   📡 Backend  : http://localhost:8080"
echo "   🌐 Frontend : http://localhost:5173"
echo ""
echo "🛠️  Utilitaires rápidos:"
echo "   • restart-all   -> pkill -f 'java -jar.*aircargo.*service' && pkill vite && ./start-all.sh"
echo "   • tail-logs     -> tail -f /tmp/(gateway|auth|flight|booking|mawb|warehouse|uld|load-planning|export|notification).log"
