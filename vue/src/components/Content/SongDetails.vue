<template>
  <div v-if="selected">
    <div class="overlay" @mousedown="closeCard"></div>
    <PopoverCard :cardSize="'l'" class="details-card">
      <div class="bg-helper">
        <div
          class="background-blur"
          :style="{
            backgroundImage: selected.thumbnailUrl ? `url(${selected.thumbnailUrl})` : 'url(../icons/noImg.png)',
          }"></div>

        <div class="content">
          <img v-if="selected.thumbnailUrl" :src="selected.thumbnailUrl" class="thumbnail" loading="lazy" />
          <img v-else src="../icons/noImg.png" class="thumbnail" loading="lazy" />

          <div class="info-text">
            <div class="song-name">{{ selected.name }}</div>
            <div class="artists">{{ selected.artists }}</div>
            <div class="date">Released: {{ formatDate(selected.date) }}</div>
          </div>
        </div>
        <div class="search-links">
          <a :href="searchLinks.spotify" target="_blank" class="link">Spotify</a>
          <a :href="searchLinks.applemusic" target="_blank" class="link">Apple Music</a>
          <a :href="searchLinks.soundcloud" target="_blank" class="link">SoundCloud</a>
          <a :href="searchLinks.bandcamp" target="_blank" class="link">Bandcamp</a>
          <a :href="searchLinks.youtube" target="_blank" class="link">YouTube</a>
          <a :href="searchLinks.youtubemusic" target="_blank" class="link">YouTube Music</a>
          <a :href="searchLinks.google" target="_blank" class="link">Google</a>
        </div>
      </div>
    </PopoverCard>
  </div>
</template>

<script setup lang="ts">
import { computed } from "vue";
import { useMainStore } from "@/store/mainStore.ts";
import PopoverCard from "../Util/PopoverCard.vue";

const store = useMainStore();

const selected = computed(() => store.selectedSongDetails);
const isoDates = computed(() => store.isoDates);

const searchLinks = computed(() => {
  const term = encodeURIComponent(`${selected.value.artists} ${selected.value.name}`);
  return {
    spotify: `https://open.spotify.com/search/${term}`,
    youtube: `https://www.youtube.com/results?search_query=${term}`,
    youtubemusic: `https://music.youtube.com/search?q=${term}`,
    soundcloud: `https://soundcloud.com/search?q=${term}`,
    bandcamp: `https://bandcamp.com/search?q=${term}`,
    applemusic: `https://music.apple.com/us/search?term=${term}`,
    google: `https://www.google.com/search?q=${term}`,
  };
});

const formatDate = (dateString: string) => {
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
  store.setSelectedSongDetails(null);
};
</script>

<style scoped>
.details-card {
  padding: 20px;
  z-index: 3;
}

.bg-helper {
  position: relative;
  min-height: 100%;
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
  filter: blur(4px);
}

.content {
  display: flex;
  gap: 20px;
  align-items: center;
  position: relative;
}

.thumbnail {
  width: 150px;
  aspect-ratio: 1 / 1;
  object-fit: cover;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
}

.info-text {
  display: flex;
  flex-direction: column;
  gap: 10px;
  user-select: text;
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

.search-links {
  display: flex;
  gap: 8px;
  margin-top: 40px;
  flex-wrap: wrap;
  font-size: 14px;
  position: relative;
}

.search-links .link {
  text-decoration: none;
  padding: 6px 12px;
  border-radius: 8px;
  color: var(--contrast-color);
  background-color: var(--duller-color);
  border: 2px solid var(--dull-color);
}
</style>
