<template>
  <div class="wrapper">
    <div class="tabs">
      <div :class="{ active: activeTab === 'beatport' }" class="sourceTab" @mousedown="setStoreTab('beatport')">BP</div>
      <div :class="{ active: activeTab === 'musicbrainz' }" class="sourceTab" @mousedown="setStoreTab('musicbrainz')">
        MB
      </div>
      <div :class="{ active: activeTab === 'youtube' }" class="sourceTab" @mousedown="setStoreTab('youtube')">YT</div>
    </div>

    <a v-if="isMrtUpdate" href="https://github.com/BLCK-B/MusicReleaseTracker/releases" target="_blank"
       class="newUpdateLink">
      Download new version
    </a>

    <button :disabled="!allowButtons" class="settingsButton" @click="openSettings()">
      <img v-if="primaryColor === 'dark'" alt="Settings" class="imageSettings" src="../icons/optionsdark.png"/>
      <img v-else alt="Settings" class="imageSettings" src="../icons/optionslight.png"/>
    </button>

    <button
        :class="{ scrapeActive: isActive }"
        class="scrapeButton"
        @click="clickScrape"
        @mouseleave="scrapeMouseOff"
        @mouseover="scrapeHover">
      <img alt="Refresh" class="imageScrape" src="../icons/refreshuniversal.png"/>
    </button>

    <transition name="fade">
      <div v-if="scrapeDateInfo" class="scrapenotice" @mouseover="scrapeMouseOff">
        <p>Last refresh: {{ scrapeLast }}</p>
      </div>
    </transition>
  </div>
</template>

<script setup lang="ts">
import {computed, ref, watch, onMounted} from "vue";
import {useRouter} from "vue-router";
import {useMainStore} from "@/store/mainStore.ts";
import axios from "axios";
import type {WebSources} from "@/types/Sources.ts";

const activeTab = ref("");
const eventSource = ref<EventSource | null>(null);
const scrapeDateInfo = ref(false);
const scrapeLast = ref("-");
const isActive = ref(false);
const isMrtUpdate = ref(false);

const router = useRouter();
const store = useMainStore();
const sourceTab = computed(() => store.sourceTab);
const allowButtons = computed(() => store.allowButtons);
const primaryColor = computed(() => store.primaryColor);
const selectedArtist = computed(() => store.selectedArtist);

// trigger sourceClick if tab is not combview
watch(sourceTab, (tabValue) => {
  activeTab.value = tabValue;
  if (tabValue) sourceClick(tabValue);
});

onMounted(() => {
  activeTab.value = sourceTab.value;
  axios
      .post("/api/fillCombview")
      .catch((error) => {
        console.error(error);
      })
      .then(() => {
        sourceClick(sourceTab.value);
      });
  scrapeLast.value = localStorage.getItem("scrapeLast") ?? "-";

  mrtUpdateCheck();
});

// set store tab, trigger sourceClick
const setStoreTab = (source: WebSources) => {
  if (sourceTab.value === source) source = "combview";
  store.setSourceTab(source);
};

// load respective table
const sourceClick = async (source: string) => {
  axios
      .get("/api/tableData", {
        params: {
          source: source,
          artist: selectedArtist.value,
        },
      })
      .then((response) => {
        store.setTableContent(response.data);
      })
      .catch((error) => {
        console.error(error);
      });
};

// trigger scraping or cancel it, SSE listener for progressbar
const clickScrape = () => {
  scrapeDateInfo.value = false;
  if (!allowButtons.value) {
    if (eventSource.value) {
      eventSource.value.close();
    }
    axios.post("/api/cancelScrape").then(() => {
      store.setAllowButtons(true);
      isActive.value = false;
    });
  } else {
    store.setAllowButtons(false);
    isActive.value = true;
    eventSource.value = new EventSource("http://localhost:57782/progress");
    eventSource.value.onmessage = (event) => {
      const progress = parseFloat(event.data);
      store.setProgress(progress);
    };

    axios.post("/api/scrape").then(() => {
      isActive.value = false;
      store.setAllowButtons(true);
      if (eventSource.value) {
        eventSource.value.close();
      }
      store.setProgress(0.0);

      const currentTime = new Date();
      const formattedTime = `${currentTime.getDate().toString().padStart(2, "0")}.${(currentTime.getMonth() + 1)
          .toString()
          .padStart(2, "0")} ${currentTime.getHours().toString().padStart(2, "0")}:${currentTime
          .getMinutes()
          .toString()
          .padStart(2, "0")}`;

      scrapeLast.value = formattedTime;
      scrapeDateInfo.value = true;
      sourceClick("combview");
      localStorage.setItem("scrapeLast", JSON.stringify(formattedTime));
    });
  }
};

const scrapeHover = () => {
  scrapeDateInfo.value = true;
};

const scrapeMouseOff = () => {
  scrapeDateInfo.value = false;
};

const openSettings = () => {
  router.push("/settings");
  store.setSettingsOpen(true);
};

const mrtUpdateCheck = async () => {
  const today = new Date().toISOString().slice(0, 10);
  if (localStorage.getItem("updateCheck") === today) {
    return;
  }
  axios
      .get("/api/isNewUpdate")
      .then((response) => {
        isMrtUpdate.value = response.data;
      })
      .catch((error) => {
        console.error(error);
      });
  localStorage.setItem("updateCheck", today);
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

.newUpdateLink {
  border-radius: 50px;
  width: 165px;
  padding: 5px;
  border: 2px solid var(--accent-color);
  background-color: transparent;
  color: var(--contrast-color);
  margin-right: 25px;
  opacity: 0.85;
  text-decoration: none;
  text-align: center;
}

.newUpdateLink:hover {
  opacity: 1;
}

.newUpdateLink {
  animation: hueRotate 15s linear infinite;
}

@keyframes hueRotate {
  0% {
    filter: hue-rotate(0deg);
  }
  50% {
    filter: hue-rotate(360deg);
  }
  100% {
    filter: hue-rotate(0deg);
  }
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
