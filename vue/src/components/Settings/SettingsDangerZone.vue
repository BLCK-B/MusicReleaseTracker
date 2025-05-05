<!--
  -         MusicReleaseTracker
  -         Copyright (C) 2023 - 2025 BLCK
  -         This program is free software: you can redistribute it and/or modify
  -         it under the terms of the GNU General Public License as published by
  -         the Free Software Foundation, either version 3 of the License, or
  -         (at your option) any later version.
  -         This program is distributed in the hope that it will be useful,
  -         but WITHOUT ANY WARRANTY; without even the implied warranty of
  -         MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  -         GNU General Public License for more details.
  -         You should have received a copy of the GNU General Public License
  -         along with this program.  If not, see <https://www.gnu.org/licenses/>.
  -->

<template>
  <p class="title">Danger zone</p>
  <div class="dangercont">
    <button v-if="settingsProtection" @click="resetSettings">Reset settings</button>
    <button v-if="!settingsProtection" @click="resetSettings" @mouseleave="resetProtection">confirm</button>

    <button v-if="dbProtection" @click="resetDB">Reset database</button>
    <button v-if="!dbProtection" @click="resetDB" @mouseleave="resetProtection">confirm</button>
  </div>
</template>

<script setup>
import {ref} from "vue";
import axios from "axios";

const settingsProtection = ref(true);
const dbProtection = ref(true);

// default the settings
const resetSettings = () => {
  if (settingsProtection.value === true) {
    settingsProtection.value = false;
  } else {
    axios.post("/api/resetSettings").catch((error) => {
      console.error(error);
    });
  }
};

// default the database
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
}
.danger button:hover {
  background-color: red;
}

.disabled {
  opacity: 0.3;
}
</style>
