import Vue from 'vue'
import Router from 'vue-router'
import Dashboard from './views/Dashboard'
import DataRequest from './views/DataRequest'
import Validation from './views/Validation'

Vue.use(Router)

export default new Router({
    mode: 'history',
    base: process.env.BASE_URL,
    routes: [{
            path: '/',
            name: 'dashboard',
            component: Dashboard
        },
        {
          path: '/request/:id?',
          name: 'request',
          component: DataRequest
        },
        {
            path: '/tovalidate',
            name: 'validation',
            component: Validation
        }
    ]
})
