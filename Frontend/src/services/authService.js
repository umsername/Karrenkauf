import axios from 'axios';

// Backend API Base URL - kann in .env konfiguriert werden
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

// Token-Schlüssel für localStorage
const TOKEN_KEY = 'karrenkauf_auth_token';
const USER_KEY = 'karrenkauf_user';

/**
 * Login-Funktion
 * @param {string} username - Benutzername
 * @param {string} password - Passwort
 * @returns {Promise<{success: boolean, message: string, token?: string, user?: object}>}
 */
export async function login(username, password) {
    try {
        const response = await axios.post(`${API_BASE_URL}/api/login`, {
            username,
            password
        });

        const responseText = response.data;

        // Backend gibt einen String zurück, der den Token enthält
        if (responseText.includes('Login successful')) {
            // Token extrahieren (nach "TOKEN:\n")
            const tokenMatch = responseText.match(/TOKEN:\s*(.+)/);
            if (tokenMatch && tokenMatch[1]) {
                const token = tokenMatch[1].trim();
                
                // Token und User-Info speichern
                localStorage.setItem(TOKEN_KEY, token);
                localStorage.setItem(USER_KEY, JSON.stringify({ username }));
                
                return {
                    success: true,
                    message: 'Login erfolgreich!',
                    token,
                    user: { username }
                };
            }
        }

        // Fehlerfall
        return {
            success: false,
            message: responseText || 'Login fehlgeschlagen'
        };

    } catch (error) {
        console.error('Login error:', error);
        
        if (error.response) {
            return {
                success: false,
                message: error.response.data || 'Serverfehler beim Login'
            };
        } else if (error.request) {
            return {
                success: false,
                message: 'Keine Verbindung zum Server möglich'
            };
        } else {
            return {
                success: false,
                message: 'Ein Fehler ist aufgetreten'
            };
        }
    }
}

/**
 * Logout-Funktion
 */
export function logout() {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
}

/**
 * Gibt den aktuellen Token zurück
 * @returns {string|null}
 */
export function getToken() {
    return localStorage.getItem(TOKEN_KEY);
}

/**
 * Gibt den aktuellen Benutzer zurück
 * @returns {object|null}
 */
export function getCurrentUser() {
    const userStr = localStorage.getItem(USER_KEY);
    if (!userStr) return null;
    
    try {
        return JSON.parse(userStr);
    } catch (e) {
        return null;
    }
}

/**
 * Prüft, ob ein Benutzer eingeloggt ist
 * @returns {boolean}
 */
export function isAuthenticated() {
    return !!getToken();
}

/**
 * Prüft den Login-Status beim Backend
 * @returns {Promise<{isValid: boolean, message: string, username?: string}>}
 */
export async function checkStatus() {
    const token = getToken();
    
    if (!token) {
        return {
            isValid: false,
            message: 'Kein Token vorhanden'
        };
    }

    try {
        const response = await axios.get(`${API_BASE_URL}/api/status`, {
            params: { token }
        });

        const message = response.data;

        if (message.includes('Already logged in')) {
            const usernameMatch = message.match(/Already logged in as:\s*(.+)/);
            return {
                isValid: true,
                message,
                username: usernameMatch ? usernameMatch[1].trim() : null
            };
        }

        // Token ungültig oder abgelaufen
        logout();
        return {
            isValid: false,
            message
        };

    } catch (error) {
        console.error('Status check error:', error);
        return {
            isValid: false,
            message: 'Fehler beim Statuscheck'
        };
    }
}
