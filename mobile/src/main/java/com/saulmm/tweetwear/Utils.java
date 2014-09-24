package com.saulmm.tweetwear;

import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    private static final String LINK_REG_EXP = "((https?|ftp|gopher|telnet|file|Unsure|http):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)";


    public static String removeUrl (String urlIstring) {

        String urlPattern = LINK_REG_EXP;
        Pattern p = Pattern.compile(urlPattern,Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(urlIstring);
        int i = 0;

        while (m.find()) {

             urlIstring = urlIstring.replaceAll(m.group(i),"").trim();
            i++;
        }
        return urlIstring;
    }

    //1 minute = 60 seconds
    //1 hour = 60 x 60 = 3600
    //1 day = 3600 x 24 = 86400
    public static String getTimeDiference(Date endDate){
        // TODO harcoded text
        String days = " days";
        String mins = " min.";
        String hours = " h.";
        String secs = " sec.";

        String [] a = new String[]{"a", "1", "2"};

        Calendar c = Calendar.getInstance();
        Date startDate = c.getTime();

        //milliseconds
        long different = endDate.getTime() - startDate.getTime();

        long secondsInMilli = 1000;
        long minutesInMilli = secondsInMilli * 60;
        long hoursInMilli = minutesInMilli * 60;
        long daysInMilli = hoursInMilli * 24;

        long elapsedDays = different / daysInMilli;
        different = different % daysInMilli;

        if (elapsedDays != 0)
            return elapsedDays + days;

        long elapsedHours = different / hoursInMilli;
        different = different % hoursInMilli;

        if (elapsedHours != 0)
            return elapsedHours + hours;

        long elapsedMinutes = different / minutesInMilli;
        different = different % minutesInMilli;

        if (elapsedMinutes != 0)
            return elapsedMinutes + mins;

        long elapsedSeconds = different / secondsInMilli;

        if (elapsedSeconds != 0)
            return elapsedSeconds + secs;


        return "";
    }
}
