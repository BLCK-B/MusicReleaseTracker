<template>
  <!-- album -->
  <template v-if="isAlbum(mediaItem)">
    <tr class="album-header">
      <td class="tdalbumname">
        {{ mediaItem.album }}
      </td>
      <td class="tdartist"></td>
      <td class="tddate">{{ formatDate(mediaItem.date) }}</td>
    </tr>
    <tr v-for="(song, songIndex) in mediaItem.songs" :key="songIndex" @click="contextMenu(song)" class="album-bubble">
      <td class="tdsong">{{ song.name }}</td>
    </tr>
  </template>
  <!-- separate songs -->
  <template v-else>
    <tr :class="{ 'future-date': isDateInFuture(mediaItem.date) }" @click="contextMenu(mediaItem)" class="single-bubble">
      <td>
        <img :src="mediaItem.thumbnailUrl" class="thumbnail" loading="lazy" />
      </td>
      <td class="tdsong">{{ mediaItem.name }}</td>
      <td v-if="artistColumnVisible" class="tdartist">{{ mediaItem.artists }}</td>
      <td class="tddate">{{ formatDate(mediaItem.date) }}</td>
    </tr>
  </template>
</template>

<script setup>
import { computed, onMounted } from "vue";
import { useStore } from "vuex";

const store = useStore();

const selectedArtist = computed(() => store.state.selectedArtist);
const isoDates = computed(() => store.state.isoDates);
const sourceTab = computed(() => store.state.sourceTab);

defineProps({
  mediaItem: [],
});

onMounted(() => {
  isDateInFuture();
});

const isDateInFuture = (dateString) => {
  return new Date(dateString) > new Date();
};

const isAlbum = (mediaItem) => {
  return mediaItem.songs && mediaItem.songs.length;
};

const artistColumnVisible = computed(() => {
  return !(sourceTab.value !== "combview" && selectedArtist.value !== "");
});

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

const contextMenu = (mediaItem) => {
  console.log(mediaItem.name);
};
</script>

<style scoped>
th,
td {
  user-select: text;
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
/* .single-bubble:hover,
.album-bubble:hover {
  background-color: var(--accent-color);
  color: var(--accent-contrast);
} */
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
  padding: 3px 0px;
}
.thumbnail {
  width: 35px;
  aspect-ratio: 1 / 1;
  object-fit: cover;
  object-position: center;
  display: block;
  border: 1px transparent;
  border-radius: 4px;
  border: 1px solid var(--primary-color);
  transition: 0.3s;
}
.thumbnail:hover {
  width: 80px;
}
</style>
