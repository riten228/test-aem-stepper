package com.riten.test.core.servlets;

import amplexor.belins.services.FormsMiddlelayerConfigurationService;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.lang3.StringUtils;
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
import java.net.URLDecoder;
import java.util.Arrays;

import static org.apache.sling.api.servlets.HttpConstants.METHOD_GET;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_METHODS;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_PATHS;

//       (lokaal) http://10.0.2.2:7777/forms/cpdb/search?firstName=John&lastName=Doe&email=1@test.com&birthdate=01/01/1960
//       http://localhost:7777/forms/cpdb/search?firstName=John&lastName=Doe&email=1@test.com&birthdate=01/01/1960
//       http://127.0.0.1:7777/forms/cpdb/search?firstName=John&lastName=Doe&email=2@test.com&birthdate=01/01/1960
//       http://127.0.0.1:7777/forms/cpdb/search?firstName=John&lastName=Doe&email=3@test.com&birthdate=01/01/1960 --> error not found
//       http://127.0.0.1:7777/forms/cpdb/search?firstName=John&lastName=Doe --> error 400
@Component(
        service = Servlet.class,
        property = {
                SLING_SERVLET_PATHS+"=/bin/forms/cpdbPerson",
                SLING_SERVLET_METHODS+"="+METHOD_GET,
                Constants.SERVICE_DESCRIPTION + "=CPDB - This servlet will get data from the forms middlelayer that can be used to prefill the form",
                Constants.SERVICE_RANKING + ":Integer=100"
        }
)
public class CpdbPersonServlet extends SlingAllMethodsServlet {

    private static final Logger LOGGER = LoggerFactory.getLogger(CpdbPersonServlet.class);
    private static final String FIRST_NAME = "Firstname";
    private static final String NAME = "Name";
    private static final String BIRTHDATE = "Birthdate";
    private static final String STI = "sti";
    private static final String STREET = "Street";
    private static final String HOUSE_NUMBER = "HouseNbr";
    private static final String BOX = "Box";
    private static final String GMIVLG = "gmivlg";
    private static final String POSTCODE = "Postcode";
    private static final String PHONE = "Phone";
    private static final String GENDER = "Gender";

    @Reference
    private FormsMiddlelayerConfigurationService formsMiddlelayerConfigurationService;

    @Override
    protected void doGet(final SlingHttpServletRequest request, final SlingHttpServletResponse response) throws ServletException, IOException {
        if (formsMiddlelayerConfigurationService != null) {
            HttpClient httpClient = new HttpClient();
            HttpMethod getMethod = new GetMethod(formsMiddlelayerConfigurationService.getCpdbPath());
            LOGGER.debug("Request url querystring {}",request.getQueryString());
            NameValuePair[] params = new NameValuePair[]{
                    new NameValuePair(FIRST_NAME, URLDecoder.decode(request.getParameter(FIRST_NAME), "UTF-8")),
                    new NameValuePair(NAME, request.getParameter(NAME)),
                    new NameValuePair(BIRTHDATE, request.getParameter(BIRTHDATE)),
                    new NameValuePair(STI, request.getParameter(STI)),
                    new NameValuePair(STREET, request.getParameter(STREET)),
                    new NameValuePair(HOUSE_NUMBER, request.getParameter(HOUSE_NUMBER)),
                    new NameValuePair(BOX, request.getParameter(BOX)),
                    new NameValuePair(GMIVLG, request.getParameter(GMIVLG)),
                    new NameValuePair(POSTCODE, request.getParameter(POSTCODE)),
                    new NameValuePair(PHONE, request.getParameter(PHONE)),
                    new NameValuePair(GENDER, request.getParameter(GENDER))
            };
            if(StringUtils.isBlank(request.getParameter(FIRST_NAME)) || StringUtils.isBlank(request.getParameter(NAME)) || StringUtils.isBlank(request.getParameter(POSTCODE))){
                LOGGER.error("Missing first name, name or zipcode, the querystring is {}",request.getQueryString());
            }
            LOGGER.debug("params: {}", Arrays.toString(params));
            getMethod.setQueryString(params);
            LOGGER.info("Sending CPDB search request to {} with query string {}", formsMiddlelayerConfigurationService.getCpdbPath(), getMethod.getQueryString());

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
