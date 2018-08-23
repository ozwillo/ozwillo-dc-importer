package org.ozwillo.dcimporter.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder

@EnableWebFluxSecurity
class WebSecurityConfig{

    @Value("\${security.basicAuth.connexion.user}")
    private val user = ""
    @Value("\${security.basicAuth.connexion.password}")
    private val password = ""
    @Value("\${security.basicAuth.connexion.role}")
    private val role = ""

    @Bean
    fun securityWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain{

        return http

        .authorizeExchange().pathMatchers( "/api/**").permitAll()
                .and()
                .authorizeExchange().anyExchange().authenticated()
                .and()
                .httpBasic()
                .and()
                .csrf().disable()
                .build()
    }

    @Bean
    fun userDetailsService(): MapReactiveUserDetailsService{
        val user: UserDetails = User
                .withUsername(user)
                .password(passwordEncoder().encode(password))
                .roles(role)
                .build()
        return MapReactiveUserDetailsService(user)
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

}