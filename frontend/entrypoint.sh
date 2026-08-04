#!/bin/sh
set -e

# ─── Aircargo — frontend nginx entrypoint ───
# Generates the nginx config at startup:
#   • HTTPS mode:  when $DOMAIN is set AND a Let's Encrypt certificate exists
#                  at /etc/letsencrypt/live/$DOMAIN (mount /etc/letsencrypt).
#                  HTTP :80 serves ACME challenges and redirects to HTTPS.
#   • HTTP mode:   otherwise (fresh EC2 / no cert yet) — app works on :80.
# This lets the same image boot on a new instance without any TLS setup.

DOMAIN="${DOMAIN:-}"
CONF="/etc/nginx/conf.d/default.conf"

gen_http() {
cat > "$CONF" <<'NGINX'
server {
    listen 80;
    server_name _;

    root /usr/share/nginx/html;
    index index.html;

    gzip on;
    gzip_types text/plain text/css application/json application/javascript text/xml application/xml application/xml+rss text/javascript image/svg+xml;
    gzip_min_length 256;

    location /api/ {
        proxy_pass http://aircargo-gateway:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_read_timeout 120s;
        proxy_connect_timeout 10s;
    }

    location /assets/ {
        expires 1y;
        add_header Cache-Control "public, immutable";
    }

    location / {
        try_files $uri $uri/ /index.html;
    }
}
NGINX
}

gen_https() {
    local domain="$1"
cat > "$CONF" <<NGINX
server {
    listen 80;
    server_name _;

    location /.well-known/acme-challenge/ { root /usr/share/nginx/html; }

    location / {
        return 301 https://\$host\$request_uri;
    }
}

server {
    listen 443 ssl;
    http2 on;
    server_name _;

    ssl_certificate     /etc/letsencrypt/live/${domain}/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/${domain}/privkey.pem;
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers HIGH:!aNULL:!MD5;
    ssl_prefer_server_ciphers on;

    root /usr/share/nginx/html;
    index index.html;

    gzip on;
    gzip_types text/plain text/css application/json application/javascript text/xml application/xml application/xml+rss text/javascript image/svg+xml;
    gzip_min_length 256;

    location /api/ {
        proxy_pass http://aircargo-gateway:8080;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
        proxy_read_timeout 120s;
        proxy_connect_timeout 10s;
    }

    location /assets/ {
        expires 1y;
        add_header Cache-Control "public, immutable";
    }

    location / {
        try_files \$uri \$uri/ /index.html;
    }
}
NGINX
}

if [ -n "$DOMAIN" ] \
   && [ -f "/etc/letsencrypt/live/$DOMAIN/fullchain.pem" ] \
   && [ -f "/etc/letsencrypt/live/$DOMAIN/privkey.pem" ]; then
    echo "aircargo-web: HTTPS enabled for ${DOMAIN}"
    gen_https "$DOMAIN"
else
    echo "aircargo-web: serving HTTP only (DOMAIN=${DOMAIN:-unset})"
    gen_http
fi

exec "$@"
