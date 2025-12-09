<script setup>
import { ref } from "vue"
import { registerUser } from "@/services/authService.js"
import { useRouter } from "vue-router"

const router = useRouter()
const username = ref("")
const password = ref("")
const errorMessage = ref("")
const isLoading = ref(false)

async function register() {
    // Basic client-side validation
    if (!username.value || !password.value) {
        errorMessage.value = "Bitte Benutzername und Passwort eingeben"
        return
    }

    isLoading.value = true
    errorMessage.value = ""

    try {
        const result = await registerUser(username.value, password.value)

        if (result.success) {
            // Registrierung erfolgreich - zur Login-Seite weiterleiten
            router.push("/login")
        } else {
            errorMessage.value = result.message
        }
    } catch (error) {
        errorMessage.value = "Ein unerwarteter Fehler ist aufgetreten"
    } finally {
        isLoading.value = false
    }
}
</script>

<template>
  <div class="auth-wrapper">
    <div class="auth-card">

      <h1 class="auth-title">Registrieren</h1>

      <form class="auth-form" @submit.prevent="register">

        <div v-if="errorMessage" class="error-message">
          {{ errorMessage }}
        </div>

        <div>
          <label>Username</label>
          <input v-model="username" type="text" class="auth-input" :disabled="isLoading" />
        </div>

        <div>
          <label>Password</label>
          <input v-model="password" type="password" class="auth-input" :disabled="isLoading" />
        </div>

        <button class="auth-btn" :disabled="isLoading">
          {{ isLoading ? "Laden..." : "Registrieren" }}
        </button>
      </form>

      <div class="auth-links">
        <a @click="$router.push('/login')">Schon registriert? Login</a>
      </div>

    </div>
  </div>
</template>
