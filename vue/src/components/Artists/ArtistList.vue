<template>
  <div v-if="!previewVis">
    <div class="artistListNormal">
      <div class="buttonspace">
        <button :disabled="!allowButtons" class="addbtn" @mousedown="addArtist">add artist</button>
        <button class="morebtn" @click="showMore()">more</button>
        <div v-if="showDropdown" class="dropdown">
          <button
              :disabled="sourceTab == null || sourceTab === 'combview' || selectedArtist === '' || !allowButtons"
              @click="deleteUrl"
              class="deletebtn"
              testid="delete-url-button">
            delete selected URL
          </button>
          <button
              :disabled="selectedArtist === '' || !allowButtons"
              @click="deleteArtist"
              class="deletebtn"
              testid="delete-button">
            delete artist
          </button>
        </div>
      </div>

      <ArtistsAddNew :addVisibility="addVisibility" @close-add-new="closeAddNew"/>

      <div class="artistlist">
        <div
            v-for="item in artistsArrayList"
            :key="item"
            :class="{ highlighted: item === selectedArtist }"
            class="listbtn"
            @mousedown="artistSelected(item)">
          <div class="listitems">
            {{ item }}
          </div>
        </div>
        <!-- adding height for enabling scroll all the way -->
        <div v-for="item in artistsArrayList" :key="item"></div>
      </div>
    </div>
  </div>

  <ArtistsPreviewDialog v-if="previewVis" class="preview"/>
</template>

<script setup lang="ts">
import {computed, onMounted, ref, watch} from "vue";
import {useMainStore} from "@/store/mainStore.ts";
import axios from "axios";
import ArtistsAddNew from "@/components/Artists/ArtistsAddNew.vue";
import ArtistsPreviewDialog from "@/components/Artists/ArtistsPreviewDialog.vue";

const store = useMainStore();
const addVisibility = ref(false);
const artistsArrayList = ref([]);
const showDropdown = ref(false);

const allowButtons = computed(() => store.allowButtons);
const sourceTab = computed(() => store.sourceTab);
const selectedArtist = computed(() => store.selectedArtist);
const previewVis = computed(() => store.previewVis);

watch(
    () => store.loadListRequest,
    (newValue) => {
      if (newValue) {
        store.setLoadRequest(false);
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

const artistSelected = async (artist: string) => {
  axios
      .get("/api/tableData", {
        params: {
          source: sourceTab.value,
          artist: artist,
        },
      })
      .then((response) => {
        store.setSelectedArtist(artist);
        store.setTableContent(response.data);
      })
      .catch((error) => {
        console.error(error);
      });
};

const addArtist = () => {
  addVisibility.value = true;
};

const closeAddNew = () => {
  addVisibility.value = false;
  loadList();
};

// delete all (last selected) artist entries from db, rebuild combview
const deleteArtist = () => {
  if (selectedArtist.value !== "") {
    axios
        .delete(`/api/artist/${selectedArtist.value}`)
        .then(() => {
          store.setSelectedArtist("");
          store.setSourceTab("combview")
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
        artistSelected(selectedArtist.value);
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
  margin: 0 0 0 2px;
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
