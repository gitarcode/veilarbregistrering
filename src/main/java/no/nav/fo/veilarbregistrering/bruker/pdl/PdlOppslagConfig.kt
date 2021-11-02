package no.nav.fo.veilarbregistrering.bruker.pdl

import no.nav.common.sts.ServiceToServiceTokenProvider
import no.nav.common.utils.EnvironmentUtils
import no.nav.fo.veilarbregistrering.bruker.PdlOppslagGateway
import no.nav.fo.veilarbregistrering.log.loggerFor
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class PdlOppslagConfig {

    @Bean
    fun pdlOppslagClient(serviceToServiceTokenProvider: ServiceToServiceTokenProvider): PdlOppslagClient {
        val baseUrl = EnvironmentUtils.getRequiredProperty("PDL_URL")
        val pdlCluster = EnvironmentUtils.getRequiredProperty("PDL_CLUSTER")

        return PdlOppslagClient(baseUrl) {
            loggerFor<PdlOppslagConfig>().info("Requesting service token for PDL")
            serviceToServiceTokenProvider
                .getServiceToken("pdl-api", "pdl", pdlCluster)
        }
    }

    @Bean
    fun pdlOppslagGateway(pdlOppslagClient: PdlOppslagClient): PdlOppslagGateway {
        return PdlOppslagGatewayImpl(pdlOppslagClient)
    }
}
