import { fetchAllLists, syncListsToBackend } from './apiService.js';
import { getData, saveData } from '@/store/dataStore.js';

/**
 * Synchronisiert Listen zwischen LocalStorage und Backend
 * 
 * Logik:
 * 1. Lädt Listen vom Backend
 * 2. Vergleicht mit LocalStorage basierend auf lastModifiedTimestamp
 * 3. Bei Konflikt: Backend-Version gewinnt (kann später erweitert werden)
 * 4. Sendet lokale Änderungen zum Backend
 * 
 * @returns {Promise<{success: boolean, message: string, updates?: object}>}
 */
export async function syncLists() {
    try {
        // 1. Backend-Daten laden
        const backendLists = await fetchAllLists();
        
        // 2. Lokale Daten holen
        const localData = getData();
        const localLists = localData.lists || {};
        
        let updatedCount = 0;
        let uploadedCount = 0;
        
        // 3. Backend-Listen verarbeiten
        if (Array.isArray(backendLists)) {
            backendLists.forEach(backendList => {
                const listId = backendList.id;
                const localList = localLists[listId];
                
                if (!localList) {
                    // Neue Liste vom Backend - hinzufügen
                    localLists[listId] = backendList;
                    updatedCount++;
                } else {
                    // Liste existiert lokal - Timestamps und Version vergleichen
                    const backendTimestamp = backendList.lastModifiedTimestamp || backendList.updatedAt || 0;
                    const localTimestamp = localList.lastModifiedTimestamp || localList.updatedAt || 0;
                    const backendVersion = backendList.version || 0;
                    const localVersion = localList.version || 0;
                    
                    // Konfliktauflösung: Version hat Priorität, bei gleicher Version entscheidet Timestamp
                    if (backendVersion > localVersion || 
                        (backendVersion === localVersion && backendTimestamp > localTimestamp)) {
                        // Backend ist neuer - überschreiben
                        localLists[listId] = backendList;
                        updatedCount++;
                    }
                    // Wenn lokal neuer: später zum Backend pushen
                }
            });
        }
        
        // 4. Lokale Änderungen identifizieren und zum Backend senden
        const localListsToUpload = {};
        let hasLocalChanges = false;
        
        Object.keys(localLists).forEach(listId => {
            const localList = localLists[listId];
            const backendList = Array.isArray(backendLists) 
                ? backendLists.find(bl => bl.id === listId) 
                : null;
            
            if (!backendList) {
                // Neue lokale Liste - zum Upload hinzufügen
                localListsToUpload[listId] = localList;
                hasLocalChanges = true;
                uploadedCount++;
            } else {
                // Prüfen ob lokal neuer (Version und Timestamp)
                const backendTimestamp = backendList.lastModifiedTimestamp || backendList.updatedAt || 0;
                const localTimestamp = localList.lastModifiedTimestamp || localList.updatedAt || 0;
                const backendVersion = backendList.version || 0;
                const localVersion = localList.version || 0;
                
                if (localVersion > backendVersion ||
                    (localVersion === backendVersion && localTimestamp > backendTimestamp)) {
                    // Lokale Version ist neuer - zum Upload hinzufügen
                    localListsToUpload[listId] = localList;
                    hasLocalChanges = true;
                    uploadedCount++;
                }
            }
        });
        
        // 5. Lokale Änderungen zum Backend senden
        if (hasLocalChanges) {
            await syncListsToBackend({ lists: localListsToUpload });
        }
        
        // 6. LocalStorage aktualisieren
        if (updatedCount > 0) {
            saveData();
        }
        
        return {
            success: true,
            message: `Synchronisation erfolgreich: ${updatedCount} aktualisiert, ${uploadedCount} hochgeladen`,
            updates: {
                fromBackend: updatedCount,
                toBackend: uploadedCount
            }
        };
        
    } catch (error) {
        console.error('Sync error:', error);
        
        return {
            success: false,
            message: error.message || 'Fehler bei der Synchronisation'
        };
    }
}

/**
 * Pusht alle lokalen Listen zum Backend (Force Upload)
 * @returns {Promise<{success: boolean, message: string}>}
 */
export async function pushAllListsToBackend() {
    try {
        const localData = getData();
        const localLists = localData.lists || {};
        
        await syncListsToBackend({ lists: localLists });
        
        return {
            success: true,
            message: 'Alle Listen erfolgreich zum Backend hochgeladen'
        };
        
    } catch (error) {
        console.error('Push error:', error);
        
        return {
            success: false,
            message: error.message || 'Fehler beim Hochladen'
        };
    }
}

/**
 * Holt alle Listen vom Backend (Force Download)
 * @param {boolean} overwrite - Soll lokale Daten überschrieben werden?
 * @returns {Promise<{success: boolean, message: string, count?: number}>}
 */
export async function pullAllListsFromBackend(overwrite = false) {
    try {
        const backendLists = await fetchAllLists();
        const localData = getData();
        const localLists = localData.lists || {};
        
        let updatedCount = 0;
        
        if (Array.isArray(backendLists)) {
            backendLists.forEach(backendList => {
                if (overwrite || !localLists[backendList.id]) {
                    localLists[backendList.id] = backendList;
                    updatedCount++;
                }
            });
        }
        
        if (updatedCount > 0) {
            saveData();
        }
        
        return {
            success: true,
            message: `${updatedCount} Listen vom Backend geladen`,
            count: updatedCount
        };
        
    } catch (error) {
        console.error('Pull error:', error);
        
        return {
            success: false,
            message: error.message || 'Fehler beim Herunterladen'
        };
    }
}
