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

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>


<fmt:bundle basename="org.wso2.carbon.email.mgt.ui.i18n.Resources">
    <carbon:breadcrumb label="email.template.mgt"
                       resourceBundle="org.wso2.carbon.email.mgt.ui.i18n.Resources"
                       topPage="true"
                       request="<%=request%>"/>

    <div id="middle">
        <h2><fmt:message key='email.template.mgt'/></h2>

        <div id="workArea">
            <table class="styledLeft" id="internal" name="internal" width="100%">
                <tr class="tableOddRow">
                    <td style="width: 30px;">
                        <div style="height:30px;">
                            <a href="javascript:document.location.href='email-template-type-add.jsp'"
                               class="icon-link"
                               style="background-image:url(images/add.gif);">
                                <fmt:message key='email.template.type.add'/>
                            </a>
                        </div>
                    </td>
                </tr>
                <tr class="tableEvenRow">
                    <td style="width: 30px;">
                        <div style="height:30px;">
                            <a href="javascript:document.location.href='email-template-add.jsp'"
                               class="icon-link"
                               style="background-image:url(images/add.gif);">
                                <fmt:message key='email.template.add'/>
                            </a>
                        </div>
                    </td>
                </tr>

            </table>

            <br/>
        </div>
    </div>
</fmt:bundle>


