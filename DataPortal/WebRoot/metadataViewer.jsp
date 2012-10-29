<%@ page language="java" import="java.util.*" pageEncoding="ISO-8859-1"
	trimDirectiveWhitespaces="true"%>
<%
  String path = request.getContextPath();
  String basePath = request.getScheme() + "://" + request.getServerName()
      + ":" + request.getServerPort() + path + "/";

  String message = (String) request.getAttribute("message");
  String type = (String) request.getAttribute("type");
  String packageId = (String) request.getAttribute("packageId");

  if (message == null || message.isEmpty()) {
    message = "An error occurred during processing.";
    type = "warning";
  }
  
  if (packageId == null || packageId.isEmpty()) {
    packageId = "unknown";
  }

  if (type != null && type.equals("xml")) {
    response.setContentType("application/xml");
    out.println(message);
  } else {
    response.setContentType("text/html");
    out.println("<!doctype html>");
    out.println("<html>");
    out.println("<head>");
    out.println("<base href=\"" + basePath + "\">");
    out.println("<title>" + packageId + "</title>");
    out.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"./css/lter-nis.css\">");
    out.println("<script language=\"JavaScript\" type=\"text/javascript\" src=\"./js/toggle.js\"></script>");
    out.println("<script language=\"JavaScript\" type=\"text/javascript\" src=\"./js/jquery-1.7.1.js\"></script>");
    out.println("</head>");
    out.println("<body>");
    
    if (type != null && type.equals("warning")) {
      out.println("<p class=\"warning\">");
      out.println(message);
      out.println("</p>");
    } else {
      out.println(message);
    }
    
    out.println("</body>");
    out.println("</html>");
  }
%>