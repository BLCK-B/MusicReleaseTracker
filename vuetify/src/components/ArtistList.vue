<template>
  <v-card class="mx-auto" max-width="300">

    <v-row justify="end">
        <v-btn @mousedown="clickAddArtist()" class="addbtn">add</v-btn>
        <v-btn class="deletebtn">delete</v-btn>
    </v-row>

    <v-list>
      <!-- loop through the array and display each artist as a clickable list item -->
      <v-btn class="listbtn" v-for="item in artistsArrayList" :key="item" @mousedown="handleItemClick(item)" :class="{'highlighted': item === lastClickedItem}" text>
          <v-row justify="start">
          <v-list-item-title>{{ item }}</v-list-item-title>
          </v-row>
      </v-btn>
    </v-list>
  </v-card>
</template>

<script>
import axios from 'axios';
import { mapState, mapMutations } from 'vuex';

export default {
  data() {
    return {
      artistsArrayList: [],
      lastClickedItem: null,
    };
  },
  created() {
    this.loadList();
  },
  methods: {
    ...mapMutations(['SET_SELECTED_ARTIST', 'SET_TABLE_CONTENT', 'SET_ADD_VIS']),
    loadList() {
      console.log("loadlist");
      axios.get('http://localhost:8080/api/loadList')
        .then(response => {
          this.artistsArrayList = response.data;
        })
        .catch(error => {
          console.error(error);
        });
    },
    handleItemClick(artist) {
      this.$store.commit('SET_ADD_VIS', false);
      this.lastClickedItem = artist;
      axios.post('http://localhost:8080/api/artistListClick', { artist })
        .then(response => {
          this.$store.commit('SET_SELECTED_ARTIST', artist); //set data to vuex store
          this.$store.commit('SET_TABLE_CONTENT', response.data);
        })
        .catch(error => {
          console.error(error);
        });
    },
    clickAddArtist() {
      this.$store.commit('SET_ADD_VIS', true);
    },
  },

};
</script>

<style scoped>
.listbtn {
  display: block;
  width: 100%;
  margin-top: 3px;
  height: 25px !important;
  border-radius: 0px;
}
.v-list-item-title {
  font-size: 14px !important;
}

.addbtn, .deletebtn {
  font-size: 12px;
  width: 70px;
  height: 25px !important;
  border-radius: 5px;
}
.addbtn {
  height: 20px;
}
.deletebtn {
  margin-left: 5px;
  margin-right: 25px;
}

.v-card {
  margin-top: 4px;
}
.v-row {
  margin-top: 1px;
  margin-bottom: 0px;
}

.highlighted {
  background-color: #CCCCFF;
}

.v-list-item-title {
  font-size: 14px;
}

</style>