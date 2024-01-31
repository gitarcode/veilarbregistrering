package no.nav.fo.veilarbregistrering.arbeidssoker.perioder.scheduler

import io.getunleash.Unleash
import no.nav.common.job.leader_election.LeaderElectionClient
import no.nav.fo.veilarbregistrering.arbeidssoker.ArbeidssokerperiodeService
import no.nav.fo.veilarbregistrering.log.logger
import org.springframework.scheduling.annotation.Scheduled

class ArbeidssokerperiodeScheduler(
    private val leaderElectionClient: LeaderElectionClient,
    private val arbeidssokerperiodeService: ArbeidssokerperiodeService,
    private val arbeidssokerperiodeProducer: ArbeidssokerperiodeProducer,
    private val unleashClient: Unleash,
) {
    @Scheduled(fixedDelay = 10_000, initialDelay = 10_000)
    fun start() {
        if (!leaderElectionClient.isLeader) {
            return
        }

        overfoerArbeidssokerperioder()
    }

    private fun overfoerArbeidssokerperioder() {
        logger.info("Arbeidssøkerperioder overføring: Feature toggle er av")
          return
    }

    companion object {
        const val FEATURE_TOGGLE = "veilarbregistrering.overfoer.arbeidssokerperioder"
    }
}
