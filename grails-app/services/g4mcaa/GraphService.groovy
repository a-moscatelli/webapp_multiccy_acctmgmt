package g4mcaa

/*
CREATE CONSTRAINT ON (dd:DATE) ASSERT dd.id IS UNIQUE
CREATE CONSTRAINT ON (dd:DATE) ASSERT dd.dt IS UNIQUE
CREATE CONSTRAINT ON (cc:CURRENCY) ASSERT cc.id IS UNIQUE
CREATE CONSTRAINT ON (cc:CURRENCY) ASSERT cc.nm IS UNIQUE
CREATE CONSTRAINT ON (uu:USER) ASSERT uu.id IS UNIQUE
CREATE CONSTRAINT ON (uu:USER) ASSERT uu.nm IS UNIQUE
*/


import java.util.Calendar
import java.text.SimpleDateFormat
import java.lang.Math

import org.neo4j.driver.*	// https://devcenter.heroku.com/articles/graphenedb#using-with-java-and-neo4j-bolt-driver-for-java
/*
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Session;
import org.neo4j.driver.Result;
import org.neo4j.driver.Transaction;
import org.neo4j.driver.TransactionWork;
import static org.neo4j.driver.Values.parameters;
*/

// https://neo4j.com/docs/cypher-manual/3.5/

// import grails.gorm.transactions.Transactional

// @Transactional
class GraphService {

	def utilService
	
	String mergeusertk1 = "WITH timestamp() as TS merge (uu:USER {nm:\$tkusn}) ON CREATE SET uu.dob=TS, uu.last=TS, uu.id=TS, uu.ip=\$tokenuip with uu, TS"
	String matchusertk1 = "WITH timestamp() as TS match (uu:USER {nm:\$tkusn}) with uu, TS"
	
	static Driver neo_driver
	//final Driver neo_driver	// cannot modify final field 'neo_driver' outside of constructor.
	static Session neo_session
	
	/*
	GraphService() {
		String graphenedbURL = System.getenv("GRAPHENEDB_BOLT_URL");
		String graphenedbUser = System.getenv("GRAPHENEDB_BOLT_USER");
		String graphenedbPass = System.getenv("GRAPHENEDB_BOLT_PASSWORD");
		String graphenedbURLHttps = System.getenv("GRAPHENEDB_URL");

		Config.ConfigBuilder builder = Config.builder().withEncryption();
		Config config = builder.build();
		//Driver driver = GraphDatabase.driver( graphenedbURL, AuthTokens.basic( graphenedbUser, graphenedbPass ), config );
		//Session session = driver.session();
		//Result result = session.run(cypher,  params)
		//neo_driver = GraphDatabase.driver( graphenedbURL, AuthTokens.basic( graphenedbUser, graphenedbPass ), config );
		neo_session = neo_driver.session();
		neo_driver = GraphDatabase.driver( graphenedbURL, AuthTokens.basic( graphenedbUser, graphenedbPass ), config );
	} */
	/*
	Error creating bean with name 'graphService': Instantiation of bean failed; ...
	Failed to instantiate [g4mcaa.GraphService]: Constructor threw exception; 
	nested exception is java.lang.NullPointerException: Cannot invoke method session() on null object
	*/
	
	List run_cypher(String cypher, Map params) {

		if(!neo_session) {

			String graphenedbURL = System.getenv("GRAPHENEDB_BOLT_URL");
			String graphenedbUser = System.getenv("GRAPHENEDB_BOLT_USER");
			String graphenedbPass = System.getenv("GRAPHENEDB_BOLT_PASSWORD");
			//String graphenedbURLHttps = System.getenv("GRAPHENEDB_URL");

			Config.ConfigBuilder builder = Config.builder().withEncryption();
			Config config = builder.build();
			//Driver driver = GraphDatabase.driver( graphenedbURL, AuthTokens.basic( graphenedbUser, graphenedbPass ), config );
			//Session session = driver.session();
			//Result result = session.run(cypher,  params)
			
			//neo_driver = GraphDatabase.driver( graphenedbURL, AuthTokens.basic( graphenedbUser, graphenedbPass ), config );
			//ho rimosso la config per usare il neo in docker.
			neo_driver = GraphDatabase.driver( graphenedbURL, AuthTokens.basic( graphenedbUser, graphenedbPass ) );
			neo_session = neo_driver.session();
		}

		Result result = neo_session.run(cypher,  params)
		//"MATCH (:Person {name: 'Tom Hanks'})-[:ACTED_IN]->(movies) RETURN movies.title AS title");
		List xr = []
		while (result.hasNext()) {
			
			//log.info "am-record:"
			Record rn = result.next()
			Map xx = rn.asMap()
			Map xc = xx.values()[0]
			// log.info "am-record:" + xc.getClass() + " - " + xc //  java.util.Collections$SingletonMap
			
			xr.add(xc)
			
			/*
			il risultato e' una mappa a singola chiave: la map con gli headers. il value e' la mappa contenuto.
			am-record:class java.util.Collections$SingletonMap - [{
                id:ccy + '-' + dt,
                vm31dt: dt,
                vm31ccy: ccy,
                vm31ac: tot,
                vm31ab: totbase
                }:[vm31ccy:GBP, id:GBP-2020-10-05 00:00:00, vm31ac:23.0, vm31ab:25.327607091729984, vm31dt:2020-10-05 00:00:00]]
				
			*/
		}
		
		/*log.info "LOG_CY9: ==== cypher returning sz: " + xr.size()
		
		if(false) xr.each {
			log.info "record:" + it
		}*/
		
		return xr

	}


///////////////////////////////////////////////



	
	
	


	
	



	
///////////////////////////////////////////////
	
	String common_exec_s(String uc, String cypher, String user, Map tokens, String expected_rowcount) {
		List xr = common_exec(uc, cypher, user, tokens, expected_rowcount)
		return utilService.simpleutils.getJsonText( expected_rowcount=="0..N" ? xr : (xr.size()>0?xr[0]:null))
	}


	List common_exec(String uc, String cypher, String user, Map tokens, String expected_rowcount) {

		assert expected_rowcount=="0..1" || expected_rowcount=="1..1" || expected_rowcount=="0..N"

		List xr = run_cypher(cypher,tokens+[tkusn:user])
		if(true) {
			log.info "ud: $uc ----------------------- BEGIN"
			xr.eachWithIndex{ it,i ->
				if(true) log.debug "RECORD($i): " + it
			}
			log.info "ud: $uc ----------------------- END"
		}
		
		log.info "LOG_CEx: uc:$uc user:$user sz:" + xr.size()
		//log.info "xr0:uc:$uc:" + xr[0]
		
		boolean rowcount_0N = expected_rowcount=="0..N"
		boolean rowcount_01 = expected_rowcount=="0..1" && xr.size()<=1
		boolean rowcount_11 = expected_rowcount=="1..1" && xr.size()==1
		
		assert rowcount_0N || rowcount_01 || rowcount_11
		
		if(uc=="balance_u") {
			boolean cached_hist_fx = xr[0].fxyn=='y'
			log.info "cached_hist_fx:$cached_hist_fx"
			if(!cached_hist_fx) {
				String fxdate = xr[0].dt
				String fxccyset = xr[0].ccy
				Map hfx = [:]
				if(fxccyset == "EUR") {
					hfx = ["EUR":1.0]
				} else {
					hfx = utilService.fx_ext2_r(fxdate.take(4+1+2+1+2),fxccyset) // can be null. unaddressed. FT20201018a
				}
				Map tokens2 = [token1:fxccyset, token2:fxdate, token3:hfx[fxccyset]?.toDouble()]
				log.info "tokens2 for fx_u:" + tokens2	// ok
				String ss = fx_u(tokens2,user)
				log.info "ss from fx_u:"+ss
			}
			
		}
		if(uc=="report_r/byccydtz") {
			// enriching:
			List xre = xr.collect {
				//log.info "computing VaR for " + it.vm31ccy
				Map addvar = [:]
				if(it.vm31ccy!="EUR") {
					Map histwin = utilService.get_1d95var_hist_window(125, it.vm31ccy, true)
					log.info "KUg:histwin:"+histwin
					List rateHistory = get_1d95var_api_hist(it.vm31ccy, histwin.dtfrom, histwin.dtto)
					
					double oneday = utilService.get_1d95var(it.vm31ccy, rateHistory, 0.95, it.vm31ac)
					double onemonth = oneday * Math.sqrt(20)
					addvar = [
							var_1m_95_200: onemonth,
							var_1m_95_200pct: 100.0 * onemonth / it.vm31ab,
							var_1d_95_200: oneday
							
						]
				} else {
					addvar = [
							var_1m_95_200: 0.0,
							var_1m_95_200pct: 0.0,
							var_1d_95_200: 0.0
						]
				}
				it + addvar	
				
				// https://merage.uci.edu/~jorion/oc/case3.html
				// https://www.investopedia.com/articles/04/101304.asp
				// there are 20 trading days in a month (T = 20)
				// AR(T days) = VAR(1 day) x SQRT(T) 
				
				// 250 tradeing days in a year
				// xr[i].var_1d_95_200 = get_1d95var(it.vm31ccy, 125, 0.95, it.vm31ac)
				//log.info "computed VaR for " + it.vm31ccy + " is " + it.var_1d_95_200
			}
			return xre
			
			/* 	vm31dt: dt,		vm31ccy: ccy,		vm31ac: tot, 		vm31ab: totbase */
		}
		return xr
	}



///////////////////////////////////////////////
	
	Map user_u(String user) {
		String theq = """
		$matchusertk1
		with uu, uu.last as uuprev, TS
		set uu.last = TS
		return {
			status: 'success',
			id: uu.id,
			nm: uu.nm,
			session_code: uu.nm,
			session_dob: uu.dob,
			session_last: uuprev
		}
		"""
		Map tokens = [:]
		List ret = common_exec("user_u", theq, user, tokens, "0..1")
		return (ret.size()>0) ? ret[0] : null
	}
	
	
	List user_b_adminonly() {
		String theq = """
		match (uu:USER) with uu
		with uu
		optional match (uu)<-[ua:HASONE]-(aa:ACCOUNT)
		with uu,
			count(*) as countof_accounts,
			max(aa.id) as nullable_latest_created_account
		optional match (uu)<-[ua:HASONE]-(aa:ACCOUNT)<-[ab:HASONE]-(bb:BALANCE)-[bd:HASONE]->(dd)
		with uu,
			count(*) as countof_balances,
			max(bb.id) as nullable_latest_created_balance,
			countof_accounts,
			nullable_latest_created_account
		with uu,
			countof_accounts,
			nullable_latest_created_account,
			countof_balances,
			nullable_latest_created_balance
		return {
			uudob: uu.dob,
			uulastd: uu.last,
			unm: uu.nm,
			uip: uu.ip,
			accounts_count: countof_accounts,
			accounts_lastd: nullable_latest_created_account,
			balances_count: countof_balances,
			balances_lastd: nullable_latest_created_balance
		}
		order by uu.dob desc
		"""
		
		Map tokens = [:]
		return common_exec("user_b", theq, "user", tokens, "0..N")
	}	
	
	
///////////////////////////////////////////////
	
	String date_c(Map params, String user, String tokenuip) {

		String theq = """
		$mergeusertk1
		merge (dd:DATE {dt:\$token1})
		ON CREATE set dd.id=TS
		set dd.last= TS
		WITH uu,dd,TS
		merge (uu)-[ud:HASMANY]->(dd)
		ON CREATE set ud.id=TS
		set ud.last= TS
		with ud, dd, (ud.last = ud.id) as created
		return {
			status: 'success',
			id: ud.id,
			vm211dt: dd.dt,
			dt: dd.dt,
			dtid:dd.id,
			dtlast:dd.last,
			created: created
		}
		"""
		// with (dd.last > dd.id) as created will be null.
		// se DATE is already there, a duplicate id is returned and webix raise an alert.
		Map tokens = [token1:params.dt.take(4+1+2+1+2), tokenuip:tokenuip]	// param.dt = 2020-09-23 00:00:00
		return common_exec_s("date_c", theq, user, tokens, "1..1")
	}

	
	
	List date_r(String user) {
		
		String theq = """
		$matchusertk1
		match (uu)-[ud:HASMANY]->(dd:DATE)
		return {
		id:ud.id,
		unm: uu.nm,
		vm211dt: dd.dt,
		vm211L: ud.L,
		vm211N: ud.N,
		vm211FX: 'L',
		vm211X: 'D'
		} order by dd.dt asc
		"""
		Map tokens = [:]
		return common_exec("date_r", theq, user, tokens, "0..N")
	}
	

	String date_u(Map params, String user) {
		
		String theq = """
		$matchusertk1
		match (uu)-[ud:HASMANY{id: toInteger(\$token1)}]->(dd:DATE)
		SET ud.N = \$token2, ud.L = \$token3, ud.last = TS
		return {
		status: 'success',
		id:ud.id
		}
		"""
		Map tokens = [token1:params.id, token2:params.vm211N, token3:params.vm211L]
		return common_exec_s("date_u", theq, user, tokens, "1..1")
	}
	
	String date_d(Map params, String user) {
		// params.id
		log.info "date_d:" + params.id
		log.info "date_d:" + user
		
		String theq = """
		$matchusertk1
		match (uu)-[ud:HASMANY{id: toInteger(\$token1)}]->(dd:DATE)
		delete ud
		return {
			status: 'success',
			id:toInteger(\$token1)
		}
		"""
		//	return id:ud.id will fail, because, well, it was deleted!
		
		Map tokens = [token1:params.id]
		return common_exec_s("date_d", theq, user, tokens, "1..1")
	}


	String account_c(Map params, String user, String tokenuip) {


		String theq = """
		$mergeusertk1
		limit 1
		merge (uu)<-[ua:HASONE]-(aa:ACCOUNT {nm: \$token1, owner:uu.id})
		on create set aa.id=TS
		set aa.last=TS
		merge (cc:CURRENCY{nm: \$token2})
		on create set cc.id=TS
		with aa, cc, TS limit 1
		merge (aa)-[ac:HASONE]->(cc)
		on create set ac.id=TS
		set ac.last=TS
		with aa, cc, (aa.last <= aa.id) as created
		with aa, cc, case when created then 'success' else 'invalid' end as status
		return {
			status: status,
			id: aa.id,
			vm221nm:aa.nm,
			vm221ccy:cc.nm,
			vm221N:aa.N
		}
		"""
		/*
		optional match (aa)-[acx:HASONE]->(cx:CURRENCY)
		delete acx
		WITH aa, TS
		*/
//		merge (uu)<-[ua:HASONE]-(aa:ACCOUNT {nm: \$token1, N:\$token3, owner:uu.uid id:TS, last:TS})
		
		// se DATE is already there, a duplicate id is returned and webix raise an alert.
		
		Map tokens = [token1:params.nm, token2:params.ccy.toUpperCase(), token3:params.N, tokenuip:tokenuip]
		return common_exec_s("account_c", theq, user, tokens, "0..1")
	}

	String account_r(String user) {
		
		String theq = """
		$matchusertk1
		match (uu)<-[ua:HASONE]-(aa:ACCOUNT)
		match (aa)-[ac:HASONE]->(cc:CURRENCY)
		return {
		id:aa.id,
		vm221nm: aa.nm,
		vm221ccy: cc.nm,
		vm221N: aa.N
		} order by aa.nm
		"""
		
		Map tokens = [:]
		return common_exec_s("account_r", theq, user, tokens, "0..N")
	}

	String account_u(Map params, String user) {
		
		String theq_also_ccy_OLD = """
		$matchusertk1
		match (uu)<-[ua:HASONE]-(aa:ACCOUNT{id: toInteger(\$token1)})
		match (aa)-[ac:HASONE]->(cc:CURRENCY)
		delete ac
		with aa, TS
		merge (cc:CURRENCY{nm: \$token2})
		on create set cc.id=TS
		merge (aa)-[ac:HASONE]->(cc)
		on create set ac.id=TS
		set ac.last=TS, aa.N = \$token3
		return {
		status: 'success',
		id: aa.id
		}
		"""

		String theq = """
		$matchusertk1
		match (uu)<-[ua:HASONE]-(aa:ACCOUNT{id: toInteger(\$token1)})
		match (aa)-[ac:HASONE]->(cc:CURRENCY)
		set aa.N = \$token2
		return {
		status: 'success',
		id: aa.id
		}
		"""

		Map tokens = [token1:params.id, token2:params.vm221N?.trim()]
		return common_exec_s("account_u", theq, user, tokens, "1..1")
	}
	
	String account_d(Map params, String user) {
		
		String theq = """
		$matchusertk1
		match (uu)<-[ua:HASONE]-(aa:ACCOUNT{id: toInteger(\$token1)})
		match (aa)-[ac:HASONE]->(cc:CURRENCY)
		optional match (aa)<-[ab:HASONE]-(bb_opt:BALANCE)-[bd:HASONE]->(dd:DATE)
		delete ua,aa,ac,ab,bb_opt,bd
		return {
			status: 'success',
			id:toInteger(\$token1)
		}
		"""
		//	return id:aa.id will fail, because, well, it was deleted!
		Map tokens = [token1:params.id]
		return common_exec_s("account_d", theq, user, tokens, "1..1")
	}
	
///////////////////////////////	
	
	String balance_r(String user) {
		
		String theq = """
		$matchusertk1
		match (uu)<-[ua:HASONE]-(aa:ACCOUNT)
		match (uu)-[ud:HASMANY]->(dd:DATE)
		match (aa)-[ac:HASONE]->(cc:CURRENCY)
		optional match (aa)<-[ab:HASONE]-(bb_opt:BALANCE)-[bd:HASONE]->(dd)
		with dd, aa, bb_opt, cc
		optional match (cc)<-[cx:HASONE]-(eurccy_opt:HISTFX)-[dx:HASONE]->(dd)
		with aa, dd, cc, bb_opt, eurccy_opt
		return {
		id:aa.id + '-' + dd.dt,
		vm31dt: dd.dt,
		vm31ac: aa.nm,
		vm31av: bb_opt.val,
		vm31ccy: cc.nm,
		vm31fx: eurccy_opt.rate
		} order by dd.dt, aa.nm
		"""

// 		optional match (dd)<-[dx:HASONE]-(eurccy_opt:HISTFX)-[cx:HASONE]->(cc)
		

		// vm31ac: aa.nm + ' (' + cc.nm + ')',
		// with aa, dd, bb_opt
		// optional match (cc)<-[cx:HASONE]-(eurccy_opt:FX)-[:HASONE]->(dd)
		// optional match (dd)<-[:HASONE]-(eurccy_opt:FX)-[:HASONE]->(cc)
		
		Map tokens = [:]
		return common_exec_s("balance_r", theq, user, tokens, "0..N")
	}
	
	
	String export_csv_SPECIAL(String user, Map cfg) {
		assert false
		String theq /* EXACTLY THE SAME OF balance_r() */ = """
		$matchusertk1
		match (uu)<-[ua:HASONE]-(aa:ACCOUNT)
		match (uu)-[ud:HASMANY]->(dd:DATE)
		match (aa)-[ac:HASONE]->(cc:CURRENCY)
		optional match (aa)<-[ab:HASONE]-(bb_opt:BALANCE)-[bd:HASONE]->(dd)
		with dd, aa, bb_opt, cc
		optional match (cc)<-[cx:HASONE]-(eurccy_opt:HISTFX)-[dx:HASONE]->(dd)
		with aa, dd, cc, bb_opt, eurccy_opt
		return {
		id:aa.id + '-' + dd.dt,
		vm31dt: dd.dt,
		vm31ac: aa.nm,
		vm31av: bb_opt.val,
		vm31ccy: cc.nm,
		vm31fx: eurccy_opt.rate
		} order by dd.dt, aa.nm
		"""
		
		List xr = [] // change: neo_empty_client.connect_and_submit(theq, [:],[usn:user],null)

		String csv_header = "reporting date,account label,currency,fxrate,amount"
		
		Closure closure_encode = {
			List exported_fields = [
				it.vm31dt.take(4+1+2+1+2),
				it.vm31ac,
				it.vm31ccy,
				(cfg.export_hist_fx_rates == 1 ? (it.vm31fx ?: '') : ''), 
				it.vm31av ?: ''
			]
			return exported_fields.join(",")
		}
		
		List csv_records = xr.collect closure_encode 
		
		return csv_header + "\n" + csv_records.join("\n")
	}	
		
		
	
	
	String balance_u(Map params, String user) {		// sbagliato
		
		if( ! params.vm31av) return '{ "status": "success" }'
		String theq = """
		$matchusertk1
		match (uu)<-[ua:HASONE]-(aa:ACCOUNT{nm: \$token1})
		match (uu)-[ud:HASMANY]->(dd:DATE{dt: \$token2})
		match (aa)-[ac:HASONE]->(cc:CURRENCY)
		with aa,dd,TS,cc
		merge (aa)<-[ab:HASONE]-(bb:BALANCE)-[bd:HASONE]->(dd)
		ON CREATE SET bb.id = TS
		SET bb.last = TS, bb.val = toFloat(\$token3)
		with bb,cc,dd
		optional match (dd)<-[dx:HASONE]-(eurccy:HISTFX)-[cx:HASONE]->(cc)
		with bb,cc,dd, case eurccy.rate when null then 'n' else 'y' end as fxyn
		return {
		status: 'success',
		id: bb.id,
		ccy: cc.nm,
		dt: dd.dt,
		fxyn: fxyn
		}
		"""
		println "==========" + params
		//req: [val:32, oldval:, cid:vm31av, rid:1602030783218-2020-10-05 00:00:00, UC:UB, controller:member, format:null, action:balance_u] user: ApaH9g1rgaRMjkaQoL8oDUG6CAHJBqMgRyEJ8RHtlZ
		// Unable to convert java.math.BigDecimal to Neo4j Value.. Stacktrace follows:
		Map tokens = [token1:params.vm31ac, token2:params.vm31dt, token3:params.vm31av?.toDouble()]
		return common_exec_s("balance_u", theq, user, tokens, "1..1")
	}
	
	String fx_u(Map params, String user) {
		// tokens2 for fx_u:[token1:GBP, token2:2020-09-01 00:00:00, token3:0.889845]
		String theq = """
		$matchusertk1
		match (uu)<-[ua:HASONE]-(aa:ACCOUNT)
		match (aa)-[ac:HASONE]->(cc:CURRENCY{nm: \$token1})
		with uu, TS, cc limit 1
		match (uu)-[ud:HASMANY]->(dd:DATE{dt: \$token2})
		with dd,cc, TS limit 1
		merge (dd)<-[dx:HASONE]-(eurccy:HISTFX)-[cx:HASONE]->(cc)
		ON CREATE SET eurccy.id = TS, eurccy.rate = toFloat(\$token3)
		return {
		status: 'success',
		id: eurccy.id
		}
		"""
		Map tokens = params
		return common_exec_s("fx_u", theq, user, tokens, "1..1")
	}
	
	
	List get_1d95var_api_hist(String ccy, String datefrom, String dateto) {
		
		if(ccy=="EUR") return null
		
		String theq_r = """
		match (cc:CURRENCY{nm: \$token1})<-[cx:HASONE]-(eurccy:HISTFXWIN)-[dx:HASONE]->(dd:DATE{dt: \$token2})
		return {
			hist200: eurccy.hist200
		}
		"""
		
		String theq_w = """
		merge (cc:CURRENCY{nm: \$token1})
		with cc
		merge (dd:DATE{dt: \$token2})
		with cc,dd
		merge (cc)<-[cx:HASONE]-(eurccy:HISTFXWIN)-[dx:HASONE]->(dd)
		set eurccy.TS = timestamp(), eurccy.hist200 = \$token3
		return {
			ts: eurccy.TS
		}
		"""
		
		Map tokens = [token1:ccy, token2:dateto]
		List cachehit = common_exec("get_1d95var_api_hist_r", theq_r, "nil user", tokens, "0..1")
		boolean cache_hit = cachehit.size()==1
		log.info "1d95var_api_hist_r cache_hit: $cache_hit ccy:$ccy dateto:$dateto"
		String txt
		if(!cache_hit) {
			
			txt = utilService.get_fxhist_range(ccy, datefrom, dateto) // can be null. unaddressed. FT20201018d
			
			Map tokens2 = [token1:ccy, token2:dateto, token3:txt]
			List cachew = common_exec("get_1d95var_api_hist_w", theq_w, "nil user", tokens2, "0..1")
		} else {
			txt = cachehit[0].hist200
		}
		
		Map apiret = utilService.simpleutils.getJsonObj(txt)
		def dates = apiret.rates.keySet()
		List histo = dates.collect { apiret.rates[it][ccy].toDouble() }
		return histo
	}
	
	String fx_d_SPECIAL(String user) {
		assert false
		String theq = """
		match (dd:DATE)<-[dx:HASONE]-(eurccy:HISTFX)-[cx:HASONE]->(cc:CURRENCY)
		delete eurccy, dx, cx
		with count(*) as c
		return {
		status: 'success',
		count: c
		}
		"""
		Map tokens = [:]
		return common_exec_s("fx_d_SPECIAL", theq, user, tokens, "0..N")		
	}
	
	
	String fx_r_SPECIAL(String user) {
		assert false
		String theq = """
		match (dd:DATE)<-[dx:HASONE]-(eurccy:HISTFX)-[cx:HASONE]->(cc:CURRENCY)
		return {
		dt:dd.dt,
		ccy:cc.nm,
		rate:eurccy.rate
		}
		"""
		// 		id: eurccy.id
		Map tokens = [:]
		return common_exec_s("fx_r_SPECIAL", theq, user, tokens, "0..N")		
	}
	
	String all_d_SPECIAL(String user) {
		assert false
		String theq = """
		MATCH (n) OPTIONAL MATCH (n)-[r]-() DELETE n,r
		return {
		done:1
		}
		"""
		Map tokens = [:]
		return common_exec_s("fx_r_SPECIAL", theq, user, tokens, "0..N")		
	}	

	String all_r_SPECIAL(String user) {
		assert false
		String theq = """
		match(n)
		with count(*) as c
		return {
		count: c
		}
		"""
		Map tokens = [:]
		return common_exec_s("fx_r_SPECIAL", theq, user, tokens, "0..N")		
	}	

	String keepwarm_r(String user) {
		String theq = """
		match (n)
		with count(*) as nodes
		return {
		ts: timestamp(),
		nodes: nodes
		}
		"""
		// crash: uuid: apoc.create.uuid()
		// crash: uuid: uuid()
		Map tokens = [:]
		return common_exec_s("keepwarm_r", theq, user, tokens, "1..1")
	}
	
	
	String report_r(String user, String mode) {
		
		String theq
		
		if(mode=="byccydt") theq = """
		$matchusertk1
		match (uu)<-[ua:HASONE]-(aa:ACCOUNT)
		match (aa)-[ac:HASONE]->(cc:CURRENCY)
		match (uu)-[ud:HASMANY]->(dd:DATE)
		match (aa)<-[ab:HASONE]-(bb:BALANCE)-[bd:HASONE]->(dd)
		match (aa)<-[ab:HASONE]-(bb:BALANCE)-[bd:HASONE]->(dd)
		match (dd)<-[dx:HASONE]-(eurccy:HISTFX)-[cx:HASONE]->(cc)
		with cc.nm as ccy, dd.dt as dt, sum(bb.val) as tot, sum(bb.val/eurccy.rate) as totbase,
		head(collect(eurccy.rate)) as histfx
		return {
		id:ccy + '-' + dt,
		vm31dt: dt,
		vm31ccy: ccy,
		vm31ac: tot,
		vm31ab: totbase,
		vm31fx : histfx
		} order by dt, ccy
		"""
		
		if(mode=="byccydtz") theq = """
		$matchusertk1
		match (uu)-[ud:HASMANY]->(ddz:DATE)
		with uu,
			max(ddz.dt) as datez,
			'1' as rowcount
		match (uu)<-[ua:HASONE]-(aa:ACCOUNT)
		match (aa)-[ac:HASONE]->(cc:CURRENCY)
		match (uu)-[ud:HASMANY]->(dd:DATE) where dd.dt = datez
		match (aa)<-[ab:HASONE]-(bb:BALANCE)-[bd:HASONE]->(dd)
		match (aa)<-[ab:HASONE]-(bb:BALANCE)-[bd:HASONE]->(dd)
		match (dd)<-[dx:HASONE]-(eurccy:HISTFX)-[cx:HASONE]->(cc)
		with cc.nm as ccy,dd.dt as dt, sum(bb.val) as tot, sum(bb.val/eurccy.rate) as totbase
		return {
		id:ccy + '-' + dt,
		vm31dt: dt,
		vm31ccy: ccy,
		vm31ac: tot,
		vm31ab: totbase
		} order by dt, ccy
		"""
		
		if(mode=="bybcydt") theq = """
		$matchusertk1
		match (uu)<-[ua:HASONE]-(aa:ACCOUNT)
		match (aa)-[ac:HASONE]->(cc:CURRENCY)
		match (uu)-[ud:HASMANY]->(dd:DATE)
		match (aa)<-[ab:HASONE]-(bb:BALANCE)-[bd:HASONE]->(dd)
		match (dd)<-[dx:HASONE]-(eurccy:HISTFX)-[cx:HASONE]->(cc)
		with dd.dt as dt, sum(bb.val/eurccy.rate) as totbase
		return {
		id:dt,
		vm31dt: dt,
		vm31ab: totbase
		} order by dt
		"""
		
		if(mode=="bybcydtvar") theq = """
		$matchusertk1
		match (uu)-[ud:HASMANY]->(ddz:DATE)
		with uu,
			max(ddz.dt) as date2,
			'1' as rowcount
		match (uu)-[ud:HASMANY]->(dd:DATE) where dd.dt = date2
		match (uu)<-[ua:HASONE]-(aa:ACCOUNT)<-[ab:HASONE]-(bb:BALANCE)-[bd:HASONE]->(dd)
		match (aa)-[ac:HASONE]->(cc:CURRENCY)<-[cx:HASONE]-(eurccy:HISTFX)-[dx:HASONE]->(dd)
		
		with uu, date2, cc.nm as ccy2,
			sum(bb.val/eurccy.rate) as eur2_byccy,
			collect(eurccy.rate)[0] as rate2,
			sum(bb.val) as amt2_byccy,
			'1*ccy' as rowcount
		match (uu)-[ud:HASMANY]->(dd:DATE) where dd.dt < date2
		match (uu)<-[ua:HASONE]-(aa:ACCOUNT)<-[ab:HASONE]-(bb:BALANCE)-[bd:HASONE]->(dd)
		match (aa)-[ac:HASONE]->(cc:CURRENCY)<-[cx:HASONE]-(eurccy:HISTFX)-[dx:HASONE]->(dd) where cc.nm=ccy2
		
		with date2, ccy2, eur2_byccy, rate2, amt2_byccy,
			 dd.dt as date1, cc.nm as ccy1,
			 sum(bb.val/eurccy.rate) as eur1_byccy,
			 collect(eurccy.rate)[0] as rate1,
			 sum(bb.val) as amt1_byccy,
			 '1*ccy*(dt-1)' as rowcount

		with date2, ccy2, eur2_byccy, rate2, amt2_byccy,			
			 date1, ccy1, eur1_byccy, rate1, amt1_byccy,
			 eur2_byccy - eur1_byccy as delta_eur22_eur11_byccy,
			 (amt2_byccy / rate1) - eur1_byccy as delta_eur21_eur11_byccy,
			 (amt1_byccy / rate2) - eur1_byccy as delta_eur12_eur11_byccy,
			 '1*ccy*(dt-1)' as rowcount
		with date1, date2, sum(eur2_byccy) as eur2,
		sum(delta_eur22_eur11_byccy) as delta_eur22_eur11,
		sum(delta_eur21_eur11_byccy) as delta_eur21_eur11,
		sum(delta_eur12_eur11_byccy) as delta_eur12_eur11,
		sum(eur1_byccy) as eur1,
		'1*(dt-1)' as rowcount
		return {
		id:      date1,
		vm31dt:  date1,
		vm31dtz: date2,
		vm31ab:  eur1,
		vm31abz: eur2,
		vm31abd: delta_eur22_eur11,
		vm31abp: 100.0 * delta_eur22_eur11 / eur1,
		vm31abdfzfx: delta_eur21_eur11,
		vm31abpfzfx: 100.0 * delta_eur21_eur11 / eur1	
		} order by date1
		"""

		

		
//		with cc.nm as ccy,dd.dt as dt, sum(bb.val) as tot, round(100*sum(bb.val/eurccy.rate))/100.00 as totbase

		Map tokens = [:]
		return common_exec_s("report_r/$mode", theq, user, tokens, "0..N")
	}

	
	List devreportdata(String user) {
		
		String theq_BY_CCY_DATE1 = """
		$matchusertk1
		match (uu)-[ud:HASMANY]->(ddz:DATE)
		with uu,
			max(ddz.dt) as date2,
			'1' as rowcount
		match (uu)-[ud:HASMANY]->(dd:DATE) where dd.dt = date2
		match (uu)<-[ua:HASONE]-(aa:ACCOUNT)<-[ab:HASONE]-(bb:BALANCE)-[bd:HASONE]->(dd)
		match (aa)-[ac:HASONE]->(cc:CURRENCY)<-[cx:HASONE]-(eurccy:HISTFX)-[dx:HASONE]->(dd)
		with uu,
			date2,
			cc.nm as ccy2,
			sum(bb.val/eurccy.rate) as eur2_byccy,
			collect(eurccy.rate)[0] as rate2,
			sum(bb.val) as amt2_byccy,
			'1*ccy' as rowcount

		match (uu)-[ud:HASMANY]->(dd:DATE) where dd.dt < date2
		match (uu)<-[ua:HASONE]-(aa:ACCOUNT)<-[ab:HASONE]-(bb:BALANCE)-[bd:HASONE]->(dd)
		match (aa)-[ac:HASONE]->(cc:CURRENCY)<-[cx:HASONE]-(eurccy:HISTFX)-[dx:HASONE]->(dd) where cc.nm=ccy2

		with
			date2,
			ccy2,
			eur2_byccy,
			rate2,
			amt2_byccy,
			
			dd.dt as date1,
			cc.nm as ccy1,
			sum(bb.val/eurccy.rate) as eur1_byccy,
			collect(eurccy.rate)[0] as rate1,
			sum(bb.val) as amt1_byccy,
			'1*ccy*(dt-1)' as rowcount

		with
			date2,
			ccy2,
			eur2_byccy,
			rate2,
			amt2_byccy,
			
			eur2_byccy - eur1_byccy as delta_eur22_eur11_byccy,
			(amt2_byccy / rate1) - eur1_byccy as delta_eur21_eur11_byccy,
			(amt1_byccy / rate2) - eur1_byccy as delta_eur12_eur11_byccy,

			date1,
			ccy1,
			eur1_byccy,
			rate1,
			amt1_byccy,
			'1*ccy*(dt-1)' as rowcount

		return {
		date2: date2,
		ccy2: ccy2,
		rate2: rate2,
		eur2_byccy: eur2_byccy,
		amt2_byccy: amt2_byccy,
		delta_eur22_eur11_byccy: delta_eur22_eur11_byccy,
		delta_eur21_eur11_byccy: delta_eur21_eur11_byccy,
		delta_eur12_eur11_byccy: delta_eur12_eur11_byccy,
		date1: date1,
		ccy1: ccy1,
		rate1: rate1,
		eur1_byccy: eur1_byccy,
		amt1_byccy: amt1_byccy,		
		id:      date2+ccy2+date1+ccy1
		} order by date1
		"""
		
		Map tokens = [:]
		return [] // change: neo_empty_client.connect_and_submit(theq, tokens,[usn:user],null)
	}
	
}
