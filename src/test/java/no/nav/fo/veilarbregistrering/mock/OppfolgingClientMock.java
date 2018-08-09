package no.nav.fo.veilarbregistrering.mock;

import no.nav.apiapp.feil.FeilDTO;
import no.nav.fo.veilarbregistrering.domain.AktiverBrukerData;
import no.nav.fo.veilarbregistrering.domain.OppfolgingStatusData;
import no.nav.fo.veilarbregistrering.httpclient.OppfolgingClient;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

public class OppfolgingClientMock extends OppfolgingClient {

    public OppfolgingClientMock() {
        super(null);
    }

    @Override
    public OppfolgingStatusData hentOppfolgingsstatus(String fnr) {
        return new OppfolgingStatusData().withUnderOppfolging(false).withKanReaktiveres(false);
    }

    @Override
    public void aktiverBruker(AktiverBrukerData fnr) {
        //sendException("BRUKER_ER_UKJENT");
        //sendException("BRUKER_KAN_IKKE_REAKTIVERES");
        //sendException("BRUKER_ER_DOD_UTVANDRET_ELLER_FORSVUNNET");
        //sendException("BRUKER_MANGLER_ARBEIDSTILLATELSE");
    }

    @Override
    public void reaktiverBruker(String fnr) {

    }

    private void sendException(String feilType) {
        FeilDTO feilDTO = new FeilDTO("1", feilType, new FeilDTO.Detaljer(feilType, "", ""));
        throw new WebApplicationException(Response.serverError().entity(feilDTO).build());
    }

}
