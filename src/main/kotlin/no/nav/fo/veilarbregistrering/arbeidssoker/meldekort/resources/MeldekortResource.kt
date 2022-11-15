package no.nav.fo.veilarbregistrering.arbeidssoker.meldekort.resources

import no.nav.fo.veilarbregistrering.arbeidssoker.meldekort.MeldekortService
import no.nav.fo.veilarbregistrering.autorisasjon.TilgangskontrollService
import no.nav.fo.veilarbregistrering.bruker.UserService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/arbeidssoker")
class MeldekortResource(
    private val userService: UserService,
    private val tilgangskontrollService: TilgangskontrollService,
    private val meldekortService: MeldekortService
) : MeldekortApi {
    @GetMapping("/meldekort")
    override fun hentMeldekort(): List<MeldekortDto> {
        val bruker = userService.finnBrukerGjennomPdl()
        tilgangskontrollService.sjekkLesetilgangTilBruker(bruker.gjeldendeFoedselsnummer)
        return meldekortService.hentMeldekort(bruker.gjeldendeFoedselsnummer)
            .sortedByDescending { it.nåværendePeriode.periodeFra }
            .map(MeldekortDto::map)
    }

    @GetMapping("/meldekort/siste")
    override fun hentSisteMeldekort(): MeldekortDto? {
        val bruker = userService.finnBrukerGjennomPdl()
        tilgangskontrollService.sjekkLesetilgangTilBruker(bruker.gjeldendeFoedselsnummer)
        return meldekortService.hentMeldekort(bruker.gjeldendeFoedselsnummer)
            .sortedByDescending { it.eventOpprettet }
            .map(MeldekortDto::map)
            .firstOrNull()
    }
}
