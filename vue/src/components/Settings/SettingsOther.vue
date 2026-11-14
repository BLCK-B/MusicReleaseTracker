<template>
  <p class="title">Other</p>
  <div class="flex-items">
    <div class="flex-padding">
      <input type="checkbox" :checked="isoDates" @change="onChange" data-setting="isoDates"/>
      <label>Dates as yyyy-MM-dd</label>
    </div>
    <div class="flex-padding">
      <input type="checkbox" :checked="loadThumbnails" @change="onChange" data-setting="loadThumbnails" />
      <label>Download thumbnails</label>
    </div>
    <div class="flex-padding">
      <input type="checkbox" :checked="isNotifyEnabled" @change="notifyUpdateToggle" data-setting="loadThumbnails" />
      <label>MRT update notification</label>
    </div>
  </div>
</template>

<script setup lang="ts">
import {computed} from "vue";

defineProps({
  isoDates: Boolean,
  loadThumbnails: Boolean,
});

const emit = defineEmits(['set-setting']);

const onChange = (event: Event) => {
  const target = event.target as HTMLInputElement;
  emit('set-setting', target.dataset.setting, target.checked);
};

// by default, notify is enabled
if (localStorage.getItem("notifyWhenUpdate") === null) {
  localStorage.setItem("notifyWhenUpdate", "true");
}

const isNotifyEnabled = computed(() => localStorage.getItem("notifyWhenUpdate") === "true");

const notifyUpdateToggle = () => {
  if (localStorage.getItem("notifyWhenUpdate") === "true") {
    localStorage.setItem("notifyWhenUpdate", "false");
  } else {
    localStorage.setItem("notifyWhenUpdate", "true");
  }
};
</script>

<style scoped>
.title {
  font-weight: bold;
}
.flex-padding {
  padding: 5px;
}
input {
  margin-right: 5px;
}
</style>
