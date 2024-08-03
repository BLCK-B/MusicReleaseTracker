<template>
  <RouterView />
</template>

<script>
import axios from "axios";
import { mapState } from "vuex";
axios.defaults.baseURL = "http://localhost:57782";

export default {
  data() {
    return {
      appliedStyles: [],
      theme: "",
      accent: "",
    };
  },
  computed: {
    ...mapState(["primaryColor", "accentColor"]),
  },
  watch: {
    // load themes whenever store theme/accent change
    primaryColor(theme) {
      this.theme = theme;
      this.applyTheme(theme, this.accent);
    },
    accentColor(accent) {
      this.accent = accent;
      this.applyTheme(this.theme, accent);
    },
  },
  created() {
    this.loadTheme();
    this.detectTheme();
  },
  methods: {
    loadTheme() {
      // on start, load themes from config
      axios
        .get("/api/getThemeConfig")
        .then((response) => {
          this.$store.commit("SET_PRIMARY_COLOR", response.data.theme);
          this.$store.commit("SET_ACCENT_COLOR", response.data.accent);
        })
        .catch((error) => {
          console.error(error);
        });
    },
    detectTheme() {
      // detecting system theme on load
      axios
        .get("/api/settingsOpened")
        .then((response) => {
          const prefersDarkMode = window.matchMedia("(prefers-color-scheme: dark)");
          if (response.data.autoTheme == true) {
            if (prefersDarkMode.matches) this.$store.commit("SET_PRIMARY_COLOR", "Black");
            else this.$store.commit("SET_PRIMARY_COLOR", "Light");
          }
        })
        .catch((error) => {
          console.error(error);
        });
    },
    applyTheme(theme, accent) {
      theme = theme.toLowerCase();
      accent = accent.toLowerCase();
      // remove previously applied css
      this.appliedStyles.forEach((style) => {
        style.remove();
      });
      this.appliedStyles = [];
      let themePath;
      let linkElement;

      if (theme !== "") {
        themePath = `./theme-${theme}.css`;
        linkElement = document.createElement("link");
        linkElement.rel = "stylesheet";
        linkElement.href = themePath;
        document.head.appendChild(linkElement);
        this.appliedStyles.push(linkElement);
      }
      if (accent !== "") {
        themePath = `./accent-${accent}.css`;
        linkElement = document.createElement("link");
        linkElement.rel = "stylesheet";
        linkElement.href = themePath;
        document.head.appendChild(linkElement);
        this.appliedStyles.push(linkElement);
      }
    },
  },
};
</script>

<style scoped>
* {
  scrollbar-color: var(--dull-color) transparent;
  font-family: "arial", sans-serif;
  font-size: 15px;
  user-select: none;
  background-color: var(--primary-color);
  color: var(--contrast-color);
  top: 0;
  left: 0;
  padding-left: 5px;
  padding-top: 3px;
  width: 100%;
  height: 100%;
}
*::-webkit-scrollbar-thumb {
  background-color: var(--dull-color);
}
*::-webkit-scrollbar {
  width: 8px;
  background: transparent;
}
</style>
