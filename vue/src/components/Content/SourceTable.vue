<template>
  <SongDetails/>

  <div v-if="tableVisible" class="table-container">
    <div class="table-body">
      <div v-for="(mediaItem, mediaIndex) in tableDataWithThumbnails" :key="mediaIndex" class="aBubble">
        <MediaItem :mediaItem="mediaItem"/>
      </div>
    </div>
  </div>

  <div v-if="urlExists && !tableVisible && !previewVis && sourceTab !== 'combview'" class="emptynotice">
    <p>table empty</p>
  </div>
  <div v-if="!urlExists && !tableVisible && !previewVis && sourceTab === 'combview'" class="quickstart">
    <p>
      <span class="title">Quickstart guide</span> <br/>
      1. click "add artist" to insert an artist <br/>
      2. select any source at the top <br/>
      3. copy URLs <br/>
      <br/>
      This message may appear after an update.
    </p>
  </div>
</template>

<script setup lang="ts">
import {useMainStore} from "@/store/mainStore.ts";
import {computed, watch, ref} from "vue";
import MediaItem from "./MediaItem.vue";
import SongDetails from "./SongDetails.vue";
import axios from "axios";
import type {MediaItemType} from "@/types/MediaItemType.ts";
import type {SongType} from "@/types/SongType.ts";

const store = useMainStore();

const tableData = computed(() => store.tableData);
const previewVis = computed(() => store.previewVis);
const sourceTab = computed(() => store.sourceTab);
const urlExists = computed(() => store.urlExists);

const thumbnailUrls = ref<string[]>([]);

watch(tableData, () => {
  axios
      .post("/api/thumbnailUrls", getThumbnailKeys())
      .then((response) => {
        thumbnailUrls.value = response.data;
      })
      .catch((error) => {
        console.error(error);
      });
});

const tableVisible = computed(() => {
  return tableData.value.some((item: MediaItemType) => item.songs !== null);
});

const tableDataWithThumbnails = computed(() => {
  // base case for tableData thumbnailUrl watcher
  if (!thumbnailUrls.value) {
    return tableData;
  }
  return tableData.value.map((item: MediaItemType) => {
    return {
      ...item,
      songs: addThumbnailUrls(item.songs) as SongType[]
    };
  });
});

const addThumbnailUrls = (songs: SongType[]) => {
  return songs.map(song => {
    // http://localhost:57782/thumbnails/stay20240411_20250718_182058.jpg
    const key = "/thumbnails/" + (String(song.name) + String(song.date)).toLowerCase().replace(/[^a-z0-9]/g, "");
    const match = thumbnailUrls.value.find((url) => url.startsWith(key)) || null;
    const thumbnailUrl = match ? "http://localhost:57782" + match : null;
    return {
      ...song,
      thumbnailUrl
    };
  });
};

const getThumbnailKeys = () => {
  return tableData.value.flatMap((mediaItem: MediaItemType) =>
      mediaItem.songs.map(song =>
          (song.name + song.date)
              .toLowerCase()
              .replace(/[^a-z0-9]/g, "")
      )
  );
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
  position: relative;
  background-color: var(--duller-color);
  width: 60%;
  border-radius: 5px;
  margin-bottom: 6px;
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
