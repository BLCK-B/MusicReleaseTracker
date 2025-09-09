<template>
  <p><span class="title">Exclusion filters</span><br />Select song types to be hidden.</p>

  <div class="filters-buttons">
    <div>
      <input type="checkbox" :checked="filterRemix" @change="$emit('set-setting', 'filterRemix', $event.target.checked)" />
      <label>Remix</label>
    </div>
    <div>
      <input type="checkbox" :checked="filterVIP" @change="$emit('set-setting', 'filterVIP', $event.target.checked)" />
      <label>VIP</label>
    </div>
    <div>
      <input
        type="checkbox"
        :checked="filterInstrumental"
        @change="$emit('set-setting', 'filterInstrumental', $event.target.checked)" />
      <label>Instrumental</label>
    </div>
    <div>
      <input type="checkbox" :checked="filterAcoustic" @change="$emit('set-setting', 'filterAcoustic', $event.target.checked)" />
      <label>Acoustic</label>
    </div>
    <div>
      <input type="checkbox" :checked="filterExtended" @change="$emit('set-setting', 'filterExtended', $event.target.checked)" />
      <label>Extended</label>
    </div>
    <div>
      <input type="checkbox" :checked="filterRemaster" @change="$emit('set-setting', 'filterRemaster', $event.target.checked)" />
      <label>Remaster</label>
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps({
  filterRemix: Boolean,
  filterVIP: Boolean,
  filterInstrumental: Boolean,
  filterAcoustic: Boolean,
  filterExtended: Boolean,
  filterRemaster: Boolean,
});
</script>

<style scoped>
.title {
  font-weight: bold;
}

.filters-buttons {
  margin-top: 10px;
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  grid-gap: 10px;
  max-height: 80px;
}
input {
  margin-right: 5px;
}
</style>
