<script setup>
import { computed } from 'vue'
import { getCurrentUser, isAuthenticated } from '@/services/authService.js'

// Aktuellen Benutzer laden
const currentUser = computed(() => getCurrentUser())
const userLoggedIn = computed(() => isAuthenticated())
</script>

<template>
  <div class="text-page">
    <div class="profile-header">
      <h1>Profil</h1>
    </div>

    <div class="profile-content">
      <div class="user-info">
        <p class="info-label">Aktueller Benutzer:</p>
        <p class="info-value">
          {{ userLoggedIn ? currentUser?.username : 'Nicht eingeloggt' }}
        </p>
      </div>

      <div v-if="!userLoggedIn" class="login-hint">
        <p>Sie sind derzeit nicht eingeloggt. Bitte melden Sie sich an, um alle Funktionen zu nutzen.</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.profile-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
}

.profile-header h1 {
  margin: 0;
}

.profile-content {
  background: white;
  padding: 2rem;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.user-info {
  margin-bottom: 1.5rem;
}

.info-label {
  font-size: 0.9rem;
  color: #666;
  margin-bottom: 0.5rem;
  font-weight: 500;
}

.info-value {
  font-size: 1.25rem;
  color: #2c3e50;
  font-weight: 600;
  margin: 0;
}

.login-hint {
  background: #fff3cd;
  border: 1px solid #ffc107;
  border-radius: 6px;
  padding: 1rem;
  margin-top: 1.5rem;
}

.login-hint p {
  margin: 0;
  color: #856404;
}
</style>

