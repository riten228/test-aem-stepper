<%@include file="/libs/fd/af/components/guidesglobal.jsp" %>
<%@page import="amplexor.belins.model.forms.CustomSubmitType,
                amplexor.belins.model.reusable.RunModes,
                amplexor.belins.services.FormsMiddlelayerConfigurationService,
                amplexor.belins.services.captcha.CaptchaValidationService" %>

<%@ page import="amplexor.belins.utils.Constants" %>
<%@ page import="amplexor.belins.utils.LinkUtil" %>
<%@ page import="org.apache.sling.api.SlingHttpServletRequest" %>
<%@ page import="org.apache.sling.api.SlingHttpServletResponse" %>
<%@ page import="org.apache.sling.api.resource.ValueMap" %>
<%@ page import="com.day.cq.commons.Externalizer" %>
<%@ page import="org.apache.commons.lang3.StringUtils" %>
<%@ page import="org.json.JSONObject" %>
<%@ page import="org.slf4j.Logger" %>
<%@ page import="org.slf4j.LoggerFactory" %>

<%-- Core Components JSON Payload Handling Imports --%>
<%@ page import="java.io.BufferedReader" %>
<%@ page import="java.io.InputStreamReader" %>
<%@ page import="java.nio.charset.StandardCharsets" %>
<%@ page import="java.text.MessageFormat" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.stream.Collectors" %>
<%@ page import="java.util.stream.Stream" %>

<%-- Outgoing Http Client Loopback Imports --%>
<%@ page import="org.apache.http.Header" %>
<%@ page import="org.apache.http.HttpEntity" %>
<%@ page import="org.apache.http.HttpResponse" %>
<%@ page import="org.apache.http.HttpStatus" %>
<%@ page import="org.apache.http.client.HttpClient" %>
<%@ page import="org.apache.http.client.methods.HttpPost" %>
<%@ page import="org.apache.http.client.utils.URLEncodedUtils" %>


<%@ page import="org.apache.http.entity.ContentType" %>
<%@ page import="org.apache.http.entity.mime.MultipartEntityBuilder" %>
<%@ page import="org.apache.http.impl.client.DefaultHttpClient" %>
<%@ page import="org.apache.http.util.EntityUtils" %>

<%-- Keeping Adaptive Form Utils only if required for backend metadata extraction --%>
<%@ page import="com.adobe.aemds.guide.common.GuideConstants" %>
<%@ page import="com.adobe.aemds.guide.utils.GuideSubmitUtils" %>
<%@ page import="com.adobe.aemds.guide.utils.GuideUtils" %>

<%@taglib prefix="sling" uri="http://sling.apache.org/taglibs/sling/1.0" %>
<%@taglib prefix="cq" uri="http://www.day.com/taglibs/cq/1.0" %>
<sling:defineObjects/>

<%!
  private final Logger log = LoggerFactory.getLogger(getClass());
%>

<%
    final SlingHttpServletRequest slingHttpServletRequest = slingRequest;
    final ValueMap props = properties;

    log.info("[CustomSubmit] Entered post.POST.jsp, resource path={}, method={}",
        resource != null ? resource.getPath() : null,
        slingHttpServletRequest != null ? slingHttpServletRequest.getMethod() : null);

    if (slingHttpServletRequest != null && props != null) {

      // 1. Core Components submit everything via JSON payload. We must read the stream.
      String jsonBody = "";
      try (BufferedReader reader = new BufferedReader(new InputStreamReader(slingHttpServletRequest.getInputStream(), StandardCharsets.UTF_8))) {
        jsonBody = reader.lines().collect(Collectors.joining(System.lineSeparator()));
      }
      log.info("[CustomSubmit] JSON body read, length={}", jsonBody.length());

      JSONObject submissionPayload = new JSONObject();
      JSONObject formDataJson = new JSONObject();
      if (StringUtils.isNotBlank(jsonBody)) {
        submissionPayload = new JSONObject(jsonBody);

        formDataJson = submissionPayload.optJSONObject("data");
        if (formDataJson == null) {
          formDataJson = new JSONObject();
        }
      }

      log.info("[CustomSubmit] Parsed data keys present (names only): {}", java.util.Arrays.toString(JSONObject.getNames(formDataJson)));

      // 2. Extract values from the JSON object
      String captchaCode = formDataJson.optString("_captchaField");
      String locale = formDataJson.optString("_localeField");
      if (StringUtils.isEmpty(locale)) {
        locale = "nl"; // Fallback safe default matching your locale routing
      }

      log.info("[CustomSubmit] Locale={}, captcha code present={}", locale, StringUtils.isNotEmpty(captchaCode));

      CustomSubmitType customSubmitType = slingHttpServletRequest.adaptTo(CustomSubmitType.class);
      CaptchaValidationService captchaValidationService = customSubmitType.getCaptchaValidationService();
      FormsMiddlelayerConfigurationService formsMiddlelayerConfigurationService = customSubmitType.getFormsMiddlelayerConfigurationService();

      log.info("[CustomSubmit] CustomSubmitType adapted={}, captchaValidationService null={}, formsMiddlelayerConfigurationService null={}",
          customSubmitType != null, captchaValidationService == null, formsMiddlelayerConfigurationService == null);

      if (formsMiddlelayerConfigurationService != null) {
        boolean validCaptcha = customSubmitType.captchaIsDisabled() || captchaValidationService.isDisabled() || captchaValidationService.verifyCaptcha(captchaCode, CaptchaValidationService.RE_CAPTCHA_INVISIBLE);

        log.info("[CustomSubmit] Captcha branch: {}, valid={}",
            (customSubmitType.captchaIsDisabled() || captchaValidationService.isDisabled()) ? "disabled" : "verified", validCaptcha);

        // 3. CAPTCHA FAILURE - Handle AJAX Return instead of Request Forward
        if (!validCaptcha) {
          log.error("Captcha verification failed.");
          response.setContentType("application/json");
          response.setCharacterEncoding("UTF-8");
          response.setStatus(javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST); // 400 bad request

          // Return structured error message that Core Components frontend can process
          JSONObject errorResponse = new JSONObject();
          errorResponse.put("error", "Captcha validation failed.");
          response.getWriter().write(errorResponse.toString());
          return; // Stop processing execution immediately
        }

        log.info("Captcha verification succeeded.");

        // Core Components handle redirects via Client-Side response parsing.
        // Setup a fallback redirect object structure
        JSONObject successRedirectResponse = new JSONObject();

        // Check If post submission is enabled
        String postUrlType = props.get("restEndpointPostUrl", (String) null);
        RunModes runModes = formsMiddlelayerConfigurationService.getRunModes();

        log.info("[CustomSubmit] enableRestEndpointPost={}, restEndpointPostUrl={}, runMode={}",
            props.get("enableRestEndpointPost", (String) null), postUrlType,
            runModes == null ? "null" : (runModes.isAuthor() ? "author" : (runModes.isPublish() ? "publish" : "other")));

        String errorPage = LinkUtil.getExternalMappedLink(slingHttpServletRequest.getResourceResolver(), slingHttpServletRequest.getResourceResolver().adaptTo(Externalizer.class), MessageFormat.format(formsMiddlelayerConfigurationService.getErrorPagePath(), locale.substring(0,2))) + ".html";

        if (props.get("enableRestEndpointPost", (String) null) != null && postUrlType != null && runModes != null && (runModes.isAuthor() || runModes.isPublish())) {
          String formsActionEndpoint = "/bin/forms/formaction";
          if(runModes.isAuthor()) {
            formsActionEndpoint = "http://localhost:4502" + formsActionEndpoint;
          } else {
            formsActionEndpoint = "http://localhost:4503" + formsActionEndpoint;
          }
          log.info("Rest endpoint post starting for type : " + postUrlType + " with endpoint : " + formsActionEndpoint);

          log.info("[CustomSubmit] Loopback endpoint URL={}", formsActionEndpoint);

          HttpClient httpclient = new DefaultHttpClient();
          HttpPost httppost = new HttpPost(formsActionEndpoint);
          MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
          multipartEntityBuilder.addTextBody(Constants.FORMS_TYPE, postUrlType);

          // 4. Handle conversion back to XML/JSON for the legacy endpoint block
          String xmlDataFormat = formDataJson.toString();

          // Filter elements dynamically from the JSON Object rather than parameters
          final JSONObject finalFormDataJson = formDataJson;
          Map<String, String> hiddenFieldsMap = formsMiddlelayerConfigurationService.getHiddenFieldsMap().entrySet().stream()
            .filter(entry -> StringUtils.isNotEmpty(finalFormDataJson.optString(entry.getKey())))
            .collect(Collectors.toMap(entry -> entry.getValue(), entry -> finalFormDataJson.optString(entry.getKey())));

          Map<String, String> hiddenPropertiesMap = formsMiddlelayerConfigurationService.getHiddenPropertiesMap().entrySet().stream()
            .filter(entry -> StringUtils.isNotEmpty((String) props.get(entry.getKey())))
            .collect(Collectors.toMap(entry -> entry.getValue(), entry -> (String) props.get(entry.getKey())));

          Map<String, String> hiddenParameters = Stream.concat(hiddenFieldsMap.entrySet().stream(), hiddenPropertiesMap.entrySet().stream())
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (hiddenField, hiddenProperty) -> hiddenField));

          log.info("[CustomSubmit] hiddenFieldsMap size={}, hiddenPropertiesMap size={}, hiddenParameters size={}",
              hiddenFieldsMap.size(), hiddenPropertiesMap.size(), hiddenParameters.size());

          String hiddenParametersJson = new JSONObject(hiddenParameters).toString();
          ContentType contentTypeXML = ContentType.create("application/xml", StandardCharsets.UTF_8);
          ContentType contentTypeJson = ContentType.create("application/json", StandardCharsets.UTF_8);

          multipartEntityBuilder.addTextBody(Constants.HIDDEN_PARAMETERS, hiddenParametersJson, contentTypeJson);
          multipartEntityBuilder.addTextBody(GuideConstants.JCR_DATA, xmlDataFormat, contentTypeXML);
          multipartEntityBuilder.addTextBody(Constants.REQUEST_FROM_POST_JSP, "true");

          // 5. Retrieve all Situational Dialog Properties
          String thankYouPageNL = (String) props.get("thankYouPageNL");
          String thankYouPageFR = (String) props.get("thankYouPageFR");
          String thankYouPageNLHappy = (String) props.get("thankYouPageNLHappy");
          String thankYouPageFRHappy = (String) props.get("thankYouPageFRHappy");
          String thankYouPageNLHappyTalkAgent = (String) props.get("thankYouPageNLHappyTalkAgent");
          String thankYouPageFRHappyTalkAgent = (String) props.get("thankYouPageFRHappyTalkAgent");

          String thankYouPage = "";

          // Determine flow conditions using form data flags (Adapt these JSON object field lookups to fit your form definition)
          boolean isHappyFlow = "true".equalsIgnoreCase(formDataJson.optString("isHappyFlow"));
          boolean requiresAgent = "true".equalsIgnoreCase(formDataJson.optString("requiresAgent"));

          log.info("[CustomSubmit] isHappyFlow={}, requiresAgent={}", isHappyFlow, requiresAgent);

          // 6. Localized and Situational Thank You Page Conditional Loops
          if (StringUtils.startsWithIgnoreCase(locale, "nl")) {
            multipartEntityBuilder.addTextBody(Constants.FORMS_LANG, "nl_be");

            if (isHappyFlow && requiresAgent && StringUtils.isNotBlank(thankYouPageNLHappyTalkAgent)) {
              thankYouPage = thankYouPageNLHappyTalkAgent;
              log.info("[CustomSubmit] Thank-you page branch selected: NL happy+agent");
            } else if (isHappyFlow && StringUtils.isNotBlank(thankYouPageNLHappy)) {
              thankYouPage = thankYouPageNLHappy;
              log.info("[CustomSubmit] Thank-you page branch selected: NL happy");
            } else if (StringUtils.isNotBlank(thankYouPageNL)) {
              thankYouPage = thankYouPageNL;
              log.info("[CustomSubmit] Thank-you page branch selected: NL default");
            }

          } else if (StringUtils.startsWithIgnoreCase(locale, "fr")) {
            multipartEntityBuilder.addTextBody(Constants.FORMS_LANG, "fr_be");

            if (isHappyFlow && requiresAgent && StringUtils.isNotBlank(thankYouPageFRHappyTalkAgent)) {
              thankYouPage = thankYouPageFRHappyTalkAgent;
              log.info("[CustomSubmit] Thank-you page branch selected: FR happy+agent");
            } else if (isHappyFlow && StringUtils.isNotBlank(thankYouPageFRHappy)) {
              thankYouPage = thankYouPageFRHappy;
              log.info("[CustomSubmit] Thank-you page branch selected: FR happy");
            } else if (StringUtils.isNotBlank(thankYouPageFR)) {
              thankYouPage = thankYouPageFR;
              log.info("[CustomSubmit] Thank-you page branch selected: FR default");
            }
          }

          log.info("[CustomSubmit] Thank-you page selected (raw)={}", thankYouPage);

          // 7. Map Derived Thank You Target Link to Payload and Frontend Response Object
          if (StringUtils.isNotBlank(thankYouPage)) {
            String resolvedThankYouUrl = LinkUtil.getExternalMappedLink(
              slingHttpServletRequest.getResourceResolver(),
              slingHttpServletRequest.getResourceResolver().adaptTo(Externalizer.class),
              thankYouPage
            ) + ".html";

            log.info("[CustomSubmit] Resolved thank-you URL={}", resolvedThankYouUrl);
            multipartEntityBuilder.addTextBody(Constants.THANKYOU_PAGE_URL, resolvedThankYouUrl);
            successRedirectResponse.put("redirectUrl", resolvedThankYouUrl);
          } else {
            log.warn("[CustomSubmit] No thank-you page resolved, redirecting to error page={}", errorPage);
            successRedirectResponse.put("redirectUrl", errorPage);
          }

          // 8. Execute HTTP Client Loopback Request
          httppost.setEntity(multipartEntityBuilder.build());
          try {
            HttpResponse middlelayerResponse = httpclient.execute(httppost);
            int statusCode = middlelayerResponse.getStatusLine().getStatusCode();
            log.info("[CustomSubmit] Middle-layer HTTP status code={}", statusCode);

            if (statusCode == HttpStatus.SC_OK || statusCode == HttpStatus.SC_MOVED_TEMPORARILY) {
              log.info("Middle-layer submission executed with code: " + statusCode);

              response.setContentType("application/json");
              response.setCharacterEncoding("UTF-8");
              response.setStatus(200);
              response.getWriter().write(successRedirectResponse.toString());
              log.info("[CustomSubmit] Success response written (200)");
            } else {
              log.error("Middle-layer integration failed with status code: " + statusCode);
              throw new Exception("Invalid response code from endpoint.");
            }
          } catch (Exception e) {
            log.error("Error executing backend form submission loop-back request", e);

            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.setStatus(500);

            JSONObject errorJson = new JSONObject();
            errorJson.put("error", "Form submission encountered an internal error.");
            errorJson.put("redirectUrl", errorPage);
            response.getWriter().write(errorJson.toString());
            log.info("[CustomSubmit] Error response written (500)");
          }
        } else {
          log.warn("[CustomSubmit] REST endpoint post skipped (enableRestEndpointPost/restEndpointPostUrl missing or run mode not author/publish); no response written");
        }
      } else {
        log.warn("[CustomSubmit] FormsMiddlelayerConfigurationService is null; no response written");
      }
    } else {
      log.warn("[CustomSubmit] Request or component properties null; no response written");
    }
%>
