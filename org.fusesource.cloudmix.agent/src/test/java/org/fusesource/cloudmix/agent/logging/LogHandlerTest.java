package org.fusesource.cloudmix.agent.logging;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.List;

import junit.framework.TestCase;

public class LogHandlerTest extends TestCase {

    private String records;

    public void setUp() throws Exception {
        InputStream ins = getClass().getResourceAsStream("/logging/log.records");
        BufferedReader reader = new BufferedReader(new InputStreamReader(ins));
        StringBuilder b = new StringBuilder();
        String s;
        while ((s = reader.readLine()) != null) {
            b.append(s).append("\n");
        }
        records = b.toString();
        reader.close();
    }
    
    public void testGetOneInfoRecord() {
        LogHandler parser = new LogHandler(new StringReader(records));
        List<LogRecord> recs = parser.findLevelRecords("INFO", 0, 1);
        assertEquals("1 record is expected", 1, recs.size());
        LogRecord rec = recs.get(0);
        verifyRecord(rec, "15:55:03", "INFO", "FelixStartLevel", "OsgiServiceFactoryBean",
                     "r.support.OsgiServiceFactoryBean", "299",
                     "Publishing service under classes [{org.osgi.service.url.URLStreamHandlerService}]");
    }

    public void testGetOneInfoRecord2() {
        LogHandler parser = new LogHandler(new StringReader(records));
        List<LogRecord> recs = parser.findLevelRecords("INFO", 0, 2);
        assertEquals("1 record is expected", 1, recs.size());
        LogRecord rec = recs.get(0);
        verifyRecord(rec, "15:55:03", "INFO", "FelixStartLevel", "OsgiServiceFactoryBean",
                     "r.support.OsgiServiceFactoryBean", "299",
                     "Publishing service under classes [{org.osgi.service.url.URLStreamHandlerService}]");
    }

    public void testGetOneInfoRecord3() {
        LogHandler parser = new LogHandler(new StringReader(records));
        try {
            parser.findLevelRecords("INFO", 6, 2);
            fail("Only 5 records are available");
        } catch (IllegalArgumentException ex) {
            // expected
        }
    }

    public void testGetNoWarnRecords() {
        LogHandler parser = new LogHandler(new StringReader(records));
        List<LogRecord> recs = parser.findLevelRecords("WARN", 5, 1);
        assertEquals("Mo records expected", 0, recs.size());
    }

    public void testGetInfoCategoryRecord() {
        LogHandler parser = new LogHandler(new StringReader(records));
        List<LogRecord> recs = parser.findRecords("INFO", "OsgiServiceFactoryBean");
        assertEquals("1 record is expected", 1, recs.size());
        LogRecord rec = recs.get(0);
        verifyRecord(rec, "15:55:03", "INFO", "FelixStartLevel", "OsgiServiceFactoryBean",
                     "r.support.OsgiServiceFactoryBean", "299",
                     "Publishing service under classes [{org.osgi.service.url.URLStreamHandlerService}]");
    }
    
    public void testGetInfoCategoryRecordWithPredicate() {
        LogHandler parser = new LogHandler(new StringReader(records));
        LogPredicate p = new LogPredicate("INFO", "OsgiServiceFactoryBean");
        List<LogRecord> recs = parser.findWithPredicate(p);
        assertEquals("1 record is expected", 1, recs.size());
        LogRecord rec = recs.get(0);
        verifyRecord(rec, "15:55:03", "INFO", "FelixStartLevel", "OsgiServiceFactoryBean",
                     "r.support.OsgiServiceFactoryBean", "299",
                     "Publishing service under classes [{org.osgi.service.url.URLStreamHandlerService}]");
    }

    public void testGetInfoCategoryRecordRegEx() {
        LogHandler parser = new LogHandler(new StringReader(records));
        List<LogRecord> recs = parser.findRecords("INFO", "Osgi.*");
        assertEquals("1 record is expected", 1, recs.size());
        LogRecord rec = recs.get(0);
        verifyRecord(rec, "15:55:03", "INFO", "FelixStartLevel", "OsgiServiceFactoryBean",
                     "r.support.OsgiServiceFactoryBean", "299",
                     "Publishing service under classes [{org.osgi.service.url.URLStreamHandlerService}]");
    }

    public void testGetInfoRecords() {
        LogHandler parser = new LogHandler(new StringReader(records));
        List<LogRecord> recs = parser.findLevelRecords("INFO");
        assertEquals("1 record is expected", 1, recs.size());
        LogRecord rec = recs.get(0);
        verifyRecord(rec, "15:55:03", "INFO", "FelixStartLevel", "OsgiServiceFactoryBean",
                     "r.support.OsgiServiceFactoryBean", "299",
                     "Publishing service under classes [{org.osgi.service.url.URLStreamHandlerService}]");
    }

    public void testGetWarnRecords() {
        LogHandler parser = new LogHandler(new StringReader(records));
        List<LogRecord> recs = parser.findLevelRecords("WARN");
        assertEquals("3 records are expected", 3, recs.size());
        verifyRecord(recs.get(0),
                     "15:55:03",
                     "WARN",
                     "pool-1-thread-1",
                     "FileMonitor",
                     "x.kernel.filemonitor.FileMonitor",
                     "272",
                     "Unsupported deployment: D:\\Work\\CloudMix\\cloudmix\\"
                         + "org.fusesource.cloudmix.agent.start\\"
                         + "apache-servicemix-4.0.0\\etc\\users.properties");
        verifyRecord(recs.get(1),
                     "15:55:03",
                     "WARN",
                     "pool-1-thread-1",
                     "FileMonitor",
                     "x.kernel.filemonitor.FileMonitor",
                     "272",
                     "Unsupported deployment: D:\\Work\\CloudMix\\cloudmix\\"
                         + "org.fusesource.cloudmix.agent.start\\apache-servicemix-4.0.0"
                         + "\\etc\\system.properties");
        verifyRecord(recs.get(2),
                     "15:55:03",
                     "WARN",
                     "pool-1-thread-1",
                     "FileMonitor",
                     "x.kernel.filemonitor.FileMonitor",
                     "272",
                     "Unsupported deployment: D:\\Work\\CloudMix\\cloudmix\\"
                         + "org.fusesource.cloudmix.agent.start\\apache-servicemix-4.0.0"
                         + "\\etc\\startup.properties");
    }

    //CHECKSTYLE:OFF - allow a ton of params here
    private void verifyRecord(LogRecord rec, String date,
                              String level, String thread,
                              String cat, String cName, 
                              String line, String message) {
        //CHECKSTYLE:ON
        assertEquals(date, rec.getDate());
        assertEquals(level, rec.getLevel());
        assertEquals(thread, rec.getThreadId());
        assertEquals(cat, rec.getCategory());
        assertEquals(cName, rec.getClassName());
        assertEquals(line, rec.getClassLineNumber());
        assertEquals(message, rec.getMessage());
    }

    public void testGetJettyRecordsFromServiceMixLog() {
        InputStream is = getClass().getResourceAsStream("/servicemix.log");
        try {
            LogHandler parser = new LogHandler(is);
            List<LogRecord> recs = parser.findCategoryRecords("jetty");
            assertEquals("3 records are expected", 3, recs.size());
            verifyRecord(recs.get(0), "15:55:36", "INFO", "Thread-6", "jetty",
                         ".service.internal.util.JCLLogger", "102",
                         "Logging to org.ops4j.pax.web.service.internal.util.JCLLogger@178b0f9 via "
                             + "org.ops4j.pax.web.service.internal.util.JCLLogger");
            verifyRecord(recs.get(1), "15:56:16", "INFO", "xtenderThread-68", "jetty",
                         ".service.internal.util.JCLLogger", "102", "jetty-6.1.x");
            verifyRecord(recs.get(2), "15:56:16", "INFO", "xtenderThread-68", "jetty",
                         ".service.internal.util.JCLLogger", "102",
                         "Started SelectChannelConnector@0.0.0.0:8192");
        } finally {
            try {
                is.close();
            } catch (IOException ex) {
                // ignore
            }
        }
    }
}
