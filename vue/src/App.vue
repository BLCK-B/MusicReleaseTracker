<template>

  <div class="app" v-if="!settingsOpen">
      <!-- sidebar -->
      <div class="list">
        <ArtistList v-if="!previewVis"/>
        <PreviewDialog v-if="previewVis" class="preview"/>
      </div>

      <div class="maincontent">
        
        <!-- top bar -->
        <div v-if="!previewVis" class="topbar">
          <SourceMenu/>
        </div>

        <!-- content -->
        <AddArtistDialog/>

        <div class ="sourcetable">
          <SourceTable/>
        </div>

        <div class="dialogsurl" v-if="!previewVis">
          <DialogsURL/>
        </div>

      </div>

      <div class="progressbar">
        <ProgressBar/>
      </div>
  
  </div>

  <div class="app" v-if="settingsOpen">
    <SettingsWindow/>
  </div>
  
</template>

<script>
import ArtistList from './components/ArtistList.vue';
import SourceMenu from './components/SourceMenu.vue';
import SourceTable from './components/SourceTable.vue';
import DialogsURL from './components/DialogsURL.vue';
import AddArtistDialog from './components/AddArtistDialog.vue';
import ProgressBar from './components/ProgressBar.vue';
import SettingsWindow from './components/SettingsWindow.vue';
import PreviewDialog from './components/PreviewDialog.vue';
import { mapState } from 'vuex';
import axios from 'axios';
axios.defaults.baseURL = 'http://localhost:57782';

export default {
  data() {
      return {
        appliedStyles: [],
        theme: "",
        accent: "",
      };
  },
  components: {
    ArtistList,
    SourceMenu,
    SourceTable,
    DialogsURL,
    AddArtistDialog,
    ProgressBar,
    SettingsWindow,
    PreviewDialog,
  },
  created() {
    this.loadTheme();
    this.detectTheme();
  },
  computed: {
    ...mapState([
    "settingsOpen",
    "primaryColor",
    "accentColor",
    "previewVis"
    ])
  },
  watch: {
    // load themes whenever store theme/accent change
    primaryColor(theme) {
      this.theme = theme;
      this.applyTheme(theme, this.accent);
    },
    accentColor(accent) {
      this.accent = accent;
      this.applyTheme(this.theme, accent);
    },
  },
  methods: {
    loadTheme() {
      // on start, load themes from config
      axios.get("/api/getThemeConfig")
        .then(response => {
          this.$store.commit('SET_PRIMARY_COLOR', response.data.theme);
          this.$store.commit('SET_ACCENT_COLOR', response.data.accent);
        })
        .catch(error => {
          console.error(error);
        });
    },
    detectTheme() {
      // detecting system theme on load
      axios.get('/api/settingsOpened')
        .then(response => {
          const prefersDarkMode = window.matchMedia('(prefers-color-scheme: dark)');
          if (response.data.autoTheme == true) {
            if (prefersDarkMode.matches)
              this.$store.commit('SET_PRIMARY_COLOR', "Black");
            else
              this.$store.commit('SET_PRIMARY_COLOR', "Light");
          }
        })
        .catch((error) => {
          console.error(error);
        });
    },
    applyTheme(theme, accent) {
      // remove previously applied css
      this.appliedStyles.forEach(style => {
      style.remove();
      });
      this.appliedStyles = [];
      let themePath;
      let linkElement;
      
      if (theme !== "") {
        themePath = `./primary${theme}.css`;
        linkElement = document.createElement('link');
        linkElement.rel = 'stylesheet';
        linkElement.href = themePath;
        document.head.appendChild(linkElement);
        this.appliedStyles.push(linkElement);
      }
      if (accent !== "") {
        themePath = `./secondary${accent}.css`;
        linkElement = document.createElement('link');
        linkElement.rel = 'stylesheet';
        linkElement.href = themePath;
        document.head.appendChild(linkElement);
        this.appliedStyles.push(linkElement);
      }
    },
  },
    
};
</script>

<style scoped>

* {
  scrollbar-color: var(--dull-color) transparent;
}
*::-webkit-scrollbar-thumb {
  background-color: var(--dull-color);
}
*::-webkit-scrollbar {
  width: 8px;
  background: transparent;
}

.app {
  font-family: 'arial', sans-serif;
  font-size: 14px;
  user-select: none;
  display: flex;
  position: fixed;
  top: 0;
  left: 0;
  padding-left: 5px;
  padding-top: 3px;
  width: 100%;
  height: 100%;
  background-color: var(--primary-color);
  color: var(--contrast-color);
  transition: 0.15s;
}
.list {
  width: 170px;
  min-width: 170px;
  padding-top: 5px;
  padding-left: 2px;
  top: -3px;
  left: -5px;
  position: relative;
  background-color: var(--subtle-color);
}
.maincontent {
  flex-grow: 1;
  height: 100vh;
}
.topbar {
  left: 5px;
  position: relative;
}
.dialogsurl {
  top: 25%;
  left: 35%;
  position: absolute;
}
.sourcetable {
  position: relative;
  top: 6px;
  height: 100%;
  overflow-y: scroll;
  margin-right: 4px;
}
.progressbar {
  position: absolute;
  bottom: 3px;
  left: 0;
  z-index: 5;
  width: 100%;
}

</style>

