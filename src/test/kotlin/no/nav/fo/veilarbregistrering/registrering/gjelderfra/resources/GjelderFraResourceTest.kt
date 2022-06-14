package no.nav.fo.veilarbregistrering.registrering.gjelderfra.resources

import io.mockk.mockk
import no.nav.fo.veilarbregistrering.toJSON
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@AutoConfigureMockMvc
@WebMvcTest
@ContextConfiguration(classes = [GjelderFraDatoResourceConfig::class])
class GjelderFraResourceTest (@Autowired private val mvc: MockMvc) {

    @Test
    fun `get - Svarer med 200 - dato=null når det ikke fins noen dato`(){
        val responseBody = mvc.get("/api/registrering/gjelder-fra")
            .andExpect {
                status { isOk() }
            }
            .andReturn()
            .response.contentAsString

        Assertions.assertThat(responseBody).isEqualTo(toJSON(GjelderFraDatoDto(null)))
    }
}

@Configuration
class GjelderFraDatoResourceConfig {
    @Bean
    fun gjelderFraDatoResource() : GjelderFraDatoResource {
        return GjelderFraDatoResource(mockk(relaxed = true), mockk(relaxed = true))
    }
}