<script setup>
import { ref } from 'vue'
import units from '../units.json'

defineProps({
  msg: String,
})

const selectedUnit = ref('')
const count = ref(0)
</script>

<template>
  <h1>Karrenkauf</h1>
  <p> Die morderne Art Einkaufslisten zu gestalten, ohne Risiko und alles lokal!</p>
  <img src="/src/assets/karrenkauf.jfif" alt="KARRENKAUF" width="200" height="200"></img>

  <div class="">
    <form action="">
      <input type="text" id="newList" name="newList" placeholder="Name der neuen Einkaufsliste" size="30"></input>
      <input type="submit" value="Erstellen"></input>
    </form>
  </div>

    <div class="">
    <form action="">
      <input type="text" id="newItem" name="newItem" placeholder="Neues Produkt" size="30"/>
        <select v-model="selectedUnit">
          <option disabled value="">Einheit wählen...</option>
          <option v-for="unit in units.units" :key="unit.value" :value="unit.value">
            {{ unit.label }}
          </option>
        </select>
      <input type="submit" value="Hinzufügen"/>
    </form>
  </div>

  <h1>{{ msg }}</h1>

  <div class="card">
    <button type="button" @click="count++">count is {{ count }}</button>
    <p>
      Edit
      <code>components/HelloWorld.vue</code> to test HMR
    </p>
  </div>

  <p>
    Check out
    <a href="https://vuejs.org/guide/quick-start.html#local" target="_blank"
      >create-vue</a
    >, the official Vue + Vite starter
  </p>
  <p>
    Learn more about IDE Support for Vue in the
    <a
      href="https://vuejs.org/guide/scaling-up/tooling.html#ide-support"
      target="_blank"
      >Vue Docs Scaling up Guide</a
    >.
  </p>
  <p class="read-the-docs">Click on the Vite and Vue logos to learn more</p>
</template>

<style scoped>
.read-the-docs {
  color: #888;
}
</style>
