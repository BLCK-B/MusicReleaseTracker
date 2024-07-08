<template>
  <div class="settings">
    <button @click="clickClose()" class="crossImgButton">
      <img v-if="primaryColor !== 'Light'" class="image" src="../components/icons/crossdark.png" alt="X" />
      <img v-if="primaryColor === 'Light'" class="image" src="../components/icons/crosslight.png" alt="X" />
    </button>

    <section class="filterscont">
      <SettingsFilters :filters="filters" @set-setting="setSetting" />
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
      isoDates: false,
      filters: {
        Remix: false,
        VIP: false,
        Instrumental: false,
        Acoustic: false,
        Extended: false,
        Remaster: false,
      },
      primaryColor: "N",
      accentColor: "N",
      autoTheme: false,
    };
  },
  // on open, load setting states from HOCON
  created() {
    axios
      .get("/api/settingsOpened")
      .then((response) => {
        this.filters = response.data;
        this.isoDates = response.data.isoDates;
        this.autoTheme = response.data.autoTheme;
        this.primaryColor = this.$store.getters.getPrimaryColor;
        this.accentColor = this.$store.getters.getAccentColor;
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
          this.primaryColor = value;
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
  font-family: "arial", sans-serif;
  font-size: 14px;
  user-select: none;
  background-color: var(--primary-color);
  color: var(--contrast-color);
  overflow-y: scroll;
  overflow-x: hidden;
  display: grid;
  position: fixed;
  align-content: start;
  justify-content: center;
  accent-color: var(--contrast-color);
  top: 0;
  left: 0;
  padding-left: 5px;
  padding-top: 3px;
  width: 100%;
  height: 100%;
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
  right: 55px;
  top: 6px;
  padding: 0;
  background-color: transparent;
  border: none;
  transition: 0s;
}
.crossImgButton:hover {
  opacity: 60%;
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
  justify-self: center;
  width: 280px;
  background-color: transparent;
}
</style>
