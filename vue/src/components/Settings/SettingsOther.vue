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
  </div>
</template>

<script setup lang="ts">
defineProps({
  isoDates: Boolean,
  loadThumbnails: Boolean,
});

const emit = defineEmits(['set-setting']);

const onChange = (event: Event) => {
  const target = event.target as HTMLInputElement;
  emit('set-setting', target.dataset.setting, target.checked);
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
