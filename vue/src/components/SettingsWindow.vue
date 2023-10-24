<template>
 <div class="settings">

    <button @click="clickClose()" class="imgbutton">
      <img v-if="primaryColor !== 'Light'" class="image" src="./icons/crossdark.png" alt="X"/>
      <img v-if="primaryColor === 'Light'" class="image" src="./icons/crosslight.png" alt="X"/>
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
          <input type="radio" v-model="theme" value="Black" @change="setTheme('Black')">
          <label>Black</label>
          <input type="radio" v-model="theme" value="Dark" @change="setTheme('Dark')">
          <label>Dark</label>
          <input type="radio" v-model="theme" value="Light" @change="setTheme('Light')">
          <label>Light</label>
          
          <div class="colorindicator"></div>
        </div>

        <div class="accent-buttons">
          <input type="radio" v-model="accent" value="Classic" @change="setTheme('Classic')">
          <label>Classic</label>
          <input type="radio" v-model="accent" value="Cactus" @change="setTheme('Cactus')">
          <label>Cactus</label>
          <input type="radio" v-model="accent" value="Rose" @change="setTheme('Rose')">
          <label>Rose</label>
          <input type="radio" v-model="accent" value="Warm" @change="setTheme('Warm')">
          <label>Warm</label>
          <input type="radio" v-model="accent" value="Cloud" @change="setTheme('Cloud')">
          <label>Cloud</label>
          <input type="radio" v-model="accent" value="Surge" @change="setTheme('Surge')">
          <label>Surge</label>
        </div>

      </div>
    </section>

    <section class="self">
      <a href="https://blck-b.github.io" target="_blank">
        <img class="blckimg" src="./icons/blcktext.png" alt="logo"/>
      </a>
      <a href="https://github.com/BLCK-B/MusicReleaseTracker" target="_blank">
        <img class="mrtimg" src="./icons/MRTlogo.png" alt="logo"/>
      </a>
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
        Black: false,
        Dark: false,
        Light: false,
      },
      accent: {
        Classic: false,
        Rose: false,
        Cactus: false,
      },
    }
  },
  computed: {
    ...mapState([
        'primaryColor',
        'accentColor',
    ]),
  },
  //on open, load setting states from HOCON
  created() {
    axios.get('http://localhost:8080/api/settingsOpened')
      .then(response => {
        this.filters = response.data;
      })
      .catch((error) => {
        console.error(error);
      });
      this.theme = this.primaryColor;
      this.accent = this.accentColor;
  },
  methods: {
    //close settings, trigger rebuild combview in app
    clickClose() {
      this.$store.commit('SET_SETTINGS_OPEN', false);
    },
    //write filter change to HOCON
    updateFilter(filter, value) {
      axios.post(`http://localhost:8080/api/toggleFilter?filter=${filter}&value=${value}`)
        .catch(error => {
          console.error(error);
        });
    },
    //write theme/accent change to HOCON
    setTheme(theme) {
      if (theme === "Black" || theme === "Dark" || theme === "Light")
        this.$store.commit('SET_PRIMARY_COLOR', this.theme);
      else
        this.$store.commit('SET_ACCENT_COLOR', this.accent);
      
      axios.post('http://localhost:8080/api/setTheme', { theme })
      .catch(error => {
        console.error(error);
      });
    },
  },
};
</script>

<style scoped>
* {
  transition: 0.15s;
}
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
  transition: 0s;
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
  transition: 0.15s;
}
.imgbutton:hover {
  opacity: 60%;
}
.self {
  text-align: center;
  padding: 8px;
}
.blckimg {
  height: 62px;
  background-color: black;
  border-radius: 5px;
  padding: 12px;
  margin-right: 20px;
}
.mrtimg {
  height: 86px;
  border-radius: 10px;
  margin-right: 10px;
}

.colorindicator {
  position: absolute;
  right: 0;
  top: 0;
  height: 100%;
  width: 8px;
  background-color: var(--accent-color);
  border-top-right-radius: 5px;
  border-bottom-right-radius: 5px;
}


</style>