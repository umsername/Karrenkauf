// später in anderen Projekten importieren mit -- import { createList } from "./dataStore.js";
// Mithilfe von export function -- können wir die Funktionen in anderen Dateien importieren und verwenden


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
let data = { lists: {} };
loadData();

// Neue Liste erstellen
export function createList(name, owner) {
    const id = uuid(); // Funktion von oben, damit wir eine ID bekommen die random ist
    const timestamp = nowMs();
    
    const list = {
        id,
        name,
        owner,
        createdAt: timestamp,
        updatedAt: timestamp,
        items: []
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
        createdAt: timestamp,
        updatedAt: timestamp
    };
}

// Fügt ein Item zu einer Liste hinzu
export function addItemToList(listId, item) {
    data.lists[listId].items.push(item);
    data.lists[listId].updatedAt = nowMs();
    saveData();               // ruft saveData Funktion auf um die Daten sofort zu speichern
}

// Fügt mehrere Items (Array) zu einer Liste hinzu - verwendet dabei addItemToList Funktion
export function addItemsToList(listId, itemsArray) {
    itemsArray.forEach(item => addItemToList(listId, item));
}




// CRUD Operationen - Create Read Update Delete

//file local speichern
export function saveData() {
    localStorage.setItem("shoppingData", JSON.stringify(data));
}

// Löscht eine Liste anhand der ID
export function deleteList(id) {
    delete data.lists[id];
    saveData();
}

// Aktualisiert ein Item mithilfe der Liste ID und der Item ID
// ID der Einkaufslist, ID des zu ersetzenden Item, neue Daten (Item Objekt mit den zu ändernden Werten)
export function updateItem(listId, itemId, newData) {
  const item = data.lists[listId].items.find(i => i.id === itemId); //Sucht Item anhand ID
  if (!item) return;

  Object.assign(item, newData);
  item.updatedAt = nowMs();
  data.lists[listId].updatedAt = nowMs();

  saveData();
}

// Löscht ein Item anhand der Liste ID und der Item ID
export function deleteItem(listId, itemId) {
  data.lists[listId].items = data.lists[listId].items.filter(i => i.id !== itemId);
  data.lists[listId].updatedAt = nowMs();
  saveData();
}

// Daten aus dem localStorage laden
export function loadData() {
    const raw = localStorage.getItem("shoppingData");
    if (!raw) {
        data = { lists: {} };
        return;
    }

    try {
        data = JSON.parse(raw);
    } catch (e) {
        console.error("Fehler beim Laden:", e);
        data = { lists: {} };
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

// Export einer Liste als JSON Datei
export function exportList(listId) {
    const list = data.lists[listId];
    if (!list) return false;

    downloadJSON(`list-${listId}.json`, list);
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
    data = imported;

    // neu speichern
    saveData();

    return true;
}
