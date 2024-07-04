<template>

  <div class="app">
      <!-- sidebar -->
      <div class="list">
        <ArtistList v-if="!previewVis"/>
        <PreviewDialog v-if="previewVis" class="preview"/>
      </div>

      <AddArtistDialog/>

      <div class="maincontent">
        
        <!-- top bar -->
        <div v-if="!previewVis" class="topbar">
          <SourceMenu/>
        </div>

        <!-- content -->

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
  
</template>

<script>
import ArtistList from '@/components/Artists/ArtistList.vue';
import SourceMenu from '@/components/Bar/SourceMenu.vue';
import SourceTable from '@/components/Content/SourceTable.vue';
import DialogsURL from '@/components/Content/DialogsURL.vue';
import AddArtistDialog from '@/components/Artists/AddArtistDialog.vue';
import ProgressBar from '@/components/Content/ProgressBar.vue';
import PreviewDialog from '@/components/Artists/PreviewDialog.vue';
import { mapState } from 'vuex';

export default {
  components: {
    ArtistList,
    SourceMenu,
    SourceTable,
    DialogsURL,
    AddArtistDialog,
    ProgressBar,
    PreviewDialog,
  },
  computed: {
    ...mapState([
    "settingsOpen",
    "primaryColor",
    "accentColor",
    "previewVis"
    ])
  },
  methods: {
   
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