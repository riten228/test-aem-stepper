package com.riten.test.core.servlets;
import com.adobe.aemds.guide.utils.GuideConstants;
import com.day.cq.commons.Externalizer;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.riten.test.core.models.Constants;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.xml.ws.http.HTTPException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apache.sling.api.servlets.HttpConstants.METHOD_GET;
import static org.apache.sling.api.servlets.HttpConstants.METHOD_POST;
import static org.apache.sling.api.servlets.ServletResolverConstants.*;
import static org.osgi.framework.Constants.SERVICE_DESCRIPTION;
import static org.osgi.framework.Constants.SERVICE_RANKING;

@Component(
        service = Servlet.class,
        property = {
                SLING_SERVLET_EXTENSIONS + "=html",
                SLING_SERVLET_SELECTORS + "=formaction",
                SLING_SERVLET_PATHS + "=/bin/forms/formaction",
                SLING_SERVLET_METHODS + "=" + METHOD_GET,
                SLING_SERVLET_METHODS + "=" + METHOD_POST,
                SLING_SERVLET_RESOURCE_TYPES + "=sling/servlet/default",
                SERVICE_DESCRIPTION + "=Forms Action Request Servlet",
                SERVICE_RANKING + ":Integer=100"
        }
)
public class FormsSubmitServlet extends SlingAllMethodsServlet {
    public static final String CHECK = "Check";
    public static final String SUBMIT_TYPE = "submitType";
    private static final Logger LOGGER = LoggerFactory.getLogger(FormsSubmitServlet.class);
    private static final String TRANSACTION_KEY = "transactionKey";
    private static final String CONSULTANT_KEY = "consultantKey";
    private static final String PRODUCTID = "productId";
    private static final String ACTION = "action";
    private static final String TOPIC_KEY = "topicKey";
    private static final String CLIENT_KEY = "clientKey";
    private static final String LANG = "lang";
    private static final String TYPE = "type";
    private static final String TYPE_ACCEPT = "accept";
    private static final String TYPE_FEEDBACK_AFTER_ACCEPT = "feedbackAfterAccept";
    private static final String TYPE_FEEDBACK_ACCEPT = "feedbackAccept";
    private static final String TYPE_NEWSLETTER_ACTIVATE = "newsletterActivate";
    private static final String TYPE_REASSIGN = "reassign";
    private static final String TYPE_REASSIGN_REQUEST = "reassignRequest";
    private static final String TYPE_CLOSE = "close";
    private static final String TYPE_SUBMIT = "submit";
    private static final String TYPE_SUBMIT_LEAD = "submitLead";
    private static final String TYPE_SUBMIT_BUSINESS = "submitBusiness";
    private static final String TYPE_SUBMIT_EVENT = "submitEvent";
    private static final String TYPE_SUBMIT_INSCRIPTION = "submitInscription";
    private static final String TYPE_SUBMIT_SALE = "submitSale";
    private static final String TYPE_SUBMIT_LEAD_CHECK = "submitLeadCheck";
    private static final String TYPE_SUBMIT_SURVEY = "submitSurvey";
    private static final String TYPE_UNSUBSCRIBE = "unsubscribe";
    private static final String TYPE_SUBMIT_CS = "submitCreditSimulator";
    private static final String ACCEPT_FROM_REASSIGNED = "reassignAcceptRequest";
    private static final String DYNAMIC_PAGE = "DynamicPage";
    private static final String NOW = "now";
    private static final String FEEDBACK_ACCEPT = "feedback-accept";
    private static final String THANKYOU_FEEDBACK_ACCEPT = "feedback-accept";
    private static final String LATER_ACCEPT = "later-accept";
    private static final String HIDDEN_PARAMTERS = "hiddenParameters";

    private static final Pattern HTML_TAG = Pattern.compile("<[^>]+>");
    private static final Pattern SAFE_VALUE = Pattern.compile("^[A-Za-z0-9_.\\-]{1,128}$");
    private static final Pattern SAFE_LOCALE = Pattern.compile("^[A-Za-z]{2}([_-][A-Za-z]{2})?$");
    private static final String DEFAULT_LOCALE = "nl_be";
    private static final int CONNECTION_TIMEOUT_MS = 10000;
    private static final int SOCKET_TIMEOUT_MS = 30000;
    private static final Set<String> ALLOWED_TYPES = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            TYPE_ACCEPT, TYPE_FEEDBACK_AFTER_ACCEPT, TYPE_FEEDBACK_ACCEPT, TYPE_NEWSLETTER_ACTIVATE, TYPE_REASSIGN,
            TYPE_REASSIGN_REQUEST, TYPE_CLOSE, TYPE_SUBMIT, TYPE_SUBMIT_LEAD, TYPE_SUBMIT_BUSINESS, TYPE_SUBMIT_EVENT,
            TYPE_SUBMIT_INSCRIPTION, TYPE_SUBMIT_SALE, TYPE_SUBMIT_SURVEY, TYPE_UNSUBSCRIBE, TYPE_SUBMIT_CS)));
    private static final List<String> VALIDATED_PARAMS = Collections.unmodifiableList(Arrays.asList(
            TRANSACTION_KEY, CONSULTANT_KEY, PRODUCTID, ACTION, CLIENT_KEY, ACCEPT_FROM_REASSIGNED));


    @Reference
    private FormsMiddlelayerConfigurationService formsMiddlelayerConfigurationService;

    private static JSONObject parseHiddenParameters(String hiddenParameters) {
        if (StringUtils.isBlank(hiddenParameters)) {
            return new JSONObject();
        }
        try {
            return new JSONObject(hiddenParameters);
        } catch (JSONException e) {
            return null;
        }
    }

    private static String getParam(String hiddenParameters, String paramToSearch) {
        JSONObject json = parseHiddenParameters(hiddenParameters);
        if (json != null && json.has(paramToSearch) && !json.isNull(paramToSearch)) {
            String value = json.optString(paramToSearch, null);
            LOGGER.debug("Parameter from hidden {}", sanitizeForLog(value));
            return value;
        }
        return null;
    }

    private static String sanitizeForLog(String value) {
        return value == null ? null : value.replaceAll("[\\r\\n\\t]", "_");
    }

    private static boolean isSafeValue(String value) {
        return value != null && SAFE_VALUE.matcher(value).matches();
    }

    /** Only absolute http(s) endpoints without user info are allowed; endpoints come from configuration only. */
    private static boolean isValidEndpoint(String endPoint) {
        try {
            URI uri = new URI(endPoint);
            String scheme = uri.getScheme();
            return ("https".equalsIgnoreCase(scheme) || "http".equalsIgnoreCase(scheme))
                    && StringUtils.isNotBlank(uri.getHost()) && uri.getUserInfo() == null;
        } catch (URISyntaxException e) {
            return false;
        }
    }

    /** Only existing internal content pages are accepted as redirect targets. */
    private static boolean isSafePagePath(SlingHttpServletRequest request, String path) {
        if (path == null || !path.startsWith("/content/") || path.contains("..") || path.contains("//")
                || path.contains("\\") || path.contains("?") || path.contains("#") || path.chars().anyMatch(Character::isISOControl)) {
            return false;
        }
        PageManager pageManager = request.getResourceResolver().adaptTo(PageManager.class);
        return pageManager != null && pageManager.getPage(path) != null;
    }

    private String resolveResultPage(SlingHttpServletRequest request, String paramName, String lang, String type) {
        String requested = request.getParameter(paramName);
        if (StringUtils.isNotEmpty(requested)) {
            if (isSafePagePath(request, requested)) {
                return requested;
            }
            LOGGER.warn("Ignoring unsafe page path for {}", sanitizeForLog(paramName));
        }
        return MessageFormat.format(formsMiddlelayerConfigurationService.getThanksPagePath(), lang, type);
    }

    @Override
    protected void doGet(@Nonnull SlingHttpServletRequest request, @Nonnull SlingHttpServletResponse response) throws ServletException, IOException {
        process(request, response);
    }

    @Override
    protected void doPost(@Nonnull SlingHttpServletRequest request, @Nonnull SlingHttpServletResponse response) throws ServletException, IOException {
        process(request, response);
    }

    private void process(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {
        String endPoint = null;
        String type = StringUtils.isNotEmpty(request.getParameter(TYPE)) ? request.getParameter(TYPE) : StringUtils.EMPTY;
        String locale = StringUtils.isNotEmpty(request.getParameter(LANG)) ? request.getParameter(LANG) : DEFAULT_LOCALE;
        boolean validLocale = SAFE_LOCALE.matcher(locale).matches();
        if (!validLocale) {
            locale = DEFAULT_LOCALE;
        }
        String lang = locale.length() > 2 ? locale.substring(0, 2) : StringUtils.EMPTY;
        String hiddenParameters = StringUtils.isNotEmpty(request.getParameter(Constants.HIDDEN_PARAMETERS)) ? request.getParameter(Constants.HIDDEN_PARAMETERS) : StringUtils.EMPTY;
        String generalErrorPage = MessageFormat.format(formsMiddlelayerConfigurationService.getErrorPagePath(), lang);
        if (!validLocale) {
            LOGGER.warn("Forms action servlet - invalid lang parameter, redirecting to error page");
            response.sendRedirect(generalErrorPage);
            return;
        }
        if (StringUtils.isNotBlank(type) && type.equalsIgnoreCase(TYPE_SUBMIT_LEAD_CHECK)) {
            type = setSubmitType(hiddenParameters, type);
        }
        if (!ALLOWED_TYPES.contains(type)) {
            LOGGER.warn("Forms action servlet - unexpected type {}, redirecting to error page", sanitizeForLog(type));
            setResponseUrl(request, response, generalErrorPage, StringUtils.EMPTY, generalErrorPage, false);
            return;
        }

        Map<String, String> safeParams = new HashMap<>();
        for (String name : VALIDATED_PARAMS) {
            String value = request.getParameter(name);
            if (value != null) {
                if (!isSafeValue(value)) {
                    LOGGER.warn("Forms action servlet - invalid value for parameter {}, redirecting to error page", name);
                    setResponseUrl(request, response, generalErrorPage, StringUtils.EMPTY, generalErrorPage, false);
                    return;
                }
                safeParams.put(name, value);
            }
        }
        if (StringUtils.isNotBlank(hiddenParameters) && parseHiddenParameters(hiddenParameters) == null) {
            LOGGER.warn("Forms action servlet - hiddenParameters is not valid JSON, redirecting to error page");
            setResponseUrl(request, response, generalErrorPage, StringUtils.EMPTY, generalErrorPage, false);
            return;
        }

        String jcrData = request.getParameter(GuideConstants.JCR_DATA);
        if (StringUtils.isNotBlank(jcrData)) {

            String dataFields = extractFieldValue(jcrData, "data");
            Map<String, String> fields = extractAllFields(dataFields);

            for (Map.Entry<String, String> entry : fields.entrySet()) {
                String fieldName = entry.getKey();
                String fieldValue = entry.getValue();

                if (StringUtils.isNotBlank(fieldValue) && HTML_TAG.matcher(fieldValue).find()) {
                    LOGGER.warn("HTML detected in field {} → blocking submit", sanitizeForLog(fieldName));
                    String errorPage = MessageFormat.format(formsMiddlelayerConfigurationService.getErrorPagePath(), lang);
                    response.sendRedirect(errorPage);
                    return; // STOP submit
                }
            }
        }

        if (StringUtils.isNotBlank(hiddenParameters)) {

            Map<String, String> values = extractJsonValues(hiddenParameters);

            for (Map.Entry<String, String> entry : values.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();

                if (value != null && HTML_TAG.matcher(value).find()) {
                    LOGGER.warn("HTML detected in hiddenParameters {} → blocking submit", sanitizeForLog(key));
                    String errorPage = MessageFormat.format(formsMiddlelayerConfigurationService.getErrorPagePath(), lang);
                    response.sendRedirect(errorPage);
                    return; // STOP submit
                }
            }
        }

        LOGGER.debug("endPoint: {}", endPoint);
        LOGGER.debug("type: {}", type);
        LOGGER.debug("locale: {}", locale);
        LOGGER.debug("lang: {}", lang);
        LOGGER.debug("hiddenParameters: {}", sanitizeForLog(hiddenParameters));
        LOGGER.debug("generalErrorPage: {}", generalErrorPage);
        LOGGER.info("Forms action servlet - incoming {} request", sanitizeForLog(type));

        Map<String, String> formData = new HashMap<>();
        Map<String, String> additionalData = new HashMap<>();
        List<NameValuePair> urlParams = new ArrayList<>();
        boolean addUrlParam = false;
        String alternative = "";
        switch (type) {
            case TYPE_FEEDBACK_ACCEPT:
                if (StringUtils.isEmpty(request.getParameter(Constants.REQUEST_FROM_POST_JSP))) {
                    endPoint = formsMiddlelayerConfigurationService.getLeadsForConsultantPath();
                    if (safeParams.get(CONSULTANT_KEY) != null) {
                        additionalData.put(CONSULTANT_KEY, safeParams.get(CONSULTANT_KEY));
                    }
                    if (safeParams.get(CONSULTANT_KEY) != null) {
                        urlParams.add(new NameValuePair(CONSULTANT_KEY, safeParams.get(CONSULTANT_KEY)));
                    }
                    type = TYPE_FEEDBACK_ACCEPT + CHECK;
                    LOGGER.debug("Type check leads for consultant  {}", type);
                } else {
                    //submit feedback
                    LOGGER.debug("Post submit accept");
                    formData.put(GuideConstants.JCR_DATA, request.getParameter(GuideConstants.JCR_DATA));
                    additionalData.put(Constants.HIDDEN_PARAMETERS, hiddenParameters);
                    endPoint = formsMiddlelayerConfigurationService.getSubmitLeadFeedbackPath();
                    String key = getParam(hiddenParameters, CONSULTANT_KEY);
                    LOGGER.debug("adding parameter for url {}", key);
                    if (isSafeValue(key)) {
                        urlParams.add(new NameValuePair(CONSULTANT_KEY, key));
                        addUrlParam = true;
                    }
                }
                break;
            case TYPE_FEEDBACK_AFTER_ACCEPT:
                //submit feedback
                LOGGER.debug("Post submit accept");
                formData.put(GuideConstants.JCR_DATA, request.getParameter(GuideConstants.JCR_DATA));
                additionalData.put(Constants.HIDDEN_PARAMETERS, hiddenParameters);
                endPoint = formsMiddlelayerConfigurationService.getSubmitLeadFeedbackPath();
                alternative = setAlternativeLink(hiddenParameters, DYNAMIC_PAGE, NOW, THANKYOU_FEEDBACK_ACCEPT, LATER_ACCEPT);
                break;
            case TYPE_ACCEPT:
                endPoint = formsMiddlelayerConfigurationService.getAcceptPath();
                additionalData.put(TRANSACTION_KEY, safeParams.get(TRANSACTION_KEY));
                if (safeParams.get(ACCEPT_FROM_REASSIGNED) != null) {
                    additionalData.put(ACCEPT_FROM_REASSIGNED, safeParams.get(ACCEPT_FROM_REASSIGNED));
                } else {
                    additionalData.put(ACCEPT_FROM_REASSIGNED, "false");
                }

                if (safeParams.get(TRANSACTION_KEY) != null) {
                    urlParams.add(new NameValuePair(TRANSACTION_KEY, safeParams.get(TRANSACTION_KEY)));
                }
                if (safeParams.get(PRODUCTID) != null) {
                    additionalData.put(PRODUCTID, safeParams.get(PRODUCTID));
                }
                if (StringUtils.isNotEmpty(lang)) {
                    additionalData.put(LANG, lang);
                }
                break;
            case TYPE_REASSIGN_REQUEST:
                LOGGER.debug("Reassign request");
                if (StringUtils.isEmpty(request.getParameter(Constants.REQUEST_FROM_POST_JSP))) {
                    endPoint = formsMiddlelayerConfigurationService.getCheckLeadStatusPath();
                    urlParams.add(new NameValuePair(TYPE, type));
                    if (safeParams.get(TRANSACTION_KEY) != null) {
                        additionalData.put(TRANSACTION_KEY, safeParams.get(TRANSACTION_KEY));
                    }
                    if (safeParams.get(TRANSACTION_KEY) != null) {
                        urlParams.add(new NameValuePair(TRANSACTION_KEY, safeParams.get(TRANSACTION_KEY)));
                    }
                    if (safeParams.get(PRODUCTID) != null) {
                        urlParams.add(new NameValuePair(PRODUCTID, safeParams.get(PRODUCTID)));
                    }
                    if (safeParams.get(ACTION) != null) {
                        urlParams.add(new NameValuePair(ACTION, safeParams.get(ACTION)));
                    }
                    if (StringUtils.isNotEmpty(lang)) {
                        urlParams.add(new NameValuePair(LANG, lang));
                    }
                    type = TYPE_REASSIGN_REQUEST + CHECK;
                    LOGGER.debug("Type Reassign request {}", type);

                } else {
                    endPoint = formsMiddlelayerConfigurationService.getReassignRequestPath();
                    formData.put(GuideConstants.JCR_DATA, request.getParameter(GuideConstants.JCR_DATA));
                    additionalData.put(Constants.HIDDEN_PARAMETERS, hiddenParameters);
                }
                break;
            case TYPE_REASSIGN:
                if (StringUtils.isEmpty(request.getParameter(Constants.REQUEST_FROM_POST_JSP))) {
                    endPoint = formsMiddlelayerConfigurationService.getCheckLeadStatusPath();
                    urlParams.add(new NameValuePair(TYPE, type));
                    if (safeParams.get(TRANSACTION_KEY) != null) {
                        urlParams.add(new NameValuePair(TRANSACTION_KEY, safeParams.get(TRANSACTION_KEY)));
                    }
                    if (safeParams.get(PRODUCTID) != null) {
                        urlParams.add(new NameValuePair(PRODUCTID, safeParams.get(PRODUCTID)));
                    }
                    if (safeParams.get(ACTION) != null) {
                        urlParams.add(new NameValuePair(ACTION, safeParams.get(ACTION)));
                    }
                    if (StringUtils.isNotEmpty(lang)) {
                        urlParams.add(new NameValuePair(LANG, lang));
                    }
                    type = TYPE_REASSIGN + CHECK;
                } else {
                    endPoint = formsMiddlelayerConfigurationService.getReassignPath();
                    if (safeParams.get(TRANSACTION_KEY) != null) {
                        additionalData.put(TRANSACTION_KEY, safeParams.get(TRANSACTION_KEY));
                    }
                    formData.put(GuideConstants.JCR_DATA, request.getParameter(GuideConstants.JCR_DATA));
                    additionalData.put(Constants.HIDDEN_PARAMETERS, hiddenParameters);
                }
                break;

            case TYPE_CLOSE:
                endPoint = formsMiddlelayerConfigurationService.getClosePath();
                additionalData.put(TRANSACTION_KEY, safeParams.get(TRANSACTION_KEY));
                additionalData.put(LANG, locale);
                break;
            case TYPE_NEWSLETTER_ACTIVATE:
                endPoint = formsMiddlelayerConfigurationService.getNewsletterAcceptPath();
                additionalData.put(TOPIC_KEY, formsMiddlelayerConfigurationService.getNewsletterSubscribeTopicKey());
                additionalData.put(CLIENT_KEY, safeParams.get(CLIENT_KEY));
                break;
            case TYPE_SUBMIT:
                endPoint = formsMiddlelayerConfigurationService.getSubmitPath();
                formData.put(GuideConstants.JCR_DATA, request.getParameter(GuideConstants.JCR_DATA));
                additionalData.put(Constants.HIDDEN_PARAMETERS, hiddenParameters);
                break;
            case TYPE_SUBMIT_LEAD:
                endPoint = formsMiddlelayerConfigurationService.getSubmitLeadPath();
                formData.put(GuideConstants.JCR_DATA, request.getParameter(GuideConstants.JCR_DATA));
                additionalData.put(Constants.HIDDEN_PARAMETERS, hiddenParameters);
                break;
            case TYPE_SUBMIT_BUSINESS:
                endPoint = formsMiddlelayerConfigurationService.getSubmitBusinessPath();
                formData.put(GuideConstants.JCR_DATA, request.getParameter(GuideConstants.JCR_DATA));
                additionalData.put(Constants.HIDDEN_PARAMETERS, hiddenParameters);
                break;
            case TYPE_SUBMIT_EVENT:
                endPoint = formsMiddlelayerConfigurationService.getSubmitEventPath();
                formData.put(GuideConstants.JCR_DATA, request.getParameter(GuideConstants.JCR_DATA));
                additionalData.put(Constants.HIDDEN_PARAMETERS, hiddenParameters);
                break;
            case TYPE_SUBMIT_INSCRIPTION:
                endPoint = formsMiddlelayerConfigurationService.getSubmitInscriptionPath();
                formData.put(GuideConstants.JCR_DATA, request.getParameter(GuideConstants.JCR_DATA));
                additionalData.put(Constants.HIDDEN_PARAMETERS, hiddenParameters);
                break;
            case TYPE_SUBMIT_SALE:
                endPoint = formsMiddlelayerConfigurationService.getSubmitSalePath();
                formData.put(GuideConstants.JCR_DATA, request.getParameter(GuideConstants.JCR_DATA));
                if (StringUtils.isNotEmpty(lang)) {
                    additionalData.put(LANG, lang);
                }
                additionalData.put(Constants.HIDDEN_PARAMETERS, hiddenParameters);
                break;
            case TYPE_SUBMIT_SURVEY:
                endPoint = formsMiddlelayerConfigurationService.getSubmitSurveyPath();
                formData.put(GuideConstants.JCR_DATA, request.getParameter(GuideConstants.JCR_DATA));
                additionalData.put(Constants.HIDDEN_PARAMETERS, hiddenParameters);
                break;
            case TYPE_UNSUBSCRIBE:
                endPoint = formsMiddlelayerConfigurationService.getUnsubscribePath();
                formData.put(GuideConstants.JCR_DATA, request.getParameter(GuideConstants.JCR_DATA));
                additionalData.put(Constants.HIDDEN_PARAMETERS, hiddenParameters);
                break;
            case TYPE_SUBMIT_CS:
                endPoint = formsMiddlelayerConfigurationService.getCreditSimulationSubmitPath();
                formData.put(GuideConstants.JCR_DATA, request.getParameter(GuideConstants.JCR_DATA));
                additionalData.put(Constants.HIDDEN_PARAMETERS, hiddenParameters);
                break;
            default:
                break;
        }

        LOGGER.debug("additionalData data");
        for (String key : additionalData.keySet()) {
            LOGGER.debug("{}: {}", key, sanitizeForLog(additionalData.get(key)));
        }

        LOGGER.debug("formData data");
        for (String key : formData.keySet()) {
            LOGGER.debug("{}: {}", key, sanitizeForLog(formData.get(key)));
        }

        Map<String, String> requestBody = Stream.of(additionalData.entrySet(), formData.entrySet())
                .flatMap(Set::stream)
                .filter(entry -> entry.getValue() != null)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        String requestBodyAsJSON = (new JSONObject(requestBody)).toString();
        LOGGER.debug("requestBodyAsJSON: {}", requestBodyAsJSON);

        if (StringUtils.isNotEmpty(endPoint) && !isValidEndpoint(endPoint)) {
            LOGGER.error("Forms action servlet - configured endpoint is not a valid http(s) URL, forwarding to generalErrorPage");
            endPoint = null;
        }

        if (StringUtils.isNotEmpty(endPoint)) {
            int returnCode;
            String resultPage;
            JSONObject json = null;
            String conflict = StringUtils.EMPTY;
            String responseTransactionKey = StringUtils.EMPTY;

            // Check lead status
            if (type.contains(CHECK)) {
                type = getTypeWithoutCheck(type, TYPE_ACCEPT);
                type = getTypeWithoutCheck(type, TYPE_FEEDBACK_ACCEPT);
                HttpClient httpClient = newHttpClient();
                HttpMethod getMethod = new GetMethod(endPoint);

                getMethod.setQueryString(urlParams.toArray(new NameValuePair[urlParams.size()]));
                try {
                    LOGGER.info("Forms action servlet - forwarding request to: {}", endPoint);
                    returnCode = httpClient.executeMethod(getMethod);
                } catch (IOException | HTTPException e) {
                    LOGGER.error("Forms action servlet - error forwarding call", e);
                    returnCode = HttpStatus.SC_INTERNAL_SERVER_ERROR;
                }

                LOGGER.debug("returnCode: {}", returnCode);

                try {
                    String conflictResponse = getMethod.getResponseBodyAsString();
                    if (StringUtils.isNotEmpty(conflictResponse)) {
                        json = new JSONObject(conflictResponse);
                        conflict = json.get(Constants.FORMS_CONFLICT).toString();
                        if (StringUtils.equals(conflict, "null")) {
                            conflict = StringUtils.EMPTY;
                        }
                    }
                } catch (IOException | JSONException e) {
                    LOGGER.error("Forms action servlet - error getting response json body");
                    returnCode = HttpStatus.SC_INTERNAL_SERVER_ERROR;
                } finally {
                    getMethod.releaseConnection();
                }
            } else {
                // Submit Data
                HttpClient httpClient = newHttpClient();
                PostMethod postMethod = new PostMethod(endPoint);
                postMethod.setRequestEntity(new StringRequestEntity(requestBodyAsJSON, "application/json", "UTF-8"));

                try {
                    LOGGER.info("Forms action servlet - forwarding request to: {}", endPoint);
                    LOGGER.debug("Method path: {} & queryString {} & toString {}", postMethod.getPath(), postMethod.getQueryString(), postMethod.toString());
                    returnCode = httpClient.executeMethod(postMethod);
                } catch (IOException | HTTPException e) {
                    LOGGER.error("Forms action servlet - error forwarding call", e);
                    returnCode = HttpStatus.SC_INTERNAL_SERVER_ERROR;
                }
                LOGGER.debug("returnCode: {}", returnCode);

                try {
                    String conflictResponse = postMethod.getResponseBodyAsString();
                    LOGGER.debug("conflictResponse: {}", conflictResponse);
                    if (StringUtils.isNotEmpty(conflictResponse)) {
                        json = new JSONObject(conflictResponse);
                        LOGGER.debug("conflictResponse as json: {}", json);
                        conflict = json.get(Constants.FORMS_CONFLICT).toString();
                        LOGGER.debug("conflict from conflictResponse as json: {}", conflict);
                        if (StringUtils.equals(conflict, "null")) {
                            conflict = StringUtils.EMPTY;
                            LOGGER.debug("conflict is null, so will me an empty String: [{}]", conflict);
                        }
                        if (StringUtils.equalsIgnoreCase(type, TYPE_SUBMIT_LEAD) && json.has(TRANSACTION_KEY)) {
                            responseTransactionKey = json.get(TRANSACTION_KEY).toString();
                            if (StringUtils.equals(responseTransactionKey, "null")) {
                                responseTransactionKey = StringUtils.EMPTY;
                            }
                            LOGGER.debug("transactionKey from conflictResponse as json: {}", responseTransactionKey);
                        }
                    }
                } catch (IOException | JSONException e) {
                    LOGGER.error("Forms action servlet - error getting response json body: {}", e.getMessage(), e);
                    returnCode = HttpStatus.SC_INTERNAL_SERVER_ERROR;
                } finally {
                    postMethod.releaseConnection();
                }
            }


            if (returnCode == HttpStatus.SC_OK) {
                if (StringUtils.isNotEmpty(responseTransactionKey) && isSafeValue(responseTransactionKey)) {
                    urlParams.add(new NameValuePair(TRANSACTION_KEY, responseTransactionKey));
                    addUrlParam = true;
                }
                String thankYouPage = getParam(hiddenParameters, Constants.THANKYOU_PAGE_URL);
                if (thankYouPage != null) {
                    switch (thankYouPage) {
                        case Constants.THANKYOU_PAGE_URL_HAPPY:
                            resultPage = resolveResultPage(request, Constants.THANKYOU_PAGE_URL_HAPPY, lang, type);
                            break;
                        case Constants.THANKYOU_PAGE_URL_HAPPY_TALK_AGENT:
                            resultPage = resolveResultPage(request, Constants.THANKYOU_PAGE_URL_HAPPY_TALK_AGENT, lang, type);
                            break;
                        default:
                            resultPage = resolveResultPage(request, Constants.THANKYOU_PAGE_URL, lang, type);
                            break;
                    }
                } else {
                    resultPage = resolveResultPage(request, Constants.THANKYOU_PAGE_URL, lang, type);
                }
                LOGGER.debug("Thank you page url: resultPage {}", resultPage);

                if (StringUtils.isNotEmpty(conflict)) {
                    LOGGER.info("Forms action servlet - Conflict found: {}", conflict);
                    switch (conflict) {
                        case Constants.ALREADY_ACCEPTED:
                            resultPage = MessageFormat.format(formsMiddlelayerConfigurationService.getAcceptedPagePath(), lang);
                            break;
                        case Constants.ALREADY_REASSIGN_REQUESTED:
                            resultPage = MessageFormat.format(formsMiddlelayerConfigurationService.getReassignRequestedPagePath(), lang);
                            break;
                        case Constants.ALREADY_REASSIGNED:
                        case Constants.ALREADY_REASSIGNED_OPEN_FOR_ACCEPTANCE:
                            resultPage = MessageFormat.format(formsMiddlelayerConfigurationService.getReassignedPagePath(), lang);
                            break;
                        case Constants.ALREADY_CLOSED:
                            resultPage = MessageFormat.format(formsMiddlelayerConfigurationService.getClosedPagePath(), lang);
                            break;
                        case Constants.NO_TICKET:
                            resultPage = resolveResultPage(request, Constants.THANKYOU_PAGE_URL_HAPPY, lang, type);
                            break;
                        default:
                            break;
                    }
                } else if (StringUtils.isNotEmpty(alternative)) {
                    LOGGER.debug("Changing resultpage from {} to {}", resultPage, MessageFormat.format(formsMiddlelayerConfigurationService.getThanksPagePath(), lang, alternative));
                    resultPage = MessageFormat.format(formsMiddlelayerConfigurationService.getThanksPagePath(), lang, alternative);
                }
            } else {
                resultPage = MessageFormat.format(formsMiddlelayerConfigurationService.getErrorPagePath(), lang);
                LOGGER.debug("resultPage formatted: {}", resultPage);
            }
            LOGGER.info("Forms action servlet - Forwarding to resultpage: {}", resultPage);
            setResponseUrl(request, response, resultPage, formatUrlParams(urlParams), generalErrorPage, addUrlParam);
        } else {
            LOGGER.error("Forms action servlet - Forwarding to generalErrorPage: {} because endPoint is empty: [{}]", generalErrorPage, endPoint);
            setResponseUrl(request, response, generalErrorPage, StringUtils.EMPTY, generalErrorPage, addUrlParam);
        }
    }

    private static HttpClient newHttpClient() {
        HttpClient httpClient = new HttpClient();
        httpClient.getHttpConnectionManager().getParams().setConnectionTimeout(CONNECTION_TIMEOUT_MS);
        httpClient.getHttpConnectionManager().getParams().setSoTimeout(SOCKET_TIMEOUT_MS);
        return httpClient;
    }

    private String getTypeWithoutCheck(String type, String typeFeedbackAcceptRecap) {
        return type.equalsIgnoreCase(typeFeedbackAcceptRecap + CHECK) ? typeFeedbackAcceptRecap : type;
    }

    private String setAlternativeLink(String hiddenParameters, String paramToSearch, String match, String url1, String url2) {
        String value = getParam(hiddenParameters, paramToSearch);
        if (value == null) {
            return null;
        }
        return value.equalsIgnoreCase(match) ? url1 : url2;
    }

    private void setResponseUrl(SlingHttpServletRequest request, SlingHttpServletResponse response, String resultPage, String urlParams, String generalErrorPage, boolean addUrlParam) {
        LOGGER.debug("method:setResponseUrl, variable:resultPage {}", resultPage);
        LOGGER.debug("method:setResponseUrl, variable:urlParams  {}", urlParams);
        LOGGER.debug("method:setResponseUrl, variable:generalErrorPage {}", generalErrorPage);

        PageManager pageManager = request.getResourceResolver().adaptTo(PageManager.class);
        Externalizer externalizer = request.getResourceResolver().adaptTo(Externalizer.class);

        if (pageManager != null && externalizer != null) {
            Page page = pageManager.getPage(resultPage) != null ? pageManager.getPage(resultPage) : pageManager.getPage(generalErrorPage);
            if (page != null) {
                LOGGER.debug("Page found: {}", page.getPath());
                String link = LinkUtil.getLink(request.getResourceResolver(), resultPage, resultPage, true, null);
                LOGGER.debug("setResponseUrl link: {}", link);
                if (link != null) {
                    String url = externalizer.externalLink(request.getResourceResolver(), LinkUtil.getExternalizerNameOfPage(page.getContentResource()), link);
                    LOGGER.debug("setResponseURl url: {}", url);

                    if (StringUtils.isEmpty(request.getParameter(Constants.REQUEST_FROM_POST_JSP))) {
                        try {
                            response.sendRedirect(url + urlParams);
                        } catch (IOException ex) {
                            LOGGER.error("Error while setting the response URL. Error code: {}", ex.getMessage(), ex);
                            response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
                        }
                    } else {
                        if (addUrlParam) {
                            url = url + urlParams;
                            LOGGER.debug("Url is now {}", url);

                        }
                        response.setStatus(HttpStatus.SC_OK);
                        response.setHeader(Constants.RESULT_PAGE_URL, url);
                    }
                } else if (!StringUtils.equals(generalErrorPage, resultPage)) {
                    LOGGER.debug("generalErrorPage: {} - resultPage: {}", generalErrorPage, resultPage);
                    setResponseUrl(request, response, generalErrorPage, StringUtils.EMPTY, generalErrorPage, addUrlParam);
                } else {
                    response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
                }
            }
        } else {
            if (pageManager == null) {
                LOGGER.error("pageManager is null");
            }

            if (externalizer == null) {
                LOGGER.error("externalizer is null");
            }
            response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private static String encode(String value) {
        try {
            return URLEncoder.encode(StringUtils.defaultString(value), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }

    private String formatUrlParams(List<NameValuePair> urlParams) {
        if (!urlParams.isEmpty()) {
            String url = urlParams.stream().map(urlParam -> "&" + encode(urlParam.getName()) + "=" + encode(urlParam.getValue())).collect(Collectors.joining()).substring(1);
            LOGGER.debug("urlParams formatted: {}", url);
            if (StringUtils.isNotEmpty(url)) {
                return "?" + url;
            }
            return url;
        }
        LOGGER.info("urlParams is empty");
        return StringUtils.EMPTY;
    }

    private String setSubmitType(String hiddenParameters, String submitType) {
        String alternative = getParam(hiddenParameters, SUBMIT_TYPE);
        return alternative != null ? alternative : submitType;
    }

    private static String extractFieldValue(String xml, String fieldName) {
        String pattern = "<" + fieldName + ">(.*?)</" + fieldName + ">";
        Pattern p = Pattern.compile(pattern, Pattern.DOTALL);
        Matcher m = p.matcher(xml);
        if (m.find()) {
            return m.group(1); // inner text
        }
        return null;
    }

    private static Map<String, String> extractAllFields(String xml) {
      Map<String, String> fields = new HashMap<>();
      if (xml == null || xml.isEmpty()) {
        LOGGER.debug("extractAllFields: xml parameter is empty>>");
        return fields;  // Return empty map
      }

      Pattern p = Pattern.compile("<([a-zA-Z0-9_]+)>(.*?)</\\1>", Pattern.DOTALL);
      Matcher m = p.matcher(xml);

      while (m.find()) {
        String fieldName = m.group(1);
        String fieldValue = m.group(2);
        fields.put(fieldName, fieldValue);
      }

      return fields;
    }

    private static Map<String, String> extractJsonValues(String json) {
        Map<String, String> map = new HashMap<>();
        JSONObject parsed = parseHiddenParameters(json);
        if (parsed != null) {
            for (String key : parsed.keySet()) {
                map.put(key, parsed.isNull(key) ? null : parsed.get(key).toString());
            }
        }
        return map;
    }
}
