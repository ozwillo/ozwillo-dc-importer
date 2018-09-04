package org.ozwillo.dcimporter.util

//TODO: temp solution until soap response parsing resolution
enum class SoapConsultationResponse(val value: String){
    CREATION_OK("<propriete nom=\"ref_interne\" statut=\"changed\">"),
    PROCESS_OK("<propriete nom=\"cle\">"),
    DCE_ATTRIBUTED("<propriete nom=\"cle\" statut=\"changed\">"),
    UPDATE_FAILED_BAD_DCE("<propriete nom=\"load_consultation_fail\" statut=\"not_changed\" message=\"no_consultation\">no_consultation</propriete>"),
    DELETE_OK("<consultation_suppr_ok etat_consultation=\"supprimee\"/>"),
    DELETION_FAILED_BAD_DCE("<consultation_cle_error"),
    DELETION_FAILED_BAD_PA("<pa_suppr_dce_error"),
    DELETION_FAILED_UNKNOWN_PASSWORD("<log_error"),
    PUBLICATION_REJECTED("<validation_erreur"),
    PUBLICATION_FAILED_NOT_FOUND("<dce_error"),
    PUBLICATION_FAILED_BAD_PA("<pa_error"),
    PUBLICATION_FAILED_UNKNOWN_PASSWORD("<identification_error")
}

enum class SoapLotResponse(val value: String){
    CREATION_OK("<propriete nom=\"ordre\">"),
    LOT_KEY_PROPERTY("<propriete nom=\"cle_lot\">"),
    BAD_DCE("<propriete nom=\"load_dce_error\">error</propriete>"),
    UPDATE_NO_CHANGE_OK("<propriete nom=\"ordre\">"),
    UPDATE_CHANGE_OK("<propriete nom=\"ordre\" statut=\"changed\">"),
    NOT_FOUND("<propriete nom=\"load_lot_error\">error</propriete>"),
    DELETE_OK("<objet type=\"ms_v2__fullweb_lot\">")

}

enum class SoapPieceResponse(val value: String){
    CREATION_OK("<propriete nom=\"nom\">"),
    PIECE_KEY_PROPERTY("<propriete nom=\"cle_piece\">"),
    CREATION_FAILED_FILE_ALREADY_EXIST("<propriete nom=\"fichier_error\"> file_exist</propriete>"),
    CREATION_FAILED_BAD_DCE("<consultation_non_trouvee"),
    BAD_PA("<pa_non_trouvee"),
    UNKNOWN_PASSWORD("<logs_non_trouves"),
    DELETE_OK("<objet type=\"ms_v2__fullweb_piece\">"),
    NOT_FOUND("<cle_piece_non_trouvee"),
    DELETE_FAILED_BAD_DCE("<cle_dce_non_trouvee")
}

enum class SoapGeneralFailureResponse(val value: String){
    BAD_PA("<propriete nom=\"load_pa_error\">error</propriete>"),
    UNKNOWN_PASSWORD("<return xsi:nil=\"true\"/>")
}

enum class SoapGeneralResponse(val value: String){
    DELETE_LAST_OK("<propriete suppression=\"true\">supprime</propriete>")
}