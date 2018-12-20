<template>
  <div id="app">
    <NavBar/>
    <router-view></router-view>
  </div>
</template>

<script>
import Vue from 'vue'
import  VueRouter from 'vue-router'
import NavBar from './components/NavBar'

export default {
  name: 'app',
  components: {NavBar},
  data () {
    return {
      msg: 'Welcome to Your Vue.js App'
    }
  }
}
</script>

<style lang="scss">
@import '../node_modules/bootstrap/scss/bootstrap.scss';

.data-access-validation-label{
    padding-right: 20px;
}

h1, h2 {
  font-weight: normal;
  font-variant: small-caps;
  color: #4c4c4c;
}

ul {
  list-style-type: none;
  padding: 0;
}

li {
  display: inline-block;
  margin: 0 10px;
}

a {
  color: #f69b04;
}

a:hover {
  color: #f8ba03;
}

.btn-outline-primary:disabled {
  color: #E62984;
  border-color: #E62984;
  pointer-events: none;
}
.btn-outline-primary {
  color: #E62984;
  border-color: #E62984;
}
.btn-outline-primary:hover, .btn-outline-primary:focus, .btn-outline-primary:active, .btn-outline-primary.active, .open > .dropdown-toggle.btn-primary {
	color: #FFF;
	border-color: #E62984;
  background-color: #E62984;
}
.btn-outline-primary:active, .btn-outline-primary.active {
	color: #FFF;
	border-color: #E62984;
  background-color: #E62984;
	box-shadow: none;
}
</style>
