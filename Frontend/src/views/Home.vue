<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import * as DS from '@/store/dataStore.js'
import unitsData from '@/assets/data/units.json'
import { syncLists } from '@/services/syncService.js'
import { isAuthenticated, getCurrentUser } from '@/services/authService.js'
import { shareListWithUser, getSharedUsers, unshareListWithUser } from '@/services/apiService.js'

import {
  PencilSquareIcon,
  TrashIcon,
  PlusIcon,
  ArrowDownTrayIcon,
  CheckIcon,
  MinusIcon,
  ChevronLeftIcon,
  ChevronUpDownIcon,
  ChevronUpIcon,
  ChevronDownIcon,
  MagnifyingGlassIcon,
  ArrowPathIcon,
  ShareIcon,
  XMarkIcon,
  UserPlusIcon,
  UserMinusIcon
} from '@heroicons/vue/24/outline'

// Grundzustand
const lists = computed(() => DS.getData().lists)
const expandedListId = ref(null)
const sortColumn = ref(null)
const sortDirection = ref('neutral')  // neutral | asc | desc

// Zustände für Modals
const isEditModalOpen = ref(false)
const isDeleteConfirmOpen = ref(false)
const isNewListModalOpen = ref(false)
const isRenameListModalOpen = ref(false)
const isShareModalOpen = ref(false)
const newListName = ref('')
const editableItem = ref(null)

// Suchfunktionalität
const searchTerm = ref('')

// Sync-Status
const isSyncing = ref(false)
const syncMessage = ref('')

// Share-Funktionalität
const shareMessage = ref('')
const shareUsername = ref('')
const sharedUsers = ref([])
const isLoadingShares = ref(false)

async function openShareModal() {
  if (!isAuthenticated()) {
    shareMessage.value = 'Bitte melden Sie sich an, um Listen zu teilen'
    setTimeout(() => shareMessage.value = '', 3000)
    return
  }

  isShareModalOpen.value = true
  shareUsername.value = ''
  shareMessage.value = ''
  await loadSharedUsers()
}

async function loadSharedUsers() {
  if (!expandedListId.value) return
  
  isLoadingShares.value = true
  try {
    const response = await getSharedUsers(expandedListId.value)
    sharedUsers.value = response.sharedWith || []
  } catch (error) {
    console.error('Failed to load shared users:', error)
    sharedUsers.value = []
  } finally {
    isLoadingShares.value = false
  }
}

async function shareListWithUsername() {
  if (!shareUsername.value.trim()) {
    shareMessage.value = 'Bitte geben Sie einen Benutzernamen ein'
    return
  }

  try {
    const result = await shareListWithUser(expandedListId.value, shareUsername.value.trim())
    shareMessage.value = result
    shareUsername.value = ''
    await loadSharedUsers()
    
    setTimeout(() => {
      shareMessage.value = ''
    }, 3000)
  } catch (error) {
    if (error.response?.data) {
      shareMessage.value = error.response.data
    } else {
      shareMessage.value = 'Fehler beim Teilen der Liste'
    }
    setTimeout(() => {
      shareMessage.value = ''
    }, 3000)
  }
}

async function removeSharedUser(username) {
  try {
    const result = await unshareListWithUser(expandedListId.value, username)
    shareMessage.value = result
    await loadSharedUsers()
    
    setTimeout(() => {
      shareMessage.value = ''
    }, 3000)
  } catch (error) {
    if (error.response?.data) {
      shareMessage.value = error.response.data
    } else {
      shareMessage.value = 'Fehler beim Entfernen des Benutzers'
    }
    setTimeout(() => {
      shareMessage.value = ''
    }, 3000)
  }
}

// Zustände für den Auswahlmodus
const isSelectionMode = ref(false)
const selectedLists = ref(new Set())

// Event Listener
function resetToOverview() {
  expandedListId.value = null
  isSelectionMode.value = false
  selectedLists.value.clear()
  searchTerm.value = '' // Suchkriterium löschen beim Verlassen der Liste
}
onMounted(() => {
  window.addEventListener("karrenkauf-home-reset", resetToOverview)
})
onUnmounted(() => {
  window.removeEventListener("karrenkauf-home-reset", resetToOverview)
})

// Hilfsfunktionen
const unitLabel = (value) => unitsData.units.find(u => u.value === value)?.label || value
const formatPrice = (value) => (typeof value === 'number' ? value.toFixed(2).replace('.', ',') + ' €' : '0,00 €')
const previewItems = (list) => list.items.slice(0, 5)

// Check if a list is shared with the current user (not owned by them)
const isSharedList = (list) => {
  if (!isAuthenticated()) return false
  const currentUser = getCurrentUser()
  if (!currentUser || !currentUser.username) return false
  return list.owner && list.owner !== currentUser.username
}

// Gesamtpreis-Berechnung
const calculateTotalPrice = (list) => {
  if (!list || !list.items) return 0

  // Einheiten, bei denen die Menge nicht multipliziert werden soll
  const singleUnitPrices = ["g", "kg", "ml", "l"]

  return list.items.reduce((total, item) => {
    const price = item.preis || 0
    const quantity = item.menge || 0
    const unit = item.unit || ""

    if (singleUnitPrices.includes(unit)) {
      return total + price // Nur Preis der Packung, nicht multiplizieren
    } else {
      return total + price * quantity // normale Berechnung
    }
  }, 0)
}

// Item-Interaktionen
const increaseQuantity = (item) => DS.updateItem(expandedListId.value, item.id, { menge: item.menge + 1 })
const decreaseQuantity = (item) => item.menge > 0 && DS.updateItem(expandedListId.value, item.id, { menge: item.menge - 1 })
const toggleChecked = (item) => DS.updateItem(expandedListId.value, item.id, { checked: !item.checked })
const deleteItem = (item) => DS.deleteItem(expandedListId.value, item.id)

//  Modal-Logik (Item & Neue Liste)
function openNewItemModal() {
  editableItem.value = { id: null, name: '', menge: null, unit: '', preis: null, category: '', beschreibung: '' }
  isEditModalOpen.value = true
}
function openEditModal(item) {
  editableItem.value = { ...item }
  isEditModalOpen.value = true
}
function saveItem() {
  if (!editableItem.value || !editableItem.value.name.trim()) return
  const itemToSave = { ...editableItem.value, menge: editableItem.value.menge || 0, preis: editableItem.value.preis || 0 }
  if (itemToSave.id) {
    DS.updateItem(expandedListId.value, itemToSave.id, itemToSave)
  } else {
    const { name, beschreibung, menge, unit, preis, category } = itemToSave
    const newItem = DS.createItem(name, beschreibung, menge, unit, preis, category)
    DS.addItemToList(expandedListId.value, newItem)
  }
  isEditModalOpen.value = false
}

function openNewListModal() {
  newListName.value = ''
  isNewListModalOpen.value = true
}
function createNewList() {
  if (!newListName.value.trim()) return
  const newListId = DS.createList(newListName.value, 'user')
  isNewListModalOpen.value = false
  expandedListId.value = newListId
}

// Logik für "Liste umbenennen"
function openRenameListModal() {
  if (expandedListId.value) {
    newListName.value = lists.value[expandedListId.value].name
    isRenameListModalOpen.value = true
  }
}
function renameList() {
  if (!newListName.value.trim() || !expandedListId.value) return
  DS.updateList(expandedListId.value, { name: newListName.value })
  isRenameListModalOpen.value = false
}

// Listen-Lösch-Logik
const openDeleteConfirm = () => isDeleteConfirmOpen.value = true
function confirmDeleteList() {
  if (expandedListId.value) {
    DS.deleteList(expandedListId.value)
    resetToOverview()
  }
  isDeleteConfirmOpen.value = false
}

// Auswahlmodus-Logik
function toggleSelectionMode() {
  isSelectionMode.value = !isSelectionMode.value
  selectedLists.value.clear()
}
function toggleListSelection(listId) {
  if (selectedLists.value.has(listId)) {
    selectedLists.value.delete(listId)
  } else {
    selectedLists.value.add(listId)
  }
}
function selectAllLists() {
  Object.keys(lists.value).forEach(id => selectedLists.value.add(id))
}
function deselectAllLists() {
  selectedLists.value.clear()
}
function deleteSelectedLists() {
  if (selectedLists.value.size === 0) return
  selectedLists.value.forEach(id => DS.deleteList(id))
  resetToOverview()
}
function exportSelectedLists() {
  if (selectedLists.value.size === 0) return
  console.log("Exportiere Listen:", Array.from(selectedLists.value))
  DS.exportMultipleLists(selectedLists.value)
}

// Sync-Funktionalität
async function handleSync() {
  if (!isAuthenticated()) {
    syncMessage.value = 'Bitte melden Sie sich an, um zu synchronisieren'
    setTimeout(() => syncMessage.value = '', 3000)
    return
  }

  isSyncing.value = true
  syncMessage.value = ''

  try {
    const result = await syncLists()
    syncMessage.value = result.message
    setTimeout(() => syncMessage.value = '', 5000)
  } catch (error) {
    syncMessage.value = 'Synchronisation fehlgeschlagen'
    setTimeout(() => syncMessage.value = '', 5000)
  } finally {
    isSyncing.value = false
  }
}

function toggleSort(column) {
  if (sortColumn.value !== column) {
    sortColumn.value = column
    sortDirection.value = 'asc'
  } else {
    if (sortDirection.value === 'asc') sortDirection.value = 'desc'
    else if (sortDirection.value === 'desc') sortDirection.value = 'neutral'
    else sortDirection.value = 'asc'
  }
}

function sortDirectionFor(column) {
  if (sortColumn.value !== column) return 'neutral'
  return sortDirection.value
}

const sortedItems = computed(() => {
  if (!expandedListId.value) return []

  let items = [...lists.value[expandedListId.value].items]
  
  // Suchfilter anwenden
  if (searchTerm.value.trim()) {
    const search = searchTerm.value.toLowerCase().trim()
    items = items.filter(item => 
      item.name && item.name.toLowerCase().includes(search)
    )
  }
  
  // Sortierung anwenden
  if (sortDirection.value === 'neutral' || !sortColumn.value) return items

  return items.sort((a, b) => {
    const vA = a[sortColumn.value]
    const vB = b[sortColumn.value]

    if (vA < vB) return sortDirection.value === 'asc' ? -1 : 1
    if (vA > vB) return sortDirection.value === 'asc' ? 1 : -1
    return 0
  })
})

</script>

<template>
  <!-- ============================================================
       DETAILANSICHT
       ============================================================ -->
  <div v-if="expandedListId && lists[expandedListId]" class="expanded">
    <div class="expanded-header">
      <button class="header-btn back" @click="resetToOverview">
        <ChevronLeftIcon class="icon" />
        <span>Zurück</span>
      </button>

      <h2 class="expanded-title">
        {{ lists[expandedListId].name }} ({{ lists[expandedListId].items.length }})
      </h2>

      <div class="header-actions">
        <button @click="openShareModal" class="header-btn">
          <ShareIcon class="icon" />
          <span>Teilen</span>
        </button>

        <button @click="openRenameListModal" class="header-btn">
          <PencilSquareIcon class="icon" />
          <span>Umbenennen</span>
        </button>

        <button class="header-btn" @click="DS.exportList(expandedListId)">
          <ArrowDownTrayIcon class="icon" />
          <span>Export</span>
        </button>

        <button @click="openDeleteConfirm" class="header-btn delete-list-btn">
          <TrashIcon class="icon" />
        </button>
      </div>
    </div>

    <!-- Share-Nachricht -->
    <div v-if="shareMessage" class="share-message">
      {{ shareMessage }}
    </div>

    <!-- Inline-Suchfeld -->
    <div class="search-box">
      <div class="search-input-wrapper">
        <MagnifyingGlassIcon class="search-icon" />
        <input 
          v-model="searchTerm" 
          type="text" 
          placeholder="Items durchsuchen..." 
          class="search-input"
        />
        <button 
          v-if="searchTerm" 
          @click="searchTerm = ''" 
          class="clear-search-btn"
          title="Suche löschen"
        >
          <XMarkIcon class="icon-sm" />
        </button>
      </div>
      <p v-if="searchTerm" class="search-result-info">
        {{ sortedItems.length }} Ergebnis(se) gefunden
      </p>
    </div>

    <div class="table-container">
      <table class="item-table">
        <thead>
          <tr>
            <th class="col-actions-header">Aktionen</th>

            <!-- NAME SORT -->
            <th>
              <button class="tbh-btn-sort" @click="toggleSort('name')">
                Name
                <span>
                  <ChevronUpDownIcon
                    v-if="sortDirectionFor('name') === 'neutral'"
                    class="icon-sm"
                  />
                  <ChevronUpIcon
                    v-else-if="sortDirectionFor('name') === 'asc'"
                    class="icon-sm"
                  />
                  <ChevronDownIcon
                    v-else
                    class="icon-sm"
                  />
                </span>
              </button>
            </th>

            <th>Menge</th>
            <th>Einheit</th>

            <!-- PREIS SORT -->
            <th>
              <button class="tbh-btn-sort" @click="toggleSort('preis')">
                Preis
                <span>
                  <ChevronUpDownIcon
                    v-if="sortDirectionFor('preis') === 'neutral'"
                    class="icon-sm"
                  />
                  <ChevronUpIcon
                    v-else-if="sortDirectionFor('preis') === 'asc'"
                    class="icon-sm"
                  />
                  <ChevronDownIcon
                    v-else
                    class="icon-sm"
                  />
                </span>
              </button>
            </th>

            <!-- KATEGORIE SORT -->
            <th>
              <button class="tbh-btn-sort" @click="toggleSort('category')">
                Kategorie
                <span>
                  <ChevronUpDownIcon
                    v-if="sortDirectionFor('category') === 'neutral'"
                    class="icon-sm"
                  />
                  <ChevronUpIcon
                    v-else-if="sortDirectionFor('category') === 'asc'"
                    class="icon-sm"
                  />
                  <ChevronDownIcon
                    v-else
                    class="icon-sm"
                  />
                </span>
              </button>
            </th>

            <th>Beschreibung</th>
          </tr>
        </thead>

        <tbody>
          <tr
            v-for="item in sortedItems"
            :key="item.id"
            :class="{ checked: item.checked }"
          >
            <td class="actions-cell">
              <button class="tbl-btn check" @click.stop="toggleChecked(item)">
                <CheckIcon class="icon-sm" />
              </button>

              <button class="tbl-btn edit" @click.stop="openEditModal(item)">
                <PencilSquareIcon class="icon-sm" />
              </button>

              <button class="tbl-btn delete" @click.stop="deleteItem(item)">
                <TrashIcon class="icon-sm" />
              </button>
            </td>

            <td>{{ item.name }}</td>

            <td>
              <div class="quantity-control">
                <button @click.stop="decreaseQuantity(item)" :disabled="item.menge === 0">
                  <MinusIcon class="icon-sm" />
                </button>

                <span>{{ item.menge }}</span>

                <button @click.stop="increaseQuantity(item)">
                  <PlusIcon class="icon-sm" />
                </button>
              </div>
            </td>

            <td>{{ unitLabel(item.unit) }}</td>
            <td>{{ formatPrice(item.preis) }}</td>
            <td>{{ item.category }}</td>
            <td>{{ item.beschreibung }}</td>
          </tr>
        </tbody>

        <tfoot>
          <tr>
            <td colspan="2" class="total-price-label">Gesamtpreis:</td>
            <td colspan="2" class="total-price-value">
              {{ formatPrice(calculateTotalPrice(lists[expandedListId])) }}
            </td>
            <td colspan="2"></td>

            <td class="add-item-cell">
              <button class="table-add-btn" @click="openNewItemModal">
                <PlusIcon class="icon-sm" />
                Neues Item
              </button>
            </td>
          </tr>
        </tfoot>
      </table>
    </div>
  </div>

  <!-- ============================================================
       ÜBERSICHTSANSICHT
       ============================================================ -->
  <div v-else>
    <div class="overview-container">
      <!-- Sync-Nachricht anzeigen -->
      <div v-if="syncMessage" class="sync-message">
        {{ syncMessage }}
      </div>

      <div class="global-actions">
        <div v-if="!isSelectionMode" class="normal-actions">
          <button @click="openNewListModal" class="action-btn primary">
            <PlusIcon class="icon" />
            <span>Neue Liste</span>
          </button>

          <button
            v-if="Object.keys(lists).length > 0"
            @click="toggleSelectionMode"
            class="action-btn"
          >
            <PencilSquareIcon class="icon" />
            <span>Bearbeiten</span>
          </button>

          <button
            v-if="isAuthenticated()"
            @click="handleSync"
            :disabled="isSyncing"
            class="action-btn sync-btn"
          >
            <ArrowPathIcon class="icon" :class="{ 'spinning': isSyncing }" />
            <span>{{ isSyncing ? 'Synchronisiere...' : 'Synchronisieren' }}</span>
          </button>
        </div>

        <div v-else class="selection-actions">
          <button @click="selectAllLists">Alle auswählen</button>
          <button @click="deselectAllLists" :disabled="selectedLists.size === 0">
            Auswahl aufheben
          </button>

          <button @click="exportSelectedLists" :disabled="selectedLists.size === 0">
            <ArrowDownTrayIcon class="icon-sm" />
            <span>Exportieren</span>
          </button>

          <button
            @click="deleteSelectedLists"
            :disabled="selectedLists.size === 0"
            class="delete"
          >
            <TrashIcon class="icon-sm" />
            <span>Löschen</span>
          </button>

          <button @click="toggleSelectionMode" class="cancel">
            Abbrechen
          </button>
        </div>
      </div>

      <div class="list-grid">
        <p v-if="Object.keys(lists).length === 0" class="text-center w-full">
          Keine Listen vorhanden. Erstelle eine neue!
        </p>

        <div
          v-for="(list, id) in lists"
          :key="id"
          class="list-card"
          :class="{ 'is-selected': selectedLists.has(id) }"
          @click="isSelectionMode ? toggleListSelection(id) : (expandedListId = id)"
        >
          <div v-if="isSelectionMode" class="selection-checkbox">
            <div class="checkbox-visual">
              <CheckIcon v-if="selectedLists.has(id)" class="icon-sm" />
            </div>
          </div>

          <div class="list-card-header">
            {{ list.name }} ({{ list.items.length }})
            <span v-if="isSharedList(list)" class="shared-badge" title="Von anderen geteilt">
              <ShareIcon class="icon-xs" />
            </span>
          </div>

          <div class="list-card-body">
            <ul class="preview-list">
              <li
                v-for="item in previewItems(list)"
                :key="item.id"
                :class="{ 'checked-item': item.checked }"
              >
                <div class="preview-item-layout">
                  <span class="item-name">• {{ item.name }}</span>
                  <span
                    class="item-quantity"
                    v-if="item.menge > 0"
                  >
                    {{ item.menge }} {{ unitLabel(item.unit) }}
                  </span>
                </div>
              </li>
            </ul>

            <p v-if="list.items.length > 5" class="preview-more">
              … und {{ list.items.length - 5 }} weitere
            </p>
          </div>

          <div class="list-card-footer">
            <span>Gesamtpreis:</span>
            <strong>{{ formatPrice(calculateTotalPrice(list)) }}</strong>
          </div>
        </div>
      </div>
    </div>
  </div>

  <!-- ============================================================
       MODALS
       ============================================================ -->

  <!-- UUID: IDENTICAL TO YOUR VERSION, NO ICON CHANGES NEEDED -->

  <div v-if="isEditModalOpen" class="modal-backdrop" @click.self="isEditModalOpen = false">
    <div class="modal">
      <h2 class="modal-title">{{ editableItem.id ? 'Item bearbeiten' : 'Neues Item' }}</h2>
      <form @submit.prevent="saveItem" class="modal-form">
        <div class="form-group">
          <label for="name">Name</label>
          <input v-model="editableItem.name" id="name" type="text" required />
        </div>

        <div class="form-group">
          <label for="menge">Menge</label>
          <input v-model.number="editableItem.menge" id="menge" type="number" min="0" />
        </div>

        <div class="form-group">
          <label for="einheit">Einheit</label>
          <select v-model="editableItem.unit" id="einheit" required>
            <option value="" disabled>Einheit auswählen...</option>
            <option
              v-for="u in unitsData.units"
              :key="u.value"
              :value="u.value"
            >
              {{ u.label }}
            </option>
          </select>
        </div>

        <div class="form-group">
          <label for="preis">Preis (€)</label>
          <input
            v-model.number="editableItem.preis"
            id="preis"
            type="number"
            min="0"
            step="0.01"
          />
        </div>

        <div class="form-group">
          <label for="kategorie">Kategorie</label>
          <input v-model="editableItem.category" id="kategorie" type="text" />
        </div>

        <div class="form-group">
          <label for="beschreibung">Beschreibung</label>
          <input v-model="editableItem.beschreibung" id="beschreibung" type="text" />
        </div>

        <div class="modal-actions">
          <button type="button" class="cancel" @click="isEditModalOpen = false">
            Abbrechen
          </button>

          <button type="submit">
            {{ editableItem.id ? 'Speichern' : 'Hinzufügen' }}
          </button>
        </div>
      </form>
    </div>
  </div>

  <div v-if="isDeleteConfirmOpen" class="modal-backdrop" @click.self="isDeleteConfirmOpen = false">
    <div class="modal">
      <h2 class="modal-title">Liste löschen?</h2>
      <p>
        Möchtest du die Liste "{{ lists[expandedListId]?.name }}" wirklich endgültig löschen?
      </p>

      <div class="modal-actions">
        <button class="cancel" @click="isDeleteConfirmOpen = false">Abbrechen</button>

        <button class="delete" @click="confirmDeleteList">Löschen</button>
      </div>
    </div>
  </div>

  <div v-if="isNewListModalOpen" class="modal-backdrop" @click.self="isNewListModalOpen = false">
    <div class="modal">
      <h2 class="modal-title">Neue Einkaufsliste</h2>

      <form @submit.prevent="createNewList" class="modal-form">
        <div class="form-group">
          <label for="new-list-name">Name der Liste</label>
          <input v-model="newListName" id="new-list-name" type="text" required />
        </div>

        <div class="modal-actions">
          <button type="button" class="cancel" @click="isNewListModalOpen = false">
            Abbrechen
          </button>

          <button type="submit">Erstellen</button>
        </div>
      </form>
    </div>
  </div>

  <div v-if="isRenameListModalOpen" class="modal-backdrop" @click.self="isRenameListModalOpen = false">
    <div class="modal">
      <h2 class="modal-title">Liste umbenennen</h2>

      <form @submit.prevent="renameList" class="modal-form">
        <div class="form-group">
          <label for="rename-list-name">Neuer Name der Liste</label>
          <input v-model="newListName" id="rename-list-name" type="text" required />
        </div>

        <div class="modal-actions">
          <button type="button" class="cancel" @click="isRenameListModalOpen = false">
            Abbrechen
          </button>

          <button type="submit">Umbenennen</button>
        </div>
      </form>
    </div>
  </div>

  <!-- Share Modal -->
  <div v-if="isShareModalOpen" class="modal-backdrop" @click.self="isShareModalOpen = false">
    <div class="modal share-modal">
      <h2 class="modal-title">Liste teilen</h2>

      <div v-if="shareMessage" class="share-message">
        {{ shareMessage }}
      </div>

      <form @submit.prevent="shareListWithUsername" class="modal-form">
        <div class="form-group">
          <label for="share-username">Benutzername</label>
          <div style="display: flex; gap: 0.5rem;">
            <input 
              v-model="shareUsername" 
              id="share-username" 
              type="text" 
              placeholder="Benutzername eingeben..."
              style="flex: 1;"
            />
            <button type="submit" class="action-btn primary">
              <UserPlusIcon class="icon-sm" />
              <span>Hinzufügen</span>
            </button>
          </div>
        </div>
      </form>

      <div class="shared-users-section">
        <h3 style="margin-bottom: 0.75rem; font-size: 1rem; font-weight: 600;">
          Geteilt mit:
        </h3>

        <div v-if="isLoadingShares" style="text-align: center; padding: 1rem;">
          Lädt...
        </div>

        <div v-else-if="sharedUsers.length === 0" style="color: #666; font-style: italic; padding: 1rem;">
          Diese Liste wurde noch nicht geteilt
        </div>

        <ul v-else class="shared-users-list">
          <li 
            v-for="username in sharedUsers" 
            :key="username"
            class="shared-user-item"
          >
            <span>{{ username }}</span>
            <button 
              @click="removeSharedUser(username)"
              class="remove-user-btn"
              title="Zugriff entfernen"
            >
              <UserMinusIcon class="icon-sm" />
            </button>
          </li>
        </ul>
      </div>

      <div class="modal-actions">
        <button type="button" class="cancel" @click="isShareModalOpen = false">
          Schließen
        </button>
      </div>
    </div>
  </div>
</template>
