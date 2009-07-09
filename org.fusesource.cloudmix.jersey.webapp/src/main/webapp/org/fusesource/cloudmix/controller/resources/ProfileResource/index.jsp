<%--

     Copyright (C) 2008 Progress Software, Inc. All rights reserved.
     http://fusesource.com

     The software in this package is published under the terms of the AGPL license
     a copy of which has been included with this distribution in the license.txt file.

--%>
<%@ page import="java.util.Date" %>
<%@page contentType="text/html" %>
<%@page pageEncoding="UTF-8" %>

<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">

<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
  <title>Profile View</title>
</head>
<body>

<h2>Profile: ${it.profileId}</h2>

<table>
  <tr>
    <th>Feature</th>
    <th>Provisioned?</th>
  </tr>
  <c:forEach var="i" items="${it.status.status.features}">
    <tr>
      <td><a href="/features/${i.featureId}">${i.featureId}</a></td>
      <td>${i.provisioned}</td>
    </tr>
  </c:forEach>
</table>

</body>
</html>
