package org.fusesource.cloudmix.agent.logging;

import com.google.common.base.Predicate;

public class LogPredicate implements Predicate<LogRecord> {

    private LogRecord r = new LogRecord(); 
    
    public LogPredicate(LogRecord searchRecord) {
        if (isEmpty(searchRecord)) {
            throw new IllegalArgumentException("Empty LogRecord");
        }
        this.r = searchRecord;    
    }
    
    public LogPredicate(String level) {
        r.setLevel(level);
    }
    
    public LogPredicate(String level, String category) {
        r.setLevel(level);
        r.setCategory(category);
    }
    
    public LogPredicate(String level, String category, String date) {
        r.setLevel(level);
        r.setCategory(category);
        r.setDate(date);
    }
    
    public boolean apply(LogRecord t) {
        if (t == null || isEmpty(t)) {
            return false;
        }
        if ((r.getLevel() == null || r.getLevel() != null && r.getLevel().equals(t.getLevel()))
            && (r.getDate() == null || r.getDate() != null && r.getDate().equals(t.getDate()))
            && (r.getCategory() == null || t.getCategory().matches(r.getCategory()))
            && (r.getClassName() == null || t.getClassName().matches(r.getClassName()))
            && (r.getMessage() == null || t.getMessage().matches(r.getMessage()))) {
            return true;
        }
        return false;
    }

    private static boolean isEmpty(LogRecord searchRecord) {
        if (searchRecord.getLevel() == null 
            && searchRecord.getDate() == null
            && searchRecord.getCategory() == null
            && searchRecord.getClassName() == null
            && searchRecord.getThreadId() == null
            && searchRecord.getClassLineNumber() == null) {
            return true;
        }
        return false;
    }
}
