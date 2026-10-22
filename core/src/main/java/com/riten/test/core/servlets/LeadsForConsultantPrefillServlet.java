package com.riten.test.core.servlets;

import amplexor.belins.services.FormsMiddlelayerConfigurationService;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.lang.StringUtils;
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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

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

    private static final Set<String> ALLOWED_PREFILL_HOSTS = new HashSet<>(Arrays.asList(
            "api.example.com",
            "campaign.example.com"
    ));

    private static final Pattern SAFE_CONSULTANT_KEY = Pattern.compile("^[A-Za-z0-9_-]{1,64}$");
    private static final Pattern SAFE_ORDER_ON = Pattern.compile("^(createdDate|updatedDate|priority)$");
    private static final Pattern SAFE_SORT_TYPE = Pattern.compile("^(asc|desc)$");

    @Reference
    private FormsMiddlelayerConfigurationService formsMiddlelayerConfigurationService;

    @Override
    protected void doGet(final SlingHttpServletRequest request, final SlingHttpServletResponse response) throws ServletException, IOException {
        response.setCharacterEncoding("UTF-8");
        LOGGER.info("AEM to Forms Middlelayer - leadsForConsultant starts");

        if (formsMiddlelayerConfigurationService == null
                || StringUtils.isBlank(formsMiddlelayerConfigurationService.getLeadsForConsultantPrefillPath())) {
            LOGGER.error("formsMiddlelayerConfigurationService is null or prefill path is blank");
            response.setStatus(500);
            return;
        }

        String prefillPath = formsMiddlelayerConfigurationService.getLeadsForConsultantPrefillPath();
        if (!isAllowedPrefillEndpoint(prefillPath)) {
            LOGGER.error("Blocked outbound prefill endpoint: {}", prefillPath);
            response.setStatus(400);
            response.getWriter().write("{\"error\":\"Invalid endpoint configuration\"}");
            return;
        }

        LOGGER.debug("LeadsForConsultantPrefillPath: {}", prefillPath);
        HttpClient httpClient = new HttpClient();
        HttpMethod getMethod = new GetMethod(prefillPath);

        List<NameValuePair> safeParams = new ArrayList<>();

        String consultantKey = validatedParam(request, CONSULTANT_KEY, SAFE_CONSULTANT_KEY, 64);
        String maxLeads = validatedMaxLeads(request);
        String orderOn = validatedParam(request, ORDER_ON, SAFE_ORDER_ON, 32);
        String sortType = validatedParam(request, SORT_TYPE, SAFE_SORT_TYPE, 4);

        if (consultantKey != null) {
            safeParams.add(new NameValuePair(CONSULTANT_KEY, consultantKey));
        }
        if (maxLeads != null) {
            safeParams.add(new NameValuePair(MAX_LEADS, maxLeads));
        }
        if (orderOn != null) {
            safeParams.add(new NameValuePair(ORDER_ON, orderOn));
        }
        if (sortType != null) {
            safeParams.add(new NameValuePair(SORT_TYPE, sortType));
        }

        NameValuePair[] params = safeParams.toArray(new NameValuePair[0]);
        LOGGER.debug("params: {}", Arrays.toString(params));
        getMethod.setQueryString(params);

        try {
            httpClient.executeMethod(getMethod);
            String responseString = getMethod.getResponseBodyAsString();

            LOGGER.debug("responseString: {}", responseString);
            response.setCharacterEncoding("UTF-8");
            if (responseString != null) {
                response.getWriter().write(responseString);
            }
            LOGGER.info("AEM to Forms Middlelayer - leadsForConsultant end");
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            response.setStatus(500);
        }
    }

    private boolean isAllowedPrefillEndpoint(String endpoint) {
        try {
            URI uri = new URI(endpoint);
            String scheme = uri.getScheme();
            String host = uri.getHost();
            if (!"https".equalsIgnoreCase(scheme) || StringUtils.isBlank(host)) {
                return false;
            }
            return ALLOWED_PREFILL_HOSTS.contains(host.toLowerCase(Locale.ROOT));
        } catch (URISyntaxException e) {
            LOGGER.error("Invalid prefill endpoint URI", e);
            return false;
        }
    }

    private String validatedParam(SlingHttpServletRequest request, String name, Pattern pattern, int maxLen) {
        String value = request.getParameter(name);
        if (value == null) {
            return null;
        }
        value = value.trim();
        if (value.isEmpty() || value.length() > maxLen || !pattern.matcher(value).matches()) {
            LOGGER.warn("Invalid request parameter dropped: {}", name);
            return null;
        }
        return value;
    }

    private String validatedMaxLeads(SlingHttpServletRequest request) {
        String value = request.getParameter(MAX_LEADS);
        if (value == null) {
            return null;
        }
        value = value.trim();
        try {
            int n = Integer.parseInt(value);
            if (n < 1 || n > 100) {
                LOGGER.warn("Invalid MAX_LEADS dropped");
                return null;
            }
            return String.valueOf(n);
        } catch (NumberFormatException ex) {
            LOGGER.warn("Non-numeric MAX_LEADS dropped");
            return null;
        }
    }
}
