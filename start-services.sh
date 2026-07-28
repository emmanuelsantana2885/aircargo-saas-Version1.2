#!/bin/bash
cd "$(dirname "$0")/backend"

echo "Compilando..."
mvn clean package -DskipTests -q

echo "Arrancando gateway (8080)..."
mvn spring-boot:run -pl aircargo-gateway -am > /tmp/aircargo-gateway.log 2>&1 &
echo $! > /tmp/aircargo-gateway.pid

sleep 2

echo "Arrancando auth-service (9092)..."
mvn spring-boot:run -pl aircargo-auth-service -am > /tmp/aircargo-auth.log 2>&1 &
echo $! > /tmp/aircargo-auth.pid

echo "Arrancando flight-service (9093)..."
mvn spring-boot:run -pl aircargo-flight-service -am > /tmp/aircargo-flight.log 2>&1 &
echo $! > /tmp/aircargo-flight.pid

echo "Arrancando booking-service (9094)..."
mvn spring-boot:run -pl aircargo-booking-service -am > /tmp/aircargo-booking.log 2>&1 &
echo $! > /tmp/aircargo-booking.pid

echo "Arrancando mawb-service (9095)..."
mvn spring-boot:run -pl aircargo-mawb-service -am > /tmp/aircargo-mawb.log 2>&1 &
echo $! > /tmp/aircargo-mawb.pid

echo "Arrancando warehouse-service (9096)..."
mvn spring-boot:run -pl aircargo-warehouse-service -am > /tmp/aircargo-warehouse.log 2>&1 &
echo $! > /tmp/aircargo-warehouse.pid

echo "Arrancando uld-service (9097)..."
mvn spring-boot:run -pl aircargo-uld-service -am > /tmp/aircargo-uld.log 2>&1 &
echo $! > /tmp/aircargo-uld.pid

echo "Arrancando load-planning-service (9098)..."
mvn spring-boot:run -pl aircargo-load-planning-service -am > /tmp/aircargo-load.log 2>&1 &
echo $! > /tmp/aircargo-load.pid

echo "Arrancando export-service (9099)..."
mvn spring-boot:run -pl aircargo-export-service -am > /tmp/aircargo-export.log 2>&1 &
echo $! > /tmp/aircargo-export.pid

echo "Arrancando notification-service (9100)..."
mvn spring-boot:run -pl aircargo-notification-service -am > /tmp/aircargo-notification.log 2>&1 &
echo $! > /tmp/aircargo-notification.pid

echo ""
echo "Todos los servicios arrancados en background."
echo "Logs en /tmp/aircargo-*.log"
echo ""
echo "Para ver el estado:  tail -f /tmp/aircargo-gateway.log"
echo "Para detener:        kill \$(cat /tmp/aircargo-*.pid) 2>/dev/null"
