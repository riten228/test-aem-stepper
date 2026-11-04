package com.riten.test.core.servlets;

import org.apache.commons.lang.StringUtils;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(
        name = "Insurance - Forms Middlelayer Configuration",
        description = "This service is used for configuring forms middlelayer settings"
)
public @interface FormsMiddlelayerConfigurationServiceConfig {

    @AttributeDefinition(
            name = "Submit endpoint",
            description = "Enter the path for submit endpoint",
            type = AttributeType.STRING
    )
    String submitPath() default StringUtils.EMPTY;

    @AttributeDefinition(
            name = "Submit lead endpoint",
            description = "Enter the path for submit lead endpoint",
            type = AttributeType.STRING
    )
    String submitLeadPath() default StringUtils.EMPTY;
    @AttributeDefinition(
            name = "Submit Business endpoint",
            description = "Enter the path for submit business endpoint",
            type = AttributeType.STRING
    )
    String submitBusinessPath() default StringUtils.EMPTY;

    @AttributeDefinition(
            name = "Submit Event endpoint",
            description = "Enter the path for submit event endpoint",
            type = AttributeType.STRING
    )
    String submitEventPath() default StringUtils.EMPTY;

    @AttributeDefinition(
            name = "Submit Sale endpoint",
            description = "Enter the path for submit lead endpoint",
            type = AttributeType.STRING
    )
    String submitSalePath() default StringUtils.EMPTY;
    @AttributeDefinition(
            name = "Submit survey endpoint",
            description = "Enter the path for submit survey endpoint",
            type = AttributeType.STRING
    )
    String submitSurveyPath() default StringUtils.EMPTY;

    @AttributeDefinition(
            name = "Unsubscribe endpoint",
            description = "Enter the path for unsubscribe endpoint",
            type = AttributeType.STRING
    )
    String unsubscribePath() default StringUtils.EMPTY;

    @AttributeDefinition(
            name = "CPDB search endpoint",
            description = "Enter the path for CPDB search endpoint",
            type = AttributeType.STRING
    )
    String cpdbPath() default StringUtils.EMPTY;

    @AttributeDefinition(
            name = "CreditSimulator calculation endpoint",
            description = "Enter the path to execute a credit simulation",
            type = AttributeType.STRING
    )
    String csCalculationPath() default StringUtils.EMPTY;

    @AttributeDefinition(
            name = "Commercial mail prefill endpoint",
            description = "Enter the path for commercial mail prefill endpoint",
            type = AttributeType.STRING
    )
    String commercialMailPath() default StringUtils.EMPTY;

    @AttributeDefinition(
            name = "General prefill endpoint",
            description = "Enter the path for general prefill endpoint for dropout mail and accept summary",
            type = AttributeType.STRING
    )
    String generalPrefillPath() default StringUtils.EMPTY;

    @AttributeDefinition(
            name = "External prefill endpoint",
            description = "Enter the path for external prefill endpoint",
            type = AttributeType.STRING
    )
    String externalPrefillPath() default StringUtils.EMPTY;
    @AttributeDefinition(
            name = "Leads for consultant prefill endpoint",
            description = "Enter the path for Leads for consultant prefill endpoint",
            type = AttributeType.STRING
    )
    String leadsForConsultantPrefillPath() default StringUtils.EMPTY;

    @AttributeDefinition(
            name = "Autosave endpoint",
            description = "Enter the path for autosave endpoint",
            type = AttributeType.STRING
    )
    String autoSavePath() default StringUtils.EMPTY;

    @AttributeDefinition(
            name = "Accept endpoint",
            description = "Enter the path for accept endpoint",
            type = AttributeType.STRING
    )
    String acceptPath() default StringUtils.EMPTY;
    @AttributeDefinition(
            name = "Leads for consultant endpoint",
            description = "Enter the path for the leads for consultant endpoint",
            type = AttributeType.STRING
    )
    String leadsForConsultantPath() default StringUtils.EMPTY;
    @AttributeDefinition(
            name = "Accept feedback endpoint",
            description = "Enter the path for accept feedback endpoint",
            type = AttributeType.STRING
    )
    String feedbackAcceptPath() default StringUtils.EMPTY;
    @AttributeDefinition(
            name = "Feedback accept endpoint post",
            description = "Enter the path for Feedback accept post endpoint",
            type = AttributeType.STRING
    )
    String feedbackAcceptPathPost() default StringUtils.EMPTY;
    @AttributeDefinition(
            name = "reassign paht endpoint",
            description = "Enter the path for reassign",
            type = AttributeType.STRING
    )
    String reassignPath() default StringUtils.EMPTY;

    @AttributeDefinition(
            name = "Reassign request endpoint",
            description = "Enter the path for reassign request endpoint",
            type = AttributeType.STRING
    )
    String reassignRequestPath() default StringUtils.EMPTY;

    @AttributeDefinition(
            name = "Close endpoint",
            description = "Enter the path for close endpoint",
            type = AttributeType.STRING
    )
    String closePath() default StringUtils.EMPTY;

    @AttributeDefinition(
            name = "Credit Simulation Submit endpoint",
            description = "Enter the path for credit simulation submit endpoint",
            type = AttributeType.STRING
    )
    String endpointsCreditsimulationSubmit() default StringUtils.EMPTY;

    @AttributeDefinition(
            name = "Thank you page",
            description = "Enter the path for thankyou page with placeholders 0 (language) and 1 (type)",
            type = AttributeType.STRING
    )
    String thanksPagePath() default StringUtils.EMPTY;

    @AttributeDefinition(
            name = "Error page",
            description = "Enter the path for error page with placeholder 0 (language)",
            type = AttributeType.STRING
    )
    String errorPagePath() default StringUtils.EMPTY;

    @AttributeDefinition(
            name = "Accepted page",
            description = "Enter the path for the already accepted page with placeholder 0 (language)",
            type = AttributeType.STRING
    )
    String acceptedPagePath() default StringUtils.EMPTY;
    @AttributeDefinition(
            name = "Feedback accepted page",
            description = "Enter the path for the already feedback accepted page with placeholder 0 (language)",
            type = AttributeType.STRING
    )
    String reassignRequestedPagePath() default StringUtils.EMPTY;

    @AttributeDefinition(
            name = "Reassigned page",
            description = "Enter the path for the already reassigned page with placeholder 0 (language)",
            type = AttributeType.STRING
    )
    String reassignedPagePath() default StringUtils.EMPTY;

    @AttributeDefinition(
            name = "Closed page",
            description = "Enter the path for the already closed page with placeholder 0 (language)",
            type = AttributeType.STRING
    )
    String closedPagePath() default StringUtils.EMPTY;

    @AttributeDefinition(
            name = "Newsletter subscription endpoint",
            description = "Enter the path for newsletter subscription endpoint",
            type = AttributeType.STRING
    )
    String newsletterSubscribePath() default StringUtils.EMPTY;

    @AttributeDefinition(
            name = "Newsletter subscription topic key",
            description = "Enter the topic key for newsletter subscription",
            type = AttributeType.STRING
    )
    String newsletterSubscribeTopicKey() default StringUtils.EMPTY;

    @AttributeDefinition(
            name = "Newsletter accept endpoint",
            description = "Enter the path for newsletter accept endpoint",
            type = AttributeType.STRING
    )
    String newsletterAcceptPath() default StringUtils.EMPTY;

    @AttributeDefinition(
            name = "Make appointment form",
            description = "Enter the path for form reference for make appointment pages",
            type = AttributeType.STRING
    )
    String makeAppointmentPath() default StringUtils.EMPTY;

    @AttributeDefinition(
            name = "Check lead status endpoint",
            description = "Enter the path for check lead status endpoint",
            type = AttributeType.STRING
    )
    String checkLeadStatusPath() default StringUtils.EMPTY;

    @AttributeDefinition(
            name = "Verify Email endpoint",
            description = "Enter the path for verify email endpoint",
            type = AttributeType.STRING
    )
    String verifyEmailPath() default StringUtils.EMPTY;

    @AttributeDefinition(
            name = "Hidden fields",
            description = "Enter the fields that should be passed in the following format 'aemKey:campaignKey'",
            type = AttributeType.STRING
    )
    String[] hiddenFields() default {StringUtils.EMPTY};

    @AttributeDefinition(
            name = "Hidden properties",
            description = "Enter the fields that should be passed in the following format 'aemKey:campaignKey'",
            type = AttributeType.STRING
    )
    String[] hiddenProperties() default {StringUtils.EMPTY};


    @AttributeDefinition(
            name = "Submit lead  feedback endpoint",
            description = "Enter path submit lead feedback",
            type = AttributeType.STRING
    )
    String submitLeadFeedbackPath() default StringUtils.EMPTY;

    @AttributeDefinition(
            name = "AlreadySubmitted endpoint",
            description = "Enter the path for AlreadySubmitted endpoint",
            type = AttributeType.STRING
    )
    String checkAlreadySubmittedPath() default StringUtils.EMPTY;

    @AttributeDefinition(
            name = "AlreadySubmittedInscription endpoint",
            description = "Enter the path for AlreadySubmittedInscription endpoint",
            type = AttributeType.STRING
    )
    String checkAlreadySubmittedInscriptionPath() default StringUtils.EMPTY;

    @AttributeDefinition(
            name = "Submit inscription endpoint",
            description = "Enter the path for submit inscription endpoint",
            type = AttributeType.STRING
    )
    String submitInscriptionPath() default StringUtils.EMPTY;

    @AttributeDefinition(
            name = "Questions and answers flow IDs",
            description = "Flow IDs for which all questions and answers of a submitLead are sent to Campaign as transaction_formQuestionsAnswers",
            type = AttributeType.STRING
    )
    String[] questionsAnswersFlowIds() default {};
}
