<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import * as DS from '@/store/dataStore.js'
import unitsData from '@/assets/data/units.json'

const expandedListId = ref(null)
const showAddDialog = ref(false)

const newItemName = ref('')
const menge = ref('')
const preis = ref('')
const selectedUnit = ref('')
const beschreibung = ref('')
const category = ref('')
const lists = computed(() => DS.getData().lists)

const unitLabel = value => {
  const found = unitsData.units.find(u => u.value === value)
  return found ? found.label : value
}

function resetToOverview() {
  expandedListId.value = null
}

onMounted(() => {
  window.addEventListener("karrenkauf-home-reset", resetToOverview)
})

onUnmounted(() => {
  window.removeEventListener("karrenkauf-home-reset", resetToOverview)
})

function preview(list) {
  return list.items.slice(0, 5)
}

function handleAddItem() {
  if (!expandedListId.value) return
  if (!newItemName.value.trim()) return

  const item = DS.createItem(
      newItemName.value,
      beschreibung.value,
      Number(menge.value),
      selectedUnit.value,
      Number(preis.value),
      category.value
  )

  DS.addItemToList(expandedListId.value, item)

  newItemName.value = ''
  selectedUnit.value = ''
  menge.value = ''
  preis.value = ''
  beschreibung.value = ''
  category.value = ''

  showAddDialog.value = false
}

function exportSingleList(id) {
  const list = lists.value[id]
  if (!list) return

  const blob = new Blob([JSON.stringify(list, null, 2)], {
    type: "application/json"
  })

  const url = URL.createObjectURL(blob)
  const a = document.createElement("a")
  a.href = url
  a.download = `${list.name}.json`
  a.click()
  URL.revokeObjectURL(url)
}

function formatPrice(value) {
  if (!value) return '0,00 €'
  return Number(value).toFixed(2).replace('.', ',') + ' €'
}
</script>

<template>
  <div v-if="!expandedListId" class="list-grid">
    <div
        v-for="(list, id) in lists"
        :key="id"
        class="list-card"
        @click="expandedListId = id"
    >
      <div class="list-card-header">
        {{ list.name }} ({{ list.items.length }})
      </div>

      <div class="list-card-body">
        <ul class="preview-list">
          <li v-for="item in preview(list)" :key="item.id">
            • {{ item.name }} — {{ item.menge }} {{ unitLabel(item.unit) }}
          </li>
        </ul>

        <p v-if="list.items.length > 5" class="preview-more">… mehr</p>
      </div>
    </div>
  </div>

  <div v-else class="expanded">
    <div class="expanded-header">
      <button class="header-btn back" @click="expandedListId = null">← Zurück</button>
      <h2 class="expanded-title">{{ lists[expandedListId].name }} ({{ lists[expandedListId].items.length }})</h2>
      <button class="header-btn export" @click="exportSingleList(expandedListId)">⬇️ Liste exportieren</button>
    </div>

    <table class="item-table">
      <thead>
      <tr>
        <th>Name</th>
        <th>Menge</th>
        <th>Einheit</th>
        <th>Preis</th>
        <th>Kategorie</th>
        <th>Beschreibung</th>
      </tr>
      </thead>

      <tbody>
      <tr v-for="item in lists[expandedListId].items" :key="item.id">
        <td>{{ item.name }}</td>
        <td>{{ item.menge }}</td>
        <td>{{ unitLabel(item.unit) }}</td>
        <td>{{ formatPrice(item.preis) }}</td>
        <td>{{ item.category }}</td>
        <td>{{ item.beschreibung }}</td>
      </tr>
      </tbody>
    </table>

    <div class="table-add-container">
      <button class="table-add-btn" @click="showAddDialog = true">➕ Neues Item</button>
    </div>

    <div v-if="showAddDialog" class="modal-backdrop" @click.self="showAddDialog = false">
      <div class="modal">
        <h2>Neues Item</h2>

        <input v-model="newItemName" placeholder="Artikelname" />
        <input v-model="menge" type="number" min="1" placeholder="Menge" />
        <input v-model="preis" type="number" min="0" placeholder="Preis" />
        <input v-model="category" placeholder="Kategorie" />
        <input v-model="beschreibung" placeholder="Beschreibung" />

        <select v-model="selectedUnit">
          <option disabled value="">Einheit…</option>
          <option v-for="u in unitsData.units" :key="u.value" :value="u.value">
            {{ u.label }}
          </option>
        </select>

        <button @click="handleAddItem">Hinzufügen</button>
        <button class="cancel" @click="showAddDialog = false">Abbrechen</button>
      </div>
    </div>
  </div>
</template>
