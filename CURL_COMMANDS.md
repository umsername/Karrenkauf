# Quick Test Commands - CURL

Diese Datei enthält sofort verwendbare CURL-Befehle zum Testen der Backend-Endpunkte.

## Voraussetzung
Backend muss laufen auf `http://localhost:8080`

## 1. Health Check

```bash
curl -v http://localhost:8080/api/public/ping
```

**Erwartete Ausgabe:**
```
🟢 Backend is reachable! Server time: 1234567890123
```

---

## 2. Registrierung

```bash
curl -X POST "http://localhost:8080/api/user?username=testuser&password=password123" -v
```

**Erwartete Ausgabe (Erfolg):**
```
👍 User registered successfully!
```

**Erwartete Ausgabe (User existiert):**
```
❌ Username already exists
```

---

## 3. Login

```bash
curl -X POST http://localhost:8080/api/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"password123"}' \
  -v
```

**Erwartete Ausgabe (Erfolg):**
```
👍 Login successful!

TOKEN:
eyJhbGciOiJIUzI1NiJ9...
```

**Erwartete Ausgabe (Fehler - User existiert nicht):**
```
❌ User does not exist
```

**Erwartete Ausgabe (Fehler - Falsches Passwort):**
```
❌ Invalid password
```

---

## 4. Status Check (Token validieren)

Zuerst musst du dich einloggen und den Token aus der Response kopieren.

```bash
# Ersetze YOUR_TOKEN_HERE mit dem tatsächlichen Token
curl -X GET "http://localhost:8080/api/status?token=YOUR_TOKEN_HERE" -v
```

**Erwartete Ausgabe (Token gültig):**
```
Already logged in as: testuser
```

**Erwartete Ausgabe (Token ungültig):**
```
Token unknown → Please login.
```

**Erwartete Ausgabe (Token abgelaufen):**
```
Token expired → Please login again.
```

---

## 5. Kompletter Test-Workflow

```bash
# 1. Health Check
echo "=== Testing Health Check ==="
curl -s http://localhost:8080/api/public/ping
echo -e "\n"

# 2. Registrierung
echo "=== Testing Registration ==="
curl -s -X POST "http://localhost:8080/api/user?username=testuser_$(date +%s)&password=testpass123"
echo -e "\n"

# 3. Login (verwende den User von oben)
echo "=== Testing Login ==="
RESPONSE=$(curl -s -X POST http://localhost:8080/api/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser_REPLACE_WITH_TIMESTAMP","password":"testpass123"}')
echo "$RESPONSE"

# Token extrahieren (wenn Login erfolgreich war)
TOKEN=$(echo "$RESPONSE" | grep -oP "TOKEN:\s*\K.+")
echo -e "\nExtracted Token: $TOKEN"

# 4. Status Check mit Token
echo -e "\n=== Testing Status Check ==="
curl -s "http://localhost:8080/api/status?token=$TOKEN"
echo -e "\n"
```

---

## Tipps

### Verbose Output
Füge `-v` hinzu für detaillierte Request/Response-Informationen:
```bash
curl -v http://localhost:8080/api/public/ping
```

### Silent Mode
Füge `-s` hinzu um Fortschrittsanzeige zu verstecken:
```bash
curl -s http://localhost:8080/api/public/ping
```

### Response Headers anzeigen
```bash
curl -i http://localhost:8080/api/public/ping
```

### Response Zeit messen
```bash
curl -w "\nTime: %{time_total}s\n" -o /dev/null -s http://localhost:8080/api/public/ping
```

---

## Erwartete Backend-Logs

Wenn du diese CURL-Befehle ausführst, solltest du folgende Logs im Backend sehen:

### Für Health Check:
```
🔧 [SECURITY] CORS configured - Origin: null
🔵 [PING] GET /api/public/ping - Request received
```

### Für Registrierung:
```
🔧 [SECURITY] CORS configured - Origin: null
🔵 [REGISTER] Request received - Username: testuser
🔐 [REGISTER] Password hashed successfully
✅ [REGISTER] User registered successfully - ID: <uuid>
```

### Für Login:
```
🔧 [SECURITY] CORS configured - Origin: null
🔵 [LOGIN] Request received - Username: testuser
🔍 [LOGIN] User found - Verifying password...
✅ [LOGIN] Login successful - Token generated for user: testuser
```

### Für Status Check:
```
🔧 [SECURITY] CORS configured - Origin: null
🔵 [STATUS] Request received - Token: eyJhbGciOiJIUzI1NiJ9...
✅ [STATUS] User is logged in: testuser
```

---

## Fehlerdiagnose

### Problem: "curl: (7) Failed to connect"
- Backend läuft nicht
- Lösung: Starte Backend mit `cd Backend && ./mvnw spring-boot:run`

### Problem: "404 Not Found"
- Falscher Endpoint oder Port
- Prüfe: `curl http://localhost:8080/api/public/ping`

### Problem: "500 Internal Server Error"
- Schaue in Backend-Logs für Stack Trace
- Prüfe ob Datenbank korrekt initialisiert wurde

### Problem: Keine Logs im Backend
- Logging ist deaktiviert oder
- Endpoint wird nicht erreicht (CORS-Problem)
- Prüfe: Network-Traffic am Backend
