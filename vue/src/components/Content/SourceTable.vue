<template>
  <div v-if="tableVisible" class="table-container">
    <div class="table-body">
      <div v-for="(mediaItem, mediaIndex) in tableData" :key="mediaIndex" class="aBubble">
        <table>
          <tbody>
            <!-- album -->
            <template v-if="isAlbum(mediaItem)">
              <tr class="album-header">
                <td class="tdalbumname">
                  {{ mediaItem.album }}
                </td>
                <td class="tdartist"></td>
                <td class="tddate">{{ formatDate(mediaItem.date) }}</td>
              </tr>
              <tr v-for="(song, songIndex) in mediaItem.songs" :key="songIndex" class="album-bubble">
                <td class="tdsong">{{ song.name }}</td>
              </tr>
            </template>
            <!-- separate songs -->
            <template v-else>
              <tr :class="{ 'future-date': isDateInFuture(mediaItem.date) }" class="single-bubble">
                <td class="tdsong">{{ mediaItem.name }}</td>
                <td v-if="artistColumnVisible" class="tdartist">{{ mediaItem.artists }}</td>
                <td class="tddate">{{ formatDate(mediaItem.date) }}</td>
              </tr>
            </template>
          </tbody>
        </table>
      </div>
    </div>
  </div>

  <div v-if="urlExists && !tableVisible && !previewVis && sourceTab !== 'combview'" class="emptynotice">
    <p>table empty</p>
  </div>
  <div v-if="!urlExists && !tableVisible && !previewVis && sourceTab === 'combview'" class="quickstart">
    <p>
      <span class="title">Quickstart guide</span> <br />
      1. click "add artist" to insert an artist <br />
      2. select any source at the top <br />
      3. find the artist on the website, copy and paste link or ID <br />
      4. to scrape, click refresh button in the top right corner <br />
    </p>
  </div>
</template>

<script setup>
import { useStore } from "vuex";
import { onMounted, computed } from "vue";

const store = useStore();

const tableData = computed(() => store.state.tableData);
const selectedArtist = computed(() => store.state.selectedArtist);
const previewVis = computed(() => store.state.previewVis);
const isoDates = computed(() => store.state.isoDates);
const sourceTab = computed(() => store.state.sourceTab);
const urlExists = computed(() => store.state.urlExists);

onMounted(() => {
  isDateInFuture();
});

const artistColumnVisible = computed(() => {
  return !(sourceTab.value !== "combview" && selectedArtist.value !== "");
});

const tableVisible = computed(() => {
  return tableData.value.some((item) => item.song !== null);
});

const isAlbum = (mediaItem) => {
  return mediaItem.songs && mediaItem.songs.length;
};

const isDateInFuture = (dateString) => {
  return new Date(dateString) > new Date();
};

const formatDate = (dateString) => {
  if (!isoDates.value) {
    if (dateString === undefined) return dateString;
    const date = new Date(dateString);
    const day = date.getDate();
    const month = date.getMonth() + 1;
    const year = date.getFullYear();
    return `${day}. ${month}. ${year}`;
  } else return dateString;
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
  text-overflow: ellipsis;
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
  overflow: hidden;
}
.tdalbumname {
  width: 50%;
  white-space: nowrap;
  overflow: visible;
  font-weight: bold;
}
.tdartist {
  width: 60%;
  white-space: nowrap;
  overflow: hidden;
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
  width: 60%;
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
    width: 80%;
  }
  .table-body {
    transform: translateX(-40px);
  }
}
@media (max-width: 920px) {
  .aBubble {
    margin-left: 150px;
    width: 95%;
  }
  .table-body {
    transform: translateX(-80px);
  }
}
</style>
