<template>

  <RouterView/>
  
</template>

<script>
import MainPage from './pages/MainPage.vue';
import SettingsPage from './pages/SettingsPage.vue';

import axios from 'axios';
axios.defaults.baseURL = 'http://localhost:57782';

const routes = [
  { path: '/', component: MainPage },
  { path: '/settings', component: SettingsPage },
]

export default {
  data() {
      return {
      };
  },
  components: {
    MainPage,
    SettingsPage,
  },
  created() {
  },
  methods: {
  },
    
};
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

</style>

