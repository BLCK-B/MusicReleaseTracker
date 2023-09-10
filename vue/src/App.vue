<template>
  <div class="app">
    <!-- sidebar -->
    <div class="sidebar">
      <artist-list />
    </div>

      <div class="maincontent">
        <!-- top bar -->
        <div class="topbar">
          <SourceMenu/>
        </div>
        <!-- content -->
        <AddArtistDialog/>
        <SourceTable class="sourcetable" @table-null="showDialogURLmethod" :TableData="tableData"/>
        <DialogsURL class="dialogsurl"/>
      </div>
  
  </div>
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
  
  .app {
    font-family: 'robotoregular', sans-serif;
    font-size: 14px;
    user-select: none;
  }
  .sidebar {
    width: 170px;
    top: 5px;
    position: absolute;
  }
  .maincontent {
    width: 75%;
    position: absolute;
    top: 0;
    left: 0;
  }
  .topbar {
    position: absolute;
    left: 180px;
  }
  .dialogsurl {
    position: absolute;
    top: 150px;
    left: 45%;
  }
  .sourcetable {
    position: absolute;
    left: 180px;
    top: 45px;
  }

</style>

