# Testing Guide für Karrenkauf Frontend

## Vorbereitung

1. Backend starten (Java Spring Boot auf Port 8080)
2. Frontend starten: `npm run dev`
3. Browser öffnen auf `http://localhost:5173`

## Test-Szenarien

### 1. Authentifizierung testen

#### Login-Test
1. Navigiere zu `/login`
2. Gebe gültige Credentials ein (z.B. aus Backend-Datenbank)
3. Klicke auf "Login"
4. **Erwartetes Ergebnis**: 
   - Weiterleitung zur Startseite
   - Token wird im LocalStorage gespeichert
   - Sync-Button erscheint in der Übersicht

#### Fehlerhafte Login-Versuche
1. Ungültiger Benutzername/Passwort
2. **Erwartetes Ergebnis**: Fehlermeldung wird angezeigt

#### Token-Validierung
1. Nach Login die Seite neu laden
2. **Erwartetes Ergebnis**: Benutzer bleibt eingeloggt, Auto-Sync startet

### 2. Datenmodell & Migration testen

#### Neue Liste erstellen
1. Klicke auf "Neue Liste"
2. Gebe einen Namen ein
3. **Erwartetes Ergebnis**: Liste wird mit allen neuen Feldern erstellt:
   - `ownerId`
   - `sharedWithUserIds: []`
   - `lastModifiedTimestamp`
   - `version: 1`
   - `searchIndex: []`

#### Migration testen
1. Importiere alte Listen (aus example.json oder alte Backups)
2. **Erwartetes Ergebnis**: Alte Listen erhalten automatisch die neuen Felder

### 3. Synchronisation testen

#### Auto-Sync beim Start
1. Einloggen
2. Seite neu laden
3. **Erwartetes Ergebnis**: 
   - Console zeigt "Auto-Sync wird gestartet..."
   - Listen werden vom Backend geladen

#### Manueller Sync
1. Klicke auf "Synchronisieren"-Button
2. **Erwartetes Ergebnis**:
   - Button zeigt "Synchronisiere..." mit drehendem Icon
   - Nach Abschluss: Erfolgsmeldung wird angezeigt
   - Meldung verschwindet nach 5 Sekunden

#### Konfliktauflösung
1. Ändere eine Liste lokal (z.B. Item hinzufügen)
2. Ändere dieselbe Liste im Backend (direkter DB-Zugriff)
3. Klicke auf "Synchronisieren"
4. **Erwartetes Ergebnis**: Neuere Version (basierend auf Timestamp) gewinnt

#### Offline-Modus
1. Backend stoppen
2. Lokale Änderungen vornehmen
3. Auf "Synchronisieren" klicken
4. **Erwartetes Ergebnis**: Fehlermeldung "Synchronisation fehlgeschlagen"
5. Backend wieder starten
6. Erneut synchronisieren
7. **Erwartetes Ergebnis**: Lokale Änderungen werden zum Backend gesendet

### 4. Suchfunktion testen

#### Suchindex-Aufbau
1. Erstelle eine Liste mit mehreren Items
2. Öffne Browser DevTools > Application > LocalStorage
3. Prüfe die Liste im JSON
4. **Erwartetes Ergebnis**: `searchIndex` Array enthält normalisierte Item-Namen und Wörter

#### Live-Suche
1. Öffne eine Liste mit vielen Items
2. Klicke auf "Suchen"-Button
3. Gebe einen Suchbegriff ein (z.B. "Milch")
4. **Erwartetes Ergebnis**:
   - Tabelle filtert Items live während der Eingabe
   - Ergebniszähler zeigt korrekte Anzahl
   - Items ohne Match verschwinden

#### Suchindex-Update
1. Füge ein neues Item hinzu
2. Prüfe LocalStorage
3. **Erwartetes Ergebnis**: `searchIndex` wurde automatisch aktualisiert
4. Bearbeite ein Item (Name ändern)
5. **Erwartetes Ergebnis**: Index wurde neu erstellt
6. Lösche ein Item
7. **Erwartetes Ergebnis**: Index wurde aktualisiert

#### Suche zurücksetzen
1. Führe eine Suche durch
2. Klicke "Filter zurücksetzen"
3. **Erwartetes Ergebnis**: Alle Items werden wieder angezeigt

### 5. Multi-User Features

#### Listen teilen (wenn Backend unterstützt)
1. Erstelle eine Liste
2. Prüfe `ownerId` im LocalStorage
3. Füge User-IDs zu `sharedWithUserIds` hinzu (manuell oder über UI, falls implementiert)
4. **Erwartetes Ergebnis**: Felder werden korrekt gespeichert

## Browser-Kompatibilität testen

- [ ] Chrome/Edge
- [ ] Firefox
- [ ] Safari (wenn verfügbar)
- [ ] Mobile Browser (Chrome Mobile, Safari iOS)

## Performance-Tests

1. Erstelle viele Listen (50+)
2. Füge viele Items zu einer Liste hinzu (100+)
3. **Erwartetes Ergebnis**: 
   - Suche bleibt schnell
   - Synchronisation funktioniert
   - UI bleibt responsiv

## Bekannte Einschränkungen

- Sync-Konflikte: Derzeit gewinnt die neueste Version (Backend oder lokal)
- Keine UI für das Teilen von Listen (nur Datenmodell vorbereitet)
- Keine Benutzer-Registrierung im Frontend

## Fehlersuche

### Sync funktioniert nicht
1. Prüfe Browser Console auf Fehler
2. Prüfe ob Backend läuft (`http://localhost:8080/api/status`)
3. Prüfe Token im LocalStorage (`karrenkauf_auth_token`)
4. Prüfe Network-Tab in DevTools

### Suchindex fehlt
1. Öffne Browser Console
2. Führe aus: `localStorage.getItem('shoppingData')`
3. Prüfe ob `searchIndex` vorhanden ist
4. Falls nicht: Seite neu laden (Migration sollte greifen)

### Migration funktioniert nicht
1. Öffne Browser Console
2. Lösche LocalStorage: `localStorage.clear()`
3. Seite neu laden
4. Importiere alte Daten
