<template>
    
      <v-tabs>
        <v-tab @click="handleSourceClick('combview')" style="height:45px;" class="cvtab">Combined view</v-tab>
        <v-tab @click="handleSourceClick('beatport')" style="height:45px;" class="btab">BP</v-tab>
        <v-tab @click="handleSourceClick('musicbrainz')" style="height:45px;" class="btab">MB</v-tab>
        <v-tab @click="handleSourceClick('junodownload')" style="height:45px;" class="btab">JD</v-tab>
      </v-tabs>
      <v-btn class="imgbutton">
        <img class="image" src="settings.png"/>
      </v-btn>
      <v-btn class="imgbutton">
        <img class="image" src="refresh.png"/>
      </v-btn>

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
    this.handleSourceClick();
  },
  watch: {
    sourceTab(tabValue) {
      if (tabValue) {
        this.handleSourceClick(tabValue);
      }
    },
  },
  methods: {
    ...mapMutations(['SET_SOURCE_TAB', 'SET_ADD_VIS']),
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
 
.image {
  height: 35px;
}
.imgbutton {
  margin-bottom: 15px;
}
.cvtab {
  width: 190px;
  font-size: 12px;
}
.btab {
  width: 20px;
  font-size: 12px;
}

</style>