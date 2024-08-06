<template>
  <div class="table-container" v-if="hideTable">
    <div class="table-header">
      <table>
        <thead>
          <tr>
            <th class="song">song</th>
            <th v-if="!hideArtistColumn" class="artist">artist</th>
            <th class="date">date</th>
          </tr>
        </thead>
      </table>
    </div>

    <div class="table-body">
      <table>
        <tbody>
          <tr
            v-for="(mediaItem, mediaIndex) in processedTableData"
            :key="mediaIndex"
            :class="{
              'album-header': mediaItem.isAlbumHeader,
              'album-song': mediaItem.isAlbumSong,
            }">
            <td class="tdsong">{{ mediaItem.name }}</td>
            <td class="tdartist" v-if="!hideArtistColumn">{{ mediaItem.artists }}</td>
            <td class="tddate">{{ mediaItem.date }}</td>
          </tr>
        </tbody>
      </table>
    </div>

    <div class="emptynotice" v-if="urlExists && !hideTable && !previewVis && sourceTab !== 'combview'">
      <p>table empty</p>
    </div>
    <div class="quickstart" v-if="!urlExists && !hideTable && !previewVis && sourceTab === 'combview'">
      <p>
        <span class="title">Quickstart guide</span> <br />
        1. click "add artist" to insert an artist <br />
        2. click on any BP / MB / JD / YT button at the top to select a source <br />
        3. find the artist on the website, copy & paste the link or ID <br />
        4. to scrape, click refresh button in the top right corner <br />
      </p>
    </div>
  </div>
</template>

<script>
import { mapState } from "vuex";

export default {
  mounted() {
    this.isDateInFuture;
  },
  computed: {
    ...mapState(["tableData", "previewVis", "artist", "isoDates", "sourceTab", "urlExists"]),
    processedTableData() {
      return this.tableData
        .map((item) => {
          if (item.album !== null) {
            return [
              {
                isAlbumHeader: true,
                name: item.album,
                artists: item.songs[0].artists,
                date: item.date,
              },
              ...item.songs.map((song) => ({
                isAlbumSong: true,
                name: song.name,
              })),
            ];
          } else {
            return {
              album: false,
              name: item.name,
              artists: item.artists,
              date: item.date,
            };
          }
        })
        .flat();
    },
    hideArtistColumn() {
      return this.tableData.some((item) => item.artist === null);
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
.table-header {
  flex-shrink: 0;
  overflow: hidden;
  z-index: 3;
  position: fixed;
  width: calc(100% - 170px);
}
.table-body {
  flex-grow: 1;
  overflow-y: auto;
  user-select: text;
  margin-bottom: 10vh;
}
table {
  width: 100%;
  min-width: 500px;
  border-collapse: collapse;
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
.song,
.tdsong {
  width: 50%;
  max-width: 120px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.artist,
.tdartist {
  width: 50%;
  max-width: 120px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.date,
.tddate {
  width: 100px;
  min-width: 100px;
}
.future-date {
  opacity: 40%;
}
.emptynotice {
  position: absolute;
  left: 40%;
  top: 40%;
  color: var(--dull-color);
}
.quickstart {
  position: relative;
  font-size: 15px;
  line-height: 22px;
  left: 5%;
  top: 5%;
}
.quickstart .title {
  font-weight: bold;
}
.tddate {
  display: flex;
  justify-content: flex-end;
  margin-right: 30px;
}
.album-header {
  background-color: var(--duller-color);
}
.album-song {
  border-left: 30px solid var(--primary-color);
}
</style>
