<template>

      <v-tabs
        v-model="sourceTab"
      >
        <v-tab style="height:45px;" value="combview">Combined view</v-tab>
        <v-tab style="height:45px;" value="beatport">BP</v-tab>
        <v-tab style="height:45px;" value="musicbrainz">MB</v-tab>
        <v-tab style="height:45px;" value="junodownload">JD</v-tab>
      </v-tabs>

</template>

<script>
import axios from 'axios';
import { mapState, mapMutations } from 'vuex';

export default {
  computed: {
    ...mapState(['sourceTab']),
  },
  //created() {
    //this.handleSourceClick();
  //},
  watch: {
    sourceTab(tabValue) {
      if (tabValue) {
        this.handleSourceClick(tabValue);
      }
    },
  },
  methods: {
    ...mapMutations(['SET_SOURCE_TAB']),
    handleSourceClick(source) {
      axios.post('http://localhost:8080/api/sourceTabClick', { source })
        .then((response) => {
          this.SET_SOURCE_TAB(source); // Update the sourceTab state
          this.SET_TABLE_CONTENT(response.data); // Update table content state
          //this.$emit('update-table', response.data);
        })
        .catch((error) => {
          console.error(error);
        });
    },
  },
};
</script>

<style scoped>
 
</style>