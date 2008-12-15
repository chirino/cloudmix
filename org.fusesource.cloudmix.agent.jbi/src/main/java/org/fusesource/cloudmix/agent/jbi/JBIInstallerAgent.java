/**
 *  Copyright (C) 2008 Progress Software, Inc. All rights reserved.
 *  http://fusesource.com
 *
 *  The software in this package is published under the terms of the AGPL license
 *  a copy of which has been included with this distribution in the license.txt file.
 */
package org.fusesource.cloudmix.agent.jbi;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.fusesource.cloudmix.agent.Bundle;
import org.fusesource.cloudmix.agent.Feature;
import org.fusesource.cloudmix.agent.InstallerAgent;
import org.fusesource.cloudmix.agent.security.SecurityUtils;
import org.fusesource.cloudmix.common.util.FileUtils;
import org.apache.servicemix.jbi.deployment.Descriptor;
import org.apache.servicemix.jbi.deployment.DescriptorFactory;
import org.apache.servicemix.jbi.deployment.ServiceAssembly;
import org.apache.servicemix.jbi.deployment.ServiceUnit;
import org.apache.servicemix.jbi.deployment.Target;
import org.apache.servicemix.jbi.util.DOMUtil;

public class JBIInstallerAgent extends InstallerAgent {

    private static final String SA_NAME_KEY = "saName";
    
    private static final Log LOGGER = LogFactory.getLog(JBIInstallerAgent.class);
    private static final String JBI_NS = "http://java.sun.com/xml/ns/jbi/management-message";
    private static final String JBI_DESC_FILE_NAME = "META-INF/jbi.xml";
    private static final int DEFAULT_MAX_DEPLOY_ATTEMPTS = 18;
    private static final int DEFAULT_DEPLOY_ATTEMPT_DELAY = 10;
    
    private MBeanServer mbeanServer;
    private ObjectName mbeanName;
    private int maxDeployAttempts = DEFAULT_MAX_DEPLOY_ATTEMPTS;
    private int deployAttemptDelay = DEFAULT_DEPLOY_ATTEMPT_DELAY;
    
    public void setMBeanServer(MBeanServer anMbeanServer) {
        mbeanServer = anMbeanServer;        
    }

    public void setMBeanName(ObjectName name) {
        this.mbeanName = name;        
    }
    
    public void setMaxDeployAttempts(int t) {
        maxDeployAttempts = t;
    }
    
    public int getMaxDeployAttempts() {
        return maxDeployAttempts;
    }
    
    public void setDeployAttemptDelay(int t) {
        deployAttemptDelay = t;
    }
    
    public int getDeployAttemptDelay() {
        return deployAttemptDelay;
    }

    /**
     * should be careful when using this: do not use hard coded value as the ID should be somewhat guaranteed
     *  to be unique. Can be used when reloading the agent settings after a restart
     * @param anId
     */
    public void setAgentId(String anId) {
        agentId = anId;
    }

    @Override
    public boolean validateAgent() {
        if (mbeanServer == null || mbeanName == null) {
            LOGGER.error("Invalid mbean server or name");
            return false;
        }
        return true;
    }

    @Override
    protected boolean installBundle(Feature feature, Bundle bundle) {
        
        LOGGER.info("installing bundle " + bundle.getUri());
        
        // To start and undeploy a SA we need the deployed name of the SA.  
        // However the deploy() operation does not return this information so
        // we use the getDeployedServiceAssemblies() before and after the 
        // deployment and look for a new entry.  There is a risk that another 
        // SA could be deployed by some other mechanism while this current 
        // deployment is happening.
        // TODO: Now that we are reading the JBI descriptor (see targetComponentsAvailable()) 
        // we can easily get the SA name.

        Set<String> before = getDeployedSAs();
        try {
            if (!targetComponentsAvailable(bundle)) {
                return false;
            }

            String[] sig = {"java.lang.String" };
            String[] deployParams = {bundle.getUri() };
            LOGGER.debug("Calling deploy() mbean operation");
            Object ret = mbeanServer.invoke(mbeanName, "deploy", deployParams, sig);
            if (!statusOK("deploy", ret)) {
                LOGGER.error("Failed to deploy service assembly "
                        + bundle.getUri()
                        + " for feature "
                        + feature.getName());
                LOGGER.info("result: " + ret);
                return false;
            }
        } catch (Exception e) {
            LOGGER.error("Exception invoking deploy(" + bundle.getUri() + ") operation");
            LOGGER.error("exception: " + e);
            return false;
        }

        Set<String> after = getDeployedSAs();

        List<String> newSAs = getDiffs(before, after);
        if (newSAs.size() == 0) {
            LOGGER.error("No new service assemblies deployed");
            return false;
        }

        if (newSAs.size() > 1) {
            LOGGER.error("Too many new service assemblies deployed");
            // Assume its the first in the list.
        }
        String saName = newSAs.get(0);
        
        try {
            String[] sig = {"java.lang.String" };        
            LOGGER.debug("Calling start() mbean operation");
            String[] startParams = {saName };
            Object ret = mbeanServer.invoke(mbeanName, "start", startParams, sig);
            if (!statusOK("start", (String) ret)) {
                LOGGER.error("Failed to start service assembly " + saName 
                        + ", bundle " + bundle.getUri() + " for feature " + feature.getName());
                LOGGER.info("result: " + ret);
                // TODO: should we undeploy the SA now?
                return false;
            }
        } catch (Exception e) {
            LOGGER.error("Exception invoking start(" + saName + ") operation");
            LOGGER.error("exception: " + e);
            return false;
        }

        addSA(bundle, saName);

        return true;
    }



 
    @Override
    protected boolean uninstallBundle(Feature feature, Bundle bundle) {
        LOGGER.info("uninstalling bundle " + bundle.getUri());
        
        String featureName = feature.getName();
        String saName = getSAName(bundle);
        if (saName == null) {
            LOGGER.error("Cannot find SA name for bundle " + bundle.getUri());
            return false;
        }
        String[] sig = {"java.lang.String" };        
        String[] params = {saName };
        
        try {
            LOGGER.debug("Calling shutDown() mbean operation");
            Object ret = mbeanServer.invoke(mbeanName, "shutDown", params, sig);
            if (!statusOK("shutDown", (String) ret)) {
                LOGGER.error("Failed to shutdown service assembly " + saName 
                        + ", bundle " + bundle.getUri() + " for feature " + featureName);
                LOGGER.info("result: " + ret);
                return false;
            }
        } catch (Exception e) {
            LOGGER.error("Exception invoking shutDown(" + params[0] + ") operation");
            LOGGER.info("exception: " + e);
            return false;
        }
            
        try {
            LOGGER.debug("Calling undeploy() mbean operation");
            Object ret = mbeanServer.invoke(mbeanName, "undeploy", params, sig);
            if (ret != null && !statusOK("undeploy", ret)) {
                LOGGER.error("Failed to undeploy service assembly " + saName 
                        + ", bundle " + bundle.getUri() + " for feature " + featureName);
                LOGGER.info("result: " + ret);
                return false;
            }
            
        } catch (Exception e) {
            LOGGER.error("Exception invoking undeploy(" + params[0] + ") operation");
            LOGGER.info("exception: " + e);
            return false;
            
        }

        removeSA(bundle);

        return true;        
    }
    
    /*
     * Extracts the JBI descriptor from the ZIP and makes sure all target components are available.
     */
    private boolean targetComponentsAvailable(Bundle bundle) {
        File jbiDescFile = null;
        ZipInputStream zip = null;
        try {

            URL saUrl = new URL(bundle.getUri());
            LOGGER.info("checking target components for SA bundle " + saUrl);
            zip = new ZipInputStream(SecurityUtils.getInputStream(saUrl));

            boolean foundDescriptor = false;
            ZipEntry ze = zip.getNextEntry();
            while (ze != null) {
                LOGGER.debug("Zip entry " + ze.getName());
                if (!ze.isDirectory() && ze.getName().equals(JBI_DESC_FILE_NAME)) {
                    foundDescriptor = true;
                    break;
                }
                ze = zip.getNextEntry();
            }

            if (!foundDescriptor) {
                LOGGER.error("cannot find JBI descriptor (" + JBI_DESC_FILE_NAME + ") in bundle "
                        + bundle);
                return false;
            }

            jbiDescFile = File.createTempFile("jbi-agent", ".xml");
            FileOutputStream os = new FileOutputStream(jbiDescFile);
            extractZipEntry(zip, os);

            Descriptor jbiDesc = DescriptorFactory.buildDescriptor(jbiDescFile);
            ServiceAssembly sa = jbiDesc.getServiceAssembly();           
            ServiceUnit[] sus = sa.getServiceUnits();

            boolean canDeploy = true;

            int max = getMaxDeployAttempts();
            for (int i = 0; i < max; i++) {
                canDeploy = true;
                for (ServiceUnit su : sus) {
                    Target target = su.getTarget();
                    if (!checkComponentAvailability(target.getComponentName())) {
                        LOGGER.info("cannot deploy SU " + su.getIdentification().getName()
                                + " to component " + target.getComponentName());
                        canDeploy = false;
                        break;
                    } else {
                        LOGGER.info("can deploy SU " + su.getIdentification().getName()
                                + " to component " + target.getComponentName());                   
                    }
                }
                if (!canDeploy) {
                    LOGGER.warn("cannot deploy JBI bundle " + bundle + " as all target components are not available");
                    int r = max - i - 1;
                    if (r != 0) {
                        int t = getDeployAttemptDelay();
                        LOGGER.info(r + " attempts remaining, sleeping for " + t + " seconds");
                        sleep(t);
                    }
                } else {
                    break;
                }
            }
            return canDeploy;

        } catch (Exception e) {
            LOGGER.error("Got exception checking availability of target components for bundle "
                    + bundle, e);
            return false;
        } finally {
            if (jbiDescFile != null) {
                LOGGER.info("deleting " + jbiDescFile);
                jbiDescFile.delete();
            }
            if (zip != null) {
                try {
                    zip.close();
                } catch (IOException e) {
                    // Complete.
                }
            }
        }
    }



    private boolean checkComponentAvailability(String componentName) {
        
        LOGGER.info("checking component " + componentName);
                   
        String[] sig = {"java.lang.String" };
        String[] canDeployParams = { componentName };
        Object ret;
        try {
            ret = mbeanServer.invoke(mbeanName, "canDeployToComponent", canDeployParams, sig);
        } catch (Exception e) {
            LOGGER.error("Exception inovking mbean canDeployToCompoent(" + componentName + ")", e);
            return false;
        }
        if (ret instanceof Boolean) {
            return ((Boolean) ret).booleanValue();
        } else {
            LOGGER.error("unexpected result from canDeployToComponent: " + ret);
            return false;
        }
    }
    
    private Set<String> getDeployedSAs() {
        
        try {
            String[] sig = {};
            String[] params = {};
            Object ret = mbeanServer.invoke(mbeanName, "getDeployedServiceAssemblies", params, sig);
            if (ret instanceof String[]) {
                Set<String> set = new HashSet<String>();
                for (String s : (String[]) ret) {
                    set.add(s);                    
                }
                return set;              
            }
        } catch (Exception e) {
            LOGGER.error("Exception invoking getDeployedServiceAssemblies() operation");
            LOGGER.error("exception: " + e);            
        }
        return null;
    }
    
    private List<String> getDiffs(Set<String> before, Set<String> after) {
        List<String> diffs = new ArrayList<String>();
        for (String s : after) {
            if (!before.contains(s)) {
                diffs.add(s);
            }            
        }
        return diffs;
    }

    private String getSAName(Bundle bundle) {
        return (String) bundle.getAgentProperties().get(SA_NAME_KEY);
    }
    
    private void addSA(Bundle bundle, String saName) {
        
        String oldName = getSAName(bundle);
        if (oldName != null) {
            LOGGER.warn("Bundle " + bundle.getUri() + " is already deployed as SA " + oldName);
        }
        
        LOGGER.info("SA name for bundle " + bundle.getUri() + " is " + saName);
        bundle.getAgentProperties().put(SA_NAME_KEY, saName);
    }

    private void removeSA(Bundle bundle) {
        bundle.getAgentProperties().remove(SA_NAME_KEY);
    }


    private boolean statusOK(String opName, Object result) {

        if (!(result instanceof String)) {
            LOGGER.error("result is not a string; class is " + result.getClass().getName());
            return false;
        }
        
        try {
            Document doc = parse((String) result);

            Element e = getElement(doc, "jbi-task");
            e = getChildElement(e, "jbi-task-result");
            e = getChildElement(e, "frmwk-task-result");
            e = getChildElement(e, "frmwk-task-result-details");
            Element details = getChildElement(e, "task-result-details");
            e = getChildElement(details, "task-result");
            String status = DOMUtil.getElementText(e);
            
            e = getChildElement(details, "task-id");
            String taskId = DOMUtil.getElementText(e);

            if (!opName.equals(taskId)) {
                LOGGER.warn("Mismatch between operation name (" + opName
                        + ") and task-id (" + taskId + ")");
            }

            return "SUCCESS".equals(status);

        } catch (Exception e) {
            LOGGER.error("Error parsing XML document; exception " + e);
            LOGGER.error("XML Document: " + result);
        }
        return false;
    }
   
    private Document parse(String result) throws ParserConfigurationException, SAXException, IOException {
        
        LOGGER.debug("Parsing XML Document:\n" + result);

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setIgnoringElementContentWhitespace(true);
        factory.setIgnoringComments(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(new InputSource(new StringReader(result)));
    }

    private Element getElement(Document doc, String name) {
        NodeList nl = doc.getElementsByTagNameNS(JBI_NS, name);
        return (Element) nl.item(0);
    }   

    private Element getChildElement(Element e, String name) {
        NodeList nl = e.getElementsByTagNameNS(JBI_NS, name);
        return (Element) nl.item(0);
    }

    private void extractZipEntry(ZipInputStream zip, OutputStream os) throws IOException {
        final byte[] buffer = new byte[FileUtils.BUFFER_SIZE];
        
        try {
            long total = 0;
            int n = zip.read(buffer);
            while (n != -1) {
                total += n;
                os.write(buffer, 0, n);
                n = zip.read(buffer);
            }
            LOGGER.info("Copied " + total + " bytes");
        } finally {
            zip.closeEntry();
            os.close();
        }
    }
    
    private void sleep(int sleepTime) {
        try {
            Thread.sleep(sleepTime * 1000);
        } catch (InterruptedException e) {
            // Complete
        }
    }

    
    
}
