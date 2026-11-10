import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router.js'
import './style.css'
import './playful.css'
import './experience.css'

createApp(App).use(createPinia()).use(router).mount('#app')
