package g4mcaa

import com.melogamy.MgSimpleUtils

import java.util.Calendar
import java.text.SimpleDateFormat
import java.lang.Math

//import grails.gorm.transactions.Transactional

// @Transactional
class UtilService {
	
	static MgSimpleUtils simpleutils = new MgSimpleUtils()
	
	String session_c() {
		long da = simpleutils.epoch_ms()
		String ss = simpleutils.getRandomAlphanum(sessionLen()-2)	// 18 alphanums: 1.8E32 - 24 alphanums: 1.0E43
		//ss = ss.substring(0,6) + "_" + ss.substring(6,12) + "_" + ss.substring(12,18) // + "_" + ss.substring(18,24)
		long dz = simpleutils.epoch_ms()
		ss = "A" + ss + "Z"
		log.info "LOG_G7y:session_c(): $ss : took ${dz-da} ms"
		return ss
    }
	
	int sessionLen() {
		return 18+2
	}
	
	List getCurrencies() {
		return ["AUD","BGN","BRL","CAD","CHF","CNY","CZK","DKK","EUR","GBP","HKD","HRK","HUF","IDR","ILS","INR","ISK","JPY","KRW","MXN","MYR","NOK","NZD","PHP","PLN","RON","RUB","SEK","SGD","THB","TRY","USD","ZAR"]
	}
	
	boolean isValidInput(String text, int type) {
		try {
			if(type==11) {
				simpleutils.get_date_of_datestr(text,11)
			}
			if(type==31) {
				text.toFloat()
			}
			if(type==41) {
				def act_pattern = /^[\w \(\)\/\-\.]+$/
				def testMatch = ( text =~ act_pattern )
				return testMatch.matches()
			}
			if(type==51) {
				return text in getCurrencies()
			}
			return true
		} catch(e) {
			log.error "LOG_HTu:isValidInput:$text:"+e
		}
		return false
	}

	
///////////////////////////////////////////////
	

	
	Map fx_ext2_r(String fxdate, String fxccyset) {	// EUR // can be null
	
		// by the BCE, cut "ECB 1415"
		// http://exchangeratesapi.io/2013-03-16?symbols=USD,AUD,CAD,PLN,MXN
		// GET https://api.exchangeratesapi.io/history?start_at=2018-01-01&end_at=2018-09-01 HTTP/1.1

		// String fxdate = "2013-03-16"
		// String fxccyset = "USD,AUD,GBP"	// EUR
		String req ="https://api.exchangeratesapi.io/${fxdate}?symbols=${fxccyset}&base=EUR"
		log.info "request to FX API: $req" 
		Map apiret = [:]
		try {
			String txt = new URL(req).getText(connectTimeout: 5000, readTimeout: 7000,
					useCaches: true, allowUserInteraction: false, requestProperties: ['Connection': 'close'])

			log.debug "answer from FX API: $txt"
			// {"error":"day is out of range for month"}	400 Bad Request
			apiret = simpleutils.getJsonObj(txt)
			log.info "answer from FX API: " + apiret.rates.keySet().size()
		} catch(e) {
			log.error "LOG_H7u:"+e
		}
		// apiret.error.code 302 = invalid date
		// https://fixer.io/documentation
		return apiret.rates
		/*
			{"rates":{"GBP":0.9081},"base":"EUR","date":"2020-10-05"}
		*/
    }	





	Map get_1d95var_histwindow(String dateto, int histVaRDepth) {
		if(histVaRDepth<=0) return null
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd")
		Date datetod = dateFormat.parse(dateto)
		Date datefromd = simpleutils.get_date_plus_ndays(datetod,-histVaRDepth)
		
		String dtfrom = dateFormat.format(datefromd)
		String dtto = dateFormat.format(datetod)
		return [dtto:dtto, dtfrom:dtfrom]
	}
	
	Map get_1d95var_api_latest(String ccy) {	// can be null
		
		if(ccy=="EUR") return null
		
		//ccy=ccy.toUpperCase()	// importante
		String getlatest= "https://api.exchangeratesapi.io/latest?symbols=$ccy&base=EUR"
		Map apiret = [:]
		try {
			String txt = new URL(getlatest).getText(connectTimeout: 5000, readTimeout: 7000,
					useCaches: true, allowUserInteraction: false, requestProperties: ['Connection': 'close'])
			log.debug "answer from FX API: $txt"
			apiret = simpleutils.getJsonObj(txt)
			// {"rates":{"GBP":0.91058},"base":"EUR","date":"2020-10-06"}
			log.info "answer from FX API: " + apiret.rates.keySet().size()
		} catch(e) {
			log.error "LOG_H6T:"+e
		}
		return apiret
	}
	
	String get_fxhist_range(String ccy, String datefrom, String dateto) {	// can be null
		
		String gethist= "https://api.exchangeratesapi.io/history?start_at=$datefrom&end_at=$dateto&symbols=$ccy&base=EUR"
		String txt
		try {
			txt = new URL(gethist).getText(connectTimeout: 5000, readTimeout: 7000,
					useCaches: true, allowUserInteraction: false, requestProperties: ['Connection': 'close'])
			log.debug "answer from FX API: $txt"
			log.info "answer from FX API: " + txt.size()
		} catch(e) {
			log.error "LOG_HB8:"+e
		}
		// {"rates":{"2018-01-09":{"GBP":0.8827},"2018-01-03":{"GBP":0.8864},"2018-01-08":{"GBP":0.88413},"2018-01-10":{"GBP":0.8867},"2018-01-05":{"GBP":0.88883},"2018-01-02":{"GBP":0.88953},"2018-01-04":{"GBP":0.89103}},"start_at":"2018-01-01","base":"EUR","end_at":"2018-01-10"}
		return txt
	}
			

	Map get_1d95var_hist_window(int histVaRDepth, String ccy, boolean skip_external_API_call) {
		log.info "LOG_CCt:"+ccy
		if(ccy=="EUR") return null
		// getting the last EOM <= today. is there a valid hist rate in the API DB ?
		String latestd
		if(skip_external_API_call) { 
			Date latestd_str = new Date()
			latestd = simpleutils.get_datestr_of_date(latestd_str,11)
		} else {
			Map latestRateMap = get_1d95var_api_latest(ccy)
			latestd = latestRateMap?.date	// can be null - unaddressed. FT20201018b
		}
		String dateto_s = simpleutils.get_datestr_floor_FirstOfMonth(latestd)
		Date dateto_s_prevEOM = simpleutils.get_date_of_datestr(dateto_s,11)
		dateto_s_prevEOM = simpleutils.get_date_plus_ndays(dateto_s_prevEOM,-1)
		dateto_s = simpleutils.get_datestr_of_date(dateto_s_prevEOM,11)
		
		Map histwin = get_1d95var_histwindow(dateto_s, histVaRDepth) // dayofmonth forced to 1.
		log.info "LOG_JUg:histwin:" + histwin
		return histwin
	}

	Double get_1d95var(String ccy, List rateHistory, Double histVaRPctile, Double latestAmountAtRiskCcy) {
		
		if(ccy=="EUR") return 0.0
		assert histVaRPctile==0.95 // || histVaRPctile==0.99
		// GIVEN rateHistory [0.902, 0.904, 0.903, 0.906, 0.904]

		Map latestRateMap = get_1d95var_api_latest(ccy)		// can be null. unaddressed. FT20201018c
		Double latestRate = latestRateMap?.rates[ccy].toDouble()
		
		
		
		int isz = rateHistory.size()		// isz = 5
		int dsz = rateHistory.size()-1		// dsz = 5-1

		def subs = 1..dsz
		List runningHistRateDelta = subs.collect { rateHistory[it] - rateHistory[it-1]}
		// THEN runningHistRateDelta [0.002, -0.001, 0.003, -0.002]
		// GIVEN latestRate 0.907		// not necessarily = rateHistory[isz-1]
		// GIVEN latestAmountAtRiskCcy 1000.0

		Double latestAmountEur = latestAmountAtRiskCcy / latestRate
		// THEN latestAmountEur = 1102.5358
		
		List EstimatedTomorrowRateSet = runningHistRateDelta.collect { latestRate + it }
		// THEN EstimatedTomorrowRateSet [0.909, 0.906, 0.91, 0.905]

		// revaluation:
		List EstimatedTomorrowAmountEur = EstimatedTomorrowRateSet.collect { latestAmountAtRiskCcy / it }
		// THEN EstimatedTomorrowAmountEur [1100.1100, 1103.7527, 1098.9010, 1104.9723]
		
		List EstimatedTomorrowAmountEurSorted = EstimatedTomorrowAmountEur.sort().reverse() // descending
		// THEN EstimatedTomorrowAmountEurSorted [1104.9723, 1103.7527, 1100.1100, 1098.9010]
		int pctile = Math.ceil((dsz-1) * histVaRPctile).toInteger()
		Double var95_EstimatedAmount = EstimatedTomorrowAmountEurSorted[pctile]
		// THEN VaR = var95_EstimatedAmount - latestAmountEur = 1098.9010 - 1102.5358 = -3.6348 EUR
		return var95_EstimatedAmount - latestAmountEur // = var95_EstimatedLoss
	}

//			pctile = pctile <= dsz-1 ? pctile : dsz-1		// cap-floor
	
/*		
		log.debug "dateto " +dateto
		log.debug "datefrom " +datefrom
		log.debug "rateHistory " + rateHistory
		log.debug "runningHistRateDelta " + runningHistRateDelta
		log.debug "latestRate " + latestRate
		log.debug "latestAmountAtRiskCcy " + latestAmountAtRiskCcy
		log.debug "latestAmountEur " + latestAmountEur
		log.debug "EstimatedTomorrowRateSet " + EstimatedTomorrowRateSet
		log.debug "EstimatedTomorrowAmountEur " + EstimatedTomorrowAmountEur
		log.debug "EstimatedTomorrowAmountEurSorted " + EstimatedTomorrowAmountEurSorted
		log.debug "95th-pctile " + pctile
		log.debug "var95_EstimatedAmount " + var95_EstimatedAmount
		log.debug "var95_EstimatedLoss " + var95_EstimatedLoss

		return var95_EstimatedLoss
	}
*/
		// ... thanks to https://mkyong.com/java/java-convert-date-to-calendar-example/




}
