package org.fusesource.cloudmix.agent.logging;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "log")
public class LogRecord {

    private String message;
    private String date;
    private String threadId;
    private String category;
    private String level;
    private String className;
    private String classLineNumber;

    public LogRecord() {
    }

    public LogRecord(String level, String category) {
        this.level = level;
        this.category = category;
    }

    public LogRecord(String level, String category, String className) {
        this(level, category);
        this.className = className;
    }

    public LogRecord(String level, String category, String className, String date) {
        this(level, category, className);
        this.date = date;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDate() {
        return date;
    }

    public void setThreadId(String threadId) {
        this.threadId = threadId;
    }

    public String getThreadId() {
        return threadId;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCategory() {
        return category;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getClassName() {
        return className;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getLevel() {
        return level;
    }

    public void setClassLineNumber(String classLineNumber) {
        this.classLineNumber = classLineNumber;
    }

    public String getClassLineNumber() {
        return classLineNumber;
    }

}
