package org.ozwillo.dcimporter.service;

import org.oasis_eu.spring.datacore.DatacoreClient;
import org.oasis_eu.spring.datacore.model.DCOperator;
import org.oasis_eu.spring.datacore.model.DCQueryParameters;
import org.oasis_eu.spring.datacore.model.DCResource;
import org.ozwillo.dcimporter.config.Prop;
import org.ozwillo.dcimporter.model.FormModel;
import org.ozwillo.dcimporter.model.ListFormsModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import org.apache.commons.codec.binary.Base64;

@Service
public class PublikService {

	private static final Logger LOGGER = LoggerFactory.getLogger(PublikService.class);

	private RestTemplate restTemplate = new RestTemplate();

	private final DatacoreClient datacoreClient;
	private final SystemUserService systemUserService;
	private final Prop props;

	@Value("${publik.formTypeEM}")
	private String formTypeEM;
	@Value("${publik.formTypeSVE}")
	private String formTypeSVE;
	@Value("${publik.algo}")
	private String algo;
	@Value("${publik.orig}")
	private String orig;
	@Value("${publik.secret}")
	private String secret;
	@Value("${publik.datacore.project}")
	private String datacoreProject;
	@Value("${publik.datacore.modelEM}")
	private String datacoreModelEM;
	@Value("${publik.datacore.modelSVE}")
	private String datacoreModelSVE;
	@Value("${publik.datacore.modelORG}")
	private String datacoreModelORG;
	@Value("${publik.datacore.modelUser}")
	private String datacoreModelUser;
	@Value("${datacore.baseUri}")
	private String datacoreBaseUri;

	@Autowired
	public PublikService(DatacoreClient datacoreClient, SystemUserService systemUserService, Prop props) {
		this.datacoreClient = datacoreClient;
		this.systemUserService = systemUserService;
		this.props = props;
	}

	private FormModel getForm(String url) throws URISyntaxException {

		URI url_finale = sign_url(url);
		LOGGER.error("URL get Form {}", url_finale);

		return restTemplate.getForObject(url_finale, FormModel.class);
	}

	public void syncPublikForms(String baseUrl, DCResource dcOrganization, String formType) {

		String initUrl = baseUrl + "/api/forms/" + formType + "/list?anonymise";

		try {
			URI url = sign_url(initUrl);
			LOGGER.debug("Calling Publik at URL {}", url);
			ListFormsModel[] forms = restTemplate.getForObject(url, ListFormsModel[].class);

			for (ListFormsModel f : forms) {
				FormModel formModel = getForm(formatUrl(f.getUrl()));
				DCResource dcResource = convertToDCResource(dcOrganization, formModel);
				systemUserService.runAs(() -> datacoreClient.saveResource(datacoreProject, dcResource));
			}
		} catch (URISyntaxException e) {
			LOGGER.error("Exception Uri Syntax : " + e);
		} catch (MalformedURLException e) {
			LOGGER.error("MalformedURLException : " + e);
		}
	}

	public void saveResourceToDC(FormModel form) {

		String orgLegalName = null;
		for (Map<String, String> instance : props.getInstance()) {
			if (form.getUrl().contains(instance.get("baseUrl")))
				orgLegalName = instance.get("organization");
		}

        Optional<DCResource> dcOrganization = getDCOrganization(orgLegalName);
		if (!dcOrganization.isPresent()) {
            LOGGER.error("Unable to get organization {}", orgLegalName);
            return;
        }

		systemUserService.runAs(() -> datacoreClient.saveResource(datacoreProject, convertToDCResource(dcOrganization.get(), form)));
	}

	private DCResource convertToDCResource(DCResource dcOrganization, FormModel form) {

        DCResource dcResource = new DCResource();

		dcResource.setBaseUri(datacoreBaseUri);

		dcResource.setIri(dcOrganization.getIri() + "/" + form.getDisplay_id());

		dcResource.set("citizenreq:displayId", form.getDisplay_id());
		dcResource.set("citizenreq:lastUpdateTime", form.getLast_update_time());
		dcResource.set("citizenreq:displayName", form.getDisplay_name());
		dcResource.set("citizenreq:submissionChannel", form.getSubmission().getChannel());
		dcResource.set("citizenreq:submissionBackoffice", form.getSubmission().getBackoffice().toString());
		dcResource.set("citizenreq:url", form.getUrl());
		dcResource.set("citizenreq:receiptTime", form.getReceipt_time());

		dcResource.set("citizenreq:criticalityLevel", form.getCriticality_level().toString());
		dcResource.set("citizenreq:id", form.getId());
		dcResource.set("citizenreq:organization", dcOrganization.getUri());

		if (form.getUser() != null)
			dcResource.set("citizenreq:user", createUserDCResource(form));

		if (isEMrequest(form)) {
			convertToDCResourceEM(dcResource, form);
		} else if (isSVErequest(form)) {
			convertToDCResourceSVE(dcResource, form);
		}
		LOGGER.debug("DCResouce --> :" + dcResource.toString());
		return dcResource;
	}

	private DCResource convertToDCResourceEM(DCResource dcResource, FormModel form) {

		dcResource.setType(datacoreModelEM);

		dcResource.set("citizenreqem:familyName", form.getFields().get("nom_famille").toString());
		dcResource.set("citizenreqem:firstName", form.getFields().get("prenom").toString());
		dcResource.set("citizenreqem:phone", form.getFields().get("telephone").toString());
		if (form.getFields().get("detail") != null)
			dcResource.set("citizenreqem:detail", form.getFields().get("detail").toString());
		dcResource.set("citizenreqem:objectSummary", form.getFields().get("objet_rendez_vous_raw").toString());
		dcResource.set("citizenreqem:objectDetail", form.getFields().get("objet_rendez_vous").toString());
		dcResource.set("citizenreqem:desiredDate", form.getFields().get("date_souhaitee").toString());
		dcResource.set("citizenreqem:email", form.getFields().get("courriel").toString());

		return dcResource;
	}

	private DCResource convertToDCResourceSVE(DCResource dcResource, FormModel form) {

		dcResource.setType(datacoreModelSVE);

		dcResource.set("citizenreqsve:title", form.getFields().get("civilite").toString());
		dcResource.set("citizenreqsve:familyName", form.getFields().get("nom").toString());
		dcResource.set("citizenreqsve:firstName", form.getFields().get("prenoms").toString());
		dcResource.set("citizenreqsve:email", form.getFields().get("email").toString());
		dcResource.set("citizenreqsve:streetAddress", form.getFields().get("voie").toString());
		dcResource.set("citizenreqsve:zipCode", form.getFields().get("code_postal").toString());
		dcResource.set("citizenreqsve:city", form.getFields().get("commune").toString());
		dcResource.set("citizenreqsve:entityType", form.getFields().get("entite_raw").toString());
		dcResource.set("citizenreqsve:object", form.getFields().get("objet").toString());
		dcResource.set("citizenreqsve:message", form.getFields().get("message").toString());

		@SuppressWarnings("unchecked")
		HashMap<String, String> doc = (HashMap<String, String>) form.getFields().get("doc");
		if (doc != null) {
			dcResource.set("citizenreqsve:docContent", doc.get("content"));
			dcResource.set("citizenreqsve:docFieldId", doc.get("field_id"));
			dcResource.set("citizenreqsve:docContentType", doc.get("content_type"));
			dcResource.set("citizenreqsve:docFileName", doc.get("filename"));
		}
		if (form.getFields().get("siret") != null)
			dcResource.set("citizenreqsve:siret", form.getFields().get("siret").toString());
		if (form.getFields().get("siret_entreprise") != null)
			dcResource.set("citizenreqsve:siret", form.getFields().get("siret_entreprise").toString());
		if (form.getFields().get("rna") != null)
			dcResource.set("citizenreqsve:rna", form.getFields().get("rna").toString());
		if (form.getFields().get("nom_entite_entreprise") != null)
			dcResource.set("citizenreqsve:legalName", form.getFields().get("nom_entite_entreprise").toString());
		if (form.getFields().get("nom_entite") != null)
			dcResource.set("citizenreqsve:entityName", form.getFields().get("nom_entite").toString());
		return dcResource;
	}

	private String createUserDCResource(FormModel form) {

		DCResource dcResource = new DCResource();

		dcResource.setBaseUri(datacoreBaseUri);
		dcResource.setType(datacoreModelUser);
		dcResource.setIri(form.getUser().getNameID()[0]);

		systemUserService.runAs(() -> {
			if (datacoreClient.getResourceFromURI(datacoreProject, dcResource.getUri()).getResource() == null) {

				dcResource.set("citizenrequser:email", form.getUser().getEmail());
				dcResource.set("citizenrequser:nameID", form.getUser().getNameID()[0]);
				dcResource.set("citizenrequser:userId", form.getUser().getId().toString());
				dcResource.set("citizenrequser:name", form.getUser().getName());
				datacoreClient.saveResource(datacoreProject, dcResource);
			}
		});

		return dcResource.getUri();
	}

	public Optional<DCResource> getDCOrganization(String orgLegalName) {
        DCQueryParameters queryParametersOrg = new DCQueryParameters("org:legalName", DCOperator.EQ, orgLegalName);
        List<DCResource> resources = new ArrayList<>();
		systemUserService.runAs(() -> {
			resources.addAll(datacoreClient
					.findResources(datacoreProject, datacoreModelORG, queryParametersOrg, 0, 1));
		});

		return resources.isEmpty() ? Optional.empty() : Optional.of(resources.get(0));
    }

	/**
	 * Calculate a signature with sha256
	 * 
	 * @return
	 */
	private String calculateSignature(String message, String key) {

		try {

			Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
			SecretKeySpec secret_key = new SecretKeySpec(key.getBytes(), "HmacSHA256");
			sha256_HMAC.init(secret_key);

			String hash = Base64.encodeBase64String(sha256_HMAC.doFinal(message.getBytes()));
			LOGGER.debug("Signature : " + hash);

			hash = URLEncoder.encode(hash, "UTF-8");
			LOGGER.debug("URL encoded hash : " + hash);

			return hash;
		} catch (Exception e) {
			LOGGER.error("Exception when calculate the signature : " + e);
			return null;
		}

	}

	private URI sign_url(String url) throws URISyntaxException {

		TimeZone tz = TimeZone.getTimeZone("UTC");
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		df.setTimeZone(tz);
		String thisMoment = df.format(new Date());

		Random random = new Random();
		// create byte array
		byte[] nbyte = new byte[16];
		// put the next byte in the array
		random.nextBytes(nbyte);
		String nonce = DatatypeConverter.printHexBinary(nbyte);

		try {
			String newQuery = "";
			URL parsedUrl = new URL(url);

			if (parsedUrl.getQuery() != null)
				newQuery = parsedUrl.getQuery() + "&";

			newQuery += "algo=" + this.algo + "&timestamp=" + URLEncoder.encode(thisMoment, "UTF-8") + "&nonce=" + nonce
					+ "&orig=" + URLEncoder.encode(this.orig, "UTF-8");
			String signature = calculateSignature(newQuery, this.secret);
			newQuery += "&signature=" + signature;

			return new URI(
					parsedUrl.getProtocol() + "://" + parsedUrl.getHost() + parsedUrl.getPath() + "?" + newQuery);
		} catch (MalformedURLException e) {
			LOGGER.error("MalformedURLException : " + e);
			return null;
		} catch (UnsupportedEncodingException e) {
			LOGGER.error("Unsupported encoding exception !?");
			return null;
		}
	}

	private String formatUrl(String url_base) throws MalformedURLException {

		URL parsedUrl = new URL(url_base);
		String url_finale = "https://" + parsedUrl.getHost() + "/api/forms" + parsedUrl.getPath();
		return url_finale;
	}

	private Boolean isEMrequest(FormModel form) {

		if (form.getId().contains(this.formTypeEM)) {
			return true;
		}
		return false;
	}

	private Boolean isSVErequest(FormModel form) {

		if (form.getId().contains(this.formTypeSVE)) {
			return true;
		}
		return false;
	}

}