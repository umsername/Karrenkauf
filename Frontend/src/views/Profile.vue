<script setup>
import { ref, computed } from 'vue'
import { getCurrentUser, isAuthenticated } from '@/services/authService.js'
import { ShareIcon } from '@heroicons/vue/24/outline'

// Aktuellen Benutzer laden
const currentUser = computed(() => getCurrentUser())
const userLoggedIn = computed(() => isAuthenticated())

// Share-Funktionalität
const shareMessage = ref('')

async function shareProfile() {
  const username = currentUser.value?.username || 'Gast'
  const shareData = {
    title: 'Mein Karrenkauf Profil',
    text: `Schau dir mein Profil auf Karrenkauf an! Benutzer: ${username}`,
    url: window.location.href
  }

  try {
    if (navigator.share) {
      // Verwende die Web Share API, wenn verfügbar (mobil)
      await navigator.share(shareData)
      shareMessage.value = 'Erfolgreich geteilt!'
    } else {
      // Fallback: Kopiere URL in die Zwischenablage
      await navigator.clipboard.writeText(window.location.href)
      shareMessage.value = 'Link in Zwischenablage kopiert!'
    }
    
    // Nachricht nach 3 Sekunden ausblenden
    setTimeout(() => {
      shareMessage.value = ''
    }, 3000)
  } catch (error) {
    console.error('Fehler beim Teilen:', error)
    shareMessage.value = 'Fehler beim Teilen'
    setTimeout(() => {
      shareMessage.value = ''
    }, 3000)
  }
}
</script>

<template>
  <div class="text-page">
    <div class="profile-header">
      <h1>Profil</h1>
      <button @click="shareProfile" class="share-btn">
        <ShareIcon class="icon" />
        <span>Teilen</span>
      </button>
    </div>

    <!-- Share-Nachricht -->
    <div v-if="shareMessage" class="share-message">
      {{ shareMessage }}
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
  flex-wrap: wrap;
  gap: 1rem;
}

.profile-header h1 {
  margin: 0;
}

.share-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1.5rem;
  background: #3498db;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 1rem;
  font-weight: 500;
  cursor: pointer;
  transition: background 0.2s;
}

.share-btn:hover {
  background: #2980b9;
}

.share-btn .icon {
  width: 1.25rem;
  height: 1.25rem;
}

.share-message {
  background: #d4edda;
  color: #155724;
  padding: 1rem;
  border-radius: 8px;
  margin-bottom: 1.5rem;
  border: 1px solid #c3e6cb;
  text-align: center;
  font-weight: 500;
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

@media (max-width: 768px) {
  .profile-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .share-btn {
    width: 100%;
    justify-content: center;
  }
}
</style>

