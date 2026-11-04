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
import java.io.IOException;
import java.util.Arrays;

import static org.apache.sling.api.servlets.HttpConstants.METHOD_GET;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_METHODS;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_PATHS;

@Component(
        service = Servlet.class,
        property = {
                SLING_SERVLET_PATHS+"=/bin/forms/commercialMail",
                SLING_SERVLET_METHODS+"="+METHOD_GET,
                Constants.SERVICE_DESCRIPTION + "=Commercial Mail - This servlet will get data from the forms middlelayer that can be used to prefill the form",
                Constants.SERVICE_RANKING + ":Integer=100"
        }
)
public class CommercialMailServlet extends SlingAllMethodsServlet {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommercialMailServlet.class);
    private static final String CAMPAIGN_KEY = "campaignKey";

    @Reference
    private FormsMiddlelayerConfigurationService formsMiddlelayerConfigurationService;

    @Override
    protected void doGet(final SlingHttpServletRequest request, final SlingHttpServletResponse response) throws ServletException, IOException {
        if (formsMiddlelayerConfigurationService != null) {
            HttpClient httpClient = new HttpClient();
            HttpMethod getMethod = new GetMethod(formsMiddlelayerConfigurationService.getCommercialMailPath());
            NameValuePair[] params = new NameValuePair[]{
                    new NameValuePair(CAMPAIGN_KEY, request.getParameter(CAMPAIGN_KEY))
            };
            LOGGER.debug("params: {}", Arrays.toString(params));
            getMethod.setQueryString(params);
            LOGGER.info("Sending commercial mail request to {} with query string {}", formsMiddlelayerConfigurationService.getCommercialMailPath(), getMethod.getQueryString());
            try {
                httpClient.executeMethod(getMethod);
                String responseString = getMethod.getResponseBodyAsString();
                LOGGER.debug("responseString: {}", responseString);
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write(responseString);
            } catch (Exception e){
                LOGGER.error(e.getMessage(), e);
            }
        } else {
            LOGGER.error("formsMiddlelayerConfigurationService is null");
        }
    }
}
