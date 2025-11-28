<script setup>
import { ref, onMounted } from 'vue'
import * as DS from '@/store/dataStore.js'
import { useRouter } from 'vue-router'

const router = useRouter()
const darkMode = ref(false)
const showCreateList = ref(false)
const newListName = ref('')
const importInput = ref(null)
const isMobileMenuOpen = ref(false)

function goToHome() {
  router.push('/')
  window.dispatchEvent(new CustomEvent("karrenkauf-home-reset"))
}

function triggerImport() {
  importInput.value.click()
}

function triggerExport() {
  DS.exportAll()
}

async function handleImport(e) {
  const file = e.target.files[0]
  if (file) await DS.importAllAdd(file)
  e.target.value = ''
}

function createListAction() {
  if (!newListName.value.trim()) return
  DS.createList(newListName.value, 'user')
  newListName.value = ''
  showCreateList.value = false
}

function toggleDarkMode() {
  darkMode.value = !darkMode.value
  document.documentElement.classList.toggle('dark', darkMode.value)
  localStorage.setItem('darkMode', darkMode.value)
}

onMounted(() => {
  const stored = localStorage.getItem('darkMode')
  if (stored === 'true') {
    darkMode.value = true
    document.documentElement.classList.add('dark')
  }
})
</script>

<template>
  <nav class="navbar">

    <div class="brand-anchor" @click="goToHome">
      <img src="/src/assets/images/karrenkauf.jfif" class="brand-image" />
      <span class="brand-title">Karrenkauf</span>
    </div>

    <div class="nav-center">
      <button class="nav-btn" @click="showCreateList = true">➕ Neue Liste</button>
      <button class="nav-btn" @click="triggerImport">⬆️ Import</button>
      <button class="nav-btn" @click="triggerExport">⬇️ Export</button>
    </div>

    <div class="nav-right">
      <button class="nav-btn" @click="toggleDarkMode">
        <span v-if="darkMode">☀️ Lightmode</span>
        <span v-else>🌙 Darkmode</span>
      </button>
      <router-link to="/profile" class="nav-btn">👤 Profil</router-link>
    </div>

    <input ref="importInput" type="file" hidden @change="handleImport" />

    <div v-if="showCreateList" class="modal-backdrop" @click.self="showCreateList = false">
      <div class="modal">
        <h2>Neue Einkaufsliste</h2>
        <input v-model="newListName" placeholder="Name der Liste" />
        <button @click="createListAction">Erstellen</button>
        <button class="cancel" @click="showCreateList = false">Abbrechen</button>
      </div>
    </div>
  </nav>
</template>
