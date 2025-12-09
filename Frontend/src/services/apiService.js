import axios from 'axios';
import { getToken } from './authService.js';

// Backend API Base URL
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

// Axios-Instanz mit Konfiguration
const apiClient = axios.create({
    baseURL: API_BASE_URL,
    headers: {
        'Content-Type': 'application/json'
    }
});

// Request-Interceptor: JWT Token zu jedem Request hinzufügen
apiClient.interceptors.request.use(
    (config) => {
        const token = getToken();
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

// Response-Interceptor: Fehlerbehandlung
apiClient.interceptors.response.use(
    (response) => response,
    (error) => {
        if (error.response?.status === 401) {
            // Token abgelaufen oder ungültig
            console.warn('Unauthorized - Token möglicherweise abgelaufen');
            // Optional: Automatisch zur Login-Seite weiterleiten
            // window.location.href = '/login';
        }
        return Promise.reject(error);
    }
);

/**
 * Holt alle Listen vom Backend
 * @returns {Promise<Array>}
 */
export async function fetchAllLists() {
    try {
        const response = await apiClient.get('/api/lists');
        return response.data;
    } catch (error) {
        console.error('Error fetching lists:', error);
        throw error;
    }
}

/**
 * Holt eine einzelne Liste vom Backend
 * @param {string} listId - ID der Liste
 * @returns {Promise<object>}
 */
export async function fetchList(listId) {
    try {
        const response = await apiClient.get(`/api/lists/${listId}`);
        return response.data;
    } catch (error) {
        console.error('Error fetching list:', error);
        throw error;
    }
}

/**
 * Synchronisiert Listen mit dem Backend
 * @param {object} listsData - Listen-Daten im Format { lists: {...} }
 * @returns {Promise<string>}
 */
export async function syncListsToBackend(listsData) {
    try {
        const response = await apiClient.post('/api/lists/sync', listsData);
        return response.data;
    } catch (error) {
        console.error('Error syncing lists:', error);
        throw error;
    }
}

/**
 * Holt alle für den Benutzer zugänglichen Listen (eigene + geteilte)
 * @returns {Promise<object>} Object with 'owned' and 'shared' arrays
 */
export async function fetchAccessibleLists() {
    try {
        const response = await apiClient.get('/api/lists/accessible');
        return response.data;
    } catch (error) {
        console.error('Error fetching accessible lists:', error);
        throw error;
    }
}

/**
 * Teilt eine Liste mit einem anderen Benutzer
 * @param {string} listId - ID der Liste
 * @param {string} username - Benutzername des Empfängers
 * @returns {Promise<string>}
 */
export async function shareListWithUser(listId, username) {
    try {
        const response = await apiClient.post(`/api/lists/${listId}/share`, { username });
        return response.data;
    } catch (error) {
        console.error('Error sharing list:', error);
        throw error;
    }
}

/**
 * Holt die Liste der Benutzer, mit denen eine Liste geteilt wurde
 * @param {string} listId - ID der Liste
 * @returns {Promise<object>} Object with listId, owner, and sharedWith array
 */
export async function getSharedUsers(listId) {
    try {
        const response = await apiClient.get(`/api/lists/${listId}/shared`);
        return response.data;
    } catch (error) {
        console.error('Error fetching shared users:', error);
        throw error;
    }
}

/**
 * Entfernt einen Benutzer von einer geteilten Liste
 * @param {string} listId - ID der Liste
 * @param {string} username - Benutzername des zu entfernenden Benutzers
 * @returns {Promise<string>}
 */
export async function unshareListWithUser(listId, username) {
    try {
        const response = await apiClient.delete(`/api/lists/${listId}/share/${username}`);
        return response.data;
    } catch (error) {
        console.error('Error unsharing list:', error);
        throw error;
    }
}

/**
 * Exportiert den konfigurierten API-Client für erweiterte Nutzung
 */
export default apiClient;
