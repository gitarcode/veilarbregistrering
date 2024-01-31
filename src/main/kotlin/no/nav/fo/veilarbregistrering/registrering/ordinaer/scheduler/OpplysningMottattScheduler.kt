package no.nav.fo.veilarbregistrering.registrering.ordinaer.scheduler

import io.getunleash.Unleash
import no.nav.common.job.leader_election.LeaderElectionClient
import no.nav.fo.veilarbregistrering.log.logger
import no.nav.fo.veilarbregistrering.registrering.ordinaer.BrukerRegistreringService
import org.springframework.scheduling.annotation.Scheduled

class OpplysningMottattScheduler(
    private val leaderElectionClient: LeaderElectionClient,
    private val brukerRegistreringService: BrukerRegistreringService,
    private val opplysningerMottattProducer: OpplysningerMottattProducer,
    private val unleashClient: Unleash,
) {
    @Scheduled(fixedDelay = 500, initialDelay = 10_000)
    fun start() {
        if (!leaderElectionClient.isLeader) {
            return
        }

        overfoerOpplysningerMottatt()
    }

    private fun overfoerOpplysningerMottatt() {
        logger.info("Opplysninger om arbeidssøker: Feature toggle er av")
          return
    }

    companion object {
        const val FEATURE_TOGGLE = "veilarbregistrering.overfoer.opplysningeromarbeidssoeker"
    }
}