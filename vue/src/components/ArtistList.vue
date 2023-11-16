<template>
  <div class="buttonspace">
    <button @mousedown="clickAddArtist()" class="addbtn" :disabled="!allowButtons">add</button>
    <button @click="showMore()" class="morebtn">more</button>
    <div class="dropdown" v-if="showDrop">
      <button @click="deleteUrl()" :disabled="sourceTab == null || sourceTab == 'combview' || artist == '' || !allowButtons" class="deletebtn">delete selected URL</button>
      <button @click="clickDeleteArtist()" :disabled="artist == '' || !allowButtons" class="deletebtn">delete artist</button>
    </div>

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
import { mapState } from 'vuex';

export default {
  data() {
    return {
      artistsArrayList: [],
      lastClickedItem: null,
      showDrop: false,
    };
  },
  created() {
    //load artist list, last clicked artist if not null
    this.loadList();
    axios.get('http://localhost:8080/api/getLastArtist')
    .then(response => {
      if (response.data !== "")
        this.lastClickedItem = response.data;
    })
  },
  computed: {
    ...mapState([
      "allowButtons",
      "sourceTab",
      "artist",
    ])
  },
  watch: {
    '$store.state.loadListRequest'(loadListRequest) {
        if (loadListRequest) {
          this.$store.commit('SET_LOAD_REQUEST', false);
          this.loadList();
        }
    }
  },
  methods: {
    //populate list from backend
    loadList() {
      axios.get('http://localhost:8080/api/loadList')
        .then(response => {
          this.artistsArrayList = response.data;
        })
        .catch(error => {
          console.error(error);
        });
    },
    //load respective table when artist selected
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
    //show AddArtistDialog
    clickAddArtist() {
      this.$store.commit('SET_ADD_VIS', true);
    },
    //delete all (last selected) artist entries from db, rebuild combview
    clickDeleteArtist() {
      if (this.lastClickedItem !== "") {
        axios.get('http://localhost:8080/api/clickArtistDelete')
        .then(() => {
          this.$store.commit('SET_SELECTED_ARTIST', "");
          this.$store.commit('SET_SOURCE_TAB', "combview");
          this.loadList();
        })
        .catch(error => {
          console.error(error);
        });
      }
    },
    showMore() {
      this.showDrop = !this.showDrop;
    },
    deleteUrl() {
      //set null specific URL, trigger table reload
      axios.post('http://localhost:8080/api/deleteUrl')
      .then(() => {
        this.handleItemClick(this.lastClickedItem);
        })
    },
  },
};
</script>

<style scoped>
*::-webkit-scrollbar {
  width: 8px;
  background: transparent;
}
*::-webkit-scrollbar-thumb {
  background-color: var(--dull-color);
}
.listbtn {
  width: 92%;
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
.artistlist li {
  list-style-type: none;
}
.buttonspace {
  margin-bottom: 5px;
}
.addbtn, .morebtn {
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
.addbtn:hover, .morebtn:hover {
  background-color: var(--accent-color);
  border: 2px solid var(--accent-color);
  color: var(--accent-contrast);
}
.addbtn:active, .morebtn:active {
  opacity: 75%;
}
.morebtn {
  margin-left: 5px;
}
.dropdown {
  position: relative;
  display: grid;
  grid-template-columns: repeat(1, 1fr);
  background-color: var(--subtle-color);
  padding-right: 10px;
  padding-left: 6px;
}
.dropdown .deletebtn {
  font-size: 12px;
  height: 25px;
  margin-top: 3px;
  border: 2px solid var(--dull-color);
  border-radius: 6px;
  background-color: transparent;
  color: var(--contrast-color);
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

:disabled {
  opacity: 0.5;
  pointer-events: none;
}



</style>