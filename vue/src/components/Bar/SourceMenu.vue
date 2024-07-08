<template>
  <div class="wrapper">
    <div class="tabs">
      <div @mousedown="setStoreTab('beatport')" :class="{ active: activeTab === 'beatport' }" class="sourceTab">BP</div>
      <div @mousedown="setStoreTab('musicbrainz')" :class="{ active: activeTab === 'musicbrainz' }" class="sourceTab">MB</div>
      <div @mousedown="setStoreTab('junodownload')" :class="{ active: activeTab === 'junodownload' }" class="sourceTab">JD</div>
      <div @mousedown="setStoreTab('youtube')" :class="{ active: activeTab === 'youtube' }" class="sourceTab">YT</div>
    </div>

    <button @click="openSettings()" class="settingsButton" :disabled="!allowButtons">
      <img v-if="primaryColor === 'Black'" class="imageSettings" src="../icons/optionsblack.png" alt="Settings" />
      <img v-else-if="primaryColor === 'Dark'" class="imageSettings" src="../icons/optionsdark.png" alt="Settings" />
      <img v-else-if="primaryColor === 'Light'" class="imageSettings" src="../icons/optionslight.png" alt="Settings" />
    </button>
    <button
      @click="clickScrape()"
      @mouseover="scrapeHover()"
      @mouseleave="scrapeMouseOff()"
      class="scrapeButton"
      :class="{ scrapeActive: isActive }">
      <img class="imageScrape" src="../icons/refreshuniversal.png" alt="Refresh" />
    </button>

    <transition name="fade">
      <div class="scrapenotice" @mouseover="scrapeMouseOff()" v-if="scrapeDateInfo">
        <p>Last scrape: {{ scrapeLast }}</p>
      </div>
    </transition>
  </div>
</template>

<script>
import axios from "axios";
import { mapState } from "vuex";

export default {
  data() {
    return {
      activeTab: "",
      eventSource: null,
      scrapeDateInfo: false,
      scrapeLast: "-",
      isActive: false,
    };
  },
  computed: {
    ...mapState(["sourceTab", "allowButtons", "primaryColor"]),
  },
  // load last clicked tab, otherwise combview as default, load scrapeLast time
  created() {
    this.activeTab = this.sourceTab;
    axios
      .post("/api/fillCombview")
      .catch((error) => {
        console.error(error);
      })
      .then(() => {
        if (this.sourceTab === "") this.setStoreTab("combview");
        else this.handleSourceClick(this.sourceTab);
      });
    axios.get("/api/getScrapeDate").then((response) => {
      this.scrapeLast = response.data;
    });
  },
  // trigger handleSourceClick if tab is not combview
  watch: {
    sourceTab(tabValue) {
      this.activeTab = tabValue;
      if (tabValue) this.handleSourceClick(tabValue);
    },
  },
  methods: {
    // set store tab, trigger handleSourceClick
    setStoreTab(source) {
      if (this.sourceTab === source) source = "combview";
      this.$store.commit("SET_SOURCE_TAB", source);
    },
    // load respective table
    handleSourceClick(source) {
      axios
        .post("/api/listOrTabClick", { item: source, origin: "tab" })
        .then((response) => {
          this.$store.commit("SET_TABLE_CONTENT", response.data);
        })
        .catch((error) => {
          console.error(error);
        });
    },
    // trigger scraping or cancel it, SSE listener for progressbar
    clickScrape() {
      this.scrapeDateInfo = false;
      const allowButtons = this.allowButtons;
      if (!allowButtons) {
        this.eventSource.close;
        axios.post("/api/cancelScrape").then(() => {
          this.$store.commit("SET_ALLOW_BUTTONS", true);
          this.isActive = false;
        });
      } else {
        this.$store.commit("SET_ALLOW_BUTTONS", false);
        this.isActive = true;
        this.eventSource = new EventSource("http://localhost:57782/progress");
        this.eventSource.onmessage = (event) => {
          const progress = parseFloat(event.data);
          this.$store.commit("SET_PROGRESS", progress);
        };

        axios.post("/api/clickScrape").then(() => {
          this.isActive = false;
          this.$store.commit("SET_ALLOW_BUTTONS", true);
          this.eventSource.close();
          let time = new Date()
            .toLocaleString("en-GB", {
              day: "2-digit",
              month: "2-digit",
              hour: "2-digit",
              minute: "2-digit",
            })
            .replace(/\//g, ".")
            .replace(",", "")
            .replace(/(\d{2})\.(\d{2})/, "$1.$2.");
          this.scrapeLast = time;
          this.scrapeDateInfo = true;
          this.handleSourceClick("combview");

          axios.post(`/api/setSetting`, { name: "lastScrape", value: time }).catch((error) => {
            console.error(error);
          });
        });
      }
    },
    scrapeHover() {
      this.scrapeDateInfo = true;
    },
    scrapeMouseOff() {
      this.scrapeDateInfo = false;
    },
    // open settings
    openSettings() {
      this.$router.push("/settings");
      this.$store.commit("SET_SETTINGS_OPEN", true);
    },
  },
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
  height: 34px;
  width: 34px;
}
.settingsButton:hover,
.scrapeButton:hover {
  opacity: 70%;
}
.settingsButton {
  border: none;
  padding: 0;
  height: 34px;
  width: 34px;
  background-color: var(--accent-color);
  margin-right: 8px;
}
.scrapeButton {
  border: none;
  padding: 0;
  background-color: var(--accent-color);
  height: 34px;
  width: 34px;
  margin-right: 25px;
  border-radius: 50px;
}
.scrapeActive {
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
  margin-right: 6px;
  opacity: 0.85;
}
.tabs :hover {
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
  z-index: 50;
  background-color: var(--duller-color);
  border-radius: 5px;
  padding-right: 10px;
  padding-left: 10px;
  right: 14px;
  top: 42px;
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
