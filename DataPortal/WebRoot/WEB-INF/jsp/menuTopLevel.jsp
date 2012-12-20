<!-- 
<ul class="menu">
	<li><a href="./home.jsp">home</a></li>
	<li><a href="./discover.jsp">discover</a></li>
	<li><a href="./tools.jsp">tools</a></li>
	<li><a href="./about.jsp">about</a></li>
	<li><a href="./help.jsp">help</a></li>
	<li><a href="./contact.jsp">contact</a></li>
</ul>
 -->
<%
    HttpSession httpSession = request.getSession();
    String uid = (String) httpSession.getAttribute("uid");
    
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
                <td width="8%"><a href="./home.jsp">home</a></td>
                <td width="8%"><a href="./discover.jsp">discover</a></td>
                <td width="8%"><a href="./tools.jsp">tools</a></td>
                <td width="8%"><a href="./about.jsp">about</a></td>
                <td width="8%"><a href="./help.jsp">help</a></td>
                <td width="8%"><a href="./contact.jsp">contact</a></td>
                <td width="52%" id="login"><%=uname%><%=identity%></td>
            </tr>
        </tbody>
    </table>
</div>
