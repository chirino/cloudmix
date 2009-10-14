/**
 *  Copyright (C) 2008 Progress Software, Inc. All rights reserved.
 *  http://fusesource.com
 *
 *  The software in this package is published under the terms of the AGPL license
 *  a copy of which has been included with this distribution in the license.txt file.
 */
package org.fusesource.cloudmix.agent.logging;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * @version $Revision$
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class LogRecordList {

    @XmlElement(name = "log", required = false)
    private List<LogRecord> records;

    public LogRecordList() {
        this.records = new ArrayList<LogRecord>();
    }

    public LogRecordList(List<LogRecord> records) {
        this.records = records;
    }

    @Override
    public String toString() {
        return "LogRecordList" + records;
    }


    // Properties
    //-------------------------------------------------------------------------

    public List<LogRecord> getRecords() {
        return records;
    }

    public void setRecords(List<LogRecord> records) {
        this.records = records;
    }

}