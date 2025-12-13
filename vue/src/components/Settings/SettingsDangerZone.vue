<template>
  <p class="title">Danger zone</p>
  <div class="dangercont">
    <button @click="resetSettings" @mouseleave="resetProtection" testid="reset-settings-btn">
      {{ settingsProtection ? "Reset settings" : "Confirm" }}
    </button>
    <button @click="resetDB" @mouseleave="resetProtection" testid="reset-db-btn">
      {{ dbProtection ? "Reset database" : "Confirm" }}
    </button>
  </div>
</template>

<script setup lang="ts">
import { ref } from "vue";
import axios from "axios";

const settingsProtection = ref(true);
const dbProtection = ref(true);

const resetSettings = () => {
  if (settingsProtection.value === true) {
    settingsProtection.value = false;
  } else {
    axios.post("/api/resetSettings").catch((error) => {
      console.error(error);
    });
  }
};

const resetDB = () => {
  if (dbProtection.value === true) {
    dbProtection.value = false;
  } else {
    axios.post("/api/resetDB").catch((error) => {
      console.error(error);
    });
  }
};

const resetProtection = () => {
  settingsProtection.value = true;
  dbProtection.value = true;
};
</script>

<style scoped>
.title {
  font-weight: bold;
}
.dangercont {
  display: flex;
  justify-content: space-evenly;
}
.danger button {
  border-radius: 5px;
  background-color: transparent;
  color: var(--contrast-color);
  border: 2px solid red;
  padding: 4px;
  width: 120px;
  cursor: pointer;
}
.danger button:hover {
  background-color: red;
}
</style>
