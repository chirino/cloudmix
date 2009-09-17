package org.fusesource.cloudmix.agent.logging;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Log parser which is capable of parsing log records formatted according to the following Log4J rule :
 * "%d{ABSOLUTE} | %-5.5p | %-16.16t | %-32.32c{1} | %-32.32C %4L | %m%n" DATE | Level | Thread | Category |
 * Class and line | Message
 */
public class DefaultLogParser implements LogParser {

    private static final String RECORD_EXPRESSION = "(\\d{2}:\\d{2}:\\d{2})[\\s|,\\d]*"
                                                    + "(FATAL|ERROR|WARN|INFO|DEBUG|TRACE)[\\s|]*"
                                                    + "([\\S-]+)[\\s|]*" + "([\\S-]+)[\\s|]*"
                                                    + "([\\S-]+)[\\s|]*" + "([\\S-]+)[\\s|]*"
                                                    + "([\\S\\-_ ]+)" + "$";

    private static final Pattern RECORD_PATTERN;

    static {
        RECORD_PATTERN = createPattern(RECORD_EXPRESSION);
    }

    private static Pattern createPattern(String expression) {
        return Pattern.compile(expression);
    }

    /**
     * Converts a text log entry into LogRecord
     * 
     * @param record the log entry
     * @return LogRecord, may be null
     */
    public LogRecord parseRecord(CharSequence record) {
        Matcher m = RECORD_PATTERN.matcher(record);
        if (m.matches()) {
            return createRecord(m);
        }
        return null;
    }

    /**
     * Indicates if a given sequence may be successfully matched. Can be useful when reading multi-line
     * entries from the log input stream.
     * 
     * @param line the text sequence which may be a complete log record
     * @return true if it may be matched
     */
    public boolean isPossibleMatch(CharSequence line) {
        String s = line.toString();
        return s.contains("INFO") || s.contains("WARN") || s.contains("FATAL") || s.contains("ERROR")
               || s.contains("DEBUG") || s.contains("TRACE");
    }

    private LogRecord createRecord(Matcher m) {
        LogRecord record = new LogRecord();
        record.setDate(m.group(1));
        record.setLevel(m.group(2));
        record.setThreadId(m.group(3));
        record.setCategory(m.group(4));
        record.setClassName(m.group(5));
        record.setClassLineNumber(m.group(6));
        record.setMessage(m.group(7));
        return record;
    }
}
