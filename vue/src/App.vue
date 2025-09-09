<template>
  <RouterView />
</template>

<script setup lang="ts">
import axios from "axios";
import { ref, computed, watch, onMounted } from "vue";
import { useStore } from "vuex";
axios.defaults.baseURL = "http://localhost:57782";

const store = useStore();

const appliedStyles = ref([]);
const theme = ref("");
const accent = ref("");

const primaryColor = computed(() => store.state.primaryColor);
const accentColor = computed(() => store.state.accentColor);

watch(primaryColor, (newTheme) => {
  theme.value = newTheme;
  applyTheme(theme.value, accent.value);
});

watch(accentColor, (newAccent) => {
  accent.value = newAccent;
  applyTheme(theme.value, accent.value);
});

onMounted(() => {
  loadTheme();
  detectTheme();
});

const loadTheme = () => {
  // on start, load themes from config
  axios
    .get("/api/themeConfig")
    .then((response) => {
      store.commit("SET_PRIMARY_COLOR", response.data.theme);
      store.commit("SET_ACCENT_COLOR", response.data.accent);
    })
    .catch((error) => {
      console.error(error);
    });
};

const detectTheme = () => {
  // detecting system theme on load
  axios
    .get("/api/settingsData")
    .then((response) => {
      const prefersDarkMode = window.matchMedia("(prefers-color-scheme: dark)");
      if (response.data.autoTheme === "true") {
        if (prefersDarkMode.matches) store.commit("SET_PRIMARY_COLOR", "dark");
        else store.commit("SET_PRIMARY_COLOR", "light");
      }
    })
    .catch((error) => {
      console.error(error);
    });
};

const applyTheme = (newTheme, newAccent) => {
  theme.value = newTheme.toLowerCase();
  accent.value = newAccent.toLowerCase();
  // remove previously applied css
  appliedStyles.value.forEach((style) => {
    style.remove();
  });
  appliedStyles.value = [];
  let themePath;
  let linkElement;

  if (newTheme !== "") {
    themePath = `./theme-${newTheme}.css`;
    linkElement = document.createElement("link");
    linkElement.rel = "stylesheet";
    linkElement.href = themePath;
    document.head.appendChild(linkElement);
    appliedStyles.value.push(linkElement);
  }
  if (newAccent !== "") {
    themePath = `./accent-${newAccent}.css`;
    linkElement = document.createElement("link");
    linkElement.rel = "stylesheet";
    linkElement.href = themePath;
    document.head.appendChild(linkElement);
    appliedStyles.value.push(linkElement);
  }
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
