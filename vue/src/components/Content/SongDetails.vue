<template>
  <div v-if="selected">
    <div class="overlay" @click="closeCard"></div>
    <Card :cardSize="'l'" class="details-card">
      <div class="inner-wrapper">
        <div
          class="background-blur"
          :style="{ backgroundImage: selected.thumbnailUrl ? `url(${selected.thumbnailUrl})` : 'url(../icons/noImg.png)' }"></div>

        <div class="content-wrapper">
          <img v-if="selected.thumbnailUrl" :src="selected.thumbnailUrl" class="thumbnail" loading="lazy" />
          <img v-else src="../icons/noImg.png" class="thumbnail" loading="lazy" />

          <div class="info-wrapper">
            <div class="song-name">{{ selected.name }}</div>
            <div class="artists">{{ selected.artists }}</div>
            <div class="date">Released: {{ formatDate(selected.date) }}</div>
            <div class="scrape-link">{{ scrapeLink }}</div>
          </div>
        </div>
      </div>
    </Card>
  </div>
</template>

<script setup>
import { computed } from "vue";
import { useStore } from "vuex";
import Card from "../Util/Card.vue";

const store = useStore();

const selected = computed(() => store.state.selectedSongDetails);
const isoDates = computed(() => store.state.isoDates);

const scrapeLink = "url";

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

const closeCard = () => {
  store.commit("SET_SELECTED_SONG_DETAILS", undefined);
};
</script>

<style scoped>
.details-card {
  padding: 20px;
  z-index: 3;
}

.inner-wrapper {
  position: relative;
  width: 100%;
  height: 100%;
  min-height: 100%;
  display: block;
}

.background-blur {
  position: absolute;
  top: -20px;
  left: -20px;
  width: calc(100% + 40px);
  height: calc(100% + 40px);
  background-size: cover;
  background-position: center;
  opacity: 0.2;
  filter: blur(6px);
}

.content-wrapper {
  display: flex;
  gap: 20px;
  align-items: center;
  position: relative;
  z-index: 1;
}

.thumbnail {
  width: 150px;
  aspect-ratio: 1 / 1;
  object-fit: cover;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
}

.info-wrapper {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.song-name {
  font-size: 22px;
  font-weight: bold;
}

.artists {
  font-size: 18px;
  font-weight: bold;
}

.date {
  font-size: 18px;
}

.overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.4);
  z-index: 2;
}
</style>
