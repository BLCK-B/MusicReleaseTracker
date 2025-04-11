<template>
  <div class="wrapper">
    <div class="tabs">
      <div @mousedown="setStoreTab('beatport')" :class="{ active: activeTab === 'beatport' }" class="sourceTab">BP</div>
      <div @mousedown="setStoreTab('musicbrainz')" :class="{ active: activeTab === 'musicbrainz' }" class="sourceTab">MB</div>
      <div @mousedown="setStoreTab('junodownload')" :class="{ active: activeTab === 'junodownload' }" class="sourceTab">JD</div>
      <div @mousedown="setStoreTab('youtube')" :class="{ active: activeTab === 'youtube' }" class="sourceTab">YT</div>
    </div>

    <button @click="openSettings()" class="settingsButton" :disabled="!allowButtons">
      <img v-if="primaryColor === 'black'" class="imageSettings" src="../icons/optionsblack.png" alt="Settings" />
      <img v-else-if="primaryColor === 'dark'" class="imageSettings" src="../icons/optionsdark.png" alt="Settings" />
      <img v-else-if="primaryColor === 'light'" class="imageSettings" src="../icons/optionslight.png" alt="Settings" />
    </button>
    <button
      @click="clickScrape"
      @mouseover="scrapeHover"
      @mouseleave="scrapeMouseOff"
      class="scrapeButton"
      :class="{ scrapeActive: isActive }">
      <img class="imageScrape" src="../icons/refreshuniversal.png" alt="Refresh" />
    </button>

    <transition name="fade">
      <div class="scrapenotice" @mouseover="scrapeMouseOff" v-if="scrapeDateInfo">
        <p>Last scrape: {{ scrapeLast }}</p>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, computed, watch, onBeforeMount } from "vue";
import { useStore } from "vuex";
import { useRouter } from "vue-router";
import axios from "axios";

const activeTab = ref("");
const eventSource = ref(null);
const scrapeDateInfo = ref(false);
const scrapeLast = ref("-");
const isActive = ref(false);

const router = useRouter();
const store = useStore();
const sourceTab = computed(() => store.state.sourceTab);
const allowButtons = computed(() => store.state.allowButtons);
const primaryColor = computed(() => store.state.primaryColor);
const selectedArtist = computed(() => store.state.selectedArtist);

// load last clicked tab, otherwise combview as default, load scrapeLast time
onBeforeMount(() => {
  activeTab.value = sourceTab.value;
  axios
    .post("/api/fillCombview")
    .catch((error) => {
      console.error(error);
    })
    .then(() => {
      if (sourceTab.value === "") setStoreTab("combview");
      else handleSourceClick(sourceTab.value);
    });
  axios.get("/api/getScrapeDate").then((response) => {
    scrapeLast.value = response.data;
  });
});

// trigger handleSourceClick if tab is not combview
watch(sourceTab, (tabValue) => {
  activeTab.value = tabValue;
  if (tabValue) handleSourceClick(tabValue);
});

// set store tab, trigger handleSourceClick
const setStoreTab = (source) => {
  if (sourceTab.value === source) source = "combview";
  store.commit("SET_SOURCE_TAB", source);
};
// load respective table
const handleSourceClick = (source) => {
  axios
    .post("/api/getTableData", { source: source, artist: selectedArtist.value })
    .then((response) => {
      store.commit("SET_TABLE_CONTENT", response.data);
    })
    .catch((error) => {
      console.error(error);
    });
};
// trigger scraping or cancel it, SSE listener for progressbar
const clickScrape = () => {
  scrapeDateInfo.value = false;
  if (!allowButtons.value) {
    eventSource.value.close;
    axios.post("/api/cancelScrape").then(() => {
      store.commit("SET_ALLOW_BUTTONS", true);
      isActive.value = false;
    });
  } else {
    store.commit("SET_ALLOW_BUTTONS", false);
    isActive.value = true;
    eventSource.value = new EventSource("http://localhost:57782/progress");
    eventSource.value.onmessage = (event) => {
      const progress = parseFloat(event.data);
      store.commit("SET_PROGRESS", progress);
    };

    axios.post("/api/clickScrape").then(() => {
      isActive.value = false;
      store.commit("SET_ALLOW_BUTTONS", true);
      eventSource.value.close();

      const currentTime = new Date();
      const time = `${currentTime.getDate().toString().padStart(2, "0")}.${(currentTime.getMonth() + 1)
        .toString()
        .padStart(2, "0")} ${currentTime.getHours().toString().padStart(2, "0")}:${currentTime
        .getMinutes()
        .toString()
        .padStart(2, "0")}`;

      scrapeLast.value = time;
      scrapeDateInfo.value = true;
      handleSourceClick("combview");

      axios.post(`/api/setSetting`, { name: "lastScrape", value: time }).catch((error) => {
        console.error(error);
      });
    });
  }
};
const scrapeHover = () => {
  scrapeDateInfo.value = true;
};
const scrapeMouseOff = () => {
  scrapeDateInfo.value = false;
};
// open settings
const openSettings = () => {
  router.push("/settings");
  store.commit("SET_SETTINGS_OPEN", true);
};
</script>

<style scoped>
.wrapper {
  min-width: 500px;
  width: 100%;
  display: flex;
  align-items: center;
  height: 38px;
}
.tabs {
  display: flex;
  text-align: center;
  font-weight: bold;
  flex-grow: 1;
  height: 38px;
}
.imageSettings,
.imageScrape {
  height: 35px;
  width: 35px;
}
.settingsButton:hover,
.scrapeButton:hover {
  opacity: 70%;
}
.settingsButton {
  border: none;
  padding: 0;
  height: 35px;
  width: 35px;
  background-color: var(--accent-color);
  margin-right: 8px;
}
.scrapeButton {
  border: none;
  padding: 0;
  background-color: var(--accent-color);
  height: 35px;
  width: 35px;
  margin-right: 19px;
  border-radius: 50px;
}
.scrapeActive {
  transition: 0.75s;
  rotate: 180deg;
  filter: hue-rotate(120deg);
}
.scrapeActive:hover {
  opacity: 1;
}
.sourceTab {
  border-radius: 5px;
  width: 20%;
  max-width: 85px;
  padding: 8px;
  border: solid 3px transparent;
  white-space: nowrap;
  overflow: hidden;
  background-color: var(--duller-color);
  margin-right: 5px;
  opacity: 0.85;
}
.tabs:hover {
  opacity: 1;
}
.active {
  transition: 0.1s;
  opacity: 1;
  background-color: var(--accent-color);
  color: var(--accent-contrast);
  border: solid 3px transparent;
}
.active:hover {
  background-color: var(--accent-color);
}

.scrapenotice {
  position: absolute;
  right: 62px;
  top: 2px;
  height: 35px;
  z-index: 3;
  border-radius: 5px;
  background-color: var(--duller-color);
  padding-right: 8px;
  padding-left: 8px;
  display: flex;
  align-items: center;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
.fade-enter-active,
.fade-leave-active {
  transition: 0.15s;
}

:disabled {
  opacity: 0.5;
  pointer-events: none;
}
</style>
