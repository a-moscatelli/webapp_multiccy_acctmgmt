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

		<title>anonymous multicurrency account reporting - DEV</title>
		
    </head>
	<body>
		
		<%-- SECTION 2of4 - COMMON JS --%>
		
		<script type="text/javascript" charset="utf-8">
		var session_code= "${session_code}";
		
		var dynamicview = {
		view:"datatable", id:"datatable1",
		autoConfig:true,
		adjust: true,
		scroll:"xy",
		url: "/member/devreportdata"
		};
		
		var exportform = {
			view: "form",
			cols:[
			{
				view: "label", label: "Export CSV", width: 100
			},
			{
				view: "button", label: "All Fields", click:function(){
					webix.toExcel($$("datatable1"));
				}
			}
			]
		};

		webix.ui({rows:[dynamicview,exportform]});
		</script>
	</body>
</html>
