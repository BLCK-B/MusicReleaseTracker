<template>
  <div v-if="!urlExists && allowButtons && selectedArtist">
    <div v-show="sourceTab === 'musicbrainz'" class="dialog">
      <div class="nameLink">
        <h1>MusicBrainz</h1>
        <a href="https://musicbrainz.org" target="_blank">musicbrainz.org</a>
      </div>
      <p>
        Find <span class="artistText">{{ selectedArtist }}</span> on the site and copy URL.
      </p>
      <input v-model="input" placeholder="https://musicbrainz.org/artist/id/..." />
      <button @click="clickConfirmURL" class="imgbutton">
        <img v-if="primaryColor !== 'light'" class="image" src="../icons/confirmdark.png" alt="OK" />
        <img v-if="primaryColor === 'light'" class="image" src="../icons/confirmlight.png" alt="OK" />
      </button>
    </div>

    <div v-show="sourceTab === 'beatport'" class="dialog">
      <div class="nameLink">
        <h1>Beatport</h1>
        <a href="https://beatport.com" target="_blank">beatport.com</a>
      </div>
      <p>
        Find <span class="artistText">{{ selectedArtist }}</span> on the site and copy URL.
      </p>
      <input v-model="input" placeholder="https://beatport.com/artist/artistname/id/..." />
      <button @click="clickConfirmURL" class="imgbutton">
        <img v-if="primaryColor !== 'light'" class="image" src="../icons/confirmdark.png" alt="OK" />
        <img v-if="primaryColor === 'light'" class="image" src="../icons/confirmlight.png" alt="OK" />
      </button>
    </div>

    <div v-show="sourceTab === 'junodownload'" class="dialog">
      <div class="nameLink">
        <h1>Junodownload</h1>
        <a href="https://junodownload.com" target="_blank">junodownload.com</a>
      </div>
      <p>
        Find <span class="artistText">{{ selectedArtist }}</span> on the site and copy URL.
      </p>
      <input v-model="input" placeholder="https://junodownload.com/artists/artistname/..." />
      <button @click="clickConfirmURL" class="imgbutton">
        <img v-if="primaryColor !== 'light'" class="image" src="../icons/confirmdark.png" alt="OK" />
        <img v-if="primaryColor === 'light'" class="image" src="../icons/confirmlight.png" alt="OK" />
      </button>
    </div>

    <div v-show="sourceTab === 'youtube'" class="dialog">
      <div class="nameLink">
        <h1>Youtube</h1>
        <a href="https://youtube.com" target="_blank">youtube.com</a>
      </div>
      <p>
        Find an auto-generated "Topic" channel of <span class="artistText">{{ selectedArtist }}</span
        >. Either a channel ID or URL is accepted. <br />Channel handles will not work.
      </p>
      <input v-model="input" placeholder="https://youtube.com/channel/UCwZEU0wAwIyZb..." />
      <button @click="clickConfirmURL" class="imgbutton">
        <img v-if="primaryColor !== 'light'" class="image" src="../icons/confirmdark.png" alt="OK" />
        <img v-if="primaryColor === 'light'" class="image" src="../icons/confirmlight.png" alt="OK" />
      </button>
    </div>
  </div>
</template>

<script setup>
import axios from "axios";
import { useStore } from "vuex";
import { ref, computed, watch } from "vue";

const store = useStore();
const userInput = ref("");
const tableData = computed(() => store.state.tableData);
const sourceTab = computed(() => store.state.sourceTab);
const allowButtons = computed(() => store.state.allowButtons);
const selectedArtist = computed(() => store.state.selectedArtist);
const primaryColor = computed(() => store.state.primaryColor);
const urlExists = computed(() => store.state.urlExists);

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
      .post("/api/clickAddURL", { source: sourceTab.value, artist: selectedArtist.value, url: url })
      .then(() => {
        axios.post("/api/getTableData", { source: sourceTab.value, artist: selectedArtist.value }).then((response) => {
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
      .post("/api/checkExistURL", { source: sourceTab.value, artist: selectedArtist.value })
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
  padding: 10px;
  padding-left: 12px;
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
.artistText {
  user-select: text;
  font-weight: bold;
}
</style>
