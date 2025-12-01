<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import * as DS from '@/store/dataStore.js'
import { useRouter } from 'vue-router'

const router = useRouter()
const darkMode = ref(false)
const importInput = ref(null)
const restoreInput = ref(null)
const isProfileMenuOpen = ref(false)

// Refs für das Menü UND den Button
const profileMenu = ref(null)
const profileMenuButton = ref(null)

function goToHome() {
  router.push('/')
  window.dispatchEvent(new CustomEvent("karrenkauf-home-reset"))
}

function triggerImport() {
  importInput.value.click()
  isProfileMenuOpen.value = false
}

function triggerRestore(){
  restoreInput.value.click()
  isProfileMenuOpen.value = false
}

async function handleImport(e) {
  const file = e.target.files[0]
  if (file) await DS.importAllAdd(file)
  e.target.value = ''
}

async function handleRestore(e) {
  const file = e.target.files[0]
  if (file) await DS.importAllRestore(file)
  e.target.value = ''
}

function toggleDarkMode() {
  darkMode.value = !darkMode.value
  document.documentElement.classList.toggle('dark', darkMode.value)
  localStorage.setItem('darkMode', darkMode.value)
}

// Logik zum Schließen des Menüs
function handleClickOutside(event) {
  if (
      profileMenu.value &&
      !profileMenu.value.contains(event.target) &&
      profileMenuButton.value &&
      !profileMenuButton.value.contains(event.target)
  ) {
    isProfileMenuOpen.value = false
  }
}

onMounted(() => {
  const stored = localStorage.getItem('darkMode')
  if (stored === 'true') {
    darkMode.value = true
    document.documentElement.classList.add('dark')
  }
  document.addEventListener('click', handleClickOutside, true)
})
onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside, true)
})
</script>

<template>
  <nav class="navbar">
    <div class="nav-left">
      <div class="brand-anchor" @click="goToHome">
        <img src="/src/assets/images/karrenkauf.jfif" class="brand-image"/>
        <span class="brand-title">Karrenkauf</span>
      </div>
    </div>
    <div class="nav-center">
      <button class="nav-btn" @click="goToHome"> Übersicht </button>
    </div>
    <div class="nav-right">
      <button class="nav-btn" @click="toggleDarkMode">
        <span v-if="darkMode">Lightmode ☀️</span>
        <span v-else>Darkmode 🌙</span>
      </button>
      <div class="profile-menu-wrapper">
        <button class="nav-btn" @click="isProfileMenuOpen = !isProfileMenuOpen" ref="profileMenuButton">
          <span>☰</span>
        </button>
        <div v-if="isProfileMenuOpen" class="profile-menu" ref="profileMenu">
          <router-link to="/profile" class="profile-menu-item" @click="isProfileMenuOpen = false">👤 Profil</router-link>
          <button @click="triggerImport" class="profile-menu-item">⬆️ Import</button>
          <button @click="triggerRestore" class="profile-menu-item"> Restore Backup </button>
        </div>
      </div>
    </div>
    <input ref="importInput" type="file" hidden @change="handleImport"/>
    <input ref="restoreInput" type="file" hidden @change="handleRestore"/>

  </nav>
</template>
