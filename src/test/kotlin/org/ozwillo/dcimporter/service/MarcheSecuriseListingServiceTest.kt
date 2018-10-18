package org.ozwillo.dcimporter.service

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.matching.EqualToPattern
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.ozwillo.dcimporter.config.RabbitMockConfig
import org.ozwillo.dcimporter.model.BusinessAppConfiguration
import org.ozwillo.dcimporter.model.BusinessMapping
import org.ozwillo.dcimporter.model.marchepublic.*
import org.ozwillo.dcimporter.model.sirene.Organization
import org.ozwillo.dcimporter.util.*
import org.ozwillo.dcimporter.utils.DCReturnModel
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.LocalDateTime

@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(RabbitMockConfig::class)
class MarcheSecuriseListingServiceTest(@Autowired val marcheSecuriseListingService: MarcheSecuriseListingService){

    private lateinit var wireMockServer: WireMockServer

    private val siret = "123456789"
    private val referenceConsultation = "ref-consultation"

    @Value("\${marchesecurise.url.updateConsultation}")
    private val updateConsultationUrl = ""


    @BeforeAll
    fun beforeAll() {
        wireMockServer = WireMockServer(WireMockConfiguration.wireMockConfig().port(8089))
        wireMockServer.start()

        WireMock.configureFor(8089)
    }

    @AfterAll
    fun afterAll() {
        wireMockServer.stop()
    }

    @Test
    fun `update datacore consultation state test`(){

        WireMock.stubFor(WireMock.post(WireMock.urlMatching("/a/token"))
                .withHeader("Authorization", EqualToPattern("Basic ZGNpbXBvcnRlcjpzZWNyZXQ="))
                .willReturn(WireMock.okJson(DCReturnModel.tokenInfoResponse).withStatus(200)))
        WireMock.stubFor(WireMock.get(WireMock.urlMatching("/dc/type/marchepublic:consultation_0\\?start=0&limit=100&mpconsultation:etat=${Etat.PUBLISHED}-"))
                .willReturn(WireMock.okJson(DCReturnModel.dcGetConsultationResponse).withStatus(200)))
        WireMock.stubFor(WireMock.get(WireMock.urlMatching("/dc/type/marchepublic:consultation_0/FR/$siret/$referenceConsultation"))
                .willReturn(WireMock.okJson(DCReturnModel.dcExistingResponse).withStatus(200)))
        WireMock.stubFor(WireMock.put(WireMock.urlMatching("/dc/type/marchepublic:consultation_0"))
                .willReturn(WireMock.okJson(DCReturnModel.dcPostConsultationResponse).withStatus(200)))

        marcheSecuriseListingService.updateFirstHundredPublishedConsultation()

        WireMock.verify(WireMock.putRequestedFor(WireMock.urlMatching("/dc/type/marchepublic:consultation_0")))
    }

    @Test
    fun `parse soap response object to Registre`(){

        val response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ns1=\"https://www.marches-securises.fr/webserv/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:SOAP-ENC=\"http://schemas.xmlsoap.org/soap/encoding/\" SOAP-ENV:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" +
                "    <SOAP-ENV:Body>\n" +
                "        <ns1:lister_reponses_electroniquesResponse>\n" +
                "            <return xsi:type=\"xsd:string\">&lt;?xml version=\"1.0\" encoding=\"UTF-8\"?&gt;\n" +
                "&lt;ifw:data xmlns:ifw=\"interbat/framwork-exportation\"&gt;\n" +
                "  &lt;objet type=\"ms__reponse\"&gt;\n" +
                "    &lt;propriete nom=\"cle\"&gt;cleReponse&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"cle_dce\"&gt;dce&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"cle_entreprise_ms\"&gt;cleEntreprise&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"denomination_ent\"&gt;nomEntreprise&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"contact\"&gt;contact&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"email_contact\"&gt;emailContact&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"date_depot\"&gt;1533300425&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"date_depot_f\"&gt;vendredi 03 août 2018 - 14:47&lt;/propriete&gt;\n" +
                "  &lt;propriete nom=\"taille_reponse\"&gt;4105705&lt;/propriete&gt;&lt;objet type=\"entreprise\"&gt;\n" +
                "    &lt;propriete nom=\"nom\"&gt;nomEntreprise&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"adresse_1\"&gt;adresse&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"adresse_2\"/&gt;\n" +
                "    &lt;propriete nom=\"code_postal\"&gt;codePostal&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"commune\"&gt;commune&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"pays\"&gt;pays&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"tel\"&gt;tel&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"fax\"&gt;fax&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"siret\"/&gt;\n" +
                "    &lt;propriete nom=\"siren\"/&gt;\n" +
                "    &lt;propriete nom=\"code_naf\"&gt;naf&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"url\"&gt;url&lt;/propriete&gt;\n" +
                "  &lt;/objet&gt;&lt;/objet&gt;\n" +
                "&lt;pagination ordre=\"\" sensordre=\"ASC\"/&gt;&lt;reponses nb_total=\"1\"/&gt;&lt;/ifw:data&gt;\n" +
                "</return>\n" +
                "        </ns1:lister_reponses_electroniquesResponse>\n" +
                "    </SOAP-ENV:Body>\n" +
                "</SOAP-ENV:Envelope>"

        val responseObject = MSUtils.parseToResponseObjectList(response, MSUtils.REPONSE_TYPE, BindingKeyAction.UPDATE.value)

        val registreReponse = marcheSecuriseListingService.parseSoapResponseObjectToRegistre(MSUtils.REPONSE_TYPE, responseObject[0], "consultationUri").block()!!
        assertThat(registreReponse.cle).isEqualTo("cleReponse")
        assertThat(registreReponse.consultationUri).isEqualTo("consultationUri")
        assertThat(registreReponse.siret).isEmpty()
        assertThat((registreReponse as RegistreReponse).emailContact).isEqualTo("emailContact")
    }

    @Test
    fun `get consultation msReference from Marches Securises`(){

        val msResponse = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ns1=\"https://www.marches-securises.fr/webserv/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:SOAP-ENC=\"http://schemas.xmlsoap.org/soap/encoding/\" SOAP-ENV:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" +
                "    <SOAP-ENV:Body>\n" +
                "        <ns1:lire_consultation_logResponse>\n" +
                "            <return xsi:type=\"xsd:string\">&lt;?xml version=\"1.0\" encoding=\"UTF-8\"?&gt;\n" +
                "&lt;ifw:data xmlns:ifw=\"interbat/framwork-exportation\"&gt;\n" +
                "  &lt;objet type=\"ms_v2__fullweb_dce\"&gt;\n" +
                "    &lt;propriete nom=\"cle\"&gt;dce&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"reference\"&gt;msReference&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"objet\"&gt;objet&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"date_creation\"&gt;12345678&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"date_publication\"&gt;7658986&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"date_publication_f\"&gt;jeudi 13 septembre 2018 - 00:00&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"date_cloture\"&gt;96543&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"date_cloture_f\"&gt;dimanche 30 décembre 2018 - 00:00&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"ref_interne\"&gt;reference&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"finalite_marche\"&gt;finaliteMarche&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"type_marche\"&gt;typeMarche&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"type_prestation\"&gt;prestation&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"departements_prestation\"&gt;departement&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"passation\"&gt;passation&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"informatique\"&gt;informatique&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"passe\"&gt;passe&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"emails\"&gt;email&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"en_ligne\"&gt;enligne&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"a_lots\"&gt;alloti&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"invisible\"&gt;0&lt;/propriete&gt;\n" +
                "  &lt;/objet&gt;\n" +
                "&lt;/ifw:data&gt;\n" +
                "</return>\n" +
                "        </ns1:lire_consultation_logResponse>\n" +
                "    </SOAP-ENV:Body>\n" +
                "</SOAP-ENV:Envelope>"

        WireMock.stubFor(WireMock.post(WireMock.urlMatching("/webserv/\\?module=dce%7Cserveur_modif_dce"))
                .willReturn(WireMock.okXml(msResponse).withStatus(200)))

        val businessMapping = BusinessMapping(dcId = "dcId", businessId = "businessId", businessId2 = "", applicationName = MarcheSecuriseService.name, type = MSUtils.CONSULTATION_TYPE)
        val businessAppConfiguration = BusinessAppConfiguration(applicationName = MarcheSecuriseService.name, baseUrl = "http://localhost:8089", organizationSiret = DCReturnModel.siret, login = "login", password = "password",
                instanceId = "instanceId")
        var response = ""

        val soapMessage = MSUtils.generateReadConsultationRequest(businessAppConfiguration.login!!, businessAppConfiguration.password!!, businessAppConfiguration.instanceId!!, businessMapping.businessId)
        if (!soapMessage.isEmpty()){
            response = MSUtils.sendSoap("${businessAppConfiguration.baseUrl}$updateConsultationUrl", soapMessage)
        }
        val responseObject = MSUtils.parseToResponseType(response, MSUtils.CONSULTATION_TYPE, BindingKeyAction.GET.value)
        if (responseObject.properties!![0].name!! == "cle" && responseObject.properties!!.size >= 2){
            businessMapping.businessId2 = responseObject.properties!![1].value!!
        }else{
            throw SoapParsingUnexpectedError("Unable to process to consultation msReference reading from Marchés Sécurisés because of unexpected error")
        }

        assertThat(businessMapping.businessId2).isEqualTo("msReference")
    }

    @Test
    fun `test of correct organization creation during datacore registre response update`(){

        WireMock.stubFor(WireMock.post(WireMock.urlMatching("/a/token"))
                .withHeader("Authorization", EqualToPattern("Basic ZGNpbXBvcnRlcjpzZWNyZXQ="))
                .willReturn(WireMock.okJson(DCReturnModel.tokenInfoResponse).withStatus(200)))
        WireMock.stubFor(WireMock.get(WireMock.urlMatching("/dc/type/orgfr:Organisation_0/FR/987654321"))
                .willReturn(WireMock.aResponse().withStatus(404)))
        WireMock.stubFor(WireMock.post(WireMock.urlMatching("/dc/type/orgfr:Organisation_0"))
                .willReturn(WireMock.okJson(DCReturnModel.dcPostOrganizationResponse).withStatus(201)))

        val registreReponse = RegistreReponse(cle = DCReturnModel.cleRegistre, nomContact = "ANAME", emailContact = "test@test.com", dateDepot = LocalDateTime.now(), poids = 400000,
                entreprise = Organization(siret = "987654321", cp = "11111", voie = "unevoie",commune = "une commune", pays = "FR", denominationUniteLegale = "un nom", tel = "un téléphone", naf = "un numéro",
                        url = "un url"), siret = "987654321", consultationUri = "http://baseUri/dc/type/${MSUtils.CONSULTATION_TYPE}/FR/${DCReturnModel.siret}/${DCReturnModel.referenceConsultation}")

        marcheSecuriseListingService.checkOrCreateOrganization(registreReponse, MSUtils.REPONSE_TYPE)

        WireMock.verify(WireMock.postRequestedFor(WireMock.urlMatching("/dc/type/orgfr:Organisation_0")))

    }

    @Test
    fun `test of correct organization creation during datacore registre retrait update`(){
        WireMock.stubFor(WireMock.post(WireMock.urlMatching("/a/token"))
                .withHeader("Authorization", EqualToPattern("Basic ZGNpbXBvcnRlcjpzZWNyZXQ="))
                .willReturn(WireMock.okJson(DCReturnModel.tokenInfoResponse).withStatus(200)))
        WireMock.stubFor(WireMock.get(WireMock.urlMatching("/dc/type/orgfr:Organisation_0/FR/987654321"))
                .willReturn(WireMock.aResponse().withStatus(404)))
        WireMock.stubFor(WireMock.post(WireMock.urlMatching("/dc/type/orgfr:Organisation_0"))
                .willReturn(WireMock.okJson(DCReturnModel.dcPostOrganizationResponse).withStatus(201)))

        val registreRetrait = RegistreRetrait(cle = DCReturnModel.cleRegistre, siret = "987654321", consultationUri = "http://baseUri/dc/type/${MSUtils.CONSULTATION_TYPE}/FR/${DCReturnModel.siret}/${DCReturnModel.referenceConsultation}" +
                "",
                pieceId = DCReturnModel.uuidPiece, nomPiece = "Unom", libellePiece = "", dateDebut = LocalDateTime.now(), dateFin = LocalDateTime.now().plusDays(3),
                personne = Personne(cle = DCReturnModel.clePersonne, genre = "m", nom = "Bugs", prenom = "Bunny", email = "test@test.com", telephone = "un téléphone", fax = ""),
                entreprise = Organization(siret = "987654321", cp = "11111", voie = "unevoie",commune = "une commune", pays = "FR", denominationUniteLegale = "un nom", tel = "un téléphone", naf = "un numéro",
                        url = "un url"))

        marcheSecuriseListingService.checkOrCreateOrganization(registreRetrait, MSUtils.RETRAIT_TYPE)

        WireMock.verify(WireMock.postRequestedFor(WireMock.urlMatching("/dc/type/orgfr:Organisation_0")))
    }

    @Test
    fun `test correct contact creation during datacore retrait update`(){
        WireMock.stubFor(WireMock.post(WireMock.urlMatching("/a/token"))
                .withHeader("Authorization", EqualToPattern("Basic ZGNpbXBvcnRlcjpzZWNyZXQ="))
                .willReturn(WireMock.okJson(DCReturnModel.tokenInfoResponse).withStatus(200)))
        WireMock.stubFor(WireMock.get(WireMock.urlMatching("/dc/type/${MSUtils.PERSONNE_TYPE}/${DCReturnModel.clePersonne}"))
                .willReturn(WireMock.aResponse().withStatus(404)))
        WireMock.stubFor(WireMock.post(WireMock.urlMatching("/dc/type/${MSUtils.PERSONNE_TYPE}"))
                .willReturn(WireMock.okJson(DCReturnModel.dcPostPersonResponse).withStatus(201)))

        val registreRetrait = RegistreRetrait(cle = DCReturnModel.cleRegistre, siret = "987654321", consultationUri = "baseUri/dc/type/${MSUtils.CONSULTATION_TYPE}/${DCReturnModel.siret}/${DCReturnModel.referenceConsultation}",
                pieceId = DCReturnModel.uuidPiece, nomPiece = "Unom", libellePiece = "", dateDebut = LocalDateTime.now(), dateFin = LocalDateTime.now().plusDays(3),
                personne = Personne(cle = DCReturnModel.clePersonne, genre = "m", nom = "Bugs", prenom = "Bunny", email = "test@test.com", telephone = "un téléphone", fax = ""),
                entreprise = Organization(siret = "987654321", cp = "11111", voie = "unevoie",commune = "une commune", pays = "FR", denominationUniteLegale = "un nom", tel = "un téléphone", naf = "un numéro",
                        url = "un url"))

        marcheSecuriseListingService.checkOrCreateOrUpdatePerson(registreRetrait)

        WireMock.verify(WireMock.postRequestedFor(WireMock.urlMatching("/dc/type/${MSUtils.PERSONNE_TYPE}")))
    }

    @Test
    fun `test correct contact update during datacore retrait update`(){
        WireMock.stubFor(WireMock.post(WireMock.urlMatching("/a/token"))
                .withHeader("Authorization", EqualToPattern("Basic ZGNpbXBvcnRlcjpzZWNyZXQ="))
                .willReturn(WireMock.okJson(DCReturnModel.tokenInfoResponse).withStatus(200)))
        WireMock.stubFor(WireMock.get(WireMock.urlMatching("/dc/type/${MSUtils.PERSONNE_TYPE}/${DCReturnModel.clePersonne}"))
                .willReturn(WireMock.okJson(DCReturnModel.dcGetPersonResponse).withStatus(200)))
        WireMock.stubFor(WireMock.put(WireMock.urlMatching("/dc/type/${MSUtils.PERSONNE_TYPE}"))
                .willReturn(WireMock.okJson(DCReturnModel.dcPostPersonResponse).withStatus(200)))

        val registreRetrait = RegistreRetrait(cle = DCReturnModel.cleRegistre, siret = "987654321", consultationUri = "baseUri/dc/type/${MSUtils.CONSULTATION_TYPE}/${DCReturnModel.siret}/${DCReturnModel.referenceConsultation}",
                pieceId = DCReturnModel.uuidPiece, nomPiece = "Unom", libellePiece = "", dateDebut = LocalDateTime.now(), dateFin = LocalDateTime.now().plusDays(3),
                personne = Personne(cle = DCReturnModel.clePersonne, genre = "m", nom = "Bugs", prenom = "Bunny", email = "un mail différent", telephone = "un téléphone différent", fax = ""),
                entreprise = Organization(siret = "987654321", cp = "11111", voie = "unevoie",commune = "une commune", pays = "FR", denominationUniteLegale = "un nom", tel = "un téléphone", naf = "un numéro",
                        url = "un url"))

        marcheSecuriseListingService.checkOrCreateOrUpdatePerson(registreRetrait)

        WireMock.verify(WireMock.putRequestedFor(WireMock.urlMatching("/dc/type/${MSUtils.PERSONNE_TYPE}")))
    }

    @Test
    fun `test no contact update during datacore retrait update`(){
        WireMock.stubFor(WireMock.post(WireMock.urlMatching("/a/token"))
                .withHeader("Authorization", EqualToPattern("Basic ZGNpbXBvcnRlcjpzZWNyZXQ="))
                .willReturn(WireMock.okJson(DCReturnModel.tokenInfoResponse).withStatus(200)))
        WireMock.stubFor(WireMock.get(WireMock.urlMatching("/dc/type/${MSUtils.PERSONNE_TYPE}/${DCReturnModel.clePersonne}"))
                .willReturn(WireMock.okJson(DCReturnModel.dcGetPersonResponse).withStatus(200)))
        WireMock.stubFor(WireMock.get(WireMock.urlMatching("/dc/type/${MSUtils.PERSONNE_TYPE}/${DCReturnModel.clePersonne}"))
                .willReturn(WireMock.okJson(DCReturnModel.dcGetPersonResponse).withStatus(200)))

        val registreRetrait = RegistreRetrait(cle = DCReturnModel.cleRegistre, siret = "987654321", consultationUri = "baseUri/dc/type/${MSUtils.CONSULTATION_TYPE}/${DCReturnModel.siret}/${DCReturnModel.referenceConsultation}",
                pieceId = DCReturnModel.uuidPiece, nomPiece = "Unom", libellePiece = "", dateDebut = LocalDateTime.now(), dateFin = LocalDateTime.now().plusDays(3),
                personne = Personne(cle = DCReturnModel.clePersonne, genre = "m", nom = "BUGS", prenom = "Bunny", email = "test@test.com", telephone = "artichaut", fax = ""),
                entreprise = Organization(siret = "987654321", cp = "11111", voie = "unevoie",commune = "une commune", pays = "FR", denominationUniteLegale = "un nom", tel = "un téléphone", naf = "un numéro",
                        url = "un url"))

        marcheSecuriseListingService.checkOrCreateOrUpdatePerson(registreRetrait)

        WireMock.verify(WireMock.getRequestedFor(WireMock.urlMatching("/dc/type/${MSUtils.PERSONNE_TYPE}/${DCReturnModel.clePersonne}")))
    }

    @Test
    fun `test create registre reponse during datacore registre update`(){
        WireMock.stubFor(WireMock.post(WireMock.urlMatching("/a/token"))
                .withHeader("Authorization", EqualToPattern("Basic ZGNpbXBvcnRlcjpzZWNyZXQ="))
                .willReturn(WireMock.okJson(DCReturnModel.tokenInfoResponse).withStatus(200)))
        WireMock.stubFor(WireMock.get(WireMock.urlMatching("/dc/type/${MSUtils.REPONSE_TYPE}/FR/${DCReturnModel.siret}/${DCReturnModel.referenceConsultation}/${DCReturnModel.cleRegistre}"))
                .willReturn(WireMock.aResponse().withStatus(404)))
        WireMock.stubFor(WireMock.post(WireMock.urlMatching("/dc/type/${MSUtils.REPONSE_TYPE}"))
                .willReturn(WireMock.okJson(DCReturnModel.dcPostRegistreReponseResponse).withStatus(201)))

        val registreReponse = RegistreReponse(cle = DCReturnModel.cleRegistre, nomContact = "ANAME", emailContact = "test@test.com", dateDepot = LocalDateTime.now(), poids = 400000,
                entreprise = Organization(siret = "987654321", cp = "11111", voie = "unevoie",commune = "une commune", pays = "FR", denominationUniteLegale = "un nom", tel = "un téléphone", naf = "un numéro",
                        url = "un url"), siret = "987654321", consultationUri = "http://baseUri/dc/type/${MSUtils.CONSULTATION_TYPE}/FR/${DCReturnModel.siret}/${DCReturnModel.referenceConsultation}")

        val dcRegistreReponse = registreReponse.toDcObject("http://baseUri/dc/type", registreReponse.cle)

        marcheSecuriseListingService.createOrUpdateRegistre(MSUtils.REPONSE_TYPE, registreReponse, dcRegistreReponse)

        WireMock.verify(WireMock.postRequestedFor(WireMock.urlMatching("/dc/type/${MSUtils.REPONSE_TYPE}")))
    }

    @Test
    fun `test update registre reponse during datacore registre update`(){
        WireMock.stubFor(WireMock.post(WireMock.urlMatching("/a/token"))
                .withHeader("Authorization", EqualToPattern("Basic ZGNpbXBvcnRlcjpzZWNyZXQ="))
                .willReturn(WireMock.okJson(DCReturnModel.tokenInfoResponse).withStatus(200)))
        WireMock.stubFor(WireMock.get(WireMock.urlMatching("/dc/type/${MSUtils.REPONSE_TYPE}/FR/${DCReturnModel.siret}/${DCReturnModel.referenceConsultation}/${DCReturnModel.cleRegistre}"))
                .willReturn(WireMock.okJson(DCReturnModel.dcGetRegistreReponse).withStatus(200)))
        WireMock.stubFor(WireMock.put(WireMock.urlMatching("/dc/type/${MSUtils.REPONSE_TYPE}"))
                .willReturn(WireMock.okJson(DCReturnModel.dcPostRegistreReponseResponse).withStatus(200)))

        val registreReponse = RegistreReponse(cle = DCReturnModel.cleRegistre, nomContact = "ANAME", emailContact = "test@test.com", dateDepot = LocalDateTime.now(), poids = 400000,
                entreprise = Organization(siret = "987654321", cp = "11111", voie = "unevoie",commune = "une commune", pays = "FR", denominationUniteLegale = "un nom", tel = "un téléphone", naf = "un numéro",
                        url = "un url"), siret = "987654321", consultationUri = "http://baseUri/dc/type/${MSUtils.CONSULTATION_TYPE}/FR/${DCReturnModel.siret}/${DCReturnModel.referenceConsultation}")

        val dcRegistreReponse = registreReponse.toDcObject("http://baseUri/dc/type", registreReponse.cle)

        marcheSecuriseListingService.createOrUpdateRegistre(MSUtils.REPONSE_TYPE, registreReponse, dcRegistreReponse)

        WireMock.verify(WireMock.putRequestedFor(WireMock.urlMatching("/dc/type/${MSUtils.REPONSE_TYPE}")))
    }

    @Test
    fun `test create registre retrait during datacore registre update`(){
        WireMock.stubFor(WireMock.post(WireMock.urlMatching("/a/token"))
                .withHeader("Authorization", EqualToPattern("Basic ZGNpbXBvcnRlcjpzZWNyZXQ="))
                .willReturn(WireMock.okJson(DCReturnModel.tokenInfoResponse).withStatus(200)))
        WireMock.stubFor(WireMock.get(WireMock.urlMatching("/dc/type/${MSUtils.RETRAIT_TYPE}/FR/${DCReturnModel.siret}/${DCReturnModel.referenceConsultation}/${DCReturnModel.cleRegistre}"))
                .willReturn(WireMock.aResponse().withStatus(404)))
        WireMock.stubFor(WireMock.post(WireMock.urlMatching("/dc/type/${MSUtils.RETRAIT_TYPE}"))
                .willReturn(WireMock.okJson(DCReturnModel.dcPostRegistreRetraitResponse).withStatus(201)))

        val registreRetrait = RegistreRetrait(cle = DCReturnModel.cleRegistre, siret = "987654321", consultationUri = "http://baseUri/dc/type/${MSUtils.CONSULTATION_TYPE}/FR/${DCReturnModel.siret}/${DCReturnModel.referenceConsultation}",
                pieceId = DCReturnModel.uuidPiece, nomPiece = "Unom", libellePiece = "", dateDebut = LocalDateTime.now(), dateFin = LocalDateTime.now().plusDays(3),
                personne = Personne(cle = DCReturnModel.clePersonne, genre = "m", nom = "BUGS", prenom = "Bunny", email = "test@test.com", telephone = "artichaut", fax = ""),
                entreprise = Organization(siret = "987654321", cp = "11111", voie = "unevoie",commune = "une commune", pays = "FR", denominationUniteLegale = "un nom", tel = "un téléphone", naf = "un numéro",
                        url = "un url"))

        val dcRegistreRetrait = registreRetrait.toDcObject("http://baseUri/dc/type", DCReturnModel.cleRegistre, DCReturnModel.clePersonne, DCReturnModel.uuidPiece)

        marcheSecuriseListingService.createOrUpdateRegistre(MSUtils.RETRAIT_TYPE, registreRetrait, dcRegistreRetrait)

        WireMock.verify(WireMock.postRequestedFor(WireMock.urlMatching("/dc/type/${MSUtils.RETRAIT_TYPE}")))
    }

    @Test
    fun `test update registre retrait during datacore registre update`(){
        WireMock.stubFor(WireMock.post(WireMock.urlMatching("/a/token"))
                .withHeader("Authorization", EqualToPattern("Basic ZGNpbXBvcnRlcjpzZWNyZXQ="))
                .willReturn(WireMock.okJson(DCReturnModel.tokenInfoResponse).withStatus(200)))
        WireMock.stubFor(WireMock.get(WireMock.urlMatching("/dc/type/${MSUtils.RETRAIT_TYPE}/FR/${DCReturnModel.siret}/${DCReturnModel.referenceConsultation}/${DCReturnModel.cleRegistre}"))
                .willReturn(WireMock.okJson(DCReturnModel.dcGetRegistreRetrait).withStatus(200)))
        WireMock.stubFor(WireMock.put(WireMock.urlMatching("/dc/type/${MSUtils.RETRAIT_TYPE}"))
                .willReturn(WireMock.okJson(DCReturnModel.dcPostRegistreRetraitResponse).withStatus(201)))

        val registreRetrait = RegistreRetrait(cle = DCReturnModel.cleRegistre, siret = "987654321", consultationUri = "http://baseUri/dc/type/${MSUtils.CONSULTATION_TYPE}/FR/${DCReturnModel.siret}/${DCReturnModel.referenceConsultation}",
                pieceId = DCReturnModel.uuidPiece, nomPiece = "Unom", libellePiece = "", dateDebut = LocalDateTime.now(), dateFin = LocalDateTime.now().plusDays(3),
                personne = Personne(cle = DCReturnModel.clePersonne, genre = "m", nom = "BUGS", prenom = "Bunny", email = "test@test.com", telephone = "artichaut", fax = ""),
                entreprise = Organization(siret = "987654321", cp = "11111", voie = "unevoie",commune = "une commune", pays = "FR", denominationUniteLegale = "un nom", tel = "un téléphone", naf = "un numéro",
                        url = "un url"))

        val dcRegistreRetrait = registreRetrait.toDcObject("http://baseUri/dc/type", DCReturnModel.cleRegistre, DCReturnModel.clePersonne, DCReturnModel.uuidPiece)

        marcheSecuriseListingService.createOrUpdateRegistre(MSUtils.RETRAIT_TYPE, registreRetrait, dcRegistreRetrait)

        WireMock.verify(WireMock.putRequestedFor(WireMock.urlMatching("/dc/type/${MSUtils.RETRAIT_TYPE}")))
    }
}