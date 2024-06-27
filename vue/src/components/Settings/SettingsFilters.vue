<template>
    
    <p><span class="title">Exclusion filters</span><br>Select types of songs to be hidden in Combined view.</p>
 
    <div class="filters-buttons">
        <div>
            <input type="checkbox" :checked="filters.Remix" @change="setSetting('filters.Remix', $event.target.checked)">
            <label>Remix</label>
        </div>
        <div>
            <input type="checkbox" :checked="filters.VIP" @change="setSetting('filters.VIP', $event.target.checked)">
            <label>VIP</label>
        </div>
        <div>
            <input type="checkbox" :checked="filters.Instrumental" @change="setSetting('filters.Instrumental', $event.target.checked)">
            <label>Instrumental</label>
        </div>
        <div>
            <input type="checkbox" :checked="filters.Acoustic" @change="setSetting('filters.Acoustic', $event.target.checked)">
            <label>Acoustic</label>
        </div>
        <div>
            <input type="checkbox" :checked="filters.Extended" @change="setSetting('filters.Extended', $event.target.checked)">
            <label>Extended</label>
        </div>
        <div>
            <input type="checkbox" :checked="filters.Remaster" @change="setSetting('filters.Remaster', $event.target.checked)">
            <label>Remaster</label>
        </div>
    </div>

</template>

<script>
import axios from 'axios';
import { mapState, mapMutations } from 'vuex';

export default {
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