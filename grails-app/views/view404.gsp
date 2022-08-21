<!DOCTYPE HTML>
<html>

	<%-- SECTION 1of4 - HEAD AND COMMON STYLE --%>
	
    <head>
		<meta charset="UTF-8">
		<link rel="stylesheet" href="https://cdn.webix.com/edge/webix.css" type="text/css">
		<script src="https://cdn.webix.com/edge/webix.js" type="text/javascript"></script> 


		<!--The following uses data URI and can be used to avoid fake favicon requests:-->
		<!-- https://stackoverflow.com/questions/1321878/how-to-prevent-favicon-ico-requests -->

		<link rel="shortcut icon" href="data:image/x-icon;," type="image/x-icon"> 



		<title>anonymous multicurrency account reporting</title>
		
		<style>
			.r_highlight{
				background-color:#FFAAAA;
			}
			.g_highlight{
				background-color:#AAFFAA;
			}
		</style>
    </head>
	<body>
		
	<%-- SECTION 2of4 - COMMON JS --%>
		
		<!-- COMMON JS -->
		<script type="text/javascript" charset="utf-8">
		
		webix.ui({
			id:"page",
			type:"wide",
			rows:[ {template:"<br>"+"page not found" + "<br><br><a href='/member/index'>home</a>"} ]
			
		});
        </script>
	</body>
</html>
