package com.melogamy
import com.melogamy.MgBase
import groovy.transform.CompileStatic


@CompileStatic
class MgSimpleUtils extends MgBase {
	
	Map mg_settings = [:]
	
	MgSimpleUtils() {
		println "Hello_by MgSimpleUtils() : NOP"
	}
	
	MgSimpleUtils(String mg_settings_env) {
		// set MG_SETTINGS=UsrMayLoginYN=Y...UsrMaySelfRegYPN=Y...UsrMailToAny=Y...UsrMailToUs=Y...SysIgnoreGeoc=N...SysSkipCaptcha=N...SysBEPublishYNA=A...MQSubOnStart=Y
		println "Hello_by MgSimpleUtils(mg_settings_env) : NOP"
		// TBC split tokenize
		String[] s = mg_settings_env.split("\\.\\.\\.")
		int ssz = s.size()
		//assert s.size()==8
		for(int i=0;i<ssz;i++) {
			String[] se = s[i].split("=")
			mg_settings.put(se[0],se[1])
		}
		println "Hello_by MgSimpleUtils(mg_settings_env) : $ssz: " + mg_settings 
	}
	
	String[] csvline2list(String line, String delim,int nf) {	// naive
		if(",;|".indexOf(delim)<0) throw new Exception("csvline2list DELIM")
		if(delim=="|") delim = "\\|"
		/* there are 12 characters with special meanings: the backslash \, the caret ^, 
		the dollar sign $, the period or dot ., the vertical bar or pipe symbol |, the question mark ?, 
		the asterisk or star *, the plus sign +, the opening parenthesis (, the closing parenthesis ), 
		and the opening square bracket [, the opening curly brace {, These special characters are often called "metacharacters".
		*/
		String[] fields = line.split(delim)
		if(nf != fields.size()) throw new Exception("csvline2list NF to be $nf but " + fields.size())
		//for(int i=0;i<fields.size();i++) {
		//	rt.add("f$i",fields[i])
		//}
		return fields
	}
	


	/*String prettymap(def mm) {
		return getPrettyJsonText(mm)
	}*/
	

	
	
	

}
