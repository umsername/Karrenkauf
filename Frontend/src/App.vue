<script setup>
import { onMounted } from 'vue'
import Navbar from '@/components/layout/Navbar.vue'
import Footer from '@/components/layout/Footer.vue'
import { syncLists } from '@/services/syncService.js'
import { isAuthenticated } from '@/services/authService.js'

// Auto-Sync beim App-Start, wenn der Benutzer eingeloggt ist
onMounted(async () => {
  if (isAuthenticated()) {
    try {
      console.log('Auto-Sync wird gestartet...')
      const result = await syncLists()
      if (result.success) {
        console.log('Auto-Sync erfolgreich:', result.message)
      } else {
        console.warn('Auto-Sync fehlgeschlagen:', result.message)
      }
    } catch (error) {
      console.error('Auto-Sync Fehler:', error)
    }
  }
})
</script>

<template>
  <div class="app-wrapper">
    <Navbar />

    <main class="main-content">
      <router-view />
    </main>

    <Footer />
  </div>
</template>