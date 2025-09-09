<template>
  <div v-if="!urlExists && selectedArtist && currentSource.linkText">
    <div class="dialog">
      <div class="nameLink">
        <h1>{{ currentSource.title }}</h1>
        <a :href="currentSource.link" target="_blank">{{ currentSource.linkText }}</a>
      </div>
      <p v-html="currentSource.instructions"></p>
      <input v-model="userInput" :placeholder="currentSource.placeholder" />
      <button :disabled="!allowButtons" class="imgbutton" data-testid="confirm-button" @click="clickConfirmURL">
        <img v-if="primaryColor !== 'light'" alt="OK" class="image" src="../icons/confirmdark.png" />
        <img v-if="primaryColor === 'light'" alt="OK" class="image" src="../icons/confirmlight.png" />
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import axios from "axios";
import { useStore } from "vuex";
import { computed, ref, watch } from "vue";

const store = useStore();

const userInput = ref("");
const tableData = computed(() => store.state.tableData);
const sourceTab = computed<sourceType>(() => store.state.sourceTab);
const allowButtons = computed(() => store.state.allowButtons);
const selectedArtist = computed(() => store.state.selectedArtist);
const primaryColor = computed(() => store.state.primaryColor);
const urlExists = computed(() => store.state.urlExists);

type sourceType = "musicbrainz" | "beatport" | "youtube";

const sources = computed(() => ({
  musicbrainz: {
    title: "MusicBrainz",
    link: "https://musicbrainz.org",
    linkText: "musicbrainz.org",
    placeholder: "https://musicbrainz.org/artist/id/...",
    instructions: `Find <b>${selectedArtist.value}</b> on the site and copy URL.`,
  },
  beatport: {
    title: "Beatport",
    link: "https://beatport.com",
    linkText: "beatport.com",
    placeholder: "https://beatport.com/artist/artistname/id/...",
    instructions: `Find <b>${selectedArtist.value}</b> on the site and copy URL.`,
  },
  youtube: {
    title: "Youtube",
    link: "https://youtube.com",
    linkText: "youtube.com",
    placeholder: "https://youtube.com/channel/UCwZEU0wAwIyZb...",
    instructions: `Find an auto-generated "Topic" channel of <b>${selectedArtist.value}</b>. Both channel ID and URL are accepted.<br/>Channel handles will not work.`,
  },
}));

const currentSource = computed(() => sources.value[sourceTab.value]);

watch(tableData, () => {
  determineDiagShow();
});

// send input to be processed, displays preview dialog with scraped source table
const clickConfirmURL = () => {
  // it needs to be encoded decoded trimmed ... because axios is changing symbols
  const url = encodeURIComponent(userInput.value);
  userInput.value = "";
  if (url) {
    axios
      .post("/api/scrapePreview", null, {
        params: {
          source: sourceTab.value,
          artist: selectedArtist.value,
          url: url,
        },
      })
      .then(() => {
        axios
          .get("/api/tableData", {
            params: {
              source: sourceTab.value,
              artist: selectedArtist.value,
            },
          })
          .then((response) => {
            store.commit("SET_TABLE_CONTENT", response.data);
            store.commit("SET_PREVIEW_VIS", true);
          });
      })
      .catch((error) => {
        console.error(error);
      });
  }
};

const determineDiagShow = () => {
  if (tableData.value.length === 0) {
    axios
      .get("/api/urlExists", {
        params: {
          source: sourceTab.value,
          artist: selectedArtist.value,
        },
      })
      .then((response) => {
        store.commit("SET_URL_EXISTS", response.data);
      })
      .catch((error) => {
        console.error(error);
      });
  } else store.commit("SET_URL_EXISTS", true);
};
</script>

<style scoped>
p {
  line-height: 1.4;
}
.dialog {
  width: 400px;
  height: 300px;
  color: var(--contrast-color);
  padding: 10px 10px 10px 12px;
  border-radius: 8px;
}
.nameLink {
  display: flex;
  align-items: center;
}
input {
  background-color: var(--duller-color);
  color: var(--contrast-color);
  border: none;
  width: 360px;
  position: relative;
  font-size: 13px;
  height: 25px;
  border-radius: 3px;
  padding-left: 5px;
}
input:focus {
  outline: none;
}
a {
  margin-left: 30px;
  padding: 6px;
  border-radius: 5px;
  background-color: var(--accent-color);
  color: black;
  font-weight: bold;
  text-decoration: none;
}
h1 {
  font-size: 18px;
  font-weight: normal;
}
button {
  color: black;
  border: none;
}
button:hover {
  opacity: 70%;
}
.imgbutton,
.image {
  height: 26px;
  width: 26px;
  padding: 0;
  float: right;
  background-color: transparent;
}
.imgbutton:hover {
  opacity: 50%;
}
:disabled {
  opacity: 0.5;
  pointer-events: none;
}
</style>
