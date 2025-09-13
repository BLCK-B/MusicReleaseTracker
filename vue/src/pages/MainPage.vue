<template>
  <div class="app">
    <!-- sidebar -->
    <div class="list">
      <ArtistList />
    </div>

    <div class="maincontent">
      <!-- top bar -->
      <div v-if="!previewVis" class="topbar">
        <SourceMenu />
      </div>

      <!-- content -->
      <div class="sourcetable">
        <SourceTable />
      </div>

      <div class="dialogsurl" v-if="!previewVis">
        <DialogsURL />
      </div>
    </div>

    <div class="progressbar">
      <ProgressBar />
    </div>
  </div>
</template>

<script setup lang="ts">
import ArtistList from "@/components/Artists/ArtistList.vue";
import SourceMenu from "@/components/Bar/SourceMenu.vue";
import SourceTable from "@/components/Content/SourceTable.vue";
import DialogsURL from "@/components/Content/DialogsURL.vue";
import ProgressBar from "@/components/Content/ProgressBar.vue";
import { computed } from "vue";
import { useMainStore } from "@/store/mainStore.ts";

const store = useMainStore();

const previewVis = computed(() => store.previewVis);
</script>

<style scoped>
* {
  scrollbar-color: var(--dull-color) transparent;
}
*::-webkit-scrollbar-thumb {
  background-color: var(--dull-color);
}
*::-webkit-scrollbar {
  width: 8px;
  background: transparent;
}
.app {
  display: flex;
  position: fixed;
  transition: 0.15s;
  width: 100%;
  height: 100%;
}
.list {
  width: 175px;
  min-width: 175px;
  padding-top: 5px;
  padding-left: 2px;
  top: -3px;
  left: -5px;
  position: relative;
  background-color: var(--subtle-color);
}
.maincontent {
  flex-grow: 1;
  display: flex;
  flex-direction: column;
}
.topbar {
  left: 5px;
  position: relative;
}
.dialogsurl {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
}
.sourcetable {
  flex-grow: 1;
  margin-top: 6px;
  overflow-y: auto;
  margin-right: 4px;
}
.progressbar {
  position: absolute;
  bottom: 3px;
  left: 0;
  z-index: 5;
  width: 100%;
}
</style>
