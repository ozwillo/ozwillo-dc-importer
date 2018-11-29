import Vue from 'vue'
import Router from 'vue-router'
import Dashboard from './views/Dashboard'
import DataRequest from './views/DataRequest'
import CheckDataAccess from './views/CheckDataAccess'

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
            path: '/request/:id/check',
            name: 'check',
            component: CheckDataAccess
        }
    ]
})
