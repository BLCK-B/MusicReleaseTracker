<template>

  <div class="app" v-if="!settingsOpen">
      <!-- sidebar -->
      <div class="list">
        <ArtistList v-if="!previewvis"/>
        <PreviewDialog v-if="previewvis" class="preview"/>
      </div>

        <div class="maincontent">
          <!-- top bar -->
          <div v-if="!previewvis" class="topbar">
            <SourceMenu/>
          </div>
          <!-- content -->
          <AddArtistDialog/>
          <SourceTable class="sourcetable"/>
          <DialogsURL v-if="!previewvis" class="dialogsurl"/>
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
import { mapState, mapMutations } from 'vuex';
import axios from 'axios';


export default {
  data() {
      return {
        appliedStyles: [],
        theme: "Black",
        accent: "Classic",
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
    PreviewDialog
},
  created() {
    this.applyTheme(this.theme, this.accent);
  },
  computed: {
    ...mapState([
    "settingsOpen",
    "primaryColor",
    "accentColor",
    "previewvis",
    ])
  },
  watch: {
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
    applyTheme(theme, accent) {
      //remove previously applied css
      this.appliedStyles.forEach(style => {
      style.remove();
      });
      this.appliedStyles = [];

      let themePath = `/src/assets/primary${theme}.css`;
      let linkElement = document.createElement('link');
      linkElement.rel = 'stylesheet';
      linkElement.href = themePath;
      document.head.appendChild(linkElement);
      this.appliedStyles.push(linkElement);

      themePath = `/src/assets/secondary${accent}.css`;
      linkElement = document.createElement('link');
      linkElement.rel = 'stylesheet';
      linkElement.href = themePath;
      document.head.appendChild(linkElement);
      this.appliedStyles.push(linkElement);
    },
  },
    
};
</script>

<style scoped>
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
  }
  .progressbar {
    position: absolute;
    bottom: 3px;
    left: 0;
    z-index: 5;
    width: 100%;
  }

</style>

