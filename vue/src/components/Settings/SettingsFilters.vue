<template>
  <p><span class="title">Exclusion filters</span><br/>Select song types you want to hide.</p>

  <div class="filters-buttons">
    <div>
      <input type="checkbox" :checked="filterAcoustic" @change="onChange" data-setting="filterAcoustic"/>
      <label>Acoustic</label>
    </div>
    <div>
      <input type="checkbox" :checked="filterExtended" @change="onChange" data-setting="filterExtended"/>
      <label>Extended</label>
    </div>
    <div>
      <input type="checkbox" :checked="filterInstrumental" @change="onChange" data-setting="filterInstrumental"/>
      <label>Instrumental</label>
    </div>
    <div>
      <input type="checkbox" :checked="filterPreview" @change="onChange" data-setting="filterPreview"/>
      <label>Preview</label>
    </div>
    <div>
      <input type="checkbox" :checked="filterRemaster" @change="onChange" data-setting="filterRemaster"/>
      <label>Remaster</label>
    </div>
    <div>
      <input type="checkbox" :checked="filterRemix" @change="onChange" data-setting="filterRemix"/>
      <label>Remix</label>
    </div>
    <div>
      <input type="checkbox" :checked="filterRework" @change="onChange" data-setting="filterRework"/>
      <label>Rework</label>
    </div>
    <div>
      <input type="checkbox" :checked="filterVIP" @change="onChange" data-setting="filterVIP"/>
      <label>VIP</label>
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
  filterRework: Boolean,
  filterPreview: Boolean
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
}

input {
  margin-right: 5px;
}
</style>
