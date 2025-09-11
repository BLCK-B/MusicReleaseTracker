<template>
  <!-- album -->
  <template v-if="isAlbum(mediaItem)">
    <table class="album">
      <tbody>
      <tr class="album-header">
        <td class="tdalbumname">
          {{ mediaItem.album }}
        </td>
        <td class="tdartist"></td>
        <td class="tddate">{{ formatDate(mediaItem.songs[0]!.date) }}</td>
      </tr>
      </tbody>
      <tbody>
      <tr v-for="(song, songIndex) in mediaItem.songs" :key="songIndex" @mousedown="showSongCard(song)"
          class="album-bubble">
        <td>
          <img v-if="song.thumbnailUrl" :src="song.thumbnailUrl" class="thumbnail" loading="lazy"/>
          <img v-else src="../icons/noImg.png" class="no-thumbnail" loading="lazy"/>
          <span class="tdsong pad">{{ song.name }}</span>
        </td>
      </tr>
      </tbody>
    </table>
  </template>
  <!-- separate songs -->
  <template v-else>
    <tr :class="{ 'future-date': isDateInFuture(single.date) }" @mousedown="showSongCard(single)"
        class="single-bubble">
      <td>
        <img v-if="(single.thumbnailUrl)" :src="single.thumbnailUrl" class="thumbnail" loading="lazy"/>
        <img v-else src="../icons/noImg.png" class="no-thumbnail" loading="lazy"/>
      </td>
      <td class="tdsong">{{ single.name }}</td>
      <td v-if="artistColumnVisible" class="tdartist">{{ single.artists }}</td>
      <td class="tddate">{{ formatDate(single.date) }}</td>
    </tr>
  </template>
</template>

<script setup lang="ts">
import {computed} from "vue";
import {useStore} from "vuex";
import type {MediaItemType} from "@/types/MediaItemType.ts";
import type {SongType} from "@/types/SongType.ts";

const store = useStore();

const selectedArtist = computed(() => store.state.selectedArtist);
const isoDates = computed(() => store.state.isoDates);
const sourceTab = computed(() => store.state.sourceTab);

const props = defineProps<{
  mediaItem: MediaItemType
}>();

const single = computed(() => toSong(props.mediaItem));

const toSong = (mediaItem: MediaItemType): SongType => {
  return mediaItem.songs[0]!;
};

const isDateInFuture = (dateString: string) => {
  return new Date(dateString) > new Date();
};

const isAlbum = (mediaItem: MediaItemType) => {
  return mediaItem.songs && mediaItem.songs.length;
};

const artistColumnVisible = computed(() => {
  return !(sourceTab.value !== "combview" && selectedArtist.value !== "");
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

const showSongCard = (song: SongType) => {
  store.commit("SET_SELECTED_SONG_DETAILS", song);
};
</script>

<style scoped>
th,
td {
  user-select: none;
  text-overflow: ellipsis;
  display: flex;
  align-items: center;
  box-sizing: border-box;
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
  margin-left: 8px;
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

.single-bubble {
  border-radius: 5px;
  background-color: var(--primary-color);
}

.single-bubble:hover,
.album-bubble:hover {
  background-color: var(--accent-color);
  color: var(--accent-contrast);
  position: relative;
  width: 100%;
}

.future-date {
  background-color: var(--duller-color);
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
  padding: 5px 0px 5px 10px;
}

.thumbnail,
.no-thumbnail {
  width: 35px;
  aspect-ratio: 1 / 1;
  object-fit: cover;
  object-position: center;
  display: block;
  border-radius: 4px;
}

.pad {
  padding: 5px;
}

.album {
  margin-bottom: 3px;
}
</style>
