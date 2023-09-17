<template>
  <div class="wrapper">
    <div class="tabs">
      <div @mousedown="handleSourceClick('combview')" :class="{ 'active': activeTab === 'combview' }" class="cvtab" id="combview">Combined view</div>
      <div @mousedown="handleSourceClick('beatport')" :class="{ 'active': activeTab === 'beatport' }" class="stab" id="beatport">BP</div>
      <div @mousedown="handleSourceClick('musicbrainz')" :class="{ 'active': activeTab === 'musicbrainz' }" class="stab" id="musicbrainz">MB</div>
      <div @mousedown="handleSourceClick('junodownload')" :class="{ 'active': activeTab === 'junodownload' }" class="stab" id="junodownload">JD</div>
    </div>
    
    <button @click="openSettings()" class="imgbutton1" :disabled="!allowButtons">
      <img class="image" src="src/components/icons/optionsblack.png" alt="Settings"/>
    </button>
    <button @click="clickScrape()" class="imgbutton2" :disabled="!allowButtons">
      <img class="image" src="src/components/icons/refreshuniversal.png" alt="Refresh"/>
    </button>

  </div>
</template>

<script>
import axios from 'axios';
import { mapState, mapMutations } from 'vuex';

export default {
  data() {
     return {
       activeTab: "combview",
       eventSource: null,
     }
  },
  computed: {
    ...mapState([
      'sourceTab',
      'tableData',
      'allowButtons',
      'settingsOpen',
    ])
  },
  created() {
    this.handleSourceClick("combview");
  },
  watch: {
    sourceTab(tabValue) {
      if (tabValue) {
        this.handleSourceClick(tabValue);
      }
    },
  },
  methods: {
    handleSourceClick(source) {
      this.activeTab = source;
      this.$store.commit('SET_ADD_VIS', false);
      axios.post('http://localhost:8080/api/sourceTabClick', { source })
        .then((response) => {
          this.$store.commit('SET_SOURCE_TAB', source); //set data to vuex store
          this.$store.commit('SET_TABLE_CONTENT', response.data);
        })
        .catch((error) => {
          console.error(error);
        });
    },
    clickScrape() {
      console.log("clickscrape");
      this.$store.commit('SET_ALLOW_BUTTONS', false);
      this.eventSource = new EventSource('http://localhost:8080/progress');
      this.eventSource.onmessage = (event) => {
        const progress = parseFloat(event.data);
        this.$store.commit('SET_PROGRESS', progress);
      };

      axios.post('http://localhost:8080/api/clickScrape')
        .then(() => {
          this.$store.commit('SET_ALLOW_BUTTONS', true);
          this.handleSourceClick("combview");
          this.eventSource.close();
          this.$store.commit('SET_PROGRESS', 0);
        })
        .catch((error) => {
          console.error(error);
        });
    },
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
.imgbutton1:hover, .imgbutton2:hover {
  opacity: 70%;
}
.imgbutton1 {
  padding: 0;
  height: 32px;
  width: 32px;
  margin-left: 8px;
  background-color: var(--accent-color);
  border: none;
  margin-top: 2px;
  height: 32px;
  width: 32px;
}
.imgbutton2 {
  padding: 0;
  margin-left: 8px;
  margin-right: 20px;
  background-color: var(--accent-color);
  border: none;
  margin-top: 2px;
  border-radius: 50px;
  height: 32px;
  width: 32px;
}
.cvtab {
  width: 80%;
  max-width: 400px;
  padding: 8px;
  border: solid 3px transparent;
  border-bottom: solid 3px var(--accent-color);
}
.stab {
  width: 20%;
  max-width: 150px;
  padding: 8px;
  border: solid 3px transparent;
  border-bottom: solid 3px var(--accent-color);
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

</style>