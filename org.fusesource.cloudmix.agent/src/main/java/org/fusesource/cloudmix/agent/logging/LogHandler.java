package org.fusesource.cloudmix.agent.logging;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.common.base.Predicate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Facilitates the retrieval of and search for log records independently of the actual log format
 */
public class LogHandler {

    private static final transient Log LOG = LogFactory.getLog(LogHandler.class);

    private LogParser logParser = new DefaultLogParser();
    private List<LogRecord> records = new ArrayList<LogRecord>();

    public LogHandler(InputStream logStream) {
        parseLog(toReader(logStream));
    }

    public LogHandler(InputStream logStream, LogParser logParser) {
        this.logParser = logParser;
        parseLog(toReader(logStream));
    }

    public LogHandler(Reader logStream) {
        parseLog(logStream);
    }

    public LogHandler(Reader logStream, LogParser logParser) {
        this.logParser = logParser;
        parseLog(logStream);
    }

    /**
     * Gets all the records
     * 
     * @return records
     */
    public List<LogRecord> getAllRecords() {
        return Collections.unmodifiableList(records);
    }

    /**
     * Gets all the records matching a provided level
     * 
     * @param level such as INFO or ERROR
     * @return the records
     */
    public List<LogRecord> findLevelRecords(String level) {
        return findLevelRecords(level, 0, records.size());
    }

    
    /**
     * Gets all the records matching a provided level
     * 
     * @param level such as INFO or ERROR
     * @param start position in the list where to start searching from
     * @param max max number of records
     * @return the records
     */
    public List<LogRecord> findLevelRecords(String level, int start, int max) {
        if (start > records.size()) {
            throw new IllegalArgumentException();
        }
        List<LogRecord> recs = new ArrayList<LogRecord>();
        for (int i = start; i < records.size() && recs.size() <= max; i++) {
            LogRecord record = records.get(i);
            if (level.equalsIgnoreCase(record.getLevel())) {
                recs.add(record);
            }
        }
        return recs;
    }

    /**
     * Gets all the records matching a provided category
     * 
     * @param category the category such as OsgiServiceFactoryBean, may be a regular expression
     * @return the records
     */
    public List<LogRecord> findCategoryRecords(String category) {
        return findCategoryRecords(category, 0, records.size());
    }

    /**
     * Gets all the records matching a provided category
     * 
     * @param category the category such as OsgiServiceFactoryBean, may be a regular expression
     * @param start position in the list where to start searching from
     * @param max max number of records
     * @return the records
     */
    public List<LogRecord> findCategoryRecords(String category, int start, int max) {
        if (start > records.size()) {
            throw new IllegalArgumentException();
        }
        List<LogRecord> recs = new ArrayList<LogRecord>();
        for (int i = start; i < records.size() && recs.size() <= max; i++) {
            LogRecord record = records.get(i);
            if (matches(record.getCategory(), category)) {
                recs.add(record);
            }
        }
        return recs;
    }

    /**
     * Gets all the records matching provided level, category
     * 
     * @param level such as INFO or ERROR
     * @param category the category such as OsgiServiceFactoryBean, may be a regular expression
     * @return the records
     */
    public List<LogRecord> findRecords(String level, String category) {
        return findRecords(level, category, 0, records.size());
    }

    /**
     * Gets all the records matching provided level and category
     * 
     * @param level such as INFO or ERROR
     * @param category the category such as OsgiServiceFactoryBean, may be a regular expression
     * @param start position in the list where to start searching from
     * @param max max number of records
     * @return the records
     */
    public List<LogRecord> findRecords(String level, String category, int start, int max) {
        if (start > records.size()) {
            throw new IllegalArgumentException();
        }
        List<LogRecord> recs = new ArrayList<LogRecord>();
        for (int i = start; i < records.size() && recs.size() <= max; i++) {
            LogRecord record = records.get(i);
            if (level.equalsIgnoreCase(record.getLevel()) && matches(record.getCategory(), category)) {
                recs.add(record);
            }
        }
        return recs;
    }

    /**
     * Gets all the records matching provided level, category and classname
     * 
     * @param level such as INFO or ERROR
     * @param category the category such as OsgiServiceFactoryBean, may be a regular expression
     * @param className the className, may be a regular expression
     * @return the records
     */
    public List<LogRecord> findRecords(String level, String category, String className) {
        return findRecords(level, category, className, 0, records.size());
    }

    /**
     * Gets all the records matching provided level and category
     * 
     * @param level such as INFO or ERROR
     * @param category the category such as OsgiServiceFactoryBean, may be a regular expression
     * @param className the className, may be a regular expression
     * @param start position in the list where to start searching from
     * @param max max number of records
     * @return the records
     */
    public List<LogRecord> findRecords(String level, String category, String className, int start, int max) {
        if (start > records.size()) {
            throw new IllegalArgumentException();
        }
        List<LogRecord> recs = new ArrayList<LogRecord>();
        for (int i = start; i < records.size() && recs.size() <= max; i++) {
            LogRecord record = records.get(i);
            if (level.equalsIgnoreCase(record.getLevel()) && matches(record.getCategory(), category)
                && matches(record.getClassName(), className)) {
                recs.add(record);
            }
        }
        return recs;
    }

    /**
     * Gets all the records using a predicate
     * 
     * @param Predicate
     * @param start position in the list where to start searching from
     * @param max max number of records
     * @return the records
     */
    public List<LogRecord> findWithPredicate(Predicate<LogRecord> p) {
        return  findWithPredicate(p, 0, records.size());
    }
    
    /**
     * Gets all the records using a predicate
     * 
     * @param Predicate
     * @param start position in the list where to start searching from
     * @param max max number of records
     * @return the records
     */
    public List<LogRecord> findWithPredicate(Predicate<LogRecord> p, int start, int max) {
        if (start > records.size()) {
            throw new IllegalArgumentException();
        }
        List<LogRecord> recs = new ArrayList<LogRecord>();
        for (int i = start; i < records.size() && recs.size() <= max; i++) {
            LogRecord record = records.get(i);
            if (p.apply(record)) {
                recs.add(record);
            }
        }
        return recs;
    }
    
    
    private boolean matches(String value, String regex) {
        if (value == null) {
            return false;
        }
        return value.matches(regex);
    }

    /**
     * Reads the log file and converts the matching records into LogRecords Huge logs may be handled
     * differently, for ex, by copying LogRecords into db tables or using NIO file channels, etc
     * 
     * @param r log reader
     */
    private void parseLog(Reader r) {
        BufferedReader reader = new BufferedReader(r);
        try {
            String s = null;
            StringBuilder sb = new StringBuilder();
            boolean validRecordAdded = false;
            while ((s = reader.readLine()) != null) {
                boolean possibleMatch = logParser.isPossibleMatch(s);
                if (!validRecordAdded && possibleMatch) {
                    sb.append(s);
                    validRecordAdded = true;
                } else if (!possibleMatch) {
                    // it appears to be a multi-line log message
                    sb.append(s);
                } else {
                    addRecord(sb.toString());
                    sb.delete(0, sb.length());
                    sb.append(s);
                }
            }
            if (sb.length() > 0) {
                addRecord(sb.toString());
            }
        } catch (IOException ex) {
            LOG.warn("Problems reading the logs file, log records may not be available");
        }
    }

    protected void addRecord(String s) {
        LogRecord rec = logParser.parseRecord(s);
        if (rec != null) {
            records.add(rec);
        }
    }

    private Reader toReader(InputStream is) {
        Reader r = null;
        try {
            r = new InputStreamReader(is, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            LOG.fatal("This should not've happened : UTF-8 is unsupported");
            throw new RuntimeException(ex);
        }
        return r;
    }

}
