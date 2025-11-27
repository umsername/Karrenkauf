<script setup>
import { ref, onMounted } from 'vue'
import HelloWorld from './components/HelloWorld.vue'
import * as DS from './components/dataStore.js'

const units = ref([])

onMounted(async () => {
  const res = await fetch('./units.json')
  units.value = await res.json()

  DS.loadData(units.value) // optional: Übergabe an DS
})
</script>

<template>
  <div>
    <HelloWorld msg="Vite + Vue" />

    <ul v-if="units.length">
      <li v-for="unit in units" :key="unit.id">{{ unit.name }}</li>
    </ul>
    <p v-else>Lade Daten...</p>
  </div>
</template>
