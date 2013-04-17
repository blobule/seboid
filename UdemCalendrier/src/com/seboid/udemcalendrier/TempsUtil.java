package com.seboid.udemcalendrier;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class TempsUtil {
	//
	// conversion de temps
	// date : 2013-03-04
	// time : 20:22:12
	// si time="null" ou "", alors c'est 00:00:00 si starting ou sinon 23:59:59
	//
	public static long dateHeure2epoch(String date, String time,
			boolean starting) {
		// String str = "Jun 13 2003 23:11:52.454 UTC";
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date d;
		if (time != null && !time.equals("") && !time.equals("null")) {
			return dateHeure2epoch(date + " " + time);
		} else {
			return dateHeure2epoch(date
					+ (starting ? " 00:00:00" : " 23:59:59"));
		}
	}

	// format 2013-03-20 22:02:00
	public static long dateHeure2epoch(String datetime) {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			return (df.parse(datetime).getTime());
		} catch (Exception e) {
			return (0);
		}
	}

	//
	// aujourd'hui, date seulement... 2013-10-20 00:00:00, debut de journee
	//
	public static String aujourdhui(int offset_jour) {
		Calendar c = Calendar.getInstance();
		c.add(Calendar.DAY_OF_MONTH, offset_jour);
		return new SimpleDateFormat("yyyy-MM-dd").format(c.getTimeInMillis());
	}
	public static String aujourdhui(long time,int offset_jour) {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(time);
		c.add(Calendar.DAY_OF_MONTH, offset_jour);
		return new SimpleDateFormat("yyyy-MM-dd").format(c.getTimeInMillis());
	}

	public static String aujourdhuiNom(int offset_jour) {
		Calendar c = Calendar.getInstance();
		c.add(Calendar.DAY_OF_MONTH, offset_jour);
		return c.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG,
				Locale.getDefault());
	}

	// aujourdhui, debut de la journee. on enleve les heures
	public static long aujourdhuiMilli() {
		Calendar calendar = Calendar.getInstance(); // current time.
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar.getTimeInMillis();
	}

}
