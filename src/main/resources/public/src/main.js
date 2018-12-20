import Vue from 'vue'
import App from './App.vue'
import router from './router'
import 'bootstrap'

new Vue({
    router: router,
    el: '#app',
    render: h => h(App)
});