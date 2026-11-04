package com.riten.test.core.captcha;

import org.apache.commons.lang.StringUtils;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(
        name = "Insurance - Captcha Configuration",
        description = "This service is used for configuring various captcha settings"
)
public @interface CaptchaConfigurationServiceConfig {

    @AttributeDefinition(
            name = "reCaptcha V2 Secret Key",
            description = "Enter the Secret Key for reCAPTCHA validation",
            type = AttributeType.STRING
    )
    String secretKey() default StringUtils.EMPTY;

    @AttributeDefinition(
            name = "reCaptcha V2 Site Key",
            description = "Enter the Site Key for reCAPTCHA validation",
            type = AttributeType.STRING
    )
    String siteKey() default StringUtils.EMPTY;

    @AttributeDefinition(
            name = "reCAPTCHA V2 Invisible secretKey",
            description = "Enter the Secret Key for invisible reCAPTCHA validation",
            type = AttributeType.STRING
    )
    String secretKeyInvisible() default StringUtils.EMPTY;

    @AttributeDefinition(
            name = "reCAPTCHA V2 Invisible siteKey",
            description = "Enter the Site Key for invisible reCAPTCHA validation",
            type = AttributeType.STRING
    )
    String siteKeyInvisible() default StringUtils.EMPTY;

    @AttributeDefinition(
            name = "reCAPTCHA V3 secretKey",
            description = "Enter the Secret Key for reCAPTCHA V3 validation",
            type = AttributeType.STRING
    )
    String secretKeyV3() default StringUtils.EMPTY;

    @AttributeDefinition(
            name = "reCAPTCHA V3 siteKey",
            description = "Enter the Site Key for reCAPTCHA V3 validation",
            type = AttributeType.STRING
    )
    String siteKeyV3() default StringUtils.EMPTY;

    @AttributeDefinition(
            name = "reCAPTCHA V3 Threshold",
            description = "Enter the threshold for reCAPTCHA V3 validation (default is 0.5)",
            type = AttributeType.STRING
    )
    String recaptchaV3Threshold() default "0.5";

    @AttributeDefinition(
            name = "URL",
            description = "Enter the URL for reCAPTCHA validation",
            type = AttributeType.STRING
    )
    String verificationUrl() default StringUtils.EMPTY;

}
