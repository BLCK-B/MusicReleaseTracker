<template>
  <div v-if="tableVisible" class="table-container">
    <!-- <SongDetails /> -->

    <div class="table-body">
      <div v-for="(mediaItem, mediaIndex) in tableData" :key="mediaIndex" class="aBubble">
        <table>
          <tbody>
            <MediaItem :mediaItem="mediaItem" />
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
import { computed } from "vue";
import MediaItem from "./MediaItem.vue";
import SongDetails from "./SongDetails.vue";

const store = useStore();

const tableData = computed(() => store.state.tableData);
const previewVis = computed(() => store.state.previewVis);
const sourceTab = computed(() => store.state.sourceTab);
const urlExists = computed(() => store.state.urlExists);

const tableVisible = computed(() => {
  return tableData.value.some((item) => item.song !== null);
});
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
  margin-bottom: 10vh;
}
table {
  border-collapse: collapse;
  table-layout: fixed;
  width: 100%;
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
