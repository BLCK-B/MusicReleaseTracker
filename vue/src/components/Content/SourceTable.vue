<template>
  <div class="table-container" v-if="hideTable">
    <div class="table-body">
      <div v-for="(mediaItem, mediaIndex) in this.tableData" :key="mediaIndex" class="aBubble">
        <table>
          <tbody>
            <template v-if="mediaItem.songs && mediaItem.songs.length">
              <tr v-if="mediaItem.album" class="album-header">
                <td class="tdalbumname">
                  <strong>{{ mediaItem.album }}</strong>
                </td>
                <td class="tdartist"></td>
                <td class="tddate">{{ formatDate(mediaItem.date) }}</td>
              </tr>
              <tr class="album-bubble" v-for="(song, songIndex) in mediaItem.songs" :key="songIndex">
                <td class="tdsong">{{ song.name }}</td>
              </tr>
            </template>
            <template v-else>
              <tr class="single-bubble" :class="{ 'future-date': isDateInFuture(mediaItem.date) }">
                <td class="tdsong">{{ mediaItem.name }}</td>
                <td class="tdartist" v-if="!hideArtistColumn">{{ mediaItem.artists }}</td>
                <td class="tddate">{{ formatDate(mediaItem.date) }}</td>
              </tr>
            </template>
          </tbody>
        </table>
      </div>
    </div>
  </div>

  <div class="emptynotice" v-if="urlExists && !hideTable && !previewVis && sourceTab !== 'combview'">
    <p>table empty</p>
  </div>
  <div class="quickstart" v-if="!urlExists && !hideTable && !previewVis && sourceTab === 'combview'">
    <p>
      <span class="title">Quickstart guide</span> <br />
      1. click "add artist" to insert an artist <br />
      2. select any source at the top <br />
      3. find the artist on the website, copy and paste link or ID <br />
      4. to scrape, click refresh button in the top right corner <br />
    </p>
  </div>
</template>

<script>
import { mapState } from "vuex";

export default {
  mounted() {
    this.isDateInFuture;
  },
  computed: {
    ...mapState(["tableData", "previewVis", "selectedArtist", "isoDates", "sourceTab", "urlExists"]),
    hideArtistColumn() {
      return this.sourceTab !== "combview" && this.selectedArtist !== "";
    },
    hideTable() {
      return this.tableData.some((item) => item.song !== null);
    },
  },
  methods: {
    isDateInFuture(dateString) {
      const date = new Date(dateString);
      return date > new Date();
    },
    formatDate(dateString) {
      if (!this.isoDates) {
        if (dateString === undefined) return dateString;
        const date = new Date(dateString);
        const day = date.getDate();
        const month = date.getMonth() + 1;
        const year = date.getFullYear();
        return `${day}. ${month}. ${year}`;
      } else return dateString;
    },
  },
};
</script>

<style scoped>
.table-container {
  margin-top: 20px;
}
.table-body {
  display: flex;
  flex-direction: column;
  align-items: center;
  transform: translateX(-90px);
  user-select: text;
  overflow-y: auto;
  margin-bottom: 10vh;
}
table {
  min-width: 500px;
  border-collapse: collapse;
  table-layout: fixed;
  width: 100%;
}
th,
td {
  padding: 4px;
}
th {
  background-color: var(--primary-color);
  border: none;
  position: sticky;
  top: 0;
}
.tdsong {
  width: 80%;
  max-width: 120px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.tdalbumname {
  width: 50%;
  max-width: 120px;
  white-space: nowrap;
  overflow: visible;
  text-overflow: ellipsis;
}
.tdartist {
  width: 50%;
  max-width: 120px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.tddate {
  width: 100px;
  min-width: 100px;
  display: flex;
  justify-content: flex-end;
  margin-right: 10px;
}
.emptynotice {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  color: var(--dull-color);
}
.quickstart {
  position: relative;
  font-size: 15px;
  line-height: 22px;
  left: 80px;
  top: 50px;
  width: 400px;
}
.quickstart .title {
  font-weight: bold;
}
.aBubble {
  background-color: var(--duller-color);
  width: 70%;
  border-radius: 5px;
  margin-bottom: 3px;
  min-width: 500px;
}
.single-bubble {
  background-color: var(--primary-color);
}
.future-date {
  opacity: 50%;
}
tr.single-bubble {
  display: flex;
  justify-content: space-between;
}
.album-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 3px 0px;
}
</style>
