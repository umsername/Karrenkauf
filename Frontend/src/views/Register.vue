<script setup>
import { ref } from "vue"

const username = ref("")
const password = ref("")

function register() {

}
</script>

<template>
  <div class="auth-wrapper">
    <div class="auth-card">

      <h1 class="auth-title">Registrieren</h1>

      <form class="auth-form" @submit.prevent="register">

        <div>
          <label>Username</label>
          <input v-model="username" type="text" class="auth-input" />
        </div>

        <div>
          <label>Password</label>
          <input v-model="password" type="password" class="auth-input" />
        </div>

        <button class="auth-btn">Registrieren</button>
      </form>

      <div class="auth-links">
        <a @click="$router.push('/login')">Schon registriert? Login</a>
      </div>

    </div>
  </div>
</template>
