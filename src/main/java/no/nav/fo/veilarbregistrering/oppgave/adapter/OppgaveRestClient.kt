package no.nav.fo.veilarbregistrering.oppgave.adapter

import no.nav.common.health.HealthCheck
import no.nav.common.health.HealthCheckResult
import no.nav.common.health.HealthCheckUtils
import no.nav.common.rest.client.RestClient
import no.nav.common.rest.client.RestUtils
import no.nav.common.sts.SystemUserTokenProvider
import okhttp3.OkHttpClient
import okhttp3.Request
import org.springframework.http.HttpStatus
import java.io.IOException
import java.util.concurrent.TimeUnit

class OppgaveRestClient constructor(
    private val baseUrl: String,
    private val systemUserTokenProvider: SystemUserTokenProvider
) : HealthCheck {
    internal fun opprettOppgave(oppgaveDto: OppgaveDto): OppgaveResponseDto {
        val request = Request.Builder()
            .url("$baseUrl/api/v1/oppgaver")
            .header("Authorization", "Bearer " + systemUserTokenProvider.systemUserToken)
            .method("POST", RestUtils.toJsonRequestBody(oppgaveDto))
            .build()
        try {
            client.newCall(request).execute().use { response ->
                if (response.code() != HttpStatus.CREATED.value()) {
                    throw RuntimeException("Opprett oppgave feilet med statuskode: " + response.code() + " - " + response)
                }
                return RestUtils.parseJsonResponseOrThrow(response, OppgaveResponseDto::class.java)
            }
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    override fun checkHealth(): HealthCheckResult {
        return HealthCheckUtils.pingUrl(baseUrl, client)
    }

    companion object {
        private const val HTTP_READ_TIMEOUT = 120000

        private val client: OkHttpClient = RestClient.baseClientBuilder()
            .readTimeout(HTTP_READ_TIMEOUT.toLong(), TimeUnit.MILLISECONDS).build()
    }
}