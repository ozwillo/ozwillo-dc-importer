import Vue from 'vue'
import App from './App.vue'
import router from './router'
import { i18n } from "@/utils/plugins/i18n"

new Vue({
  router: router,
  el: '#app',
  i18n,
  render: h => h(App)
});
