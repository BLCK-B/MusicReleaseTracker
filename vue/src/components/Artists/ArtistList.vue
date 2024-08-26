<template>
  <div v-if="!previewVis">
    <div class="artistListNormal">
      <div class="buttonspace">
        <button @mousedown="clickAddArtist()" class="addbtn" :disabled="!allowButtons">add artist</button>
        <button @click="showMore()" class="morebtn">more</button>
        <div class="dropdown" v-if="showDropdown">
          <button
            @click="deleteUrl()"
            :disabled="sourceTab == null || sourceTab == 'combview' || selectedArtist == '' || !allowButtons"
            class="deletebtn">
            delete selected URL
          </button>
          <button @click="clickDeleteArtist()" :disabled="selectedArtist == '' || !allowButtons" class="deletebtn">
            delete artist
          </button>
        </div>
      </div>

      <ArtistsAddNew :addVisibility="addVisibility" @close-add-new="closeAddNew" />

      <div class="artistlist">
        <li
          v-for="item in artistsArrayList"
          :key="item"
          @mousedown="handleItemClick(item)"
          :class="{ highlighted: item === selectedArtist }"
          class="listbtn">
          <div class="listitems">
            {{ item }}
          </div>
        </li>
        <!-- adding height for enabling scroll all the way -->
        <li v-for="item in artistsArrayList" :key="item"></li>
      </div>
    </div>
  </div>

  <ArtistsPreviewDialog v-if="previewVis" class="preview" />
</template>

<script>
import ArtistsAddNew from "@/components/Artists/ArtistsAddNew.vue";
import ArtistsPreviewDialog from "@/components/Artists/ArtistsPreviewDialog.vue";
import axios from "axios";
import { mapState } from "vuex";

export default {
  components: {
    ArtistsAddNew,
    ArtistsPreviewDialog,
  },
  data() {
    return {
      addVisibility: false,
      artistsArrayList: [],
      showDropdown: false,
    };
  },
  computed: {
    ...mapState(["allowButtons", "sourceTab", "selectedArtist", "previewVis"]),
  },
  created() {
    // load artist list, last clicked artist if not null
    this.loadList();
    // if (response.data !== "") this.lastClickedItem = response.data;
  },
  watch: {
    "$store.state.loadListRequest"(loadListRequest) {
      if (loadListRequest) {
        this.$store.commit("SET_LOAD_REQUEST", false);
        this.loadList();
      }
    },
  },
  methods: {
    // populate list from backend
    loadList() {
      axios
        .get("/api/loadList")
        .then((response) => {
          this.artistsArrayList = response.data;
        })
        .catch((error) => {
          console.error(error);
        });
    },
    // load respective table when artist selected
    handleItemClick(artist) {
      if (artist === this.selectedArtist) return;
      axios
        .post("/api/getTableData", { source: this.sourceTab, artist: artist })
        .then((response) => {
          this.$store.commit("SET_SELECTED_ARTIST", artist);
          this.$store.commit("SET_TABLE_CONTENT", response.data);
          this.$forceUpdate();
        })
        .catch((error) => {
          console.error(error);
        });
    },
    // show AddArtistDialog
    clickAddArtist() {
      this.addVisibility = true;
    },
    closeAddNew() {
      this.addVisibility = false;
    },
    // delete all (last selected) artist entries from db, rebuild combview
    clickDeleteArtist() {
      if (this.lastClickedItem !== "") {
        axios
          .post("/api/deleteArtist", this.selectedArtist)
          .then(() => {
            this.$store.commit("SET_SELECTED_ARTIST", "");
            this.$store.commit("SET_SOURCE_TAB", "combview");
            this.loadList();
          })
          .catch((error) => {
            console.error(error);
          });
      }
    },
    showMore() {
      this.showDropdown = !this.showDropdown;
    },
    deleteUrl() {
      // set null specific URL, trigger table reload
      axios.post("/api/deleteUrl", { source: this.sourceTab, artist: this.selectedArtist }).then(() => {
        this.handleItemClick(this.lastClickedItem);
      });
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

button {
  border: none;
  border-radius: 5px;
  background-color: var(--duller-color);
  color: var(--contrast-color);
  opacity: 0.85;
}
button:hover {
  opacity: 1;
}
button:active {
  opacity: 75%;
}

.listbtn {
  width: 92%;
  height: 28px;
  border-radius: 3px;
  display: flex;
  align-items: center;
  white-space: nowrap;
  overflow: hidden;
  padding-left: 6px;
  margin: 0;
  margin-left: 2px;
}
.artistlist {
  height: calc(100vh - 40px);
  overflow-y: scroll;
}
.artistlist li {
  list-style-type: none;
}
.buttonspace {
  margin-bottom: 5px;
}
.addbtn,
.morebtn {
  font-size: 13px;
  width: 75px;
  height: 28px;
  margin-left: 7px;
}
.dropdown {
  position: relative;
  display: grid;
  grid-template-columns: repeat(1, 1fr);
  background-color: var(--subtle-color);
  padding-right: 11px;
  padding-left: 7px;
}
.dropdown .deletebtn {
  font-size: 13px;
  height: 25px;
  margin-top: 5px;
}
.deletebtn:hover {
  background-color: red;
}
.listbtn:hover {
  background-color: var(--duller-color);
}
.highlighted {
  background-color: var(--accent-color);
  color: var(--accent-contrast);
}
.highlighted:hover {
  background-color: var(--accent-color);
}
:disabled {
  opacity: 0.5;
  pointer-events: none;
}
</style>
