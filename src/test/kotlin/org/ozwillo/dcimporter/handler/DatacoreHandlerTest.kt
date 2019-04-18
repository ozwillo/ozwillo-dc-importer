package org.ozwillo.dcimporter.handler

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.matching.EqualToPattern
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.ozwillo.dcimporter.config.DatacoreProperties
import org.ozwillo.dcimporter.config.RabbitMockConfig
import org.ozwillo.dcimporter.model.datacore.DCResource
import org.ozwillo.dcimporter.util.DCUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.client.postForEntity
import org.springframework.context.annotation.Import
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.client.ClientHttpRequestInterceptor
import java.time.LocalDateTime

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(RabbitMockConfig::class)
class DatacoreHandlerTest(
    @Autowired val restTemplate: TestRestTemplate,
    @Autowired val datacoreProperties: DatacoreProperties
) {

    private lateinit var wireMockServer: WireMockServer

    private val siret = "123456789"
    private val grantedSiret = "987654321"
    private val dateConvention = LocalDateTime.now()
    private val objet = "Test-dev-1"

    private val dcGetOrganizationResponse = """
        {
            "@id": "https://data.ozwillo.com/dc/type/orgfr:Organisation_0/FR/$siret"
        }
        """
    private val dcPostSubventionResponse = """
        {
            "@id": "http://data.ozwillo.com/dc/type/grant:association_0/FR/$siret/$dateConvention/$objet"
        }
        """
    private val inseeGetTokenResponse = """
        {
            "access_token": "accessToken"
        }
        """
    private val inseeGetOrgResponse = """
        {
            "header": {
                "statut": 200,
                "message": "ok"
            },
            "etablissement": {
                "siret": "$grantedSiret",
                "uniteLegale": {
                    "nomUniteLegale": "ANAME",
                    "denominationUniteLegale": null
                },
                "adresseEtablissement": {
                    "codePostalEtablissement": "11111",
                    "numeroVoieEtablissement": null,
                    "typeVoieEtablissement": null,
                    "libelleVoieEtablissement": "ASTREET"
                }
            }
        }
        """
    private val dcPostOrganizationResponse = """
        {
            "@id": "https://data.ozwillo.com/dc/type/orgfr:Organisation_0/FR/$grantedSiret"
        }
        """

    private val dcGetSubventionResponse = """
        {
            "@id": "http://data.ozwillo.com/dc/type/grant:association_0/FR/$siret/$dateConvention/$objet",
            "o:version": 0
        }
        """

    private val dcPutSubventionResponse = """
        {
            "@id": "http://data.ozwillo.com/dc/type/grant:association_0/FR/$siret/$dateConvention/$objet-1",
            "o:version": 1
        }
        """

    @BeforeAll
    fun beforeAll() {
        wireMockServer = WireMockServer(WireMockConfiguration.wireMockConfig().port(8089))
        wireMockServer.start()

        // we need a fake bearer to go through the verification chain
        restTemplate.restTemplate.interceptors.add(ClientHttpRequestInterceptor { request, body, execution ->
            request.headers["Authorization"] = "Bearer secrettoken"
            execution.execute(request, body)
        })

        WireMock.configureFor(8089)
    }

    @AfterAll
    fun afterAll() {
        wireMockServer.stop()
    }

    @Test
    fun `correct subvention creation with organization already saved in dc`() {
        WireMock.stubFor(
            WireMock.get(WireMock.urlMatching("/dc/type/orgfr:Organisation_0/FR/$siret"))
                .willReturn(WireMock.okJson(dcGetOrganizationResponse).withStatus(200))
        )
        WireMock.stubFor(
            WireMock.get(WireMock.urlMatching("/dc/type/orgfr:Organisation_0/FR/$grantedSiret"))
                .willReturn(WireMock.okJson(dcGetOrganizationResponse).withStatus(200))
        )
        WireMock.stubFor(
            WireMock.post(WireMock.urlMatching("/dc/type/grant:association_0"))
                .willReturn(WireMock.okJson(dcPostSubventionResponse).withStatus(201))
        )
        WireMock.stubFor(
            WireMock.get(WireMock.urlMatching("/dc/type/grant:association_0/FR/$siret/$dateConvention/$objet"))
                .willReturn(WireMock.okJson(dcPostSubventionResponse).withStatus(200))
        )

        val resourceLight = DCResource(
            DCUtils.getUri(
                datacoreProperties.baseUri, "grant:association_0",
                "FR/$siret/$dateConvention/$objet"
            )
        )
        val giverOrgUri = DCUtils.getUri(datacoreProperties.baseUri, "orgfr:Organisation_0", "FR/$siret")
        val grantedOrgUri = DCUtils.getUri(datacoreProperties.baseUri, "orgfr:Organisation_0", "FR/$grantedSiret")
        resourceLight.setStringValue("grantassociation:idAttribuant", giverOrgUri)
        resourceLight.setStringValue("grantassociation:idBeneficiaire", grantedOrgUri)
        resourceLight.setStringValue("grantassociation:conditionsVersement", "unique")
        resourceLight.setDateTimeValue("grantassociation:dateConvention", dateConvention)
        resourceLight.setDateTimeValue("grantassociation:datesPeriodeVersement", dateConvention.plusMonths(3))
        resourceLight.setIntegerValue("grantassociation:montant", 10)
        resourceLight.setListValue("grantassociation:nature", listOf("aide en numéraire"))
        resourceLight.setStringValue("grantassociation:nomAttribuant", "SICTIAM")
        resourceLight.setStringValue("grantassociation:nomBeneficiaire", "Mairie de Benoit Orihuela")
        resourceLight.setBooleanValue("grantassociation:notificationUE", true)
        resourceLight.setStringValue("grantassociation:objet", objet)
        resourceLight.setIntegerValue("grantassociation:pourcentageSubvention", 1)

        val entity = restTemplate.postForEntity<String>("/dc/type/grant:association_0", resourceLight)
        Assertions.assertThat(entity.statusCode).isEqualTo(HttpStatus.CREATED)
        Assertions.assertThat(entity.body).contains(siret)
        Assertions.assertThat(entity.body).contains(objet)
    }

    @Test
    fun `correct subvention creation with organization unknown from dc`() {
        WireMock.stubFor(
            WireMock.get(WireMock.urlMatching("/dc/type/orgfr:Organisation_0/FR/$siret"))
                .willReturn(WireMock.okJson(dcGetOrganizationResponse).withStatus(200))
        )
        WireMock.stubFor(
            WireMock.get(WireMock.urlMatching("/dc/type/orgfr:Organisation_0/FR/$grantedSiret"))
                .willReturn(WireMock.okJson(dcGetOrganizationResponse).withStatus(404))
        )
        WireMock.stubFor(
            WireMock.post(WireMock.urlMatching("/token"))
                .withHeader("Authorization", EqualToPattern("Basic secret"))
                .withHeader("Content-Type", EqualToPattern("application/x-www-form-urlencoded;charset=UTF-8"))
                .willReturn(WireMock.okJson(inseeGetTokenResponse).withStatus(200))
        )
        WireMock.stubFor(
            WireMock.get(
                WireMock.urlMatching(
                    "/entreprises/sirene/V3/siret/$grantedSiret\\?champs=codePostalEtablissement,numeroVoieEtablissement,typeVoieEtablissement,libelleVoieEtablissement,nomUniteLegale,denominationUniteLegale,siret"))
                .willReturn(WireMock.okJson(inseeGetOrgResponse).withStatus(200))
        )
        WireMock.stubFor(
            WireMock.post(WireMock.urlMatching("/dc/type/orgfr:Organisation_0"))
                .willReturn(WireMock.okJson(dcPostOrganizationResponse).withStatus(201))
        )
        WireMock.stubFor(
            WireMock.post(WireMock.urlMatching("/dc/type/grant:association_0"))
                .willReturn(WireMock.okJson(dcPostSubventionResponse).withStatus(201))
        )
        WireMock.stubFor(
            WireMock.get(WireMock.urlMatching("/dc/type/grant:association_0/FR/$siret/$dateConvention/$objet"))
                .willReturn(WireMock.okJson(dcPostSubventionResponse).withStatus(200))
        )

        val resourceLight = DCResource(
            DCUtils.getUri(
                datacoreProperties.baseUri, "grant:association_0",
                "FR/$siret/$dateConvention/$objet"
            )
        )
        val giverOrgUri = DCUtils.getUri(datacoreProperties.baseUri, "orgfr:Organisation_0", "FR/$siret")
        val grantedOrgUri = DCUtils.getUri(datacoreProperties.baseUri, "orgfr:Organisation_0", "FR/$grantedSiret")
        resourceLight.setStringValue("grantassociation:idAttribuant", giverOrgUri)
        resourceLight.setStringValue("grantassociation:idBeneficiaire", grantedOrgUri)
        resourceLight.setStringValue("grantassociation:conditionsVersement", "unique")
        resourceLight.setDateTimeValue("grantassociation:dateConvention", dateConvention)
        resourceLight.setDateTimeValue("grantassociation:datesPeriodeVersement", dateConvention.plusMonths(3))
        resourceLight.setIntegerValue("grantassociation:montant", 10)
        resourceLight.setListValue("grantassociation:nature", listOf("aide en numéraire"))
        resourceLight.setStringValue("grantassociation:nomAttribuant", "SICTIAM")
        resourceLight.setStringValue("grantassociation:nomBeneficiaire", "Mairie de Benoit Orihuela")
        resourceLight.setBooleanValue("grantassociation:notificationUE", true)
        resourceLight.setStringValue("grantassociation:objet", objet)
        resourceLight.setIntegerValue("grantassociation:pourcentageSubvention", 1)

        val entity = restTemplate.postForEntity<String>("/dc/type/grant:association_0", resourceLight)
        Assertions.assertThat(entity.statusCode).isEqualTo(HttpStatus.CREATED)
        Assertions.assertThat(entity.body).contains(siret)
        Assertions.assertThat(entity.body).contains(objet)
    }

    @Test
    fun `correct subvention update with organization already saved in dc`() {
        WireMock.stubFor(
            WireMock.get(WireMock.urlMatching("/dc/type/orgfr:Organisation_0/FR/$siret"))
                .willReturn(WireMock.okJson(dcGetOrganizationResponse).withStatus(200))
        )
        WireMock.stubFor(
            WireMock.get(WireMock.urlMatching("/dc/type/orgfr:Organisation_0/FR/$grantedSiret"))
                .willReturn(WireMock.okJson(dcGetOrganizationResponse).withStatus(200))
        )
        WireMock.stubFor(
            WireMock.post(WireMock.urlMatching("/dc/type/grant:association_0"))
                .willReturn(WireMock.okJson(dcPutSubventionResponse).withStatus(200))
        )
        WireMock.stubFor(
            WireMock.get(WireMock.urlMatching("/dc/type/grant:association_0/FR/$siret/$dateConvention/$objet"))
                .willReturn(WireMock.okJson(dcPutSubventionResponse).withStatus(200))
        )

        WireMock.stubFor(
            WireMock.get(WireMock.urlMatching("/dc/type/orgfr:Organisation_0/FR/$siret"))
                .willReturn(WireMock.okJson(dcGetOrganizationResponse).withStatus(200))
        )
        WireMock.stubFor(
            WireMock.get(WireMock.urlMatching("/dc/type/orgfr:Organisation_0/FR/$grantedSiret"))
                .willReturn(WireMock.okJson(dcGetOrganizationResponse).withStatus(200))
        )
        WireMock.stubFor(
            WireMock.get(WireMock.urlMatching("/dc/type/grant:association_0/FR/$siret/$dateConvention/$objet-1"))
                .willReturn(WireMock.okJson(dcPutSubventionResponse).withStatus(200))
        )
        WireMock.stubFor(
            WireMock.put(WireMock.urlMatching("/dc/type/grant:association_0"))
                .willReturn(WireMock.okJson(dcPutSubventionResponse).withStatus(200))
        )

        val resourceLight = DCResource(
            DCUtils.getUri(
                datacoreProperties.baseUri, "grant:association_0",
                "FR/$siret/$dateConvention/$objet"
            )
        )
        val giverOrgUri = DCUtils.getUri(datacoreProperties.baseUri, "orgfr:Organisation_0", "FR/$siret")
        val grantedOrgUri = DCUtils.getUri(datacoreProperties.baseUri, "orgfr:Organisation_0", "FR/$grantedSiret")
        resourceLight.setStringValue("grantassociation:idAttribuant", giverOrgUri)
        resourceLight.setStringValue("grantassociation:idBeneficiaire", grantedOrgUri)
        resourceLight.setStringValue("grantassociation:conditionsVersement", "unique")
        resourceLight.setDateTimeValue("grantassociation:dateConvention", dateConvention)
        resourceLight.setDateTimeValue("grantassociation:datesPeriodeVersement", dateConvention.plusMonths(3))
        resourceLight.setIntegerValue("grantassociation:montant", 10)
        resourceLight.setListValue("grantassociation:nature", listOf("aide en numéraire"))
        resourceLight.setStringValue("grantassociation:nomAttribuant", "SICTIAM")
        resourceLight.setStringValue("grantassociation:nomBeneficiaire", "Mairie de Benoit Orihuela")
        resourceLight.setBooleanValue("grantassociation:notificationUE", true)
        resourceLight.setStringValue("grantassociation:objet", objet)
        resourceLight.setIntegerValue("grantassociation:pourcentageSubvention", 1)

        restTemplate.postForEntity<String>("/dc/type/grant:association_0", resourceLight)

        resourceLight.setStringValue("grantassociation:objet", "$objet-1")
        val httpEntity = HttpEntity(resourceLight)
        val updatedEntity = restTemplate.exchange(
            "/dc/type/grant:association_0",
            HttpMethod.PUT, httpEntity, String::class.java
        )
        Assertions.assertThat(updatedEntity.statusCode).isEqualTo(HttpStatus.OK)
    }

    @Test
    fun `correct subvention update with organization unknown from dc`() {
        WireMock.stubFor(
            WireMock.get(WireMock.urlMatching("/dc/type/orgfr:Organisation_0/FR/$siret"))
                .willReturn(WireMock.okJson(dcGetOrganizationResponse).withStatus(200))
        )
        WireMock.stubFor(
            WireMock.get(WireMock.urlMatching("/dc/type/orgfr:Organisation_0/FR/$grantedSiret"))
                .willReturn(WireMock.okJson(dcGetOrganizationResponse).withStatus(404))
        )
        WireMock.stubFor(
            WireMock.post(WireMock.urlMatching("/token"))
                .withHeader("Authorization", EqualToPattern("Basic secret"))
                .withHeader("Content-Type", EqualToPattern("application/x-www-form-urlencoded;charset=UTF-8"))
                .willReturn(WireMock.okJson(inseeGetTokenResponse).withStatus(200))
        )
        WireMock.stubFor(
            WireMock.get(
                WireMock.urlMatching(
                    "/entreprises/sirene/V3/siret/$grantedSiret\\?champs=codePostalEtablissement,numeroVoieEtablissement,typeVoieEtablissement,libelleVoieEtablissement,nomUniteLegale,denominationUniteLegale,siret"))
                .willReturn(WireMock.okJson(inseeGetOrgResponse).withStatus(200))
        )
        WireMock.stubFor(
            WireMock.post(WireMock.urlMatching("/dc/type/orgfr:Organisation_0"))
                .willReturn(WireMock.okJson(dcPostOrganizationResponse).withStatus(201))
        )
        WireMock.stubFor(
            WireMock.post(WireMock.urlMatching("/dc/type/grant:association_0"))
                .willReturn(WireMock.okJson(dcPostSubventionResponse).withStatus(201))
        )
        WireMock.stubFor(
            WireMock.get(WireMock.urlMatching("/dc/type/grant:association_0/FR/$siret/$dateConvention/$objet"))
                .willReturn(WireMock.okJson(dcPostSubventionResponse).withStatus(200))
        )

        WireMock.stubFor(
            WireMock.get(WireMock.urlMatching("/dc/type/orgfr:Organisation_0/FR/$siret"))
                .willReturn(WireMock.okJson(dcGetOrganizationResponse).withStatus(200))
        )
        WireMock.stubFor(
            WireMock.get(WireMock.urlMatching("/dc/type/orgfr:Organisation_0/FR/$grantedSiret"))
                .willReturn(WireMock.okJson(dcGetOrganizationResponse).withStatus(404))
        )
        WireMock.stubFor(
            WireMock.post(WireMock.urlMatching("/token"))
                .withHeader("Authorization", EqualToPattern("Basic secret"))
                .withHeader("Content-Type", EqualToPattern("application/x-www-form-urlencoded;charset=UTF-8"))
                .willReturn(WireMock.okJson(inseeGetTokenResponse).withStatus(200))
        )
        WireMock.stubFor(
            WireMock.get(
                WireMock.urlMatching(
                    "/entreprises/sirene/V3/siret/$grantedSiret\\?champs=codePostalEtablissement,numeroVoieEtablissement,typeVoieEtablissement,libelleVoieEtablissement,nomUniteLegale,denominationUniteLegale,siret"))
                .willReturn(WireMock.okJson(inseeGetOrgResponse).withStatus(200))
        )
        WireMock.stubFor(
            WireMock.post(WireMock.urlMatching("/dc/type/orgfr:Organisation_0"))
                .willReturn(WireMock.okJson(dcPostOrganizationResponse).withStatus(201))
        )
        WireMock.stubFor(
            WireMock.get(WireMock.urlMatching("/dc/type/grant:association_0/FR/$siret/$dateConvention/$objet"))
                .willReturn(WireMock.okJson(dcGetSubventionResponse).withStatus(200))
        )
        WireMock.stubFor(
            WireMock.put(WireMock.urlMatching("/dc/type/grant:association_0"))
                .willReturn(WireMock.okJson(dcPostSubventionResponse).withStatus(201))
        )

        val resourceLight = DCResource(
            DCUtils.getUri(
                datacoreProperties.baseUri, "grant:association_0",
                "FR/$siret/$dateConvention/$objet"
            )
        )
        val giverOrgUri = DCUtils.getUri(datacoreProperties.baseUri, "orgfr:Organisation_0", "FR/$siret")
        val grantedOrgUri = DCUtils.getUri(datacoreProperties.baseUri, "orgfr:Organisation_0", "FR/$grantedSiret")
        resourceLight.setStringValue("grantassociation:idAttribuant", giverOrgUri)
        resourceLight.setStringValue("grantassociation:idBeneficiaire", grantedOrgUri)
        resourceLight.setStringValue("grantassociation:conditionsVersement", "unique")
        resourceLight.setDateTimeValue("grantassociation:dateConvention", dateConvention)
        resourceLight.setDateTimeValue("grantassociation:datesPeriodeVersement", dateConvention.plusMonths(3))
        resourceLight.setIntegerValue("grantassociation:montant", 10)
        resourceLight.setListValue("grantassociation:nature", listOf("aide en numéraire"))
        resourceLight.setStringValue("grantassociation:nomAttribuant", "SICTIAM")
        resourceLight.setStringValue("grantassociation:nomBeneficiaire", "Mairie de Benoit Orihuela")
        resourceLight.setBooleanValue("grantassociation:notificationUE", true)
        resourceLight.setStringValue("grantassociation:objet", objet)
        resourceLight.setIntegerValue("grantassociation:pourcentageSubvention", 1)

        restTemplate.postForEntity<String>("/dc/type/grant:association_0", resourceLight)

        resourceLight.setStringValue("grantassociation:objet", "$objet-1")
        val httpEntity = HttpEntity(resourceLight)
        val updatedEntity = restTemplate.exchange(
            "/dc/type/grant:association_0",
            HttpMethod.PUT, httpEntity, String::class.java
        )
        Assertions.assertThat(updatedEntity.statusCode).isEqualTo(HttpStatus.OK)
    }

}
