<template>
  <div v-if="!previewVis">
    <div class="buttonspace">
      <button :disabled="!allowButtons" class="addbtn" @mousedown="addArtist">add artist</button>
      <button class="morebtn" @click="showMore()">more</button>
      <div v-show="showDropdown" class="dropdown">
        <button
            :disabled="urlButtonDisabled"
            @click="visitUrl"
            class="menubtn">
          visit source URL
        </button>
        <button
            :disabled="urlButtonDisabled"
            @click="deleteUrl"
            class="menubtn">
          delete selected URL
        </button>
        <button
            :disabled="selectedArtist === '' || !allowButtons"
            @click="deleteArtist"
            class="menubtn">
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
      <br/>
      <br/>
      <br/>
      <br/>
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
const artistsArrayList = ref<string[]>([]);
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

const urlButtonDisabled = computed(() => {
  return sourceTab.value == null || sourceTab.value === 'combview' || selectedArtist.value === '' || !allowButtons.value || !store.sourcesWithUrls.some(s => s === sourceTab.value);
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

const getSourcesWithUrls = async (artist: string) => {
  try {
    const response = await axios.get("/api/sourcesWithUrl", {
      params: {
        artist
      },
    });
    return response.data;
  } catch (error) {
    console.error(error);
    return [];
  }
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

  store.setSourcesWithUrls(await getSourcesWithUrls(artist));
};

const addArtist = () => {
  addVisibility.value = true;
};

const closeAddNew = () => {
  addVisibility.value = false;
  loadList();
};

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

const visitUrl = async () => {
  try {
    const response = await axios.get("/api/sourceUrl", {
      params: {
        source: sourceTab.value,
        artist: selectedArtist.value,
      },
    });
    const url = response.data;
    if (url) {
      window.open(url, "_blank", "noopener,noreferrer");
    }
  } catch (error) {
    console.error(error);
  }
};

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
  cursor: pointer;
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
  cursor: pointer;
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
  display: flex;
  flex-direction: column;
  background-color: var(--subtle-color);
  padding-right: 11px;
  padding-left: 7px;
}

.dropdown .menubtn {
  font-size: 13px;
  height: 25px;
  margin-top: 5px;
}

.menubtn:hover {
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
