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
import org.wso2.carbon.email.mgt.config.Config;
import org.wso2.carbon.email.mgt.config.ConfigBuilder;
import org.wso2.carbon.email.mgt.config.ConfigType;
import org.wso2.carbon.email.mgt.config.EmailConfigTransformer;
import org.wso2.carbon.email.mgt.config.EmailNotificationConfig;
import org.wso2.carbon.email.mgt.config.EmailTemplateManager;
import org.wso2.carbon.email.mgt.config.StorageType;
import org.wso2.carbon.email.mgt.dto.EmailTemplateDTO;
import org.wso2.carbon.email.mgt.exceptions.I18nEmailMgtException;
import org.wso2.carbon.email.mgt.exceptions.I18nEmailMgtServiceException;
import org.wso2.carbon.email.mgt.exceptions.I18nMgtEmailConfigException;
import org.wso2.carbon.email.mgt.model.EmailTemplateType;
import org.wso2.carbon.identity.base.IdentityException;

import java.util.List;
import java.util.Properties;

/**
 * This service is to configure the Internationalization Management
 * functionality.
 */
public class I18nEmailMgtConfigService {

    Log log = LogFactory.getLog(I18nEmailMgtConfigService.class);
    private EmailTemplateManager templateManager = new EmailTemplateManager();

    public void addEmailTemplateType(EmailTemplateType emailTemplateType) throws I18nEmailMgtServiceException {
        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        try {
            templateManager.addTemplateType(emailTemplateType, tenantDomain);
        } catch (I18nEmailMgtException e) {
            String errorMsg = String.format("Error while adding email template type to %s tenant.", tenantDomain);
            log.error(errorMsg, e);
            throw new I18nEmailMgtServiceException(errorMsg, e);
        }
    }

    public EmailTemplateType[] getEmailTemplateTypes() throws I18nEmailMgtServiceException {
        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        try {
            List<EmailTemplateType> emailTemplateTypes = templateManager.getAvailableTemplateTypes(tenantDomain);
            return emailTemplateTypes.toArray(new EmailTemplateType[emailTemplateTypes.size()]);
        } catch (I18nEmailMgtException e) {
            String errorMsg = String.format("Error while retrieving email template types of %s tenant.", tenantDomain);
            log.error(errorMsg, e);
            throw new I18nEmailMgtServiceException(errorMsg, e);
        }
    }

    /**
     * This method is used to save the email template specific to a tenant.
     *
     * @param emailTemplate - Email templates to be saved.
     * @throws I18nEmailMgtServiceException
     */
    public void saveEmailConfig(EmailTemplateDTO emailTemplate)
            throws I18nEmailMgtServiceException {

        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        EmailNotificationConfig emailConfig = new EmailNotificationConfig();
        ConfigBuilder configBuilder = ConfigBuilder.getInstance();
        try {
            Properties props = EmailConfigTransformer.transform(emailTemplate);
            emailConfig.setProperties(props);

            configBuilder.saveConfiguration(StorageType.REGISTRY, tenantId,
                    emailConfig);
        } catch (IdentityException e) {
            log.error("Error occurred while transforming to Email Template Object ", e);
            throw new I18nEmailMgtServiceException("Error occurred while saving email template configurations", e);
        } catch (I18nMgtEmailConfigException e) {
            log.error("Error occurred while saving email template configuration", e);
            throw new I18nEmailMgtServiceException("Error occurred while writing email template configurations to " +
                    "registry path", e);
        }
    }

    /**
     * This method is used to add an email template specific to a tenant.
     *
     * @param emailTemplate - Email templates to be saved.
     * @throws I18nEmailMgtServiceException
     */
    public void addEmailConfig(EmailTemplateDTO emailTemplate) throws I18nEmailMgtServiceException {

        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        EmailNotificationConfig emailConfig = new EmailNotificationConfig();
        ConfigBuilder configBuilder = ConfigBuilder.getInstance();
        try {
            Properties props = EmailConfigTransformer.transform(emailTemplate);
            emailConfig.setProperties(props);

            configBuilder.addEmailConfiguration(StorageType.REGISTRY, tenantId,
                    emailConfig);

        } catch (IdentityException e) {
            log.error("Error occurred while transforming to Email Template Object ", e);
            throw new I18nEmailMgtServiceException("Error occurred adding an email template", e);
        } catch (I18nMgtEmailConfigException e) {
            log.error("Error occurred while adding email template configuration to registry path", e);
            throw new I18nEmailMgtServiceException("Error occurred adding an email template", e);
        }
    }

    /**
     * This method is used to load the email template specific to a tenant.
     *
     * @return an Array of templates.
     * @throws I18nEmailMgtServiceException
     */
    public EmailTemplateDTO[] getEmailConfig() throws I18nEmailMgtServiceException {

        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        Config emailConfig = null;
        EmailTemplateDTO[] templates = null;
        ConfigBuilder configBuilder = ConfigBuilder.getInstance();

        try {
            emailConfig = configBuilder.loadConfiguration(ConfigType.EMAIL,
                    StorageType.REGISTRY, tenantId);
            if (emailConfig != null) {
                templates = EmailConfigTransformer.transform(emailConfig.getProperties());
            }
        } catch (I18nMgtEmailConfigException e) {
            log.error("Error occurred while transforming to email template object ", e);
            throw new I18nEmailMgtServiceException("Error occurred while reading email templates", e);
        }

        return templates;
    }
}
