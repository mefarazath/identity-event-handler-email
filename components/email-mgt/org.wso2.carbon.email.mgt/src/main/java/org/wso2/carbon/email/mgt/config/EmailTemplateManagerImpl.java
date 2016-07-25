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
import org.wso2.carbon.email.mgt.exceptions.I18nEmailMgtServerException;
import org.wso2.carbon.email.mgt.internal.I18nMgtDataHolder;
import org.wso2.carbon.email.mgt.model.EmailTemplate;
import org.wso2.carbon.email.mgt.util.I18nEmailUtil;
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
    public void addEmailTemplateType(String emailTemplateDisplayName, String tenantDomain) throws
            I18nEmailMgtException {

        validateEmailTemplateType(emailTemplateDisplayName);
        // get template directory name from display name.
        String normalizedTemplateName = I18nEmailUtil.getNormalizedName(emailTemplateDisplayName);

        // persist the template type to registry ie. create a directory.
        String path = TEMPLATE_BASE_PATH + PATH_SEPARATOR + normalizedTemplateName;
        try {
            // check whether a template exists with the same name.
            if (resourceMgtService.isResourceExists(path, tenantDomain)) {
                String errorMsg = String.format(I18nMgtConstants.ErrorMsg.DUPLICATE_TEMPLATE_TYPE,
                        emailTemplateDisplayName, tenantDomain);
                throw new I18nEmailMgtClientException(errorMsg);
            }

            Collection collection = createTemplateType(normalizedTemplateName, emailTemplateDisplayName);
            resourceMgtService.putIdentityResource(collection, path, tenantDomain);
        } catch (IdentityRuntimeException ex) {
            String errorMsg = String.format("Error adding template type %s to %s tenant.", emailTemplateDisplayName,
                    tenantDomain);
            log.error(errorMsg);
            throw new I18nEmailMgtServerException(errorMsg, ex);
        }
    }

    @Override
    public void deleteEmailTemplateType(String emailTemplateDisplayName, String tenantDomain) throws
            I18nEmailMgtException {

        validateEmailTemplateType(emailTemplateDisplayName);

        String templateType = I18nEmailUtil.getNormalizedName(emailTemplateDisplayName);
        String path = TEMPLATE_BASE_PATH + PATH_SEPARATOR + templateType;

        try {
            resourceMgtService.deleteIdentityResource(path, tenantDomain);
        } catch (IdentityRuntimeException ex) {
            String errorMsg = String.format
                    ("Error deleting email template type %s from %s tenant.", emailTemplateDisplayName, tenantDomain);
            handleServerException(errorMsg, ex);
        }
    }

    /**
     * @param tenantDomain
     * @return
     * @throws I18nEmailMgtServerException
     */
    @Override
    public List<String> getAvailableTemplateTypes(String tenantDomain) throws I18nEmailMgtServerException {
        try {
            List<String> templateTypeList = new ArrayList<>();
            Collection collection = (Collection) resourceMgtService.getIdentityResource(TEMPLATE_BASE_PATH,
                    tenantDomain);

            for (String templatePath : collection.getChildren()) {
                Resource templateTypeResource = resourceMgtService.getIdentityResource(templatePath, tenantDomain);
                if (templateTypeResource != null) {
                    String emailTemplateType = getTemplateDisplayName(templateTypeResource);
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
    public List<EmailTemplate> getAllEmailTemplates(String tenantDomain) throws I18nEmailMgtException {
        List<EmailTemplate> templateList = new ArrayList<>();

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
                                // TODO  check here!
                                try {
                                    EmailTemplate templateDTO = I18nEmailUtil.getEmailTemplateDTO(templateResource);
                                    templateList.add(templateDTO);
                                } catch (I18nEmailMgtException ex) {
                                    log.error(ex.getMessage(), ex);
                                }
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
    public EmailTemplate getEmailTemplate(String emailTemplateDisplayName, String locale, String tenantDomain) throws
            I18nEmailMgtException {

        EmailTemplate emailTemplate = null;

        validateEmailTemplateType(emailTemplateDisplayName);
        validateLocale(locale);

        String templateDirectory = I18nEmailUtil.getNormalizedName(emailTemplateDisplayName);
        String path = TEMPLATE_BASE_PATH + PATH_SEPARATOR + templateDirectory;

        try {
            Resource emailResource = resourceMgtService.getIdentityResource(path, tenantDomain, locale);
            if (emailResource != null) {
                emailTemplate = I18nEmailUtil.getEmailTemplateDTO(emailResource);
            }
        } catch (IdentityRuntimeException ex) {
            String error = "Error when retrieving '%s:%s' template from %s tenant registry.";
            handleServerException(String.format(emailTemplateDisplayName, locale, tenantDomain), ex);
        }

        return null;
    }

    @Override
    public void addEmailTemplate(EmailTemplate emailTemplate, String tenantDomain) throws I18nEmailMgtException {

        // validate the email template object before processing it.
        validateEmailTemplate(emailTemplate);

        Resource templateResource = I18nEmailUtil.createTemplateResource(emailTemplate);
        String templateTypeDisplayName = emailTemplate.getTemplateDisplayName();
        String templateType = I18nEmailUtil.getNormalizedName(templateTypeDisplayName);
        String locale = emailTemplate.getLocale();

        String path = TEMPLATE_BASE_PATH + PATH_SEPARATOR + templateType; // template type root directory
        try {
            // check whether a template type root directory exists
            if (!resourceMgtService.isResourceExists(path, tenantDomain)) {
                // we add new template type with relevant properties
                addEmailTemplateType(templateTypeDisplayName, tenantDomain);
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
    public void deleteEmailTemplate(String templateTypeName, String localeCode, String tenantDomain) throws
            I18nEmailMgtException {
        // validate the name and locale code.
        if (StringUtils.isBlank(templateTypeName)) {
            throw new I18nEmailMgtClientException("Cannot Delete template. Email displayName cannot be null.");
        }

        if (StringUtils.isBlank(localeCode)) {
            throw new I18nEmailMgtClientException("Cannot Delete template. Email locale cannot be null.");
        }

        String templateType = I18nEmailUtil.getNormalizedName(templateTypeName);
        String path = TEMPLATE_BASE_PATH + PATH_SEPARATOR + templateType;

        try {
            resourceMgtService.deleteIdentityResource(path, tenantDomain, localeCode);
        } catch (IdentityRuntimeException ex) {
            String msg = String.format("Error deleting %s:%s template from %s tenant registry.", templateTypeName,
                    localeCode, tenantDomain);
            handleServerException(msg, ex);
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
            String error = "Error when tried to check for default email templates in %s tenant registry";
            log.error(String.format(error, tenantDomain), ex);
        }

        // load DTOs from the I18nEmailUtil class
        List<EmailTemplate> defaultTemplates = I18nEmailUtil.getDefaultEmailTemplates();
        // iterate through the list and write to registry!
        for (EmailTemplate emailTemplateDTO : defaultTemplates) {
            addEmailTemplate(emailTemplateDTO, tenantDomain);
            if (log.isDebugEnabled()) {
                String msg = "Default template added to %s tenant registry : %n%s";
                log.debug(String.format(msg, tenantDomain, emailTemplateDTO.toString()));
            }
        }

        if (log.isDebugEnabled()) {
            String msg = "Added %d default email templates to %s tenant registry";
            log.debug(String.format(msg, defaultTemplates.size(), tenantDomain));
        }
    }


    /**
     * Validate an EmailTemplate object before persisting it into tenant's registry.
     *
     * @param emailTemplate
     */
    private void validateEmailTemplate(EmailTemplate emailTemplate) throws I18nEmailMgtClientException {
        if (emailTemplate == null) {
            throw new I18nEmailMgtClientException("Email Template object cannot be null.");
        }

        // TODO
        // validate email template meta-data
        // displayName

        // templateTypeName --> if not equal normalized displayname set it to normalized display name.
        // locale
        // template type
        //


        // validate subject
        // validate body
        // validate footer

    }

    /**
     * Validate the displayName of a email template type.
     *
     * @param templateDisplayName
     * @throws I18nEmailMgtClientException
     */
    private void validateEmailTemplateType(String templateDisplayName) throws I18nEmailMgtClientException {
        // check for null or empty
        if (StringUtils.isBlank(templateDisplayName)) {
            throw new I18nEmailMgtClientException("Email Template Type displayname cannot be null.");
        }

        // check whether display name of the template satisfy our regex ie. Only alphanumerics and spaces.
        // TODO regex whitelist: alphanumeric + space / blacklist: registry invalid chars.
    }


    private void validateLocale(String localeCode) throws I18nEmailMgtClientException {
        if (StringUtils.isBlank(localeCode)) {
            throw new I18nEmailMgtClientException("Locale code cannot be empty or null");
        }
        // regex check for registry invalid chars.
    }

    private void handleServerException(String errorMsg, Throwable ex) throws I18nEmailMgtServerException {
        log.error(errorMsg);
        throw new I18nEmailMgtServerException(errorMsg, ex);
    }

    private String getTemplateDisplayName(Resource templateTypeResource) {
        String templateDisplayName = templateTypeResource.getProperty(I18nMgtConstants.EMAIL_TEMPLATE_TYPE_DISPLAY_NAME);
        return templateDisplayName;
    }

    private Collection createTemplateType(String normalizedTemplateName, String templateDisplayName) {
        Collection collection = new CollectionImpl();
        collection.addProperty(I18nMgtConstants.EMAIL_TEMPLATE_NAME, normalizedTemplateName);
        collection.addProperty(I18nMgtConstants.EMAIL_TEMPLATE_TYPE_DISPLAY_NAME, templateDisplayName);
        return collection;
    }


}
