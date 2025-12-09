# Initialisierung
In einem Ordner deiner Wahl das **Terminal öffnen** (*Netzlaufwerke* gehen *nicht*!)<br/>
Zuerst mit **git clone https://github.com/umsername/Karrenkauf.git** Projekt downloaden<br/>
Danach mit **cd Karrenkauf/Frontend** in das Projektverzeichniss navigieren. <br/>

Einmal mit **npm install** alle Abhängigkeiten installieren.<br/>
Mit **npm run dev** wird ein Webserver gestartet und ein Localhost Link generiert.<br/>
Das muss jedes mal gemacht werden, wenn das Terminal geschlossen wird.<br/>

# Neue Features

## 🔐 Authentifizierung & Multi-User Support
- Login-System mit JWT-Tokens
- Unterstützung für mehrere Benutzer
- Listen können mit anderen Benutzern geteilt werden (`sharedWithUserIds`)
- Jede Liste hat einen Besitzer (`ownerId`)

## 🔄 Synchronisation
- Automatische Synchronisation beim App-Start (wenn eingeloggt)
- Manuelle Synchronisation über den "Synchronisieren"-Button
- Intelligente Konfliktauflösung basierend auf `lastModifiedTimestamp`
- Versionierung zur Konfliktprävention

## 🔍 Suchfunktionalität
- Schnelle Suche innerhalb von Listen
- Suchindex für optimierte Performance
- Live-Filterung der Items während der Eingabe
- Wortbasierte Suche (einzelne Wörter und ganze Namen)

## 🛠️ Konfiguration

Erstelle eine `.env` Datei im Frontend-Verzeichnis (basierend auf `.env.example`):

```
VITE_API_BASE_URL=http://localhost:8080
```

Passe die Backend-URL nach Bedarf an (z.B. für Produktionsumgebungen).

## 📡 API-Endpunkte

Das Frontend kommuniziert mit folgenden Backend-Endpunkten:

- `POST /api/login` - Benutzer-Login
- `GET /api/status` - Token-Validierung
- `GET /api/lists` - Alle Listen abrufen
- `GET /api/lists/{id}` - Einzelne Liste abrufen
- `POST /api/lists/sync` - Listen synchronisieren

## 📦 Datenmodell

Listen enthalten nun folgende zusätzliche Felder:

```javascript
{
  id: string,
  name: string,
  owner: string,
  ownerId: string,              // NEU: Besitzer-ID
  sharedWithUserIds: array,     // NEU: Liste geteilter User-IDs
  createdAt: timestamp,
  updatedAt: timestamp,
  lastModifiedTimestamp: timestamp, // NEU: Für Synchronisation
  version: number,              // NEU: Für Konfliktauflösung
  items: array,
  searchIndex: array            // NEU: Optimierter Suchindex
}
```

## 🚀 Entwicklung

Die App migriert automatisch alte Datenstrukturen beim ersten Laden. Bestehende Listen im LocalStorage werden mit den neuen Feldern erweitert.


# Vue 3 + Vite

This template should help get you started developing with Vue 3 in Vite. The template uses Vue 3 `<script setup>` SFCs, check out the [script setup docs](https://v3.vuejs.org/api/sfc-script-setup.html#sfc-script-setup) to learn more.

Learn more about IDE Support for Vue in the [Vue Docs Scaling up Guide](https://vuejs.org/guide/scaling-up/tooling.html#ide-support).
