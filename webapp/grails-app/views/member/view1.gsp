<!DOCTYPE HTML>
<html>

	<%-- SECTION 1of4 - HEAD AND COMMON STYLE --%>
	
    <head>
		<meta charset="UTF-8">
		
		<!--script src="https://cdnjs.cloudflare.com/ajax/libs/amplifyjs/1.1.0/amplify.min.js"></script-->
		<!--
		https://cdn.jsdelivr.net/npm/radiojs@1.0.1/src/radio.min.js
		https://cdnjs.cloudflare.com/ajax/libs/radio/0.2.0/radio.min.js
		-->
		
		<link rel="stylesheet" href="https://cdn.webix.com/edge/webix.css" type="text/css">
		<script src="https://cdn.webix.com/edge/webix.min.js" type="text/javascript"></script> 

		<!-- skin
		<link rel="stylesheet" href="https://cdn.webix.com/site/skins/flat.css" type="text/css"   charset="utf-8">
		 -->

		<!--The following uses data URI and can be used to avoid fake favicon requests:-->
		<!-- https://stackoverflow.com/questions/1321878/how-to-prevent-favicon-ico-requests -->

		<link rel="shortcut icon" href="data:image/x-icon;," type="image/x-icon"> 



		<title>Anonymous multi-currency account reporting</title>
		
		<!-- safe colors -->
		<style>
			.r_highlight {
				background-color:#CC6666;
			}
			.g_highlight {
				background-color:#99CC99;
			}
			.g_editable_sleep {
				background-color:#FFFFCC;
			}
			.g_editable_sleep_ALT1 {
				background-color:#FFFFCC;
				border-width: 1px;
				border-style: solid;
				border-color: grey;
			}
			.k_border {
				border:1px solid black;
			}
			.a_center {
				text-align:center;
			}
			.a_right{
				text-align:right;
			}
		</style>
    </head>
	<%-- g_editable_sleep : also: light green: #CCFFCC; --%>
	<body>
		
	<script id="vm53T_TOS" type="text/template">
	<br>
	This application is for demo purposes only.<br>
	<br>
	<b>privacy</b><br>
	<br>
	no cookies are used,<br>
	no tracking is performed.<br>
	<br>
	<b>your data</b><br>
	<br>
	Lost or forgotten session codes cannot be recovered.<br>
	Any data wiping will be announced on this page on a one-week notice.<br>
	Private data export is strongly recommended (menu "balances").<br>
	<br>
	<b>&#9888;</b> Next data wiping on: TBC<br>
	<br>
	<b>financials</b><br>
	<br>
	The domestic currency is EUR.<br>
	The VaR is given as a 95% 1mo H-VaR(6mo) i.e. the expected loss within one month at the 95% confidence level. It is based on the past 125 historical FX rates<br>
	The variations Diff|noFX are computed as: amount@date2 / fxrate@date1 - amount@date1 / fxrate@date1.<br>
	The historical FX rates are taken on the ECB 1415 cut<br>
	<br>
	The VaR report should not be considered professional financial investment advice.<br>
	<br>
	&copy; 2020<br>
	<br>
	<br>
	</script>
	
	<script id="vm52T_contact" type="text/template">
	<br>
	email &rarr; a DOT moscatelli AT gmail DOT com<br>
	<br>
	</script>
	
	
	<script id="vm54T_guide" type="text/template">
	<br>
	<b>about</b><br>
	<br>
	This demo application is for multi-currency account holders, who can:<br>
	<ul style="list-style-type:circle">
	<li>record their balances on a regular basis</li>
	<li>get consolidated reports in the domestic currency</li>
	<li>monitor the variations and the exposure</li>
	</ul>
	<br>
	<b>registration and login</b><br>
	<ul style="list-style-type:circle">
	<li>a session code is generated on your first visit</li>
	<li>keep it in a safe place</li>
	<li>use it to login next time and access your data anonymously</li>
	<li>your session code is generated randomly - there can be 10<sup>32</sup> unique codes</li>
	<li>menu <a onclick="showlogin()" href="#">login</a></li>
	</ul>
	<br>
	<b>data entry</b><br>
	<ul style="list-style-type:circle">
	<li>menu <a onclick="showmenu('m21',false)" href="#"><i>dates</i></a> : enter the reporting dates with an optional label. Example: <i>2020-03-30</i> and <i>20Q1</i></li>
	<li>menu <a onclick="showmenu('m22',false)" href="#"><i>accounts</i></a> : enter your bank account labels and their currencies. Example:<i>HSBC UK - saving GBP</i> and <i>GBP</i></li>
	<li>menu <a onclick="showmenu('m31',false)" href="#"><i>balances</i></a> : record the balance for each reporting date and bank account</li>
	<li>export a "csv" file with your data from the menu <a onclick="showmenu('m31',false)" href="#"><i>balances</i></a> (UTF-8 encoding is used)</li>
	<li>import a "csv" file from the menu <a onclick="showmenu('m31',false)" href="#"><i>balances</i></a> - same format and encoding of the exported files - download <a href="${assetPath(src: 'test_upload.csv')}">sample</a></li>
	</ul>
	<br>
	<b>reports</b><br>
	<ul style="list-style-type:circle">
	<li>menu <a onclick="showmenu('m41',false)" href="#"><i>reports</i></a></li>
	<li>check your totals converted in the domestic currency (assumed EUR)</li>
	<li>check the variations (Diff) in the domestic currency</li>
	<li>check the variations (Diff|noFX) in the domestic currency after removing the FX effect</li>
	<li>check the VaR exposure, i.e. the expected worst loss due to FX effect within one month</li>
	<li>historical FX rates are retrieved in the background from the ECB</li>
	<li>more detail on the ToS page</li>
	</ul>
	<br>
	&copy; 2020<br>
	<br>
	<br>
	</script>
	
	
	<script id="vm51T_design" type="text/template">
	<br>
	<h2>design</h2>
	<br>
	excerpts of design specs and source code<br>
	<br>
	<h3>UML class diagram</h3>
	<i>enforcement of unique bank-account-name per user and unique reporting-date per user.</i><br>
	<img src="${assetPath(src: 'mcaa_uml_class.png')}" alt="[UML class diagram]" width=900><br>
	<br>
	<h3>UML communication diagram - eager loading (in use)</h3>
	<i>PROs : user wait time is minimised<br>
	CONs : server workload is maximised</i><br>
	<img src="${assetPath(src: 'mcaa_uml_communication_eager.png')}" alt="[UML communication diagram - eager loading]" width=900><br>
	<br>
	<h3>UML communication diagram - lazy loading (alternative)</h3>
	<i>PROs : server workload is minimised<br>
	CONs : user wait time is maximised</i><br>
	<img src="${assetPath(src: 'mcaa_uml_communication_lazy.png')}" alt="[UML communication diagram - lazy loading]" width=900><br>
	<br>
	<h3>delta-report query (in use)</h3>
	<i>for each user's reporting-date that is lower than the highest,<br>
	show the totals and the variations with going FX rates and frozen FX rates</i><br>
	<img src="${assetPath(src: 'delta_report_cypher_part1.png')}" alt="[generation of the delta report - part 1]" width=900><br>
	<img src="${assetPath(src: 'delta_report_cypher_part2.png')}" alt="[generation of the delta report - part 2]" width=900><br>
	<br>
	<h3>delta-report query (equivalent SQL)</h3><br>
	<img src="${assetPath(src: 'delta_report_equiv_sql_part1.png')}" alt="[SQL generation of the delta report - part 1]" width=900><br>
	<img src="${assetPath(src: 'delta_report_equiv_sql_part2.png')}" alt="[SQL generation of the delta report - part 2]" width=900><br>
	<br>
	<h3>1-day 95% VaR (baseline for the displayed 1-month VaR)</h3><br>
	<img src="${assetPath(src: 'mcaa_hvar_function.png')}" alt="[generation of the VaR report]" width=900><br>
	<br>
	<br>
	&copy; 2020<br>
	<br>
	<br>
	</script>
								
	
	<%-- SECTION 2of4 - COMMON JS --%>
		
		<!-- COMMON JS DYNAMIC -->
		<script type="text/javascript" charset="utf-8">
		var session_code="${session_code}";
		var session_stats_c="(created just now)";
		var session_stats_a="";
		var isdev = ${isdev};
		
		<g:if test="${session_dob && session_last}">
		var session_dob=${session_dob};
		var session_last=${session_last};
		var session_stats_c = "created on:<br>" + new Date(session_dob).toString().substr(0,21);  // Wed Oct 14 2020 12:00:02
		var session_stats_a = "last login on:<br>" + new Date(session_last).toString().substr(0,21);
		</g:if>
		</script>
		<!-- COMMON JS STATIC -->
		
		<asset:javascript src="mcaa.js" alt=""/>
		<%-- <script type="text/javascript" charset="utf-8"></script> --%>
		
	</body>
</html>
