<template>
 <div class="settings">

    <button @click="clickClose()" class="imgbutton">
      <img v-if="primaryColor !== 'Light'" class="image" src="./icons/crossdark.png" alt="X"/>
      <img v-if="primaryColor === 'Light'" class="image" src="./icons/crosslight.png" alt="X"/>
    </button>

    <section class="filterscont">
      <p><span class="title">Exclusion filters</span><br>Select types of songs to be hidden in Combined view.</p>

      <div class="filters-buttons">
        <div>
          <input type="checkbox" v-model="filters.Remix" @change="setSetting('filters.Remix', $event.target.checked)">
          <label>Remix</label>
        </div>
        <div>
          <input type="checkbox" v-model="filters.VIP" @change="setSetting('filters.VIP', $event.target.checked)">
          <label>VIP</label>
        </div>
        <div>
          <input type="checkbox" v-model="filters.Instrumental" @change="setSetting('filters.Instrumental', $event.target.checked)">
          <label>Instrumental</label>
        </div>
        <div>
          <input type="checkbox" v-model="filters.Acoustic" @change="setSetting('filters.Acoustic', $event.target.checked)">
          <label>Acoustic</label>
        </div>
        <div>
          <input type="checkbox" v-model="filters.Extended" @change="setSetting('filters.Extended', $event.target.checked)">
          <label>Extended</label>
        </div>
        <div>
          <input type="checkbox" v-model="filters.Remaster" @change="setSetting('filters.Remaster', $event.target.checked)">
          <label>Remaster</label>
        </div>
      </div>
    </section>

    <section class="appearance">
      <p class="title">Appearance</p>
      <div class="appearancecont">

        <div class="theme-buttons">
          <input type="radio" v-model="theme" value="Black" @change="setSetting('theme', 'Black')">
          <label>Black</label>
          <input type="radio" v-model="theme" value="Dark" @change="setSetting('theme', 'Dark')">
          <label>Dark</label>
          <input type="radio" v-model="theme" value="Light" @change="setSetting('theme', 'Light')">
          <label>Light</label>
          
          <div class="colorindicator"></div>
        </div>

        <div class="accent-buttons">
          <input type="radio" v-model="accent" value="Classic" @change="setSetting('accent', 'Classic')">
          <label>Classic</label>
          <input type="radio" v-model="accent" value="Cactus" @change="setSetting('accent', 'Cactus')">
          <label>Cactus</label>
          <input type="radio" v-model="accent" value="Rose" @change="setSetting('accent', 'Rose')">
          <label>Rose</label>
          <input type="radio" v-model="accent" value="Warm" @change="setSetting('accent', 'Warm')">
          <label>Warm</label>
          <input type="radio" v-model="accent" value="Cloud" @change="setSetting('accent', 'Cloud')">
          <label>Cloud</label>
          <input type="radio" v-model="accent" value="Surge" @change="setSetting('accent', 'Surge')">
          <label>Surge</label>
        </div>

      </div>
    </section>

    <section class="other">
      <p class="title">Other</p>
      <div class="flex-items">
        <div class="flex-padding">
          <input type="checkbox" v-model="longTimeout" @change="setSetting('longTimeout', $event.target.checked)">
              <label>Longer timeout for unreliable internet</label>
        </div>
        <div class="flex-padding">
          <input type="checkbox" v-model="isoDates" @change="setSetting('isoDates', $event.target.checked)">
              <label>Dates in yyyy-MM-dd (ISO 8601)</label>
        </div>
      </div>
    </section>

    <section class="danger">
      <p class="title">Danger zone</p>
      <div class="dangercont">

        <button v-if="settingsProtection" @click="resetSettings()">Reset settings</button>
        <button v-if="!settingsProtection" @click="resetSettings()" @mouseleave="resetProtection()">confirm</button>
       
        <button v-if="dbProtection" @click="resetDB()">Reset database</button>
        <button v-if="!dbProtection" @click="resetDB()" @mouseleave="resetProtection()">confirm</button>
         
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
      settingsProtection: true,
      dbProtection: true,
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
      longTimeout: false,
      isoDates: false,
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
        this.longTimeout = response.data.longTimeout;
        this.isoDates = response.data.isoDates;
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
    //write single setting in config
    setSetting(name, value) {
      switch(name) {
        case ("theme"): this.$store.commit('SET_PRIMARY_COLOR', this.theme);
        case ("accent"): this.$store.commit('SET_ACCENT_COLOR', this.accent);
        case ("isoDates"): this.$store.commit("SET_ISODATES", this.isoDates);
      }
      axios.post(`http://localhost:8080/api/setSetting`, { name: name, value: value })
      .catch(error => {
        console.error(error);
      });
    },
    //default the settings
    resetSettings() {
      if (this.settingsProtection == true) {
        this.settingsProtection = false;
      }
      else {
        axios.post('http://localhost:8080/api/resetSettings')
        .then(() => {
          this.clickClose();
        })
        .catch(error => {
            console.error(error);
        });
      }
    },
    //default database
    resetDB() {
      if (this.dbProtection == true) {
        this.dbProtection = false;
      }
      else {
        axios.post('http://localhost:8080/api/resetDB')
        .then(() => {
          this.clickClose();
        })
        .catch(error => {
            console.error(error);
        });
      }
    },
    resetProtection() {
      this.settingsProtection = true;
      this.dbProtection = true;
    }
  },
};
</script>

<style scoped>
* {
  transition: 0.15s;
}
.title {
  font-weight: bold;
}
.settings {
  font-family: 'arial', sans-serif;
  font-size: 14px;
  user-select: none;
  background-color: var(--primary-color);
  color: var(--contrast-color);
  overflow-y: scroll;
  display: grid;
  align-content: start;
  width: 100%;
  justify-content: center;
}
@media screen and (min-width: 950px) {
  .settings {
    display: grid;
    grid-template-columns: repeat(2, 0fr);
  }
  section {
    margin-right: 90px;
  }
}

.filters-buttons {
  margin-top: 10px;
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  grid-gap: 10px;
  accent-color: var(--contrast-color);
  max-height: 80px;
}
.flex-items {
  display: flex;
  flex-direction: column;
}
.flex-padding {
  padding: 5px;
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
.imgbutton:hover {
  opacity: 60%;
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
  left: 40px;
  padding: 1px 15px 10px 15px;
  background-color: var(--duller-color);
  border-radius: 5px;
  transition: 0.15s;
  width: 345px;
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
.other {
  accent-color: var(--contrast-color);
}
.dangercont {
  display: flex;
  justify-content: space-evenly;
}
.danger button {
  border-radius: 5px;
  background-color: transparent;
  color: var(--contrast-color);
  border: 2px solid red;
  padding: 4px;
  width: 120px;
}
.danger button:hover {
  background-color: red;
}


</style>