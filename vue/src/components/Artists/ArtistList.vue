<template>
  <div v-if="!previewVis">
    <div class="artistListNormal">
      <div class="buttonspace">
        <button :disabled="!allowButtons" class="addbtn" @mousedown="clickAddArtist()">add artist</button>
        <button class="morebtn" @click="showMore()">more</button>
        <div v-if="showDropdown" class="dropdown">
          <button
            :disabled="sourceTab == null || sourceTab == 'combview' || selectedArtist == '' || !allowButtons"
            class="deletebtn"
            data-testid="delete-url-button"
            @click="deleteUrl()">
            delete selected URL
          </button>
          <button
            :disabled="selectedArtist == '' || !allowButtons"
            class="deletebtn"
            data-testid="delete-button"
            @click="clickDeleteArtist()">
            delete artist
          </button>
        </div>
      </div>

      <ArtistsAddNew :addVisibility="addVisibility" @close-add-new="closeAddNew" />

      <div class="artistlist">
        <li
          v-for="item in artistsArrayList"
          :key="item"
          :class="{ highlighted: item === selectedArtist }"
          class="listbtn"
          @mousedown="handleItemClick(item)">
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

<script setup>
import { computed, onMounted, ref, watch } from "vue";
import { useStore } from "vuex";
import axios from "axios";
import ArtistsAddNew from "@/components/Artists/ArtistsAddNew.vue";
import ArtistsPreviewDialog from "@/components/Artists/ArtistsPreviewDialog.vue";

const store = useStore();
const addVisibility = ref(false);
const artistsArrayList = ref([]);
const showDropdown = ref(false);

const allowButtons = computed(() => store.state.allowButtons);
const sourceTab = computed(() => store.state.sourceTab);
const selectedArtist = computed(() => store.state.selectedArtist);
const previewVis = computed(() => store.state.previewVis);

watch(
  () => store.state.loadListRequest,
  (newValue) => {
    if (newValue) {
      store.commit("SET_LOAD_REQUEST", false);
      loadList();
    }
  }
);

onMounted(() => {
  loadList();
});

const loadList = async () => {
  axios
    .get("/api/loadList")
    .then((response) => {
      artistsArrayList.value = response.data;
    })
    .catch((error) => {
      console.error(error);
    });
};

const handleItemClick = async (artist) => {
  axios
    .get("/api/tableData", {
      params: {
        source: sourceTab.value,
        artist: artist,
      },
    })
    .then((response) => {
      store.commit("SET_SELECTED_ARTIST", artist);
      store.commit("SET_TABLE_CONTENT", response.data);
    })
    .catch((error) => {
      console.error(error);
    });
};

const clickAddArtist = () => {
  addVisibility.value = true;
};

const closeAddNew = () => {
  addVisibility.value = false;
  loadList();
};

// delete all (last selected) artist entries from db, rebuild combview
const clickDeleteArtist = () => {
  if (selectedArtist.value !== "") {
    axios
      .delete(`/api/artist/${selectedArtist.value}`)
      .then(() => {
        store.commit("SET_SELECTED_ARTIST", "");
        store.commit("SET_SOURCE_TAB", "combview");
        loadList();
      })
      .catch((error) => {
        console.error(error);
      });
  }
};

const showMore = () => (showDropdown.value = !showDropdown.value);

const deleteUrl = () => {
  axios
    .delete("/api/url", {
      params: {
        source: sourceTab.value,
        artist: selectedArtist.value,
      },
    })
    .then(() => {
      handleItemClick(selectedArtist.value);
    })
    .catch((error) => {
      console.error("Error deleting URL:", error);
    });
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
