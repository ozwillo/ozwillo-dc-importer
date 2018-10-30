import Vue from 'vue'
import Router from 'vue-router'
import Dashboard from './components/Dashboard'
import DemandeAcces from './components/templates/DemandeAcces'

Vue.use(Router)

export default new Router({
  mode: 'history',
  base: process.env.BASE_URL,
  routes: [
    {
      path: '/',
      name: 'dashboard',
      component: Dashboard
    },
    {
      path: '/create',
      name: 'Create new data request access',
      component: DemandeAcces
    }
  ]
})
