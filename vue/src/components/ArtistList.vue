<template>
  <div class="buttonspace">
    <button @mousedown="clickAddArtist" class="addbtn" :disabled="!allowButtons">add</button>
    <button @mousedown="clickDeleteArtist" class="deletebtn" :disabled="!allowButtons">delete</button>
  </div>

    <div class="artistlist">
      <!-- loop through the array and display each artist as a clickable list item -->
      <li v-for="item in artistsArrayList" :key="item" @mousedown="handleItemClick(item)" :class="{'highlighted': item === lastClickedItem}" class="listbtn">
          <div class="listitems">
            {{ item }}
          </div>
      </li>
      <!-- adding height for enabling scroll all the way -->
      <li v-for="item in artistsArrayList" :key="item"></li>
    </div>

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
  computed: {
    ...mapState([
      'allowButtons',
    ])
  },
  methods: {
    loadList() {
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
          this.$store.commit('SET_SELECTED_ARTIST', artist);
          this.$store.commit('SET_TABLE_CONTENT', response.data);
        })
        .catch(error => {
          console.error(error);
        });
    },
    clickAddArtist() {
      this.$store.commit('SET_ADD_VIS', true);
    },
    clickDeleteArtist() {
      if (this.lastClickedItem !== "") {
        axios.get('http://localhost:8080/api/clickArtistDelete')
        .then(() => {
          this.$store.commit('SET_SELECTED_ARTIST', "");
          this.$store.commit('SET_SOURCE_TAB', "musicbrainz");
          this.loadList();
        })
        .catch(error => {
          console.error(error);
        });
      }
    },
  },
  watch: {
    '$store.state.loadListRequest'(loadListRequest) {
      if (loadListRequest) {
        this.$store.commit('SET_LOAD_REQUEST', false);
        this.loadList();
      }
    }
  },
};
</script>

<style scoped>
.listbtn {
  width: 95%;
  height: 28px;
  border-radius: 0px;
  margin: 0;
  display: flex;
  align-items: center;
  white-space: nowrap;
  overflow: hidden;
  padding-left: 7px;
}
.artistlist {
  height: 100vh;
  overflow-y: scroll;
}
.buttonspace {
  margin-bottom: 5px;
}
.addbtn, .deletebtn {
  font-size: 12px;
  width: 75px;
  height: 28px;
  border: 2px solid var(--dull-color);
  border-radius: 6px;
  background-color: transparent;
  color: var(--contrast-color);
}
.addbtn {
  margin-left: 5px;
}
.addbtn:hover {
  background-color: var(--accent-color);
  border: 2px solid var(--accent-color);
  color: var(--accent-contrast);
}
.deletebtn {
  margin-left: 5px;
}
.deletebtn:hover {
  background-color: red;
  border: 2px solid red;
}
.listbtn:hover {
  background-color: var(--duller-color);
}
.highlighted {
  background-color: var(--accent-color);
  color: var(--accent-contrast);
}
.highlighted:hover {
  background-color:  var(--accent-color);
}


</style>