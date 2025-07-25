<!--
  -         MusicReleaseTracker
  -         Copyright (C) 2023 - 2025 BLCK
  -         This program is free software: you can redistribute it and/or modify
  -         it under the terms of the GNU General Public License as published by
  -         the Free Software Foundation, either version 3 of the License, or
  -         (at your option) any later version.
  -         This program is distributed in the hope that it will be useful,
  -         but WITHOUT ANY WARRANTY; without even the implied warranty of
  -         MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  -         GNU General Public License for more details.
  -         You should have received a copy of the GNU General Public License
  -         along with this program.  If not, see <https://www.gnu.org/licenses/>.
  -->

<template>
  <div class="wrapper">
    <div class="tabs">
      <div :class="{ active: activeTab === 'beatport' }" class="sourceTab" @mousedown="setStoreTab('beatport')">BP</div>
      <div :class="{ active: activeTab === 'musicbrainz' }" class="sourceTab" @mousedown="setStoreTab('musicbrainz')">MB</div>
      <div :class="{ active: activeTab === 'youtube' }" class="sourceTab" @mousedown="setStoreTab('youtube')">YT</div>
    </div>

    <button :disabled="!allowButtons" class="settingsButton" @click="openSettings()">
      <img v-if="primaryColor === 'black'" alt="Settings" class="imageSettings" src="../icons/optionsblack.png" />
      <img v-else-if="primaryColor === 'dark'" alt="Settings" class="imageSettings" src="../icons/optionsdark.png" />
      <img v-else-if="primaryColor === 'light'" alt="Settings" class="imageSettings" src="../icons/optionslight.png" />
    </button>
    <button
      :class="{ scrapeActive: isActive }"
      class="scrapeButton"
      @click="clickScrape"
      @mouseleave="scrapeMouseOff"
      @mouseover="scrapeHover">
      <img alt="Refresh" class="imageScrape" src="../icons/refreshuniversal.png" />
    </button>

    <transition name="fade">
      <div v-if="scrapeDateInfo" class="scrapenotice" @mouseover="scrapeMouseOff">
        <p>Last scrape: {{ scrapeLast }}</p>
      </div>
    </transition>
  </div>
</template>

<script setup>
import {computed, onBeforeMount, ref, watch} from "vue";
import {useStore} from "vuex";
import {useRouter} from "vue-router";
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
  axios.get("/api/scrapeDate").then((response) => {
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
    .get("/api/tableData", {
      params: {
        source: source,
        artist: selectedArtist.value,
      },
    })
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

    axios.post("/api/scrape").then(() => {
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
      const params = new URLSearchParams({
        name: "lastScrape",
        value: time,
      });
      axios.put(`/api/setting?${params.toString()}`).catch((error) => {
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
