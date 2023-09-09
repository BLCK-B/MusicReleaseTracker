//import App from 'vue';
import 'vuetify/dist/vuetify.min.css'
import { createVuetify } from 'vuetify'
import { createApp } from 'vue'
import App from './App.vue'
import store from './store'

const vuetify = createVuetify()

const app = createApp(App)
app.use(vuetify)
app.use(store)
app.mount('#app')


