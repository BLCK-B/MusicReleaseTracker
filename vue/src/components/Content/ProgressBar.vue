<template>
  <div v-if="progress !== 0" class="progress-text">{{ progressText }}</div>
  <div :class="{ loadingThumbnails: progress === 100 }">
    <div class="progress-bar" :style="{ width: `${progress}%` }"></div>
  </div>
</template>

<script setup lang="ts">
import { computed } from "vue";
import { useMainStore } from "@/store/mainStore.ts";

const store = useMainStore();

const progress = computed(() => store.progress);

const progressText = computed(() => {
  return progress.value === 100 ? "Loading thumbnails..." : "Fetching data...";
});
</script>

<style scoped>
* {
  transition: 0.2s;
}

.progress-bar {
  height: 6px;
  line-height: 5px;
  text-align: center;
  background-color: var(--accent-color);
  transition: width 0.3s ease;
  border-radius: 0 3px 3px 0;
}

.loadingThumbnails {
  animation: hueRotate 4s linear infinite;
}

@keyframes hueRotate {
  0% {
    filter: hue-rotate(0deg);
  }
  50% {
    filter: hue-rotate(120deg);
  }
  100% {
    filter: hue-rotate(0deg);
  }
}

.progress-text {
  position: fixed;
  left: 50%;
  bottom: 10px;
  width: 155px;
  padding-top: 4px;
  padding-bottom: 4px;
  background-color: var(--duller-color);
  border: 2px solid var(--dull-color);
  border-radius: 4px;
  transform: translateX(-50%);
  transition: width 0.3s ease;
  text-align: center;
}
</style>
