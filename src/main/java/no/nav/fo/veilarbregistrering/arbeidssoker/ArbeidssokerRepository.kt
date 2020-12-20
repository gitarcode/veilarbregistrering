package no.nav.fo.veilarbregistrering.arbeidssoker

import no.nav.fo.veilarbregistrering.bruker.Foedselsnummer

interface ArbeidssokerRepository {
    fun lagre(command: EndretFormidlingsgruppeCommand): Long
    fun finnFormidlingsgrupper(foedselsnummer: Foedselsnummer): Arbeidssokerperioder
    fun finnFormidlingsgrupper(foedselsnummerList: List<Foedselsnummer>): Arbeidssokerperioder
}