package org.fusesource.cloudmix.agent.logging;

/**
 * Parses a log record using a specific regular expression or some alternative mechanism
 */
public interface LogParser {

    /**
     * Converts a text log entry into LogRecord
     * 
     * @param record the log entry
     * @return LogRecord, may be null
     */
    LogRecord parseRecord(CharSequence record);

    /**
     * Indicates if a given sequence may be successfully matched. Can be useful when reading multi-line
     * entries from the log input stream.
     * 
     * @param line the text sequence which may be a complete log record
     * @return true if it may be matched
     */
    boolean isPossibleMatch(CharSequence line);
}
