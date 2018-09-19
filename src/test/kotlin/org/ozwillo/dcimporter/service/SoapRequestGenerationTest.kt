package org.ozwillo.dcimporter.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.ozwillo.dcimporter.model.marchepublic.FinaliteMarcheType
import org.ozwillo.dcimporter.model.marchepublic.TypeMarcheType
import org.ozwillo.dcimporter.model.marchepublic.TypePrestationType
import org.ozwillo.dcimporter.util.MSUtils
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SoapRequestGenerationTest{

    private var login: String = "login"
    private var password: String = "password"
    private var pa: String = "instance pa"

    val dce = "1533297690p44lmzk2fidz"
    val objet = "Consultation WS Test"
    val enligne = MSUtils.booleanToInt(true).toString()
    val datePublication = LocalDateTime.now().atZone(ZoneId.of("Europe/Paris")).toInstant().epochSecond.toString()
    val dateCloture = LocalDateTime.now().plusMonths(3).atZone(ZoneId.of("Europe/Paris")).toInstant().epochSecond.toString()
    val reference = "F-SICTIAM_06_20180622W2_01"
    val finaliteMarche = FinaliteMarcheType.AUTRE.toString().toLowerCase()
    val typeMarche = TypeMarcheType.AUTRE.toString().toLowerCase()
    val prestation = TypePrestationType.AUTRES.toString().toLowerCase()
    val passation = "AORA"
    val informatique = MSUtils.booleanToInt(true).toString()
    val alloti = MSUtils.booleanToInt(false).toString()
    val departement = MSUtils.intListToString(listOf(74, 38, 6))
    val email = if(MSUtils.stringListToString(listOf("test1@test.com", "test2@test.com", "test3@test.com")).length > 255) (MSUtils.stringListToString(listOf("test1@test.com", "test2@test.com", "test3@test.com"))).substring(0,255) else MSUtils.stringListToString(listOf("test1@test.com", "test2@test.com", "test3@test.com"))

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
    private final val byteArrayContenu = "un contenu texte".toByteArray()
    val contenu = Base64.getEncoder().encodeToString(byteArrayContenu)!!
    val poids = 10.toString()

    @Test
    fun correct_consultation_creation_request_generation_test(){
        val goodRequest:String = "<soapenv:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:web=\"https://www.marches-securises.fr/webserv/\">\n" +
                "   <soapenv:Header/>\n" +
                "   <soapenv:Body>\n" +
                "      <web:creer_consultation_log soapenv:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" +
                "         <login xsi:type=\"xsd:string\">$login</login>\n" +
                "         <password xsi:type=\"xsd:string\">$password</password>\n" +
                "         <pa xsi:type=\"xsd:string\">$pa</pa>\n" +
                "      </web:creer_consultation_log>\n" +
                "   </soapenv:Body>\n" +
                "</soapenv:Envelope>"

        val request = MSUtils.generateCreateConsultationLogRequest(login, password, pa)
        assertThat(request).isEqualTo(goodRequest)
    }

    @Test
    fun correct_consultation_update_request_generation_test(){
        val goodRequest:String = "<soapenv:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:web=\"https://www.marches-securises.fr/webserv/\">\n" +
                "    <soapenv:Header/>\n" +
                "    <soapenv:Body>\n" +
                "        <web:modifier_consultation_log soapenv:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" +
                "            <login xsi:type=\"xsd:string\">$login</login>\n" +
                "            <password xsi:type=\"xsd:string\">$password</password>\n" +
                "            <pa xsi:type=\"xsd:string\">$pa</pa>\n" +
                "            <dce xsi:type=\"xsd:string\">$dce</dce>\n" +
                "            <tableau xsi:type=\"Map\" xmlns=\"http://xml.apache.org/xml-soap\">\n" +
                "                <item>\n" +
                "                    <key xsi:type=\"xsd:string\">objet</key>\n" +
                "                    <value xsi:type=\"xsd:string\">$objet</value>\n" +
                "                </item>\n" +
                "                <item>\n" +
                "                    <key xsi:type=\"xsd:string\">en_ligne</key>\n" +
                "                    <value xsi:type=\"xsd:int\">$enligne</value>\n" +
                "                </item>\n" +
                "                <item>\n" +
                "                    <key xsi:type=\"xsd:string\">date_publication</key>\n" +
                "                    <value xsi:type=\"xsd:timestamp\">$datePublication</value>\n" +
                "                </item>\n" +
                "                <item>\n" +
                "                    <key xsi:type=\"xsd:string\">date_cloture</key>\n" +
                "                    <value xsi:type=\"xsd:timestamp\">$dateCloture</value>\n" +
                "                </item>\n" +
                "                <item>\n" +
                "                    <key xsi:type=\"xsd:string\">ref_interne</key>\n" +
                "                    <value xsi:type=\"xsd:string\">$reference</value>\n" +
                "                </item>\n" +
                "                <item>\n" +
                "                    <key xsi:type=\"xsd:string\">finalite_marche</key>\n" +
                "                    <value xsi:type=\"xsd:string\">$finaliteMarche</value>\n" +
                "                </item>\n" +
                "                <item>\n" +
                "                    <key xsi:type=\"xsd:string\">type_marche</key>\n" +
                "                    <value xsi:type=\"xsd:string\">$typeMarche</value>\n" +
                "                </item>\n" +
                "                <item>\n" +
                "                    <key xsi:type=\"xsd:string\">type_prestation</key>\n" +
                "                    <value xsi:type=\"xsd:string\">$prestation</value>\n" +
                "                </item>\n" +
                "                <item>\n" +
                "                    <key xsi:type=\"xsd:string\">passation</key>\n" +
                "                    <value xsi:type=\"xsd:string\">$passation</value>\n" +
                "                </item>\n" +
                "                <item>\n" +
                "                  <key xsi:type=\"xsd:string\">informatique</key>\n" +
                "                  <value xsi:type=\"xsd:string\">$informatique</value>\n" +
                "                </item>\n" +
                "                <item>\n" +
                "                    <key xsi:type=\"xsd:string\">a_lots</key>\n" +
                "                    <value xsi:type=\"xsd:int\">$alloti</value>\n" +
                "                </item>\n" +
                "                <item>\n" +
                "                    <key xsi:type=\"xsd:string\">departements_prestation</key>\n" +
                "                    <value xsi:type=\"xsd:string\">$departement</value>\n" +
                "                </item>\n" +
                "                <item>\n" +
                "                    <key xsi:type=\"xsd:string\">emails</key>\n" +
                "                    <value xsi:type=\"xsd:string\">$email</value>\n" +
                "                </item>\n" +
                "            </tableau>     \n" +
                "        </web:modifier_consultation_log>\n" +
                "    </soapenv:Body>\n" +
                "</soapenv:Envelope>"

        val request = MSUtils.generateModifyConsultationLogRequest(login, password, pa, dce, objet, enligne, datePublication, dateCloture, reference, finaliteMarche, typeMarche, prestation, passation, informatique, alloti, departement, email)
        assertThat(request).isEqualTo(goodRequest)
    }

    @Test
    fun correct_consultation_deletion_request_generation_test(){
        val goodRequest = "<soapenv:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:web=\"https://www.marches-securises.fr/webserv/\">\n" +
                "   <soapenv:Header/>\n" +
                "   <soapenv:Body>\n" +
                "      <web:supprimer_consultation_log soapenv:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" +
                "         <login xsi:type=\"xsd:string\">$login</login>\n" +
                "         <password xsi:type=\"xsd:string\">$password</password>\n" +
                "         <pa xsi:type=\"xsd:string\">$pa</pa>\n" +
                "         <dce xsi:type=\"xsd:string\">$dce</dce>\n" +
                "      </web:supprimer_consultation_log>\n" +
                "   </soapenv:Body>\n" +
                "</soapenv:Envelope>"

        val request = MSUtils.generateDeleteConsultationLogRequest(login, password, pa, dce)
        assertThat(request).isEqualTo(goodRequest)
    }

    @Test
    fun correct_consultation_check_request_generation_test(){
        val goodRequest = "<soapenv:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:web=\"https://www.marches-securises.fr/webserv/\">\n" +
                "   <soapenv:Header/>\n" +
                "   <soapenv:Body>\n" +
                "      <web:verifier_informations_log soapenv:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" +
                "         <login xsi:type=\"xsd:string\">$login</login>\n" +
                "         <password xsi:type=\"xsd:string\">$password</password>\n" +
                "         <idpa xsi:type=\"xsd:string\">$pa</idpa>\n" +
                "         <dce xsi:type=\"xsd:string\">$dce</dce>\n" +
                "      </web:verifier_informations_log>\n" +
                "   </soapenv:Body>\n" +
                "</soapenv:Envelope>"

        val request = MSUtils.generateCheckConsultationRequest(login, password, pa, dce)
        assertThat(request).isEqualTo(goodRequest)
    }

    @Test
    fun correct_consultation_publication_request_generation_test(){
        val goodRequest = "<soapenv:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:web=\"https://www.marches-securises.fr/webserv/\">\n" +
                "   <soapenv:Header/>\n" +
                "   <soapenv:Body>\n" +
                "      <web:publication_dce_log soapenv:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" +
                "         <login xsi:type=\"xsd:string\">$login</login>\n" +
                "         <password xsi:type=\"xsd:string\">$password</password>\n" +
                "         <idpa xsi:type=\"xsd:string\">$pa</idpa>\n" +
                "         <dce xsi:type=\"xsd:string\">$dce</dce>\n" +
                "      </web:publication_dce_log>\n" +
                "   </soapenv:Body>\n" +
                "</soapenv:Envelope>"

        val request = MSUtils.generatePublishConsultationRequest(login, password, pa, dce)
        assertThat(request).isEqualTo(goodRequest)
    }

    @Test
    fun correct_lot_creation_request_generation_test(){
        val goodRequest = "<soapenv:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:web=\"https://www.marches-securises.fr/webserv/\">\n" +
                "    <soapenv:Header/>\n" +
                "    <soapenv:Body>\n" +
                "        <web:creer_lot_consultation_log soapenv:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" +
                "            <login xsi:type=\"xsd:string\">$login</login>\n" +
                "            <password xsi:type=\"xsd:string\">$password</password>\n" +
                "            <pa xsi:type=\"xsd:string\">$pa</pa>\n" +
                "            <dce xsi:type=\"xsd:string\">$dce</dce>\n" +
                "            <libelle xsi:type=\"xsd:string\">$libelleLot</libelle>\n" +
                "            <ordre xsi:type=\"xsd:string\">$ordreLot</ordre>\n" +
                "            <numero xsi:type=\"xsd:string\">$numeroLot</numero>\n" +
                "        </web:creer_lot_consultation_log>\n" +
                "    </soapenv:Body>\n" +
                "</soapenv:Envelope>"

        val request = MSUtils.generateCreateLotLogRequest(login, password, pa, dce, libelleLot, ordreLot, numeroLot)
        assertThat(request).isEqualTo(goodRequest)
    }

    @Test
    fun correct_lot_update_request_generation_test(){
        val goodRequest = "<soapenv:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:web=\"https://www.marches-securises.fr/webserv/\">\n" +
                "   <soapenv:Header/>\n" +
                "   <soapenv:Body>\n" +
                "      <web:modifier_lot_consultation_log soapenv:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" +
                "         <login xsi:type=\"xsd:string\">$login</login>\n" +
                "         <password xsi:type=\"xsd:string\">$password</password>\n" +
                "         <pa xsi:type=\"xsd:string\">$pa</pa>\n" +
                "         <dce xsi:type=\"xsd:string\">$dce</dce>\n" +
                "         <clelot xsi:type=\"xsd:string\">$cleLot</clelot>\n" +
                "         <libelle xsi:type=\"xsd:string\">$libelleLot</libelle>\n" +
                "         <ordre xsi:type=\"xsd:string\">$ordreLot</ordre>\n" +
                "         <numero xsi:type=\"xsd:string\">$numeroLot</numero>\n" +
                "      </web:modifier_lot_consultation_log>\n" +
                "   </soapenv:Body>\n" +
                "</soapenv:Envelope>"

        val request = MSUtils.generateModifyLotRequest(login, password, pa, dce, cleLot, libelleLot, ordreLot, numeroLot)
        assertThat(request).isEqualTo(goodRequest)
    }

    @Test
    fun correct_lot_deletion_request_generation_test(){
        val goodRequest = "<soapenv:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:web=\"https://www.marches-securises.fr/webserv/\">\n" +
                "   <soapenv:Header/>\n" +
                "   <soapenv:Body>\n" +
                "      <web:supprimer_lot_consultation_log soapenv:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" +
                "         <login xsi:type=\"xsd:string\">$login</login>\n" +
                "         <password xsi:type=\"xsd:string\">$password</password>\n" +
                "         <pa xsi:type=\"xsd:string\">$pa</pa>\n" +
                "         <dce xsi:type=\"xsd:string\">$dce</dce>\n" +
                "         <clelot xsi:type=\"xsd:string\">$cleLot</clelot>\n" +
                "      </web:supprimer_lot_consultation_log>\n" +
                "   </soapenv:Body>\n" +
                "</soapenv:Envelope>"

        val request = MSUtils.generateDeleteLotRequest(login, password, pa, dce, cleLot)
        assertThat(request).isEqualTo(goodRequest)
    }

    @Test
    fun correct_allLots_deletion_request_generation_test(){
        val goodRequest = "<soapenv:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:web=\"https://www.marches-securises.fr/webserv/\">\n" +
                "   <soapenv:Header/>\n" +
                "   <soapenv:Body>\n" +
                "      <web:supprimer_all_lots_log soapenv:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" +
                "         <login xsi:type=\"xsd:string\">$login</login>\n" +
                "         <password xsi:type=\"xsd:string\">$password</password>\n" +
                "         <pa xsi:type=\"xsd:string\">$pa</pa>\n" +
                "         <dce xsi:type=\"xsd:string\">$dce</dce>\n" +
                "      </web:supprimer_all_lots_log>\n" +
                "   </soapenv:Body>\n" +
                "</soapenv:Envelope>"

        val request = MSUtils.generateDeleteAllLotRequest(login, password, pa, dce)
        assertThat(request).isEqualTo(goodRequest)
    }

    @Test
    fun correct_piece_creation_request_generation_test(){
        val goodRequest = "<soapenv:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:web=\"https://www.marches-securises.fr/webserv/\" xmlns:s2=\"http://xml.apache.org/xml-soap\">\n" +
                "   <soapenv:Header/>\n" +
                "   <soapenv:Body>\n" +
                "      <web:nouveau_fichier_log soapenv:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" +
                "         <login xsi:type=\"xsd:string\">$login</login>\n" +
                "         <mdp xsi:type=\"xsd:string\">$password</mdp>\n" +
                "         <pa xsi:type=\"xsd:string\">$pa</pa>\n" +
                "         <dce xsi:type=\"xsd:string\">$dce</dce>\n" +
                "         <fichier xsi:type=\"s2:Map\">\n" +
                "          <item>\n" +
                "            <key xsi:type=\"xsd:string\">lot</key>\n" +
                "            <value xsi:type=\"xsd:string\">$cleLot</value>\n" +
                "          </item>\n" +
                "          <item>\n" +
                "            <key xsi:type=\"xsd:string\">libelle</key>\n" +
                "            <value xsi:type=\"xsd:string\">$libellePiece</value>\n" +
                "          </item>\n" +
                "          <item>\n" +
                "            <key xsi:type=\"xsd:string\">la</key>\n" +
                "            <value xsi:type=\"xsd:string\">$la</value>\n" +
                "          </item>\n" +
                "          <item>\n" +
                "            <key xsi:type=\"xsd:string\">ordre</key>\n" +
                "            <value xsi:type=\"xsd:int\">$ordrePiece</value>\n" +
                "          </item>\n" +
                "          <item>\n" +
                "            <key xsi:type=\"xsd:string\">nom</key>\n" +
                "            <value xsi:type=\"xsd:string\">$nom</value>\n" +
                "          </item>\n" +
                "          <item>\n" +
                "            <key xsi:type=\"xsd:string\">extension</key>\n" +
                "            <value xsi:type=\"xsd:string\">$extension</value>\n" +
                "          </item>\n" +
                "          <item>\n" +
                "            <key xsi:type=\"xsd:string\">contenu</key>\n" +
                "            <value xsi:type=\"xsd:string\">$contenu</value>\n" +
                "          </item>\n" +
                "          <item>\n" +
                "            <key xsi:type=\"xsd:string\">poids</key>\n" +
                "            <value xsi:type=\"xsd:int\">$poids</value>\n" +
                "          </item>\n" +
                "         </fichier>\n" +
                "      </web:nouveau_fichier_log>\n" +
                "   </soapenv:Body>\n" +
                "</soapenv:Envelope>"

        val request = MSUtils.generateCreatePieceLogRequest(login, password, pa, dce, cleLot, libellePiece, la, ordrePiece, nom, extension, contenu, poids)
        assertThat(request).isEqualTo(goodRequest)
    }

    @Test
    fun correct_update_piece_request_generation_test(){
        val goodRequest = "<soapenv:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:web=\"https://www.marches-securises.fr/webserv/\">\n" +
                "   <soapenv:Header/>\n" +
                "   <soapenv:Body>\n" +
                "        <web:modifier_fichier_log soapenv:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" +
                "        <login xsi:type=\"xsd:string\">$login</login>\n" +
                "        <mdp xsi:type=\"xsd:string\">$password</mdp>\n" +
                "        <pa xsi:type=\"xsd:string\">$pa</pa>\n" +
                "        <dce xsi:type=\"xsd:string\">$dce</dce>\n" +
                "        <piece xsi:type=\"xsd:string\">$clePiece</piece>\n" +
                "        <fichier xsi:type=\"Map\" xmlns=\"http://xml.apache.org/xml-soap\">\n" +
                "          <item>\n" +
                "            <key xsi:type=\"xsd:string\">lot</key>\n" +
                "            <value xsi:type=\"xsd:string\">$cleLot</value>\n" +
                "          </item>\n" +
                "          <item>\n" +
                "            <key xsi:type=\"xsd:string\">libelle</key>\n" +
                "            <value xsi:type=\"xsd:string\">$libellePiece</value>\n" +
                "          </item>\n" +
                "          <item>\n" +
                "            <key xsi:type=\"xsd:string\">ordre</key>\n" +
                "            <value xsi:type=\"xsd:int\">$ordrePiece</value>\n" +
                "          </item>\n" +
                "          <item>\n" +
                "            <key xsi:type=\"xsd:string\">nom</key>\n" +
                "            <value xsi:type=\"xsd:string\">$nom</value>\n" +
                "          </item>\n" +
                "          <item>\n" +
                "            <key xsi:type=\"xsd:string\">extension</key>\n" +
                "            <value xsi:type=\"xsd:string\">$extension</value>\n" +
                "          </item>\n" +
                "          <item>\n" +
                "            <key xsi:type=\"xsd:string\">contenu</key>\n" +
                "            <value xsi:type=\"xsd:string\">$contenu</value>\n" +
                "          </item>\n" +
                "          <item>\n" +
                "            <key xsi:type=\"xsd:string\">poids</key>\n" +
                "            <value xsi:type=\"xsd:int\">$poids</value>\n" +
                "          </item>\n" +
                "        </fichier>\n" +
                "    </web:modifier_fichier_log>\n" +
                "   </soapenv:Body>\n" +
                "</soapenv:Envelope>"

        val request = MSUtils.generateModifyPieceLogRequest(login, password, pa, dce, clePiece, cleLot, libellePiece, ordrePiece, nom, extension, contenu, poids)
        assertThat(request).isEqualTo(goodRequest)
    }

    @Test
    fun correct_piece_deletion_request_generation_test(){
        val goodRequest = "<soapenv:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:web=\"https://www.marches-securises.fr/webserv/\">\n" +
                "   <soapenv:Header/>\n" +
                "   <soapenv:Body>\n" +
                "      <web:supprimer_fichier_log soapenv:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" +
                "         <login xsi:type=\"xsd:string\">$login</login>\n" +
                "         <mdp xsi:type=\"xsd:string\">$password</mdp>\n" +
                "         <pa xsi:type=\"xsd:string\">$pa</pa>\n" +
                "         <dce xsi:type=\"xsd:string\">$dce</dce>\n" +
                "         <piece xsi:type=\"xsd:string\">$clePiece</piece>\n" +
                "      </web:supprimer_fichier_log>\n" +
                "   </soapenv:Body>\n" +
                "</soapenv:Envelope>"

        val request = MSUtils.generateDeletePieceRequest(login, password, pa, dce, clePiece)
        assertThat(request).isEqualTo(goodRequest)
    }
}