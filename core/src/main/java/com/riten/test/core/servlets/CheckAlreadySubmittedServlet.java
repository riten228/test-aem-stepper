package com.riten.test.core.servlets;

import amplexor.belins.services.FormsMiddlelayerConfigurationService;
import com.adobe.aemds.guide.utils.GuideConstants;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
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

import javax.annotation.Nonnull;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.xml.ws.http.HTTPException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apache.sling.api.servlets.HttpConstants.METHOD_POST;
import static org.apache.sling.api.servlets.ServletResolverConstants.*;

@Component(
        service = Servlet.class,
        property = {
                SLING_SERVLET_PATHS+"=/bin/forms/checkAlreadySubmitted",
                SLING_SERVLET_METHODS+"="+METHOD_POST,
                SLING_SERVLET_SELECTORS+"=checkalreadysubmittedaction",
                SLING_SERVLET_RESOURCE_TYPES+"=sling/servlet/default",
                Constants.SERVICE_DESCRIPTION + "=check Already Submitted - This servlet will post data from the form to Campaign",
                Constants.SERVICE_RANKING + ":Integer=100"
        }
)
public class CheckAlreadySubmittedServlet extends SlingAllMethodsServlet {

    private static final Logger LOGGER = LoggerFactory.getLogger(CheckAlreadySubmittedServlet.class);
    private static final String DATA_XML = "dataXml";
    private static final String TRANSACTION_PKEY = "transactionPKey";
    private static final String LANG = "lang";
    private static final String FLOWID = "flowID";
    private static final String PRODUCTID = "productId";
    private static final String CONSULTANT_URL_PAGE_NAME = "consultantURLPageName";
    private static final String LEAD_TYPE = "leadType";
    private static final String PSI = "psi";

    @Reference
    private FormsMiddlelayerConfigurationService formsMiddlelayerConfigurationService;

    @Override
    protected void doPost(@Nonnull SlingHttpServletRequest request, @Nonnull SlingHttpServletResponse response) throws ServletException, IOException {

        if (formsMiddlelayerConfigurationService != null && StringUtils.isNotEmpty(formsMiddlelayerConfigurationService.getCheckAlreadySubmittedPath())) {
            String endPoint = formsMiddlelayerConfigurationService.getCheckAlreadySubmittedPath();

            LOGGER.info("CheckAlreadySubmitted servlet - incoming request");

            HttpClient httpClient = new HttpClient();
            PostMethod postMethod = new PostMethod(endPoint);

            Map<String, String> formData = new HashMap<>();
            formData.put(GuideConstants.DATA_XML, request.getParameter(DATA_XML));

            LOGGER.debug("formData data");
            for(String key: formData.keySet()){
                LOGGER.debug(key+": "+formData.get(key));
            }

            Map<String, String> additionalData = new HashMap<>();
            if (request.getParameter(TRANSACTION_PKEY) != null) {
                additionalData.put(TRANSACTION_PKEY, request.getParameter(TRANSACTION_PKEY));
                LOGGER.debug("TRANSACTION_PKEY of CheckAlreadySubmitted {}",TRANSACTION_PKEY);
            }
            if (request.getParameter(LANG) != null) {
                additionalData.put(LANG, request.getParameter(LANG));
            }
            if (request.getParameter(FLOWID) != null) {
                additionalData.put(FLOWID, request.getParameter(FLOWID));
            }

            if (request.getParameter(PRODUCTID) != null) {
                additionalData.put(PRODUCTID, request.getParameter(PRODUCTID));
            }

//            if (request.getParameter(AUTOSAVE_UUID) != null) {
//                additionalData.put(AUTOSAVE_UUID, request.getParameter(AUTOSAVE_UUID));
//                LOGGER.debug("AUTOSAVE_UUID of CheckAlreadySubmitted {}",AUTOSAVE_UUID);
//            }
            if (request.getParameter(CONSULTANT_URL_PAGE_NAME) != null) {
                additionalData.put(CONSULTANT_URL_PAGE_NAME, request.getParameter(CONSULTANT_URL_PAGE_NAME));
            }

            if (request.getParameter(LEAD_TYPE) != null) {
                additionalData.put(LEAD_TYPE, request.getParameter(LEAD_TYPE));
            }

            LOGGER.debug("additionalData data");
            for(String key: additionalData.keySet()){
                LOGGER.debug(key+": "+additionalData.get(key));
            }

            Map<String, String> requestBody = Stream.of(additionalData.entrySet(), formData.entrySet())
                    .flatMap(Set::stream)
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            String requestBodyAsJSON = (new JSONObject(requestBody)).toString();
            LOGGER.debug("requestBodyAsJSON {}",requestBodyAsJSON);
            postMethod.setRequestEntity(new StringRequestEntity(requestBodyAsJSON, "application/json", "UTF-8"));

            try {
                LOGGER.info("CheckAlreadySubmitted servlet - forwarding request to: {}", endPoint);
                httpClient.executeMethod(postMethod);
            } catch (IOException | HTTPException e) {
                LOGGER.error("CheckAlreadySubmitted servlet - error forwarding call", e);
                response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            }
            String responseString = postMethod.getResponseBodyAsString();
            LOGGER.debug("responseString {}",responseString);
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(responseString);
        }
    }
}
