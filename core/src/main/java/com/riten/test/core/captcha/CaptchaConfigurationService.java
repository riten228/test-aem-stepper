package com.riten.test.core.captcha;

import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(
        service = CaptchaConfigurationService.class,
        immediate = true,
        property = {
                Constants.SERVICE_DESCRIPTION + "=Insurance - This service is used for configuring various captcha settings"
        }
)
@Designate(ocd = CaptchaConfigurationServiceConfig.class)
public class CaptchaConfigurationService {

    private static final Logger LOG = LoggerFactory.getLogger(CaptchaConfigurationService.class);

    private String secretKey;
    private String siteKey;
    private String secretKeyInvisible;
    private String siteKeyInvisible;
    private String verificationUrl;
    private String secretKeyV3;
    private String siteKeyV3;

    private String recaptchaV3Threshold;

    @Activate
    @Modified
    public void configurationModified(CaptchaConfigurationServiceConfig config) {
        LOG.info("Updating/Modifying configuration, captcha");
        secretKey = config.secretKey();
        siteKey = config.siteKey();
        secretKeyInvisible = config.secretKeyInvisible();
        siteKeyInvisible = config.siteKeyInvisible();
        verificationUrl = config.verificationUrl();
        secretKeyV3 = config.secretKeyV3();
        siteKeyV3 = config.siteKeyV3();
        recaptchaV3Threshold = config.recaptchaV3Threshold();
        LOG.info("Updated/Modified configuration, captcha");

        LOG.debug("secretKey: {}", secretKey);
        LOG.debug("siteKey: {}", siteKey);
        LOG.debug("secretKeyInvisible: {}", secretKeyInvisible);
        LOG.debug("siteKeyInvisible: {}", siteKeyInvisible);
        LOG.debug("verificationUrl: {}", verificationUrl);
    }

    public String getSecretKey() {
        return secretKey;
    }

    public String getSiteKey() {
        return siteKey;
    }

    public String getSecretKeyInvisible() { return secretKeyInvisible;}

    public String getSiteKeyInvisible() {return siteKeyInvisible;}

    public String getVerificationUrl() {
        return verificationUrl;
    }

    public String getSecretKeyV3() {
      return secretKeyV3;
    }

    public String getSiteKeyV3() {
      return siteKeyV3;
    }

    public String getRecaptchaV3Threshold() {
      return recaptchaV3Threshold;
    }
}
