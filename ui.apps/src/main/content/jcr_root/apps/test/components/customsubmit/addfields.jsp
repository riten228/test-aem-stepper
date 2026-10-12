<%------------------------------------------------------------------------
 ~
 ~ ADOBE CONFIDENTIAL
 ~ __________________
 ~
 ~  Copyright 2014 Adobe Systems Incorporated
 ~  All Rights Reserved.
 ~
 ~ NOTICE:  All information contained herein is, and remains
 ~ the property of Adobe Systems Incorporated and its suppliers,
 ~ if any.  The intellectual and technical concepts contained
 ~ herein are proprietary to Adobe Systems Incorporated and its
 ~ suppliers and may be covered by U.S. and Foreign Patents,
 ~ patents in process, and are protected by trade secret or copyright law.
 ~ Dissemination of this information or reproduction of this material
 ~ is strictly forbidden unless prior written permission is obtained
 ~ from Adobe Systems Incorporated.
 --------------------------------------------------------------------------%>
<%@include file="/libs/fd/af/components/guidesglobal.jsp"%>
<%@page import="org.slf4j.Logger" %>
<%@page import="org.slf4j.LoggerFactory" %>
<input type="hidden" id="hidden_profile_firstName" name="_hidden_profile_firstName" value="">
<input type="hidden" id="hidden_profile_lastName" name="_hidden_profile_lastName" value="">
<input type="hidden" id="hidden_client_zipCode" name="_hidden_client_zipCode" value="">
<input type="hidden" id="hidden_profile_birthDate" name="_hidden_profile_birthDate" value="">
<input type="hidden" id="hidden_client_sti" name="_hidden_client_sti" value="">
<input type="hidden" id="hidden_client_houseNr" name="_hidden_client_houseNr" value="">
<input type="hidden" id="hidden_client_gmiVlg" name="_hidden_client_gmiVlg" value="">
<input type="hidden" id="hidden_profile_phone" name="_hidden_profile_phone" value="">
<input type="hidden" id="hidden_profile_gender" name="_hidden_profile_gender" value="">

<input type="hidden" id="guideValueMap" name="_guideValueMap" value="yes"/>
<input type="hidden" id="localeField" name="_localeField" value="">
<input type="hidden" id="captchaField" name="_captchaField" value="">
<input type="hidden" id="hiddenPsi" name="_hiddenPsi" value="">
<input type="hidden" id="hiddenPsiAdb" name="_hiddenPsiAdb" value="">
<input type="hidden" id="hiddenDynamicPage" name="_hiddenDynamicPage" value="">
<input type="hidden" id="hiddenHdk" name="_hiddenHdk" value="">
<input type="hidden" id="hiddenKti" name="_hiddenKti" value="">
<input type="hidden" id="hiddenNationalNumber" name="_hiddenNationalNumber" value="">

<input type="hidden" id="hiddenFunnelID" name="_hiddenFunnelID" value="">
<input type="hidden" id="hiddenCampaignKey" name="_hiddenCampaignKey" value="">
<input type="hidden" id="hiddenEventKey" name="_hiddenEventKey" value="">
<input type="hidden" id="hiddenTransactionKey" name="_hiddenTransactionKey" value="">
<input type="hidden" id="hiddenConsultantKey" name="_hiddenConsultantKey" value="">
<input type="hidden" id="hiddenLeadsForConsultant" name="_hiddenLeadsForConsultant" value="">
<input type="hidden" id="hiddenAutoSaveUUID" name="_hiddenAutoSaveUUID" value="">
<input type="hidden" id="hiddenClientKey" name="_hiddenClientKey" value="">
<input type="hidden" id="hiddenClientOldKey" name="_hiddenClientOldKey" value="">
<input type="hidden" id="hiddenIsAlreadySubmitted" name="_hiddenIsAlreadySubmitted" value="">
<input type="hidden" id="hiddenTicketCount" name="_hiddenTicketCount" value="">
<input type="hidden" id="hiddenTransactionConnectedExistingLead" name="_hiddenTransactionConnectedExistingLead" value="no">
<input type="hidden" id="prodId" name="_prodId" value="">
<input type="hidden" id="hiddenPKey" name="_hiddenPKey" value="">
<%--_hiddenStreet could be removed in the future if the GuideCompositeField is used for the client_sti field - this allows the widget to pass 2 values--%>
<input type="hidden" id="hiddenStreet" name="_hiddenStreet" value="">
<input type="hidden" id="hiddenStreet2" name="_hiddenStreet2" value="">
<input type="hidden" id="hiddenUtmCampaign" name="_hiddenUtmCampaign" value="">
<input type="hidden" id="hiddenUtmMedium" name="_hiddenUtmMedium" value="">
<input type="hidden" id="hiddenUtmSource" name="_hiddenUtmSource" value="">
<input type="hidden" id="hiddenUtmContent" name="_hiddenUtmContent" value="">
<input type="hidden" id="hiddenCmpVariable" name="_hiddenCmpVariable" value="">

<input type="hidden" id="cs_interestRateFixed" name="cs_interestRateFixed" value="">
<input type="hidden" id="cs_interestRateVariable" name="cs_interestRateVariable" value="">
<input type="hidden" id="cs_aprcFixed" name="cs_aprcFixed" value="">
<input type="hidden" id="cs_aprcVariable" name="cs_aprcVariable" value="">

<input type="hidden" id="cs_monthlyAmountFixed" name="cs_monthlyAmountFixed" value="">
<input type="hidden" id="cs_monthlyAmountVariable" name="cs_monthlyAmountVariable" value="">
<input type="hidden" id="cs_totalAmountWithRateFixed" name="cs_totalAmountWithRateFixed" value="">
<input type="hidden" id="cs_totalAmountWithRateVariable" name="cs_totalAmountWithRateVariable" value="">

<input type="hidden" id="cs_totalAmountFixed" name="cs_totalAmountFixed" value="">
<input type="hidden" id="cs_totalAmountVariable" name="cs_totalAmountVariable" value="">

<input type="hidden" id="dp_calculatedProduct" name="dp_calculatedProduct" value="">
<input type="hidden" id="dp_calculatedMonthlyProductPrice" name="dp_calculatedMonthlyProductPrice" value="">
<input type="hidden" id="dp_calculatedProductPrice" name="dp_calculatedProductPrice" value="">
<input type="hidden" id="dp_calculatedOriginalProductPrice" name="dp_calculatedOriginalProductPrice" value="">

<%-- moto flow --%>
<input type="hidden" id="dp_age" name="dp_age" value="">
<input type="hidden" id="dp_motoMobility" name="dp_motoMobility" value="">
<input type="hidden" id="dp_dynamic_assurance_type" name="dp_dynamic_assurance_type" value="">
<input type="hidden" id="dp_rb_amount" name="dp_rb_amount" value="">
<input type="hidden" id="dp_assistance_amount" name="dp_assistance_amount" value="">

<%-- moto mail values --%>
<input type="hidden" id="dp_rechtsbijstand" name="dp_rechtsbijstand" value="">
<input type="hidden" id="dp_assistance" name="dp_assistance" value="">
<input type="hidden" id="dp_carInsurance" name="dp_carInsurance" value="">

<%-- family mail values --%>
<input type="hidden" id="dp_extendedCivil" name="dp_extendedCivil" value="">


<%-- cocoon flow --%>
<input type="hidden" id="cocoon_currentAddressIdentical" name="cocoon_currentAddressIdentical" value="">
<input type="hidden" id="cocoon_commercialPurposes" name="cocoon_commercialPurposes" value="">
<input type="hidden" id="cocoon_hiddenAccomodationStreet" name="cocoon_hiddenAccomodationStreet" value="">
<input type="hidden" id="happyFlowThankYouPage" name="happyFlowThankYouPage" value="">

<input type="hidden" id="cocoon_hiddenThankYouPage" name="cocoon_hiddenThankYouPage" value="">
<input type="hidden" id="cocoon_checkByAgent" name="cocoon_checkByAgent" value="">
<input type="hidden" id="cocoon_submitType" name="cocoon_submitType" value="">
<input type="hidden" id="hidden_leadType" name="hidden_leadType" value="">

<%-- 10 data - Price Card --%>
<input type="hidden" id="pc_totalPrice" name="pc_totalPrice" value="">
<input type="hidden" id="pc_totalDiscountPrice" name="pc_totalDiscountPrice" value="">
<input type="hidden" id="pc_discountTypeTotal" name="pc_discountTypeTotal" value="">

<input type="hidden" id="pc_primaryPrice" name="pc_primaryPrice" value="">
<input type="hidden" id="pc_primaryDiscountPrice" name="pc_primaryDiscountPrice" value="">
<input type="hidden" id="pc_discountTypePrimary" name="pc_discountTypePrimary" value="">

<input type="hidden" id="pc_optionOnePrice" name="pc_optionOnePrice" value="">
<input type="hidden" id="pc_optionOneDiscountPrice" name="pc_optionOneDiscountPrice" value="">
<input type="hidden" id="pc_discountTypeOptionOne" name="pc_discountTypeOptionOne" value="">

<input type="hidden" id="pc_optionTwoPrice" name="pc_optionTwoPrice" value="">
<input type="hidden" id="pc_optionTwoDiscountPrice" name="pc_optionTwoDiscountPrice" value="">
<input type="hidden" id="pc_discountTypeOptionTwo" name="pc_discountTypeOptionTwo" value="">

<input type="hidden" id="pc_optionThreePrice" name="pc_optionThreePrice" value="">
<input type="hidden" id="pc_optionThreeDiscountPrice" name="pc_optionThreeDiscountPrice" value="">
<input type="hidden" id="pc_discountTypeOptionThree" name="pc_discountTypeOptionThree" value="">

<%-- Event Data --%>
<input type="hidden" id="hidden_eventName" name="_hidden_eventName" value="">
<input type="hidden" id="hidden_eventNameNL" name="_hidden_eventNameNL" value="">
<input type="hidden" id="hidden_eventNameFR" name="_hidden_eventNameFR" value="">
<input type="hidden" id="hidden_eventInfoText" name="_hidden_eventInfoText" value="">
<input type="hidden" id="hidden_eventInfoText2" name="_hidden_eventInfoText2" value="">
<input type="hidden" id="hidden_eventDT" name="_hidden_eventDT" value="">
<input type="hidden" id="hidden_eventZipcode" name="_hidden_eventZipcode" value="">

<%-- Insurance scan Data --%>
<input type="hidden" id="hidden_transaction_insuranceScanResult" name="_hidden_transaction_insuranceScanResult" value=""/>