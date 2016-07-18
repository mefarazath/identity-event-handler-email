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

package org.wso2.carbon.email.mgt.config;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.email.mgt.constants.I18nMgtConstants;
import org.wso2.carbon.email.mgt.exceptions.I18nEmailMgtClientException;
import org.wso2.carbon.email.mgt.exceptions.I18nEmailMgtException;
import org.wso2.carbon.email.mgt.exceptions.I18nEmailMgtServiceException;
import org.wso2.carbon.email.mgt.exceptions.I18nMgtEmailConfigException;
import org.wso2.carbon.email.mgt.internal.I18nMgtDataHolder;
import org.wso2.carbon.email.mgt.model.EmailTemplateType;
import org.wso2.carbon.identity.base.IdentityRuntimeException;
import org.wso2.carbon.identity.base.IdentityValidationException;
import org.wso2.carbon.identity.base.IdentityValidationUtil;
import org.wso2.carbon.identity.core.persistence.registry.RegistryResourceMgtService;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.CollectionImpl;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Provides functionality to manage email templates used in notification emails.
 */
public class EmailTemplateManager {
    private RegistryResourceMgtService resourceMgtService = I18nMgtDataHolder.getInstance().
            getRegistryResourceMgtService();

    private static final String TEMPLATE_BASE_PATH = I18nMgtConstants.EMAIL_TEMPLATE_PATH;
    private static final Log log = LogFactory.getLog(EmailTemplateManager.class);


    /**
     * @param emailTemplateType
     * @param tenantDomain
     * @throws I18nEmailMgtException
     */
    public void addTemplateType(EmailTemplateType emailTemplateType, String tenantDomain) throws I18nEmailMgtException {
        if (emailTemplateType == null) {
            throw new I18nEmailMgtClientException("Email Template Type cannot be null.");
        }

        // validate the template type input
        try {
            validate(emailTemplateType.getName());
            validate(emailTemplateType.getDisplayName());
        } catch (IdentityValidationException e) {
            throw new I18nMgtEmailConfigException("Invalid Characters exist in the email template type.", e);
        }

        String templateName = emailTemplateType.getName();
        String templateDisplayName = emailTemplateType.getDisplayName();
        // replace all spaces and convert to
        String normalizedTemplateName = templateName.replaceAll("//s+", "").toLowerCase();

        // persist the template type to registry ie. create a directory.
        String path = TEMPLATE_BASE_PATH + RegistryConstants.PATH_SEPARATOR + normalizedTemplateName;

        Collection collection = new CollectionImpl();
        collection.addProperty(I18nMgtConstants.EMAIL_TEMPLATE_NAME, normalizedTemplateName);
        collection.addProperty(I18nMgtConstants.EMAIL_TEMPLATE_DISPLAY_NAME, templateDisplayName);
        try {
            resourceMgtService.putIdentityResource(collection, path, tenantDomain);
        } catch (IdentityRuntimeException ex) {
            String errorMsg = String.format("Error adding template type %s to %s tenant.", emailTemplateType, tenantDomain);
            log.error(errorMsg);
            throw new I18nEmailMgtServiceException(errorMsg, ex);
        }
    }

    /**
     * @param tenantDomain
     * @return
     * @throws I18nEmailMgtServiceException
     */
    public List<EmailTemplateType> getAvailableTemplateTypes(String tenantDomain) throws I18nEmailMgtServiceException {
        try {
            List<EmailTemplateType> templateTypeList = new ArrayList<>();
            Collection collection = (Collection) resourceMgtService.getIdentityResource(TEMPLATE_BASE_PATH, tenantDomain);

            for (String templatePath : collection.getChildren()) {
                Resource templateTypeResource = resourceMgtService.getIdentityResource(templatePath, tenantDomain);
                if (templateTypeResource != null) {
                        EmailTemplateType emailTemplateType = getTemplateType(templateTypeResource);
                        templateTypeList.add(emailTemplateType);
                }
            }
            return templateTypeList;
        } catch (IdentityRuntimeException | RegistryException ex) {
            String errorMsg = String.format("Error when retrieving email template types of %s tenant.", tenantDomain);
            log.error(errorMsg);
            throw new I18nEmailMgtServiceException(errorMsg, ex);
        }
    }


    /**
     * @param emailTemplateType
     * @throws IdentityValidationException
     */
    private void validate(String emailTemplateType) throws IdentityValidationException {
        if (StringUtils.isBlank(emailTemplateType)) {
            throw new IdentityValidationException("Email Template Type properties cannot be empty.");
        }

 //       String regex = IdentityValidationUtil.ValidatorPattern.ALPHANUMERICS_ONLY.getRegex();
//        IdentityValidationUtil.getValidInputOverWhiteListPatterns(emailTemplateType, regex);
    }


    private EmailTemplateType getTemplateType(Resource templateTypeResource) {
        String templateName = templateTypeResource.getProperty(I18nMgtConstants.EMAIL_TEMPLATE_NAME);
        String templateDisplayName = templateTypeResource.getProperty(I18nMgtConstants.EMAIL_TEMPLATE_DISPLAY_NAME);

        return new EmailTemplateType(templateName, templateDisplayName);
    }

}
