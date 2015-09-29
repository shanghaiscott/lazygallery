<%@taglib prefix="c" uri="http://java.sun.com/jstl/core"%>
<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>

<%@page import="java.io.*"%>

<%
  String imageURL = request.getPathInfo();
  String exifURL = imageURL + ".exif";
  if (!imageURL.contains("previews")) {
    exifURL = "/previews" + imageURL + ".exif";
  }
  File exifFile = new File (session.getServletContext().getRealPath(exifURL));
  boolean exif = exifFile.exists();
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Strict//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html>
  <head>
    <title>Image Preview <%=imageURL%></title>
  </head>
  <body>
  
  <div id="content" style="margin:0px;">
    <h4>Photo Preview <%=imageURL%></h4>

    <img src="<%=imageURL%>" 
         alt="<%=imageURL%>">
    <div>     
    <% if (exif) { %>
    <iframe src="<%=exifURL%>"></iframe>
    <% } %>
    
    </div>
  </div>

  </body>
</html>
