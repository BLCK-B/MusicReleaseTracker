<template>

  <v-app>
    <!-- sidebar -->
    <v-navigation-drawer app class="sidebar" permanent>
      <artist-list/>
    </v-navigation-drawer>
    
    
    <v-main class="main-content">
      <!-- top bar -->
      <v-app-bar app class="app-bar">
        <SourceMenu/>
      </v-app-bar>
      <!-- content -->
      <v-container class="maincontent">

        <AddArtistDialog/>
        
        <SourceTable class="sourcetable" @table-null="showDialogURLmethod" :TableData="tableData"/>

        <DialogsURL/>
        
      </v-container>
    </v-main>
  </v-app>

</template>

<script>
import ArtistList from './components/ArtistList.vue';
import SourceMenu from './components/SourceMenu.vue';
import SourceTable from './components/SourceTable.vue';
import DialogsURL from './components/DialogsURL.vue';
import AddArtistDialog from './components/AddArtistDialog.vue';
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
    },
    computed: {
      
    },
    methods: {
      
    },
    
};
</script>

<style scoped>
  .sidebar {
    width: 170px !important;
  }
  .app-bar {
    height: 45px;
  }
  .maincontent, .app-bar {
    left: 180px !important;
  }
  .app-bar {
    width: 560px !important;
  }
  .sourcetable {
    position: absolute;
    left: 180px;
    top: 45px;
  }

</style>

