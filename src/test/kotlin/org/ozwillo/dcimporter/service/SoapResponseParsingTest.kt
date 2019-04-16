package org.ozwillo.dcimporter.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.ozwillo.dcimporter.model.marchepublic.*
import org.ozwillo.dcimporter.util.*
import org.springframework.boot.test.context.SpringBootTest
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SoapResponseParsingTest {

    private var pa: String = "instance pa"

    val dce = "1533297690p44lmzk2fidz"
    val msReference = "F-SICTIAM_06_20180925W2_01"
    val objet = "Consultation WS Test"
    val enligne = MSUtils.booleanToInt(true).toString()
    val datePublication = LocalDateTime.now().atZone(ZoneId.of("Europe/Paris")).toInstant().epochSecond.toString()
    val dateCloture =
        LocalDateTime.now().plusMonths(3).atZone(ZoneId.of("Europe/Paris")).toInstant().epochSecond.toString()
    val reference = "F-SICTIAM_06_20180622W2_01"
    val finaliteMarche = FinaliteMarcheType.AUTRE.toString().toLowerCase()
    val typeMarche = TypeMarcheType.AUTRE.toString().toLowerCase()
    val prestation = TypePrestationType.AUTRES.toString().toLowerCase()
    val passation = "AORA"
    val informatique = MSUtils.booleanToInt(true).toString()
    val alloti = MSUtils.booleanToInt(false).toString()
    val departement = MSUtils.intListToString(listOf(74, 38, 6))
    val email = if (MSUtils.stringListToString(
            listOf(
                "test1@test.com",
                "test2@test.com",
                "test3@test.com"
            )
        ).length > 255
    ) (MSUtils.stringListToString(listOf("test1@test.com", "test2@test.com", "test3@test.com"))).substring(
        0,
        255
    ) else MSUtils.stringListToString(listOf("test1@test.com", "test2@test.com", "test3@test.com"))

    val cleLot = "1532963100xz12dzos6jyh"
    val libelleLot = "Un premier test"
    val ordreLot = 1.toString()
    val numeroLot = 1.toString()

    val clePiece = "1532504326yqgjft2lti7x"
    val libellePiece = "Test modification pièce3"
    val la = MSUtils.booleanToInt(false).toString()
    val ordrePiece = 1.toString()
    val nom = "NomDuFichierSansTiret6"
    val extension = "txt"

    val clePersonne = "1335945366ODE0aP0Xgd"
    val genre = "m"
    val nomContact = "Bugs"
    val prenomContact = "Bunny"
    val telephoneContact = "numéro de téléphone du contact"

    val cleRetrait = "1533743662n9qa4hxbnjhs"
    val dateDebut = LocalDateTime.now().atZone(ZoneId.of("Europe/Paris")).toInstant().epochSecond.toString()
    val dateFin = LocalDateTime.now().plusMonths(3).atZone(ZoneId.of("Europe/Paris")).toInstant().epochSecond.toString()

    val cleReponse = "1533048729cetzyl2xvn78"
    val cleEntreprise = "12678986288x531"
    val nomEntreprise = "Compte entreprise pour F-SICTIAM_06"
    val contact = "Bugs Bunny"
    val emailContact = "test1@test.com"
    val dateDepot = LocalDateTime.now().atZone(ZoneId.of("Europe/Paris")).toInstant().epochSecond.toString()
    val poidsReponse = 4105705.toString()
    val adresse = "Une adresse"
    val codePostal = 11111.toString()
    val commune = "Vallauris"
    val pays = "FR"
    val tel = "un numéro de téléphone"
    val fax = "un numéro de fax"
    val naf = "un code naf"
    val url = "www.unurldesite.fr"

    @Test
    fun `correct consultation creation response parsing test`() {

        val response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<SOAP-ENV:Envelope\n" +
                "    xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\"\n" +
                "    xmlns:ns1=\"https://www.marches-securises.fr/webserv/\"\n" +
                "    xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"\n" +
                "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                "    xmlns:SOAP-ENC=\"http://schemas.xmlsoap.org/soap/encoding/\" SOAP-ENV:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" +
                "    <SOAP-ENV:Body>\n" +
                "        <ns1:creer_consultation_logResponse>\n" +
                "            <return xsi:type=\"xsd:string\">&lt;?xml version=\"1.0\" encoding=\"UTF-8\"?&gt;\n" +
                "&lt;ifw:data\n" +
                "                xmlns:ifw=\"interbat/framwork-exportation\"&gt;\n" +
                "  &lt;objet type=\"ms_v2__fullweb_dce\"&gt;\n" +
                "    &lt;propriete nom=\"cle\" statut=\"changed\"&gt;$dce&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"cle_pa\" statut=\"changed\"&gt;$pa&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"reference\"&gt;$msReference&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"objet\" statut=\"changed\"&gt;$objet&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"date_publication\" statut=\"changed\"&gt;$datePublication&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"date_publication_f\" statut=\"changed\"&gt;${LocalDateTime.now()}&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"date_cloture\" statut=\"changed\"&gt;$dateCloture&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"date_cloture_f\" statut=\"changed\"&gt;${LocalDateTime.now()}&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"ref_interne\" statut=\"changed\"&gt;$reference&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"finalite_marche\" statut=\"changed\"&gt;$finaliteMarche&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"type_marche\" statut=\"changed\"&gt;$typeMarche&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"type_prestation\" statut=\"changed\"&gt;$prestation&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"departements_prestation\"&gt;$departement&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"passation\"/&gt;\n" +
                "    &lt;propriete nom=\"informatique\"/&gt;\n" +
                "    &lt;propriete nom=\"passe\" statut=\"changed\"&gt;passe&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"emails\"/&gt;\n" +
                "    &lt;propriete nom=\"en_ligne\" statut=\"changed\"&gt;$enligne&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"a_lots\" statut=\"changed\"&gt;$alloti&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"invisible\"/&gt;\n" +
                "  &lt;nombre_lots nom=\"nb_lot\"&gt;0&lt;/nombre_lots&gt;&lt;/objet&gt;\n" +
                "&lt;/ifw:data&gt;\n" +
                "\n" +
                "            </return>\n" +
                "        </ns1:creer_consultation_logResponse>\n" +
                "    </SOAP-ENV:Body>\n" +
                "</SOAP-ENV:Envelope>"

        val responseObject =
            MSUtils.parseToResponseType(response, MSUtils.CONSULTATION_TYPE, BindingKeyAction.CREATE.value)

        assertThat(responseObject).isNotNull
        assertThat(responseObject.lotNbr).isEqualTo(0)
        assertThat(responseObject.properties!!.size).isEqualTo(20)
        assertThat(responseObject.properties!![0].name).isEqualTo("cle")
        assertThat(responseObject.properties!![0].status).isEqualTo("changed")
        assertThat(responseObject.properties!![0].value).isEqualTo(dce)
        assertThat(responseObject.properties!![13].name).isEqualTo("passation")
        assertThat(responseObject.properties!![13].value).isEmpty()
    }

    @Test
    fun `consultation creation response with bad pa parsing test`() {
        val response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ns1=\"https://www.marches-securises.fr/webserv/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:SOAP-ENC=\"http://schemas.xmlsoap.org/soap/encoding/\" SOAP-ENV:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" +
                "    <SOAP-ENV:Body>\n" +
                "        <ns1:creer_consultation_logResponse>\n" +
                "            <return xsi:type=\"xsd:string\">&lt;?xml version=\"1.0\" encoding=\"UTF-8\"?&gt;\n" +
                "&lt;ifw:data xmlns:ifw=\"interbat/framwork-exportation\"&gt;\n" +
                "  &lt;objet type=\"error\"&gt;\n" +
                "    &lt;propriete nom=\"load_pa_error\"&gt;error&lt;/propriete&gt;\n" +
                "  &lt;/objet&gt;\n" +
                "&lt;/ifw:data&gt;\n" +
                "</return>\n" +
                "        </ns1:creer_consultation_logResponse>\n" +
                "    </SOAP-ENV:Body>\n" +
                "</SOAP-ENV:Envelope>"

        val responseObject =
            MSUtils.parseToResponseType(response, MSUtils.CONSULTATION_TYPE, BindingKeyAction.CREATE.value)

        assertThat(responseObject.type).isEqualTo("error")
        assertThat(responseObject.properties!!.size).isEqualTo(1)
        assertThat(responseObject.properties!![0].name).isEqualTo("load_pa_error")
        assertThat(responseObject.properties!![0].value).isEqualTo("error")
    }

    @Test
    fun `consultation creation response with bad login or password parsing test`() {
        val response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ns1=\"https://www.marches-securises.fr/webserv/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:SOAP-ENC=\"http://schemas.xmlsoap.org/soap/encoding/\" SOAP-ENV:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" +
                "    <SOAP-ENV:Body>\n" +
                "        <ns1:creer_consultation_logResponse>\n" +
                "            <return xsi:nil=\"true\"/>\n" +
                "        </ns1:creer_consultation_logResponse>\n" +
                "    </SOAP-ENV:Body>\n" +
                "</SOAP-ENV:Envelope>"

        try {
            MSUtils.parseToResponseType(response, MSUtils.CONSULTATION_TYPE, BindingKeyAction.CREATE.value)
        } catch (e: BadLogError) {
            assertThat(e.message).isEqualTo(
                "Unable to process ${MSUtils.CONSULTATION_TYPE} ${BindingKeyAction.CREATE.value} request for following reason : Unknown login/password.")
        }
    }

    @Test
    fun `response to bad request format parsing test`() {
        val response = "<SOAP-ENV:Fault>\n" +
                "            <faultcode>SOAP-ENV:Server</faultcode>\n" +
                "            <faultstring>SOAP-ERROR: Encoding: Violation of encoding rules</faultstring>\n" +
                "        </SOAP-ENV:Fault>"

        try {
            MSUtils.parseToResponseType(response, MSUtils.CONSULTATION_TYPE, BindingKeyAction.CREATE.value)
        } catch (e: SoapParsingUnexpectedError) {
            assertThat(e.message).isEqualTo(
                "An error occurs during soap response parsing from Marchés Sécurisés. Please check your request format.")
        }
    }

    @Test
    fun `correct consultation update response parsing test`() {
        val response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<SOAP-ENV:Envelope\n" +
                "    xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\"\n" +
                "    xmlns:ns1=\"https://www.marches-securises.fr/webserv/\"\n" +
                "    xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"\n" +
                "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                "    xmlns:SOAP-ENC=\"http://schemas.xmlsoap.org/soap/encoding/\" SOAP-ENV:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" +
                "    <SOAP-ENV:Body>\n" +
                "        <ns1:modifier_consultation_logResponse>\n" +
                "            <return xsi:type=\"xsd:string\">&lt;?xml version=\"1.0\" encoding=\"UTF-8\"?&gt;\n" +
                "&lt;ifw:data\n" +
                "                xmlns:ifw=\"interbat/framwork-exportation\"&gt;\n" +
                "  &lt;objet type=\"ms_v2__fullweb_dce\"&gt;\n" +
                "    &lt;propriete nom=\"cle\"&gt;$dce&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"reference\"&gt;$msReference&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"objet\" statut=\"changed\"&gt;$objet&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"date_creation\" statut=\"changed\"&gt;$datePublication&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"date_publication\" statut=\"changed\"&gt;$datePublication&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"date_publication_f\"&gt;${LocalDateTime.now()}&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"date_cloture\" statut=\"changed\"&gt;$dateCloture&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"date_cloture_f\"&gt;${LocalDateTime.now()}&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"ref_interne\" statut=\"changed\"&gt;$reference&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"finalite_marche\" statut=\"changed\"&gt;$finaliteMarche&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"type_marche\" statut=\"changed\"&gt;$typeMarche&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"type_prestation\" statut=\"changed\"&gt;$prestation&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"departements_prestation\" statut=\"changed\"&gt;$departement&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"passation\"&gt;$passation&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"informatique\" statut=\"changed\"&gt;$informatique&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"passe\"&gt;passe&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"emails\" statut=\"changed\"&gt;$email&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"en_ligne\"&gt;$enligne&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"a_lots\"&gt;$alloti&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"invisible\"&gt;0&lt;/propriete&gt;\n" +
                "  &lt;nombre_lots nom=\"nb_lot\"&gt;0&lt;/nombre_lots&gt;&lt;/objet&gt;\n" +
                "&lt;/ifw:data&gt;\n" +
                "\n" +
                "            </return>\n" +
                "        </ns1:modifier_consultation_logResponse>\n" +
                "    </SOAP-ENV:Body>\n" +
                "</SOAP-ENV:Envelope>"

        val responseObject =
            MSUtils.parseToResponseType(response, MSUtils.CONSULTATION_TYPE, BindingKeyAction.UPDATE.value)
        assertThat(responseObject).isNotNull
        assertThat(responseObject.lotNbr).isEqualTo(0)
        assertThat(responseObject.properties!!.size).isEqualTo(20)
        assertThat(responseObject.properties!![0].name).isEqualTo("cle")
        assertThat(responseObject.properties!![0].value).isEqualTo(dce)
        assertThat(responseObject.properties!![2].name).isEqualTo("objet")
        assertThat(responseObject.properties!![2].status).isEqualTo("changed")
        assertThat(responseObject.properties!![2].value).isEqualTo(objet)
        assertThat(responseObject.properties!![4].name).isEqualTo("date_publication")
        assertThat(responseObject.properties!![4].status).isEqualTo("changed")
        assertThat(responseObject.properties!![4].value).isEqualTo(datePublication)
        assertThat(responseObject.properties!![12].name).isEqualTo("departements_prestation")
        assertThat(responseObject.properties!![12].status).isEqualTo("changed")
        assertThat(responseObject.properties!![12].value).isEqualTo(departement)
    }

    @Test
    fun `consultation update with bad data response parsing test`() {
        val response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<SOAP-ENV:Envelope\n" +
                "    xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\"\n" +
                "    xmlns:ns1=\"https://www.marches-securises.fr/webserv/\"\n" +
                "    xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"\n" +
                "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                "    xmlns:SOAP-ENC=\"http://schemas.xmlsoap.org/soap/encoding/\" SOAP-ENV:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" +
                "    <SOAP-ENV:Body>\n" +
                "        <ns1:modifier_consultation_logResponse>\n" +
                "            <return xsi:type=\"xsd:string\">&lt;?xml version=\"1.0\" encoding=\"UTF-8\"?&gt;\n" +
                "&lt;ifw:data\n" +
                "                xmlns:ifw=\"interbat/framwork-exportation\"&gt;\n" +
                "  &lt;objet type=\"ms_v2__fullweb_dce\"&gt;\n" +
                "    &lt;propriete nom=\"cle\"&gt;$dce&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"reference\"&gt;$msReference&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"objet\" statut=\"changed\"&gt;$objet&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"date_creation\" statut=\"changed\"&gt;$datePublication&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"date_publication\" statut=\"changed\"&gt;$datePublication&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"date_publication_f\"&gt;${LocalDateTime.now()}&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"date_cloture\" statut=\"changed\"&gt;$dateCloture&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"date_cloture_f\"&gt;${LocalDateTime.now()}&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"ref_interne\" statut=\"changed\"&gt;$reference&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"finalite_marche\" statut=\"changed\"&gt;$finaliteMarche&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"type_marche\" statut=\"changed\"&gt;$typeMarche&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"type_prestation\" statut=\"changed\"&gt;$prestation&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"departements_prestation\" statut=\"changed\"&gt;$departement&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"passation\" statut=\"not_changed\" message=\"value_not_allowed\"&gt;$passation&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"informatique\" statut=\"changed\"&gt;$informatique&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"passe\"&gt;passe&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"emails\" statut=\"changed\"&gt;$email&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"en_ligne\"&gt;$enligne&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"a_lots\"&gt;$alloti&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"invisible\"&gt;0&lt;/propriete&gt;\n" +
                "  &lt;nombre_lots nom=\"nb_lot\"&gt;0&lt;/nombre_lots&gt;&lt;/objet&gt;\n" +
                "&lt;/ifw:data&gt;\n" +
                "\n" +
                "            </return>\n" +
                "        </ns1:modifier_consultation_logResponse>\n" +
                "    </SOAP-ENV:Body>\n" +
                "</SOAP-ENV:Envelope>"

        val responseObject =
            MSUtils.parseToResponseType(response, MSUtils.CONSULTATION_TYPE, BindingKeyAction.UPDATE.value)
        assertThat(responseObject).isNotNull
        assertThat(responseObject.lotNbr).isEqualTo(0)
        assertThat(responseObject.properties!!.size).isEqualTo(20)
        assertThat(responseObject.properties!![0].name).isEqualTo("cle")
        assertThat(responseObject.properties!![0].value).isEqualTo(dce)
        assertThat(responseObject.properties!![0].message).isNull()
        assertThat(responseObject.properties!![13].name).isEqualTo("passation")
        assertThat(responseObject.properties!![13].status).isEqualTo("not_changed")
        assertThat(responseObject.properties!![13].value).isEqualTo(passation)
        assertThat(responseObject.properties!![13].message).isEqualTo("value_not_allowed")
    }

    @Test
    fun `update consultation with bad login or password response parsing test`() {
        val response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ns1=\"https://www.marches-securises.fr/webserv/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:SOAP-ENC=\"http://schemas.xmlsoap.org/soap/encoding/\" SOAP-ENV:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" +
                "    <SOAP-ENV:Body>\n" +
                "        <ns1:modifier_consultation_logResponse>\n" +
                "            <return xsi:nil=\"true\"/>\n" +
                "        </ns1:modifier_consultation_logResponse>\n" +
                "    </SOAP-ENV:Body>\n" +
                "</SOAP-ENV:Envelope>"

        try {
            MSUtils.parseToResponseType(response, MSUtils.CONSULTATION_TYPE, BindingKeyAction.UPDATE.value)
        } catch (e: BadLogError) {
            assertThat(e.message).isEqualTo(
                "Unable to process marchepublic:consultation_0 ${BindingKeyAction.UPDATE.value} request for following reason : Unknown login/password.")
        }
    }

    @Test
    fun `update consultation with bad pa response parsing test`() {
        val response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<SOAP-ENV:Envelope\n" +
                "    xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\"\n" +
                "    xmlns:ns1=\"https://www.marches-securises.fr/webserv/\"\n" +
                "    xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"\n" +
                "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                "    xmlns:SOAP-ENC=\"http://schemas.xmlsoap.org/soap/encoding/\" SOAP-ENV:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" +
                "    <SOAP-ENV:Body>\n" +
                "        <ns1:modifier_consultation_logResponse>\n" +
                "            <return xsi:type=\"xsd:string\">&lt;?xml version=\"1.0\" encoding=\"UTF-8\"?&gt;\n" +
                "&lt;ifw:data\n" +
                "                xmlns:ifw=\"interbat/framwork-exportation\"&gt;\n" +
                "  &lt;objet type=\"error\"&gt;\n" +
                "    &lt;propriete nom=\"load_pa_error\"&gt;error&lt;/propriete&gt;\n" +
                "  &lt;/objet&gt;\n" +
                "&lt;/ifw:data&gt;\n" +
                "\n" +
                "            </return>\n" +
                "        </ns1:modifier_consultation_logResponse>\n" +
                "    </SOAP-ENV:Body>\n" +
                "</SOAP-ENV:Envelope>"

        val responseObject =
            MSUtils.parseToResponseType(response, MSUtils.CONSULTATION_TYPE, BindingKeyAction.UPDATE.value)

        assertThat(responseObject.type).isEqualTo("error")
        assertThat(responseObject.properties!!.size).isEqualTo(1)
        assertThat(responseObject.properties!![0].name).isEqualTo("load_pa_error")
        assertThat(responseObject.properties!![0].value).isEqualTo("error")
    }

    @Test
    fun `update consultation with bad dce response parsing test`() {
        val response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<SOAP-ENV:Envelope\n" +
                "    xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\"\n" +
                "    xmlns:ns1=\"https://www.marches-securises.fr/webserv/\"\n" +
                "    xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"\n" +
                "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                "    xmlns:SOAP-ENC=\"http://schemas.xmlsoap.org/soap/encoding/\" SOAP-ENV:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" +
                "    <SOAP-ENV:Body>\n" +
                "        <ns1:modifier_consultation_logResponse>\n" +
                "            <return xsi:type=\"xsd:string\">&lt;?xml version=\"1.0\" encoding=\"UTF-8\"?&gt;\n" +
                "&lt;ifw:data\n" +
                "                xmlns:ifw=\"interbat/framwork-exportation\"&gt;\n" +
                "  &lt;objet type=\"ms_v2__fullweb_dce\"&gt;\n" +
                "    &lt;propriete nom=\"load_consultation_fail\" statut=\"not_changed\" message=\"no_consultation\"&gt;no_consultation&lt;/propriete&gt;\n" +
                "  &lt;/objet&gt;\n" +
                "&lt;/ifw:data&gt;\n" +
                "\n" +
                "            </return>\n" +
                "        </ns1:modifier_consultation_logResponse>\n" +
                "    </SOAP-ENV:Body>\n" +
                "</SOAP-ENV:Envelope>"

        val responseObject =
            MSUtils.parseToResponseType(response, MSUtils.CONSULTATION_TYPE, BindingKeyAction.UPDATE.value)

        assertThat(responseObject.properties!!.size).isEqualTo(1)
        assertThat(responseObject.properties!![0].name).isEqualTo("load_consultation_fail")
        assertThat(responseObject.properties!![0].status).isEqualTo("not_changed")
        assertThat(responseObject.properties!![0].message).isEqualTo("no_consultation")
        assertThat(responseObject.properties!![0].value).isEqualTo("no_consultation")
    }

    @Test
    fun `update consultation with bad or missing array parsing test`() {
        val response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<SOAP-ENV:Envelope\n" +
                "    xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\"\n" +
                "    xmlns:ns1=\"https://www.marches-securises.fr/webserv/\"\n" +
                "    xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"\n" +
                "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                "    xmlns:SOAP-ENC=\"http://schemas.xmlsoap.org/soap/encoding/\" SOAP-ENV:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" +
                "    <SOAP-ENV:Body>\n" +
                "        <ns1:modifier_consultation_logResponse>\n" +
                "            <return xsi:type=\"xsd:string\">&lt;?xml version=\"1.0\" encoding=\"UTF-8\"?&gt;\n" +
                "&lt;ifw:data\n" +
                "                xmlns:ifw=\"interbat/framwork-exportation\"&gt;\n" +
                "  &lt;objet type=\"ms_v2__fullweb_dce\"&gt;\n" +
                "    &lt;propriete nom=\"array_expected\" statut=\"not_changed\" message=\"no_array\"&gt;no_array&lt;/propriete&gt;\n" +
                "  &lt;nombre_lots nom=\"nb_lot\"&gt;0&lt;/nombre_lots&gt;&lt;/objet&gt;\n" +
                "&lt;/ifw:data&gt;\n" +
                "\n" +
                "            </return>\n" +
                "        </ns1:modifier_consultation_logResponse>\n" +
                "    </SOAP-ENV:Body>\n" +
                "</SOAP-ENV:Envelope>"

        val responseObject =
            MSUtils.parseToResponseType(response, MSUtils.CONSULTATION_TYPE, BindingKeyAction.UPDATE.value)

        assertThat(responseObject.properties!!.size).isEqualTo(1)
        assertThat(responseObject.properties!![0].name).isEqualTo("array_expected")
        assertThat(responseObject.properties!![0].status).isEqualTo("not_changed")
        assertThat(responseObject.properties!![0].message).isEqualTo("no_array")
        assertThat(responseObject.properties!![0].value).isEqualTo("no_array")
    }

    @Test
    fun `correct delete consultation response parsing test`() {
        val response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ns1=\"https://www.marches-securises.fr/webserv/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:SOAP-ENC=\"http://schemas.xmlsoap.org/soap/encoding/\" SOAP-ENV:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" +
                "    <SOAP-ENV:Body>\n" +
                "        <ns1:supprimer_consultation_logResponse>\n" +
                "            <return xsi:type=\"xsd:string\">&lt;?xml version=\"1.0\" encoding=\"UTF-8\"?&gt;\n" +
                "&lt;suppression_consultation_ok xmlns=\"interbat/suppression_effectuee\"&gt;&lt;consultation_suppr_ok etat_consultation=\"supprimee\"/&gt;&lt;/suppression_consultation_ok&gt;\n" +
                "</return>\n" +
                "        </ns1:supprimer_consultation_logResponse>\n" +
                "    </SOAP-ENV:Body>\n" +
                "</SOAP-ENV:Envelope>"

        val responseObject =
            MSUtils.parseToResponseType(response, MSUtils.CONSULTATION_TYPE, BindingKeyAction.DELETE.value)
        assertThat(responseObject.consultationState).isEqualTo("supprimee")
    }

    @Test
    fun `delete consultation with bad dce response parsing test`() {
        val response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ns1=\"https://www.marches-securises.fr/webserv/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:SOAP-ENC=\"http://schemas.xmlsoap.org/soap/encoding/\" SOAP-ENV:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" +
                "    <SOAP-ENV:Body>\n" +
                "        <ns1:supprimer_consultation_logResponse>\n" +
                "            <return xsi:type=\"xsd:string\">&lt;?xml version=\"1.0\" encoding=\"UTF-8\"?&gt;\n" +
                "&lt;chargement_consultation_error xmlns=\"interbat/suppression_refusee\"&gt;&lt;consultation_cle_error cle=\"$dce\" reference=\"\" objet=\"\" date_creation=\"$datePublication\" date_publication=\"\" date_publication_f=\"${LocalDateTime.now()}\" date_cloture=\"\" date_cloture_f=\"${LocalDateTime.now()}\" ref_interne=\"\" nomenclature_interne=\"\" finalite_marche=\"\" type_marche=\"\" type_prestation=\"\" departements_prestation=\"\" informatique=\"\" passe=\"passe\" emails=\"$email\" en_ligne=\"\" a_lots=\"\" invisible=\"\"/&gt;&lt;/chargement_consultation_error&gt;\n" +
                "</return>\n" +
                "        </ns1:supprimer_consultation_logResponse>\n" +
                "    </SOAP-ENV:Body>\n" +
                "</SOAP-ENV:Envelope>"

        try {
            MSUtils.parseToResponseType(response, MSUtils.CONSULTATION_TYPE, BindingKeyAction.DELETE.value)
        } catch (e: BadDceError) {
            assertThat(e.message).isEqualTo(
                "Unable to process to consultation deletion in Marchés Sécurisés beacause of following error : Bad Dce. Please check ms.deadletter queue")
        }
    }

    @Test
    fun `delete consultation with bad pa response parsing test`() {
        val response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ns1=\"https://www.marches-securises.fr/webserv/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:SOAP-ENC=\"http://schemas.xmlsoap.org/soap/encoding/\" SOAP-ENV:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" +
                "    <SOAP-ENV:Body>\n" +
                "        <ns1:supprimer_consultation_logResponse>\n" +
                "            <return xsi:type=\"xsd:string\">&lt;?xml version=\"1.0\" encoding=\"UTF-8\"?&gt;\n" +
                "&lt;pa_error xmlns=\"interbat/pa_refuse\"&gt;&lt;pa_suppr_dce_error cle=\"\" reference=\"\" objet=\"\" date_creation=\"\" date_publication=\"\" date_publication_f=\"\" date_cloture=\"\" date_cloture_f=\"\" ref_interne=\"\" nomenclature_interne=\"\" finalite_marche=\"\" type_marche=\"\" type_prestation=\"\" departements_prestation=\"\" informatique=\"\" passe=\"\" emails=\"b.orihuela@sictiam.fr\" en_ligne=\"\" a_lots=\"\" invisible=\"\"/&gt;&lt;/pa_error&gt;\n" +
                "</return>\n" +
                "        </ns1:supprimer_consultation_logResponse>\n" +
                "    </SOAP-ENV:Body>\n" +
                "</SOAP-ENV:Envelope>"

        try {
            MSUtils.parseToResponseType(response, MSUtils.CONSULTATION_TYPE, BindingKeyAction.DELETE.value)
        } catch (e: BadPaError) {
            assertThat(e.message).isEqualTo(
                "Unable to process to consultation deletion in Marchés Sécurisés beacause of following error : Bad Pa. Please check ms.deadletter queue")
        }
    }

    @Test
    fun `delete consultation with bad login response parsing test`() {
        val response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ns1=\"https://www.marches-securises.fr/webserv/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:SOAP-ENC=\"http://schemas.xmlsoap.org/soap/encoding/\" SOAP-ENV:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" +
                "    <SOAP-ENV:Body>\n" +
                "        <ns1:supprimer_consultation_logResponse>\n" +
                "            <return xsi:type=\"xsd:string\">&lt;?xml version=\"1.0\" encoding=\"UTF-8\"?&gt;\n" +
                "&lt;log_error xmlns=\"interbat/log_refuse\"&gt;&lt;log_error cle=\"\" reference=\"\" objet=\"\" date_creation=\"\" date_publication=\"\" date_publication_f=\"\" date_cloture=\"\" date_cloture_f=\"\" ref_interne=\"\" nomenclature_interne=\"\" finalite_marche=\"\" type_marche=\"\" type_prestation=\"\" departements_prestation=\"\" informatique=\"\" passe=\"\" emails=\"b.orihuela@sictiam.fr\" en_ligne=\"\" a_lots=\"\" invisible=\"\"/&gt;&lt;/log_error&gt;\n" +
                "</return>\n" +
                "        </ns1:supprimer_consultation_logResponse>\n" +
                "    </SOAP-ENV:Body>\n" +
                "</SOAP-ENV:Envelope>"

        try {
            MSUtils.parseToResponseType(response, MSUtils.CONSULTATION_TYPE, BindingKeyAction.DELETE.value)
        } catch (e: BadLogError) {
            assertThat(e.message).isEqualTo(
                "Unable to process to consultation deletion in Marchés Sécurisés beacause of following error : unknown login/password. Please check ms.deadletter queue")
        }
    }

    @Test
    fun `correct check consultation response test`() {
        val response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ns1=\"https://www.marches-securises.fr/webserv/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:SOAP-ENC=\"http://schemas.xmlsoap.org/soap/encoding/\" SOAP-ENV:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" +
                "    <SOAP-ENV:Body>\n" +
                "        <ns1:verifier_informations_logResponse>\n" +
                "            <return xsi:type=\"xsd:string\">&lt;?xml version=\"1.0\" encoding=\"UTF-8\"?&gt;\n" +
                "&lt;ifw:data xmlns:ifw=\"interbat/framwork-exportation\"&gt;\n" +
                "  &lt;objet type=\"ms_v2__fullweb_dce\"&gt;\n" +
                "    &lt;propriete nom=\"cle\"&gt;$dce&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"reference\"&gt;$msReference&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"objet\"&gt;$objet&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"date_creation\"&gt;$datePublication&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"date_publication\"&gt;$datePublication&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"date_publication_f\"&gt;${LocalDateTime.now()}&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"date_cloture\"&gt;$dateCloture&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"date_cloture_f\"&gt;${LocalDateTime.now()}&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"ref_interne\"&gt;$reference&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"nomenclature_interne\"&gt;webserv_$pa&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"finalite_marche\"&gt;$finaliteMarche&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"type_marche\"&gt;$typeMarche&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"type_prestation\"&gt;$prestation&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"departements_prestation\"&gt;$departement&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"informatique\"&gt;$informatique&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"passe\"&gt;passe&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"emails\"&gt;$email&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"en_ligne\"&gt;$enligne&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"a_lots\"&gt;$alloti&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"invisible\"&gt;0&lt;/propriete&gt;\n" +
                "  &lt;/objet&gt;\n" +
                "&lt;/ifw:data&gt;\n" +
                "</return>\n" +
                "        </ns1:verifier_informations_logResponse>\n" +
                "    </SOAP-ENV:Body>\n" +
                "</SOAP-ENV:Envelope>"

        val responseObject =
            MSUtils.parseToResponseType(response, MSUtils.CONSULTATION_TYPE, BindingKeyAction.CHECK.value)

        assertThat(responseObject).isNotNull
        assertThat(responseObject.properties!!.size).isEqualTo(20)
        assertThat(responseObject.properties!![0].name).isEqualTo("cle")
        assertThat(responseObject.properties!![0].value).isEqualTo(dce)
        assertThat(responseObject.properties!![9].name).isEqualTo("nomenclature_interne")
    }

    @Test
    fun `rejected check consultation response parsing test`() {
        val response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ns1=\"https://www.marches-securises.fr/webserv/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:SOAP-ENC=\"http://schemas.xmlsoap.org/soap/encoding/\" SOAP-ENV:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" +
                "    <SOAP-ENV:Body>\n" +
                "        <ns1:verifier_informations_logResponse>\n" +
                "            <return xsi:type=\"xsd:string\">&lt;?xml version=\"1.0\" encoding=\"UTF-8\"?&gt;\n" +
                "&lt;validation_error xmlns=\"interbat/validation_refusee\"&gt;&lt;validation_erreur erreur_0=\"La_date_de_cloture_n'est_pas_valide\" cle=\"$dce\" reference=\"F-SICTIAM_06_20180905W2_01\" objet=\"$objet\" date_creation=\"$datePublication\" date_publication=\"$datePublication\" date_publication_f=\"${LocalDateTime.now()}\" date_cloture=\"$dateCloture\" date_cloture_f=\"${LocalDateTime.now()}\" ref_interne=\"$reference\" nomenclature_interne=\"webserv_$pa\" finalite_marche=\"$finaliteMarche\" type_marche=\"$typeMarche\" type_prestation=\"$prestation\" departements_prestation=\"$departement\" informatique=\"$informatique\" passe=\"passe\" emails=\"$email\" en_ligne=\"$enligne\" a_lots=\"$alloti\" invisible=\"0\"/&gt;&lt;/validation_error&gt;\n" +
                "</return>\n" +
                "        </ns1:verifier_informations_logResponse>\n" +
                "    </SOAP-ENV:Body>\n" +
                "</SOAP-ENV:Envelope>"

        try {
            MSUtils.parseToResponseType(response, MSUtils.CONSULTATION_TYPE, BindingKeyAction.CHECK.value)
        } catch (e: ConsultationRejectedError) {
            assertThat(e.message).contains("La_date_de_cloture_n'est_pas_valide")
            assertThat(e.message).contains(dce)
            assertThat(e.message).contains(reference)
            assertThat(e.message).contains(objet)
        }
    }

    @Test
    fun `check consultation with bad dce response parsing test`() {
        val response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ns1=\"https://www.marches-securises.fr/webserv/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:SOAP-ENC=\"http://schemas.xmlsoap.org/soap/encoding/\" SOAP-ENV:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" +
                "    <SOAP-ENV:Body>\n" +
                "        <ns1:verifier_informations_logResponse>\n" +
                "            <return xsi:type=\"xsd:string\">&lt;?xml version=\"1.0\" encoding=\"UTF-8\"?&gt;\n" +
                "&lt;dce_error xmlns=\"interbat/dce_refusee\"&gt;&lt;dce_error cle=\"$dce\" reference=\"\" objet=\"\" date_creation=\"$datePublication\" date_publication=\"\" date_publication_f=\"${LocalDateTime.now()}\" date_cloture=\"\" date_cloture_f=\"${LocalDateTime.now()}\" ref_interne=\"\" nomenclature_interne=\"\" finalite_marche=\"\" type_marche=\"\" type_prestation=\"\" departements_prestation=\"\" informatique=\"\" passe=\"passe\" emails=\"\" en_ligne=\"\" a_lots=\"\" invisible=\"\"/&gt;&lt;/dce_error&gt;\n" +
                "</return>\n" +
                "        </ns1:verifier_informations_logResponse>\n" +
                "    </SOAP-ENV:Body>\n" +
                "</SOAP-ENV:Envelope>"

        try {
            MSUtils.parseToResponseType(response, MSUtils.CONSULTATION_TYPE, BindingKeyAction.CHECK.value)
        } catch (e: BadDceError) {
            assertThat(e.message).isEqualTo(
                "Unable to process to consultation publication in Marchés Sécurisés beacause of following error : Bad Dce. Please check ms.deadletter queue")
        }
    }

    @Test
    fun `checkConsultation with bad pa response parsing test`() {
        val response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ns1=\"https://www.marches-securises.fr/webserv/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:SOAP-ENC=\"http://schemas.xmlsoap.org/soap/encoding/\" SOAP-ENV:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" +
                "    <SOAP-ENV:Body>\n" +
                "        <ns1:verifier_informations_logResponse>\n" +
                "            <return xsi:type=\"xsd:string\">&lt;?xml version=\"1.0\" encoding=\"UTF-8\"?&gt;\n" +
                "&lt;pa_error xmlns=\"interbat/pa_refusee\"&gt;&lt;pa_error cle=\"\" reference=\"\" objet=\"\" date_creation=\"\" date_publication=\"\" date_publication_f=\"\" date_cloture=\"\" date_cloture_f=\"\" ref_interne=\"\" nomenclature_interne=\"\" finalite_marche=\"\" type_marche=\"\" type_prestation=\"\" departements_prestation=\"\" informatique=\"\" passe=\"\" emails=\"b.orihuela@sictiam.fr\" en_ligne=\"\" a_lots=\"\" invisible=\"\"/&gt;&lt;/pa_error&gt;\n" +
                "</return>\n" +
                "        </ns1:verifier_informations_logResponse>\n" +
                "    </SOAP-ENV:Body>\n" +
                "</SOAP-ENV:Envelope>"

        try {
            MSUtils.parseToResponseType(response, MSUtils.CONSULTATION_TYPE, BindingKeyAction.CHECK.value)
        } catch (e: BadPaError) {
            assertThat(e.message).isEqualTo(
                "Unable to process to consultation publication in Marchés Sécurisés beacause of following error : Bad Pa. Please check ms.deadletter queue")
        }
    }

    @Test
    fun `check consultation with bad log response parsing test`() {
        val response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ns1=\"https://www.marches-securises.fr/webserv/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:SOAP-ENC=\"http://schemas.xmlsoap.org/soap/encoding/\" SOAP-ENV:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" +
                "    <SOAP-ENV:Body>\n" +
                "        <ns1:verifier_informations_logResponse>\n" +
                "            <return xsi:type=\"xsd:string\">&lt;?xml version=\"1.0\" encoding=\"UTF-8\"?&gt;\n" +
                "&lt;identification_error xmlns=\"interbat/identification_refusee\"&gt;&lt;identification_error cle=\"\" reference=\"\" objet=\"\" date_creation=\"\" date_publication=\"\" date_publication_f=\"\" date_cloture=\"\" date_cloture_f=\"\" ref_interne=\"\" nomenclature_interne=\"\" finalite_marche=\"\" type_marche=\"\" type_prestation=\"\" departements_prestation=\"\" informatique=\"\" passe=\"\" emails=\"b.orihuela@sictiam.fr\" en_ligne=\"\" a_lots=\"\" invisible=\"\"/&gt;&lt;/identification_error&gt;\n" +
                "</return>\n" +
                "        </ns1:verifier_informations_logResponse>\n" +
                "    </SOAP-ENV:Body>\n" +
                "</SOAP-ENV:Envelope>"

        try {
            MSUtils.parseToResponseType(response, MSUtils.CONSULTATION_TYPE, BindingKeyAction.CHECK.value)
        } catch (e: BadLogError) {
            assertThat(e.message).isEqualTo(
                "Unable to process to consultation publication in Marchés Sécurisés beacause of following error : unknown login/password. Please check ms.deadletter queue")
        }
    }

    @Test
    fun `correct publish consultation response parsing test`() {
        val response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ns1=\"https://www.marches-securises.fr/webserv/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:SOAP-ENC=\"http://schemas.xmlsoap.org/soap/encoding/\" SOAP-ENV:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" +
                "    <SOAP-ENV:Body>\n" +
                "        <ns1:publication_dce_logResponse>\n" +
                "            <return xsi:type=\"xsd:string\">&lt;?xml version=\"1.0\" encoding=\"UTF-8\"?&gt;\n" +
                "&lt;ifw:data xmlns:ifw=\"interbat/framwork-exportation\"&gt;\n" +
                "  &lt;objet type=\"ms_v2__fullweb_dce\"&gt;\n" +
                "    &lt;propriete nom=\"cle\"&gt;$dce&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"reference\"&gt;$msReference&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"objet\"&gt;$objet&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"date_creation\"&gt;$datePublication&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"date_publication\"&gt;$datePublication&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"date_publication_f\"&gt;${LocalDateTime.now()}&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"date_cloture\"&gt;$dateCloture&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"date_cloture_f\"&gt;${LocalDateTime.now()}&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"ref_interne\"&gt;$reference&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"nomenclature_interne\"&gt;webserv_$pa&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"finalite_marche\"&gt;$finaliteMarche&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"type_marche\"&gt;$typeMarche&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"type_prestation\"&gt;$prestation&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"departements_prestation\"&gt;$departement&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"informatique\"&gt;$informatique&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"passe\"&gt;passe&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"emails\"&gt;$email&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"en_ligne\"&gt;$enligne&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"a_lots\"&gt;$alloti&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"invisible\"&gt;0&lt;/propriete&gt;\n" +
                "  &lt;/objet&gt;\n" +
                "&lt;/ifw:data&gt;\n" +
                "</return>\n" +
                "        </ns1:publication_dce_logResponse>\n" +
                "    </SOAP-ENV:Body>\n" +
                "</SOAP-ENV:Envelope>"

        val responseObject =
            MSUtils.parseToResponseType(response, MSUtils.CONSULTATION_TYPE, BindingKeyAction.PUBLISH.value)
        assertThat(responseObject).isNotNull
        assertThat(responseObject.properties!!.size).isEqualTo(20)
        assertThat(responseObject.properties!![0].name).isEqualTo("cle")
        assertThat(responseObject.properties!![0].value).isEqualTo(dce)

    }

    @Test
    fun `error on publish consultation response parsing test`() {
        val response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ns1=\"https://www.marches-securises.fr/webserv/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:SOAP-ENC=\"http://schemas.xmlsoap.org/soap/encoding/\" SOAP-ENV:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" +
                "    <SOAP-ENV:Body>\n" +
                "        <ns1:publication_dce_logResponse>\n" +
                "            <return xsi:type=\"xsd:string\">&lt;?xml version=\"1.0\" encoding=\"UTF-8\"?&gt;\n" +
                "&lt;publication_error xmlns=\"interbat/publication_refusee\"&gt;&lt;publication_refusee cle=\"$dce\" reference=\"F-SICTIAM_06_20180905W2_01\" objet=\"$objet\" date_creation=\"$datePublication\" date_publication=\"$datePublication\" date_publication_f=\"${LocalDateTime.now()}\" date_cloture=\"$dateCloture\" date_cloture_f=\"${LocalDateTime.now()}\" ref_interne=\"$reference\" nomenclature_interne=\"webserv_$pa\" finalite_marche=\"$finaliteMarche\" type_marche=\"$typeMarche\" type_prestation=\"$prestation\" departements_prestation=\"$departement\" informatique=\"$informatique\" passe=\"passe\" emails=\"$email\" en_ligne=\"$enligne\" a_lots=\"$alloti\" invisible=\"0\"/&gt;&lt;/publication_error&gt;\n" +
                "</return>\n" +
                "        </ns1:publication_dce_logResponse>\n" +
                "    </SOAP-ENV:Body>\n" +
                "</SOAP-ENV:Envelope>"

        try {
            MSUtils.parseToResponseType(response, MSUtils.CONSULTATION_TYPE, BindingKeyAction.PUBLISH.value)
        } catch (e: ConsultationRejectedError) {
            assertThat(e.message).contains(dce)
            assertThat(e.message).contains(reference)
            assertThat(e.message).contains(objet)
        }
    }

    @Test
    fun `correct create lot response parsing`() {
        val response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ns1=\"https://www.marches-securises.fr/webserv/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:SOAP-ENC=\"http://schemas.xmlsoap.org/soap/encoding/\" SOAP-ENV:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" +
                "    <SOAP-ENV:Body>\n" +
                "        <ns1:creer_lot_consultation_logResponse>\n" +
                "            <return xsi:type=\"xsd:string\">&lt;?xml version=\"1.0\" encoding=\"UTF-8\"?&gt;\n" +
                "&lt;ifw:data xmlns:ifw=\"interbat/framwork-exportation\"&gt;\n" +
                "  &lt;objet type=\"ms_v2__fullweb_lot\"&gt;\n" +
                "    &lt;propriete nom=\"cle\"&gt;1536572210acqajjp4r3pj&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"cle_lot\"&gt;1536572210acqajjp4r3pj&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"cle_dce\"&gt;$dce&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"numero\"&gt;2&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"libelle\"&gt;Lot de test 1&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"ordre\"&gt;3&lt;/propriete&gt;\n" +
                "  &lt;nombre_lots nom=\"nb_lot\"&gt;2&lt;/nombre_lots&gt;&lt;/objet&gt;\n" +
                "  &lt;objet type=\"ms_v2__fullweb_lot\"&gt;\n" +
                "    &lt;propriete nom=\"cle\"&gt;$cleLot&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"cle_lot\"&gt;$cleLot&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"cle_dce\"&gt;$dce&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"numero\"&gt;$numeroLot&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"libelle\"&gt;$libelleLot&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"ordre\"&gt;$ordreLot&lt;/propriete&gt;\n" +
                "  &lt;nombre_lots nom=\"nb_lot\"&gt;2&lt;/nombre_lots&gt;&lt;/objet&gt;\n" +
                "&lt;/ifw:data&gt;\n" +
                "</return>\n" +
                "        </ns1:creer_lot_consultation_logResponse>\n" +
                "    </SOAP-ENV:Body>\n" +
                "</SOAP-ENV:Envelope>"

        val responseObject =
            MSUtils.parseToResponseType(response, MSUtils.LOT_TYPE, BindingKeyAction.CREATE.value, ordreLot)

        assertThat(responseObject).isNotNull
        assertThat(responseObject.lotNbr).isEqualTo(2)
        assertThat(responseObject.properties!!.size).isGreaterThanOrEqualTo(6)
        assertThat(responseObject.properties!![0].name).isEqualTo("cle")
        assertThat(responseObject.properties!![0].value).isEqualTo(cleLot)
        assertThat(responseObject.properties!![2].name).isEqualTo("cle_dce")
        assertThat(responseObject.properties!![2].value).isEqualTo(dce)
        assertThat(responseObject.properties!![5].name).isEqualTo("ordre")
        assertThat(responseObject.properties!![5].value).isEqualTo(ordreLot)
        assertThat(responseObject.properties!!.find { p -> p.value == "error" }).isNull()
    }

    @Test
    fun `create lot with bad dce response parsing test`() {
        val response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ns1=\"https://www.marches-securises.fr/webserv/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:SOAP-ENC=\"http://schemas.xmlsoap.org/soap/encoding/\" SOAP-ENV:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" +
                "    <SOAP-ENV:Body>\n" +
                "        <ns1:creer_lot_consultation_logResponse>\n" +
                "            <return xsi:type=\"xsd:string\">&lt;?xml version=\"1.0\" encoding=\"UTF-8\"?&gt;\n" +
                "&lt;ifw:data xmlns:ifw=\"interbat/framwork-exportation\"&gt;\n" +
                "  &lt;objet type=\"error\"&gt;\n" +
                "    &lt;propriete nom=\"load_dce_error\"&gt;error&lt;/propriete&gt;\n" +
                "  &lt;/objet&gt;\n" +
                "&lt;/ifw:data&gt;\n" +
                "</return>\n" +
                "        </ns1:creer_lot_consultation_logResponse>\n" +
                "    </SOAP-ENV:Body>\n" +
                "</SOAP-ENV:Envelope>"

        val responseObject =
            MSUtils.parseToResponseType(response, MSUtils.LOT_TYPE, BindingKeyAction.CREATE.value, ordreLot)
        assertThat(responseObject).isNotNull
        assertThat(responseObject.type).isEqualTo("error")
        assertThat(responseObject.properties!!.size).isGreaterThan(0)
        assertThat(responseObject.properties!![0].name).isEqualTo("load_dce_error")
        assertThat(responseObject.properties!![0].value).isEqualTo("error")
    }

    @Test
    fun `create lot with bad pa response parsing test`() {
        val response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ns1=\"https://www.marches-securises.fr/webserv/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:SOAP-ENC=\"http://schemas.xmlsoap.org/soap/encoding/\" SOAP-ENV:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" +
                "    <SOAP-ENV:Body>\n" +
                "        <ns1:creer_lot_consultation_logResponse>\n" +
                "            <return xsi:type=\"xsd:string\">&lt;?xml version=\"1.0\" encoding=\"UTF-8\"?&gt;\n" +
                "&lt;ifw:data xmlns:ifw=\"interbat/framwork-exportation\"&gt;\n" +
                "  &lt;objet type=\"error\"&gt;\n" +
                "    &lt;propriete nom=\"load_pa_error\"&gt;error&lt;/propriete&gt;\n" +
                "  &lt;/objet&gt;\n" +
                "&lt;/ifw:data&gt;\n" +
                "</return>\n" +
                "        </ns1:creer_lot_consultation_logResponse>\n" +
                "    </SOAP-ENV:Body>\n" +
                "</SOAP-ENV:Envelope>"

        val responseObject = MSUtils.parseToResponseType(response, MSUtils.LOT_TYPE, BindingKeyAction.CREATE.value)
        assertThat(responseObject).isNotNull
        assertThat(responseObject.type).isEqualTo("error")
        assertThat(responseObject.properties!!.size).isGreaterThan(0)
        assertThat(responseObject.properties!![0].name).isEqualTo("load_pa_error")
        assertThat(responseObject.properties!![0].value).isEqualTo("error")
    }

    @Test
    fun `create lot with bad log response parsing test`() {
        val response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ns1=\"https://www.marches-securises.fr/webserv/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:SOAP-ENC=\"http://schemas.xmlsoap.org/soap/encoding/\" SOAP-ENV:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" +
                "    <SOAP-ENV:Body>\n" +
                "        <ns1:creer_lot_consultation_logResponse>\n" +
                "            <return xsi:nil=\"true\"/>\n" +
                "        </ns1:creer_lot_consultation_logResponse>\n" +
                "    </SOAP-ENV:Body>\n" +
                "</SOAP-ENV:Envelope>"

        try {
            MSUtils.parseToResponseType(response, MSUtils.LOT_TYPE, BindingKeyAction.CREATE.value)
        } catch (e: BadLogError) {
            assertThat(e.message).isEqualTo(
                "Unable to process marchepublic:lot_0 ${BindingKeyAction.CREATE.value} request for following reason : Unknown login/password.")
        }
    }

    @Test
    fun `correct update lot response parsing test`() {
        val response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ns1=\"https://www.marches-securises.fr/webserv/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:SOAP-ENC=\"http://schemas.xmlsoap.org/soap/encoding/\" SOAP-ENV:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" +
                "    <SOAP-ENV:Body>\n" +
                "        <ns1:modifier_lot_consultation_logResponse>\n" +
                "            <return xsi:type=\"xsd:string\">&lt;?xml version=\"1.0\" encoding=\"UTF-8\"?&gt;\n" +
                "&lt;ifw:data xmlns:ifw=\"interbat/framwork-exportation\"&gt;\n" +
                "  &lt;objet type=\"ms_v2__fullweb_lot\"&gt;\n" +
                "    &lt;propriete nom=\"cle\"&gt;$cleLot&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"cle_dce\"&gt;$dce&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"numero\" statut=\"changed\"&gt;$numeroLot&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"libelle\" statut=\"changed\"&gt;$libelleLot&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"ordre\"&gt;$ordreLot&lt;/propriete&gt;\n" +
                "  &lt;nombre_lots nom=\"nb_lot\"&gt;2&lt;/nombre_lots&gt;&lt;/objet&gt;\n" +
                "&lt;/ifw:data&gt;\n" +
                "</return>\n" +
                "        </ns1:modifier_lot_consultation_logResponse>\n" +
                "    </SOAP-ENV:Body>\n" +
                "</SOAP-ENV:Envelope>"

        val responseObject =
            MSUtils.parseToResponseType(response, MSUtils.LOT_TYPE, BindingKeyAction.UPDATE.value, ordreLot)

        assertThat(responseObject).isNotNull
        assertThat(responseObject.lotNbr).isEqualTo(2)
        assertThat(responseObject.properties!!.size).isEqualTo(5)
        assertThat(responseObject.properties!![0].name).isEqualTo("cle")
        assertThat(responseObject.properties!![0].value).isEqualTo(cleLot)
        assertThat(responseObject.properties!![1].name).isEqualTo("cle_dce")
        assertThat(responseObject.properties!![1].value).isEqualTo(dce)
        assertThat(responseObject.properties!![3].status).isEqualTo("changed")
        assertThat(responseObject.properties!![3].value).isEqualTo(libelleLot)
        assertThat(responseObject.properties!![4].name).isEqualTo("ordre")
        assertThat(responseObject.properties!![4].value).isEqualTo(ordreLot)
        val isLoadError = responseObject.properties!!.find { p -> p.name == "load_lot_error" }
        assertThat(isLoadError).isNull()
        assertThat(responseObject.properties!!.find { p -> p.value == "error" }).isNull()
    }

    @Test
    fun `update lot with bad cleLot response parsing test`() {
        val response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ns1=\"https://www.marches-securises.fr/webserv/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:SOAP-ENC=\"http://schemas.xmlsoap.org/soap/encoding/\" SOAP-ENV:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" +
                "    <SOAP-ENV:Body>\n" +
                "        <ns1:modifier_lot_consultation_logResponse>\n" +
                "            <return xsi:type=\"xsd:string\">&lt;?xml version=\"1.0\" encoding=\"UTF-8\"?&gt;\n" +
                "&lt;ifw:data xmlns:ifw=\"interbat/framwork-exportation\"&gt;\n" +
                "  &lt;objet type=\"ms_v2__fullweb_lot\"&gt;\n" +
                "    &lt;propriete nom=\"cle\"&gt;$cleLot&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"cle_lot\"&gt;$cleLot&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"cle_dce\"&gt;$dce&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"numero\"&gt;$numeroLot&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"libelle\"&gt;$libelleLot&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"ordre\"&gt;$ordreLot&lt;/propriete&gt;\n" +
                "  &lt;propriete nom=\"load_lot_error\"&gt;error&lt;/propriete&gt;&lt;nombre_lots nom=\"nb_lot\"&gt;2&lt;/nombre_lots&gt;&lt;/objet&gt;\n" +
                "  &lt;objet type=\"ms_v2__fullweb_lot\"&gt;\n" +
                "    &lt;propriete nom=\"cle\"&gt;1536329571x14fr67uq33u&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"cle_lot\"&gt;1536329571x14fr67uq33u&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"cle_dce\"&gt;15363016768hx4muk2ched&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"numero\"&gt;2&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"libelle\"&gt;Lot de test&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"ordre\"&gt;2&lt;/propriete&gt;\n" +
                "  &lt;propriete nom=\"load_lot_error\"&gt;error&lt;/propriete&gt;&lt;nombre_lots nom=\"nb_lot\"&gt;2&lt;/nombre_lots&gt;&lt;/objet&gt;\n" +
                "&lt;/ifw:data&gt;\n" +
                "</return>\n" +
                "        </ns1:modifier_lot_consultation_logResponse>\n" +
                "    </SOAP-ENV:Body>\n" +
                "</SOAP-ENV:Envelope>"

        val responseObject =
            MSUtils.parseToResponseType(response, MSUtils.LOT_TYPE, BindingKeyAction.UPDATE.value, ordreLot)

        assertThat(responseObject).isNotNull
        assertThat(responseObject.properties!!.size).isGreaterThanOrEqualTo(7)
        assertThat(responseObject.properties!![6].name).isEqualTo("load_lot_error")
        assertThat(responseObject.properties!![6].value).isEqualTo("error")
        val isLoadError = responseObject.properties!!.find { p -> p.name == "load_lot_error" }
        assertThat(isLoadError).isNotNull
    }

    @Test
    fun `update lot with bad dce response parsing test`() {
        val response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ns1=\"https://www.marches-securises.fr/webserv/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:SOAP-ENC=\"http://schemas.xmlsoap.org/soap/encoding/\" SOAP-ENV:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" +
                "    <SOAP-ENV:Body>\n" +
                "        <ns1:modifier_lot_consultation_logResponse>\n" +
                "            <return xsi:type=\"xsd:string\">&lt;?xml version=\"1.0\" encoding=\"UTF-8\"?&gt;\n" +
                "&lt;ifw:data xmlns:ifw=\"interbat/framwork-exportation\"&gt;\n" +
                "  &lt;objet type=\"error\"&gt;\n" +
                "    &lt;propriete nom=\"load_dce_error\"&gt;error&lt;/propriete&gt;\n" +
                "  &lt;/objet&gt;\n" +
                "&lt;/ifw:data&gt;\n" +
                "</return>\n" +
                "        </ns1:modifier_lot_consultation_logResponse>\n" +
                "    </SOAP-ENV:Body>\n" +
                "</SOAP-ENV:Envelope>"

        val responseObject =
            MSUtils.parseToResponseType(response, MSUtils.LOT_TYPE, BindingKeyAction.UPDATE.value, ordreLot)

        assertThat(responseObject).isNotNull
        assertThat(responseObject.properties!!.size).isGreaterThanOrEqualTo(1)
        assertThat(responseObject.properties!![0].name).isEqualTo("load_dce_error")
        assertThat(responseObject.properties!![0].value).isEqualTo("error")
    }

    @Test
    fun `update lot with bad pa response parsing test`() {
        val response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ns1=\"https://www.marches-securises.fr/webserv/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:SOAP-ENC=\"http://schemas.xmlsoap.org/soap/encoding/\" SOAP-ENV:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" +
                "    <SOAP-ENV:Body>\n" +
                "        <ns1:modifier_lot_consultation_logResponse>\n" +
                "            <return xsi:type=\"xsd:string\">&lt;?xml version=\"1.0\" encoding=\"UTF-8\"?&gt;\n" +
                "&lt;ifw:data xmlns:ifw=\"interbat/framwork-exportation\"&gt;\n" +
                "  &lt;objet type=\"error\"&gt;\n" +
                "    &lt;propriete nom=\"load_pa_error\"&gt;error&lt;/propriete&gt;\n" +
                "  &lt;/objet&gt;\n" +
                "&lt;/ifw:data&gt;\n" +
                "</return>\n" +
                "        </ns1:modifier_lot_consultation_logResponse>\n" +
                "    </SOAP-ENV:Body>\n" +
                "</SOAP-ENV:Envelope>"

        val responseObject =
            MSUtils.parseToResponseType(response, MSUtils.LOT_TYPE, BindingKeyAction.UPDATE.value, ordreLot)

        assertThat(responseObject).isNotNull
        assertThat(responseObject.properties!!.size).isGreaterThanOrEqualTo(1)
        assertThat(responseObject.properties!![0].name).isEqualTo("load_pa_error")
        assertThat(responseObject.properties!![0].value).isEqualTo("error")
    }

    @Test
    fun `update lot with bad log response parsing test`() {
        val response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ns1=\"https://www.marches-securises.fr/webserv/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:SOAP-ENC=\"http://schemas.xmlsoap.org/soap/encoding/\" SOAP-ENV:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" +
                "    <SOAP-ENV:Body>\n" +
                "        <ns1:modifier_lot_consultation_logResponse>\n" +
                "            <return xsi:nil=\"true\"/>\n" +
                "        </ns1:modifier_lot_consultation_logResponse>\n" +
                "    </SOAP-ENV:Body>\n" +
                "</SOAP-ENV:Envelope>"

        try {
            MSUtils.parseToResponseType(response, MSUtils.LOT_TYPE, BindingKeyAction.UPDATE.value, ordreLot)
        } catch (e: BadLogError) {
            assertThat(e.message).isEqualTo(
                "Unable to process marchepublic:lot_0 ${BindingKeyAction.UPDATE.value} request for following reason : Unknown login/password.")
        }
    }

    @Test
    fun `correct delete lot response parsing test`() {
        val response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ns1=\"https://www.marches-securises.fr/webserv/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:SOAP-ENC=\"http://schemas.xmlsoap.org/soap/encoding/\" SOAP-ENV:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" +
                "    <SOAP-ENV:Body>\n" +
                "        <ns1:supprimer_lot_consultation_logResponse>\n" +
                "            <return xsi:type=\"xsd:string\">&lt;?xml version=\"1.0\" encoding=\"UTF-8\"?&gt;\n" +
                "&lt;ifw:data xmlns:ifw=\"interbat/framwork-exportation\"&gt;\n" +
                "  &lt;objet type=\"ms_v2__fullweb_lot\"&gt;\n" +
                "    &lt;propriete nom=\"cle\"&gt;1536329571x14fr67uq33u&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"cle_lot\"&gt;1536329571x14fr67uq33u&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"cle_dce\"&gt;$dce&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"numero\"&gt;2&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"libelle\"&gt;Lot de test&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"ordre\"&gt;2&lt;/propriete&gt;\n" +
                "  &lt;nombre_lots nom=\"nb_lot\"&gt;1&lt;/nombre_lots&gt;&lt;/objet&gt;\n" +
                "&lt;/ifw:data&gt;\n" +
                "</return>\n" +
                "        </ns1:supprimer_lot_consultation_logResponse>\n" +
                "    </SOAP-ENV:Body>\n" +
                "</SOAP-ENV:Envelope>"

        val responseObject =
            MSUtils.parseToResponseType(response, MSUtils.LOT_TYPE, BindingKeyAction.DELETE.value, cleLot)

        assertThat(responseObject).isNotNull
        assertThat(responseObject.properties!!.size).isGreaterThanOrEqualTo(5)
        assertThat(responseObject.properties!![2].name).isEqualTo("cle_dce")
        assertThat(responseObject.properties!![2].value).isEqualTo(dce)
        assertThat(responseObject.properties!!.find { p -> p.value == "error" }).isNull()
        assertThat(responseObject.properties!!.find { p -> p.value == cleLot }).isNull()
        assertThat(responseObject.properties!!.find { p -> p.value == ordreLot }).isNull()
    }

    @Test
    fun `correct delete of last lot parsing test`() {
        val response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ns1=\"https://www.marches-securises.fr/webserv/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:SOAP-ENC=\"http://schemas.xmlsoap.org/soap/encoding/\" SOAP-ENV:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" +
                "    <SOAP-ENV:Body>\n" +
                "        <ns1:supprimer_lot_consultation_logResponse>\n" +
                "            <return xsi:type=\"xsd:string\">&lt;?xml version=\"1.0\" encoding=\"UTF-8\"?&gt;\n" +
                "&lt;ifw:data xmlns:ifw=\"interbat/framwork-exportation\"&gt;\n" +
                "  &lt;objet lot=\"last\"&gt;\n" +
                "    &lt;propriete suppression=\"true\"&gt;supprime&lt;/propriete&gt;\n" +
                "    &lt;nombre_lots nom=\"nb_lot\"&gt;0&lt;/nombre_lots&gt;\n" +
                "  &lt;/objet&gt;\n" +
                "&lt;/ifw:data&gt;\n" +
                "</return>\n" +
                "        </ns1:supprimer_lot_consultation_logResponse>\n" +
                "    </SOAP-ENV:Body>\n" +
                "</SOAP-ENV:Envelope>"

        val responseObject =
            MSUtils.parseToResponseType(response, MSUtils.LOT_TYPE, BindingKeyAction.DELETE.value, cleLot)

        assertThat(responseObject).isNotNull
        assertThat(responseObject.properties!!.size).isGreaterThanOrEqualTo(1)
        assertThat(responseObject.properties!![0].suppression).isEqualTo(true)
        assertThat(responseObject.properties!![0].value).isEqualTo("supprime")
        assertThat(responseObject.properties!!.find { p -> p.value == "error" }).isNull()
        assertThat(responseObject.properties!!.find { p -> p.value == cleLot }).isNull()
        assertThat(responseObject.properties!!.find { p -> p.value == ordreLot }).isNull()
    }

    @Test
    fun `lot delete with bad cleLot response parsing test`() {
        val response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ns1=\"https://www.marches-securises.fr/webserv/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:SOAP-ENC=\"http://schemas.xmlsoap.org/soap/encoding/\" SOAP-ENV:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" +
                "    <SOAP-ENV:Body>\n" +
                "        <ns1:supprimer_lot_consultation_logResponse>\n" +
                "            <return xsi:type=\"xsd:string\">&lt;?xml version=\"1.0\" encoding=\"UTF-8\"?&gt;\n" +
                "&lt;ifw:data xmlns:ifw=\"interbat/framwork-exportation\"&gt;\n" +
                "  &lt;objet type=\"error\"&gt;\n" +
                "    &lt;propriete nom=\"load_lot_error\"&gt;error&lt;/propriete&gt;\n" +
                "  &lt;/objet&gt;\n" +
                "&lt;/ifw:data&gt;\n" +
                "</return>\n" +
                "        </ns1:supprimer_lot_consultation_logResponse>\n" +
                "    </SOAP-ENV:Body>\n" +
                "</SOAP-ENV:Envelope>"

        val responseObject =
            MSUtils.parseToResponseType(response, MSUtils.LOT_TYPE, BindingKeyAction.DELETE.value, cleLot)

        assertThat(responseObject).isNotNull
        assertThat(responseObject.properties!!.size).isGreaterThanOrEqualTo(1)
        assertThat(responseObject.properties!![0].name).isEqualTo("load_lot_error")
        assertThat(responseObject.properties!![0].value).isEqualTo("error")
    }

    @Test
    fun `lot delete with bad dce response parsing test`() {
        val response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ns1=\"https://www.marches-securises.fr/webserv/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:SOAP-ENC=\"http://schemas.xmlsoap.org/soap/encoding/\" SOAP-ENV:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" +
                "    <SOAP-ENV:Body>\n" +
                "        <ns1:supprimer_lot_consultation_logResponse>\n" +
                "            <return xsi:type=\"xsd:string\">&lt;?xml version=\"1.0\" encoding=\"UTF-8\"?&gt;\n" +
                "&lt;ifw:data xmlns:ifw=\"interbat/framwork-exportation\"&gt;\n" +
                "  &lt;objet type=\"error\"&gt;\n" +
                "    &lt;propriete nom=\"load_dce_error\"&gt;error&lt;/propriete&gt;\n" +
                "  &lt;/objet&gt;\n" +
                "&lt;/ifw:data&gt;\n" +
                "</return>\n" +
                "        </ns1:supprimer_lot_consultation_logResponse>\n" +
                "    </SOAP-ENV:Body>\n" +
                "</SOAP-ENV:Envelope>"

        val responseObject =
            MSUtils.parseToResponseType(response, MSUtils.LOT_TYPE, BindingKeyAction.DELETE.value, cleLot)

        assertThat(responseObject).isNotNull
        assertThat(responseObject.properties!!.size).isGreaterThanOrEqualTo(1)
        assertThat(responseObject.properties!![0].name).isEqualTo("load_dce_error")
        assertThat(responseObject.properties!![0].value).isEqualTo("error")
    }

    @Test
    fun `lot delete with bad pa response parsing test`() {
        val response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ns1=\"https://www.marches-securises.fr/webserv/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:SOAP-ENC=\"http://schemas.xmlsoap.org/soap/encoding/\" SOAP-ENV:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" +
                "    <SOAP-ENV:Body>\n" +
                "        <ns1:supprimer_lot_consultation_logResponse>\n" +
                "            <return xsi:type=\"xsd:string\">&lt;?xml version=\"1.0\" encoding=\"UTF-8\"?&gt;\n" +
                "&lt;ifw:data xmlns:ifw=\"interbat/framwork-exportation\"&gt;\n" +
                "  &lt;objet type=\"error\"&gt;\n" +
                "    &lt;propriete nom=\"load_pa_error\"&gt;error&lt;/propriete&gt;\n" +
                "  &lt;/objet&gt;\n" +
                "&lt;/ifw:data&gt;\n" +
                "</return>\n" +
                "        </ns1:supprimer_lot_consultation_logResponse>\n" +
                "    </SOAP-ENV:Body>\n" +
                "</SOAP-ENV:Envelope>"

        val responseObject =
            MSUtils.parseToResponseType(response, MSUtils.LOT_TYPE, BindingKeyAction.DELETE.value, cleLot)

        assertThat(responseObject).isNotNull
        assertThat(responseObject.properties!!.size).isGreaterThanOrEqualTo(1)
        assertThat(responseObject.properties!![0].name).isEqualTo("load_pa_error")
        assertThat(responseObject.properties!![0].value).isEqualTo("error")
    }

    @Test
    fun `lot delete with bad log response parsing test`() {
        val response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ns1=\"https://www.marches-securises.fr/webserv/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:SOAP-ENC=\"http://schemas.xmlsoap.org/soap/encoding/\" SOAP-ENV:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" +
                "    <SOAP-ENV:Body>\n" +
                "        <ns1:supprimer_lot_consultation_logResponse>\n" +
                "            <return xsi:nil=\"true\"/>\n" +
                "        </ns1:supprimer_lot_consultation_logResponse>\n" +
                "    </SOAP-ENV:Body>\n" +
                "</SOAP-ENV:Envelope>"

        try {
            MSUtils.parseToResponseType(response, MSUtils.LOT_TYPE, BindingKeyAction.DELETE.value, cleLot)
        } catch (e: BadLogError) {
            assertThat(e.message).isEqualTo(
                "Unable to process marchepublic:lot_0 ${BindingKeyAction.DELETE.value} request for following reason : Unknown login/password.")
        }
    }

    @Test
    fun `correct create piece response parsing test`() {
        val response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ns1=\"https://www.marches-securises.fr/webserv/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:SOAP-ENC=\"http://schemas.xmlsoap.org/soap/encoding/\" SOAP-ENV:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" +
                "    <SOAP-ENV:Body>\n" +
                "        <ns1:nouveau_fichier_logResponse>\n" +
                "            <return xsi:type=\"xsd:string\">&lt;?xml version=\"1.0\" encoding=\"UTF-8\"?&gt;\n" +
                "&lt;ifw:data xmlns:ifw=\"interbat/framwork-exportation\"&gt;\n" +
                "  &lt;objet type=\"ms_v2__fullweb_piece\"&gt;\n" +
                "    &lt;propriete nom=\"cle_piece\"&gt;1536570055pl442adp662f&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"cle_dce\"&gt;$dce&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"cle_lot\"/&gt;\n" +
                "    &lt;propriete nom=\"libelle\"&gt;libelle&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"ordre\"&gt;2&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"nom\"&gt;FileName.txt&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"la\"&gt;1&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"extention\"&gt;txt&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"type_mime\"&gt;text/plain; charset=us-ascii&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"taille\"&gt;3&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"date_mise_en_ligne\"&gt;1536570055&lt;/propriete&gt;\n" +
                "  &lt;/objet&gt;\n" +
                "  &lt;objet type=\"ms_v2__fullweb_piece\"&gt;\n" +
                "    &lt;propriete nom=\"cle_piece\"&gt;$clePiece&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"cle_dce\"&gt;$dce&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"cle_lot\"/&gt;\n" +
                "    &lt;propriete nom=\"libelle\"&gt;$libellePiece&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"ordre\"&gt;$ordrePiece&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"nom\"&gt;$nom.$extension&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"la\"&gt;$la&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"extention\"&gt;$extension&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"type_mime\"&gt;text/plain; charset=us-ascii&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"taille\"&gt;4&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"date_mise_en_ligne\"&gt;1536570066&lt;/propriete&gt;\n" +
                "  &lt;/objet&gt;\n" +
                "&lt;/ifw:data&gt;\n" +
                "</return>\n" +
                "        </ns1:nouveau_fichier_logResponse>\n" +
                "    </SOAP-ENV:Body>\n" +
                "</SOAP-ENV:Envelope>"

        val responseObject =
            MSUtils.parseToResponseType(response, MSUtils.PIECE_TYPE, BindingKeyAction.CREATE.value, "$nom.$extension")

        assertThat(responseObject).isNotNull
        assertThat(responseObject.properties!!.size).isEqualTo(11)
        assertThat(responseObject.properties!![0].name).isEqualTo("cle_piece")
        assertThat(responseObject.properties!![0].value).isEqualTo(clePiece)
        assertThat(responseObject.properties!![1].name).isEqualTo("cle_dce")
        assertThat(responseObject.properties!![1].value).isEqualTo(dce)
        assertThat(responseObject.properties!![2].name).isEqualTo("cle_lot")
        assertThat(responseObject.properties!![2].value).isEmpty()
        assertThat(responseObject.properties!![5].name).isEqualTo("nom")
        assertThat(responseObject.properties!![5].value).isEqualTo("$nom.$extension")
        assertThat(responseObject.type).isNotEqualTo("error")
        assertThat(responseObject.properties!!.find { p -> p.name == "fichier_error" }).isNull()
    }

    @Test
    fun `create piece error cause file already axist response parsing test`() {
        val response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ns1=\"https://www.marches-securises.fr/webserv/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:SOAP-ENC=\"http://schemas.xmlsoap.org/soap/encoding/\" SOAP-ENV:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" +
                "    <SOAP-ENV:Body>\n" +
                "        <ns1:nouveau_fichier_logResponse>\n" +
                "            <return xsi:type=\"xsd:string\">&lt;?xml version=\"1.0\" encoding=\"UTF-8\"?&gt;\n" +
                "&lt;ifw:data xmlns:ifw=\"interbat/framwork-exportation\"&gt;\n" +
                "  &lt;objet type=\"error\"&gt;\n" +
                "    &lt;propriete nom=\"fichier_error\"&gt; file_exist&lt;/propriete&gt;\n" +
                "  &lt;/objet&gt;\n" +
                "&lt;/ifw:data&gt;\n" +
                "</return>\n" +
                "        </ns1:nouveau_fichier_logResponse>\n" +
                "    </SOAP-ENV:Body>\n" +
                "</SOAP-ENV:Envelope>"

        val responseObject =
            MSUtils.parseToResponseType(response, MSUtils.PIECE_TYPE, BindingKeyAction.CREATE.value, "$nom.$extension")

        assertThat(responseObject).isNotNull
        assertThat(responseObject.type).isEqualTo("error")
        assertThat(responseObject.properties!!.size).isEqualTo(1)
        assertThat(responseObject.properties!![0].name).isEqualTo("fichier_error")
        assertThat(responseObject.properties!![0].value).isEqualTo(" file_exist")
    }

    @Test
    fun `create piece with bad array type response parsing test`() {
        val response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
                "    <SOAP-ENV:Body>\n" +
                "        <SOAP-ENV:Fault>\n" +
                "            <faultcode>SOAP-ENV:Server</faultcode>\n" +
                "            <faultstring>Cannot use object of type stdClass as array</faultstring>\n" +
                "        </SOAP-ENV:Fault>\n" +
                "    </SOAP-ENV:Body>\n" +
                "</SOAP-ENV:Envelope>"

        try {
            MSUtils.parseToResponseType(response, MSUtils.PIECE_TYPE, BindingKeyAction.CREATE.value, "$nom.$extension")
        } catch (e: SoapParsingUnexpectedError) {
            assertThat(e.message).isEqualTo("Unknown array type. Please check SOAP request.")
        }
    }

    @Test
    fun `create piece with missing, bad named or incomplete array response parsing test`() {
        val response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ns1=\"https://www.marches-securises.fr/webserv/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:SOAP-ENC=\"http://schemas.xmlsoap.org/soap/encoding/\" SOAP-ENV:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" +
                "    <SOAP-ENV:Body>\n" +
                "        <ns1:nouveau_fichier_logResponse>\n" +
                "            <return xsi:type=\"xsd:string\">&lt;?xml version=\"1.0\" encoding=\"UTF-8\"?&gt;\n" +
                "&lt;creation_piece_error xmlns=\"interbat/erreur_tableau\"&gt;&lt;tableau_incomplet cle_pa=\"$pa\" cle_dce=\"15365700217272xhu312mr\" cle_lot=\"\" libelle=\"\" ordre=\"\" nom=\"\" extention=\"\" type_mime=\"\" taille=\"\" date_mise_en_ligne=\"1536588790\"/&gt;&lt;/creation_piece_error&gt;\n" +
                "</return>\n" +
                "        </ns1:nouveau_fichier_logResponse>\n" +
                "    </SOAP-ENV:Body>\n" +
                "</SOAP-ENV:Envelope>"

        try {
            MSUtils.parseToResponseType(response, MSUtils.PIECE_TYPE, BindingKeyAction.CREATE.value, "$nom.$extension")
        } catch (e: SoapParsingUnexpectedError) {
            assertThat(e.message).isEqualTo(
                "Array error, please check SOAP request format. Array name must be \"fichier\" and must contains 8 key/value items (keys : lot, libelle, la, ordre, nom, extension, contenu and poids)")
        }
    }

    @Test
    fun `create piece with bad dce response parsing test`() {
        val response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ns1=\"https://www.marches-securises.fr/webserv/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:SOAP-ENC=\"http://schemas.xmlsoap.org/soap/encoding/\" SOAP-ENV:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" +
                "    <SOAP-ENV:Body>\n" +
                "        <ns1:nouveau_fichier_logResponse>\n" +
                "            <return xsi:type=\"xsd:string\">&lt;?xml version=\"1.0\" encoding=\"UTF-8\"?&gt;\n" +
                "&lt;creation_file_error xmlns=\"interbat/erreur_crea_file\"&gt;&lt;consultation_non_trouvee cle_pa=\"$pa\" cle_dce=\"15365700217272xhu312mrzzz\" cle_piece=\"\" cle_lot=\"\" libelle=\"\" ordre=\"\" nom=\"\" extention=\"\" type_mime=\"\" taille=\"\" date_mise_en_ligne=\"\"/&gt;&lt;/creation_file_error&gt;\n" +
                "</return>\n" +
                "        </ns1:nouveau_fichier_logResponse>\n" +
                "    </SOAP-ENV:Body>\n" +
                "</SOAP-ENV:Envelope>"

        try {
            MSUtils.parseToResponseType(response, MSUtils.PIECE_TYPE, BindingKeyAction.CREATE.value, "$nom.$extension")
        } catch (e: BadDceError) {
            assertThat(e.message).isEqualTo(
                "Unable to process to piece creation in Marchés Sécurisés beacause of following error : Bad Dce. Please check ms.deadletter queue")
        }
    }

    @Test
    fun `create piece with bad pa response parsing test`() {
        val response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ns1=\"https://www.marches-securises.fr/webserv/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:SOAP-ENC=\"http://schemas.xmlsoap.org/soap/encoding/\" SOAP-ENV:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" +
                "    <SOAP-ENV:Body>\n" +
                "        <ns1:nouveau_fichier_logResponse>\n" +
                "            <return xsi:type=\"xsd:string\">&lt;?xml version=\"1.0\" encoding=\"UTF-8\"?&gt;\n" +
                "&lt;creation_file_error xmlns=\"interbat/erreur_crea_file\"&gt;&lt;pa_non_trouvee cle_pa=\"$pa\" cle_dce=\"15365700217272xhu312mr\" cle_piece=\"\" cle_lot=\"\" libelle=\"\" ordre=\"\" nom=\"\" extention=\"\" type_mime=\"\" taille=\"\" date_mise_en_ligne=\"\"/&gt;&lt;/creation_file_error&gt;\n" +
                "</return>\n" +
                "        </ns1:nouveau_fichier_logResponse>\n" +
                "    </SOAP-ENV:Body>\n" +
                "</SOAP-ENV:Envelope>"

        try {
            MSUtils.parseToResponseType(response, MSUtils.PIECE_TYPE, BindingKeyAction.CREATE.value, "$nom.$extension")
        } catch (e: BadPaError) {
            assertThat(e.message).isEqualTo(
                "Unable to process to piece creation in Marchés Sécurisés beacause of following error : Bad Pa. Please check ms.deadletter queue")
        }
    }

    @Test
    fun `create piece with bad log response parsing test`() {
        val response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ns1=\"https://www.marches-securises.fr/webserv/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:SOAP-ENC=\"http://schemas.xmlsoap.org/soap/encoding/\" SOAP-ENV:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" +
                "    <SOAP-ENV:Body>\n" +
                "        <ns1:nouveau_fichier_logResponse>\n" +
                "            <return xsi:type=\"xsd:string\">&lt;?xml version=\"1.0\" encoding=\"UTF-8\"?&gt;\n" +
                "&lt;identification_file_error xmlns=\"interbat/erreur_identification\"&gt;&lt;logs_non_trouves cle_pa=\"\" cle_dce=\"\" cle_piece=\"\" cle_lot=\"\" libelle=\"\" ordre=\"\" nom=\"\" extention=\"\" type_mime=\"\" taille=\"\" date_mise_en_ligne=\"\"/&gt;&lt;/identification_file_error&gt;\n" +
                "</return>\n" +
                "        </ns1:nouveau_fichier_logResponse>\n" +
                "    </SOAP-ENV:Body>\n" +
                "</SOAP-ENV:Envelope>"

        try {
            MSUtils.parseToResponseType(response, MSUtils.PIECE_TYPE, BindingKeyAction.CREATE.value, "$nom.$extension")
        } catch (e: BadLogError) {
            assertThat(e.message).isEqualTo(
                "Unable to process to piece creation in Marchés Sécurisés beacause of following error : unknown login/password. Please check ms.deadletter queue")
        }
    }

    @Test
    fun `correct delete piece response parsing test`() {
        val response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ns1=\"https://www.marches-securises.fr/webserv/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:SOAP-ENC=\"http://schemas.xmlsoap.org/soap/encoding/\" SOAP-ENV:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" +
                "    <SOAP-ENV:Body>\n" +
                "        <ns1:supprimer_fichier_logResponse>\n" +
                "            <return xsi:type=\"xsd:string\">&lt;?xml version=\"1.0\" encoding=\"UTF-8\"?&gt;\n" +
                "&lt;ifw:data xmlns:ifw=\"interbat/framwork-exportation\"&gt;\n" +
                "  &lt;objet type=\"ms_v2__fullweb_piece\"&gt;\n" +
                "    &lt;propriete nom=\"cle_piece\"&gt;1536571572ynzu4tsqgj9w&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"cle_dce\"&gt;$dce&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"cle_lot\"/&gt;\n" +
                "    &lt;propriete nom=\"libelle\"&gt;libelle&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"ordre\"&gt;1&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"nom\"&gt;FileName3.txt&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"extention\"&gt;txt&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"type_mime\"&gt;text/plain; charset=us-ascii&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"taille\"&gt;5&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"date_mise_en_ligne\"&gt;1536571572&lt;/propriete&gt;\n" +
                "  &lt;/objet&gt;\n" +
                "  &lt;objet type=\"ms_v2__fullweb_piece\"&gt;\n" +
                "    &lt;propriete nom=\"cle_piece\"&gt;1536590887xhevw7yrd4mv&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"cle_dce\"&gt;$dce&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"cle_lot\"/&gt;\n" +
                "    &lt;propriete nom=\"libelle\"&gt;libelle&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"ordre\"&gt;2&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"nom\"&gt;FileName4.txt&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"extention\"&gt;txt&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"type_mime\"&gt;text/plain; charset=us-ascii&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"taille\"&gt;6&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"date_mise_en_ligne\"&gt;1536571572&lt;/propriete&gt;\n" +
                "  &lt;/objet&gt;\n" +
                "&lt;/ifw:data&gt;\n" +
                "</return>\n" +
                "        </ns1:supprimer_fichier_logResponse>\n" +
                "    </SOAP-ENV:Body>\n" +
                "</SOAP-ENV:Envelope>"

        val responseObject =
            MSUtils.parseToResponseType(response, MSUtils.PIECE_TYPE, BindingKeyAction.DELETE.value, clePiece)

        assertThat(responseObject).isNotNull
        assertThat(responseObject.properties!!.size).isEqualTo(10)
        assertThat(responseObject.properties!![1].name).isEqualTo("cle_dce")
        assertThat(responseObject.properties!![1].value).isEqualTo(dce)
        assertThat(responseObject.properties!!.find { p -> p.value == "error" }).isNull()
        assertThat(responseObject.properties!!.find { p -> p.value == clePiece }).isNull()
        assertThat(responseObject.properties!!.find { p -> p.value == "$nom.$extension" }).isNull()
    }

    @Test
    fun `correct delete of last piece response parsing test`() {
        val response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ns1=\"https://www.marches-securises.fr/webserv/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:SOAP-ENC=\"http://schemas.xmlsoap.org/soap/encoding/\" SOAP-ENV:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" +
                "    <SOAP-ENV:Body>\n" +
                "        <ns1:supprimer_fichier_logResponse>\n" +
                "            <return xsi:type=\"xsd:string\">&lt;?xml version=\"1.0\" encoding=\"UTF-8\"?&gt;\n" +
                "&lt;ifw:data xmlns:ifw=\"interbat/framwork-exportation\"&gt;\n" +
                "  &lt;objet piece=\"last\"&gt;\n" +
                "    &lt;propriete suppression=\"true\"&gt;supprime&lt;/propriete&gt;\n" +
                "  &lt;/objet&gt;\n" +
                "&lt;/ifw:data&gt;\n" +
                "</return>\n" +
                "        </ns1:supprimer_fichier_logResponse>\n" +
                "    </SOAP-ENV:Body>\n" +
                "</SOAP-ENV:Envelope>"

        val responseObject =
            MSUtils.parseToResponseType(response, MSUtils.PIECE_TYPE, BindingKeyAction.DELETE.value, clePiece)

        assertThat(responseObject).isNotNull
        assertThat(responseObject.properties!!.size).isEqualTo(1)
        assertThat(responseObject.properties!![0].suppression).isEqualTo(true)
        assertThat(responseObject.properties!![0].value).isEqualTo("supprime")
        assertThat(responseObject.properties!!.find { p -> p.value == "error" }).isNull()
        assertThat(responseObject.properties!!.find { p -> p.value == clePiece }).isNull()
        assertThat(responseObject.properties!!.find { p -> p.value == "$nom.$extension" }).isNull()
    }

    @Test
    fun `delete piece with bad clePiece response parsing test`() {
        val response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ns1=\"https://www.marches-securises.fr/webserv/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:SOAP-ENC=\"http://schemas.xmlsoap.org/soap/encoding/\" SOAP-ENV:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" +
                "    <SOAP-ENV:Body>\n" +
                "        <ns1:supprimer_fichier_logResponse>\n" +
                "            <return xsi:type=\"xsd:string\">&lt;?xml version=\"1.0\" encoding=\"UTF-8\"?&gt;\n" +
                "&lt;cle_piece_error xmlns=\"interbat/erreur_cle_piece\"&gt;&lt;cle_piece_non_trouvee cle_pa=\"$pa\" cle_dce=\"15365700217272xhu312mr\" cle_piece=\"1536570066jujzgfdujulr\" cle_lot=\"\" libelle=\"\" ordre=\"\" nom=\"\" extention=\"\" type_mime=\"\" taille=\"\" date_mise_en_ligne=\"\"/&gt;&lt;/cle_piece_error&gt;\n" +
                "</return>\n" +
                "        </ns1:supprimer_fichier_logResponse>\n" +
                "    </SOAP-ENV:Body>\n" +
                "</SOAP-ENV:Envelope>"

        try {
            MSUtils.parseToResponseType(response, MSUtils.PIECE_TYPE, BindingKeyAction.DELETE.value, clePiece)
        } catch (e: BadClePiece) {
            assertThat(e.message).isEqualTo(
                "Unable to process to piece deletion in Marchés Sécurisés beacause of following error : requested piece is not found. Please check ms.deadletter queue")
        }
    }

    @Test
    fun `delete piece with bad dce response parsing test`() {
        val response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ns1=\"https://www.marches-securises.fr/webserv/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:SOAP-ENC=\"http://schemas.xmlsoap.org/soap/encoding/\" SOAP-ENV:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" +
                "    <SOAP-ENV:Body>\n" +
                "        <ns1:supprimer_fichier_logResponse>\n" +
                "            <return xsi:type=\"xsd:string\">&lt;?xml version=\"1.0\" encoding=\"UTF-8\"?&gt;\n" +
                "&lt;cle_dce_error xmlns=\"interbat/erreur_cle_dce\"&gt;&lt;cle_dce_non_trouvee cle_pa=\"$pa\" cle_dce=\"15365700217272xhu312mr000\" cle_piece=\"\" cle_lot=\"\" libelle=\"\" ordre=\"\" nom=\"\" extention=\"\" type_mime=\"\" taille=\"\" date_mise_en_ligne=\"\"/&gt;&lt;/cle_dce_error&gt;\n" +
                "</return>\n" +
                "        </ns1:supprimer_fichier_logResponse>\n" +
                "    </SOAP-ENV:Body>\n" +
                "</SOAP-ENV:Envelope>"

        try {
            MSUtils.parseToResponseType(response, MSUtils.PIECE_TYPE, BindingKeyAction.DELETE.value, clePiece)
        } catch (e: BadDceError) {
            assertThat(e.message).isEqualTo(
                "Unable to process to piece deletion in Marchés Sécurisés beacause of following error : Bad Dce. Please check ms.deadletter queue")
        }
    }

    @Test
    fun `delete piece with bad pa response parsing test`() {
        val response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ns1=\"https://www.marches-securises.fr/webserv/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:SOAP-ENC=\"http://schemas.xmlsoap.org/soap/encoding/\" SOAP-ENV:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" +
                "    <SOAP-ENV:Body>\n" +
                "        <ns1:supprimer_fichier_logResponse>\n" +
                "            <return xsi:type=\"xsd:string\">&lt;?xml version=\"1.0\" encoding=\"UTF-8\"?&gt;\n" +
                "&lt;creation_lot_error xmlns=\"interbat/erreur_crea_lot\"&gt;&lt;pa_non_trouvee cle_pa=\"1267898337p8xft000\" cle_dce=\"15365700217272xhu312mr\" cle_piece=\"\" cle_lot=\"\" libelle=\"\" ordre=\"\" nom=\"\" extention=\"\" type_mime=\"\" taille=\"\" date_mise_en_ligne=\"\"/&gt;&lt;/creation_lot_error&gt;\n" +
                "</return>\n" +
                "        </ns1:supprimer_fichier_logResponse>\n" +
                "    </SOAP-ENV:Body>\n" +
                "</SOAP-ENV:Envelope>"

        try {
            MSUtils.parseToResponseType(response, MSUtils.PIECE_TYPE, BindingKeyAction.DELETE.value, clePiece)
        } catch (e: BadPaError) {
            assertThat(e.message).isEqualTo(
                "Unable to process to piece deletion in Marchés Sécurisés beacause of following error : Bad Pa. Please check ms.deadletter queue")
        }
    }

    @Test
    fun `delete piece with bad log response parsing test`() {
        val response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ns1=\"https://www.marches-securises.fr/webserv/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:SOAP-ENC=\"http://schemas.xmlsoap.org/soap/encoding/\" SOAP-ENV:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" +
                "    <SOAP-ENV:Body>\n" +
                "        <ns1:supprimer_fichier_logResponse>\n" +
                "            <return xsi:type=\"xsd:string\">&lt;?xml version=\"1.0\" encoding=\"UTF-8\"?&gt;\n" +
                "&lt;identification_lot_error xmlns=\"interbat/erreur_identification\"&gt;&lt;logs_non_trouves cle_pa=\"\" cle_dce=\"\" cle_piece=\"\" cle_lot=\"\" libelle=\"\" ordre=\"\" nom=\"\" extention=\"\" type_mime=\"\" taille=\"\" date_mise_en_ligne=\"\"/&gt;&lt;/identification_lot_error&gt;\n" +
                "</return>\n" +
                "        </ns1:supprimer_fichier_logResponse>\n" +
                "    </SOAP-ENV:Body>\n" +
                "</SOAP-ENV:Envelope>"

        try {
            MSUtils.parseToResponseType(response, MSUtils.PIECE_TYPE, BindingKeyAction.DELETE.value, clePiece)
        } catch (e: BadLogError) {
            assertThat(e.message).isEqualTo(
                "Unable to process to piece deletion in Marchés Sécurisés beacause of following error : unknown login/password. Please check ms.deadletter queue")
        }
    }

    @Test
    fun `correct read consultation from marche securise response parsing test`() {
        val response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ns1=\"https://www.marches-securises.fr/webserv/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:SOAP-ENC=\"http://schemas.xmlsoap.org/soap/encoding/\" SOAP-ENV:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" +
                "    <SOAP-ENV:Body>\n" +
                "        <ns1:lire_consultation_logResponse>\n" +
                "            <return xsi:type=\"xsd:string\">&lt;?xml version=\"1.0\" encoding=\"UTF-8\"?&gt;\n" +
                "&lt;ifw:data xmlns:ifw=\"interbat/framwork-exportation\"&gt;\n" +
                "  &lt;objet type=\"ms_v2__fullweb_dce\"&gt;\n" +
                "    &lt;propriete nom=\"cle\"&gt;$dce&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"reference\"&gt;$msReference&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"objet\"&gt;$objet&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"date_creation\"&gt;$datePublication&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"date_publication\"&gt;$datePublication&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"date_publication_f\"&gt;jeudi 13 septembre 2018 - 00:00&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"date_cloture\"&gt;$dateCloture&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"date_cloture_f\"&gt;dimanche 30 décembre 2018 - 00:00&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"ref_interne\"&gt;$reference&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"finalite_marche\"&gt;$finaliteMarche&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"type_marche\"&gt;$typeMarche&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"type_prestation\"&gt;$prestation&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"departements_prestation\"&gt;$departement&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"passation\"&gt;$passation&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"informatique\"&gt;$informatique&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"passe\"&gt;passe&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"emails\"&gt;$email&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"en_ligne\"&gt;$enligne&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"a_lots\"&gt;$alloti&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"invisible\"&gt;0&lt;/propriete&gt;\n" +
                "  &lt;/objet&gt;\n" +
                "&lt;/ifw:data&gt;\n" +
                "</return>\n" +
                "        </ns1:lire_consultation_logResponse>\n" +
                "    </SOAP-ENV:Body>\n" +
                "</SOAP-ENV:Envelope>"
        val responseObject =
            MSUtils.parseToResponseType(response, MSUtils.CONSULTATION_TYPE, BindingKeyAction.GET.value)
        assertThat(responseObject.properties!![1].value).isEqualTo(msReference)
        assertThat(responseObject.properties!![1].name).isEqualTo("reference")
    }

    @Test
    fun `read consultation from marche securise with bad log response parsing test`() {
        val response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ns1=\"https://www.marches-securises.fr/webserv/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:SOAP-ENC=\"http://schemas.xmlsoap.org/soap/encoding/\" SOAP-ENV:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" +
                "    <SOAP-ENV:Body>\n" +
                "        <ns1:lire_consultation_logResponse>\n" +
                "            <return xsi:nil=\"true\"/>\n" +
                "        </ns1:lire_consultation_logResponse>\n" +
                "    </SOAP-ENV:Body>\n" +
                "</SOAP-ENV:Envelope>"

        try {
            MSUtils.parseToResponseType(response, MSUtils.CONSULTATION_TYPE, BindingKeyAction.GET.value)
        } catch (e: BadLogError) {
            assertThat(e.message).isEqualTo(
                "Unable to process marchepublic:consultation_0 get request for following reason : Unknown login/password.")
        }
    }

    @Test
    fun `read consultation from marche securise with bad pa response parsing test`() {
        val response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ns1=\"https://www.marches-securises.fr/webserv/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:SOAP-ENC=\"http://schemas.xmlsoap.org/soap/encoding/\" SOAP-ENV:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" +
                "    <SOAP-ENV:Body>\n" +
                "        <ns1:lire_consultation_logResponse>\n" +
                "            <return xsi:type=\"xsd:string\">&lt;?xml version=\"1.0\" encoding=\"UTF-8\"?&gt;\n" +
                "&lt;ifw:data xmlns:ifw=\"interbat/framwork-exportation\"&gt;\n" +
                "  &lt;objet type=\"error\"&gt;\n" +
                "    &lt;propriete nom=\"load_pa_error\"&gt;error&lt;/propriete&gt;\n" +
                "  &lt;/objet&gt;\n" +
                "&lt;/ifw:data&gt;\n" +
                "</return>\n" +
                "        </ns1:lire_consultation_logResponse>\n" +
                "    </SOAP-ENV:Body>\n" +
                "</SOAP-ENV:Envelope>"

        val responseObject =
            MSUtils.parseToResponseType(response, MSUtils.CONSULTATION_TYPE, BindingKeyAction.GET.value)
        assertThat(responseObject.properties!![0].name).isEqualTo("load_pa_error")
    }

    @Test
    fun `read consultation from marche securise with bad dce response parsing test`() {
        val response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ns1=\"https://www.marches-securises.fr/webserv/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:SOAP-ENC=\"http://schemas.xmlsoap.org/soap/encoding/\" SOAP-ENV:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" +
                "    <SOAP-ENV:Body>\n" +
                "        <ns1:lire_consultation_logResponse>\n" +
                "            <return xsi:type=\"xsd:string\">&lt;?xml version=\"1.0\" encoding=\"UTF-8\"?&gt;\n" +
                "&lt;ifw:data xmlns:ifw=\"interbat/framwork-exportation\"&gt;\n" +
                "  &lt;objet type=\"ms_v2__fullweb_dce\"&gt;\n" +
                "    &lt;propriete nom=\"load_consultation_fail\" statut=\"not_changed\" message=\"no_consultation\"&gt;no_consultation&lt;/propriete&gt;\n" +
                "  &lt;/objet&gt;\n" +
                "&lt;/ifw:data&gt;\n" +
                "</return>\n" +
                "        </ns1:lire_consultation_logResponse>\n" +
                "    </SOAP-ENV:Body>\n" +
                "</SOAP-ENV:Envelope>"

        val responseObject =
            MSUtils.parseToResponseType(response, MSUtils.CONSULTATION_TYPE, BindingKeyAction.GET.value)
        assertThat(responseObject.properties!![0].name).isEqualTo("load_consultation_fail")
    }

    @Test
    fun `correct reponse listing response parsing test`() {
        val response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ns1=\"https://www.marches-securises.fr/webserv/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:SOAP-ENC=\"http://schemas.xmlsoap.org/soap/encoding/\" SOAP-ENV:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" +
                "    <SOAP-ENV:Body>\n" +
                "        <ns1:lister_reponses_electroniquesResponse>\n" +
                "            <return xsi:type=\"xsd:string\">&lt;?xml version=\"1.0\" encoding=\"UTF-8\"?&gt;\n" +
                "&lt;ifw:data xmlns:ifw=\"interbat/framwork-exportation\"&gt;\n" +
                "  &lt;objet type=\"ms__reponse\"&gt;\n" +
                "    &lt;propriete nom=\"cle\"&gt;$cleReponse&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"cle_dce\"&gt;$dce&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"cle_entreprise_ms\"&gt;$cleEntreprise&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"denomination_ent\"&gt;$nomEntreprise&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"contact\"&gt;$contact&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"email_contact\"&gt;$emailContact&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"date_depot\"&gt;$dateDepot&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"date_depot_f\"&gt;vendredi 03 août 2018 - 14:47&lt;/propriete&gt;\n" +
                "  &lt;propriete nom=\"taille_reponse\"&gt;$poidsReponse&lt;/propriete&gt;&lt;objet type=\"entreprise\"&gt;\n" +
                "    &lt;propriete nom=\"nom\"&gt;$nomEntreprise&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"adresse_1\"&gt;$adresse&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"adresse_2\"/&gt;\n" +
                "    &lt;propriete nom=\"code_postal\"&gt;$codePostal&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"commune\"&gt;$commune&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"pays\"&gt;$pays&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"tel\"&gt;$tel&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"fax\"&gt;$fax&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"siret\"/&gt;\n" +
                "    &lt;propriete nom=\"siren\"/&gt;\n" +
                "    &lt;propriete nom=\"code_naf\"&gt;$naf&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"url\"&gt;$url&lt;/propriete&gt;\n" +
                "  &lt;/objet&gt;&lt;/objet&gt;\n" +
                "&lt;pagination ordre=\"\" sensordre=\"ASC\"/&gt;&lt;reponses nb_total=\"1\"/&gt;&lt;/ifw:data&gt;\n" +
                "</return>\n" +
                "        </ns1:lister_reponses_electroniquesResponse>\n" +
                "    </SOAP-ENV:Body>\n" +
                "</SOAP-ENV:Envelope>"

        val responseObject =
            MSUtils.parseToResponseObjectList(response, MSUtils.REPONSE_TYPE, BindingKeyAction.UPDATE.value)
        assertThat(responseObject).isNotNull
        assertThat(responseObject.size).isEqualTo(1)
        assertThat(responseObject[0].properties!!.size).isEqualTo(9)
        assertThat(responseObject[0].responseObject!!.size).isEqualTo(1)
        assertThat(responseObject[0].responseObject!![0].type).isEqualTo("entreprise")
        assertThat(responseObject[0].responseObject!![0].properties!!.size).isEqualTo(12)
        assertThat(responseObject[0].responseObject!![0].properties!![3].name).isEqualTo("code_postal")

        val registreReponse =
            RegistreReponse.fromSoapObject("http://baseUri/dc/type", responseObject[0], "consultationUri")

        assertThat(registreReponse.cle).isEqualTo(cleReponse)
        assertThat(registreReponse.nomContact).isEqualTo(contact)
        assertThat(registreReponse.emailContact).isEqualTo(emailContact)
        assertThat(registreReponse.dateDepot).isEqualTo(
            LocalDateTime.ofInstant(
                Instant.ofEpochSecond((dateDepot).toLong()),
                TimeZone.getDefault().toZoneId()
            )
        )
        assertThat(registreReponse.poids).isEqualTo(poidsReponse.toInt())
        assertThat(registreReponse.siret).isEqualTo("")
    }

    @Test
    fun `correct reponse listing empty response parsing test`() {
        val response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ns1=\"https://www.marches-securises.fr/webserv/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:SOAP-ENC=\"http://schemas.xmlsoap.org/soap/encoding/\" SOAP-ENV:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" +
                "    <SOAP-ENV:Body>\n" +
                "        <ns1:lister_reponses_electroniquesResponse>\n" +
                "            <return xsi:type=\"xsd:string\">&lt;?xml version=\"1.0\" encoding=\"UTF-8\"?&gt;\n" +
                "&lt;ifw:data xmlns:ifw=\"interbat/framwork-exportation\"&gt;\n" +
                "  &lt;pagination ordre=\"\" sensordre=\"ASC\"/&gt;\n" +
                "  &lt;reponses nb_total=\"0\"/&gt;\n" +
                "&lt;/ifw:data&gt;\n" +
                "</return>\n" +
                "        </ns1:lister_reponses_electroniquesResponse>\n" +
                "    </SOAP-ENV:Body>\n" +
                "</SOAP-ENV:Envelope>"

        val responseObject =
            MSUtils.parseToResponseObjectList(response, MSUtils.REPONSE_TYPE, BindingKeyAction.UPDATE.value)
        assertThat(responseObject).isEmpty()
    }

    @Test
    fun `correct retrait listing response parsing test`() {
        val response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ns1=\"https://www.marches-securises.fr/webserv/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:SOAP-ENC=\"http://schemas.xmlsoap.org/soap/encoding/\" SOAP-ENV:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" +
                "    <SOAP-ENV:Body>\n" +
                "        <ns1:lister_retraits_electroniques_detailsResponse>\n" +
                "            <return xsi:type=\"xsd:string\">&lt;?xml version=\"1.0\" encoding=\"UTF-8\"?&gt;\n" +
                "&lt;ifw:data xmlns:ifw=\"interbat/framwork-exportation\"&gt;\n" +
                "  &lt;objet type=\"ms__retrait\"&gt;\n" +
                "    &lt;propriete nom=\"cle\"&gt;$cleRetrait&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"cle_piece\"&gt;$clePiece&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"nom_piece\"&gt;$nom.$extension&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"libelle_piece\"&gt;$libellePiece&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"cle_dce\"&gt;$dce&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"cle_ent_ms\"&gt;$cleEntreprise&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"denomination_ent\"&gt;E$nomEntreprise&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"date_debut\"&gt;$dateDebut&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"date_debut_f\"&gt;mercredi 08 août 2018 - 17:54&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"date_fin\"&gt;$dateFin&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"cle_utilisateur\"&gt;$clePersonne&lt;/propriete&gt;\n" +
                "  &lt;objet type=\"entreprise\"&gt;\n" +
                "    &lt;propriete nom=\"nom\"&gt;$nomEntreprise&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"adresse_1\"&gt;$adresse&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"adresse_2\"&gt;vgc&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"code_postal\"&gt;$codePostal&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"commune\"&gt;$commune&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"pays\"&gt;$pays&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"tel\"&gt;$tel&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"fax\"&gt;$fax&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"siret\"&gt;987654321&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"siren\"/&gt;\n" +
                "    &lt;propriete nom=\"code_naf\"&gt;$naf&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"url\"/&gt;\n" +
                "  &lt;/objet&gt;&lt;objet type=\"personne\"&gt;\n" +
                "    &lt;propriete nom=\"genre\"&gt;$genre&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"nom\"&gt;$nomContact&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"prenom\"&gt;$prenomContact&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"email\"&gt;$emailContact&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"tel\"&gt;$telephoneContact&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"fax\"/&gt;\n" +
                "  &lt;/objet&gt;&lt;/objet&gt;\n" +
                "  &lt;objet type=\"ms__retrait\"&gt;\n" +
                "    &lt;propriete nom=\"cle\"&gt;$cleRetrait-2&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"cle_piece\"&gt;$clePiece-2&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"nom_piece\"&gt;$nom-2.$extension&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"libelle_piece\"&gt;$libellePiece-2&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"cle_dce\"&gt;$dce&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"cle_ent_ms\"&gt;$cleEntreprise-2&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"denomination_ent\"&gt;$nomEntreprise-2&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"date_debut\"&gt;${dateDebut.toInt() + 86400}&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"date_debut_f\"&gt;mercredi 08 août 2018 - 17:54&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"date_fin\"&gt;${dateFin.toInt() + 86400}&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"cle_utilisateur\"&gt;$clePersonne-2&lt;/propriete&gt;\n" +
                "  &lt;objet type=\"entreprise\"&gt;\n" +
                "    &lt;propriete nom=\"nom\"&gt;$nomEntreprise-2&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"adresse_1\"&gt;$adresse-2&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"adresse_2\"&gt;vgc&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"code_postal\"&gt;$codePostal-2&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"commune\"&gt;$commune-2&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"pays\"&gt;$pays&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"tel\"&gt;$tel&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"fax\"&gt;$fax&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"siret\"/&gt;\n" +
                "    &lt;propriete nom=\"siren\"/&gt;\n" +
                "    &lt;propriete nom=\"code_naf\"&gt;$naf-2&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"url\"/&gt;\n" +
                "  &lt;/objet&gt;&lt;objet type=\"personne\"&gt;\n" +
                "    &lt;propriete nom=\"genre\"&gt;${genre}me&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"nom\"&gt;un nom&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"prenom\"&gt;un prénom&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"email\"&gt;un mail&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"tel\"&gt;un téléphone&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"fax\"/&gt;\n" +
                "  &lt;/objet&gt;&lt;/objet&gt;\n" +
                "  &lt;objet type=\"ms__retrait\"&gt;\n" +
                "    &lt;propriete nom=\"cle\"&gt;$cleRetrait-3&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"cle_piece\"&gt;$clePiece-3&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"nom_piece\"&gt;$nom-3.$extension&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"libelle_piece\"&gt;$libellePiece&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"cle_dce\"&gt;$dce&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"cle_ent_ms\"&gt;$cleEntreprise&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"denomination_ent\"&gt;$nomEntreprise&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"date_debut\"&gt;${dateDebut.toInt() - 86400}&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"date_debut_f\"&gt;mercredi 08 août 2018 - 17:54&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"date_fin\"&gt;${dateFin.toInt() - 86400}&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"cle_utilisateur\"&gt;$clePersonne&lt;/propriete&gt;\n" +
                "  &lt;objet type=\"entreprise\"&gt;\n" +
                "    &lt;propriete nom=\"nom\"&gt;$nomEntreprise-3&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"adresse_1\"&gt;$adresse-3&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"adresse_2\"&gt;vgc&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"code_postal\"&gt;$codePostal-3&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"commune\"&gt;$commune-3&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"pays\"&gt;$pays&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"tel\"&gt; un téléphone&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"fax\"&gt;un fax&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"siret\"/&gt;\n" +
                "    &lt;propriete nom=\"siren\"/&gt;\n" +
                "    &lt;propriete nom=\"code_naf\"&gt;$naf-3&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"url\"/&gt;\n" +
                "  &lt;/objet&gt;&lt;objet type=\"personne\"&gt;\n" +
                "    &lt;propriete nom=\"genre\"&gt;$genre&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"nom\"&gt;un nom&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"prenom\"&gt;Un prénom&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"email\"&gt;un mail&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"tel\"&gt;un téléphone&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"fax\"/&gt;\n" +
                "  &lt;/objet&gt;&lt;/objet&gt;\n" +
                "  &lt;objet type=\"ms__retrait\"&gt;\n" +
                "    &lt;propriete nom=\"cle\"&gt;$cleRetrait-4&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"cle_piece\"&gt;$clePiece-4&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"nom_piece\"&gt;$nom-4.$extension&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"libelle_piece\"/&gt;\n" +
                "    &lt;propriete nom=\"cle_dce\"&gt;$dce&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"cle_ent_ms\"&gt;$cleEntreprise-4&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"denomination_ent\"&gt;Un nom d'entreprise&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"date_debut\"&gt;${dateDebut.toInt() + 44000}&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"date_debut_f\"&gt;mercredi 08 août 2018 - 17:54&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"date_fin\"&gt;${dateFin.toInt() + 44000}&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"cle_utilisateur\"&gt;$clePersonne-4&lt;/propriete&gt;\n" +
                "  &lt;objet type=\"entreprise\"&gt;\n" +
                "    &lt;propriete nom=\"nom\"&gt;Un nom d'entreprise&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"adresse_1\"&gt;Une adresse&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"adresse_2\"&gt;vgc&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"code_postal\"&gt;un code postal&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"commune\"&gt;Une commune&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"pays\"&gt;FR&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"tel\"&gt;Un téléphone&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"fax\"&gt;Un téléphone&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"siret\"/&gt;\n" +
                "    &lt;propriete nom=\"siren\"/&gt;\n" +
                "    &lt;propriete nom=\"code_naf\"&gt;$naf-4&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"url\"/&gt;\n" +
                "  &lt;/objet&gt;&lt;objet type=\"personne\"&gt;\n" +
                "    &lt;propriete nom=\"genre\"&gt;$genre&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"nom\"&gt;Un nom&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"prenom\"&gt;Un prénom&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"email\"&gt;Un mail&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"tel\"&gt;Un téléphone&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"fax\"/&gt;\n" +
                "  &lt;/objet&gt;&lt;/objet&gt;\n" +
                "  &lt;objet type=\"ms__retrait\"&gt;\n" +
                "    &lt;propriete nom=\"cle\"&gt;$cleRetrait-5&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"cle_piece\"&gt;$clePiece-5&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"nom_piece\"&gt;$nom-5.$extension&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"libelle_piece\"/&gt;\n" +
                "    &lt;propriete nom=\"cle_dce\"&gt;$dce&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"cle_ent_ms\"&gt;$cleEntreprise-5&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"denomination_ent\"&gt;Un nom d'entreprise&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"date_debut\"&gt;${dateDebut.toInt() - 44000}&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"date_debut_f\"&gt;mercredi 08 août 2018 - 17:54&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"date_fin\"&gt;${dateFin.toInt() - 44000}&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"cle_utilisateur\"&gt;$clePersonne-5&lt;/propriete&gt;\n" +
                "  &lt;objet type=\"entreprise\"&gt;\n" +
                "    &lt;propriete nom=\"nom\"&gt;Un nom d'entreprise&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"adresse_1\"&gt;Une adresse&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"adresse_2\"&gt;vgc&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"code_postal\"&gt;Un code postal&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"commune\"&gt;Une commune&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"pays\"&gt;$pays&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"tel\"&gt;Un téléphone&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"fax\"&gt;Un téléphone&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"siret\"/&gt;\n" +
                "    &lt;propriete nom=\"siren\"/&gt;\n" +
                "    &lt;propriete nom=\"code_naf\"&gt;$naf-5&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"url\"/&gt;\n" +
                "  &lt;/objet&gt;&lt;objet type=\"personne\"&gt;\n" +
                "    &lt;propriete nom=\"genre\"&gt;$genre&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"nom\"&gt;Un nom&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"prenom\"&gt;Un prénom&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"email\"&gt;un mail&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"tel\"&gt;Un téléphone&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"fax\"/&gt;\n" +
                "  &lt;/objet&gt;&lt;/objet&gt;\n" +
                "  &lt;objet type=\"ms__retrait\"&gt;\n" +
                "    &lt;propriete nom=\"cle\"&gt;$cleRetrait-6&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"cle_piece\"&gt;$clePiece-6&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"nom_piece\"&gt;$nom-6.$extension&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"libelle_piece\"/&gt;\n" +
                "    &lt;propriete nom=\"cle_dce\"&gt;$dce&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"cle_ent_ms\"&gt;$cleEntreprise-6&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"denomination_ent\"&gt;Un nom&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"date_debut\"&gt;${dateDebut.toInt() + 60000}&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"date_debut_f\"&gt;mercredi 08 août 2018 - 17:54&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"date_fin\"&gt;${dateFin.toInt() + 60000}&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"cle_utilisateur\"&gt;$clePersonne-6&lt;/propriete&gt;\n" +
                "  &lt;objet type=\"entreprise\"&gt;\n" +
                "    &lt;propriete nom=\"nom\"&gt;Un nom&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"adresse_1\"&gt;Une adresse&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"adresse_2\"&gt;vgc&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"code_postal\"&gt;Un code postal&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"commune\"&gt;Une commune&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"pays\"&gt;$pays&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"tel\"&gt;Un téléphone&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"fax\"&gt;Un téléphone&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"siret\"/&gt;\n" +
                "    &lt;propriete nom=\"siren\"/&gt;\n" +
                "    &lt;propriete nom=\"code_naf\"&gt;$naf-6&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"url\"/&gt;\n" +
                "  &lt;/objet&gt;&lt;objet type=\"personne\"&gt;\n" +
                "    &lt;propriete nom=\"genre\"&gt;$genre&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"nom\"&gt;Un nom&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"prenom\"&gt;Un prénom&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"email\"&gt;un mail&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"tel\"&gt;un téléphone&lt;/propriete&gt;\n" +
                "    &lt;propriete nom=\"fax\"/&gt;\n" +
                "  &lt;/objet&gt;&lt;/objet&gt;\n" +
                "&lt;pagination ordre=\"\" sensordre=\"ASC\"/&gt;&lt;retraits nb_total=\"6\"/&gt;&lt;/ifw:data&gt;\n" +
                "</return>\n" +
                "        </ns1:lister_retraits_electroniques_detailsResponse>\n" +
                "    </SOAP-ENV:Body>\n" +
                "</SOAP-ENV:Envelope>"

        val responseObject =
            MSUtils.parseToResponseObjectList(response, MSUtils.RETRAIT_TYPE, BindingKeyAction.UPDATE.value)
        assertThat(responseObject.size).isEqualTo(6)
        assertThat(responseObject[0].responseObject!!.size).isEqualTo(2)
        assertThat(responseObject[0].type).isEqualTo("ms__retrait")
        assertThat(responseObject[0].responseObject!![0].type).isEqualTo("entreprise")
        assertThat(responseObject[0].responseObject!![1].type).isEqualTo("personne")

        val retrait: RegistreRetrait =
            RegistreRetrait.fromSoapObject("http://baseUri/dc/type", responseObject[0], "consultation Uri", "pieceUuid")
        assertThat(retrait.cle).isEqualTo(cleRetrait)
        assertThat(retrait.siret).isEqualTo("987654321")
        assertThat(retrait.nomPiece).isEqualTo("$nom.$extension")
        assertThat(retrait.libellePiece).isEqualTo(libellePiece)
        assertThat(retrait.dateDebut).isEqualTo(
            LocalDateTime.ofInstant(
                Instant.ofEpochSecond((dateDebut).toLong()),
                TimeZone.getDefault().toZoneId()
            )
        )
        assertThat(retrait.dateFin).isEqualTo(
            LocalDateTime.ofInstant(
                Instant.ofEpochSecond((dateFin).toLong()),
                TimeZone.getDefault().toZoneId()
            )
        )
        assertThat(retrait.personne!!.cle).isEqualTo(clePersonne)
        assertThat(retrait.personne!!.genre).isEqualTo(genre)
        assertThat(retrait.personne!!.nom).isEqualTo(nomContact)
        assertThat(retrait.personne!!.prenom).isEqualTo(prenomContact)
        assertThat(retrait.personne!!.email).isEqualTo(emailContact)
        assertThat(retrait.personne!!.telephone).isEqualTo(telephoneContact)
        assertThat(retrait.personne!!.fax).isEmpty()
    }
}
