package no.nav.fo.veilarbregistrering.bruker

import no.bekk.bekkopen.person.FodselsnummerValidator
import no.nav.common.auth.Constants
import no.nav.common.auth.context.AuthContextHolder
import no.nav.fo.veilarbregistrering.bruker.Bruker.Companion.of
import no.nav.fo.veilarbregistrering.bruker.feil.ManglendeBrukerInfoException
import no.nav.fo.veilarbregistrering.config.RequestContext.servletRequest
import no.nav.fo.veilarbregistrering.log.logger
import org.springframework.stereotype.Service
import java.util.*

@Service
class UserService(
    private val pdlOppslagGateway: PdlOppslagGateway,
    private val authContextHolder: AuthContextHolder,
    enableSyntetiskeFnr: Boolean = false
) {
    init {
        if (enableSyntetiskeFnr) {
            logger.warn("Enabler syntetiske fødselsnummer")
            FodselsnummerValidator.ALLOW_SYNTHETIC_NUMBERS = true;
        } else {
            logger.warn("Disabler syntetiske fødselsnummer")
            FodselsnummerValidator.ALLOW_SYNTHETIC_NUMBERS = false;
        }
    }

    fun finnBrukerGjennomPdl(): Bruker {
        return if (authContextHolder.erInternBruker() || authContextHolder.erSystemBruker()) {
            finnBrukerGjennomPdl(hentFnrFraUrl())
        } else {
            finnBrukerGjennomPdl(hentFnrFraToken())
        }
    }
    fun finnBrukerGjennomPdl(fnr: Foedselsnummer): Bruker = map(pdlOppslagGateway.hentIdenter(fnr))

    fun hentBruker(aktorId: AktorId): Bruker = map(pdlOppslagGateway.hentIdenter(aktorId))

    fun getEnhetIdFromUrlOrThrow(): String =
        servletRequest().getParameter("enhetId") ?: throw ManglendeBrukerInfoException("Mangler enhetId")

    private fun hentFnrFraUrl(): Foedselsnummer {
        val fnr: String =
            servletRequest().getParameter("fnr")
        if (!FodselsnummerValidator.isValid(fnr)) {
            throw ManglendeBrukerInfoException("Fødselsnummer hentet fra URL er ikke gyldig. Kan ikke gjøre oppslag i PDL.")
        }
        return Foedselsnummer(fnr)
    }

    private fun hentFnrFraToken(): Foedselsnummer {
        val fnr: String = if (authContextHolder.hentFnrFraPid().isPresent) {
            logger.info("Henter FNR fra PID-claim")
            authContextHolder.hentFnrFraPid()
        } else {
            logger.info("Henter FNR fra subject-claim")
            authContextHolder.hentFnrFraSubject()
        }.orElseThrow { IllegalStateException("Fant ikke FNR hverken i PID-claim eller subject i token for ekstern bruker. Kan ikke gjøre oppslag i PDL") }

        if (!FodselsnummerValidator.isValid(fnr)) {
            throw ManglendeBrukerInfoException("Fødselsnummer hentet fra token er ikke gyldig. Kan ikke gjøre oppslag i PDL.")
        }
        return Foedselsnummer(fnr)
    }

    companion object {
        private fun map(identer: Identer): Bruker {
            return of(
                identer.finnGjeldendeFnr(),
                identer.finnGjeldendeAktorId(),
                identer.finnHistoriskeFoedselsnummer()
            )
        }
    }
}

fun AuthContextHolder.hentFnrFraPid(): Optional<String> {
    return idTokenClaims.flatMap { getStringClaim(it, Constants.ID_PORTEN_PID_CLAIM) }
}

fun AuthContextHolder.hentFnrFraSubject(): Optional<String> {
    return subject
}