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
      <SettingsAppearance :autoTheme="autoTheme" @set-setting="setSetting" :primaryColor="primaryColor" :accentColor="accentColor" />
    </section>

    <section class="other">
      <SettingsOther :isoDates="isoDates" @set-setting="setSetting" />
    </section>

    <section class="danger">
      <SettingsDangerZone />
    </section>

    <section class="self">
      <SettingsSelf />
    </section>
  </div>
</template>

<script>
import axios from "axios";
import { mapState } from "vuex";
import SettingsOther from "../components/Settings/SettingsOther.vue";
import SettingsDangerZone from "../components/Settings/SettingsDangerZone.vue";
import SettingsFilters from "../components/Settings/SettingsFilters.vue";
import SettingsAppearance from "../components/Settings/SettingsAppearance.vue";
import SettingsSelf from "../components/Settings/SettingsSelf.vue";

export default {
  components: {
    SettingsOther,
    SettingsDangerZone,
    SettingsFilters,
    SettingsAppearance,
    SettingsSelf,
  },
  data() {
    return {
      filterRemix: false,
      filterVIP: false,
      filterInstrumental: false,
      filterAcoustic: false,
      filterExtended: false,
      filterRemaster: false,
      isoDates: false,
      accentColor: "N",
      autoTheme: false,
      appVersion: "",
    };
  },
  computed: {
    ...mapState(["primaryColor"]),
  },
  created() {
    axios
      .get("/api/settingsOpened")
      .then((response) => {
        this.filterRemix = response.data.filterRemix === "true";
        this.filterVIP = response.data.filterVIP === "true";
        this.filterInstrumental = response.data.filterInstrumental === "true";
        this.filterAcoustic = response.data.filterAcoustic === "true";
        this.filterExtended = response.data.filterExtended === "true";
        this.filterRemaster = response.data.filterRemaster === "true";
        this.isoDates = response.data.isoDates === "true";
        this.autoTheme = response.data.autoTheme === "true";
        if (this.autoTheme == "false") this.primaryColor = response.data.theme;
        this.accentColor = response.data.accent;
      })
      .catch((error) => {
        console.error(error);
      });
    axios
      .get("/api/getAppVersion")
      .then((response) => {
        this.appVersion = response.data;
      })
      .catch((error) => {
        console.error(error);
      });
  },
  methods: {
    // close settings, trigger rebuild combview in app
    clickClose() {
      this.$store.commit("SET_SETTINGS_OPEN", false);
      this.$router.push("/");
    },
    // write single setting in config
    setSetting(name, value) {
      switch (name) {
        case "theme":
          this.$store.commit("SET_PRIMARY_COLOR", value);
          break;
        case "accent":
          this.$store.commit("SET_ACCENT_COLOR", value);
          this.accentColor = value;
          break;
        case "isoDates":
          this.$store.commit("SET_ISODATES", value);
          break;
        case "autoTheme":
          this.autoTheme = value;
          break;
      }
      axios.post(`/api/setSetting`, { name: name, value: value }).catch((error) => {
        console.error(error);
      });
    },
  },
};
</script>

<style scoped>
* {
  transition: 0.1s;
}
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
  right: 62px;
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
