<script setup>
import { ref } from "vue"

const username = ref("")
const password = ref("")

function login() {

}
</script>

<template>
  <div class="auth-wrapper">
    <div class="auth-card">

      <h1 class="auth-title">Login</h1>

      <form class="auth-form" @submit.prevent="login">

        <div>
          <label>Username</label>
          <input v-model="username" type="text" class="auth-input" />
        </div>

        <div>
          <label>Password</label>
          <input v-model="password" type="password" class="auth-input" />
        </div>

        <button class="auth-btn">Login</button>
      </form>

      <div class="auth-links">
        <a @click="$router.push('/forgot-password')">Passwort vergessen?</a>
        <a @click="$router.push('/register')">Noch kein Konto? Registrieren</a>
      </div>

    </div>
  </div>
</template>
