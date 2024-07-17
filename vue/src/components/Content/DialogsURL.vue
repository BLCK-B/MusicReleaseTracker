<template>
  <div v-if="!urlExists && allowButtons && selectedArtist">
    <div v-if="sourceTab === 'musicbrainz'" class="dialog">
      <div class="nameLink">
        <h1>MusicBrainz</h1>
        <a href="https://musicbrainz.org" target="_blank">musicbrainz.org</a>
      </div>
      <p>
        Find <span class="artistText">{{ selectedArtist }}</span> on the site and copy URL.
      </p>
      <input v-model="input" placeholder="https://musicbrainz.org/artist/id/..." />
      <button @click="clickURL" class="imgbutton">
        <img v-if="primaryColor !== 'Light'" class="image" src="../icons/confirmdark.png" alt="OK" />
        <img v-if="primaryColor === 'Light'" class="image" src="../icons/confirmlight.png" alt="OK" />
      </button>
    </div>

    <div v-else-if="sourceTab === 'beatport'" class="dialog">
      <div class="nameLink">
        <h1>Beatport</h1>
        <a href="https://beatport.com" target="_blank">beatport.com</a>
      </div>
      <p>
        Find <span class="artistText">{{ selectedArtist }}</span> on the site and copy URL.
      </p>
      <input v-model="input" placeholder="https://beatport.com/artist/artistname/id/..." />
      <button @click="clickURL" class="imgbutton">
        <img v-if="primaryColor !== 'Light'" class="image" src="../icons/confirmdark.png" alt="OK" />
        <img v-if="primaryColor === 'Light'" class="image" src="../icons/confirmlight.png" alt="OK" />
      </button>
    </div>

    <div v-else-if="sourceTab === 'junodownload'" class="dialog">
      <div class="nameLink">
        <h1>Junodownload</h1>
        <a href="https://junodownload.com" target="_blank">junodownload.com</a>
      </div>
      <p>
        Find <span class="artistText">{{ selectedArtist }}</span> on the site and copy URL.
      </p>
      <input v-model="input" placeholder="https://junodownload.com/artists/artistname/..." />
      <button @click="clickURL" class="imgbutton">
        <img v-if="primaryColor !== 'Light'" class="image" src="../icons/confirmdark.png" alt="OK" />
        <img v-if="primaryColor === 'Light'" class="image" src="../icons/confirmlight.png" alt="OK" />
      </button>
    </div>

    <div v-else-if="sourceTab === 'youtube'" class="dialog">
      <div class="nameLink">
        <h1>Youtube</h1>
        <a href="https://youtube.com" target="_blank">youtube.com</a>
      </div>
      <p>
        Find an auto-generated "Topic" channel of <span class="artistText">{{ selectedArtist }}</span
        >. Either a channel ID or URL is accepted. <br />Channel handles will not work.
      </p>
      <input v-model="input" placeholder="https://youtube.com/channel/UCwZEU0wAwIyZb..." />
      <button @click="clickURL" class="imgbutton">
        <img v-if="primaryColor !== 'Light'" class="image" src="../icons/confirmdark.png" alt="OK" />
        <img v-if="primaryColor === 'Light'" class="image" src="../icons/confirmlight.png" alt="OK" />
      </button>
    </div>
  </div>
</template>

<script>
import axios from "axios";
import { mapState } from "vuex";

export default {
  data: () => ({
    input: "",
  }),
  computed: {
    ...mapState(["tableData", "sourceTab", "allowButtons", "selectedArtist", "primaryColor", "urlExists"]),
  },
  watch: {
    // trigger url check on every tableData change
    tableData() {
      this.determineDiagShow();
    },
  },
  methods: {
    // send input to be processed, displays preview dialog with scraped source table
    clickURL() {
      // it needs be encoded decoded trimmed ... because axios is changing symbols
      const url = encodeURIComponent(this.input);
      this.input = "";
      if (url) {
        axios
          .post("/api/clickAddURL", url)
          .then(() => {
            axios.post("/api/getTableArtistClick", { item: this.selectedArtist }).then((response) => {
              this.$store.commit("SET_TABLE_CONTENT", response.data);
              this.$store.commit("SET_PREVIEW_VIS", true);
            });
          })
          .catch((error) => {
            console.error(error);
          });
      }
    },
    // only show dialog when table is null, and if URL does not exist
    determineDiagShow() {
      if (this.tableData.length === 0) {
        axios
          .get("/api/checkExistURL")
          .then((response) => {
            this.$store.commit("SET_URL_EXISTS", response.data);
          })
          .catch((error) => {
            console.error(error);
          });
      } else this.$store.commit("SET_URL_EXISTS", true);
    },
  },
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
