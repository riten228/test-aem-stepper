package com.riten.test.core.servlets;

import amplexor.belins.services.FormsMiddlelayerConfigurationService;
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
                SLING_SERVLET_PATHS+"=/bin/forms/leadsForConsultant",
                SLING_SERVLET_METHODS+"="+METHOD_GET,
                Constants.SERVICE_DESCRIPTION + "=Leads for consultant Prefill - This servlet will get the leads for consultant and prefill",
                Constants.SERVICE_RANKING + ":Integer=100"
        }
)
public class LeadsForConsultantPrefillServlet extends SlingAllMethodsServlet {

    private static final Logger LOGGER = LoggerFactory.getLogger(LeadsForConsultantPrefillServlet.class);
    private static final String CONSULTANT_KEY = "consultantKey";
    private static final String MAX_LEADS = "maxLeads";
    private static final String ORDER_ON = "_order";
    private static final String SORT_TYPE = "sortType";


    @Reference
    private FormsMiddlelayerConfigurationService formsMiddlelayerConfigurationService;

    @Override
    protected void doGet(final SlingHttpServletRequest request, final SlingHttpServletResponse response) throws ServletException, IOException {
        response.setCharacterEncoding("UTF-8");
        LOGGER.info("AEM to Forms Middlelayer - leadsForConsultant starts");
        if (formsMiddlelayerConfigurationService != null) {
            String prefillPath = formsMiddlelayerConfigurationService.getLeadsForConsultantPrefillPath();
            LOGGER.debug("LeadsForConsultantPrefillPath: {}", prefillPath);
            HttpClient httpClient = new HttpClient();
            HttpMethod getMethod = new GetMethod(prefillPath);
            NameValuePair[] params = new NameValuePair[]{
                    new NameValuePair(CONSULTANT_KEY, request.getParameter(CONSULTANT_KEY)),
                    new NameValuePair(MAX_LEADS, request.getParameter(MAX_LEADS)),
                    new NameValuePair(ORDER_ON, request.getParameter(ORDER_ON)),
                    new NameValuePair(SORT_TYPE, request.getParameter(SORT_TYPE))
            };
            LOGGER.debug("params: {}", Arrays.toString(params));
            getMethod.setQueryString(params);
            try {
                httpClient.executeMethod(getMethod);
                String responseString = getMethod.getResponseBodyAsString();

                LOGGER.debug("responseString: {}", responseString);
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write(responseString);
                LOGGER.info("AEM to Forms Middlelayer - leadsForConsultant end");
            }catch (IOException e){
                LOGGER.error(e.getMessage(), e);
            }
        }
        LOGGER.debug("formsMiddlelayerConfigurationService is null");
    }
}
