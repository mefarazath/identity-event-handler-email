<!--
~ Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
~
~ WSO2 Inc. licenses this file to you under the Apache License,
~ Version 2.0 (the "License"); you may not use this file except
~ in compliance with the License.
~ You may obtain a copy of the License at
~
~ http://www.apache.org/licenses/LICENSE-2.0
~
~ Unless required by applicable law or agreed to in writing,
~ software distributed under the License is distributed on an
~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
~ KIND, either express or implied. See the License for the
~ specific language governing permissions and limitations
~ under the License.
-->
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@page import="org.apache.axis2.context.ConfigurationContext" %>
<%@page import="org.apache.commons.lang.StringUtils" %>
<%@page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.email.mgt.model.xsd.EmailTemplate" %>
<%@ page import="org.wso2.carbon.email.mgt.ui.I18nEmailMgtConfigServiceClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@page import="org.wso2.carbon.utils.ServerConstants" %>


<%
    String httpMethod = request.getMethod();
    if (!"post".equalsIgnoreCase(httpMethod)) {
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        return;
    }

    request.setCharacterEncoding("UTF-8");
    String templateDisplayName = request.getParameter("emailTypes");
    String emailContentType = request.getParameter("emailContentType");
    String emailLocaleCode = request.getParameter("emailLanguage");

    request.setCharacterEncoding("UTF-8");
    String emailSubject = request.getParameter("emailSubject");
    String emailBody = request.getParameter("emailBody");
    String emailFooter = request.getParameter("emailFooter");

    System.out.println(emailSubject);

    // params to handle deleting templates
    boolean deleteTemplate = false;
    String delete = request.getParameter("delete");
    if (StringUtils.isNotBlank(delete)) {
        deleteTemplate = Boolean.parseBoolean(delete);
    }

    String templateTypeToDelete = request.getParameter("templateName");
    String locale = request.getParameter("locale");

    EmailTemplate templateChanged = new EmailTemplate();
    if (!deleteTemplate) {
        if (StringUtils.isNotBlank(templateDisplayName)) {
            templateChanged.setTemplateDisplayName(templateDisplayName);
        }
        if (StringUtils.isNotBlank(emailLocaleCode)) {
            templateChanged.setLocale(emailLocaleCode);
        }
        if (StringUtils.isNotBlank(emailSubject)) {
            templateChanged.setSubject(emailSubject);
        }
        if (StringUtils.isNotBlank(emailBody)) {
            templateChanged.setBody(emailBody);
        }
        if (StringUtils.isNotBlank(emailFooter)) {
            templateChanged.setFooter(emailFooter);
        }
        if (StringUtils.isNotBlank(emailContentType)) {
            templateChanged.setEmailContentType(emailContentType);
        }
    }
    try {
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext = (ConfigurationContext) config
                .getServletContext()
                .getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);

        I18nEmailMgtConfigServiceClient configClient =
                new I18nEmailMgtConfigServiceClient(cookie, backendServerURL, configContext);

        if (deleteTemplate) {
            if (StringUtils.equalsIgnoreCase(locale, "ALL")) {
                configClient.deleteEmailTemplateType(templateTypeToDelete);
                CarbonUIMessage.sendCarbonUIMessage(templateTypeToDelete + " email template type successfully deleted.",
                        CarbonUIMessage.INFO, request);
            } else {
                configClient.deleteEmailTemplate(templateTypeToDelete, locale);
                CarbonUIMessage.sendCarbonUIMessage(templateTypeToDelete + ":" + locale +
                        " email template successfully deleted.", CarbonUIMessage.INFO, request);
            }
        } else {
            configClient.saveEmailConfig(templateChanged);
            CarbonUIMessage.sendCarbonUIMessage("Email Template successfully Saved.", CarbonUIMessage.INFO, request);
        }
%>
<script type="text/javascript">
    location.href = "email-template-config.jsp";
</script>
<%
} catch (Exception e) {
    CarbonUIMessage.sendCarbonUIMessage(e.getMessage(), CarbonUIMessage.ERROR, request);
%>
<script type="text/javascript">
    location.href = "email-template-config.jsp";
</script>
<%
        return;
    }
%>


