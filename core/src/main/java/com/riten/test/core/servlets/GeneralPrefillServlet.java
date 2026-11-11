package com.riten.test.core.servlets;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.regex.Pattern;

import static org.apache.sling.api.servlets.HttpConstants.METHOD_GET;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_METHODS;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_PATHS;

@Component(
        service = Servlet.class,
        property = {
                SLING_SERVLET_PATHS+"=/bin/forms/generalPrefill",
                SLING_SERVLET_METHODS+"="+METHOD_GET,
                Constants.SERVICE_DESCRIPTION + "=General Prefill - This servlet will get data for the dropout mail or accept summary prefilling",
                Constants.SERVICE_RANKING + ":Integer=100"
        }
)
public class GeneralPrefillServlet extends SlingAllMethodsServlet {

    private static final Logger LOGGER = LoggerFactory.getLogger(GeneralPrefillServlet.class);
    private static final String TRANSACTION_KEY = "transactionKey";
    private static final String EVENT_KEY = "eventKey";
    private static final Pattern SAFE_PARAM_PATTERN = Pattern.compile("^[A-Za-z0-9_\\-]{1,128}$");

    @Reference
    private FormsMiddlelayerConfigurationService formsMiddlelayerConfigurationService;

    private static boolean isValidParam(final String value) {
        return value != null && SAFE_PARAM_PATTERN.matcher(value).matches();
    }

    @Override
    protected void doGet(final SlingHttpServletRequest request, final SlingHttpServletResponse response) throws ServletException, IOException {
        if (formsMiddlelayerConfigurationService != null) {
            String generalPrefillPath = formsMiddlelayerConfigurationService.getGeneralPrefillPath();
            LOGGER.debug("generalPrefillPath: {}", generalPrefillPath);

            String transactionKey = request.getParameter(TRANSACTION_KEY);
            String eventKey = request.getParameter(EVENT_KEY);
            if (!isValidParam(transactionKey) || !isValidParam(eventKey)) {
                LOGGER.warn("Invalid {} or {} parameter", TRANSACTION_KEY, EVENT_KEY);
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            HttpClient httpClient = new HttpClient();
            HttpMethod getMethod = new GetMethod(generalPrefillPath);
            NameValuePair[] params = new NameValuePair[]{
                    new NameValuePair(TRANSACTION_KEY, transactionKey),
                    new NameValuePair(EVENT_KEY, eventKey)
            };
            LOGGER.debug("params: {}", Arrays.toString(params));
            getMethod.setQueryString(params);

            try {
                httpClient.executeMethod(getMethod);
                String responseString = getMethod.getResponseBodyAsString();
                LOGGER.debug("responseString: {}", responseString);
                response.setCharacterEncoding("UTF-8");
                response.setContentType("application/json");
                response.getWriter().write(responseString);
            } catch (IOException e){
                LOGGER.error(e.getMessage(), e);
            } finally {
                getMethod.releaseConnection();
            }
        } else {
            LOGGER.error("formsMiddlelayerConfigurationService is null");
        }
    }
}
