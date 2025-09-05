package utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import play.Logger;

public class TimeConvert {

	public static final String timeStamptoDate (String timeStamp) {
		if(timeStamp == ""){
			return "";
		}
		Date date = new Date(Long.parseLong(timeStamp)*1000);
		DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm");
		String time = dateFormat.format(date);
		return time;
	}
	
	public static final long datetoTimeStamp (String time) throws ParseException{
		if(time == null || time.equals("")){
			return 0;
		}
		SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss.SSS");
		String temptime = time + ":00.000";
		Date parsedDate = dateFormat.parse(temptime);
		long timeStamp = parsedDate.getTime();
		return timeStamp;
		
	}

	/**
	 * @Description: "EEE MMM dd HH:mm:ss zzz yyyy" -> day count in a year
	 * @Param:  String time
	 * @return: String dayCount
	 */
	public static final String dateToDayCount( String time ) throws ParseException {

		DateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US);

		int len = time.length();
		String dayCount = time.substring(len-4,len) + String.format("%tj",dateFormat.parse(time));

		return dayCount;
	}
}
