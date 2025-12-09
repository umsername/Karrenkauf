<script setup>
import { ref } from "vue"
import { login } from "@/services/authService.js"
import { useRouter } from "vue-router"

const router = useRouter()
const username = ref("")
const password = ref("")
const errorMessage = ref("")
const isLoading = ref(false)

async function handleLogin() {
    if (!username.value || !password.value) {
        errorMessage.value = "Bitte Benutzername und Passwort eingeben"
        return
    }

    isLoading.value = true
    errorMessage.value = ""

    try {
        const result = await login(username.value, password.value)

        if (result.success) {
            // Login erfolgreich - zur Profilseite weiterleiten
            router.push("/profile")
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

      <h1 class="auth-title">Login</h1>

      <form class="auth-form" @submit.prevent="handleLogin">

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
          {{ isLoading ? "Laden..." : "Login" }}
        </button>
      </form>

      <div class="auth-links">
        <a @click="$router.push('/forgot-password')">Passwort vergessen?</a>
        <a @click="$router.push('/register')">Noch kein Konto? Registrieren</a>
      </div>

    </div>
  </div>
</template>
