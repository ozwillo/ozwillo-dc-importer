<template>
  <nav id="navbar" class="navbar navbar-expand-lg navbar-light">
    <router-link to="/" class="navbar-brand">
      <img src="../assets/logo-ozwillo.png" width="30" height="30" class="d-inline-block align-top" alt="logo_ozwillo" />
      <span id="app-name" class="ml-sm-1">DC Importer</span>
    </router-link>
    <button class="navbar-toggler" type="button" data-toggle="collapse"
            data-target="#navbarSupportedContent" aria-controls="navbarSupportedContent"
            aria-expanded="false" aria-label="Toggle navigation">
      <span class="navbar-toggler-icon"></span>
    </button>
    <div class="collapse navbar-collapse" id="navbarSupportedContent">
      <ul class="navbar-nav mr-auto">
        <li class="nav-item active">
          <router-link class="nav-link" to="/" style="color:#fff">
            dashboard
            <span class="sr-only">(current)</span>
          </router-link>
        </li>
      </ul>
    </div>
  </nav>
</template>

<script>
    import Vue from 'vue'
    import  VueRouter from 'vue-router'
    export default {
        name: "NavBar"
    }
</script>

<style scoped  lang="scss">
  #navbar {
    background-color: #4c2d62
  }
  #app-name {
    color: white
  }
</style>
