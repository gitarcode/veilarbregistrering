package no.nav.fo.veilarbregistrering.bruker.krr

import no.nav.common.sts.ServiceToServiceTokenProvider
import no.nav.common.sts.SystemUserTokenProvider
import no.nav.common.utils.EnvironmentUtils
import no.nav.fo.veilarbregistrering.bruker.KrrGateway
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class KrrConfig {
    @Bean
    fun krrClient(systemUserTokenProvider: SystemUserTokenProvider, serviceToServiceTokenProvider: ServiceToServiceTokenProvider): KrrClient {
        val baseUrl = EnvironmentUtils.getRequiredProperty(KRR_URL_PROPERTY_NAME)
        val cluster = EnvironmentUtils.getRequiredProperty(KRR_CLUSTER_PROPERTY_NAME)

        return KrrClient(baseUrl, systemUserTokenProvider) {
            serviceToServiceTokenProvider
                .getServiceToken("dkif", "team-rocket", cluster)
        }
    }

    @Bean
    fun krrGateway(krrClient: KrrClient?): KrrGateway {
        return KrrGatewayImpl(krrClient!!)
    }

    companion object {
        private const val KRR_URL_PROPERTY_NAME = "KRR_BASE_URL"
        private const val KRR_CLUSTER_PROPERTY_NAME = "KRR_BASE_URL"
    }
}