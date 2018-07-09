package org.ozwillo.dcimporter.web.marchesecurise.rabbitMQ;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.ozwillo.dcimporter.AbstractIntegrationTests;
import org.ozwillo.dcimporter.config.DatacoreProperties;
import org.ozwillo.dcimporter.model.datacore.DCBusinessResourceLight;
import org.ozwillo.dcimporter.model.datacore.DCResourceLight;
import org.ozwillo.dcimporter.model.marchepublic.Consultation;
import org.ozwillo.dcimporter.model.marchepublic.FinaliteMarcheType;
import org.ozwillo.dcimporter.model.marchepublic.TypeMarcheType;
import org.ozwillo.dcimporter.model.marchepublic.TypePrestationType;
import org.ozwillo.dcimporter.service.DatacoreService;
import org.ozwillo.dcimporter.service.marchesecurise.rabbitMQ.JsonConverter;
import org.ozwillo.dcimporter.service.marchesecurise.rabbitMQ.SenderMS;
import org.ozwillo.dcimporter.web.MarchePublicHandler;
import org.springframework.amqp.AmqpException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class CreationConsultationSenderTest{

    @Autowired
    SenderMS sender;

    @Autowired
    DatacoreProperties datacoreProperties;

    @Autowired
    TestRestTemplate testRestTemplate;


    private WireMockServer wireMockServer;

    private final String SIRET = "123456789";
    private final String REFERENCE = "ref-consultation";
    private final String BEARER = "eyJpZCI6IjE0MmQyNzI1LTI2NzMtNDRkNi04ZDBhLWYzNjZmNjE0YWQzYy95MXlyZ2VuTmhLSnpmNWktQmlyTWh3IiwiaWF0IjoxNTMxMTE5MzI0LjgwMjAwMDAwMCwiZXhwIjoxNTMxMTIyOTI0LjgwMjAwMDAwMH0";

    @BeforeAll
    private void setUp(){
        wireMockServer = new WireMockServer(wireMockConfig().port(8089));
        wireMockServer.start();

    }

    @AfterAll
    private void tearDwon(){
        wireMockServer.stop();
    }


    @Test
    public void sendMessage() throws InterruptedException {

        String reference = REFERENCE;
        String objet = "test objet";
        LocalDateTime datePublication = LocalDateTime.now();
        LocalDateTime dateCloture = LocalDateTime.now();
        FinaliteMarcheType finaliteMarche = FinaliteMarcheType.AUTRE;
        TypeMarcheType typeMarche = TypeMarcheType.AUTRE;
        TypePrestationType typePrestation = TypePrestationType.AUTRES;
        List<Integer> departementsPrestation = Arrays.asList(74, 38, 69, 06);
        String passation = "test passation";
        boolean informatique = true;
        String passe = "test passe";
        List<String> emails = Arrays.asList("test@test.com");
        boolean enLigne = true;
        boolean alloti = true;
        boolean invisible = true;
        int nbLots = 0;

        Consultation consultation = new Consultation(reference, objet, datePublication, dateCloture, finaliteMarche, typeMarche, typePrestation, departementsPrestation, passation, informatique, passe, emails, enLigne, alloti, invisible, nbLots);

        DCResourceLight dcConsultation = consultation.toDcObject(datacoreProperties.baseUri,SIRET, REFERENCE);

        try {
            sender.send(dcConsultation, BEARER, "create");
        }catch (AmqpException e){
            e.getMessage();
        }
    }
}