<template>
 
 <div class="settings">

    <button @click="clickClose()" class="imgbutton">
      <img class="image" src="src/components/icons/cross.png" alt="close"/>
    </button>

    <section class="typefilters">
      <p>Exclusion filters<br>Select types of songs to be hidden in Combined view.</p>

      <div class="toggle-buttons">
        <div class="grid-item">
          <label>Remix</label>
          <input type="checkbox" v-model="filters.Remix" @change="updateFilter('Remix', $event.target.checked)">
        </div>
        <div class="grid-item">
          <label>VIP</label>
          <input type="checkbox" v-model="filters.VIP" @change="updateFilter('VIP', $event.target.checked)">
        </div>
        <div class="grid-item">
          <label>Instrumental</label>
          <input type="checkbox" v-model="filters.Instrumental" @change="updateFilter('Instrumental', $event.target.checked)">
        </div>
        <div class="grid-item">
          <label>Acoustic</label>
          <input type="checkbox" v-model="filters.Acoustic" @change="updateFilter('Acoustic', $event.target.checked)">
        </div>
        <div class="grid-item">
          <label>Extended</label>
          <input type="checkbox" v-model="filters.Extended" @change="updateFilter('Extended', $event.target.checked)">
        </div>
        <div class="grid-item">
          <label>Remaster</label>
          <input type="checkbox" v-model="filters.Remaster" @change="updateFilter('Remaster', $event.target.checked)">
        </div>
      </div>

    </section>

 </div>

</template>
  
<script>
import axios from 'axios';
import { mapState, mapActions } from 'vuex';

export default {
  data() {
    return {
      filters: {
        Remix: false,
        VIP: false,
        Instrumental: false,
        Acoustic: false,
        Extended: false,
        Remaster: false
      }
    }
  },
  computed: {
    
  },
  created() {
    axios.get('http://localhost:8080/api/settingsOpened')
      .then(response => {
        this.filters = response.data;
      })
      .catch((error) => {
        console.error(error);
      });
  },
  methods: {
    updateFilter(filter, value) {
      console.log(filter, value);
      axios.post(`http://localhost:8080/api/toggleFilter?filter=${filter}&value=${value}`)
        .catch(error => {
          console.error(error);
        });
    },
    clickClose() {
      this.$store.commit('SET_SETTINGS_OPEN', false);
      
    }
  },
};
</script>

<style scoped>
* {
  font-family: 'arial', sans-serif;
  font-size: 14px;
  user-select: none;
  background-color: var(--primary-color);
  color: var(--contrast-color);
}
.toggle-buttons {
  margin-top: 10px;
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  grid-gap: 10px;
}

.radio-buttons {
  display: flex;
  flex-direction: column;
}
.radio-option {
  display: flex;
  align-items: center;
}
.imgbutton {
  position: absolute;
  right: 0;
  top: 0;
  padding: 0;
}
.image {
  height: 25px;
}

</style>