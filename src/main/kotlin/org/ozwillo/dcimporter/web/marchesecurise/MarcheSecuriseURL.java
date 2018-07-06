package org.ozwillo.dcimporter.web.marchesecurise;

class MarcheSecuriseURL{
    private static final String CREATE_CONSULTATION_URL = "https://www.marches-securises.fr/webserv/?module=dce|serveur_crea_dce";
    private static final String MODIFY_CONSULTATION_URL = "https://www.marches-securises.fr/webserv/?module=dce|serveur_modif_dce";
    private static final String DELETE_CONSULTATION_URL = "https://www.marches-securises.fr/webserv/?module=dce|serveur_suppr_dce";

    private static final String LOT_URL = "https://www.marches-securises.fr/webserv/?module=dce|serveur_lot_dce";

    public static String getCreateConsultationUrl() {
        return CREATE_CONSULTATION_URL;
    }

    public static String getModifyConsultationUrl() {
        return MODIFY_CONSULTATION_URL;
    }

    public static String getDeleteConsultationUrl() {
        return DELETE_CONSULTATION_URL;
    }

    public static String getLotUrl() {
        return LOT_URL;
    }
}