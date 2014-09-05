package com.saulmm.tweetwear;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by wtf on 28/08/14.
 */
public class Utils {
    public static final String LINK_REG_EXP = "((https?|ftp|gopher|telnet|file|Unsure|http):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)";


    public static String removeUrl(String commentstr) {
        String urlPattern = LINK_REG_EXP;
        Pattern p = Pattern.compile(urlPattern,Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(commentstr);
        int i = 0;
        while (m.find()) {
            commentstr = commentstr.replaceAll(m.group(i),"").trim();
            i++;
        }
        return commentstr;
    }


}
