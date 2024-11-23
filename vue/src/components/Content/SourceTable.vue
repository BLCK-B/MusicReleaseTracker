<!--
  -         MusicReleaseTracker
  -         Copyright (C) 2023 - 2024 BLCK
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
  <div v-if="hideTable" class="table-container">
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
              <tr v-for="(song, songIndex) in mediaItem.songs" :key="songIndex" class="album-bubble">
                <td class="tdsong">{{ song.name }}</td>
              </tr>
            </template>
            <template v-else>
              <tr :class="{ 'future-date': isDateInFuture(mediaItem.date) }" class="single-bubble">
                <td class="tdsong">{{ mediaItem.name }}</td>
                <td v-if="!hideArtistColumn" class="tdartist">{{ mediaItem.artists }}</td>
                <td class="tddate">{{ formatDate(mediaItem.date) }}</td>
              </tr>
            </template>
          </tbody>
        </table>
      </div>
    </div>
  </div>

  <div v-if="urlExists && !hideTable && !previewVis && sourceTab !== 'combview'" class="emptynotice">
    <p>table empty</p>
  </div>
  <div v-if="!urlExists && !hideTable && !previewVis && sourceTab === 'combview'" class="quickstart">
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
import {mapState} from "vuex";

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
  margin-bottom: 10vh;
}
table {
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
  white-space: nowrap;
  overflow: visible;
  text-overflow: ellipsis;
}
.tdalbumname {
  width: 50%;
  white-space: nowrap;
  overflow: visible;
  text-overflow: ellipsis;
}
.tdartist {
  width: 60%;
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
@media (max-width: 1100px) {
  .aBubble {
    width: 85%;
  }
  .table-body {
    transform: translateX(-30px);
  }
}
@media (max-width: 950px) {
  .aBubble {
    margin-left: 150px;
    width: 95%;
  }
  .table-body {
    transform: translateX(-80px);
  }
}
</style>
