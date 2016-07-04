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

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.email.mgt.constants.I18nMgtConstants;
import org.wso2.carbon.email.mgt.exceptions.I18nMgtEmailConfigException;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * This Singleton is used to load and save tenant specific email configurations.
 */
public class ConfigBuilder {

    private static final Log log = LogFactory.getLog(ConfigBuilder.class);
    private static ConfigBuilder instance = null;
    private ConfigManager cm = null;

    private ConfigBuilder() {
        cm = new ConfigManagerImpl();
    }

    public static ConfigBuilder getInstance() {
        if (instance == null) {
            instance = new ConfigBuilder();
        }

        return instance;
    }

    /**
     * Loads tenant specific email configuration.
     *
     * @param configType - Configuration type.
     * @param stype      - Persistence storage type.
     * @param tenantId   - Tenant Id which the email template configuration belongs.
     * @return The populated email template configuration object.
     * @throws org.wso2.carbon.email.mgt.exceptions.I18nMgtEmailConfigException
     */
    public Config loadConfiguration(ConfigType configType, StorageType stype, int tenantId) throws
            I18nMgtEmailConfigException {
        Config config = null;
        switch (stype) {
            case REGISTRY:
                cm.setReader(new RegistryConfigReader());
                switch (configType) {
                    case EMAIL:
                        cm.setConfig(new EmailNotificationConfig());
                        cm.setResourcePath(I18nMgtConstants.EMAIL_TEMPLATE_PATH);
                        config = cm.loadConfig(tenantId);
                        if (config == null) {
                            throw new I18nMgtEmailConfigException("Default email templates haven't written successfully");
                        }
                        break;
                    default:
                        throw new I18nMgtEmailConfigException("Configuration type not supported");
                }
                break;
            case DB:
            case FILE:
                break;
            default:
                throw new I18nMgtEmailConfigException("Configuration storage type not supported");
        }

        return config;
    }

    /**
     * Loads the tenant specific configuration in server startup.
     *
     * @param configType - Configuration type.
     * @param stype      - Persistence storage type.
     * @param tenantId   - Tenant Id which the email template configuration belongs.
     * @throws org.wso2.carbon.email.mgt.exceptions.I18nMgtEmailConfigException
     */
    public void loadDefaultConfiguration(ConfigType configType, StorageType stype, int tenantId) throws
            I18nMgtEmailConfigException {
        switch (stype) {
            case REGISTRY:
                cm.setReader(new RegistryConfigReader());
                cm.setWriter(new RegistryConfigWriter());
                switch (configType) {
                    case EMAIL:
                        cm.setConfig(new EmailNotificationConfig());
                        cm.setResourcePath(I18nMgtConstants.EMAIL_TEMPLATE_PATH);
                        Config config = cm.loadConfig(tenantId);
                        if (config == null) {
                            config = loadEmailConfigFile();
                            cm.saveConfig(config, tenantId);
                        }
                        break;
                    default:
                        throw new I18nMgtEmailConfigException("Configuration type not supported");
                }
                break;
            case DB:
            case FILE:
                break;
            default:
                throw new I18nMgtEmailConfigException("Configuration storage type not supported");
        }
    }

    /**
     * Save the tenant specific email template configurations.
     *
     * @param stype    - Persistence storage type.
     * @param tenantId - Tenant Id which the email template configuration belongs.
     * @param config   - Configuration type.
     * @throws org.wso2.carbon.email.mgt.exceptions.I18nMgtEmailConfigException
     */
    public void saveConfiguration(StorageType stype, int tenantId, Config config) throws I18nMgtEmailConfigException {
        switch (stype) {
            case REGISTRY:
                cm.setWriter(new RegistryConfigWriter());
                if (config instanceof EmailNotificationConfig) {
                    cm.setConfig(new EmailNotificationConfig());
                    cm.setResourcePath(I18nMgtConstants.EMAIL_TEMPLATE_PATH);
                    cm.saveConfig(config, tenantId);
                } else {
                    throw new I18nMgtEmailConfigException("Configuration type not supported");
                }
                break;
            case DB:
            case FILE:
                break;
            default:
                throw new I18nMgtEmailConfigException("Configuration storage type not supported");
        }
    }

    /**
     * Add tenant specific new email template configurations.
     *
     * @param stype    - Persistence storage type.
     * @param tenantId - Tenant Id which the email template configuration belongs.
     * @param config   - Configuration type.
     * @throws org.wso2.carbon.email.mgt.exceptions.I18nMgtEmailConfigException
     */
    public void addEmailConfiguration(StorageType stype, int tenantId, Config config)
            throws I18nMgtEmailConfigException {
        switch (stype) {
            case REGISTRY:
                cm.setWriter(new RegistryConfigAdd());
                if (config instanceof EmailNotificationConfig) {
                    cm.setConfig(new EmailNotificationConfig());
                    cm.setResourcePath(I18nMgtConstants.EMAIL_TEMPLATE_PATH);
                    cm.addEmailConfig(config, tenantId);
                } else {
                    throw new I18nMgtEmailConfigException("Configuration type not supported");
                }
                break;
            case DB:
            case FILE:
                break;
            default:
                throw new I18nMgtEmailConfigException("Configuration storage type not supported");
        }
    }


    private Config loadEmailConfigFile() {
        String confXml = CarbonUtils.getCarbonConfigDirPath() + File.separator
                + I18nMgtConstants.EMAIL_CONF_DIRECTORY + File.separator
                + I18nMgtConstants.EMAIL_ADMIN_CONF_FILE;

        Config emailConfig = new EmailNotificationConfig();

        File configfile = new File(confXml);
        if (!configfile.exists()) {
            log.warn("Email Configuration File is not present at: " + confXml);
        }

        XMLStreamReader parser = null;
        InputStream stream = null;

        try {
            stream = new FileInputStream(configfile);
            parser = XMLInputFactory.newInstance()
                    .createXMLStreamReader(stream);
            StAXOMBuilder builder = new StAXOMBuilder(parser);
            OMElement documentElement = builder.getDocumentElement();
            Iterator iterator = documentElement.getChildElements();
            while (iterator.hasNext()) {
                OMElement omElement = (OMElement) iterator.next();
                String configType1 = omElement.getAttributeValue(new QName(
                        "type"));
                String configType2 = omElement.getAttributeValue(new QName(
                        "display"));
                String configType3 = omElement.getAttributeValue(new QName(
                        "locale"));
                String configType4 = omElement.getAttributeValue(new QName(
                        "emailContentType"));
                String configFinal = configType1 + "|" + configType2 + "|" + configType3 + "|" + configType4;
                if (StringUtils.isNotBlank(configFinal)) {
                    emailConfig.setProperty(configFinal, loadEmailConfig(omElement));
                }

            }
        } catch (XMLStreamException | FileNotFoundException e) {
            log.warn("Error while loading email config. using default configuration", e);
        } finally {
            try {
                if (parser != null) {
                    parser.close();
                }
                if (stream != null) {
                    stream.close();
                }
            } catch (XMLStreamException e) {
                log.error("Error while closing XML stream", e);
            } catch (IOException e) {
                log.error("Error while closing input stream", e);
            }
        }
        return emailConfig;

    }

    private String loadEmailConfig(OMElement configElement) {
        StringBuilder emailTemplate = new StringBuilder();
        Iterator it = configElement.getChildElements();
        while (it.hasNext()) {
            OMElement element = (OMElement) it.next();
            if ("subject".equals(element.getLocalName())) {
                emailTemplate.append(element.getText());
            } else if ("body".equals(element.getLocalName())) {
                emailTemplate.append("|");
                emailTemplate.append(element.getText());
            } else if ("footer".equals(element.getLocalName())) {
                emailTemplate.append("|");
                emailTemplate.append(element.getText());
            }
        }
        return emailTemplate.toString();

    }
}
