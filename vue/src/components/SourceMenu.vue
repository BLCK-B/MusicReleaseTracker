<template>
  <div class="wrapper">

    <div class="tabs">
      <div @mousedown="setStoreTab('combview')" :class="{ 'active': activeTab === 'combview' }" class="cvtab">Combined view</div>
      <div @mousedown="setStoreTab('beatport')" :class="{ 'active': activeTab === 'beatport' }" class="stab">BP</div>
      <div @mousedown="setStoreTab('musicbrainz')" :class="{ 'active': activeTab === 'musicbrainz' }" class="stab">MB</div>
      <div @mousedown="setStoreTab('junodownload')" :class="{ 'active': activeTab === 'junodownload' }" class="stab">JD</div>
      <div @mousedown="setStoreTab('youtube')" :class="{ 'active': activeTab === 'youtube' }" class="stab">YT</div>
    </div>
    
    <button @click="openSettings()" class="settingsButton" :disabled="!allowButtons">
      <img v-if="primaryColor === 'Black'" class="image" src="./icons/optionsblack.png" alt="Settings"/>
      <img v-else-if="primaryColor === 'Dark'" class="image" src="./icons/optionsdark.png" alt="Settings"/>
      <img v-else-if="primaryColor === 'Light'" class="image" src="./icons/optionslight.png" alt="Settings"/>
    </button>
    <button @click="clickScrape()" @mouseover="scrapeHover()" @mouseleave="scrapeMouseOff()" class="scrapeButton" :class="{ 'scrapeActive': isActive }">
      <img class="image" src="./icons/refreshuniversal.png" alt="Refresh"/>
    </button>

    <transition name="fade">
      <div class="scrapenotice" @mouseover="scrapeMouseOff()" v-if="scrapeNotice">
        <p>Last scrape: {{ scrapeLast }}</p>
      </div>
    </transition>

  </div>
</template>

<script>
import axios from 'axios';
import { mapState, mapMutations } from 'vuex';

export default {
  data() {
     return {
       activeTab: "",
       eventSource: null,
       scrapeNotice: false,
       scrapeLast: "-",
       isActive: false,
     }
  },
  computed: {
    ...mapState([
      'sourceTab',
      'allowButtons',
      'primaryColor',
    ])
  },
  // load last clicked tab, otherwise combview as default, load scrapeLast time
  created() {
    this.activeTab = this.sourceTab;
    axios.post('/api/fillCombview')
        .catch((error) => {
          console.error(error);
        })
        .then(() => {
          if (this.sourceTab === "")
            this.setStoreTab("combview");
          else
            this.handleSourceClick(this.sourceTab);
        });
    axios.get('/api/getScrapeDate')
    .then(response => {
      this.scrapeLast = response.data;
    })
  },
  // trigger handleSourceClick if tab is not combview
  watch: {
    sourceTab(tabValue) {
      this.activeTab = tabValue;
      if (tabValue)
        this.handleSourceClick(tabValue);
    },
  },
  methods: {
    // set store tab, trigger handleSourceClick
    setStoreTab(source) {
      this.$store.commit('SET_SOURCE_TAB', source);
    },
    // load respective table
    handleSourceClick(source) {
      axios.post('/api/listOrTabClick', { item: source, origin: "tab" })
        .then((response) => {
          this.$store.commit('SET_TABLE_CONTENT', response.data);
        })
        .catch((error) => {
          console.error(error);
        });
    },
    // trigger scraping or cancel it, SSE listener for progressbar
    clickScrape() {
      this.scrapeNotice = false;
      const allowButtons = this.allowButtons;
      if (!allowButtons) {
        axios.post('/api/cancelScrape')
          .then(() => {
            this.$store.commit('SET_ALLOW_BUTTONS', true);
            this.isActive = false;
          })
      }
      else {
        this.$store.commit('SET_ALLOW_BUTTONS', false);
        this.isActive = true;
        this.eventSource = new EventSource('/progress');
        this.eventSource.onmessage = (event) => {
          const progress = parseFloat(event.data);
          this.$store.commit('SET_PROGRESS', progress);
        };

        axios.post('/api/clickScrape')
          .then(() => {
            this.isActive = false;
            this.$store.commit('SET_ALLOW_BUTTONS', true);
            this.eventSource.close();
            let time = new Date().toLocaleString('en-GB', {
              day: '2-digit',
              month: '2-digit',
              hour: '2-digit',
              minute: '2-digit'
            }).replace(/\//g, '.').replace(',', '').replace(/(\d{2})\.(\d{2})/, '\$1.\$2.');
            this.scrapeLast = time;
            this.scrapeNotice = true;
            this.handleSourceClick("combview");

            axios.post(`/api/setSetting`, { name: 'lastScrape', value: time })
            .catch(error => {
              console.error(error);
            });
          })
      }
    },
    scrapeHover() {
      this.scrapeNotice = true;
    },
    scrapeMouseOff() {
      this.scrapeNotice = false;
    },
    // open settings
    openSettings() {
      this.$store.commit('SET_SETTINGS_OPEN', true);
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
  .image {
    height: 32px;
    width: 32px;
  }
  .settingsButton, .scrapeButton {
    padding: 0;
    margin-left: 8px;
    margin-top: 2px;
    height: 32px;
    width: 32px;
    border: none;
  }
  .settingsButton:hover, .scrapeButton:hover {
    opacity: 70%;
  }
  .settingsButton {
    height: 32px;
    width: 32px;
    background-color: var(--accent-color);
  }
  .scrapeButton {
    background-color: var(--accent-color);
    margin-right: 20px;
    border-radius: 50px;
  }
  .scrapeActive {
    transition: 0.75s;
    rotate: 180deg;
    filter:hue-rotate(120deg);
  }
  .scrapeActive:hover {
    opacity: 1;
  }
  .cvtab {
    width: 80%;
    max-width: 390px;
    padding: 8px;
    border: solid 3px transparent;
    border-bottom: solid 3px var(--accent-color);
  }
  .stab {
    width: 20%;
    max-width: 110px;
    padding: 8px;
    border: solid 3px transparent;
    border-bottom: solid 3px var(--accent-color);
    white-space: nowrap;
    overflow: hidden;
  }
  .tabs :hover {
    border-bottom: solid 3px var(--dull-color);;
  }
  .active {
    transition: 0.15s;
    background-color: var(--accent-color);
    color: var(--accent-contrast);
    border-bottom: solid 3px var(--accent-color);
    border-radius: 5px;
  }
  .active:hover {
    border-bottom: solid 3px var(--accent-color);
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

  .fade-enter-from, .fade-leave-to {
    opacity: 0;
  }
  .fade-enter-active, .fade-leave-active {
    transition: 0.15s;
  }

  :disabled {
      opacity: 0.5;
      pointer-events: none;
  }

</style>