<%
    HttpSession httpSession = request.getSession();
    String uid = (String) httpSession.getAttribute("uid");
    String menuId = (String) httpSession.getAttribute("menuid");
    
    String home = "<td width=\"8%\"><a href=\"./home.jsp\">home</a></td>";
    String discover = "<td width=\"8%\"><a href=\"./discover.jsp\">discover</a></td>";
    String tools = "<td width=\"8%\"><a href=\"./tools.jsp\">tools</a></td>";
    String about = "<td width=\"8%\"><a href=\"./about.jsp\">about</a></td>";
    String help = "<td width=\"8%\"><a href=\"./help.jsp\">help</a></td>";
    String contact = "<td width=\"8%\"><a href=\"./contact.jsp\">contact</a></td>";
    
    if (menuId != null && !menuId.isEmpty()) {
	    if (menuId.equals("home")) {
	        home = "<td width=\"8%\" id=\"selected\"><a href=\"./home.jsp\">home</a></td>";
	        httpSession.setAttribute("menuid", null);
	    } else if (menuId.equals("discover")) {
	        discover = "<td width=\"8%\" id=\"selected\"><a href=\"./discover.jsp\">discover</a></td>";
	        httpSession.setAttribute("menuid", null);
	    } else if (menuId.equals("tools")) {
	        tools = "<td width=\"8%\" id=\"selected\"><a href=\"./tools.jsp\">tools</a></td>";
	        httpSession.setAttribute("menuid", null);
	    } else if (menuId.equals("about")) {
	        about = "<td width=\"8%\" id=\"selected\"><a href=\"./about.jsp\">about</a></td>";
	        httpSession.setAttribute("menuid", null);
	    } else if (menuId.equals("help")) {
	        help = "<td width=\"8%\" id=\"selected\"><a href=\"./help.jsp\">help</a></td>";
	        httpSession.setAttribute("menuid", null);
	    } else if (menuId.equals("contact")) {
	        contact = "<td width=\"8%\" id=\"selected\"><a href=\"./contact.jsp\">contact</a></td>";
	        httpSession.setAttribute("menuid", null);
	    }   
    }
    
    String identity = null;
    String uname = null;
    
    if ((uid == null) || (uid.equals(""))) {
        identity = "<a id=\"login\" href=\"./login.jsp\">login</a>";
        uname = " ";
    } else {
        identity = "<a id=\"login\" href=\"./logout\">logout</a>";
        uname = uid + " - ";
    }
%>
<div class="menu">
    <table>
        <tbody>
            <tr>
                <%=home%>
                <%=discover%>
                <%=tools%>
                <%=about%>
                <%=help%>
                <%=contact%>
                <td width="52%" id="login"><%=uname%><%=identity%></td>
            </tr>
        </tbody>
    </table>
</div>
