package no.nav.fo.veilarbregistrering.arbeidssoker.formidlingsgruppe.kafka

import io.getunleash.DefaultUnleash
import io.getunleash.Unleash
import no.nav.common.log.MDCConstants
import no.nav.fo.veilarbregistrering.arbeidssoker.formidlingsgruppe.FormidlingsgruppeMottakService
import no.nav.fo.veilarbregistrering.arbeidssoker.formidlingsgruppe.kafka.FormidlingsgruppeMapper.Companion.map
import no.nav.fo.veilarbregistrering.log.CallId.leggTilCallId
import no.nav.fo.veilarbregistrering.log.logger
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import java.time.Duration
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.function.Consumer

/**
 * 1. Den skal konsumere TOPIC for "Formidlingsgruppe" fra Arena
 * 2. Den skal kjøre i evig løkka
 * 3. Den skal kalle på et internt API for å lagre formidlingsgruppe knyttet til person
 */
class FormidlingsgruppeKafkaConsumer internal constructor(
    private val kafkaConsumerProperties: Properties,
    private val topic: String,
    private val formidlingsgruppeMottakService: FormidlingsgruppeMottakService,
    private val unleashClient: Unleash
) : Runnable {
    init {
        val forsinkelseIMinutterVedOppstart = 5
        val forsinkelseIMinutterVedStopp = 5
        Executors.newSingleThreadScheduledExecutor()
            .scheduleWithFixedDelay(
                this,
                forsinkelseIMinutterVedOppstart.toLong(),
                forsinkelseIMinutterVedStopp.toLong(),
                TimeUnit.MINUTES
            )
    }

    override fun run() {
        MDC.put(mdcTopicKey, topic)
        LOG.info("Running")
        try {
            KafkaConsumer<String, String>(kafkaConsumerProperties).use { consumer ->
                consumer.subscribe(listOf(topic))
                LOG.info("Subscribing to {}", topic)
                while (true) {
                    val consumerRecords = consumer.poll(Duration.ofMinutes(2))
                    LOG.info("Leser {} events fra topic {}", consumerRecords.count(), topic)

                    if (consumerRecords.isEmpty) {
                        logger.info("Ingen nye events - venter på neste poll ...")
                        continue
                    }

                    consumerRecords.forEach(Consumer { record: ConsumerRecord<String, String> ->
                        leggTilCallId()
                        try {
                            behandleFormidlingsgruppeMelding(record)
                        } catch (e: IllegalArgumentException) {
                            LOG.warn(String.format("Behandling av record feilet: %s", record.value()), e)
                        } catch (e: RuntimeException) {
                            LOG.error(String.format("Behandling av record feilet: %s", record.value()), e)
                            throw e
                        } finally {
                            MDC.remove(MDCConstants.MDC_CALL_ID)
                        }
                    })
                    logger.info("Nyeste offset er: ${consumerRecords.last().offset()}")
                    consumer.commitSync()
                }
                LOG.info("Stopper lesing av topic etter at toggle `{}` er skrudd på", KILL_SWITCH_TOGGLE_NAME)
            }
        } catch (e: Exception) {
            LOG.error(String.format("Det oppstod en ukjent feil ifm. konsumering av events fra %s", topic), e)
        } finally {
            MDC.remove(MDCConstants.MDC_CALL_ID)
            MDC.remove(mdcTopicKey)
        }
    }

    private fun behandleFormidlingsgruppeMelding(melding: ConsumerRecord<String, String>) =
        formidlingsgruppeMottakService.behandle(map(melding.value()))

    companion object {
        private val LOG = LoggerFactory.getLogger(FormidlingsgruppeKafkaConsumer::class.java)
        private const val mdcTopicKey = "topic"
        private const val KILL_SWITCH_TOGGLE_NAME = "veilarbregistrering.stopKonsumeringAvFormidlingsgruppe"
    }
}
