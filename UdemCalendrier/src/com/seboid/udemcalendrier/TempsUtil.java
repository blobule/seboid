package com.seboid.udemcalendrier;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TempsUtil {
	//
	// conversion de temps
	// date : 2013-03-04
	// time : 20:22:12
	// si time="null" ou "", alors c'est 00:00:00 si starting ou sinon 23:59:59
	//
	public static long dateHeure2epoch(String date,String time,boolean starting) {
		String str = "Jun 13 2003 23:11:52.454 UTC";
		SimpleDateFormat df=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date d;
		try {
			if( time!=null && !time.equals("") && !time.equals("null") ) {
				d=df.parse(date+" "+time);
			}else{
				d=df.parse(date+(starting?" 00:00:00":" 23:59:59"));
			}
			return(d.getTime());
		} catch( Exception e ) {
			return(0);
		}
	}
}
