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

<%--<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>--%>
<script type="text/javascript">

    function addTemplateType() {
        var typeDisplayName = document.getElementsByName("templateDisplayName")[0].value;
        if (typeDisplayName == null || typeDisplayName == "") {
            alert("Not a valid name");
            location.href = '#';
        } else if (typeDisplayName.length > 50) {
            return false;
        }

        else {
                document.templateTypeForm.submit();
                return true;
        }
    }
</script>


<form id="templateTypeForm" name="templateTypeForm" method="post" action="test2.jsp">
    <input size="50" name="templateDisplayName" id="templateDisplayName" />
    <button onclick="addTemplateType()" type="button" class="button">Add</button>
</form>


