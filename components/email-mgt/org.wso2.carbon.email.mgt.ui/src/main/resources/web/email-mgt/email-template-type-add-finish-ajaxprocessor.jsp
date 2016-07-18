<%--
  ~ Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
  ~
  ~ Licensed under the Apache License, Version 2.0 (the "License");
  ~ you may not use this file except in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~ http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License.
  --%>

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

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@page import="org.apache.axis2.context.ConfigurationContext" %>
<jsp:include page="../dialog/display_messages.jsp"/>
<%@page import="org.apache.commons.lang.StringUtils" %>
<%@page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.email.mgt.model.xsd.EmailTemplateType" %>
<%@ page import="org.wso2.carbon.email.mgt.ui.I18nEmailMgtConfigServiceClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.owasp.encoder.Encode" %>
<script type="text/javascript" src="extensions/js/vui.js"></script>
<script type="text/javascript" src="../admin/js/main.js"></script>


<%
    String httpMethod = request.getMethod();
    if (!"post".equalsIgnoreCase(httpMethod)) {
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        return;
    }

    String templateTypeName = request.getParameter("templateTypeName");
    String templateTypeDisplayName = request.getParameter("displayName");

    EmailTemplateType emailTemplateType = null;
    if (StringUtils.isNotBlank(templateTypeName) && StringUtils.isNotBlank(templateTypeDisplayName)) {
        emailTemplateType = new EmailTemplateType();
        emailTemplateType.setName(templateTypeName);
        emailTemplateType.setDisplayName(templateTypeDisplayName);
    }

    try {
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext = (ConfigurationContext) config
                .getServletContext()
                .getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);

        I18nEmailMgtConfigServiceClient emailMgtServiceClient =
                new I18nEmailMgtConfigServiceClient(cookie, backendServerURL, configContext);

        emailMgtServiceClient.addTemplateType(emailTemplateType);
        CarbonUIMessage.sendCarbonUIMessage("Email Template Type successfully Added", CarbonUIMessage.INFO, request);

%>

<script type="text/javascript">
    location.href = "email-template-add.jsp?templateType=" + "<%=Encode.forJavaScriptBlock(templateTypeDisplayName)%>";
</script>
<%
} catch (Exception e) {
    CarbonUIMessage.sendCarbonUIMessage("Error while trying to add a template type : " + e.getMessage(), CarbonUIMessage.ERROR, request);
%>
<script type="text/javascript">
    location.href = "email-template-add.jsp";
</script>
<%
        return;
    }
%>