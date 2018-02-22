package org.ozwillo.dcimporter.config

import org.oasis_eu.spring.config.OasisSecurityConfiguration
import org.oasis_eu.spring.kernel.security.OasisAuthenticationFilter
import org.oasis_eu.spring.kernel.security.OpenIdCConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.access.ExceptionTranslationFilter
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter
import org.springframework.security.web.util.matcher.AntPathRequestMatcher

import java.util.Arrays

@Configuration
class WebSecurityConfig : OasisSecurityConfiguration() {

    @Bean
    @Primary
    fun openIdCConfiguration(): OpenIdCConfiguration {
        val configuration = OpenIdConnectConfiguration()
        configuration.addSkippedPaths(Arrays.asList("/img/", "/js/", "/css/", "/status", "/api", "/build/", "/error"))
        return configuration
    }

    @Throws(Exception::class)
    override fun oasisAuthenticationFilter(): OasisAuthenticationFilter {
        return super.oasisAuthenticationFilter()
    }

    @Throws(Exception::class)
    override fun configure(http: HttpSecurity) {
        http.addFilterBefore(oasisAuthenticationFilter(), AbstractPreAuthenticatedProcessingFilter::class.java)
                .authorizeRequests()
                    .antMatchers("/api/**").permitAll().and()
                .logout()
                    .logoutRequestMatcher(AntPathRequestMatcher("/logout"))
                    .logoutSuccessHandler(logoutHandler()).and()
                .exceptionHandling()
                    .authenticationEntryPoint(authenticationEntryPoint()).and()
                .addFilterAfter(oasisExceptionTranslationFilter(authenticationEntryPoint()), ExceptionTranslationFilter::class.java)
    }
}
