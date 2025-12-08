<script setup>
import { ref } from "vue"

const email = ref("")

function recover() {
    console.log("Recover password for:", email.value)
}
</script>

<template>
  <div class="auth-wrapper">
    <div class="auth-card">

      <h1 class="auth-title">Passwort vergessen?</h1>
      <h2> PECH gehabt! </h2>


      <div class="auth-links">
        <a @click="$router.push('/login')">Zurück zum Login</a>
        <a @click="$router.push('/register')">Noch kein Konto? Registrieren</a>
      </div>

    </div>
  </div>
</template>
