<template>
    <nav id="navbar" class="navbar navbar-expand-lg navbar-light">
        <router-link to="/" class="navbar-brand">
            <img src="../assets/logo-ozwillo.png" width="30" height="30" class="d-inline-block align-top" alt="logo_ozwillo" />
            <span id="app-name" class="ml-sm-1">DC Importer</span>
        </router-link>
        <Button
            className="navbar-toggler"
            data-toggle="collapse"
            data-target="#navbarSupportedContent"
            aria-expanded="false"
            aria-label="Toggle navigation"
            aria-controls="navbarSupportedContent">
            <span class="navbar-toggler-icon"/>
        </Button>
        <div class="collapse navbar-collapse" id="navbarSupportedContent">
            <ul class="navbar-nav mr-auto">
                <li class="nav-item active">
                    <router-link class="nav-link" to="/" style="color:#fff">
                        Dashboard
                        <span class="sr-only">(current)</span>
                    </router-link>
                </li>
                <li class="nav-item">
                    <router-link class="nav-link" to="/request" style="color:#fff">
                      Data request
                      <span class="sr-only">(current)</span>
                    </router-link>
                </li>
                <li class="nav-item">
                    <router-link class="nav-link" to="/tovalidate" style="color:#fff">
                      Validation
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
    import Button from './atoms/Button.vue'

    export default {
        name: "NavBar",
        components: {Button}
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
