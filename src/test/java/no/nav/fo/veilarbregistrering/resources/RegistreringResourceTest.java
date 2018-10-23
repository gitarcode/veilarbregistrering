package no.nav.fo.veilarbregistrering.resources;

import no.nav.apiapp.security.PepClient;
import no.nav.fo.veilarbregistrering.domain.BrukerRegistrering;
import no.nav.fo.veilarbregistrering.domain.StartRegistreringStatus;
import no.nav.fo.veilarbregistrering.domain.besvarelse.Besvarelse;
import no.nav.fo.veilarbregistrering.domain.besvarelse.HelseHinderSvar;
import no.nav.fo.veilarbregistrering.service.ArbeidsforholdService;
import no.nav.fo.veilarbregistrering.service.BrukerRegistreringService;
import no.nav.fo.veilarbregistrering.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RegistreringResourceTest {

    private PepClient pepClient;
    private RegistreringResource registreringResource;
    private UserService userService;
    private BrukerRegistreringService brukerRegistreringService;
    private ArbeidsforholdService arbeidsforholdService;

    @BeforeEach
    public void setup() {
        pepClient = mock(PepClient.class);
        userService = mock(UserService.class);
        arbeidsforholdService = mock(ArbeidsforholdService.class);
        brukerRegistreringService = mock(BrukerRegistreringService.class);

        registreringResource = new RegistreringResource(
                pepClient,
                userService,
                arbeidsforholdService,
                brukerRegistreringService
        );
    }


    @Test
    public void skalSjekkeTilgangTilBrukerVedHentingAvSisteArbeidsforhold() {
        registreringResource.hentSisteArbeidsforhold();
        verify(pepClient, times(1)).sjekkLeseTilgangTilFnr(any());
    }

    @Test
    public void skalSjekkeTilgangTilBrukerVedHentingAvStartRegistreringsstatus() {
        when(brukerRegistreringService.hentStartRegistreringStatus(any())).thenReturn(new StartRegistreringStatus());
        registreringResource.hentStartRegistreringStatus();
        verify(pepClient, times(1)).sjekkLeseTilgangTilFnr(any());
    }

    @Test
    public void skalSjekkeTilgangTilBrukerVedHentingAvRegistrering() {
        when(brukerRegistreringService.hentStartRegistreringStatus(any())).thenReturn(new StartRegistreringStatus());
        registreringResource.hentProfilertRegistrering();
        verify(pepClient, times(1)).sjekkLeseTilgangTilFnr(any());
    }

    @Test
    public void skalSjekkeTilgangTilBrukerVedRegistreringAvBruker() {
        BrukerRegistrering brukerRegistrering = new BrukerRegistrering()
                .setEnigIOppsummering(true)
                .setBesvarelse(new Besvarelse().setHelseHinder(HelseHinderSvar.NEI));

        String ident = "10108000398"; //Aremark fiktivt fnr.";
        when(userService.getFnr()).thenReturn(ident);
        registreringResource.registrerBruker(brukerRegistrering);
        verify(pepClient, times(1)).sjekkSkriveTilgangTilFnr(any());
    }
}