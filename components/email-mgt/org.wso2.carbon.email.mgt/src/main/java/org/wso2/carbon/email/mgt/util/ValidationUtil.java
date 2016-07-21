/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.email.mgt.util;

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.email.mgt.dto.EmailTemplateDTO;
import org.wso2.carbon.email.mgt.exceptions.I18nEmailMgtClientException;
import org.wso2.carbon.email.mgt.exceptions.I18nEmailMgtException;
import org.wso2.carbon.email.mgt.exceptions.I18nEmailMgtServerException;
import org.wso2.carbon.email.mgt.exceptions.I18nMgtEmailConfigException;
import org.wso2.carbon.email.mgt.model.EmailTemplateType;
import org.wso2.carbon.identity.base.IdentityValidationException;
import org.wso2.carbon.identity.base.IdentityValidationUtil;

public class ValidationUtil {
    private static final String REGEX_KEY_PREFIX = "org.wso2.carbon.email.mgt_";

    private static final String TEMPLATE_DISPLAY_NAME_REGEX_KEY = REGEX_KEY_PREFIX + "displayNameRegex";
    private static final String TEMPLATE_DISPLAY_NAME_REGEX = "^[a-zA-Z0-9/s]+$";

    public static void validateEmailTemplateType(EmailTemplateType emailTemplateType) throws I18nEmailMgtException {
        if (emailTemplateType == null) {
            throw new I18nEmailMgtClientException("Email Template Type cannot be null.");
        }

        if (StringUtils.isBlank(emailTemplateType.getDisplayName())) {
            throw new I18nMgtEmailConfigException("Template type properties empty.");
        }

        validateTemplateDisplayName(emailTemplateType.getDisplayName());
    }


    public static void validateTemplateDisplayName(String displayName) throws I18nEmailMgtException {
        if (StringUtils.isBlank(displayName)) {
            throw new I18nMgtEmailConfigException("Template Type display name cannot be empty.");
        }

        // TODO uncomment once the PR is merged.
//        IdentityValidationUtil.addPattern(TEMPLATE_DISPLAY_NAME_REGEX_KEY, TEMPLATE_DISPLAY_NAME_REGEX);
//        try {
//            if(!IdentityValidationUtil.isValidOverWhiteListPatterns(displayName, TEMPLATE_DISPLAY_NAME_REGEX_KEY)){
//                throw new I18nEmailMgtClientException("Invalid Template Display Name : " +displayName);
//            }
//        } catch (IdentityValidationException e) {
//            throw new I18nEmailMgtServerException("Error when validating template type name : " + displayName, e);
//        }

    }

    public static void validateEmailTemplate(EmailTemplateDTO emailTemplate) throws I18nEmailMgtClientException{
        if (emailTemplate == null) {
            throw new I18nEmailMgtClientException("Email Template cannot be null");
        }
    }


}
