<script setup>
import { ref } from 'vue'
import axios from 'axios'

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'

// State
const healthStatus = ref('')
const registerStatus = ref('')
const loginStatus = ref('')
const isTestingHealth = ref(false)
const isTestingRegister = ref(false)
const isTestingLogin = ref(false)

// Test data
const testUsername = 'testuser_' + Date.now()
const testPassword = 'password123'

/**
 * Test Health - Einfacher GET Request an /api/public/ping
 */
async function checkHealth() {
  isTestingHealth.value = true
  healthStatus.value = '⏳ Teste Verbindung...'
  
  try {
    const response = await axios.get(`${API_BASE_URL}/api/public/ping`)
    
    healthStatus.value = `✅ SUCCESS
Status: ${response.status}
Response: ${response.data}

Headers:
${JSON.stringify(response.headers, null, 2)}`
  } catch (error) {
    healthStatus.value = formatError('Health Check', error)
  } finally {
    isTestingHealth.value = false
  }
}

/**
 * Test Register - POST Request mit Testuser
 */
async function testRegister() {
  isTestingRegister.value = true
  registerStatus.value = '⏳ Teste Registrierung...'
  
  try {
    const response = await axios.post(
      `${API_BASE_URL}/api/user`,
      null,
      {
        params: {
          username: testUsername,
          password: testPassword
        }
      }
    )
    
    registerStatus.value = `✅ REGISTRATION SUCCESS
Status: ${response.status}
Response: ${response.data}

Test User: ${testUsername}
Test Password: ${testPassword}

Full Response:
${JSON.stringify(response.data, null, 2)}`
  } catch (error) {
    registerStatus.value = formatError('Register', error)
  } finally {
    isTestingRegister.value = false
  }
}

/**
 * Test Login - POST Request mit Testuser
 */
async function testLogin() {
  isTestingLogin.value = true
  loginStatus.value = '⏳ Teste Login...'
  
  try {
    const response = await axios.post(
      `${API_BASE_URL}/api/login`,
      {
        username: testUsername,
        password: testPassword
      }
    )
    
    // Token extrahieren
    const responseText = response.data
    const tokenMatch = responseText.match(/TOKEN:\s*(.+)/)
    const token = tokenMatch ? tokenMatch[1].trim() : 'No token found'
    
    loginStatus.value = `✅ LOGIN SUCCESS
Status: ${response.status}
Response: ${response.data}

Test User: ${testUsername}
Test Password: ${testPassword}

Extracted Token:
${token}

Full Response:
${JSON.stringify(response.data, null, 2)}`
  } catch (error) {
    loginStatus.value = formatError('Login', error)
  } finally {
    isTestingLogin.value = false
  }
}

/**
 * Format error information for display
 */
function formatError(testName, error) {
  let errorMsg = `❌ ${testName.toUpperCase()} ERROR\n\n`
  
  if (error.response) {
    // Server responded with error
    errorMsg += `Status Code: ${error.response.status}\n`
    errorMsg += `Status Text: ${error.response.statusText}\n\n`
    errorMsg += `Response Data:\n${JSON.stringify(error.response.data, null, 2)}\n\n`
    errorMsg += `Headers:\n${JSON.stringify(error.response.headers, null, 2)}`
  } else if (error.request) {
    // Request made but no response
    errorMsg += `Keine Antwort vom Server erhalten.\n\n`
    errorMsg += `Mögliche Ursachen:\n`
    errorMsg += `- Server läuft nicht auf ${API_BASE_URL}\n`
    errorMsg += `- CORS-Fehler\n`
    errorMsg += `- Netzwerkproblem\n\n`
    errorMsg += `Request Details:\n${error.request}`
  } else {
    // Error setting up request
    errorMsg += `Fehler beim Erstellen des Requests:\n${error.message}`
  }
  
  return errorMsg
}

/**
 * Run all tests sequentially
 */
async function runAllTests() {
  await checkHealth()
  await new Promise(resolve => setTimeout(resolve, 500))
  await testRegister()
  await new Promise(resolve => setTimeout(resolve, 500))
  await testLogin()
}
</script>

<template>
  <div class="connection-tester">
    <div class="tester-header">
      <h2>🔧 Connection Tester</h2>
      <p class="api-url">Backend URL: <code>{{ API_BASE_URL }}</code></p>
    </div>

    <div class="test-controls">
      <button 
        @click="checkHealth" 
        :disabled="isTestingHealth"
        class="test-btn health"
      >
        {{ isTestingHealth ? '⏳ Testing...' : '🏥 Check Health' }}
      </button>

      <button 
        @click="testRegister" 
        :disabled="isTestingRegister"
        class="test-btn register"
      >
        {{ isTestingRegister ? '⏳ Testing...' : '📝 Test Register' }}
      </button>

      <button 
        @click="testLogin" 
        :disabled="isTestingLogin"
        class="test-btn login"
      >
        {{ isTestingLogin ? '⏳ Testing...' : '🔐 Test Login' }}
      </button>

      <button 
        @click="runAllTests" 
        :disabled="isTestingHealth || isTestingRegister || isTestingLogin"
        class="test-btn all"
      >
        ▶️ Run All Tests
      </button>
    </div>

    <div class="test-results">
      <div v-if="healthStatus" class="result-box">
        <h3>Health Check Result</h3>
        <pre>{{ healthStatus }}</pre>
      </div>

      <div v-if="registerStatus" class="result-box">
        <h3>Registration Test Result</h3>
        <pre>{{ registerStatus }}</pre>
      </div>

      <div v-if="loginStatus" class="result-box">
        <h3>Login Test Result</h3>
        <pre>{{ loginStatus }}</pre>
      </div>
    </div>

    <div class="test-info">
      <h3>📋 Test Info</h3>
      <p><strong>Test Username:</strong> <code>{{ testUsername }}</code></p>
      <p><strong>Test Password:</strong> <code>{{ testPassword }}</code></p>
      <p class="note">
        ⚠️ Hinweis: Bei jedem Seitenaufruf wird ein neuer Testuser mit Timestamp erstellt.
      </p>
    </div>
  </div>
</template>

<style scoped>
.connection-tester {
  max-width: 1200px;
  margin: 2rem auto;
  padding: 1.5rem;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.tester-header {
  margin-bottom: 2rem;
  text-align: center;
}

.tester-header h2 {
  margin: 0 0 0.5rem 0;
  color: #2c3e50;
}

.api-url {
  margin: 0;
  color: #666;
  font-size: 0.9rem;
}

.api-url code {
  background: #f0f0f0;
  padding: 0.2rem 0.5rem;
  border-radius: 4px;
  color: #e74c3c;
  font-family: monospace;
}

.test-controls {
  display: flex;
  gap: 1rem;
  flex-wrap: wrap;
  margin-bottom: 2rem;
}

.test-btn {
  flex: 1;
  min-width: 150px;
  padding: 0.75rem 1rem;
  border: none;
  border-radius: 6px;
  font-size: 1rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  color: white;
}

.test-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.test-btn.health {
  background: #3498db;
}

.test-btn.health:hover:not(:disabled) {
  background: #2980b9;
}

.test-btn.register {
  background: #2ecc71;
}

.test-btn.register:hover:not(:disabled) {
  background: #27ae60;
}

.test-btn.login {
  background: #9b59b6;
}

.test-btn.login:hover:not(:disabled) {
  background: #8e44ad;
}

.test-btn.all {
  background: #e67e22;
}

.test-btn.all:hover:not(:disabled) {
  background: #d35400;
}

.test-results {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  margin-bottom: 2rem;
}

.result-box {
  background: #f8f9fa;
  border: 1px solid #dee2e6;
  border-radius: 6px;
  padding: 1rem;
}

.result-box h3 {
  margin: 0 0 0.75rem 0;
  color: #2c3e50;
  font-size: 1.1rem;
}

.result-box pre {
  margin: 0;
  padding: 1rem;
  background: #2c3e50;
  color: #ecf0f1;
  border-radius: 4px;
  overflow-x: auto;
  font-size: 0.85rem;
  line-height: 1.5;
  font-family: 'Courier New', monospace;
  white-space: pre-wrap;
  word-wrap: break-word;
}

.test-info {
  background: #fff3cd;
  border: 1px solid #ffc107;
  border-radius: 6px;
  padding: 1rem;
}

.test-info h3 {
  margin: 0 0 0.75rem 0;
  color: #856404;
  font-size: 1rem;
}

.test-info p {
  margin: 0.5rem 0;
  color: #856404;
}

.test-info code {
  background: rgba(0, 0, 0, 0.05);
  padding: 0.2rem 0.4rem;
  border-radius: 3px;
  font-family: monospace;
}

.test-info .note {
  margin-top: 1rem;
  font-size: 0.9rem;
  font-style: italic;
}

@media (max-width: 768px) {
  .test-controls {
    flex-direction: column;
  }
  
  .test-btn {
    min-width: 100%;
  }
}
</style>
