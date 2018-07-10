package org.ozwillo.dcimporter.web.marchesecurise.rabbitMQ;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.ozwillo.dcimporter.config.DatacoreProperties;
import org.ozwillo.dcimporter.model.marchepublic.Consultation;
import org.ozwillo.dcimporter.model.marchepublic.FinaliteMarcheType;
import org.ozwillo.dcimporter.model.marchepublic.TypeMarcheType;
import org.ozwillo.dcimporter.model.marchepublic.TypePrestationType;
import org.ozwillo.dcimporter.service.DatacoreService;
import org.ozwillo.dcimporter.service.marchesecurise.rabbitMQ.SenderMS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;


@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class SenderWithConsultationFormatTest {

    @Autowired
    SenderMS sender;

    private final String SIRET = "123456789";
    private final String REFERENCE = "ref-consultation";
    private final String BEARER = "eyJpZCI6IjgxZTEzZTBiLWVlYjktNDgyOC05NWM4LWJmNmJlNjAyNTA1Yy9lQkQ0aVRiX1lOVUdqOXZwbDdPbXlBIiwiaWF0IjoxNTMxMTQ5NzkwLjg4OTAwMDAwMCwiZXhwIjoxNTMxMTUzMzkwLjg4OTAwMDAwMH0";


    //TODO:Modifier paramètre JsonConverter, SenderMS et ReciverMS pour passer Consultation au lieu de DCRessourceLight

    @Test
    public void send_ConsultationFormatJson_by_rabbitMQ() throws InterruptedException {

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

        /*try {
            sender.send(consultation, "consultation", "create");
        }catch (AmqpException e){
            e.getMessage();
        }*/
    }
}