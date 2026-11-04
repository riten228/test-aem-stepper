package com.riten.test.core.servlets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.json.JSONObject;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_METHODS;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_PATHS;

@Component(
        immediate = true,
        service = Servlet.class,
        property = {
                SLING_SERVLET_PATHS+"=/bin/newsletter",
                SLING_SERVLET_METHODS+"=POST",
                Constants.SERVICE_DESCRIPTION + "=Insurance - This service is used to subscribe to the newsletter",
                Constants.SERVICE_RANKING + ":Integer=100"
        }
)
public class NewsletterSubscriptionService extends SlingAllMethodsServlet {

    private static final Logger LOGGER = LoggerFactory.getLogger(NewsletterSubscriptionService.class);

    private static final String POST_EMAIL_PARAM = "email";
    private static final String POST_LANGUAGE_PARAM = "lang";
    private static final String POST_TOPIC_KEY_PARAM = "topicKey";
    private static final String CAPTCHA_RESPONSE_PARAM = "recaptchaResponse";
    private static final String RECAPTCHA_TOKEN = "recaptchaToken";

    @Reference
    private CaptchaValidationService captchaValidationService;

    @Reference
    private FormsMiddlelayerConfigurationService formsMiddlelayerConfigurationService;

    @Override
    protected void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException, IOException {

        String email = request.getParameter(POST_EMAIL_PARAM);
        String language = request.getParameter(POST_LANGUAGE_PARAM);
        String locale = StringUtils.startsWith(language, "fr") ? "fr_be" : "nl_be";
        String recaptchaToken = request.getParameter(RECAPTCHA_TOKEN);

        LOGGER.debug("RecaptchaToken : {}", recaptchaToken);

        int responseCode = 0;
        if (captchaValidationService != null && captchaValidationService.verifyRecaptcha(recaptchaToken)) {
            responseCode = doFormsMiddlelayerCall(email, locale);
        }
        LOGGER.debug("responseCode: {}", responseCode);

        NewsletterSubscriptionResponse newsletterSubscriptionResponse = new NewsletterSubscriptionResponse();
        newsletterSubscriptionResponse.setStatus(responseCode);

        Gson gson = new GsonBuilder().create();
        String json = gson.toJson(newsletterSubscriptionResponse);
        LOGGER.debug("json: {}", json);
        response.getWriter().print(json);
    }

    private int doFormsMiddlelayerCall(final String email, final String locale) throws UnsupportedEncodingException {
        final String endPoint = formsMiddlelayerConfigurationService.getNewsletterSubscribePath();
        final String topicKey = formsMiddlelayerConfigurationService.getNewsletterSubscribeTopicKey();

        LOGGER.debug("endPoint: {}", endPoint);
        LOGGER.debug("topicKey: {}", topicKey);

        Map<String, String> requestData = new HashMap<>();
        requestData.put(POST_EMAIL_PARAM, email);
        requestData.put(POST_LANGUAGE_PARAM, locale);
        requestData.put(POST_TOPIC_KEY_PARAM, topicKey);
        String requestBodyAsJSON = (new JSONObject(requestData)).toString();
        LOGGER.debug("requestBodyAsJSON: {}", requestBodyAsJSON);

        if (StringUtils.isNotEmpty(endPoint) && StringUtils.isNotEmpty(topicKey)) {
            try {
                HttpClient httpClient = new HttpClient();
                PostMethod postMethod = new PostMethod(endPoint);
                postMethod.setRequestEntity(new StringRequestEntity(requestBodyAsJSON, "application/json", "UTF-8"));
                int returnStatusCode = httpClient.executeMethod(postMethod);
                LOGGER.debug("returnStatusCode: {}", returnStatusCode);
                LOGGER.info("Newsletter subscription servlet - request forwarded to: {}", endPoint);
                if (returnStatusCode == HttpServletResponse.SC_OK) {
                    return NewsletterSubscriptionResponse.STATUS_SUBSCRIBED;
                } else {
                    return NewsletterSubscriptionResponse.STATUS_ERROR;
                }
            } catch (final IOException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        return NewsletterSubscriptionResponse.STATUS_ERROR;
    }
}
