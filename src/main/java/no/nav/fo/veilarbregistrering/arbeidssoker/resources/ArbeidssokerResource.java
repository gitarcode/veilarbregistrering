package no.nav.fo.veilarbregistrering.arbeidssoker.resources;

import no.nav.apiapp.security.veilarbabac.VeilarbAbacPepClient;
import no.nav.fo.veilarbregistrering.arbeidssoker.ArbeidssokerService;
import no.nav.fo.veilarbregistrering.arbeidssoker.Arbeidssokerperiode;
import no.nav.fo.veilarbregistrering.bruker.Bruker;
import no.nav.fo.veilarbregistrering.bruker.BrukerAdapter;
import no.nav.fo.veilarbregistrering.bruker.Periode;
import no.nav.fo.veilarbregistrering.bruker.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;

@Component
@Path("/arbeidssoker")
@Produces("application/json")
public class ArbeidssokerResource implements ArbeidssokerApi {

    private static final Logger LOG = LoggerFactory.getLogger(ArbeidssokerResource.class);

    private final ArbeidssokerService arbeidssokerService;
    private final UserService userService;
    private final VeilarbAbacPepClient pepClient;

    public ArbeidssokerResource(ArbeidssokerService arbeidssokerService, UserService userService, VeilarbAbacPepClient pepClient) {
        this.arbeidssokerService = arbeidssokerService;
        this.userService = userService;
        this.pepClient = pepClient;
    }

    @GET
    @Path("/perioder")
    @Override
    public ArbeidssokerperioderDto hentArbeidssokerperioder(
            @QueryParam("fnr") String fnr,
            @QueryParam("fraOgMed") LocalDate fraOgMed,
            @QueryParam("tilOgMed") LocalDate tilOgMed
    ) {
        Bruker bruker = userService.hentBruker();

        pepClient.sjekkLesetilgangTilBruker(BrukerAdapter.map(bruker));

        List<Arbeidssokerperiode> arbeidssokerperiodes = arbeidssokerService.hentArbeidssokerperioder(
                bruker.getFoedselsnummer(), Periode.gyldigPeriode(fraOgMed, tilOgMed));

        LOG.info(String.format("Ferdig med henting av arbeidssokerperioder - fant %s perioder", arbeidssokerperiodes.size()));

        return map(arbeidssokerperiodes);
    }

    private ArbeidssokerperioderDto map(List<Arbeidssokerperiode> arbeidssokerperioder) {
        List<ArbeidssokerperiodeDto> arbeidssokerperiodeDtoer = arbeidssokerperioder.stream()
                .map(periode -> new ArbeidssokerperiodeDto(
                        periode.getPeriode().getFra().toString(),
                        ofNullable(periode.getPeriode().getTil())
                                .map(LocalDate::toString)
                                .orElse(null)))
                .collect(Collectors.toList());

        return new ArbeidssokerperioderDto(arbeidssokerperiodeDtoer);
    }

}
