<template>
  <div v-if="!urlExists && selectedArtist && currentSource.linkText">
    <div class="dialog">
      <div class="nameLink">
        <h1>{{ currentSource.title }}</h1>
        <a :href="currentSource.link" target="_blank">{{ currentSource.linkText }}</a>
      </div>
      <p v-html="currentSource.instructions"></p>
      <input v-model="userInput" :placeholder="currentSource.placeholder"/>
      <button :disabled="!allowButtons" @click="clickConfirmURL" class="imgbutton" testid="confirm-button">
        <img v-if="primaryColor !== 'light'" alt="OK" class="image" src="../icons/confirmdark.png"/>
        <img v-if="primaryColor === 'light'" alt="OK" class="image" src="../icons/confirmlight.png"/>
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import axios from "axios";
import {useMainStore} from "@/store/mainStore.ts";
import {computed, ref, watch} from "vue";
import type {WebSource} from "@/types/Sources.ts";

const store = useMainStore();

const userInput = ref("");
const tableData = computed(() => store.tableData);
const sourceTab = computed<WebSource>(() => store.sourceTab);
const allowButtons = computed(() => store.allowButtons);
const selectedArtist = computed(() => store.selectedArtist);
const primaryColor = computed(() => store.primaryColor);
const urlExists = computed(() => store.urlExists);

type SourceDetails = {
  title: string;
  link: string;
  linkText: string;
  placeholder: string;
  instructions: string;
};

const sources = computed<Record<WebSource, SourceDetails>>(() => ({
  musicbrainz: {
    title: "MusicBrainz",
    link: `https://musicbrainz.org/search?query=${selectedArtist.value}&type=artist`,
    linkText: "musicbrainz.org",
    placeholder: "https://musicbrainz.org/artist/id/...",
    instructions: `Find <b>${selectedArtist.value}</b> on the website and copy URL of their page.<br/>This source does not provide thumbnails.`,
  },
  beatport: {
    title: "Beatport",
    link: `https://www.beatport.com/search?q=${selectedArtist.value}`,
    linkText: "beatport.com",
    placeholder: "https://beatport.com/artist/artistname/id/...",
    instructions: `Find <b>${selectedArtist.value}</b> on the website and copy URL of their page.`,
  },
  youtube: {
    title: "Youtube",
    link: `https://www.youtube.com/results?search_query=${selectedArtist.value}+-+topic&sp=EgIQAg%253D%253D`,
    linkText: "youtube.com",
    placeholder: "https://youtube.com/channel/UCwZEU0wAwIyZb...",
    instructions: `Find an auto-generated channel called <b>${selectedArtist.value} - Topic</b>. You can find it by searching the artist name on YT and filtering by channels. Or try the link above.<br/>A channel ID and URL are supported. Channel handles will not work.`,
  },
  combview: {
    title: "", link: "", linkText: "", placeholder: "", instructions: "",
  }
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
                store.setTableContent(response.data);
                store.setPreviewVis(true);
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
          store.setUrlExists(response.data);
        })
        .catch((error) => {
          console.error(error);
        });
  } else store.setUrlExists(true);
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
