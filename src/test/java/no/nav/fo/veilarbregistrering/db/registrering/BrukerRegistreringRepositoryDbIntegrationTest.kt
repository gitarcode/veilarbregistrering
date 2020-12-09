package no.nav.fo.veilarbregistrering.db.registrering

import no.nav.fo.veilarbregistrering.besvarelse.*
import no.nav.fo.veilarbregistrering.bruker.AktorId
import no.nav.fo.veilarbregistrering.bruker.Bruker
import no.nav.fo.veilarbregistrering.bruker.Foedselsnummer
import no.nav.fo.veilarbregistrering.db.DatabaseConfig
import no.nav.fo.veilarbregistrering.db.MigrationUtils
import no.nav.fo.veilarbregistrering.db.RepositoryConfig
import no.nav.fo.veilarbregistrering.db.TransactionalTest
import no.nav.fo.veilarbregistrering.registrering.bruker.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ContextConfiguration

@TransactionalTest
@ContextConfiguration(classes = [DatabaseConfig::class, RepositoryConfig::class])
open class BrukerRegistreringRepositoryDbIntegrationTest(

    @Autowired
    private val jdbcTemplate: JdbcTemplate,
    @Autowired
    private val brukerRegistreringRepository: BrukerRegistreringRepository) {

    @BeforeEach
    fun setup() {
        MigrationUtils.createTables(jdbcTemplate)
    }

    @Test
    fun registrerBruker() {
        val registrering = OrdinaerBrukerRegistreringTestdataBuilder.gyldigBrukerRegistrering()
        val ordinaerBrukerRegistrering = brukerRegistreringRepository.lagre(registrering, BRUKER_1)
        assertRegistrertBruker(registrering, ordinaerBrukerRegistrering)
    }

    @Test
    fun hentBrukerregistreringForAktorId() {
        val registrering1 = OrdinaerBrukerRegistreringTestdataBuilder.gyldigBrukerRegistrering().setBesvarelse(BesvarelseTestdataBuilder.gyldigBesvarelse()
                .setAndreForhold(AndreForholdSvar.JA))
        val registrering2 = OrdinaerBrukerRegistreringTestdataBuilder.gyldigBrukerRegistrering().setBesvarelse(BesvarelseTestdataBuilder.gyldigBesvarelse()
                .setAndreForhold(AndreForholdSvar.NEI))
        brukerRegistreringRepository.lagre(registrering1, BRUKER_1)
        brukerRegistreringRepository.lagre(registrering2, BRUKER_1)
        val registrering = brukerRegistreringRepository.hentOrdinaerBrukerregistreringForAktorId(AKTOR_ID_11111)
        assertRegistrertBruker(registrering2, registrering)
    }

    @Test
    fun hentSykmeldtregistreringForAktorId() {
        val bruker1 = SykmeldtRegistreringTestdataBuilder.gyldigSykmeldtRegistrering().setBesvarelse(BesvarelseTestdataBuilder.gyldigSykmeldtSkalTilbakeSammeJobbBesvarelse()
                .setTilbakeIArbeid(TilbakeIArbeidSvar.JA_FULL_STILLING))
        val bruker2 = SykmeldtRegistreringTestdataBuilder.gyldigSykmeldtRegistrering().setBesvarelse(BesvarelseTestdataBuilder.gyldigSykmeldtSkalTilbakeSammeJobbBesvarelse()
                .setTilbakeIArbeid(TilbakeIArbeidSvar.JA_REDUSERT_STILLING))
        brukerRegistreringRepository.lagreSykmeldtBruker(bruker1, AKTOR_ID_11111)
        brukerRegistreringRepository.lagreSykmeldtBruker(bruker2, AKTOR_ID_11111)
        val registrering = brukerRegistreringRepository.hentSykmeldtregistreringForAktorId(AKTOR_ID_11111)
        assertSykmeldtRegistrertBruker(bruker2, registrering)
    }

    @Test
    fun hentOrdinaerBrukerRegistreringForAktorId() {
        val registrering = OrdinaerBrukerRegistreringTestdataBuilder.gyldigBrukerRegistrering().setBesvarelse(BesvarelseTestdataBuilder.gyldigBesvarelse()
                .setAndreForhold(AndreForholdSvar.JA))
        val lagretBruker = brukerRegistreringRepository.lagre(registrering, BRUKER_1)
        registrering.setId(lagretBruker.id).opprettetDato = lagretBruker.opprettetDato
        val ordinaerBrukerRegistrering = brukerRegistreringRepository
                .hentOrdinaerBrukerregistreringForAktorId(AKTOR_ID_11111)
        assertEquals(registrering, ordinaerBrukerRegistrering)
    }

    @Test
    fun hentOrdinaerBrukerRegistreringForAktorIdSkalReturnereNullHvisBrukerIkkeErRegistret() {
        val uregistrertAktorId = AktorId.of("9876543")
        val profilertBrukerRegistrering = brukerRegistreringRepository
                .hentOrdinaerBrukerregistreringForAktorId(uregistrertAktorId)
        assertNull(profilertBrukerRegistrering)
    }

    private fun assertRegistrertBruker(bruker: OrdinaerBrukerRegistrering, ordinaerBrukerRegistrering: OrdinaerBrukerRegistrering) {
        assertThat(ordinaerBrukerRegistrering.besvarelse).isEqualTo(bruker.besvarelse)
        assertThat(ordinaerBrukerRegistrering.sisteStilling).isEqualTo(bruker.sisteStilling)
        assertThat(ordinaerBrukerRegistrering.teksterForBesvarelse).isEqualTo(bruker.teksterForBesvarelse)
    }

    private fun assertSykmeldtRegistrertBruker(bruker: SykmeldtRegistrering, sykmeldtRegistrering: SykmeldtRegistrering) {
        assertThat(sykmeldtRegistrering.besvarelse).isEqualTo(bruker.besvarelse)
        assertThat(sykmeldtRegistrering.teksterForBesvarelse).isEqualTo(bruker.teksterForBesvarelse)
    }

    @Test
    fun skal_hente_foedselsnummer_tilknyttet_ordinaerBrukerRegistrering() {
        val ordinaerBrukerRegistrering = brukerRegistreringRepository.lagre(OrdinaerBrukerRegistreringTestdataBuilder.gyldigBrukerRegistrering(), BRUKER_1)
        val bruker = brukerRegistreringRepository.hentBrukerTilknyttet(ordinaerBrukerRegistrering.id)
        assertThat(bruker.gjeldendeFoedselsnummer).isEqualTo(BRUKER_1.gjeldendeFoedselsnummer)
        assertThat(bruker.aktorId).isEqualTo(BRUKER_1.aktorId)
    }

    @Test
    fun findRegistreringByPage_skal_returnere_eldste_registrering_pa_bakgrunn_av_id() {
        brukerRegistreringRepository.lagre(OrdinaerBrukerRegistreringTestdataBuilder.gyldigBrukerRegistrering(), BRUKER_1)
        brukerRegistreringRepository.lagre(OrdinaerBrukerRegistreringTestdataBuilder.gyldigBrukerRegistrering(), BRUKER_2)
        brukerRegistreringRepository.lagre(OrdinaerBrukerRegistreringTestdataBuilder.gyldigBrukerRegistrering(), BRUKER_3)
        val pageRequest = PageRequest.of(0, 2)
        val registreringByPage = brukerRegistreringRepository.findRegistreringByPage(pageRequest)
        assertThat(registreringByPage.totalPages).isEqualTo(2)
    }

    @Test
    fun findRegistreringByPage_skal_paging_for_a_levere_batcher_med_rader() {
        brukerRegistreringRepository.lagre(OrdinaerBrukerRegistreringTestdataBuilder.gyldigBrukerRegistrering(), BRUKER_1)
        brukerRegistreringRepository.lagre(OrdinaerBrukerRegistreringTestdataBuilder.gyldigBrukerRegistrering(), BRUKER_2)
        brukerRegistreringRepository.lagre(OrdinaerBrukerRegistreringTestdataBuilder.gyldigBrukerRegistrering(), BRUKER_3)
        val pageRequest = PageRequest.of(1, 2)
        val registreringByPage = brukerRegistreringRepository.findRegistreringByPage(pageRequest)
        assertThat(registreringByPage.totalPages).isEqualTo(2)
    }

    @Test
    fun findRegistreringByPage_skal_returnere_internEvents() {
        brukerRegistreringRepository.lagre(OrdinaerBrukerRegistreringTestdataBuilder.gyldigBrukerRegistrering(), BRUKER_1)
        brukerRegistreringRepository.lagre(OrdinaerBrukerRegistreringTestdataBuilder.gyldigBrukerRegistrering(), BRUKER_2)
        brukerRegistreringRepository.lagre(OrdinaerBrukerRegistreringTestdataBuilder.gyldigBrukerRegistrering(), BRUKER_3)
        val pageRequest = PageRequest.of(0, 2)
        val registreringByPage = brukerRegistreringRepository.findRegistreringByPage(pageRequest)
        val randomEvent = registreringByPage.content[0]
        assertThat(randomEvent.brukersSituasjon).hasValue(DinSituasjonSvar.JOBB_OVER_2_AAR)
        assertThat(randomEvent.utdanningSvar).hasValue(UtdanningSvar.HOYERE_UTDANNING_5_ELLER_MER)
        assertThat(randomEvent.utdanningBestattSvar).hasValue(UtdanningBestattSvar.JA)
        assertThat(randomEvent.utdanningGodkjentSvar).hasValue(UtdanningGodkjentSvar.JA)
    }

    companion object {
        private val FOEDSELSNUMMER = Foedselsnummer.of("12345678911")
        private val AKTOR_ID_11111 = AktorId.of("11111")
        private val BRUKER_1 = Bruker.of(FOEDSELSNUMMER, AKTOR_ID_11111)
        private val FOEDSELSNUMMER_2 = Foedselsnummer.of("22345678911")
        private val AKTOR_ID_22222 = AktorId.of("22222")
        private val BRUKER_2 = Bruker.of(FOEDSELSNUMMER_2, AKTOR_ID_22222)
        private val FOEDSELSNUMMER_3 = Foedselsnummer.of("32345678911")
        private val AKTOR_ID_33333 = AktorId.of("33333")
        private val BRUKER_3 = Bruker.of(FOEDSELSNUMMER_3, AKTOR_ID_33333)
    }
}