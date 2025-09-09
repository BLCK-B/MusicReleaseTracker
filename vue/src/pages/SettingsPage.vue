<template>
  <div class="settings">
    <button @click="clickClose()" class="crossImgButton">
      <img v-if="primaryColor !== 'light'" class="image" src="../components/icons/crossdark.png" alt="X" />
      <img v-if="primaryColor === 'light'" class="image" src="../components/icons/crosslight.png" alt="X" />
    </button>
    <div class="version">MRT v{{ appVersion }}</div>

    <section class="filterscont">
      <SettingsFilters
        :filterRemix="filterRemix"
        :filterVIP="filterVIP"
        :filterInstrumental="filterInstrumental"
        :filterAcoustic="filterAcoustic"
        :filterExtended="filterExtended"
        :filterRemaster="filterRemaster"
        @set-setting="setSetting" />
    </section>

    <section class="appearance">
      <SettingsAppearance
        :autoTheme="autoTheme"
        @set-setting="setSetting"
        :primaryColor="primaryColor"
        :accentColor="accentColor" />
    </section>

    <section class="other">
      <SettingsOther :isoDates="isoDates" :loadThumbnails="loadThumbnails" @set-setting="setSetting" />
    </section>

    <section class="danger">
      <SettingsDangerZone />
    </section>

    <section class="self">
      <SettingsSelf />
    </section>
  </div>
</template>

<script setup lang="ts">
import axios from "axios";
import { useRouter } from "vue-router";
import { ref, computed, onBeforeMount } from "vue";
import { useStore } from "vuex";
import SettingsOther from "../components/Settings/SettingsOther.vue";
import SettingsDangerZone from "../components/Settings/SettingsDangerZone.vue";
import SettingsFilters from "../components/Settings/SettingsFilters.vue";
import SettingsAppearance from "../components/Settings/SettingsAppearance.vue";
import SettingsSelf from "../components/Settings/SettingsSelf.vue";

const router = useRouter();
const store = useStore();

const filterRemix = ref(false);
const filterVIP = ref(false);
const filterInstrumental = ref(false);
const filterAcoustic = ref(false);
const filterExtended = ref(false);
const filterRemaster = ref(false);
const accentColor = ref("N");
const autoTheme = ref(false);
const appVersion = ref("");
const loadThumbnails = ref(false);

const primaryColor = computed(() => store.state.primaryColor);
const isoDates = computed(() => store.state.isoDates);

onBeforeMount(() => {
  axios
    .get("/api/settingsData")
    .then((response) => {
      filterRemix.value = response.data.filterRemix === "true";
      filterVIP.value = response.data.filterVIP === "true";
      filterInstrumental.value = response.data.filterInstrumental === "true";
      filterAcoustic.value = response.data.filterAcoustic === "true";
      filterExtended.value = response.data.filterExtended === "true";
      filterRemaster.value = response.data.filterRemaster === "true";
      autoTheme.value = response.data.autoTheme === "true";
      store.commit("SET_ISODATES", response.data.isoDates === "true");
      accentColor.value = response.data.accent;
      loadThumbnails.value = response.data.loadThumbnails === "true";
    })
    .catch((error) => {
      console.error(error);
    });
  axios
    .get("/api/appVersion")
    .then((response) => {
      appVersion.value = response.data;
    })
    .catch((error) => {
      console.error(error);
    });
});

// close settings, trigger rebuild combview in app
const clickClose = () => {
  store.commit("SET_SETTINGS_OPEN", false);
  router.push("/");
};

// write single setting in config
const setSetting = (name: string, value: any) => {
  switch (name) {
    case "theme":
      store.commit("SET_PRIMARY_COLOR", value);
      break;
    case "accent":
      store.commit("SET_ACCENT_COLOR", value);
      accentColor.value = value;
      break;
    case "isoDates":
      store.commit("SET_ISODATES", value);
      break;
    case "autoTheme":
      autoTheme.value = value;
      break;
    case "loadThumbnails":
      loadThumbnails.value = value;
      break;
  }
  axios.put(`/api/setting?name=${name}&value=${value}`).catch((error) => {
    console.error(error);
  });
};
</script>

<style scoped>
.settings {
  overflow-y: scroll;
  overflow-x: hidden;
  display: grid;
  position: fixed;
  align-content: start;
  justify-content: center;
  accent-color: var(--contrast-color);
}
@media screen and (min-width: 1050px) {
  .settings {
    display: grid;
    grid-template-columns: repeat(2, 0fr);
  }
  section {
    margin-right: 90px;
  }
}
.image {
  height: 33px;
}
.crossImgButton {
  position: absolute;
  right: 41px;
  top: 6px;
  padding: 0;
  background-color: transparent;
  border: none;
  transition: 0s;
}
.crossImgButton:hover {
  opacity: 60%;
}
.version {
  position: absolute;
  left: 10px;
  top: 8px;
  font-weight: bold;
}
section {
  position: relative;
  margin-top: 20px;
  left: 40px;
  padding: 1px 15px 10px 15px;
  background-color: var(--duller-color);
  border-radius: 5px;
  transition: 0.15s;
  width: 345px;
}
.self {
  background-color: transparent;
}
</style>
