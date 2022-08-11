package no.nav.fo.veilarbregistrering.tokenveksling

import no.nav.common.auth.context.AuthContextHolder

class TokenResolver(private val authContextHolder: AuthContextHolder) {

    fun token(): String {
        return authContextHolder.requireContext().idToken.serialize()
    }

    fun erAzureAdToken(): Boolean {
        return authContextHolder.erAADToken()
    }

    fun erTokenXToken(): Boolean {
        return authContextHolder.erTokenXToken()
    }
}

fun AuthContextHolder.erAADToken(): Boolean = hentIssuer().contains("login.microsoftonline.com")
private fun AuthContextHolder.erTokenXToken(): Boolean = hentIssuer().contains("tokendings")
private fun AuthContextHolder.hentIssuer(): String = this.requireIdTokenClaims().issuer
