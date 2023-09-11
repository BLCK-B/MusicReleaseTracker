<template>
  <div class="app">
    <!-- sidebar -->
    <div class="list">
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
    font-family: 'arial', sans-serif;
    font-size: 14px;
    user-select: none;
    display: flex;
  }
  .list {
    width: 170px;
    min-width: 170px;
    top: 3px;
    position: relative;
  }
  .maincontent {
    flex-grow: 1;
  }
  .topbar {
    left: 5px;
    position: relative;
  }
  .dialogsurl {
    position: relative;
    top: 110px;
    left: 55px;
  }
  .sourcetable {
    position: relative;
    top: 10px;
  }

</style>

