// external-dynamic:
// session_code

var gdb=isdev; // debugging messages

var filler = {}; /* valvola di sfogo */
var ksp = "&#9657;"; 					// "&nbsp;&nbsp;";
									//var ksp="&nbsp;&nbsp;&nbsp;";

var kbr = "<br>";
var kdel = "&#10008;"; 				//	"&#9746;"; // 9747
var g_editaction = "click";

var g_loaded = {
	dates: false,
	accounts: false,
	balances: false,
	reports: false
};
var g_created = {
	dates: false,
	accounts: false,
	balances: false,
	reports: false
};


webix.csv.delimiter.cols = ",";

function showmenu(mid,byclick) {
	// byclick = true only inside the view "menu" on:{}
	if(!byclick) {
		$$('leftbar').select(mid);
	}
	if(true) {
		$$('rightviews').getChildViews().forEach(function(o,i) {o.hide();});
		$$("v"+mid).show();
	}
}

function showlogin() {
	showmenu("m10",false);
}

function dodel(id,uc) {
	//webix.message("deleting"+id+" - "+uc);
	if(uc==1) delete_upon_confirmation("delete this date?",$$("vm21_dates").getItem(id).vm211dt.substr(0,4+1+2+1+2),"vm21_add_dt",id);
	if(uc==2) delete_upon_confirmation("delete this account?",$$("vm22_accounts").getItem(id).vm221nm,"vm22_add_act",id);
}

function refreshreports() {
	$$("vm41ra").load("/member/report/byccydt");
	$$("vm41rav").load("/member/report/byccydtz");
	$$("vm41rb").load("/member/report/bybcydt");
	$$("vm41rbd").load("/member/report/bybcydtvar");	
}

function delete_upon_confirmation(title,text,form_id,id) {
	var promise = webix.confirm({	title:title, ok:"Yes", cancel:"No",	text: text});
	// HEY! if user press ESC, the Cancel is the way.
	//promise.then(function(){});
	// $$(form_id).getValues()
	if(form_id=="vm21_add_dt") promise.then(function(){
			$$("vm21_dates").remove(id);
			// $$("vm31a").load("/member/balance"); already triggerd by vm21_dates
			refreshreports();
	});
	if(form_id=="vm22_add_act") promise.then(function(){
			$$("vm22_accounts").remove(id);
			// $$("vm31a").load("/member/balance"); already triggerd by vm21_dates
			refreshreports();
	});
		
}

function ajax_update_field(oldval,newval,cid,rid,UC,error_msg) {		/*wrt_editability*/
	//webix.message("updating");
	// state.value, 
	//ajax_update_field(state.value,editor.column,editor.row,"vm31a","error updating balance");
	if(oldval==newval) return;
	if(UC=="UB") {
		var url="/member/balance";
		var vm31ac= $$("vm31a").getItem(rid).vm31ac;
		var vm31dt= $$("vm31a").getItem(rid).vm31dt;
		var promise = webix.ajax().post(url,{webix_operation:"customupdate",vm31av:newval, oldval:oldval, vm31ac:vm31ac, vm31dt:vm31dt});
		//promise.then(function(data){});
		promise.fail(function(err){ webix.message({ text:error_msg, type:"error"});});
		promise.finally(function() { if(UC=="UB") {
										$$("vm31a").load("/member/balance");
										refreshreports();
									}});
	}
}

var leftbar_fixed = {
	view:"menu",
	id:"leftbar",
	layout:"y",
	width:100,
	select:true,
	click:function(mid) { showmenu(mid,true);},
	data: [
		{ id:"m54", value: "home"},
		{ id:"m10", value: "login"},
		//{ id:"m10", value: "user"},
		{ $template: "Separator" },
		//{ id:"m22", value: ksp+"bank accounts"},
		{ id:"m22", value: ksp+"accounts"},
		//{ id:"m21", value: ksp+"reporting dates"},
		{ id:"m21", value: ksp+"dates"},
		{ $template: "Separator" },
		{ id:"m31", value: ksp+ksp+"balances"},	
		{ $template: "Separator" },
		//{ id:"m41", value: ksp+ksp+ksp+"reports"},		
		{ id:"m41", value: ksp+ksp+ksp+"risk"},		
		{ $template: "Separator" },
		{ id:"m53", value: "ToS"},
		//{ id:"m51", value: "behind-the-scenes"},
		{ id:"m51", value: "design"},
		{ id:"m52", value: "contact"}
		//{ id:"m52", value: "contact us"}
	]
};



var leftbar = leftbar_fixed;

var vm10_user_LW = 150;
var vm10_user_W = 420;




var vm10_user = { /* scroll:"y", */ rows: [
					
					//{template:" ", adjust:true},
		
					/* // VERBOSE
						{ cols: [ { view:"form", adjust:true, elements:[
						{ template:"current session", type:"section"},
						{ view:"label", label: "Welcome!", 
										labelWidth:vm10_user_LW},
						{ view:"label", label: "This is your current session code:",
										labelWidth:vm10_user_LW},
						{ view:"label", label: "<mark>"+session_code+"</mark>",
										labelWidth:vm10_user_LW},
						{ view:"label", label: "Just keep it in a safe place and copy below to retrieve your data next time.", 
										labelWidth:vm10_user_LW}
						]},{}]
					}, */
					
		{ view:"form", //width:vm10_user_W, /*adjust:true,  */ 
			elements:[
				{ template:"current session", type:"section"},
				//{ view:"label", label: "you are logged in as:"},	//		labelWidth:300},
				//{ view:"label", label: "Welcome!", labelWidth:vm10_user_LW},
				//{ view:"label", label: "current session:", labelWidth:vm10_user_LW},
				// { view:"label", label: "<font color=lightgrey>"+session_code+"</font>",	labelWidth:300},
				{ view:"accordion", multi:true, rows:[
					{ header:"reveal current session code", collapsed:true, 
					  body: { height:140, template: "<br>you are logged in as:<br>"
								+ session_code + "<br>" + session_stats_c + "<br>" + session_stats_a }
					}
				]}
			//{ view:"label", label: "<font color=darkgrey>"+session_code+"</font>"},	//,	labelWidth:300},
			//{ view:"label", label: session_stats_c},	//,	labelWidth:300},
			//{ view:"label", label: session_stats_a}	// labelWidth:300}
			]
		},
		{ view:"form", /*autowidth:true, */ 
			// width:vm10_user_W,
			id:"vm10_switchuser", 
				elementsConfig:{labelPosition:"top"}, elements:[
			{ template:"switch to session", type:"section"},
			//{ label:"session code", type:"password", name:"account", required:true, labelWidth:vm10_user_LW},
			{ view:"text", type:"password", label:"session code", required:true, name:"account", labelWidth:vm10_user_LW},
			{ view:"button", minWidth:180, maxWidth:300, 
				hotkey: "enter+ctrl", value:"login", click:function(id,e){
					webix.send("/member/login",
					$$("vm10_switchuser").getValues(),"POST");
				}
			}
			]
		},
		{ view:"form", // width:vm10_user_W, 
			/*adjust:true, */ elements:[
			{ template:"reset session", type:"section"},
			{ view:"button", value:"logout", click:function(id,e){
					webix.send("/member/logout", {},"POST");
				}
			}
			]
		}
	]
	//}		]
};


var vm10_sessionmgmt_wrap = {cols: [vm10_user,filler]};

var vm21_dates_addform = { view:"form", id:"vm21_add_dt", elementsConfig:{labelPosition:"top"},
	elements:[
			//{ template:"add reporting date", type:"section"},
			{
				view: "datepicker",
				minWidth:180, maxWidth:300,
				// will be 300 on desktop, 236 on mobile, 240 driven by the invalidMessage
				invalidMessage: "please add today or past date",
				//value: new Date(2017,10,9),
				label: "Reporting Date",
				required:true,
				name:"dt",
				//labelWidth:100
				timepicker: false
			}, //{width:20},
			{ view:"button",
				value:"add", click:function(id,e){
					if(!$$("vm21_add_dt").validate()) return;
					$$("vm21_add_dt").disable();
					$$("vm21_add_dt").showProgress({ type:"icon", delay:0});
					$$("vm21_add_dt").setValues({ webix_operation:"insert_custom"},true);
					var promise = webix.ajax().post("/member/date",$$("vm21_add_dt").getValues());
					promise.then(function(data){
						//if(gdb) webix.message("added date:<br>"+JSON.stringify(data));	// {}
						//if(gdb) webix.message("added date:<br>"+data.text()); // '{...}'
						var jsonc=data.json();
						if(!jsonc.created) {webix.message({type:"debug", text:"duplicated date"})} // error:114
						//if(gdb) webix.message("ajax added date:<br>"+data.json().id);
						if(gdb) webix.message("ajax added date:<br>"+data.json().dt);
						//$$("vm21_dates").add(jsonc); // no. duplications may not be added. decided server-side.
						$$("vm21_dates").load("/member/date");
						$$("vm31a").load("/member/balance");
						
					});
					promise.fail(function(err){
						webix.message({ text:"could not add date", type:"error"});
					});
					//webix.send("http://localhost:8080/anonymous_multicurrency_account.html",
					//	$$("vm10_switchuser").getValues(),"POST");
					promise.finally(function(){
						$$("vm21_add_dt").hideProgress();
						$$("vm21_add_dt").enable();
					});
				}
			}
		], // end elements
		rules: {
			"dt": function(value) { return value <= new Date();} // Thu Oct 08 2020 00:00:00 GMT+0800 (Singapore Standard Time)
		}
}; // view form

vm21_dates_table = {
		view:"datatable", select: "cell", 
		//scroll:"xy", 
		minWidth:500, // minWidth:400, 
		//minWidth:236, maxWidth:300,
		id:"vm21_dates", url: "/member/date",
		editable:true, editaction:g_editaction,	/*wrt_editability*/
		columns: [	// adjust: data,header,true
			{ id: "vm211X",  width:100, header: "Delete", adjust:"header", css:"a_center", template:"<div onClick='dodel(#id#,1)'>"+kdel+"</div>" }, // delete #id#</div>" }
			{ id: "vm211dt", width:100, header: "Date", sort:"string", format:webix.Date.dateToStr("%Y-%m-%d") }, // , editor:"date", map:"(date)#vm211dt#" },
			{ id: "vm211L",  width:100, header: "Label", editor:"text", css:"g_editable_sleep" },
			{ id: "vm211N",  fillspace:true, header: "Comment", editor:"text", css:"g_editable_sleep" }
			// fillspace:true,
			//{ id: "vm211FX",header: "FX data status", adjust: "header" },
		],
		ready:function() {
			//webix.extend(this, webix.ProgressBar);
		},
		updateFromResponse:true, // ?? no effect...
		on: {
			onStructureLoad:function() {
				//webix.extend(this, webix.ProgressBar);
			},
			onBeforeLoad: function() {
				if(!g_created.dates) {
					webix.extend(this, webix.ProgressBar);
					g_created.dates=true;
				}
				this.showProgress({ type:"icon", delay:0});
				//webix.message("onBeforeLoad");
			},
			// onAfterLoad: function() {	webix.message("onAfterLoad");},
			
			//the add() function
			onBeforeAdd:	function() {
				this.showProgress({ type:"icon", delay:0});
				if(gdb) webix.message("onBeforeAdd"); // fired
			},
			onAfterAdd:	function() {	
				this.hideProgress();
				if(gdb) webix.message("onAfterAdd"); // fired
			},
			onBeforeDelete:	function() {
				this.showProgress({ type:"icon", delay:0});
				if(gdb) webix.message("EEE onBeforeDelete");
			},
			onAfterDelete:	function() {
				this.hideProgress();
				if(gdb) webix.message("EEE onAfterDelete");
			},
			onAfterEditStop: function(state, editor, ignoreUpdate){
				if(state.value != state.old){
					//often
					if(gdb) webix.message("onAfterEditStop Cell value was changed");
				}
			},
			//onAfterRender: function(){
				// often
				//if(gdb) webix.message("onAfterRender");
			//},
			onDataUpdate: function(id, data, old){
				if(gdb) webix.message("EEE onDataUpdate Cell value was changed");
			},
			onStoreUpdated: function(id, data, old){
				if(gdb) webix.message("onStoreUpdated Cell value was changed");
			},
			onLiveEdit: function(state, editor, ignoreUpdate){
				if(gdb) webix.message("onLiveEdit Current value: " + state.value);
			},
			onLoadError: function(xhr){
				this.hideProgress();
				webix.message({ text:"error on loading dates", type:"error"});	// error:125
				//if(gdb) webix.message("onLoadError Loading error");
			},
			onAfterLoad: function(){
				this.hideProgress();
				if(gdb) webix.message("EEE onAfterLoad vm21_dates Data loaded");
				if(this.count()==0) webix.message({ text:"please add reporting dates", type:"debug"}); // error:122
			},
			onAfterSave: function(response, id, details){
				if(gdb) webix.message("onAfterSave vm21_dates Data saved");
			},
			onAfterSaveError:  function(response, id, details){
				if(gdb) webix.message("onAfterSaveError vm21_dates Data saved");
			},
			onAfterUpdate: function(response, id, object){
				if(gdb) webix.message("onAfterUpdate vm21_dates Data saved");
			},
			onAfterSync: function(statusObj, text, data, loader){
				if(gdb) webix.message("onAfterSync vm21_dates Data saved");
			}

			
			// never:
			// onDataRequest: function() {	webix.message("onDataRequest");},
			// onDataUpdate:	function() { webix.message("onDataUpdate");}
	//	webix.extend($$("vm21_dates"), webix.OverlayBox);
	
	//$$("rightviews").showProgress({ type:"icon", delay:3000});
	
		},
		
		//url: function(){ return webix.ajax("/member/date").fail(function(){ if(gdb) webix.message("errore grave")})},
		save:{
			"insert":"/member/date",		// scatta 1. l'ajax del form button onItemClick. 2. onafterload
			"update":"/member/date",		// scatta nothing.
			"delete":"/member/date"		// scatta 1. onbeforedelete e 2. onafterdelete
		}
		//autowidth:true
		//width:500
};


var vm21_dates_addform_wrap = {cols: [vm21_dates_addform,filler]};
var vm21_dates_table_wrap = {view:"scrollview", scroll:"auto", body:vm21_dates_table};

var vm22_accounts_W=250;

var vm22_accounts_addform = { view:"form", id:"vm22_add_act", elementsConfig:{labelPosition:"top"}, elements:[
			//{ template:"add account", type:"section"},
			{
				view: "text",
				label: "Bank Account Label",
				//width:vm22_accounts_W,
				minWidth:180, maxWidth:300,		// minWidth:236,
				//minWidth:50, // vm10_user_LW+100,
				//maxWidth:200,
				required:true,
				name: "nm",
				invalidMessage: "letters, digits, ()._-/"
//				labelWidth:130
			},
			//{width:20},
			/*{
				view: "text",
				label: "Currency",
				//width:vm22_accounts_W,
				//minWidth:50, // vm10_user_LW+100,
				//maxWidth:200,
				//minWidth:236, maxWidth:300,
				required:true,
				//inputWidth:3,
				name:"ccy",
				invalidMessage: "ISO currency names are accepted (e.g. EUR)"
				//labelWidth:100
			},*/ //{width:20},
			
			/*
			{	view:"richselect", label: "Currency", name:"ccy", // required:true,
				suggest: "/member/currencies"
				//options:[
				//	'AUD','BGN','BRL','CAD','CHF','CNY','CZK','DKK','EUR','GBP','HKD','HRK','HUF','IDR','ILS','INR','ISK','JPY','KRW','MXN','MYR','NOK','NZD','PHP','PLN','RON','RUB','SEK','SGD','THB','TRY','USD','ZAR'
				//]
				//invalidMessage: "please choose one"
			},*/
			
			{
				view:"combo", label: "Currency", name:"ccy", id: "acctccy",
				//options: "/member/currencies"
				options:[
					'AUD','BGN','BRL','CAD','CHF','CNY','CZK','DKK','EUR','GBP','HKD','HRK','HUF','IDR','ILS','INR','ISK','JPY','KRW','MXN','MYR','NOK','NZD','PHP','PLN','RON','RUB','SEK','SGD','THB','TRY','USD','ZAR'
				]
			},
			/*{
				view: "text",
				label: "Note",
				name:"N",
				labelWidth:vm10_user_LW
			},*/
			{ view:"button", 
				//minWidth:50, // vm10_user_LW+100,
				//maxWidth:200,
				// width:vm22_accounts_W,
				hotkey: "enter+ctrl", value:"add", on:{ onItemClick:function(id,e){
				if(!$$("vm22_add_act").validate()) return;
				$$("vm22_add_act").disable();
				$$("vm22_add_act").showProgress({ type:"icon", delay:0});
				$$("vm22_add_act").setValues({ webix_operation:"insert_custom"},true);	// true = merge into
				var promise = webix.ajax().post("/member/account",$$("vm22_add_act").getValues());
				promise.then(function(data){
					//if(gdb) webix.message("added date:<br>"+JSON.stringify(data));	// {}
					//if(gdb) webix.message("added date:<br>"+data.text()); // '{...}'
					var jsonc=data.json();
					if(gdb) webix.message("added account id:<br>"+jsonc.id);
					if(gdb) webix.message("added account nm:<br>"+jsonc.vm221nm);
					if(jsonc.status=='success') {
						$$("vm22_accounts").add(jsonc);
						$$("vm31a").load("/member/balance"); // dependent
						$$("vm31a").refresh();
						$$("vm22_accounts").refresh();
					} else {
						webix.message({ text:"duplicated account", type:"debug"});	// error:214
					}
				});
				promise.fail(function(err){ webix.message({ text:"could not add account", type:"error"});}); // error:215
				//webix.send("http://localhost:8080/anonymous_multicurrency_account.html",
				//	$$("vm10_switchuser").getValues(),"POST");
				promise.finally(function(){
					$$("vm22_add_act").hideProgress();
					$$("vm22_add_act").enable();
				});
				}}
			}
		],
		rules: {
			// "ccy": function(value) { return /^[a-zA-Z]{3}$/.test(value)},
			"nm":  function(value) { return /^[\w \(\)\/\-\.]+$/.test(value)}
		}
	};
	

var vm22_accounts_table = {
		view:"datatable", select: "cell", id:"vm22_accounts", url: "/member/account",
		editable:true, editaction:g_editaction,	/*wrt_editability*/
		minWidth:500,
		columns: [
			{ id: "vm221X",   header: "Delete", css:"a_center", adjust: "header", template:"<div onClick='dodel(#id#,2)'>"+kdel+"</div>" }, // delete #id#</div>" }
			{ id: "vm221nm",  header: "Account", sort:"string", width:200 },	// , editor:"text"
			{ id: "vm221ccy", header: "Currency", sort:"string", adjust: "header"}, // ,editor:"text", css:"g_editable_sleep" },
			{ id: "vm221N",   fillspace:true, header: "Comment",editor:"text", css:"g_editable_sleep" },
		],
		save: "/member/account",
		on: {
			onBeforeLoad: function() {
				if(!g_created.account) {
					webix.extend(this, webix.ProgressBar);
					g_created.account=true;
				}
				this.showProgress({ type:"icon", delay:0});
				//webix.message("onBeforeLoad");
			},
			onBeforeAdd:	function() {
				$$("vm22_accounts").showProgress({ type:"icon", delay:0});
				if(gdb) webix.message("onBeforeAdd"); // fired
			},
			onAfterAdd:	function() {	
				$$("vm22_accounts").hideProgress();
				if(gdb) webix.message("onAfterAdd"); // fired
			},
			onBeforeDelete:	function() {
				$$("vm22_accounts").showProgress({ type:"icon", delay:0});
				if(gdb) webix.message("EEE onBeforeDelete");
			},
			onAfterDelete:	function() {
				$$("vm22_accounts").hideProgress();
				if(gdb) webix.message("EEE onAfterDelete");
			},
			onAfterLoad: function(){
				this.hideProgress();
				if(gdb) webix.message("EEE onAfterLoad vm22_accounts Data loaded");
				if(this.count()==0) webix.message({ text:"please add accounts", type:"debug"}); // error:222
			},
			onLoadError: function(xhr){
				this.hideProgress();
				webix.message({ text:"error on loading accounts", type:"error"});	// error:225
				//if(gdb) webix.message("onLoadError Loading error");
			},			
		}
};

var vm22_accounts_addform_wrap = {cols: [vm22_accounts_addform,filler]};	
var vm22_accounts_table_wrap = {view:"scrollview", scroll:"auto", body:vm22_accounts_table};

var vm31_balances_exportform = {
		view: "form",
		minWidth:180, maxWidth:300,	
		elements: [
		//{	view: "label", label: "Export CSV", width: 100},
		{	view: "button", label: "Export CSV", click:function(){ 
					webix.toCSV( $$("vm31a"), {filename: "balances", ignore: { "vm31fx":false }});
				}
		},
		{
			view: "uploader", id:"csvuploader", value: "Upload CSV",	multiple:false, name:"files",
			accept:"text/csv",
			link:"mylist",  upload:"/member/upload",
			on: {
				onViewShow: function() {
					var or1 = $$("vm21_dates").count() >0
					var or2 = $$("vm22_accounts").count() >0
					if(or1 || or2) {$$("csvuploader").disable();}
					if(gdb) webix.message("csvuploader onViewShow");
				},
				onFileUpload: function(file, response){
					if(gdb) webix.message("done");
					$$("csvuploader").disable();
					$$("vm21_dates").load("/member/date");
					$$("vm22_accounts").load("/member/account");
					$$("vm31a").load("/member/balance");
					refreshreports();
				},
				onFileUploadError:function(file, response){
					//console.log(response.totinvalid);
					//console.log(response.errortype);
					webix.message({ 
						text:"error at line "+response.firstline + " ("+response.errortype+")",
						type:"error"});
				}
			}
		},
		{
			view:"list",  id:"mylist", type:"uploader",
			autoheight:true, borderless:true	
		}
		/*,
		{
			view: "button", label: "Get value", click: function() {
				var text = this.getParentView().getValues();
				text = JSON.stringify(text, "\n");
				webix.message("<pre>"+text+"</pre>");
			}
		}*/
		//{},{}
		]
}
	
var vm31_balances_table = {
		view:"datatable",	select: "cell", id: "vm31a", // scroll:"xy", 
		url: "/member/balance",
		minWidth:650,	// important. if missed, blank table is displayed.
		// keep minWidth > sum(widths) below so that you do not see TWO scroll bars (table's + container's)
		editable:true, editaction:g_editaction,	/*wrt_editability*/
		//autoConfig:true,
		columns: [
			{ id: "vm31dt", header: "Date", sort:"string", width:100, format:webix.Date.dateToStr("%Y-%m-%d") },
			{ id: "vm31ac", header: "Account", sort:"string", width:150 }, // editor:"text", /*wrt_editability*/ },
			{ id: "vm31av", header: "Amount", width:150, editor:"text", css:"g_editable_sleep" /*wrt_editability*/ },
			{ id: "vm31ccy", header: "Currency", sort:"string", width:100}, // editor:"text", /*wrt_editability*/ },
			{ id: "vm31fx", header: "Hist. FX", width:100, format:webix.Number.numToStr(fxfmt) }, // editor:"text", /*wrt_editability*/ },
			{ id: "vm31fill", header: "", fillspace:true } // editor:"text", /*wrt_editability*/ },
			//{ id: "vm31a3", header: "HSBC savings EUR (EUR)", adjust: "header", editor:"text", /*wrt_editability*/ }
		],		
		on: {
			onAfterEditStop: function(state, editor) {	/*wrt_editability*/
					if(editor.column == "vm31av"){
						ajax_update_field(state.old, state.value, editor.column, editor.row, "UB","error updating balance");
					}
			}
			/*onDataUpdate: function(id, data, old){
				if(gdb) webix.message("EEE onDataUpdate Cell value was changed");
				//ajax_update_field(state.value,editor.column,editor.row,"vm31a","error updating balance");
				
			}*/		
		}
		//save: "/member/balance_u"
		//ajax_update_field(state.value,editor.column,editor.row,"vm31a","error updating balance");

};
	
	//{ autoheight:true, template:"<br><br>"+"<a href='/member/export'>export</a>"+"<br><br>" }
// ]};

var vm31_balances_exportform_wrap = {cols: [vm31_balances_exportform,filler]};	
var vm31_balances_table_wrap = {view:"scrollview", scroll:"auto", body:vm31_balances_table};

var amtfmt = {
	groupDelimiter:",",
	groupSize:3,
	decimalDelimiter:".",
	decimalSize:2
};
var fxfmt = {
	groupDelimiter:",",
	groupSize:3,
	decimalDelimiter:".",
	decimalSize:4
};



var vm41_reports = { view:"accordion", multi:false, /*"mixed,"*/ rows:[
		{ header:"TOTALS BY DATE, CURRENCY", collapsed:true, body: {
			view:"datatable",	select: "cell", id: "vm41ra", url: "/member/report/byccydt",
			columns: [
				{ id: "vm31dt", header: "Date", adjust: true }, // , format:webix.Date.dateToStr("%Y-%m-%d") },
				{ id: "vm31ccy", header: "Currency", adjust: "header"}, // editor:"text", /*wrt_editability*/ },
				{ id: "vm31ac", header: "Total", adjust: true, css:"a_right", format:webix.Number.numToStr(amtfmt)}, // , editor:"text" /*wrt_editability*/ }
				{ id: "vm31ab", header: "Total (EUR)", adjust: true, css:"a_right", format:webix.Number.numToStr(amtfmt) }, //, editor:"text", /*wrt_editability*/ }
				{ id: "vm31fx", header: "Hist. FX", adjust: true, css:"a_right", format:webix.Number.numToStr(fxfmt)} //, editor:"text", /*wrt_editability*/ }
			]
		}},
		{ header:"TOTALS IN BASE CURRENCY (EUR), BY DATE", collapsed:true, body: {
			view:"datatable",	select: "cell", id: "vm41rb", url: "/member/report/bybcydt",
			columns: [
				{ id: "vm31dt", header: "Date", adjust: true}, // format:webix.Date.dateToStr("%Y-%m-%d") },
				{ id: "vm31ab", header: "Total (EUR)", adjust: true, css:"a_right", format:webix.Number.numToStr(amtfmt) } //, editor:"text", /*wrt_editability*/ }
			]			
		}},
		{ header:"DIFF IN BASE CURRENCY (EUR), BY DATE", collapsed:true, body: {
			view:"datatable",	select: "cell", id: "vm41rbd", url: "/member/report/bybcydtvar",
			columns: [
			{ id: "vm31dt", header: "From Date", adjust: true }, // , format:webix.Date.dateToStr("%Y-%m-%d") },
			{ id: "vm31ab", header: "Total (EUR)", adjust: true, css:"a_right", format:webix.Number.numToStr(amtfmt) }, //, editor:"text", /*wrt_editability*/ }
			{ id: "vm31fs1", header: ""},
			{ id: "vm31dtz", header: "To Date", adjust: true }, // , format:webix.Date.dateToStr("%Y-%m-%d") },
			{ id: "vm31abz", header: "Total (EUR)", adjust: true, css:"a_right", format:webix.Number.numToStr(amtfmt) }, //, editor:"text", /*wrt_editability*/ }
			{ id: "vm31fs2", header: ""},
			{ id: "vm31abd", header: "Diff (EUR)", adjust: true, css:"a_right", format:webix.Number.numToStr(amtfmt) }, //, editor:"text", /*wrt_editability*/ }
			{ id: "vm31abp", header: "Diff%", adjust: true, css:"a_right", format:webix.Number.numToStr(amtfmt) }, //, editor:"text", /*wrt_editability*/ }
			//{ id: "vm31abzfzfx", header: "Total|noFX (EUR)", adjust: true, format:webix.Number.numToStr(amtfmt) }, //, editor:"text", /*wrt_editability*/ }
			{ id: "vm31abdfzfx", header: "Diff|noFX (EUR)", adjust: true, css:"a_right", format:webix.Number.numToStr(amtfmt) }, //, editor:"text", /*wrt_editability*/ }
			{ id: "vm31abpfzfx", header: "Diff|noFX%", adjust: true, css:"a_right", format:webix.Number.numToStr(amtfmt) } //, editor:"text", /*wrt_editability*/ }
			]
		}},
		{ header:"one-month VaR, BY CURRENCY (LAST DATE ONLY)", collapsed:false, body: {
			view:"datatable",	select: "cell", id: "vm41rav", url: "/member/report/byccydtz",
			columns: [
				{ id: "vm31dt", header: "Date", adjust: true }, // , format:webix.Date.dateToStr("%Y-%m-%d") },
				{ id: "vm31ccy", header: "Currency", adjust: "header"}, 
				{ id: "vm31ac", header: "Total", adjust: true, css:"a_right", format:webix.Number.numToStr(amtfmt)}, 
				{ id: "vm31ab", header: "Total (EUR)", adjust: true, css:"a_right", format:webix.Number.numToStr(amtfmt) },
				{ id: "var_1m_95_200", header: "VaR (EUR)", adjust: true, css:"a_right", format:webix.Number.numToStr(amtfmt) },
				{ id: "var_1m_95_200pct", header: "VaR%", adjust: true, css:"a_right", format:webix.Number.numToStr(amtfmt) }
			]
		}},		
		]
		/*
		on:{
			onBeforeLoad: function(){ this.showProgress({ type:"icon", delay:300});},
			onAfterLoad: function(){ this.hideProgress();}
		} */
};
//]};


var right_views = { id:"rightviews",
	//view:"scrollview",
	//scroll:"xy",
	//body:{
	cols: [
		{ id: "vm10", hidden:true, type:"wide", rows: [vm10_sessionmgmt_wrap]},
		//{ id: "vm21", view:"scrollview", hidden:true, type:"wide", scroll:"auto", body: vm21_dates},
		{ id: "vm21", hidden:true, type:"wide", rows: [vm21_dates_addform_wrap, vm21_dates_table_wrap]},
		{ id: "vm22", hidden:true, type:"wide", rows: [vm22_accounts_addform_wrap, vm22_accounts_table_wrap]},
		{ id: "vm31", hidden:true, type:"wide", rows: [vm31_balances_exportform_wrap, vm31_balances_table_wrap]},
		{ id: "vm41", view:"scrollview", hidden:true, type:"wide", scroll:"auto", body: vm41_reports},
		//{ id: "vm41", hidden:true, type:"wide", rows: [vm41_rep1_wrap]},
		{ id: "vm52", template: "html->vm52T_contact", hidden:true},
		{ id: "vm53", template: "html->vm53T_TOS", hidden:true, scroll:"y"},
		{ id: "vm54", template: "html->vm54T_guide", hidden:true, scroll:"y"},
		{ id: "vm51", template: "html->vm51T_design", hidden:true, scroll:"xy"}
	]
	//}
};

var leftbar_wrapper_alt = {
    view:"accordion", multi:true,
    //type:"wide",
    cols:[
        { header:"menu", body:leftbar} // , width:150 }
    ]
};
var leftbar_wrapper = leftbar;

webix.ready(function(){
	
	if(true) webix.ui.fullScreen();
	
	webix.ui({
		id:"page",
		type:"space",
		cols:[ leftbar_wrapper, right_views]
		
	});
	

	showmenu("m54",false);
	
	// webix.extend($$("vm21_dates"), webix.ProgressBar);
	webix.extend($$("vm21_add_dt"), webix.ProgressBar);
	webix.extend($$("vm22_add_act"), webix.ProgressBar);
	// webix.extend($$("vm22_accounts"), webix.ProgressBar);
	
	// webix.extend($$("vm41rbd"), webix.ProgressBar);
	
	
	//webix.extend($$("rightviews"), webix.ProgressBar);
	//$$("rightviews").showProgress({ type:"icon", delay:3000});
	//..ok
	
	
	
});
