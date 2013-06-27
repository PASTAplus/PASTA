<%@ page language="java" import="java.util.*" pageEncoding="ISO-8859-1"%>
<%
	HttpSession httpSession = request.getSession();
	String uid = (String) httpSession.getAttribute("uid");
	
	String identity = null;
	String uname = null;
	
	if ((uid == null) || (uid.equals(""))) {
		identity = "<a href=\"./login.jsp\">login</a>";
		uname = " ";
	} else {
		identity = "<a href=\"./logout\">logout</a>";
		uname = uid;
	}
%>
<div class="head">
	<table width="100%">
		<tbody>
			<tr>
				<td width="25%">
					<img src="./images/LTER-logo.jpg" title="LTER Network Logo" width="54px" height="58px"/>
				</td>
				<td valign="middle" align="center" width="50%">
					<p id="head-title">LTER Network Data Portal</p>
				</td>
				<td align="right" valign="bottom" width="25%">
				</td>
			</tr>
		</tbody>
	</table>
</div>
