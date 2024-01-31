package no.nav.fo.veilarbregistrering.registrering.publisering.scheduler

import io.getunleash.Unleash
import no.nav.common.job.leader_election.LeaderElectionClient
import no.nav.fo.veilarbregistrering.log.CallId
import no.nav.fo.veilarbregistrering.registrering.publisering.PubliseringAvEventsService
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.scheduling.annotation.Scheduled

class PubliseringAvRegistreringEventsScheduler(
    private val publiseringAvEventsService: PubliseringAvEventsService,
    private val leaderElectionClient: LeaderElectionClient,
    private val unleashClient: Unleash
) {
    @Scheduled(cron = HVERT_TIENDE_SEKUND)
    fun publiserRegistreringEvents() {
        try {
            CallId.leggTilCallId()
            if (!leaderElectionClient.isLeader) {
                return
            }
            publiseringAvEventsService.publiserEvents()
        } finally {
            MDC.clear()
        }
    }

    companion object {
        const val HVERT_TIENDE_SEKUND = "0/10 * * * * *"
    }
}
