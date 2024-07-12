<template>
  <p><span class="title">Exclusion filters</span><br />Select song types to be hidden.</p>

  <div class="filters-buttons">
    <div>
      <input type="checkbox" :checked="filters.Remix" @change="$emit('set-setting', 'filters.Remix', $event.target.checked)" />
      <label>Remix</label>
    </div>
    <div>
      <input type="checkbox" :checked="filters.VIP" @change="$emit('set-setting', 'filters.VIP', $event.target.checked)" />
      <label>VIP</label>
    </div>
    <div>
      <input
        type="checkbox"
        :checked="filters.Instrumental"
        @change="$emit('set-setting', 'filters.Instrumental', $event.target.checked)" />
      <label>Instrumental</label>
    </div>
    <div>
      <input type="checkbox" :checked="filters.Acoustic" @change="$emit('set-setting', 'filters.Acoustic', $event.target.checked)" />
      <label>Acoustic</label>
    </div>
    <div>
      <input type="checkbox" :checked="filters.Extended" @change="$emit('set-setting', 'filters.Extended', $event.target.checked)" />
      <label>Extended</label>
    </div>
    <div>
      <input type="checkbox" :checked="filters.Remaster" @change="$emit('set-setting', 'filters.Remaster', $event.target.checked)" />
      <label>Remaster</label>
    </div>
  </div>
</template>

<script>
export default {
  emits: ["set-setting"],
  props: {
    filters: Object,
  },
};
</script>

<style scoped>
* {
  transition: 0.1s;
}
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
.disabled {
  opacity: 0.3;
}
</style>
