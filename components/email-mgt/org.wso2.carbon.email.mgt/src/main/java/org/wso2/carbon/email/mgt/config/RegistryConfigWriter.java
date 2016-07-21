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

package org.wso2.carbon.email.mgt.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.email.mgt.constants.I18nMgtConstants;
import org.wso2.carbon.email.mgt.exceptions.I18nMgtEmailConfigException;
import org.wso2.carbon.email.mgt.internal.I18nMgtDataHolder;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.session.UserRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

public class RegistryConfigWriter implements ConfigWriter {

    private static Log log = LogFactory.getLog(RegistryConfigWriter.class);

	/**
	 * This method is used to add a new Email template in a specific tenant space.
	 *
	 * @param tenantId     - The tenant Id of the tenant that specific email template needs to be add.
	 * @param props        - Property configurations of the specific email template.
	 * @param resourcePath - Path to be add the specific email template.
	 * @throws org.wso2.carbon.email.mgt.exceptions.I18nMgtEmailConfigException
	 */
	@Override
	public void write(int tenantId, Properties props, String resourcePath) throws I18nMgtEmailConfigException {

		if (log.isDebugEnabled()) {
			log.debug("Saving email template at registry path : " + resourcePath);
		}

		RegistryService registry = I18nMgtDataHolder.getInstance().getRegistryService();
		try {
			UserRegistry userReg = registry.getConfigSystemRegistry(tenantId);
			Set<String> names = props.stringPropertyNames();

			if (!userReg.resourceExists(resourcePath)) {
				for (String propsKeyName : names) {
					// This is done due to casting to List in JDBCRegistryDao when adding a registry property.
					List<String> value = new ArrayList<String>();
					String[] propertyArray = propsKeyName.split("\\|");
					value.add(propertyArray[1]);
					Collection emailCollection = userReg.newCollection();

                    String emailTemplateDisplayName = propertyArray[0];
                    String emailTemplateFolderName = propertyArray[0].replaceAll("\\s", "").toLowerCase();
					String defaultEmailLocale = propertyArray[2];

					emailCollection.setProperty(emailTemplateFolderName, value);
                    emailCollection.setProperty(I18nMgtConstants.EMAIL_TEMPLATE_NAME, emailTemplateFolderName);
                    emailCollection.setProperty(I18nMgtConstants.EMAIL_TEMPLATE_TYPE_DISPLAY_NAME, emailTemplateDisplayName);


                    String emailTemplateFilename = emailTemplateFolderName + I18nMgtConstants.EMAIL_LOCALE_SEPARATOR +
							defaultEmailLocale;
					String emailTemplateFolderResourcePath = resourcePath +
							I18nMgtConstants.EMAIL_FOLDER_SEPARATOR + emailTemplateFolderName;
					String emailTemplateFileResourcePath = resourcePath + I18nMgtConstants.EMAIL_FOLDER_SEPARATOR
							+ emailTemplateFolderName + I18nMgtConstants.EMAIL_FOLDER_SEPARATOR + emailTemplateFilename;

					userReg.put(emailTemplateFolderResourcePath, emailCollection);
					Resource resource = userReg.newResource();
					String emailTemplateContent = props.getProperty(propsKeyName);
					resource.setMediaType(propertyArray[3]);
					resource.setContent(emailTemplateContent);
					userReg.put(emailTemplateFileResourcePath, resource);
				}

				if (log.isDebugEnabled()) {
					log.debug("Default email template added to : " + resourcePath + "successfully.");
				}

			} else {
				String[] propsKeyName = names.toArray(new String[names.size()]);
				String[] propsKeyNameSplit = propsKeyName[0].split("\\.");
				String emailTemplateFolderName = propsKeyNameSplit[0];
				String emailTemplateFilename = emailTemplateFolderName +
						I18nMgtConstants.EMAIL_LOCALE_SEPARATOR + propsKeyNameSplit[1];
				String emailTemplateFileResourcePath = resourcePath + I18nMgtConstants.EMAIL_FOLDER_SEPARATOR +
						emailTemplateFolderName + I18nMgtConstants.EMAIL_FOLDER_SEPARATOR + emailTemplateFilename;

				Resource resource = userReg.newResource();
				String emailTemplateContent = props.getProperty(propsKeyName[0]);
				resource.setMediaType(propsKeyNameSplit[2]);
				resource.setContent(emailTemplateContent);
				userReg.put(emailTemplateFileResourcePath, resource);

				if (log.isDebugEnabled()) {
					log.debug("Email template : " + emailTemplateFilename +
							" saved successfully to path " + emailTemplateFileResourcePath);
				}

			}
		} catch (RegistryException e) {
			throw new I18nMgtEmailConfigException("Error occurred while adding email template to registry path : "
					+ resourcePath, e);
		}

	}

}
