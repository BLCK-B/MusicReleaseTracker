<template>
  <p class="title">Other</p>
  <div class="flex-items">
    <div class="flex-padding">
      <input type="checkbox" :checked="isoDates" @change="$emit('set-setting', 'isoDates', $event.target.checked)" />
      <label>Dates as yyyy-MM-dd</label>
    </div>
    <div class="flex-padding">
      <input type="checkbox" :checked="loadThumbnails" @change="$emit('set-setting', 'loadThumbnails', $event.target.checked)" />
      <label>Download thumbnails (feature preview)</label>
    </div>
  </div>
</template>

<script setup>
defineProps({
  isoDates: Boolean,
  loadThumbnails: Boolean,
});
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
