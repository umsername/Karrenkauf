# Implementation Summary: Vue.js Frontend Backend Integration

## Übersicht

Diese Implementierung erweitert die Karrenkauf-Einkaufslisten-App um Multi-User-Support, Authentifizierung und Synchronisation zwischen Frontend und Backend.

## Implementierte Features

### 1. Datenmodell-Erweiterungen

#### Neue Felder in `Einkaufsliste`:
```javascript
{
  // Bestehende Felder
  id: string,
  name: string,
  owner: string,  // Legacy, für Kompatibilität
  createdAt: timestamp,
  updatedAt: timestamp,
  items: array,
  
  // Neue Felder
  ownerId: string,              // ID des Besitzers
  sharedWithUserIds: array,     // Array von User-IDs für geteilte Listen
  lastModifiedTimestamp: number,// Unix-Timestamp für Sync-Vergleich
  version: number,              // Versionsnummer für Konfliktauflösung
  searchIndex: array            // Optimierter Suchindex (Array von Strings)
}
```

#### Migration & Kompatibilität
- Automatische Migration beim App-Start via `migrateListsToNewFormat()`
- Alle bestehenden Listen erhalten die neuen Felder mit Standardwerten
- Keine Breaking Changes für bestehende Daten

### 2. Authentifizierung (`authService.js`)

#### Funktionen:
- `login(username, password)` - Login mit JWT-Token-Erhalt
- `logout()` - Token aus LocalStorage entfernen
- `getToken()` - Aktuellen Token abrufen
- `getCurrentUser()` - Eingeloggten User abrufen
- `isAuthenticated()` - Prüfung ob User eingeloggt
- `checkStatus()` - Token-Validierung beim Backend

#### Token-Storage:
- JWT-Token: `karrenkauf_auth_token`
- User-Info: `karrenkauf_user`
- LocalStorage für persistente Sessions

#### Backend-Endpoints:
- `POST /api/login` - Authentifizierung
- `GET /api/status?token=xxx` - Token-Validierung

### 3. API-Service (`apiService.js`)

#### Axios-Konfiguration:
- Base URL: `VITE_API_BASE_URL` (default: `http://localhost:8080`)
- Request Interceptor: Fügt JWT-Token zu allen Requests hinzu
- Response Interceptor: Behandelt 401 Unauthorized Errors

#### API-Methoden:
- `fetchAllLists()` - GET /api/lists
- `fetchList(listId)` - GET /api/lists/{id}
- `syncListsToBackend(listsData)` - POST /api/lists/sync

### 4. Synchronisations-Service (`syncService.js`)

#### Haupt-Funktion: `syncLists()`

**Ablauf:**
1. Backend-Listen laden
2. Mit LocalStorage vergleichen
3. Konfliktauflösung:
   - **Version-Priorität**: Höhere Version gewinnt
   - **Timestamp-Fallback**: Bei gleicher Version entscheidet Timestamp
4. Lokale Updates zum Backend senden
5. LocalStorage aktualisieren

**Rückgabewert:**
```javascript
{
  success: boolean,
  message: string,
  updates: {
    fromBackend: number,  // Anzahl aktualisierter Listen
    toBackend: number     // Anzahl hochgeladener Listen
  }
}
```

#### Zusatzfunktionen:
- `pushAllListsToBackend()` - Erzwingt Upload aller lokalen Listen
- `pullAllListsFromBackend(overwrite)` - Erzwingt Download aller Backend-Listen

#### Konfliktauflösung-Logik:
```javascript
// Backend gewinnt wenn:
backendVersion > localVersion || 
(backendVersion === localVersion && backendTimestamp > localTimestamp)

// Lokal gewinnt wenn:
localVersion > backendVersion ||
(localVersion === backendVersion && localTimestamp > backendTimestamp)
```

### 5. Suchfunktionalität

#### Suchindex-System:

**Storage-Format:**
- Im JSON/LocalStorage: `searchIndex: ["milch", "butter", "käse", ...]`
- Zur Laufzeit: `Set<string>` für Performance

**Funktionen in `dataStore.js`:**
- `searchIndexSetToArray(indexSet)` - Konvertierung Set → Array
- `searchIndexArrayToSet(indexArray)` - Konvertierung Array → Set
- `updateSearchIndex(listId)` - Index neu erstellen
- `searchItemsInList(listId, searchTerm)` - Items filtern
- `initializeAllSearchIndexes()` - Migration für alle Listen

**Index-Aufbau:**
- Normalisierung: `toLowerCase().trim()`
- Ganzer Item-Name wird hinzugefügt
- Einzelne Wörter werden separiert und hinzugefügt
- Automatische Aktualisierung bei Item-Änderungen

**UI-Integration:**
- Suchmodal in Listendetailansicht
- Live-Filterung während der Eingabe
- Ergebniszähler
- "Filter zurücksetzen"-Button

### 6. UI-Updates

#### Login.vue
- Fehlerbehandlung mit visuellen Feedback
- Loading-State während Login
- Error-Messages in Rot
- Disabled-State für Buttons während Loading

#### Home.vue
- **Synchronisieren-Button** in Übersicht
  - Nur sichtbar wenn eingeloggt
  - Loading-Animation (rotierendes Icon)
  - Status-Message nach Sync
- **Such-Button** in Detailansicht
  - Öffnet Suchmodal
  - Live-Filterung der Tabelle

#### App.vue
- Auto-Sync beim App-Start
- Nur wenn User authentifiziert
- Console-Logging für Debug-Zwecke

### 7. CSS-Erweiterungen

**Neue Styles:**
- `.error-message` - Fehleranzeige in Auth-Forms
- `.sync-message` - Sync-Status-Nachricht
- `.spinning` - Rotation-Animation für Sync-Icon
- `.search-result-info` - Suchergebnis-Zähler
- Disabled-States für Buttons

## Datei-Struktur

```
Frontend/
├── src/
│   ├── services/
│   │   ├── authService.js       # Authentifizierung
│   │   ├── apiService.js        # API-Client
│   │   └── syncService.js       # Synchronisations-Logik
│   ├── store/
│   │   └── dataStore.js         # Erweitertes Datenmodell
│   ├── views/
│   │   ├── Login.vue            # Update mit Auth-Logic
│   │   └── Home.vue             # Sync & Such-UI
│   ├── styles/
│   │   └── main.css             # Neue CSS-Styles
│   └── App.vue                  # Auto-Sync Integration
├── .env.example                 # API-Konfiguration
├── TESTING.md                   # Test-Guide
└── README.md                    # Feature-Dokumentation
```

## Verwendung

### Konfiguration

1. `.env` Datei erstellen (basierend auf `.env.example`):
```env
VITE_API_BASE_URL=http://localhost:8080
```

2. Dependencies installieren:
```bash
npm install
```

3. Entwicklungsserver starten:
```bash
npm run dev
```

### Programmablauf

1. **App-Start:**
   - LocalStorage wird geladen
   - Migration auf neues Datenformat läuft automatisch
   - Wenn eingeloggt: Auto-Sync startet

2. **Login:**
   - User gibt Credentials ein
   - Token wird empfangen und gespeichert
   - Weiterleitung zur Startseite
   - Auto-Sync wird ausgelöst

3. **Liste erstellen:**
   - Neue Liste erhält alle neuen Felder
   - `version: 1` initial
   - `searchIndex: []` initial

4. **Items hinzufügen/bearbeiten:**
   - Timestamps werden aktualisiert
   - Version wird inkrementiert
   - Suchindex wird neu erstellt

5. **Synchronisation:**
   - Manuell via Button oder automatisch
   - Bidirektionaler Abgleich
   - Konfliktauflösung via Version/Timestamp

6. **Suche:**
   - Suchmodal öffnen
   - Eingabe triggert Live-Filterung
   - Nur passende Items werden angezeigt

## Offline-Modus

- App funktioniert weiterhin offline
- Änderungen werden lokal gespeichert
- Bei nächster Verbindung: Automatischer Sync
- Lokale Änderungen werden hochgeladen

## Sicherheit

### Durchgeführte Checks:
- ✅ CodeQL Security Scan: Keine Vulnerabilities
- ✅ Code Review: Feedback adressiert
- ✅ Build erfolgreich

### Best Practices:
- JWT-Token in LocalStorage (akzeptabel für diese App-Größe)
- Request-Interceptor für konsistente Auth-Header
- Fehlerbehandlung bei API-Calls
- Input-Validation in Login-Form

## Bekannte Limitierungen

1. **Keine UI für Listen teilen**: Datenmodell vorbereitet, aber UI fehlt noch
2. **Timestamp-Konflikte**: Bei exakt gleicher Version und Timestamp gewinnt Backend
3. **Keine Benutzer-Registrierung**: Nur Login, Registrierung muss separat implementiert werden
4. **LocalStorage-Limit**: Bei sehr vielen Listen könnte Limit erreicht werden

## Nächste Schritte (Optional)

1. **Sharing-UI implementieren**: Benutzer zu `sharedWithUserIds` hinzufügen
2. **Registrierungs-Flow**: Register.vue mit Backend verbinden
3. **Profil-Seite**: User-Einstellungen, geteilte Listen anzeigen
4. **Erweiterte Konfliktauflösung**: 3-Way-Merge, Conflict-UI
5. **Offline-Indicator**: Zeige Verbindungsstatus an
6. **Batch-Operations**: Mehrere Listen gleichzeitig bearbeiten/teilen

## Testing

Siehe `TESTING.md` für detaillierte Test-Szenarien.

**Empfohlene manuelle Tests:**
1. Login-Flow mit gültigen/ungültigen Credentials
2. Auto-Sync beim Neu-Laden
3. Offline-Änderungen → Online-Sync
4. Suche mit verschiedenen Begriffen
5. Migration alter Daten

## Performance

- Suchindex als Set: O(1) Lookup
- Lazy Migration: Nur beim Laden
- Minimale Daten-Transfers bei Sync
- Optimistic UI Updates

## Kompatibilität

- ✅ Moderne Browser (Chrome, Firefox, Safari, Edge)
- ✅ Mobile Browser
- ✅ Rückwärtskompatibel mit alten Daten
- ✅ Funktioniert auch ohne Backend (Offline-Modus)
