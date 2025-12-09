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
 * Exportiert den konfigurierten API-Client für erweiterte Nutzung
 */
export default apiClient;
