<template>

  <v-app>
    <!-- Sidebar -->
    <v-navigation-drawer app class="sidebar" permanent>

      <v-row justify="end">
        <v-col cols="auto">
          <v-btn>add</v-btn>
          <v-btn>remove</v-btn>
        </v-col>
      </v-row>
      
      <artist-list @update-table="handleUpdateTable" :data="artistsArrayList"/>
    </v-navigation-drawer>
    
    <!-- Main content -->
    <v-main class="main-content">
      <!-- Top App Bar -->
      <v-app-bar app class="app-bar">
        <SourceMenu @update-table="handleUpdateTable" :data="sourceTab"/>

        <v-btn class="imgbutton">
          <img class="image" src="settings.png"/>
        </v-btn>
        <v-btn class="imgbutton">
          <img class="image" src="refresh.png"/>
        </v-btn>

      </v-app-bar>
      <!-- Page content -->
      <v-container fluid>
        
        <SourceTable @table-null="showDialogURLmethod" :TableData="TableData"/>

        <DialogsURL v-if="showDialogURL"/>
        
      </v-container>
    </v-main>
  </v-app>

</template>

<script>
import ArtistList from './components/ArtistList.vue';
import SourceMenu from './components/SourceMenu.vue';
import SourceTable from './components/SourceTable.vue';
import DialogsURL from './components/DialogsURL.vue';

export default {
    data() {
        return {
            artistsArrayList: [],
            sourceTab: [],
            TableData: [],
            message: '',
            showDialogURL: false,
        };
    },
    components: {
      ArtistList,
      SourceMenu,
      SourceTable,
      DialogsURL,
    },
    methods: {
      handleUpdateTable(data) {
        this.TableData = data;
        this.showDialogURL = false;
      },
      showDialogURLmethod() {
        this.showDialogURL = true;
      },
    },
};
</script>

<style scoped>
  .sidebar {
    width: 10rem !important;
  }
  .app-bar {
    height: 45px;
  }
  .image {
    height: 35px;
  }
  .imgbutton {
    margin-bottom: 15px;
  }
</style>

