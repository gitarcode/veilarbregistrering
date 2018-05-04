package no.nav.fo.veilarbregistrering.service;


import no.nav.fo.veilarbregistrering.domain.Arbeidsforhold;
import no.nav.fo.veilarbregistrering.domain.OppfolgingStatus;
import no.nav.fo.veilarbregistrering.utils.FnrUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import static java.lang.Integer.getInteger;
import static java.util.Objects.nonNull;
import static no.nav.fo.veilarbregistrering.utils.ArbeidsforholdUtils.oppfyllerKravOmArbeidserfaring;
import static no.nav.fo.veilarbregistrering.utils.DateUtils.erDatoEldreEnnEllerLikAar;


public class StartRegistreringUtilsService {

    private static final Logger LOG = LoggerFactory.getLogger(StartRegistreringUtilsService.class);

    static final int ANTALL_AAR_ISERV = 2;
    public static final String MIN_ALDER_AUTOMATISK_REGISTRERING = "min.alder.automatisk.registrering";
    public static final String MAX_ALDER_AUTOMATISK_REGISTRERING = "maks.alder.automatisk.registrering";

    public boolean oppfyllerKravOmAutomatiskRegistrering(String fnr, Supplier<List<Arbeidsforhold>> arbeidsforholdSupplier,
                                                                OppfolgingStatus oppfolgingStatus, LocalDate dagensDato) {
        LocalDate fodselsdato = FnrUtils.utledFodselsdatoForFnr(fnr);
        int alder = FnrUtils.antallAarSidenDato(fodselsdato, dagensDato);
        LocalDate inaktiveringsdato = Optional.ofNullable(oppfolgingStatus).map(OppfolgingStatus::getInaktiveringsdato).orElse(null);

        return oppfyllerKravOmInaktivitet(dagensDato, inaktiveringsdato) &&
                oppfyllerKravOmAlder(alder) &&
                oppfyllerKravOmArbeidserfaring(arbeidsforholdSupplier.get(), dagensDato);
    }

    boolean oppfyllerKravOmAlder(int alder) {
        Integer minAlderAutomatiskRegistrering = getInteger(MIN_ALDER_AUTOMATISK_REGISTRERING);
        Integer maksAlderAutomatiskRegistrering = getInteger(MAX_ALDER_AUTOMATISK_REGISTRERING);

        if (nonNull(minAlderAutomatiskRegistrering) && nonNull(maksAlderAutomatiskRegistrering)) {
            return alder >= minAlderAutomatiskRegistrering && alder <= maksAlderAutomatiskRegistrering;
        } else {
            LOG.error("Parametrene " + MAX_ALDER_AUTOMATISK_REGISTRERING + " eller " +
                    MIN_ALDER_AUTOMATISK_REGISTRERING + " mangler eller har ugyldig innhold.");
            return false;
        }
    }

    boolean oppfyllerKravOmInaktivitet(LocalDate dagensDato, LocalDate inaktiveringsdato) {
        return Objects.isNull(inaktiveringsdato) || erDatoEldreEnnEllerLikAar(dagensDato, inaktiveringsdato, ANTALL_AAR_ISERV);
    }
}
