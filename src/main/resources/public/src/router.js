import Vue from 'vue'
import Router from 'vue-router'
import Dashboard from './views/Dashboard'
import DataRequest from './views/DataRequest'
import CheckDataAccess from './views/CheckDataAccess'
import ConnectorsManagement from './views/ConnectorsManagement'
import ProcessingStat from './views/ProcessingStat'
import ProcessingStatByModel from './views/ProcessingStatByModel'
import ProcessingStatByOrganization from './views/ProcessingStatByOrganization'

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
        },
        {
            path: '/connectors-management',
            name: 'connectors',
            component: ConnectorsManagement
        },
        {
            path: '/stat-view',
            name: 'stat',
            component: ProcessingStat
        },
        {
            path: '/stat-view/models',
            name: 'statByModel',
            component: ProcessingStatByModel
        },
        {
            path: '/stat-view/organizations',
            name: 'statByOrganization',
            component: ProcessingStatByOrganization
        }
    ]
})