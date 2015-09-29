<%@taglib prefix="c" uri="http://java.sun.com/jstl/core"%>
<%@taglib prefix="sp" uri="/WEB-INF/tlds/spacepirates.tld"%>
<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>

<%
String pageTitle="";
if (request.getParameter("imageDir") != null){ // starting directory, e.g. /china
  pageTitle=request.getParameter("imageDir");
} else {
  pageTitle=request.getPathInfo();
}
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Strict//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html>
  <head>
    <meta HTTP-EQUIV="Content-Type" CONTENT="text/html; charset=UTF-8">
    <title>Image Gallery <%=pageTitle%></title>
    <script src="<c:url value="/lib/slimbox-1.41/js/slimbox.js"/>" type="text/javascript"></script>
    <link href="<c:url value="/lib/slimbox-1.41/css/slimbox.css"/>"/>
  </head>
  <div id="content">
    <h4>Lazy Gallery <%=pageTitle%></h4>
  
    <sp:lazygallery thumbsPerRow="4" debug="true" lightbox="true"/>
    
    </div>
  </body>
</html>
