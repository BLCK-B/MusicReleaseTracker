<template>
 
 <div class="settings">

    <button @click="clickClose()" class="imgbutton">
      <img class="image" src="src/components/icons/cross.png" alt="close"/>
    </button>

    <section>
      <p>Exclusion filters<br>Select types of songs to be hidden in Combined view.</p>

      <div class="toggle-buttons">
        <div class="grid-item">
          <input type="checkbox" v-model="filters.Remix" @change="updateFilter('Remix', $event.target.checked)">
          <label>Remix</label>
        </div>
        <div class="grid-item">
          <input type="checkbox" v-model="filters.VIP" @change="updateFilter('VIP', $event.target.checked)">
          <label>VIP</label>
        </div>
        <div class="grid-item">
          <input type="checkbox" v-model="filters.Instrumental" @change="updateFilter('Instrumental', $event.target.checked)">
          <label>Instrumental</label>
        </div>
        <div class="grid-item">
          <input type="checkbox" v-model="filters.Acoustic" @change="updateFilter('Acoustic', $event.target.checked)">
          <label>Acoustic</label>
        </div>
        <div class="grid-item">
          <input type="checkbox" v-model="filters.Extended" @change="updateFilter('Extended', $event.target.checked)">
          <label>Extended</label>
        </div>
        <div class="grid-item">
          <input type="checkbox" v-model="filters.Remaster" @change="updateFilter('Remaster', $event.target.checked)">
          <label>Remaster</label>
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
.settings {
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
.imgbutton {
  position: absolute;
  right: 0;
  top: 0;
  padding: 0;
  margin: 10px;
  background-color: transparent;
  border: none;
}
.image {
  height: 33px;
}
input {
  margin-right: 5px;
}
section {
  position: relative;
  top: 20px;
  left: 20px;
  padding: 1px 15px 10px 15px;
  background-color: var(--duller-color);
  border-radius: 5px;
}

</style>