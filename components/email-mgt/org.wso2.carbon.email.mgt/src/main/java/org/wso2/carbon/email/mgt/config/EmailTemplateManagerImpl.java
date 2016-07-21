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
import org.wso2.carbon.email.mgt.dto.EmailTemplateDTO;
import org.wso2.carbon.email.mgt.exceptions.I18nEmailMgtClientException;
import org.wso2.carbon.email.mgt.exceptions.I18nEmailMgtException;
import org.wso2.carbon.email.mgt.exceptions.I18nEmailMgtServerException;
import org.wso2.carbon.email.mgt.internal.I18nMgtDataHolder;
import org.wso2.carbon.email.mgt.model.EmailTemplateType;
import org.wso2.carbon.email.mgt.util.Util;
import org.wso2.carbon.email.mgt.util.ValidationUtil;
import org.wso2.carbon.identity.base.IdentityRuntimeException;
import org.wso2.carbon.identity.core.persistence.registry.RegistryResourceMgtService;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.CollectionImpl;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides functionality to manage email templates used in notification emails.
 */
public class EmailTemplateManagerImpl implements EmailTemplateManager {
    private I18nMgtDataHolder dataHolder = I18nMgtDataHolder.getInstance();
    private RegistryResourceMgtService resourceMgtService = dataHolder.getRegistryResourceMgtService();

    private static final String TEMPLATE_BASE_PATH = I18nMgtConstants.EMAIL_TEMPLATE_PATH;
    private static final String PATH_SEPARATOR = RegistryConstants.PATH_SEPARATOR;
    private static final Log log = LogFactory.getLog(EmailTemplateManagerImpl.class);


    @Override
    public void addEmailTemplateType(EmailTemplateType emailTemplateType, String tenantDomain) throws
            I18nEmailMgtException {
        ValidationUtil.validateEmailTemplateType(emailTemplateType);

        String templateDisplayName = emailTemplateType.getDisplayName();
        String normalizedTemplateName = Util.getNormalizedName(templateDisplayName);

        // persist the template type to registry ie. create a directory.
        String path = TEMPLATE_BASE_PATH + PATH_SEPARATOR + normalizedTemplateName;
        try {
            // check whether a template exists with the same name.
            if (resourceMgtService.isResourceExists(path, tenantDomain)) {
                String errorMsg = String.format(I18nMgtConstants.ErrorMsg.ERROR_DUPLICATE_TEMPLATE_TYPE,
                        templateDisplayName, tenantDomain);
                throw new I18nEmailMgtClientException(errorMsg);
            }

            Collection collection = createTemplateType(normalizedTemplateName, templateDisplayName);
            resourceMgtService.putIdentityResource(collection, path, tenantDomain);
        } catch (IdentityRuntimeException ex) {
            String errorMsg = String.format("Error adding template type %s to %s tenant.", emailTemplateType,
                    tenantDomain);
            log.error(errorMsg);
            throw new I18nEmailMgtServerException(errorMsg, ex);
        }
    }

    @Override
    public void deleteEmailTemplateType(String templateDisplayName, String tenantDomain) throws I18nEmailMgtException {
        ValidationUtil.validateTemplateDisplayName(templateDisplayName);

        String templateType = Util.getNormalizedName(templateDisplayName);
        String path = TEMPLATE_BASE_PATH + PATH_SEPARATOR + templateType;

        try {
            resourceMgtService.deleteIdentityResource(path, tenantDomain);
        } catch (IdentityRuntimeException ex) {
            String errorMsg = String.format
                    ("Error deleting email template type %s from %s tenant.", templateDisplayName, tenantDomain);
            handleServerException(errorMsg, ex);
        }
    }

    /**
     * @param tenantDomain
     * @return
     * @throws I18nEmailMgtServerException
     */
    @Override
    public List<EmailTemplateType> getAvailableTemplateTypes(String tenantDomain) throws I18nEmailMgtServerException {
        try {
            List<EmailTemplateType> templateTypeList = new ArrayList<>();
            Collection collection = (Collection) resourceMgtService.getIdentityResource(TEMPLATE_BASE_PATH,
                    tenantDomain);

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
            throw new I18nEmailMgtServerException(errorMsg, ex);
        }
    }

    @Override
    public List<EmailTemplateDTO> getAllEmailTemplates(String tenantDomain) throws I18nEmailMgtException {
        List<EmailTemplateDTO> templateList = new ArrayList<>();

        try {
            Collection baseDirectory = (Collection) resourceMgtService.getIdentityResource(
                    TEMPLATE_BASE_PATH, tenantDomain);
            if (baseDirectory != null) {
                for (String templateTypeDirectory : baseDirectory.getChildren()) {
                    Collection templateType = (Collection) resourceMgtService.getIdentityResource(
                            templateTypeDirectory, tenantDomain);
                    if (templateType != null) {
                        for (String template : templateType.getChildren()) {
                            Resource templateResource = resourceMgtService.getIdentityResource(template, tenantDomain);
                            if (templateResource != null) {
                                templateList.add(Util.getEmailTemplateDTO(templateResource));
                            }
                        }
                    }
                }
            }
        } catch (RegistryException | IdentityRuntimeException e) {
            String error = String.format("Error when retrieving email templates of %s tenant.", tenantDomain);
            throw new I18nEmailMgtServerException(error, e);
        }

        return templateList;
    }

    @Override
    public EmailTemplateDTO getEmailTemplate(String templateType, String locale, String tenantDomain) throws
            I18nEmailMgtException {
        return null;
    }

    @Override
    public void addEmailTemplate(EmailTemplateDTO templateDTO, String tenantDomain) throws I18nEmailMgtException {
        ValidationUtil.validateEmailTemplate(templateDTO);

        Resource templateResource = Util.createTemplateResource(templateDTO);
        String templateTypeDisplayName = templateDTO.getDisplayName();
        String templateType = Util.getNormalizedName(templateTypeDisplayName);
        String locale = templateDTO.getLocale();

        String path = TEMPLATE_BASE_PATH + PATH_SEPARATOR + templateType; // template type root directory
        try {
            // check whether a template type root directory exists
            if (!resourceMgtService.isResourceExists(path, tenantDomain)) {
                // we add new template type with relevant properties
                EmailTemplateType emailTemplateType = new EmailTemplateType(templateType, templateTypeDisplayName);
                addEmailTemplateType(emailTemplateType, tenantDomain);
                if (log.isDebugEnabled()) {
                    String msg = "Creating template type %s in %s tenant registry.";
                    log.debug(String.format(msg, templateTypeDisplayName, tenantDomain));
                }
            }

            resourceMgtService.putIdentityResource(templateResource, path, tenantDomain, locale);
        } catch (IdentityRuntimeException ex) {
            String errorMsg = "Error when adding new email template of %s type, %s locale to %s tenant registry.";
            handleServerException(String.format(errorMsg, templateResource, locale, tenantDomain), ex);
        }
    }

    @Override
    public void updateEmailTemplate(EmailTemplateDTO templateDTO, String tenantDomain) {
    }

    @Override
    public void deleteEmailTemplate(String templateTypeName, String localeCode, String tenantDomain) throws
            I18nEmailMgtException {
        // validate the name and locale code. TOD

        if (StringUtils.isBlank(templateTypeName)) {
            throw new I18nEmailMgtClientException("Email displayName cannot be null.");
        }

        if (StringUtils.isBlank(localeCode)) {
            throw new I18nEmailMgtClientException("Email locale cannot be null.");
        }

        String templateType = Util.getNormalizedName(templateTypeName);
        String path = TEMPLATE_BASE_PATH + PATH_SEPARATOR + templateType;

        try {
            resourceMgtService.deleteIdentityResource(path, tenantDomain, localeCode);
        } catch (IdentityRuntimeException ex) {
            String msg = String.format("Error deleting %s:%s template from %s tenant registry.", templateTypeName,
                    localeCode, tenantDomain);
            throw new I18nEmailMgtServerException(msg);
        }
    }


    @Override
    public void addDefaultEmailTemplates(String tenantDomain) throws I18nEmailMgtException {
        // before loading templates we check whether they already exist.
        try {
            if (resourceMgtService.isResourceExists(TEMPLATE_BASE_PATH, tenantDomain)) {
                if (log.isDebugEnabled()) {
                    String msg = "Default email templates already exist in %s tenant domain.";
                    log.debug(String.format(msg, tenantDomain));
                }
                return;
            }
        } catch (IdentityRuntimeException ex) {
            String error = "Error when tried to look for default email templates in %s tenant registry";
            log.error(String.format(error, tenantDomain));
        }

        // load DTOs from the Util class
        List<EmailTemplateDTO> defaultTemplates = Util.getDefaultEmailTemplates();
        // iterate through the list and write to registry!
        for (EmailTemplateDTO emailTemplateDTO : defaultTemplates) {
            addEmailTemplate(emailTemplateDTO, tenantDomain);
            if (log.isDebugEnabled()) {
                String msg = "Default template added to %s tenant registry : \n%s";
                log.debug(String.format(msg, tenantDomain, emailTemplateDTO.toString()));
            }
        }

        if (log.isDebugEnabled()) {
            String msg = "Added %d default email templates to %s tenant registry";
            log.debug(String.format(msg, defaultTemplates.size(), tenantDomain));
        }
    }

    private void handleServerException(String errorMsg, Throwable ex) throws I18nEmailMgtServerException {
        log.error(errorMsg);
        throw new I18nEmailMgtServerException(errorMsg, ex);
    }

    private EmailTemplateType getTemplateType(Resource templateTypeResource) {
        String templateName = templateTypeResource.getProperty(I18nMgtConstants.EMAIL_TEMPLATE_NAME);
        String templateDisplayName = templateTypeResource.getProperty(
                I18nMgtConstants.EMAIL_TEMPLATE_TYPE_DISPLAY_NAME);
        return new EmailTemplateType(templateName, templateDisplayName);
    }

    private Collection createTemplateType(String normalizedTemplateName, String templateDisplayName) {
        Collection collection = new CollectionImpl();
        collection.addProperty(I18nMgtConstants.EMAIL_TEMPLATE_NAME, normalizedTemplateName);
        collection.addProperty(I18nMgtConstants.EMAIL_TEMPLATE_TYPE_DISPLAY_NAME, templateDisplayName);
        return collection;
    }


}
