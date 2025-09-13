<template>
  <p><span class="title">Exclusion filters</span><br/>Select song types to be hidden.</p>

  <div class="filters-buttons">
    <div>
      <input type="checkbox" :checked="filterRemix" @change="onChange" data-setting="filterRemix"/>
      <label>Remix</label>
    </div>
    <div>
      <input type="checkbox" :checked="filterVIP" @change="onChange" data-setting="filterVIP"/>
      <label>VIP</label>
    </div>
    <div>
      <input type="checkbox" :checked="filterInstrumental" @change="onChange" data-setting="filterInstrumental"/>
      <label>Instrumental</label>
    </div>
    <div>
      <input type="checkbox" :checked="filterAcoustic" @change="onChange" data-setting="filterAcoustic"/>
      <label>Acoustic</label>
    </div>
    <div>
      <input type="checkbox" :checked="filterExtended" @change="onChange" data-setting="filterExtended"/>
      <label>Extended</label>
    </div>
    <div>
      <input type="checkbox" :checked="filterRemaster" @change="onChange" data-setting="filterRemaster"/>
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
