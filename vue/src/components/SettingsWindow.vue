<template>
 
 <div class="settings">

    <button @click="clickClose()" class="imgbutton">
      <img v-if="primaryColor !== 'Light'" class="image" src="src/components/icons/crossdark.png" alt="close"/>
      <img v-if="primaryColor === 'Light'" class="image" src="src/components/icons/crosslight.png" alt="close"/>
    </button>

    <section>
      <p>Exclusion filters<br>Select types of songs to be hidden in Combined view.</p>

      <div class="filters-buttons">
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

    <section class="appearance">
      <p>Appearance</p>
      <div class="appearancecont">
        <div class="theme-buttons">
          <input type="radio" v-model="theme" value="Black" @change="updateTheme()">
          <label>Black</label>
          <input type="radio" v-model="theme" value="Dark" @change="updateTheme()">
          <label>Dark</label>
          <input type="radio" v-model="theme" value="Light" @change="updateTheme()">
          <label>Light</label>
        </div>
        <div class="accent-buttons">
          <input type="radio" v-model="accent" value="Classic" @change="updateAccent()">
          <label>Classic</label>
          <input type="radio" v-model="accent" value="Rose" @change="updateAccent()">
          <label>Rose</label>
          <input type="radio" v-model="accent" value="Cactus" @change="updateAccent()">
          <label>Cactus</label>
          <input type="radio" value="None">
          <label>-</label>
          <input type="radio" value="None">
          <label>-</label>
          <input type="radio" value="None">
          <label>-</label>
        </div>
      </div>
    </section>

 </div>

</template>
  
<script>
import axios from 'axios';
import { mapState, mapMutations } from 'vuex';

export default {
  data() {
    return {
      theme: "",
      accent: "",
      filters: {
        Remix: false,
        VIP: false,
        Instrumental: false,
        Acoustic: false,
        Extended: false,
        Remaster: false
      },
      theme: {
        Black: true,
        Dark: false,
        Light: false,
      },
      accent: {
        Classic: true,
        Rose: false,
        Cactus: false,
      },
    }
  },
  computed: {
    ...mapState([
        'primaryColor',
    ]),
  },
  //on open, load settings from HOCON
  created() {
    axios.get('http://localhost:8080/api/settingsOpened')
      .then(response => {
        this.filters = response.data;
      })
      .catch((error) => {
        console.error(error);
      });
      //this will be replaced once settings implementation is done
      this.theme = "Black";
      this.accent = "Classic";
  },
  methods: {
    //close settings, rebuild combview
    clickClose() {
      axios.post('http://localhost:8080/api/fillCombview')
        .catch((error) => {
          console.error(error);
        });
      this.$store.commit('SET_SETTINGS_OPEN', false);
    },
    //write filter change to HOCON
    updateFilter(filter, value) {
      console.log(filter, value);
      axios.post(`http://localhost:8080/api/toggleFilter?filter=${filter}&value=${value}`)
        .catch(error => {
          console.error(error);
        });
    },
    //set theme in store
    updateTheme() {
      this.$store.commit('SET_PRIMARY_COLOR', this.theme);
    },
    //set accent in store
    updateAccent() {
      this.$store.commit('SET_ACCENT_COLOR', this.accent);
    },
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
.filters-buttons {
  margin-top: 10px;
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  grid-gap: 10px;
  accent-color: var(--contrast-color);
}
.appearancecont {
  display:flex;
  accent-color: var(--dull-color);
}
.theme-buttons {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  width: 30%;
  padding-right: 20px;
  line-height: 18px;
}
.accent-buttons {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
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
  margin-top: 20px;
  left: 20px;
  padding: 1px 15px 10px 15px;
  background-color: var(--duller-color);
  border-radius: 5px;
}
.appearance {
  border-right: 5px solid var(--accent-color);
}
.imgbutton:hover {
  opacity: 60%;
}

</style>