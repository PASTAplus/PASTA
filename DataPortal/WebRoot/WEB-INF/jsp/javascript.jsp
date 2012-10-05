<%@ page language="java" import="java.util.*" pageEncoding="ISO-8859-1"%>
<script src="./js/jquery-1.7.1.js" type="text/javascript"></script>
<script src="./js/superfish.js" type="text/javascript"></script>
<script>
	$(document).ready(function() {
		$('ul.nav').superfish({
			delay : 1000,
			animation : {
				opacity : 'show',
				height : 'show'
			},
			speed : 'fast',
			autoArrows : false,
			dropShadows : false
		});
	});
</script>