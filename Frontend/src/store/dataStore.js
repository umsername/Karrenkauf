// später in anderen Projekten importieren mit -- import { createList } from "./dataStore.js";
// Mithilfe von export function -- können wir die Funktionen in anderen Dateien importieren und verwenden
import { reactive } from "vue"

let data = reactive({ lists: {} })
loadData()

// --UTILS--
// Liefert eine UUID 
export function uuid() {
    return crypto.randomUUID();
}

// Liefert die aktuelle Zeit in Millisekunden
export function nowMs() {
    return Date.now();
}


// legt leeres data Objekt an oder lädt bestehendes
loadData();

// Neue Liste erstellen
export function createList(name, owner) {
    const id = uuid(); // Funktion von oben, damit wir eine ID bekommen die random ist
    const timestamp = nowMs();
    
    const list = {
        id,
        name,
        owner,
        ownerId: owner, // Multi-user support: ID des Besitzers
        sharedWithUserIds: [], // Multi-user support: Liste von User-IDs, mit denen geteilt wurde
        createdAt: timestamp,
        updatedAt: timestamp,
        lastModifiedTimestamp: timestamp, // Für Synchronisation
        version: 1, // Für Conflict Resolution
        items: [],
        searchIndex: [] // Suchindex (als Array gespeichert, zur Laufzeit als Set)
    };

    // Liste wird im data Objekt speichern
    data.lists[id] = list;
    saveData();               // ruft saveData Funktion auf um die Daten sofort zu speichern
    return id;               // damit wir wissen, welche Liste gerade erstellt wurde 
}

// Erstellt ein Item für die Liste
export function createItem(name, beschreibung, menge, unit, preis, category) {
    const timestamp= nowMs();
    
    return {
        id: uuid(),
        name,
        beschreibung,
        menge,
        unit,
        preis,
        done: false,
        category,
        checked:false,
        createdAt: timestamp,
        updatedAt: timestamp
    };
}

// Fügt ein Item zu einer Liste hinzu
export function addItemToList(listId, item) {
    const list = data.lists[listId];
    list.items.push(item);
    
    const timestamp = nowMs();
    list.updatedAt = timestamp;
    list.lastModifiedTimestamp = timestamp;
    list.version = (list.version || 0) + 1;
    
    // Suchindex aktualisieren
    updateSearchIndex(listId);
    
    saveData();               // ruft saveData Funktion auf um die Daten sofort zu speichern
}

// Fügt mehrere Items (Array) zu einer Liste hinzu - verwendet dabei addItemToList Funktion
export function addItemsToList(listId, itemsArray) {
    itemsArray.forEach(item => addItemToList(listId, item));
}

// CRUD Operationen - Create Read Update Delete

// file local speichern
export function saveData() {
    localStorage.setItem("shoppingData", JSON.stringify(data));
}

// Löscht eine Liste anhand der ID
export function deleteList(id) {
    delete data.lists[id];
    saveData();
}

// Setzt das Listen-Objekt auf ein leeres Objekt zurück
export function deleteAllLists() {
    data.lists = {};
    saveData();
}

// Aktualisiert die Daten einer Liste (z.B. den Namen)
export function updateList(id, newData) {
    const list = data.lists[id];
    if (!list) return;

    Object.assign(list, newData); // Fügt die neuen Daten zum Listenobjekt hinzu
    const timestamp = nowMs();
    list.updatedAt = timestamp;
    list.lastModifiedTimestamp = timestamp; // Sync-Timestamp aktualisieren
    list.version = (list.version || 0) + 1; // Version inkrementieren
    saveData();
}

// Aktualisiert ein Item mithilfe der Liste ID und der Item ID
// ID der Einkaufslist, ID des zu ersetzenden Item, neue Daten (Item Objekt mit den zu ändernden Werten)
export function updateItem(listId, itemId, newData) {
  const list = data.lists[listId];
  const item = list.items.find(i => i.id === itemId); //Sucht Item anhand ID
  if (!item) return;

  Object.assign(item, newData);
  const timestamp = nowMs();
  item.updatedAt = timestamp;
  list.updatedAt = timestamp;
  list.lastModifiedTimestamp = timestamp;
  list.version = (list.version || 0) + 1;
  
  // Suchindex aktualisieren wenn Name geändert wurde
  if (newData.name !== undefined) {
    updateSearchIndex(listId);
  }

  saveData();
}

// Löscht ein Item anhand der Liste ID und der Item ID
export function deleteItem(listId, itemId) {
  const list = data.lists[listId];
  list.items = list.items.filter(i => i.id !== itemId);
  
  const timestamp = nowMs();
  list.updatedAt = timestamp;
  list.lastModifiedTimestamp = timestamp;
  list.version = (list.version || 0) + 1;
  
  // Suchindex aktualisieren
  updateSearchIndex(listId);
  
  saveData();
}

// Daten aus dem localStorage laden
export function loadData() {
    const raw = localStorage.getItem("shoppingData");
    if (!raw) return

    try {
        const parsed = JSON.parse(raw)
        data.lists = parsed.lists || {}
        // Migration auf neues Format
        migrateListsToNewFormat();
    } catch (e) {
        console.error("Fehler beim Laden:", e)
        data.lists = {}
    }
}

// Daten zurückgeben - für andere Module
export function getData() {
    return data;
}

export function getList(id) {
    return data.lists[id];
}


// Daten als JSON Datei herunterladen 
function downloadJSON(filename, dataObj) {
    const dataStr = JSON.stringify(dataObj, null, 2);
    const blob = new Blob([dataStr], { type: "application/json" });
    const url = URL.createObjectURL(blob);

    const a = document.createElement("a");
    a.href = url;
    a.download = filename;
    a.click();

    URL.revokeObjectURL(url);
}

// Export mehrere Listen anhand ID
export function exportMultipleLists(selectedSet){
    const exportObj = { lists: {} };
    for (const listId of selectedSet){
        const list = data.lists[listId];
        if (list) {
            exportObj.lists[listId] = list;
        }
    }
    downloadJSON("multiple-lists-export.json", exportObj);
}

// Export einer Liste als JSON Datei
export function exportList(listId) {
    const list = data.lists[listId];
    if (!list) return false;
    const exportObj = {
        lists: {
            [listId]: list
        }
    };
    downloadJSON(`list-${listId}.json`, exportObj);
    return true;
}

// Export aller Listen als JSON Datei
export function exportAll() {
    downloadJSON("shoppingData.json", data);
}

// Import einer Liste aus einer JSON Datei
export async function importList(file) {
    const text = await file.text();
    const imported = JSON.parse(text);

    let id = imported.id;

    // ID collision → neue ID erzeugen
    if (data.lists[id]) {
        id = uuid();
        imported.id = id;
    }

    data.lists[id] = imported;
    saveData();
    return id;
}

// Import aller Listen aus einer JSON Datei (kein Löschen)
export async function importAllAdd(file) {
    const text = await file.text();
    const imported = JSON.parse(text);

    if (!imported.lists) throw new Error("Ungültiges Format.");

    for (let id in imported.lists) {
        let list = imported.lists[id];
        // Kollision verhindern
        let newId = id;
        if (data.lists[id]) {
            newId = uuid();
            list.id = newId;
        }
        data.lists[newId] = list;
    }

    saveData();
    return true;
}

// Import aller Listen aus einer JSON Datei (komplettes Ersetzen) -- RESTORE!!
export async function importAllRestore(file) {
    const text = await file.text();
    const imported = JSON.parse(text);

    if (!imported.lists) throw new Error("Ungültiges Format.");
    // localStorage komplett löschen
    localStorage.removeItem("shoppingData");
    // DataStore ersetzen
    data.lists = imported.lists;
    // neu speichern
    saveData();

    return true;
}

// --SUCHINDEX-FUNKTIONEN--
// Konvertiert Set zu Array für JSON-Speicherung
export function searchIndexSetToArray(indexSet) {
    return Array.from(indexSet);
}

// Konvertiert Array zu Set für Laufzeit-Performance
export function searchIndexArrayToSet(indexArray) {
    return new Set(indexArray || []);
}

// Aktualisiert den Suchindex einer Liste basierend auf Item-Namen
export function updateSearchIndex(listId) {
    const list = data.lists[listId];
    if (!list) return;
    
    // Index als Set erstellen (für Performance)
    const indexSet = new Set();
    
    list.items.forEach(item => {
        if (item.name) {
            // Namen normalisieren und in Tokens aufteilen
            const normalized = item.name.toLowerCase().trim();
            // Ganzen Namen hinzufügen
            indexSet.add(normalized);
            // Einzelne Wörter hinzufügen
            normalized.split(/\s+/).forEach(word => {
                if (word.length > 0) {
                    indexSet.add(word);
                }
            });
        }
    });
    
    // Als Array speichern
    list.searchIndex = searchIndexSetToArray(indexSet);
}

// Sucht Items in einer Liste anhand eines Suchbegriffs
export function searchItemsInList(listId, searchTerm) {
    const list = data.lists[listId];
    if (!list || !searchTerm) return list?.items || [];
    
    const normalized = searchTerm.toLowerCase().trim();
    
    // Filtere Items, deren Name den Suchbegriff enthält
    return list.items.filter(item => 
        item.name && item.name.toLowerCase().includes(normalized)
    );
}

// Initialisiert Suchindizes für alle Listen (für Migration)
export function initializeAllSearchIndexes() {
    Object.keys(data.lists).forEach(listId => {
        updateSearchIndex(listId);
    });
    saveData();
}

// Stellt sicher, dass alle Listen die neuen Felder haben (Migration)
export function migrateListsToNewFormat() {
    let hasChanges = false;
    
    Object.keys(data.lists).forEach(listId => {
        const list = data.lists[listId];
        
        // Fehlende Felder hinzufügen
        if (!list.ownerId) {
            list.ownerId = list.owner || 'user';
            hasChanges = true;
        }
        if (!list.sharedWithUserIds) {
            list.sharedWithUserIds = [];
            hasChanges = true;
        }
        if (!list.lastModifiedTimestamp) {
            list.lastModifiedTimestamp = list.updatedAt || nowMs();
            hasChanges = true;
        }
        if (!list.version) {
            list.version = 1;
            hasChanges = true;
        }
        if (!list.searchIndex) {
            list.searchIndex = [];
            updateSearchIndex(listId);
            hasChanges = true;
        }
    });
    
    if (hasChanges) {
        saveData();
    }
}
