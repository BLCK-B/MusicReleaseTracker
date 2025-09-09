<template>
  <div class="preview">
    <h1>preview</h1>
    <p v-if="!hideTable">Confirm only if the table has usable information.</p>
    <p v-if="hideTable">Incorrect link.</p>
    <button @click="clickConfirm" class="imgbutton" :disabled="hideTable" data-testid="confirm-button">
      <img v-if="primaryColor !== 'light'" class="image" src="../icons/confirmdark.png" alt="OK" />
      <img v-if="primaryColor === 'light'" class="image" src="../icons/confirmlight.png" alt="OK" />
    </button>
    <button @click="clickCancel" class="imgbutton" data-testid="cancel-button">
      <img v-if="primaryColor !== 'light'" class="image" src="../icons/crossdark.png" alt="X" />
      <img v-if="primaryColor === 'light'" class="image" src="../icons/crosslight.png" alt="X" />
    </button>
  </div>
</template>

<script setup lang="ts">
import { computed } from "vue";
import { useStore } from "vuex";
import axios from "axios";

const store = useStore();
const tableData = computed(() => store.state.tableData);
const primaryColor = computed(() => store.state.primaryColor);
const sourceTab = computed(() => store.state.sourceTab);
const selectedArtist = computed(() => store.state.selectedArtist);

const hideTable = computed(() => tableData.value.length === 0);

function clickCancel() {
  axios
    .post("/api/cleanArtistSource", null, {
      params: {
        source: sourceTab.value,
        artist: selectedArtist.value,
      },
    })
    .catch((error) => {
      console.error(error);
    });
  store.commit("SET_PREVIEW_VIS", false);
}

function clickConfirm() {
  axios
    .post("/api/confirmSaveUrl", null, {
      params: {
        source: sourceTab.value,
        artist: selectedArtist.value,
      },
    })
    .catch((error) => {
      console.error(error);
    });
  store.commit("SET_PREVIEW_VIS", false);
}
</script>

<style scoped>
.preview {
  width: 100%;
  height: 100%;
  background-color: var(--subtle-color);
  font-size: 15px;
}
h1 {
  font-size: 18px;
  color: var(--accent-color);
}
p,
h1 {
  position: relative;
  left: 6px;
}
button {
  margin-left: 8px;
  border: none;
  border-radius: 5px;
  width: 60px;
}
.imgbutton,
.image {
  margin-left: 22px;
  height: 26px;
  width: 26px;
  padding: 0;
  background-color: transparent;
}
.imgbutton:hover {
  opacity: 50%;
}
:disabled {
  opacity: 0.5;
  pointer-events: none;
}
</style>
