import { createRouter, createWebHistory } from 'vue-router'

import MainPage from '../pages/MainPage.vue';
import SettingsPage from '../pages/SettingsPage.vue';

const routes = [
  {
    path: '/',
    component: MainPage
  },
  {
    path: '/settings',
    component: SettingsPage
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

export default router