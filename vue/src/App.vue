<template>
  <div class="app" v-if="!settingsOpen">
      <!-- sidebar -->
      <div class="list">
        <ArtistList/>
      </div>

        <div class="maincontent">
          <!-- top bar -->
          <div class="topbar">
            <SourceMenu/>
          </div>
          <!-- content -->
          <AddArtistDialog/>
          <SourceTable class="sourcetable"/>
          <DialogsURL class="dialogsurl"/>
        </div>

        <div class="progressbar">
        <ProgressBar/>
        </div>
  
  </div>

  <SettingsWindow v-if="settingsOpen"/>

</template>


<script>
import ArtistList from './components/ArtistList.vue';
import SourceMenu from './components/SourceMenu.vue';
import SourceTable from './components/SourceTable.vue';
import DialogsURL from './components/DialogsURL.vue';
import AddArtistDialog from './components/AddArtistDialog.vue';
import ProgressBar from './components/ProgressBar.vue';
import SettingsWindow from './components/SettingsWindow.vue';
import { mapState, mapActions } from 'vuex';


export default {
  data() {
      return {
          
      };
  },
  components: {
    ArtistList,
    SourceMenu,
    SourceTable,
    DialogsURL,
    AddArtistDialog,
    ProgressBar,
    SettingsWindow
  },
  created() {
    const themePath1 = '/src/assets/primaryLight.css';
    const linkElement1 = document.createElement('link');
    linkElement1.rel = 'stylesheet';
    linkElement1.href = themePath1;
    document.head.appendChild(linkElement1);

    const themePath2 = '/src/assets/secondaryCactus.css';
    const linkElement2 = document.createElement('link');
    linkElement2.rel = 'stylesheet';
    linkElement2.href = themePath2;
    document.head.appendChild(linkElement2);
  },
  computed: {
    ...mapState([
    'settingsOpen',
    ])
  },
  methods: {
    
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
    background-color: #f3f3f3;
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
    top: 180px;
    left: 260px;
    position: absolute;
  }
  .sourcetable {
    position: relative;
    top: 10px;
  }
  .progressbar {
    position: absolute;
    bottom: 36px;
    left: 0;
    z-index: 5;
    width: 100%;
  }

</style>

