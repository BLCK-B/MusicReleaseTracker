<template>
  <div class="wrapper">
    <div class="tabs">
      <div @mousedown="handleSourceClick('combview')" class="cvtab" id="combview">Combined view</div>
      <div @mousedown="handleSourceClick('beatport')" class="stab" id="beatport">BP</div>
      <div @mousedown="handleSourceClick('musicbrainz')" class="stab" id="musicbrainz">MB</div>
      <div @mousedown="handleSourceClick('junodownload')" class="stab" id="junodownload">JD</div>
    </div>
    
    <button @click="handleImgButtonClick('settings.png')" class="imgbutton">
      <img class="image" src="src/components/icons/settings.png" alt="Settings"/>
    </button>
    <button @click="handleImgButtonClick('refresh.png')" class="imgbutton">
      <img class="image" src="src/components/icons/refresh.png" alt="Refresh"/>
    </button>

  </div>
</template>

<script>
import axios from 'axios';
import { mapState, mapMutations } from 'vuex';

export default {
  computed: {
    ...mapState([
      'sourceTab',
      'tableData'
    ])
  },
  created() {
    this.handleSourceClick("combview");
  },
  watch: {
    sourceTab(tabValue) {
      if (tabValue) {
        this.handleSourceClick(tabValue);
      }
    },
  },
  methods: {
    handleSourceClick(source) {
      this.$store.commit('SET_ADD_VIS', false);
      axios.post('http://localhost:8080/api/sourceTabClick', { source })
        .then((response) => {
          this.$store.commit('SET_SOURCE_TAB', source); //set data to vuex store
          this.$store.commit('SET_TABLE_CONTENT', response.data);
        })
        .catch((error) => {
          console.error(error);
        });
    },
  },
};
</script>

<style scoped>
 
.wrapper {
  min-width: 500px;
  display: flex;
  align-items: center;
}
.tabs {
  display: flex;
  text-align: center;
  font-weight: bold;
}
.image {
  height: 30px;
}
.imgbutton {
  padding: 0;
  margin-left: 8px;
}
.cvtab {
  width: 180px;
  background-color: #CCCCFF;
  padding: 8px;
}
.stab {
  width: 60px;
  background-color: #CCCCFF;
  padding: 8px;
}
.tabs :hover {
  background-color: grey;
}

</style>