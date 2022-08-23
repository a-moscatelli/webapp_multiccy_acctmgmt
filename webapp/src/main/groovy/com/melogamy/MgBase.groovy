
package com.melogamy

import groovy.transform.CompileStatic


import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import java.net.URLEncoder
import java.text.SimpleDateFormat
import org.apache.commons.lang.RandomStringUtils //import org.apache.commons.lang3.RandomStringUtils
import java.util.Calendar
//for des:
import java.security.Key
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
//
import groovy.json.StringEscapeUtils
// https://docs.groovy-lang.org/latest/html/gapi/groovy/json/StringEscapeUtils.html
import java.nio.ByteBuffer
//import java.util.Base64

// not yet in use
// https://groovy-lang.org/objectorientation.html#_abstract_class
// Abstract classes represent generic concepts, thus, they cannot be instantiated, being created to be subclassed.
// Their members include fields/properties and abstract or concrete methods. 
// Abstract methods do not have implementation, and must be implemented by concrete subclasses.


//import org.apache.commons.lang.RandomStringUtils //import org.apache.commons.lang3.RandomStringUtils
// https://repo1.maven.org/maven2/commons-lang/commons-lang/2.6/commons-lang-2.6.jar
// copied under M:\APPS\apache-groovy\groovy-2.4.13\lib
//no:
// Unsupported major.minor version 52.0
//import org.apache.commons.lang3.RandomStringUtils
// https://repo1.maven.org/maven2/org/apache/commons/commons-lang3/3.9/commons-lang3-3.9.jar



@CompileStatic
abstract class MgBase {




	/**********************
	
		section: attributes
	
	***********************/


	boolean debugging = false
	
	final String charsetz = "UTF-8"
	
	// private cannot be seen from the child class
	// protected = public in groovy

	/**********************
	
		section: UTF-8 small File/stream <-> String
	
	***********************/

	public String mg_get_string_of_small_utf8_file_content(String file_full_pathname) {	// File2String
		return new File(file_full_pathname).getText(charsetz)
	}
	
	public String mg_get_string_from_utf8_inputstream(InputStream is) {	// iStream2String
		return is.getText(charsetz)
	}

	public String mg_get_string_from_utf8_bytes(byte[] bb) { // byte[]2String
		return new String(bb, charsetz)
	}
	

	public byte[] mg_get_utf8_bytes_from_string(String str) { // String2byte[]
		return str.getBytes(charsetz)
	}
	
	


	/**********************
	
			section: log
	
	***********************/

	public void log_error(def s) {
		String tx = get_date_for_log()
		println "$tx ERROR :MGSA:" + s
	}
	
	public void log_info(def s) {
		String tx = get_date_for_log()
		println "$tx INFO " + s
	}

	public void log_debug(def s) {
		String tx = get_date_for_log()
		if(debugging) println "$tx DEBUG " + s
	}

	/**********************
	
			section: json
	
	***********************/
	
	
	public def getObjClass(def obj) {
		// https://code-maven.com/groovy-determine-type-of-object
		// https://stackoverflow.com/questions/2060427/groovy-grails-how-to-determine-a-data-type
		// (somObject instanceof Date)
		// (somObject.getClass() == Date)
		// not: obj.getClass() == "List"
		return obj.getClass()
	}
	
	public String getPrettyJsonText(def jsonObj) {	// it was prettymap
	
		// jsonObj is Map or ArrayList
		return JsonOutput.prettyPrint(JsonOutput.toJson(jsonObj))
		// for Strings, just do JsonOutput.prettyPrint
															// see https://www.tutorialspoint.com/groovy/groovy_json.htm
	}

	public String getPrettyJsonTextText(String jsonText) {

		return JsonOutput.prettyPrint(jsonText)
															// see https://www.tutorialspoint.com/groovy/groovy_json.htm
	}
	
	public String getJsonText(def jsonObj) {
	
		// jsonObj is Map or ArrayList
		return JsonOutput.toJson(jsonObj)
															// see https://www.tutorialspoint.com/groovy/groovy_json.htm
	}
	
	
	
	
	
	
	public def getJsonObj(String jsonText) {
		
		// PERFORMANCE: for(int i=0;i< 100,000,000; i++) { JsonSlurper jsonslurper = new JsonSlurper()} // tot ms:1749

		// returned jsonObj is Map or ArrayList
		// https://docs.groovy-lang.org/latest/html/gapi/groovy/json/JsonSlurper.html
		// https://docs.groovy-lang.org/latest/html/gapi/groovy/json/JsonParserType.html
		JsonSlurper jsonslurper = new JsonSlurper()
		def jsonObj = jsonslurper.parseText(jsonText)
		// Map & Arraylist
		return jsonObj
															// see https://www.tutorialspoint.com/groovy/groovy_json.htm
	}
	
	public List getJsonAsList(String jsonText) {
		JsonSlurper jsonslurper = new JsonSlurper()
		Object object = jsonslurper.parseText(jsonText)
		assert object instanceof List
		return object
															// see https://www.tutorialspoint.com/groovy/groovy_json.htm
	}
	public Map getJsonAsMap(String jsonText) {
		JsonSlurper jsonslurper = new JsonSlurper()
		Object object = jsonslurper.parseText(jsonText)
		assert object instanceof Map
		return object
															// see https://www.tutorialspoint.com/groovy/groovy_json.htm
	}	
	
	/**********************
	
			section: text
	
	***********************/


	String escapeJava4NeoParams(String s) {
		return StringEscapeUtils.escapeJava(s)
		// qwertyu`erty` gui"ghjk" fghjk'dfghjk'   -->   qwertyu`erty` gui\"ghjk\" fghjk'dfghjk'
	}
	
	String unescapeJava4NeoParams(String s) {
		return StringEscapeUtils.unescapeJava(s)
	}
	
	
	String[] split_by_pipe(String line) {
		return line.split("\\|")
	}

	boolean contains_pipe(String line) {
		return line.indexOf("|") >= 0
	}

	String replace_pipes_with_spaces(String line) {
		return line.replaceAll("\\|"," ")
	}

	/*String get_normalized_input_text(String raw) {
		String denullified = raw==null ? "" : raw
		return denullified.trim().toLowerCase()
	}
	
	Map get_normalized_input_map(Map params) {
		for (String k : params.keySet()) {
			params[k] = get_normalized_input_text(params[k] as String)
		}
		return params
	} */

	/*
	boolean isValidEmailAddress(String email) {
		try { email = getSanitized(email,'email','EU') } catch(e) { return false}
		return true
	}
	*/
	
	/**********************
	
			section: entity string normalization / sanitization
	
	***********************/
	
	String getSanitized(String entity_name, String entity_type, String exception_type_when_null) { // never returns "", only null
		// idempotent. returns a normalized string or exception if format is not valid
		
		boolean empty_ename = entity_name=="" || entity_name==null
		boolean null_extype = exception_type_when_null==null
		boolean good_extype = exception_type_when_null in ['ED','EU','ES']
		boolean x_extype = !null_extype && !good_extype
		String temp
		if(x_extype) throw new Exception("ED:LOG_eyj:invalid exception_type_when_null:$exception_type_when_null")
		if(empty_ename) {
			if(good_extype) throw new Exception("$exception_type_when_null:$entity_type:void entity_name")
			return null
		}
		entity_name = entity_name.trim()
		switch(entity_type) {
			
			
			case 'short_str':
						// see define M4_MIN_RECOVQ_LEN
						if(entity_name.length() < 3) throw new Exception("EU:$entity_type:LOG_jLs:short")
						return entity_name
			case 'sec_role':
						return entity_name.toUpperCase()
			case 'sec_unm': 
						// see define M4_MIN_USERNAME_LEN
						if(entity_name.length() < 6) throw new Exception("EU:$entity_type:LOG_jLu:short")
						return entity_name.toLowerCase()
			case 'sec_pwd': 
						// see define M4_MIN_PASSWORD_LEN
						// see define M4_MIN_RECOVA_LEN
						if(entity_name.length() < 8) throw new Exception("EU:$entity_type:LOG_jLp:short")
						return entity_name
			case 'sec_pwd_long': 
						// see define M4_MIN_PASSWORD_LEN
						// see define M4_MIN_RECOVA_LEN
						if(entity_name.length() < 20) throw new Exception("EU:$entity_type:LOG_jLp:short")
						return entity_name
			case 'dt_yaml': 
						// see define M4_MIN_PASSWORD_LEN
						// see define M4_MIN_RECOVA_LEN
						//if(entity_name.length() < 20) throw new Exception("EU:$entity_type:LOG_jLp:short")
						String supported_biz_format1 = "yyyy-MM-dd HH:mm"
						// 2014-3-4 20:00 will be also accepted
						//String supported_biz_format2 = "dd-MM-yyyy HH:mm"
						try {
							Date date = new SimpleDateFormat(supported_biz_format1).parse(entity_name)
						} catch(e) {
							throw new Exception("EU:$entity_type:LOG_YdT:unsupported format [$entity_name][$supported_biz_format1]")
						}
						return entity_name
			case 'email':
						temp = entity_name.toLowerCase()
						// https://owasp.org/www-community/OWASP_Validation_Regex_Repository
						// [^[a-zA-Z0-9_+&*-]+(?:\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\.)+[a-zA-Z]{2,7}$]
						def regexStr = /[a-zA-Z0-9_+&*-]+(?:\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\.)+[a-zA-Z]{2,7}/
						//def regexStr_beforemay2020 = /[a-zA-Z0-9.'_%+-]+@[a-zA-Z0-9.-]+\.[A-Za-z]{2,4}/
						
						if (temp.matches(regexStr)) return temp
						throw new Exception("EU:$entity_type:LOG_jfm:invalid format")
						/* try { InternetAddress emailAddr = new InternetAddress(email); emailAddr.validate();
						} catch (AddressException ex) {	result = false;	} */
						break
						
			/*
			case 'sec_role/optional': 
			case 'sec_username/optional': 
			case 'sec_password/optional': 
			
			case 'sec_role/mandatory': 
			case 'sec_username/mandatory': 
			case 'sec_password/mandatory': 
			*/
		}
		throw new Exception("ED:LOG_4ju:invalid entity_type:$entity_type")
	}	

	/**********************
	
			section: random
	
	***********************/

	String getNewSN() {
		
		// returns SN for NEO4J randomAlphanumeric. base62. 26+26+10=62.
		// con z=7 ho 62^7 = 3.5T combinazioni; eg E5APOgK
		// con z=8 ho 62^8 = 218T combinazioni; (in use) 2E14 = 100 * 1M * 1M
		
		// BTW: last-minute, GRAILS CYPHER SERVICE ci appende ulteriormente un carattere per classe.

		// you can also check https://neo4j.com/docs/labs/apoc/3.5/misc/text-functions/#text-functions-random-string

		int TOTAL_STRLEN_BESN = 9
		return RandomStringUtils.randomAlphanumeric(TOTAL_STRLEN_BESN-1)	// 1 is the class suffix
	}
	
	
	String getSalt() {							// from utilService
		// generate a salt, a 20-char random
		return RandomStringUtils.randomAlphanumeric(20)
	}

	String getRandomAlphanum(int size) {							// from utilService
		// generate a salt, a 20-char random
		return RandomStringUtils.randomAlphanumeric(size)
	}

	
	// https://en.wikipedia.org/wiki/Ascii85

	String uuid() {									// from utilService
		UUID.randomUUID().toString()
		// https://stackoverflow.com/questions/10687505/uuid-format-8-4-4-4-12-why
		// https://www.tutorialspoint.com/java/util/java_util_uuid.htm
	}
	
	String uuid_b64safe() {
		// https://en.wikipedia.org/wiki/Base64#Implementations_and_history		base64url URL- and filename-safe (RFC 4648 §5)
		// https://stackoverflow.com/questions/772802/storing-uuid-as-base64-string
		UUID uuid = UUID.fromString(uuid())
		ByteBuffer bb = ByteBuffer.wrap(new byte[16])
		bb.putLong(uuid.getMostSignificantBits())
		bb.putLong(uuid.getLeastSignificantBits())
		String uu = bb.array().encodeBase64().toString()
		// 90ujSNC/Tx6cZ0sZMVGMcg==
		// bxB3yC+lRkCDOv3FJQXYgA==
		// this is not so good for params
		// http://localhost:8080/first/testid?id=90ujSNC/Tx6cZ0sZMVGMcg==&id2=bxB3yC+lRkCDOv3FJQXYgA==
		// = e / are preserved, + is LOST
		// so: base64url URL- and filename-safe (RFC 4648 §5)
		
		uu =	uu.replaceAll("\\+","-").replaceAll("/","_")
		
		// fiC6P7Y0S5mGK//ZZ4umvA== -> fiC6P7Y0S5mGK__ZZ4umvA==
		// EIUtV7LMSH6+WARZ+XS9dA== -> EIUtV7LMSH6-WARZ-XS9dA==
		// HXl+Her4S4WTX/Kjm4jWFA== -> HXl-Her4S4WTX_Kjm4jWFA==
		
		return uu.take(22)	// the padding "==" will always be there so it is removed.
	}
	
	
	
	/**********************
	
			section: security / encode / decode
	
	***********************/


	
	 
	
	
	String getBase64encryptedText(String text) {	// from utilService
		// DES+ base64

		String key = System.getenv().get("MG_AES_KEY")
		key = key.take(16)
		
		// create cipher and key:
		Key aesKey = new SecretKeySpec(key.getBytes(),"AES")	// "UTF-8" ??
		Cipher cipher = Cipher.getInstance("AES")
		
		// encrypt:
		cipher.init(Cipher.ENCRYPT_MODE,aesKey)
		byte[] encrypted = cipher.doFinal(text.getBytes())		// "UTF-8" ??
		//println new String(encrypted)
				
		// storing b64:
		String encrypted_b64_s = encrypted.encodeBase64().toString()
		//println "b64: " + encrypted_b64_s
		
		return encrypted_b64_s
		
}


	String getBase64decryptedText(String encrypted_b64_text) {		// from utilService
		// DES+ base64
		
		String key = System.getenv().get("MG_AES_KEY")
		key = key.take(16)
		
		// create cipher and key:
		Key aesKey = new SecretKeySpec(key.getBytes(),"AES")		// "UTF-8" ??
		Cipher cipher = Cipher.getInstance("AES")
		
		// loading b64:
		byte[] encrypted = encrypted_b64_text.decodeBase64()
				
		// decrypt:
		
		cipher.init(Cipher.DECRYPT_MODE,aesKey)
		String decrypted = new String(cipher.doFinal(encrypted))

		return decrypted
		
}	

	String getAuthHeader(String uname_colon_pwd) {
		
		String b64 = uname_colon_pwd==null ? "" : uname_colon_pwd.bytes.encodeBase64().toString()
		// used to do a basic auth http request
		"Basic " + b64
	}	
	

	/*
	String getBase64ofBase16(String b16)
	typically b16 = ObjectId().str --- mongodb client. so you go from 24-char to 16-char
	byte[] ba = b16.decodeHex()
	return ba.encodeBase64().toString()
	
	String getBase16ofBase64(String b64) {
	b64 will have A-Za-z0-9 and + and /	, and = which may be used for padding. any blank / NL is ignored.
	byte[] ba = b64.decodeBase64()
	return ba.encodeHex()
	*/	
	
	/**********************
	
			section: date / time
	
	***********************/
	
	
	
	
	long epoch_ms() {
		
		// ms since the UNIX EPOCH
		// this epoch_ms / 1000 / 60 /60 /24 /365 will be around 50 (years) in 2020
		
		return new Date().getTime()
	}
	
	// https://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html
	
	String get_human_date() {
		Date now = new Date()
		SimpleDateFormat formatter = new SimpleDateFormat('yyyy-MM-dd HH:mm:ss')
		return formatter.format(now)
	}
	
	String get_month_year(long epoch) {
		Date now = new Date(epoch)
		SimpleDateFormat formatter = new SimpleDateFormat('MMMMM-yyyy')
		return formatter.format(now)
	}

	String get_date_for_log() {
		Date now = new Date()
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS")
		return formatter.format(now)
	}
	
	
	

	String getYMDEpoch(long epoch, boolean safe) {		// empty string or Exception
		String rt = ''
		try {
			Date now = new Date(epoch)
			SimpleDateFormat formatter = new SimpleDateFormat('dd-MM-yyyy')
			rt=formatter.format(now)
		} catch (e) {
			log_error("LOG_Hyi:epoch:$epoch:"+e)
			if(!safe) throw e
		}
		return rt
	}
	
	
	
	String getGuiDate(String gui_dt) {
		// example 17 June, 2020 06:14 PM
		// https://rmr.fandom.com/wiki/Groovy_Date_Parsing_and_Formatting
		Date date = new SimpleDateFormat("dd MMMMM, yyyy K:mm a").parse(gui_dt)
		SimpleDateFormat formatter = new SimpleDateFormat('yyyy-MM-dd HH:mm')
		return formatter.format(date)
	}
	
	
	
	Date getHHhoursfromNow(int hoursToAdd) {	// from utilService
		// returns nn hours from now

	// https://www.tutorialspoint.com/javaexamples/date_add_time.htm
	// https://www.mkyong.com/java/java-date-and-calendar-examples/
	// https://examples.javacodegeeks.com/core-java/util/calendar/add-subtract-hours-from-date-with-calendar/
		
		//	http://stackoverflow.com/questions/3581258/adding-n-hours-to-a-date-in-java
		
		//Date newDate = DateUtils.addHours(nowdt, hh);
		
		Calendar c = Calendar.getInstance();
		c.add(Calendar.HOUR, hoursToAdd);
        Date date =  c.getTime();
		return date
	}
	
	Date get_date_plus_ndays(Date dtref, int ndays) {
		Calendar c = Calendar.getInstance()
		c.setTime(dtref)
		c.add(Calendar.DATE, ndays)
		return c.getTime()
	}
	
	String get_datestr_floor_FirstOfMonth(String sdref) {	// YYYY-MM-DD
		// window reframing to improve cache hits.
		return sdref.take(4+1+2+1) + "01"
	}
	
	Date get_date_floor_FirstOfMonth(Date dtref) {
		String dtref_str = get_datestr_of_date(dtref,11)
		String dtfloor_str = get_datestr_floor_FirstOfMonth(dtref_str)
		return get_date_of_datestr(dtfloor_str,11)
	}
	
	Date get_date_of_datestr(String yyyymmdd, int fmt) {
		assert fmt == 11
		SimpleDateFormat formatter = new SimpleDateFormat('yyyy-MM-dd')
		return formatter.parse(yyyymmdd)
	}
	
	String get_datestr_of_date(Date yyyymmdd, int fmt) {
		assert fmt == 11
		SimpleDateFormat formatter = new SimpleDateFormat('yyyy-MM-dd')
		return formatter.format(yyyymmdd)
	}
	
	
	
	
	/**********************
	
			section: http
	
	***********************/


	public Map get_EnvUrl_Parts(String uri) {
		
		// regex of supported: 		protocol:// 	(user:pwd@)?		host (:port)?	( / pathname) ?  (? params)?
		// given https://u:p@host
		// returns [ https://host - u:p - Basic hash(u:p) ]
		// uri may also be: http://localhost:7474
		// neo4j:vds:2015 e' OK. ???
		
		// now I force http proto because URL do not support special ones like redis:// or postgres://
		// no: String proto = uri.getProtocol()

		//println "uri uri uri"
		//println uri
		//println uri.indexOf(":")
		String uri_http_s = "http" + uri.substring(uri.indexOf(":"))
		URL uri_forced_http = new URL(uri_http_s)
		
		
		// test:   M:\DEV\GRAILS\mg3a\m4\src\groovyscripts
		// M:\APPS\apache-groovy\groovy-2.4.13\bin\groovy.bat utils.groovy parseurl DATABASE_URL

		// https://paas:06c3682544320c6f7b3949c46b9f032b@kili-eu-west-1.searchly.com
		// https://kili-eu-west-1.searchly.com
		
		String getUserInfoz = uri_forced_http.getUserInfo()
		String getUserInfoz0 = getUserInfoz ? getUserInfoz.split(":")[0] : ""
		String getUserInfoz1 = getUserInfoz ? getUserInfoz.split(":")[1] : ""
		String protocol = uri.substring(0,uri.indexOf(":"))	// like http (without colon)
		String getBasicAuth = getAuthHeader(getUserInfoz)


		Map rtmap = [
			ucolonpwd:	getUserInfoz,
			user:		getUserInfoz0,
			pwd:		getUserInfoz1,
			basicauth:	getBasicAuth,
			host:		uri_forced_http.getHost(),
			port:		uri_forced_http.getPort(),
			file:		uri_forced_http.getFile(),	// (will start with SLASH)
			withoutup:	uri.replaceAll("$protocol://$getUserInfoz@","$protocol://"),
			withup:		uri,
			uri:		uri
		]
		return rtmap
	}	

		
	
	String getUrlQuery(Map params) {
		List thequerylist = []
		// how about duplicated entries ??? will this work ???
		// https://www.w3schools.com/java/java_hashmap.asp
		for (String k : params.keySet()) {
			String v = params.get(k)
			v = URLEncoder.encode(v, charsetz)
			thequerylist.push(k +"=" +v)
		}

		String thequerystring = thequerylist.join('&')
		return thequerystring
	}
	
	
	HttpURLConnection http_open(String opening_url) {
		// https://www.codejava.net/java-se/networking/how-to-use-java-urlconnection-and-httpurlconnection
		URLConnection cn =  new URL(opening_url).openConnection()
		return (HttpURLConnection) cn //new URL(opening_url).openConnection()
	}
	
	void set_common_urlconn_props(HttpURLConnection huc, String method, String basic_auth) {
		huc.setRequestMethod(method)
		huc.setDoInput(true)
		huc.setDoOutput(method=="POST" || method=="PUT")
				// java.net.ProtocolException: cannot write to a URLConnection if doOutput=false
		huc.setRequestProperty("Authorization",basic_auth)
		huc.setConnectTimeout(60000)	// 60 seconds
		huc.setReadTimeout(60000)		// 60 seconds
		huc.setRequestProperty("Content-Type", "application/json; charset=$charsetz")	// ; charset=UTF-8 just added
		huc.setRequestProperty("Accept", "application/json; charset=$charsetz")
		huc.setRequestProperty("X-Stream", 'true')
	}
				// https://stackoverflow.com/questions/1051004/how-to-send-put-delete-http-request-in-httpurlconnection
	

	private void http_pxxt(HttpURLConnection huc, String postmessagemap_s, String basic_auth, String method) {
		assert method=="POST" || method=="PUT"
		set_common_urlconn_props(huc,method,basic_auth)
		OutputStream httpos = huc.getOutputStream()
		byte[] bytes = postmessagemap_s.getBytes(charsetz)
		httpos.write(bytes)
		httpos.flush()
		httpos.close()	// I am closing the output stream, not the connection.
		log_debug("LOG_ESX:httpos.closed:/")
	}

	void http_post(HttpURLConnection huc, String messagemap_s, String basic_auth) {

		http_pxxt(huc, messagemap_s, basic_auth, "POST")
	}
	// was HttpURLConnection before compilestatic
	void http_put(HttpURLConnection huc, String messagemap_s, String basic_auth) {

		http_pxxt(huc, messagemap_s, basic_auth, "PUT")

		// https://en.wikipedia.org/wiki/List_of_HTTP_status_codes#4xx_Client_errors
		// 201 created
	}
	
	Map http_get_resp(HttpURLConnection huc) {
		int http_rc = huc.getResponseCode()
		// https://en.wikipedia.org/wiki/List_of_HTTP_status_codes
		// 201 = created (after PUT)
		// 200 = found  (after GET)
		// 200 = updated, created a new version (after PUT)
		// 400 = Bad Request
		// 401 = unauthorized
		// 404 = not found
		// 500 internal server error
		// log_info "LOG_er7:cmd:$method idx:$indexname status:$http_rc :"

		// from https://stackoverflow.com/questions/25692515/groovy-built-in-rest-http-client

		//println http_rc
		//if(http_rc==404) return [http_err:true, http_rc:http_rc]
		//if(http_rc>=400) return [http_err:true, http_rc:http_rc]
		if(http_rc>=300) return [http_err:true, http_rc:http_rc]		// since 24jun2020
		log_debug("LOG_39d:http_rc:$http_rc")
		InputStream is = huc.getInputStream()
		/*
		byte[] isb = is.getBytes()
		String resp_s = getStringFromBytes(isb) // huc.getInputStream().getText("UTF-8")
		*/
		//log_info("LOG_38d:is:"+is)
		String resp_s = mg_get_string_from_utf8_inputstream(is)
		if(resp_s) log_info("LOG_38e:is(resp_s):"+resp_s.length()) else log_info("LOG_38e:is:"+resp_s)
		// str.getBytes();
		//println resp_s
		def jsonresp = getJsonObj(resp_s)
		is.close() // since 24jun2020
		log_debug("LOG_ESX:httpis.closed:/")
		
		return [http_err:false, http_rc:http_rc, resp:jsonresp]
	}
	

	
	//abstract def abstractMethod()

}