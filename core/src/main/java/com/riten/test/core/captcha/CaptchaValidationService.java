package com.riten.test.core.captcha;

import com.google.gson.Gson;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.json.JSONObject;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@Component(
        service = CaptchaValidationService.class,
        immediate = true,
        property = {
                Constants.SERVICE_DESCRIPTION + "=Insurance - This service is used to validate a reCAPTCHA",
                Constants.SERVICE_RANKING + ":Integer=100"
        }
)
@Designate(ocd = CaptchaValidationService.CaptchaValidationServiceConfig.class)
public class CaptchaValidationService extends SlingAllMethodsServlet {

    private static final Logger LOGGER = LoggerFactory.getLogger(CaptchaValidationService.class);
    public static final String RE_CAPTCHA_V2="reCaptchaV2";
    public static final String RE_CAPTCHA_INVISIBLE="reCaptchaInvisible";

    private boolean disabled;

    @Reference
    private CaptchaConfigurationService captchaConfigurationService;

    @Activate
    @Modified
    public void configurationModified(CaptchaValidationServiceConfig config) {
        LOGGER.info("Updating/Modifying configuration, captcha validation");
        this.disabled = config.disabled();
        LOGGER.info("Updated/Modified configuration, captcha validation");

        LOGGER.debug("disabled: {}", this.disabled);
    }

  public boolean verifyCaptcha(String captchaCode, String reCaptchaVersion) {
    if (this.disabled) {
      return true;
    }

    boolean succeeded = false;
    HttpClient httpClient = new HttpClient();

    // Configure proxy if available
    /*String proxyHost = captchaConfigurationService.getProxyHost(); // Add this method to your service
    Integer proxyPort = captchaConfigurationService.getProxyPort(); // Add this method to your service*/
    String proxyHost = "proxy-app.belwired.net"; // e.g., "proxy.company.com"
    Integer proxyPort = 8080; // e.g., 8080
    if (StringUtils.isNotEmpty(proxyHost) && proxyPort != null && proxyPort > 0) {
      HostConfiguration hostConfig = new HostConfiguration();
      hostConfig.setProxy(proxyHost, proxyPort);
      httpClient.setHostConfiguration(hostConfig);
      LOGGER.debug("Using proxy: {}:{}", proxyHost, proxyPort);
    } else {
      LOGGER.debug("No proxy configured, using direct connection");
    }

    if (StringUtils.isNotEmpty(captchaCode) && captchaConfigurationService != null) {
      final PostMethod postMethod = new PostMethod(captchaConfigurationService.getVerificationUrl());
      if (RE_CAPTCHA_V2.equals(reCaptchaVersion)) {
        postMethod.addParameter("secret", captchaConfigurationService.getSecretKey());
      } else if (RE_CAPTCHA_INVISIBLE.equals(reCaptchaVersion)) {
        postMethod.addParameter("secret", captchaConfigurationService.getSecretKeyInvisible());
      } else {
        LOGGER.error("Wrong reCaptcha version specified");
        return false;
      }

      postMethod.addParameter("response", captchaCode);
      try {
        LOGGER.error("Verifying reCAPTCHA with response: {}", captchaCode);
        int returnCode = httpClient.executeMethod(postMethod);
        LOGGER.debug("returnCode: {}", returnCode);
        if (returnCode == HttpStatus.SC_OK) {
          String responseString = postMethod.getResponseBodyAsString();
          LOGGER.debug("responseString: {}", responseString);
          CaptchaResponse captchaValidationResponse = new Gson().fromJson(responseString, CaptchaResponse.class);
          if (captchaValidationResponse != null && captchaValidationResponse.success) {
            succeeded = true;
          }
        }
      } catch (final IOException e) {
        LOGGER.error("reCAPTCHA validation failed", e);
      } finally {
        postMethod.releaseConnection();
      }
    }
    return succeeded;
  }

  public boolean verifyRecaptcha(String token) {
    String secretKey = captchaConfigurationService.getSecretKeyV3();
    String verifyUrl = captchaConfigurationService.getVerificationUrl();
    LOGGER.info("verifyRecaptcha, secretKey {} >> ", secretKey);
    LOGGER.info("verifyRecaptcha, verifyUrl {} >> ", verifyUrl);
    boolean succeeded = false;

    if (StringUtils.isNotEmpty(token) && captchaConfigurationService != null) {
      // Proxy configuration (replace with your actual proxy details)
      String proxyHost = "proxy-app.belwired.net"; // e.g., "proxy.company.com"
      int proxyPort = 8080; // e.g., 8080

      HttpHost proxy = new HttpHost(proxyHost, proxyPort);
      RequestConfig config = RequestConfig.custom()
        .setProxy(proxy)
        .setConnectTimeout(5000) // 5 seconds
        .setSocketTimeout(5000)  // 5 seconds
        .build();

      try (CloseableHttpClient httpClient = HttpClients.custom()
        .setDefaultRequestConfig(config)
        .build()) {

        HttpPost httpPost = new HttpPost(verifyUrl);
        httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");

        // Add parameters to the request body
        StringEntity params = new StringEntity(
          "secret=" + secretKey + "&response=" + token
        );
        httpPost.setEntity(params);

        LOGGER.info("Verifying reCAPTCHA V3 with response: {}", token);
        String responseString = EntityUtils.toString(httpClient.execute(httpPost).getEntity());
        LOGGER.debug("responseString: {}", responseString);

        JSONObject jsonResponse = new JSONObject(responseString);
        double scoreThreshold = Double.parseDouble(captchaConfigurationService.getRecaptchaV3Threshold());
        LOGGER.info("verifyRecaptcha, scoreThreshold from config {} >> ", scoreThreshold);
        LOGGER.info("verifyRecaptcha, response score {} >> ", jsonResponse.getDouble("score"));
        LOGGER.info("verifyRecaptcha, response isSuccess {} >> ", jsonResponse.getBoolean("success"));

        if (jsonResponse.getBoolean("success") && jsonResponse.getDouble("score") >= scoreThreshold) {
          succeeded = true;
        }
      } catch (final IOException e) {
        LOGGER.error("reCAPTCHA validation failed", e);
      }
    }
    return succeeded;
  }

    public boolean isDisabled() {
        return disabled;
    }

    @ObjectClassDefinition(
            name = "Insurance - Captcha Validation",
            description = "This service is used for configuring captcha validation settings"
    )
    public @interface CaptchaValidationServiceConfig {

        @AttributeDefinition(
                name = "disabled",
                description = "Explicitly disable reCAPTCHA validation on this environment",
                type = AttributeType.BOOLEAN
        )
        boolean disabled() default false;
    }

}
