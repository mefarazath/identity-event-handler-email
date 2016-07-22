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
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar"
           prefix="carbon" %>
<%@page import="org.apache.axis2.context.ConfigurationContext" %>
<%@page import="org.apache.commons.lang.ArrayUtils" %>
<%@page import="org.apache.commons.lang.StringUtils" %>
<%@page import="org.owasp.encoder.Encode" %>
<%@page import="org.wso2.carbon.CarbonConstants" %>
<%@page import="org.wso2.carbon.email.mgt.dto.xsd.EmailTemplateDTO" %>
<script type="text/javascript" src="extensions/js/vui.js"></script>
<script type="text/javascript" src="../extensions/core/js/vui.js"></script>
<script type="text/javascript" src="../admin/js/main.js"></script>

<jsp:include page="../dialog/display_messages.jsp"/>
<%@ page import="org.wso2.carbon.email.mgt.ui.I18nEmailMgtConfigServiceClient" %>
<%@ page import="org.wso2.carbon.email.mgt.ui.Util" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ResourceBundle" %>

<%
    String templateType = request.getParameter("templateType");

    String username = request.getParameter("username");
    String forwardTo = null;
    I18nEmailMgtConfigServiceClient client = null;

    EmailTemplateDTO[] emailTemplates = null;
    String emailSubject = "";
    String emailBody = "";
    String emailFooter = "";
    String templateName = "";
    String emailSubject0 = "";
    String emailBody0 = "";
    String emailFooter0 = "";
    String templateName0 = "";
    String displayLanguage = null;
    String displayLanguage0 = "";
    String emailContentType = null;
    String emailContentType0 = null;
    String[] emailContentTypeArr = {"text/html", "text/plain"};


    if (username == null) {
        username = (String) request.getSession().getAttribute("logged-user");
    }
    String BUNDLE = "org.wso2.carbon.email.mgt.ui.i18n.Resources";
    ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());

    try {
        String cookie = (String) session
                .getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        String backendServerURL = CarbonUIUtil.getServerURL(config
                .getServletContext(), session);
        ConfigurationContext configContext = (ConfigurationContext) config
                .getServletContext().getAttribute(
                        CarbonConstants.CONFIGURATION_CONTEXT);
        client = new I18nEmailMgtConfigServiceClient(cookie,
                backendServerURL, configContext);

        // get template types
        emailTemplates = client.loadEmailTemplates();
        if (emailTemplates == null) {
            emailTemplates = new EmailTemplateDTO[0];
        }

    } catch (Exception e) {
        String message = resourceBundle
                .getString("error.while.loading.email.template.data");
        CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request);
        forwardTo = "../admin/error.jsp";
    }
%>
<script type="text/javascript">

    function validate() {
        var value = document.getElementsByName("emailSubject")[0].value;
        if (value == '') {
            CARBON.showWarningDialog('<fmt:message key="email.template.subject.is.required"/>');
            return false;
        } else if (value.length > 50) {
            CARBON.showWarningDialog('<fmt:message key="email.template.subject.is.too.long"/>');
            return false;
        }

        var value = document.getElementsByName("emailBody")[0].value;
        if (value == '') {
            CARBON.showWarningDialog('<fmt:message key="email.template.body.is.required"/>');
            return false;
        }

        var value = document.getElementsByName("emailFooter")[0].value;
        if (value == '') {
            CARBON.showWarningDialog('<fmt:message key="email.template.footer.is.required"/>');
            return false;
        }
        document.templateForm.submit();
    }

    function updateFields(elm) {
        var $selectedOption = jQuery(elm).find(":selected");
        jQuery('#emailSubject').val($selectedOption.attr('data-subject'));
        jQuery('#emailBody').val($selectedOption.attr('data-body'));
        jQuery('#emailFooter').val($selectedOption.attr('data-footer'));
        jQuery('#templateName').val($selectedOption.attr('data-templateName'));
        jQuery('#emailContentType').val($selectedOption.attr('data-emailContentType'));
    }


    function updateLocale(elm) {
        var $selectedOption = jQuery(elm).find(":selected").text().trim();
        jQuery('<form>', {
            "id": "getTemplateType",
            "html": '<input type="text" name="templateType" value="' + $selectedOption + '" />',
            "action": window.location.href
        }).appendTo(document.body).submit();
    }

    function deleteTemplate() {
        var templateName = document.getElementsByName("emailTypes")[0].value;
        var locale = document.getElementsByName("emailLanguage")[0].value;

        var deleteFunc = function doDelete() {
            $.ajax({
                type: 'POST',
                url: 'email-template-config-finish-ajaxprocessor.jsp',
                headers: {
                    Accept: "text/html"
                },
                data: {'delete': true, 'templateName': templateName, 'locale': locale},
                async: false,
                success: function (responseText, status) {
                    if (status == "success") {
                        location.assign("email-template-config.jsp");
                    }
                }
            });
        }
        var msg = "This will delete {0}:{1} email template. Are you sure you want to continue?";
        msg = msg.replace("{0}", templateName).replace("{1}", locale);
        CARBON.showConfirmationDialog(msg, deleteFunc, null, null);
    }


    function deleteTemplateType() {
        var templateName = document.getElementsByName("emailTypes")[0].value;
        var deleteFunc = function doDelete() {
            $.ajax({
                type: 'POST',
                url: 'email-template-config-finish-ajaxprocessor.jsp',
                headers: {
                    Accept: "text/html"
                },
                data: {'delete': true, 'templateName': templateName, 'locale': "ALL"},
                async: false,
                success: function (responseText, status) {
                    if (status == "success") {
                        location.assign("email-template-config.jsp");
                    }
                }
            });
        }
        var msg = "This will delete all email templates of type {0}. Are you sure you want to continue?";
        CARBON.showConfirmationDialog(msg.replace("{0}", templateName), deleteFunc, null);
    }

</script>
<%
    if (forwardTo != null) {
%>
<script type="text/javascript">
    function forward() {
        location.href = "<%=Encode.forJavaScriptBlock(forwardTo)%>";
    }
</script>

<script type="text/javascript">
    forward();
</script>
<%
        return;
    }
%>

<script type="text/javascript" src="extensions/js/vui.js"></script>
<script type="text/javascript" src="../extensions/core/js/vui.js"></script>
<script type="text/javascript" src="../admin/js/main.js"></script>
<script type="text/javascript" src="../carbon/admin/js/breadcrumbs.js"></script>
<script type="text/javascript" src="../carbon/admin/js/cookies.js"></script>

<fmt:bundle basename="org.wso2.carbon.email.mgt.ui.i18n.Resources">
    <carbon:breadcrumb label="email.template"
                       resourceBundle="org.wso2.carbon.identity.user.profile.ui.i18n.Resources"
                       topPage="true" request="<%=request%>"/>

    <div id="middle">
        <h2>
            <fmt:message key="email.template.heading"/>
        </h2>

        <div id="workArea">
            <% if(ArrayUtils.isNotEmpty(emailTemplates)) {%>

            <form name="templateForm" action="email-template-config-finish-ajaxprocessor.jsp" method="post">
                <div class="sectionSeperator">
                    <fmt:message key="email.template.set"/>
                </div>
                <div class=”sectionSub”>
                    <table class="carbonFormTable">
                        <tr>
                            <td class="leftCol-med labelField"><fmt:message key="email.types"/></td>
                            <td><select id="emailTypes" name="emailTypes" class="leftCol-med"
                                        onchange="updateLocale(this);">
                                <%
                                    for (EmailTemplateDTO emailTemplate : emailTemplates) {
                                        System.out.println("API AWAAAAAAAAAA  : " + emailTemplates.length);
                                        String displayName = emailTemplate.getDisplayName();
                                        String selected = StringUtils.equalsIgnoreCase(templateType, displayName) ? "selected" : "";
                                %>
                                <option value="<%=displayName%>" <%=selected%>>
                                    <%=Encode.forHtmlContent(displayName)%>
                                </option>
                                <%
                                    }

                                    if (StringUtils.isBlank(templateType)) {
                                        if (ArrayUtils.isNotEmpty(emailTemplates)) {
                                            System.out.println("API AWAAAAAAAAAA  : " + emailTemplates.length);
                                            templateType = emailTemplates[0].getDisplayName();
                                        }
                                    }
                                %>

                            </select></td>
                        </tr>
                        <tr>
                            <td class="leftCol-med labelField"><fmt:message key="email.language"/></td>
                            <td><select id="emailLanguage" name="emailLanguage" class="leftCol-med"
                                        onchange="updateFields(this)">
                                <%
                                    List<EmailTemplateDTO> templatesList = new ArrayList<EmailTemplateDTO>();
                                    for (EmailTemplateDTO template : emailTemplates) {
                                        System.out.println("TEMPLATES : " + emailTemplates.length);
                                        if (template.getDisplayName().equalsIgnoreCase(templateType)) {
                                            templatesList.add(template);
                                        }
                                    }

                                    for (int i = 0; i < templatesList.size(); i++) {
                                        EmailTemplateDTO template = templatesList.get(i);
                                        if (i == 0) {
                                            emailSubject0 = template.getSubject();
                                            emailBody0 = template.getBody();
                                            emailFooter0 = template.getFooter();
                                            templateName0 = template.getName();
                                            emailContentType0 = template.getEmailContentType();
                                        }

                                        emailSubject = template.getSubject();
                                        emailBody = template.getBody();
                                        emailFooter = template.getFooter();
                                        templateName = template.getName();
                                        emailContentType = template.getEmailContentType();

                                        String localeCode = template.getLocale();
                                        String localeDisplayName = Util.getLocaleDisplayName(localeCode);

                                %>
                                <option
                                        value="<%=localeCode%>"
                                        data-subject="<%=Encode.forHtmlAttribute(emailSubject)%>"
                                        data-body="<%=Encode.forHtmlAttribute(emailBody)%>"
                                        data-footer="<%=Encode.forHtmlAttribute(emailFooter)%>"
                                        data-templateName="<%=Encode.forHtmlAttribute(templateName)%>"
                                        data-emailContentType="<%=Encode.forHtmlAttribute(emailContentType)%>">
                                    <%=Encode.forHtmlContent(localeDisplayName)%>
                                </option>
                                <%
                                    }
                                %>
                            </select></td>
                        </tr>
                        <tr>
                            <td class="leftCol-med labelField"><fmt:message key="email.template.content"/></td>

                            <td><select id="emailContentType" name="emailContentType" class="leftCol-med">
                                <%
                                    for (String currentType : emailContentTypeArr) {
                                        String currentSelectedAttr = "";
                                        if (currentType.equals(emailContentType0)) {
                                            currentSelectedAttr = "selected=\"selected\"";
                                        }
                                %>
                                <option <%=Encode.forHtmlAttribute(currentSelectedAttr)%>><%=Encode.forHtmlContent(currentType)%>
                                </option>
                                <%
                                    }
                                %>
                            </select></td>
                        </tr>
                        <tr>
                            <td><fmt:message key="emailSubject"/></td>
                            <td><input type="text" name="emailSubject" id="emailSubject" style="width : 500px;"
                                       value="<%=Encode.forHtmlAttribute(emailSubject0)%>"/></td>
                        </tr>
                        <tr>
                            <td><fmt:message key="emailBody"/></td>
                            <td><textarea name="emailBody" id="emailBody"
                                          class="text-box-big"
                                          style="width: 500px; height: 170px;"><%=Encode.forHtmlContent(emailBody0)%>
                            </textarea></td>
                        </tr>
                        <tr>
                            <td><fmt:message key="emailFooter"/></td>
                            <td><textarea name="emailFooter" id="emailFooter"
                                          class="text-box-big"
                                          style="width: 265px; height: 87px;"><%=Encode.forHtmlContent(emailFooter0)%>
                            </textarea></td>
                        </tr>
                        <tr>
                            <td><input type="hidden" name="templateName" id="templateName"
                                       value="<%=Encode.forHtmlAttribute(templateName0)%>"/></td>
                        </tr>
                    </table>
                </div>
                <div class="buttonRow">
                    <input type="button" class="button" value="Save" onclick="validate()"/>
                    <input type="button" class="button" value="Delete Template" onclick="deleteTemplate()"/>
                    <input type="button" class="button" value="Delete Template Type" onclick="deleteTemplateType()"/>
                </div>
            </form>
            <%} else {%>
            <div class="buttonRow">
                <fmt:message key="email.templates.empty"/>
            </div>
            <%}%>
        </div>
    </div>
</fmt:bundle>
