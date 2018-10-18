package org.ozwillo.dcimporter.utils

class DCReturnModel{

    companion object {

        val siret = "123456789"
        val grantedSiret = "987654321"
        val referenceConsultation = "ref-consultation"
        val uuidLot = "lotUUID"
        val uuidPiece = "pieceUUID"
        val cleRegistre = "1533048729cetzyl2xvn78"
        val clePersonne = "1335945366ODE0aP0Xgd"


        val tokenInfoResponse = """
        {
            "access_token": "secretToken",
            "expires_in": 3600,
            "scope": "datacore openid profile offline_access email",
            "token_type": "Bearer"
        }
        """

        val dcPostOrganizationResponse = """
        {
            "@id": "https://data.ozwillo.com/dc/type/orgfr:Organisation_0/FR/$grantedSiret"
        }
        """

        val dcPostPersonResponse = """
           {
                "@id": "http://data.ozwillo.com/dc/type/marchepublic:personne_0/$clePersonne",
                "o:version": 0
           }
        """

        val dcPostRegistreReponseResponse = """
            {
                "@id": "http://data.ozwillo.com/dc/type/marchepublic:retrait_0/FR/$siret/$referenceConsultation/$cleRegistre",
                "o:version": 0
           }
        """

        val dcPostRegistreRetraitResponse = """
            {
                "@id": "http://data.ozwillo.com/dc/type/marchepublic:retrait_0/FR/$siret/$referenceConsultation/$cleRegistre",
                "o:version": 0
           }
        """

        val dcGetPersonResponse = """
            {
                "@id": "http://data.ozwillo.com/dc/type/marchepublic:personne_0/$clePersonne",
                "o:version": 0,
                "@type": [
                  "marchepublic:personne_0"
                ],
                "dc:created": "2018-10-16T15:53:39.000+02:00",
                "dc:modified": "2018-10-16T15:53:39.156+02:00",
                "dc:creator": "2239bc06-5f33-49dd-af99-b5c87da055ab",
                "dc:contributor": "2239bc06-5f33-49dd-af99-b5c87da055ab",
                "mppersonne:prenom": "Bunny",
                "mppersonne:email": "test@test.com",
                "mppersonne:fax": "",
                "mppersonne:genre": "m",
                "mppersonne:nom": "BUGS",
                "mppersonne:tel": "artichaut"
            }
        """

        val dcGetConsultationResponse = """
        [
            {
                "@id": "http://data.ozwillo.com/dc/type/marchepublic:consultation_0/FR/$siret/$referenceConsultation",
                "mpconsultation:organization": "http://data.ozwillo.com/dc/type/orgfr:Organisation_0/FR/$siret",
                "mpconsultation:reference": "$referenceConsultation",
                "mpconsultation:etat": "PUBLISHED",
                "mpconsultation:departementsPrestation": [],
                "mpconsultation:objet": "",
                "mpconsultation:datePublication": "2018-09-13T00:00:48.278Z",
                "mpconsultation:dateCloture": "2018-09-13T00:00:48.278Z",
                "mpconsultation:typePrestation": "SERVICES",
                "mpconsultation:finaliteMarche": "ACCORD",
                "mpconsultation:typeMarche": "ORDONNANCE2005",
                "mpconsultation:informatique": false,
                "mpconsultation:invisible": false,
                "mpconsultation:passation": "AORA",
                "mpconsultation:enLigne": false,
                "mpconsultation:alloti": true,
                "mpconsultation:nbLots": 1,
                "mpconsultation:emails": []
            }
        ]
        """

        val dcGetAllConsultationResponse = """
        [
            {
                "@id": "http://data.ozwillo.com/dc/type/marchepublic:consultation_0/FR/$siret/$referenceConsultation",
                "mpconsultation:organization": "http://data.ozwillo.com/dc/type/orgfr:Organisation_0/FR/$siret",
                "mpconsultation:reference": "$referenceConsultation",
                "mpconsultation:etat": "PUBLISHED",
                "mpconsultation:departementsPrestation": [],
                "mpconsultation:objet": "",
                "mpconsultation:datePublication": "2018-09-13T00:00:48.278Z",
                "mpconsultation:dateCloture": "2018-09-13T00:00:48.278Z",
                "mpconsultation:typePrestation": "SERVICES",
                "mpconsultation:finaliteMarche": "ACCORD",
                "mpconsultation:typeMarche": "ORDONNANCE2005",
                "mpconsultation:informatique": false,
                "mpconsultation:invisible": false,
                "mpconsultation:passation": "AORA",
                "mpconsultation:enLigne": false,
                "mpconsultation:alloti": true,
                "mpconsultation:nbLots": 1,
                "mpconsultation:emails": []
            },
            {
                "@id": "http://data.ozwillo.com/dc/type/marchepublic:consultation_0/FR/$siret/$referenceConsultation-2",
                "mpconsultation:organization": "http://data.ozwillo.com/dc/type/orgfr:Organisation_0/FR/$siret",
                "mpconsultation:reference": "$referenceConsultation-2",
                "mpconsultation:etat": "PUBLISHED",
                "mpconsultation:departementsPrestation": [],
                "mpconsultation:objet": "",
                "mpconsultation:datePublication": "2018-09-13T00:00:48.278Z",
                "mpconsultation:dateCloture": "2018-09-13T00:00:48.278Z",
                "mpconsultation:typePrestation": "SERVICES",
                "mpconsultation:finaliteMarche": "ACCORD",
                "mpconsultation:typeMarche": "ORDONNANCE2005",
                "mpconsultation:informatique": false,
                "mpconsultation:invisible": false,
                "mpconsultation:passation": "AORA",
                "mpconsultation:enLigne": false,
                "mpconsultation:alloti": true,
                "mpconsultation:nbLots": 1,
                "mpconsultation:emails": []
            },
            {
                "@id": "http://data.ozwillo.com/dc/type/marchepublic:consultation_0/FR/$siret/$referenceConsultation-3",
                "mpconsultation:organization": "http://data.ozwillo.com/dc/type/orgfr:Organisation_0/FR/$siret",
                "mpconsultation:reference": "$referenceConsultation-3",
                "mpconsultation:etat": "PUBLISHED",
                "mpconsultation:departementsPrestation": [],
                "mpconsultation:objet": "",
                "mpconsultation:datePublication": "2018-09-13T00:00:48.278Z",
                "mpconsultation:dateCloture": "2018-09-13T00:00:48.278Z",
                "mpconsultation:typePrestation": "SERVICES",
                "mpconsultation:finaliteMarche": "ACCORD",
                "mpconsultation:typeMarche": "ORDONNANCE2005",
                "mpconsultation:informatique": false,
                "mpconsultation:invisible": false,
                "mpconsultation:passation": "AORA",
                "mpconsultation:enLigne": false,
                "mpconsultation:alloti": true,
                "mpconsultation:nbLots": 1,
                "mpconsultation:emails": []
            }
        ]
        """

        val dcGetAllRegisterResponse = """
        [
            {
                "@id": "http://data.ozwillo.com/dc/type/marchepublic:reponse_0/FR/$siret/$referenceConsultation/$cleRegistre",
                "mpreponse:contact": "ANAME",
                "mpreponse:mscle": "$cleRegistre",
                "mpreponse:email": "un mail",
                "mpreponse:dateDepot": "2018-08-03T14:47:05.000Z",
                "mpreponse:poids": 4105705
            }
        ]
        """
        val dcGetAllRegisterRetrait = """
        [
          {
            "@id": "http://data.ozwillo.com/dc/type/marchepublic:retrait_0/FR/$siret/$referenceConsultation/$cleRegistre",
            "mpretrait:mscle": "$cleRegistre",
            "mpretrait:dateFin": "2018-08-08T17:54:24.000Z",
            "mpretrait:piece": "http://data.ozwillo.com/dc/type/marchepublic:piece_0/FR/$siret/$referenceConsultation/$uuidPiece",
            "mpretrait:personne": "http://data.ozwillo.com/dc/type/marchepublic:personne_0/$clePersonne",
            "mpretrait:entreprise": "http://data.ozwillo.com/dc/type/orgfr:Organisation_0/FR/987654321",
            "mpretrait:nomPiece": "CCAP.pdf",
            "mpretrait:libellePiece": "",
            "mpretrait:dateDebut": "2018-08-08T17:54:24.000Z"
          }
        ]
        """

        val dcGetRegistreReponse = """
            {
                "@id": "http://data.ozwillo.com/dc/type/marchepublic:reponse_0/FR/$siret/$referenceConsultation/$cleRegistre",
                "o:version": 0,
                "mpreponse:consultation": "http://data.ozwillo.com/dc/type/marchepublic:consultation_0/FR/$siret/$referenceConsultation",
                "mpreponse:contact": "Afirst-And LastName",
                "mpreponse:mscle": "$cleRegistre",
                "mpreponse:email": "test@test.com",
                "mpreponse:dateDepot": "2018-08-03T14:47:05.000Z",
                "mpreponse:poids": 4105705
            }
        """

        val dcGetRegistreRetrait = """
            {
                "@id": "http://data.ozwillo.com/dc/type/marchepublic:retrait_0/FR/$siret/$referenceConsultation/$cleRegistre",
                "o:version": 0,
                "mpretrait:mscle": "$cleRegistre",
                "mpretrait:consultation": "http://data.ozwillo.com/dc/type/marchepublic:consultation_0/FR/$siret/$referenceConsultation",
                "mpretrait:dateFin": "2018-08-08T17:54:24.000Z",
                "mpretrait:piece": "http://data.ozwillo.com/dc/type/marchepublic:piece_0/FR/$siret/$referenceConsultation/$uuidPiece",
                "mpretrait:personne": "http://data.ozwillo.com/dc/type/marchepublic:personne_0/$clePersonne",
                "mpretrait:entreprise": "http://data.ozwillo.com/dc/type/orgfr:Organisation_0/FR/$grantedSiret",
                "mpretrait:nomPiece": "Unnom.ext",
                "mpretrait:libellePiece": "Un libellé de pièce",
                "mpretrait:dateDebut": "2018-08-08T17:54:24.000Z"
            }
        """

        val dcGetOrganizationForRegistre = """
        {
            "@id": "http://data.ozwillo.com/dc/type/orgfr:Organisation_0/FR/987654321",
            "o:version": 0,
            "adrpost:streetAndNumber": "UNE ADRESSE",
            "org:country": "http://data.ozwillo.com/dc/type/geocofr:Pays_0/FR",
            "org:phoneNumber": "telephone : un telephone, fax : un fax",
            "org:webSite": "une url",
            "org:regNumber": "987654321",
            "adrpost:country": "http://data.ozwillo.com/dc/type/geocofr:Pays_0/FR",
            "org:legalName": [
              {
                "l": "fr",
                "v": "ANAME"
              }
            ],
            "adrpost:postCode": "11111"
        }
        """

        val dcGetEmpty = """
        []
        """

        val dcGetTheOnlyConsultationResponse = """
        [
            {
                "@id": "http://data.ozwillo.com/dc/type/marchepublic:consultation_0/FR/$siret/$referenceConsultation",
                "mpconsultation:organization": "http://data.ozwillo.com/dc/type/orgfr:Organisation_0/FR/$siret",
                "mpconsultation:reference": "$referenceConsultation",
                "mpconsultation:etat": "PUBLISHED",
                "mpconsultation:departementsPrestation": [],
                "mpconsultation:objet": "",
                "mpconsultation:datePublication": "2018-09-13T00:00:48.278Z",
                "mpconsultation:dateCloture": "2018-09-13T00:00:48.278Z",
                "mpconsultation:typePrestation": "SERVICES",
                "mpconsultation:finaliteMarche": "ACCORD",
                "mpconsultation:typeMarche": "ORDONNANCE2005",
                "mpconsultation:informatique": false,
                "mpconsultation:invisible": false,
                "mpconsultation:passation": "AORA",
                "mpconsultation:enLigne": false,
                "mpconsultation:alloti": true,
                "mpconsultation:nbLots": 1,
                "mpconsultation:emails": []
            }
        ]
        """

        val dcGetOrganizationResponse = """
        {
            "@id": "https://data.ozwillo.com/dc/type/orgfr:Organisation_0/FR/$siret"
        }
        """

        val dcPostConsultationResponse = """
        {
            "@id": "http://data.ozwillo.com/dc/type/marchepublic:consultation_0/FR/$siret/$referenceConsultation"
        }
        """

        val dcExistingResponse = """
            {
                "@id": "http://data.ozwillo.com/dc/type/marchepublic:consultation_0/FR/$siret/$referenceConsultation",
                "o:version": "0"
            }
            """

        val dcPostLotResponse = """
        {
            "@id": "http://data.ozwillo.com/dc/type/marchepublic:lot_0/FR/$siret/$referenceConsultation/$uuidLot"
        }
        """

        val dcExistingLotResponse = """
            {
                "@id": "http://data.ozwillo.com/dc/type/marchepublic:lot_0/FR/$siret/$referenceConsultation/$uuidLot",
                "o:version": "0"
            }
            """
    }
}