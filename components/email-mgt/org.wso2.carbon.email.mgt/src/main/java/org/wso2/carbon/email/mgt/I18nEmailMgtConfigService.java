/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.email.mgt;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.email.mgt.config.ConfigBuilder;
import org.wso2.carbon.email.mgt.config.EmailConfigTransformer;
import org.wso2.carbon.email.mgt.config.EmailNotificationConfig;
import org.wso2.carbon.email.mgt.config.EmailTemplateManagerImpl;
import org.wso2.carbon.email.mgt.config.StorageType;
import org.wso2.carbon.email.mgt.dto.EmailTemplateDTO;
import org.wso2.carbon.email.mgt.exceptions.I18nEmailMgtException;
import org.wso2.carbon.email.mgt.exceptions.I18nEmailMgtServerException;
import org.wso2.carbon.email.mgt.exceptions.I18nMgtEmailConfigException;
import org.wso2.carbon.email.mgt.model.EmailTemplateType;
import org.wso2.carbon.identity.base.IdentityException;

import java.util.List;
import java.util.Properties;

/**
 * This service provides functionality for managing internationalized email templates used for notifications across the
 * Identity components.
 */
public class I18nEmailMgtConfigService {

    private static final Log log = LogFactory.getLog(I18nEmailMgtConfigService.class);
    private EmailTemplateManagerImpl templateManager = new EmailTemplateManagerImpl();

    /**
     * @param emailTemplateType
     * @throws I18nEmailMgtServerException
     */
    public void addEmailTemplateType(EmailTemplateType emailTemplateType) throws I18nEmailMgtServerException {
        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        try {
            templateManager.addEmailTemplateType(emailTemplateType, tenantDomain);
        } catch (I18nEmailMgtException e) {
            String errorMsg = String.format("Error while adding email template type to %s tenant.", tenantDomain);
            log.error(errorMsg, e);
            throw new I18nEmailMgtServerException(errorMsg, e);
        }
    }

    /**
     * @return
     * @throws I18nEmailMgtServerException
     */
    public EmailTemplateType[] getEmailTemplateTypes() throws I18nEmailMgtServerException {
        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        try {
            List<EmailTemplateType> emailTemplateTypes = templateManager.getAvailableTemplateTypes(tenantDomain);
            return emailTemplateTypes.toArray(new EmailTemplateType[emailTemplateTypes.size()]);
        } catch (I18nEmailMgtException e) {
            String errorMsg = String.format("Error while retrieving email template types of %s tenant.", tenantDomain);
            log.error(errorMsg, e);
            throw new I18nEmailMgtServerException(errorMsg, e);
        }
    }

    /**
     * This method is used to save the email template specific to a tenant.
     *
     * @param emailTemplate - Email templates to be saved.
     * @throws I18nEmailMgtServerException
     */
    public void saveEmailConfig(EmailTemplateDTO emailTemplate) throws I18nEmailMgtServerException {

        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        EmailNotificationConfig emailConfig = new EmailNotificationConfig();
        ConfigBuilder configBuilder = ConfigBuilder.getInstance();
        try {
            Properties props = EmailConfigTransformer.transform(emailTemplate);
            emailConfig.setProperties(props);
            configBuilder.saveConfiguration(StorageType.REGISTRY, tenantId, emailConfig);
        } catch (IdentityException e) {
            log.error("Error occurred while transforming to Email Template Object ", e);
            throw new I18nEmailMgtServerException("Error occurred while saving email template configurations", e);
        } catch (I18nMgtEmailConfigException e) {
            log.error("Error occurred while saving email template configuration", e);
            throw new I18nEmailMgtServerException("Error occurred while writing email template configurations to " +
                    "registry path", e);
        }
    }

    /**
     * This method is used to add an email template specific to a tenant.
     *
     * @param emailTemplate - Email templates to be saved.
     * @throws I18nEmailMgtServerException
     */
    public void addEmailConfig(EmailTemplateDTO emailTemplate) throws I18nEmailMgtServerException {

        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        try {
            templateManager.addEmailTemplate(emailTemplate, tenantDomain);
        } catch (I18nEmailMgtException e) {
            log.error("Error occurred while adding email template to " + tenantDomain + " tenant registry", e);
            throw new I18nEmailMgtServerException("Error occurred adding an email template", e);
        }
    }

    /**
     * This method is used to load the email template specific to a tenant.
     *
     * @return an Array of templates.
     * @throws I18nEmailMgtServerException
     */
    public EmailTemplateDTO[] getEmailConfig() throws I18nEmailMgtServerException {

        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        EmailTemplateDTO[] templates = null;
        try {
            List<EmailTemplateDTO> templateDTOs = templateManager.getAllEmailTemplates(tenantDomain);
            templates = templateDTOs.toArray(new EmailTemplateDTO[templateDTOs.size()]);
        } catch (I18nEmailMgtException e) {
            String errorMsg = "Error occurred while retrieving email templates of " + tenantDomain + " tenant.";
            log.error(errorMsg, e);
            throw new I18nEmailMgtServerException(errorMsg, e);
        }

        return templates;
    }


    public void deleteEmailTemplateType(String emailTemplateType) {

    }


    public void deleteEmailTemplate(String templateTypeType, String locale) {

    }
}
