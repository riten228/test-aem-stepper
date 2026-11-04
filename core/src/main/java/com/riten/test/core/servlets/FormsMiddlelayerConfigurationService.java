package com.riten.test.core.servlets;

import org.apache.sling.settings.SlingSettingsService;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@Component(
        service = FormsMiddlelayerConfigurationService.class,
        immediate = true,
        property = {
                Constants.SERVICE_DESCRIPTION + "=Insurance - This service is used for configuring forms middlelayer settings",
        }
)
@Designate(ocd = FormsMiddlelayerConfigurationServiceConfig.class)
public class FormsMiddlelayerConfigurationService {

    private static final Logger LOG = LoggerFactory.getLogger(FormsMiddlelayerConfigurationService.class);

    private String submitPath;
    private String submitLeadPath;
    private String submitBusinessPath;
    private String submitEventPath;
    private String submitInscriptionPath;
    private String submitSalePath;
    private String submitSurveyPath;
    private String unsubscribePath;
    private String cpdbPath;
    private String csCalculationPath;
    private String commercialMailPath;
    private String generalPrefillPath;
    private String externalPrefillPath;
    private String leadsForConsultantPrefillPath;
    private String checkAlreadySubmittedPath;
    private String checkAlreadySubmittedInscriptionPath;
    private String autoSavePath;
    private String acceptPath;
    private String reassignPath;
    private String reassignRequestPath;
    private String closePath;
    private String creditSimulationSubmitPath;
    private String thanksPagePath;
    private String errorPagePath;
    private String acceptedPagePath;
    private String reassignRequestedPagePath;
    private String reassignedPagePath;
    private String closedPagePath;
    private String newsletterSubscribePath;
    private String newsletterSubscribeTopicKey;
    private String newsletterAcceptPath;
    private String makeAppointmentPath;
    private String checkLeadStatusPath;
    private String verifyEmailPath;
    private String feedbackAcceptPath;
    private String feedbackAcceptPathPost;
    private String[] hiddenFields;
    private String[] hiddenProperties;
    private String leadsForConsultantPath;
    private String submitLeadFeedbackPath;
    private String[] questionsAnswersFlowIds;
    @Activate
    public void configurationModified(FormsMiddlelayerConfigurationServiceConfig config) {
        LOG.info("Updating/Modifying configuration, forms middlelayer");
        submitPath = config.submitPath();
        submitLeadPath = config.submitLeadPath();
        submitBusinessPath = config.submitBusinessPath();
        submitEventPath = config.submitEventPath();
        submitInscriptionPath = config.submitInscriptionPath();
        submitSalePath = config.submitSalePath();
        submitSurveyPath = config.submitSurveyPath();
        unsubscribePath = config.unsubscribePath();
        cpdbPath = config.cpdbPath();
        csCalculationPath = config.csCalculationPath();
        commercialMailPath = config.commercialMailPath();
        generalPrefillPath = config.generalPrefillPath();
        externalPrefillPath = config.externalPrefillPath();
        leadsForConsultantPrefillPath=config.leadsForConsultantPrefillPath();
        checkAlreadySubmittedPath= config.checkAlreadySubmittedPath();
        checkAlreadySubmittedInscriptionPath = config.checkAlreadySubmittedInscriptionPath();
        autoSavePath = config.autoSavePath();
        acceptPath = config.acceptPath();
        leadsForConsultantPath = config.leadsForConsultantPath();
        feedbackAcceptPath = config.feedbackAcceptPath();
        feedbackAcceptPathPost = config.feedbackAcceptPathPost();
        reassignPath = config.reassignPath();
        reassignRequestPath = config.reassignRequestPath();
        closePath = config.closePath();
        creditSimulationSubmitPath = config.endpointsCreditsimulationSubmit();
        thanksPagePath = config.thanksPagePath();
        errorPagePath = config.errorPagePath();
        acceptedPagePath = config.acceptedPagePath();
        reassignRequestedPagePath = config.reassignRequestedPagePath();
        reassignedPagePath = config.reassignedPagePath();
        closedPagePath = config.closedPagePath();
        newsletterSubscribePath = config.newsletterSubscribePath();
        newsletterSubscribeTopicKey = config.newsletterSubscribeTopicKey();
        newsletterAcceptPath = config.newsletterAcceptPath();
        makeAppointmentPath = config.makeAppointmentPath();
        checkLeadStatusPath = config.checkLeadStatusPath();
        verifyEmailPath = config.verifyEmailPath();
        hiddenFields = config.hiddenFields();
        submitLeadFeedbackPath=config.submitLeadFeedbackPath();
        hiddenProperties = config.hiddenProperties();
        questionsAnswersFlowIds = config.questionsAnswersFlowIds();
        LOG.info("Updated/Modified configuration, forms middlelayer");

        LOG.debug("submitPath: {}", submitPath);
        LOG.debug("submitLeadPath: {}", submitLeadPath);
        LOG.debug("submitSalePath: {}", submitSalePath);
        LOG.debug("submitSurveyPath: {}", submitSurveyPath);
        LOG.debug("unsubscribePath: {}", unsubscribePath);
        LOG.debug("cpdbPath: {}", cpdbPath);
        LOG.debug("csCalculationPath: {}", csCalculationPath);
        LOG.debug("commercialMailPath: {}", commercialMailPath);
        LOG.debug("generalPrefillPath: {}", generalPrefillPath);
        LOG.debug("externalPrefillPath: {}", externalPrefillPath);
        LOG.debug("leadsForConsultantPrefillPath: {}", leadsForConsultantPrefillPath);
        LOG.debug("autoSavePath: {}", autoSavePath);
        LOG.debug("acceptPath: {}", acceptPath);
        LOG.debug("feedbackAcceptPath: {}", feedbackAcceptPath);
        LOG.debug("feedbackAcceptPathPost: {}", feedbackAcceptPathPost);
        LOG.debug("reassignPath: {}", reassignPath);
        LOG.debug("reassignRequestPath: {}", reassignRequestPath);
        LOG.debug("closePath: {}", closePath);
        LOG.debug("creditSimulationSubmitPath: {}", creditSimulationSubmitPath);
        LOG.debug("thanksPagePath: {}", thanksPagePath);
        LOG.debug("errorPagePath: {}", errorPagePath);
        LOG.debug("acceptedPagePath: {}", acceptedPagePath);
        LOG.debug("reassignRequestedPagePath: {}", reassignRequestedPagePath);
        LOG.debug("reassignedPagePath: {}", reassignedPagePath);
        LOG.debug("closedPagePath: {}", closedPagePath);
        LOG.debug("newsletterSubscribePath: {}", newsletterSubscribePath);
        LOG.debug("newsletterSubscribeTopicKey: {}", newsletterSubscribeTopicKey);
        LOG.debug("newsletterAcceptPath: {}", newsletterAcceptPath);
        LOG.debug("makeAppointmentPath: {}", makeAppointmentPath);
        LOG.debug("checkLeadStatusPath: {}", checkLeadStatusPath);
        LOG.debug("verifyEmailPath: {}", verifyEmailPath);
        LOG.debug("hiddenFields: {}", (Object) hiddenFields);
        LOG.debug("hiddenProperties: {}", (Object) hiddenProperties);
        LOG.debug("leadsForConsultantPath: {}", leadsForConsultantPath);
        LOG.debug("submitLeadFeedbackPath: {}", submitLeadFeedbackPath);
        LOG.debug("questionsAnswersFlowIds: {}", (Object) questionsAnswersFlowIds);
    }

    @Reference
    private SlingSettingsService slingSettingsService;

    public RunModes getRunModes() {
        if (slingSettingsService != null) {
            return new RunModes(this.slingSettingsService);
        }
        return null;
    }

    public String getSubmitPath() {
        return submitPath;
    }

    public String getSubmitLeadPath() {
        return submitLeadPath;
    }

    public String getSubmitBusinessPath() {
        return submitBusinessPath;
    }
    public String getSubmitEventPath() {
        return submitEventPath;
    }

    public String getSubmitInscriptionPath() {
        return submitInscriptionPath;
    }

    public String getSubmitSalePath() {
        return submitSalePath;
    }
    public String getSubmitSurveyPath() {
        return submitSurveyPath;
    }

    public String getUnsubscribePath() {
        return unsubscribePath;
    }

    public String getCpdbPath() {
        return cpdbPath;
    }

    public String getCsCalculationPath() {
        return csCalculationPath;
    }

    public String getCommercialMailPath() {
        return commercialMailPath;
    }

    public String getGeneralPrefillPath() {
        return generalPrefillPath;
    }

    public String getExternalPrefillPath() {
        return externalPrefillPath;
    }
    public String getLeadsForConsultantPrefillPath() {
        return leadsForConsultantPrefillPath;
    }

    public String getCheckAlreadySubmittedPath() {
        return checkAlreadySubmittedPath;
    }

    public String getCheckAlreadySubmittedInscriptionPath() {
        return checkAlreadySubmittedInscriptionPath;
    }

    public String getAutoSavePath() {
        return autoSavePath;
    }

    public String getAcceptPath() {
        return acceptPath;
    }
    public String getLeadsForConsultantPath() {
        return leadsForConsultantPath;
    }
    public String getFeedbackAcceptPath() {
        return feedbackAcceptPath;
    }
    public String getFeedbackAcceptPathPost() {
        return feedbackAcceptPathPost;
    }

    public String getReassignPath() {
        return reassignPath;
    }

    public String getReassignRequestPath() {
        return reassignRequestPath;
    }

    public String getClosePath() {
        return closePath;
    }

    public String getThanksPagePath() {
        return thanksPagePath;
    }

    public String getErrorPagePath() {
        return errorPagePath;
    }

    public String getAcceptedPagePath() {
        return acceptedPagePath;
    }

    public String getReassignRequestedPagePath() {
        return reassignRequestedPagePath;
    }

    public String getReassignedPagePath() {
        return reassignedPagePath;
    }

    public String getClosedPagePath() {
        return closedPagePath;
    }

    public String getNewsletterSubscribePath() {
        return newsletterSubscribePath;
    }

    public String getNewsletterSubscribeTopicKey() {
        return newsletterSubscribeTopicKey;
    }

    public String getNewsletterAcceptPath() {
        return newsletterAcceptPath;
    }

    public String getMakeAppointmentPath() {
        return makeAppointmentPath;
    }

    public String getCheckLeadStatusPath() {
        return checkLeadStatusPath;
    }

    public String getCreditSimulationSubmitPath() {
        return creditSimulationSubmitPath;
    }

    public String getVerifyEmailPath() {
        return verifyEmailPath;
    }
public String getSubmitLeadFeedbackPath(){return submitLeadFeedbackPath;}

    public String[] getQuestionsAnswersFlowIds() {
        return questionsAnswersFlowIds;
    }

    public Map<String, String> getHiddenFieldsMap() {
        return Arrays.stream(this.hiddenFields).map(hiddenKeyVal -> hiddenKeyVal.split(":")).collect(Collectors.toMap(hiddenField -> hiddenField[0], hiddenField -> hiddenField[1]));
    }

    public Map<String, String> getHiddenPropertiesMap() {
        return Arrays.stream(this.hiddenProperties).map(hiddenKeyVal -> hiddenKeyVal.split(":")).collect(Collectors.toMap(hiddenProperty -> hiddenProperty[0], hiddenProperty -> hiddenProperty[1]));
    }
}
