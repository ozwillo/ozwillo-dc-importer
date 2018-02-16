package org.ozwillo.dcimporter.service

import org.oasis_eu.spring.kernel.security.OpenIdCService
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service

@Service
class SystemUserService(private val openIdCService: OpenIdCService) {

    @Value("\${datacore.systemAdminUser.refreshToken:GET IT USING NODE LIB}")
    private lateinit var refreshToken: String

    @Value("\${datacore.systemAdminUser.nonce:SET WHEN GETTING REFRESH TOKEN}")
    private lateinit var refreshTokenNonce: String

    @Value("\${kernel.auth.callback_uri:\${application.url}/callback}")
    private lateinit var callbackUri: String

    fun runAs(runnable: Runnable) {
        val endUserAuth = SecurityContextHolder.getContext().authentication
        SecurityContextHolder.getContext().authentication = null // or UnauthAuth ?? anyway avoid to do next queries to Kernel with user auth
        try {
            loginAs()
            runnable.run()
        } finally {
            SecurityContextHolder.getContext().authentication = endUserAuth
        }
    }

    private fun loginAs() {
        /* 1. do query to Kernel "exchange refresh token for access token" (similar to "exchange code for acces token")
                - see https://tools.ietf.org/html/rfc6749#section-6
           2. wrap token in a Spring Authentication impl */

        // Refresh_Token rather than Code
        // NB. In the Kernel auth spec says that the SCOPE is required, but actually if the scope is not added in the refresh token,
        // the original scope is taken by default. If the scope has new elements, then it will throw an exception. So is better not send it.
        val authentication = openIdCService.processAuthentication(null, refreshToken.trim { it <= ' ' }, null, null, refreshTokenNonce.trim { it <= ' ' }, callbackUri.trim { it <= ' ' })
        // set it as authenticated user for current context:
        SecurityContextHolder.getContext().authentication = authentication
    }
}