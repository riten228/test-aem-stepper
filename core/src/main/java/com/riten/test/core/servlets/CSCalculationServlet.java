package com.riten.test.core.servlets;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.ServletResolverConstants;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;

import static org.osgi.framework.Constants.SERVICE_DESCRIPTION;
import static org.osgi.framework.Constants.SERVICE_VENDOR;

@Component(
  service = Servlet.class,
  property = {
    ServletResolverConstants.SLING_SERVLET_METHODS + "=GET",
    ServletResolverConstants.SLING_SERVLET_PATHS + "=/bin/forms/csCalculation",
    SERVICE_DESCRIPTION + "=CreditSimulator - Calculator integration servlet",
    SERVICE_VENDOR + "=Sites",
    "service.ranking:Integer=100",
  }
)
@Designate(ocd = CSCalculationServlet.Config.class)
public class CSCalculationServlet extends SlingSafeMethodsServlet {

    @ObjectClassDefinition(
      name = "CreditSimulator - Calculator integration servlet",
      description = "CreditSimulator - This servlet will get data from the forms middlelayer that can be used to calculate a credit"
    )

    public @interface Config {
      @AttributeDefinition(
        name = "CreditSimulator - Calculator integration servlet",
        description = "CreditSimulator - This servlet will get data from the forms middlelayer that can be used to calculate a credit"
      )
      boolean enable() default true;;
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(CSCalculationServlet.class);
    private static final String TYPE_LOAN = "typeLoan";
    private static final String AMOUNT_LOAN = "amountLoan";
    private static final String DURATION_LOAN = "durationLoan";

    @Reference
    private FormsMiddlelayerConfigurationService formsMiddlelayerConfigurationService;

    @Override
    protected void doGet(final SlingHttpServletRequest request, final SlingHttpServletResponse response) throws ServletException, IOException {
        if (formsMiddlelayerConfigurationService != null) {
            HttpClient httpClient = new HttpClient();
            HttpMethod getMethod = new GetMethod(formsMiddlelayerConfigurationService.getCsCalculationPath());
            NameValuePair[] params = new NameValuePair[]{
                    new NameValuePair(TYPE_LOAN, request.getParameter(TYPE_LOAN)),
                    new NameValuePair(AMOUNT_LOAN, request.getParameter(AMOUNT_LOAN)),
                    new NameValuePair(DURATION_LOAN, request.getParameter(DURATION_LOAN)),
            };
            LOGGER.debug("params: {}", Arrays.toString(params));
            getMethod.setQueryString(params);
            LOGGER.info("Sending credit simulation request to {} with query string {}", formsMiddlelayerConfigurationService.getCsCalculationPath(), getMethod.getQueryString());
            try {
                int errCode = httpClient.executeMethod(getMethod);
                LOGGER.info("Call to forms backend ended up in HTTP code '{}' with response {}", errCode, getMethod.getResponseBodyAsString());
                if (errCode == HttpServletResponse.SC_INTERNAL_SERVER_ERROR) {
                    response.sendError(errCode);
                }
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write(getMethod.getResponseBodyAsString());
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        } else {
            LOGGER.error("formsMiddlelayerConfigurationService is null");
        }
    }
}
