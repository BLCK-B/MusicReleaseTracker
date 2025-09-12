<template>
  <div v-if="addVisibility" class="barrier">
    <div class="pill">
      <input v-model="input" placeholder="Artist's name" />

      <div class="buttons">
        <button @click="$emit('close-add-new')" class="imgbutton" data-testid="close-button">
          <img v-if="primaryColor !== 'light'" class="image" src="../icons/crossdark.png" alt="X" />
          <img v-if="primaryColor === 'light'" class="image" src="../icons/crosslight.png" alt="X" />
        </button>
        <button @click="clickAdd" class="imgbutton" :disabled="!isValid" data-testid="add-button">
          <img v-if="primaryColor !== 'light'" class="image" src="../icons/confirmdark.png" alt="OK" />
          <img v-if="primaryColor === 'light'" class="image" src="../icons/confirmlight.png" alt="OK" />
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from "vue";
import { useMainStore } from "@/store/mainStore.ts";
import axios from "axios";

defineProps({
  addVisibility: Boolean,
});
const emit = defineEmits(["close-add-new"]);

const input = ref("");
const store = useMainStore();
// empty input forbidden
const rules: Array<(value: string) => boolean> = [
  (value) => !!value.trim(),
  (value) => (value || "").length <= 25
];
const isValid = computed(() => rules.every((rule) => rule(input.value)));

const primaryColor = computed(() => store.primaryColor);

function clickAdd() {
  try {
    const artistId = String(input.value).trim();
    axios.post(`/api/artist/${artistId}`);
    input.value = "";
    store.setSelectedArtist(artistId);
    emit("close-add-new");
    store.setLoadRequest(true);
  } catch (error) {
    console.error(error);
  }
}
</script>

<style scoped>
.barrier {
  position: absolute;
  gap: 4px;
  top: 3px;
  left: 6px;
  width: 290px;
  z-index: 4;
  height: 38px;
}
.pill {
  border: 2px solid var(--accent-color);
  width: 220px;
}
button {
  border: none;
}
input {
  position: absolute;
  height: 26px;
  background-color: var(--primary-color);
  color: var(--contrast-color);
  width: 152px;
  padding-left: 6px;
  border: none;
  display: inline-block;
}
input:focus {
  outline: none;
}
.buttons {
  height: 28px;
  background-color: var(--duller-color);
  align-content: center;
}
.imgbutton,
.image {
  height: 23px;
  width: 23px;
  padding: 0;
  float: right;
  margin-left: 5px;
  margin-right: 2px;
  background-color: transparent;
}
.imgbutton {
  position: relative;
}
.imgbutton:hover {
  opacity: 50%;
}
:disabled {
  opacity: 0.3;
  pointer-events: none;
}
</style>
