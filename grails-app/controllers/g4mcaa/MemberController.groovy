package g4mcaa

// 	https://multicurrency-anac.herokuapp.com/

/****************
https://docs.webix.com/desktop__loadingerror.html
	By default if an error occurs, there appears a webix.message with details being shown in console.
	This functionality works only in the uncompressed code version while the minified version doesn't show any error messages.

https://docs.webix.com/api__env_mobile_other.html
https://blog.webix.com/how-to-build-mobile-web-app/

https://docs.webix.com/datatable__columns_configuration.html#widthsofcolumns

https://docs.webix.com/desktop__accordion.html	multi:true multi:false multi:"mixed"

https://docs.webix.com/desktop__spreadsheet.html

https://docs.webix.com/desktop__data_validation.html
https://docs.webix.com/desktop__serverside.html
https://docs.webix.com/api__refs__dataprocessor.html
https://docs.webix.com/datatable__controls.html
https://docs.webix.com/helpers__ajax_operations.html
https://docs.webix.com/desktop__dataprocessor.html			EEE status "success"/"error"/"invalid"		id and newid
https://docs.webix.com/helpers__top_ten_helpers.html
https://docs.webix.com/desktop__data_loading.html
https://docs.webix.com/datatable__loading_data.html#loadingscreen
https://docs.webix.com/api__toc__ui_mixins.html
https://docs.webix.com/desktop__serverside.html
https://docs.webix.com/desktop__editing.html#editortypes
https://docs.webix.com/desktop__dimensions.html
https://docs.webix.com/desktop__working_with_dates.html
https://docs.webix.com/desktop__working_with_dates.html
https://docs.webix.com/mobile_calendar__date_format.html
https://docs.webix.com/helpers__number_formatting_methods.html

https://neo4j.com/docs/cypher-manual/3.5/syntax/parameters/
https://neo4j.com/docs/http-api/3.5/
https://neo4j.com/developer/java/
https://devcenter.heroku.com/articles/graphenedb#using-with-java-and-neo4j-bolt-driver-for-java
https://docs.webix.com/desktop__progress.html#settingprogressbaricon

https://bertramdev.github.io/grails-asset-pipeline/guide/usage.html

***/

//import org.grails.plugins.csv.CSVMapReader
//import com.xlson.groovycsv.CsvParser
import static com.xlson.groovycsv.CsvParser.parseCsv


class MemberController {

	def graphService
	def utilService
	boolean method_enabled=true
	
	def index() {
		
		log.info "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~"
		log.info "LOG_e8y:index()"
		log.info "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~"
		
		// http://docs.grails.org/latest/guide/conf.html#environments
		
		// log.info "LOG_HYE:" + grailsApplication.config.getProperty('grails.env') // null
		log.info "LOG_HYE:" + grails.util.Environment.current // "DEVELOPMENT"
		boolean isdev = grails.util.Environment.current == grails.util.Environment.DEVELOPMENT
		
		if(session.code == null) {
			
			Map request_source = [
				// https://stackoverflow.com/questions/2140859/how-do-you-get-client-ip-address-in-a-grails-controller
				RemoteAddr: 	request.getRemoteAddr(),
				XForwardedFor: 	request.getHeader("X-Forwarded-For"),
				ClientIP: 		request.getHeader("Client-IP"),
				UserAgent: 		request.getHeader("User-Agent")
			]
			log.info "User_agent:" + request_source
			// in DEV:
			// User agent: [UserAgent:Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:81.0) Gecko/20100101 Firefox/81.0, 
			// RemoteAddr:0:0:0:0:0:0:0:1, XForwardedFor:null, ClientIP:null]
			// in PROD:
			// User agent: [UserAgent:Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:81.0) Gecko/20100101 Firefox/81.0,
			// RemoteAddr:58.182.79.97, XForwardedFor:null, ClientIP:null]
			//
			// https://www.geolocation.com/?ip=58.182.79.97#ipresult
			//https://rapidapi.com/blog/ip-geolocation-api/
			/*
			 "latitude": "1.28967",
			"longitude": "103.85007",
			"zip_code": "179431",
			*/
			
			session.code = utilService.session_c()
			session.request_source = request_source.toString()
		}
		render(view: "view1", model: [isdev:isdev, session_code:session.code, session_dob: session.session_dob, session_last: session.session_last])
	}
	def mockerror() {
		render(status:500)
	}
	def mocknotfound() {
		render(status:404)
	}
	def logout() {
		// src: http://docs.grails.org/3.1.1/ref/Servlet%20API/session.html
		log.info "User agent: " + request.getHeader("User-Agent")
        //log.info "User agent: " + request.getHeader("User-Agent")
        session.invalidate()
        redirect(action: "index")
    }

	def admin() {
		
		String jsontxt = '{"rc":401,"rcs":"Unauthorized"}'	// 501 Not Implemented / 401 Unauthorized 
		boolean is_admin_account = false
		
		if(session.code) {
			if(session.code.size() == utilService.sessionLen()) {
				if(session.code == System.getenv("ADMINACCOUNT")) {
					is_admin_account =  true
				}
			}
		}
		if(is_admin_account) {
			List retarray = graphService.user_b_adminonly()
			List retarray_enriched = []
			retarray.each { user ->
				Map user_enriched = [:] + user	// I create a clone that is not immutable and enrichable
				["uudob","uulastd","accounts_lastd","balances_lastd"].each { userepoch ->
					if(user[userepoch]) {
						user_enriched.put(userepoch+"_human",new Date(user[userepoch]).toString())
					}
				}
				retarray_enriched.add(user_enriched)
			}
			jsontxt = utilService.simpleutils.getJsonText( [ rc:200, userstats: retarray_enriched ])
		}
		
		render( contentType: "application/json", encoding: "UTF-8", text:jsontxt )	
	}
	
	def login() {	// intended to create new session with a given old session code
		if(request.post && params.account) {
			String account = params.account.trim()
			if(account.size()==utilService.sessionLen()) {
				//session.invalidate() // new
				session.code = account
				log.info "LOG_HUL:session code login:" + account
				Map ret = graphService.user_u(account)
				//log.info "LOG_SUL:session:" + ret
				if(ret) {
					session.session_dob = ret.session_dob
					session.session_last = ret.session_last
				}
			} else {
				/// TBC
				log.error "LOG_EUL:session code login:" + account
			}
		}
		/*
		if(request.post && params.account) {
			session.invalidate() // new
			Map ret = graphService.user_u(params.account.trim())
			if(ret) {
				session.code = params.account.trim()
				log.info "session code sz:" + session.code.size()
				session.session_dob = ret.session_dob
				session.session_last = ret.session_last
			}
		} */
		redirect(action: "index") //, model: [session_code:session.code, session_dob: session.session_dob, session_last: session.session_last]) // params: [id: "b_n"]
	}
	
	def date() {
		String ret='{"status":"error"}'
		log.info "LOG_D8e:date() - user: " + session.code + " - params: " + params + " - method: " + request.method
		
		//Thread.sleep(5000)
		
		if(request.get) {	// keep it uncaught
			List neor = graphService.date_r(session.code)
			ret=utilService.simpleutils.getJsonText(neor)
			// assert false // TESTED
		}
			if(request.post && params.webix_operation=="delete") {
				ret = graphService.date_d(params,session.code)
			}
		try {
			if(request.post && params.webix_operation=="update") {
				ret = graphService.date_u(params, session.code)
			}
			if(request.post && params.webix_operation=="insert") { assert false }
			if(request.post && params.webix_operation=="insert_custom") {
				String today_s = utilService.simpleutils.get_human_date().take(4+1+2+1+2)
				String guidate_s = params.dt.take(4+1+2+1+2)
				//log.info guidate_s
				//log.info today_s
				boolean acceptable = (guidate_s <= today_s)
				ret = acceptable ? graphService.date_c(params, session.code, session.request_source) : '{"status":"error"}'
			}
			log.info "LOG_D8g:date() json ret sz: " + ret.size()		
			log.debug "LOG_D8g:date() json ret: $ret"		
		} catch(e) {
			log.error "LOG_D8h:date(): " + e
			ret = '{"status":"error"}'
		}
		render(contentType: 'text/json', text:ret)
	}
	
	def currencies() {
		String ret = utilService.simpleutils.getJsonText(utilService.getCurrencies())
		//String ret = '["AUD","BGN","BRL","CAD","CHF","CNY","CZK","DKK","EUR","GBP","HKD","HRK","HUF","IDR","ILS","INR","ISK","JPY","KRW","MXN","MYR","NOK","NZD","PHP","PLN","RON","RUB","SEK","SGD","THB","TRY","USD","ZAR"]'
		render(contentType: 'text/json', text:ret)
	}
	

	/*
	def date_d() {
		log.info "LOG_ekh:date_d()" + " user: " + session.code
		String ret = graphService.date_d(params,session.code)
		render(contentType: 'text/json', text:ret)
	}*/

/////////////////////

	def account() {
		String ret='{"status":"error"}'
		log.info "LOG_A8e:account() - user: " + session.code + " - params: " + params + " - method: " + request.method
		if(request.get) {
			ret = graphService.account_r(session.code)
		}
		try {
			if(request.post && params.webix_operation=="insert_custom") ret = graphService.account_c(params, session.code, session.request_source)
			if(request.post && params.webix_operation=="delete") ret = graphService.account_d(params,session.code)
			if(request.post && params.webix_operation=="update") ret = graphService.account_u(params, session.code)
			// if(request.post && params.webix_operation=="insert") ret = '{"id":0, "status": "success"}'
			
			//if(params.id=="d") {
				// ret = graphService.date_d(params,session.code)
			//}
			log.info "LOG_A8P: account() json ret sz: " + ret.size()		
			log.debug "LOG_A8P: account() json ret: $ret"				
		} catch(e) {
			log.error "LOG_A8h: account(): " + e
		}
		render(contentType: 'text/json', text:ret)
	}


	
	def balance() {
		String ret='{"status":"error"}'
		log.info "LOG_B8e:balance() - user: " + session.code + " - params: " + params + " - method: " + request.method
		try {
			if(request.get) ret = graphService.balance_r(session.code)
			if(request.post && params.webix_operation=="customupdate") ret = graphService.balance_u(params, session.code)
				//if(params.webix_operation=="delete") 
				//if(params.webix_operation=="update")
				//if(params.webix_operation=="insert")
		
		} catch(e) {
			log.error "LOG_B8h: balance(): " + e
		}
		render(contentType: 'text/json', text:ret)
	}
		
	
	
	def report() {
		String ret='{"status":"error"}'
		log.info "LOG_R8e:report() - user: " + session.code + " - params: " + params
		try {
			/*switch(params.id) {
				case "byccydt":
				case "bybcydt":
				case "byddt":
			}*/
			long da = utilService.simpleutils.epoch_ms()
			ret = graphService.report_r(session.code,params.id)
			long dza = utilService.simpleutils.epoch_ms() - da
			log.info "LOG_RTE:report() ${params.id} ms $dza"
			
		} catch(e) {
			log.error "LOG_R8h: report(): " + e
		}
		render(contentType: 'text/json', text:ret)
	}
	
	
	def upload() {
		// https://stackoverflow.com/questions/9519767/importing-a-file-multipartfile-and-the-process-java-grails
		log.info "upload()"
		println params
		println request.getFile('files')
		String sret='{"status":"server"}'
		//println params
		def fpic = request.getFile('upload')
		/*if (fpic.empty) {
			println "empty!"
			//assert 2==3
		}*/


		int sz=fpic.getSize()	// 77128 = 77KB
		log.info "sz:$sz"
		def data_iterator
		Map dates = [:]
		Map date_accounts = [:]
		Map accounts = [:]
		List balances = []

		if(sz > 100000) {
			sret='{"status":"error"}'
			data_iterator = []
		} else {
		
			InputStream is = fpic.getInputStream() // new BufferedInputStream(fpic.getInputStream())
			String csv_content = is.getText("utf-8")
			/*
			Date,Account,Amount,Currency,Hist. FX,
			2020-10-12,a,11,AUD,1.64,
			2020-10-12,e,12,EUR,1.00,
			*/
			data_iterator = parseCsv(csv_content, separator: ',', readFirstLine: false)
			
		}
		int LN=1
		int dt_err=0
		int amt_err=0
		int ccy_err=0
		int actnm_err=0
		int actpk_err=0
		int dtact_err=0
		int line_1st_err=0
		String type_1st_err
		
		for (line in data_iterator) {
			log.debug "$LN : " + line
			LN++
			// line = line*.trim()
			String datek = line[0].trim()
			String acctk = line[1].trim()
			String amtv  = line[2].trim()
			if(amtv=="") amtv="0"
			String ccyv  = line[3].trim()
			dates[datek] = true
			boolean acct_pk_check = true
			if(!utilService.isValidInput(datek, 11)) {
				dt_err++
				if(!type_1st_err) {line_1st_err=LN; type_1st_err="date format: $datek"}
			}
			if(!utilService.isValidInput(acctk, 41)) {
				actnm_err++
				if(!type_1st_err) {line_1st_err=LN; type_1st_err="account name: $acctk"}
			}
			if(date_accounts[datek+acctk]) {
				dtact_err++
				if(!type_1st_err) {line_1st_err=LN; type_1st_err="date: $datek account: $acctk (duplicate)"}
			} else {
				date_accounts[datek+acctk]=1
			}
			if(!utilService.isValidInput(amtv, 31)) {
				amt_err++
				if(!type_1st_err) {line_1st_err=LN; type_1st_err="balance: $amtv"}
			}
			if(!utilService.isValidInput(ccyv, 51)) {
				ccy_err++
				if(!type_1st_err) {line_1st_err=LN; type_1st_err="currency: $ccyv"}
				acct_pk_check=false
			}
			if(acct_pk_check) {
				if(! accounts[acctk]) {
					accounts[acctk] = ccyv
				} else {
					if(accounts[acctk] != ccyv) {
						actpk_err++
						if(!type_1st_err) {line_1st_err=LN; type_1st_err="account: $acctk currency: $ccyv / " + accounts[acctk]}
					}
				}
			}
			balances.add([dt:datek,act:acctk,amt:amtv])
		}
			
		log.info "LOG_Hud:dates.size:" + dates.size() + ":bad_format:$dt_err"
		log.info "LOG_Hue:accounts.size:" + accounts.size() + ":bad_format:$actnm_err:bad_PK:$actpk_err"
		log.info "LOG_Huf:balances.size:" + balances.size() + ":bad_format_val:$amt_err:bad_format_ccy:$ccy_err"
		
		int tot_errors = dt_err + amt_err + actnm_err + actpk_err + ccy_err + dtact_err
		if(tot_errors>0) {
			Map mapret = [
				status : "error",
				totinvalid: tot_errors,
				firstline: line_1st_err,
				errortype: type_1st_err
			]
			sret = utilService.simpleutils.getJsonText(mapret)
			
		} else {
		
			dates.each { entry ->
				String ret = graphService.date_c([dt:entry.key], session.code, session.request_source)
			}
			accounts.each { entry ->
				Map tokens = [nm:entry.key, ccy:entry.value]
				String ret = graphService.account_c(tokens, session.code, session.request_source)
			}
			balances.each { map ->		
				Map tokens = [vm31ac:map.act, vm31dt:map.dt, vm31av:map.amt]
				String ret = graphService.balance_u(tokens, session.code)
			}
		}
		
		// reporting date: 31/7/2020, account label: hsbc sg EUR, currency: EUR,  fxrate :  1.00 , amount: 32,  amount (EUR) :  32.00 ,  amount (EUR)(fz) : , : , amt2: , fx2: , amtfx2: , amt1: , fx1: , amtfx1:
		
		// https://code-maven.com/groovy-read-csv-file
		// https://stackoverflow.com/questions/2621180/groovy-load-csv-files
		
			// https://docs.webix.com/desktop__uploader_serverside.html
		// http://guides.grails.org/grails-upload-file/guide/index.html
		render(contentType: 'text/json', text:sret)
	}

	
	def keepwarm() {
		String ret='{"status":"error"}'
		log.info "LOG_W8e: keepwarm() - user: " + session.code + " - params: " + params
		try {
			ret = graphService.keepwarm_r(session.code)
		} catch(e) {
			log.error "LOG_W8h: keepwarm(): " + e
		}
		render(contentType: 'text/json', text:ret)
	}
	
//	SPECIAL DEV STUFF	

	def export() {
		assert false
		Map cfg = [export_hist_fx_rates:1]
		String expcsv =  graphService.export_csv_SPECIAL(session.code, cfg)
		render(file: expcsv.getBytes("UTF-8"), fileName: "balances.csv")
	}
	
	def devreport() {
		assert false
		render(view:"view2", model: [session_code:session.code])
	}
	
	def devreportdata() {
		assert false
		List ret = graphService.devreportdata(session.code)
		String sret = utilService.simpleutils.getJsonText(ret)
		render(contentType: 'text/json', text:sret)
	}
	

	
	def testvar1d() {
		assert false
		Double thevar = 0.0 // utilService.get_1d95var("GBP", 200, 0.95, 100000)
		println thevar
		render(text:thevar)
	}
	
	def special() {
		assert false
		String ret = '{"status":"disabled"}'
		boolean enabled = true
		if(enabled && params.id =="deletefx") {
			assert false
			log.info "LOG_B7h:special/deletefx" + " user: " + session.code
			ret = graphService.fx_d_SPECIAL("ignore user")
		}
		if(enabled && params.id =="readfx") {
			log.info "LOG_B7h:special/readfx" + " user: " + session.code
			ret = graphService.fx_r_SPECIAL("ignore user")
		}
		if(enabled && params.id =="deleteall") {
			log.info "LOG_B7h:special/deleteall" + " user: " + session.code
			ret = graphService.all_d_SPECIAL("ignore user")
		}
		if(enabled && params.id =="countall") {
			log.info "LOG_B7h:special/countall" + " user: " + session.code
			ret = graphService.all_r_SPECIAL("ignore user")
		}
		render(contentType: 'text/json', text:ret)
	}
	
				
}
