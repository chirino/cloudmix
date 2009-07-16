/**
 *  Copyright (C) 2008 Progress Software, Inc. All rights reserved.
 *  http://fusesource.com
 *
 *  The software in this package is published under the terms of the AGPL license
 *  a copy of which has been included with this distribution in the license.txt file.
 */
package org.fusesource.cloudmix.agent;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.net.URLConnection;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.xml.sax.SAXException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class FeatureList implements Serializable {
    private static final transient Log LOG = LogFactory.getLog(FeatureList.class);
    private static final long serialVersionUID = -563410326809040533L;

    private Map<String, Feature> features;

    public FeatureList(URL url, String credentials) throws IOException {
        load(url, credentials);
    }

    public FeatureList(String url, String credentials) throws IOException {
        URL urlObject = null;
        try {
            urlObject = new URL(url);
        } catch (MalformedURLException e) {
            LOG.warn("Could not create URL for: " + url + ". Reason: " + e);
            throw e;
        }
        load(urlObject, credentials);
    }
    
    // Based on SMX4 org.apache.servicemix.gshell.features.internal.RepositoryImpl.load()        
    private void load(URL url, String credentials) throws IOException {

        try {
            URLConnection conn = url.openConnection();
            if (credentials != null) {
                conn.addRequestProperty("Authorization", credentials);
            }
            System.out.println("URL is: " + url);
            InputStream is = conn.getInputStream();
            features = new HashMap<String, Feature>();
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            Document doc = factory.newDocumentBuilder().parse(is);
            NodeList nodes = doc.getDocumentElement().getChildNodes();
            for (int i = 0; i < nodes.getLength(); i++) {
                Node node = nodes.item(i);
                if (!(node instanceof Element) || !"feature".equals(node.getNodeName())) {
                    continue;
                }
                Element e = (Element) nodes.item(i);
                String name = e.getAttribute("name");
                Feature f = new Feature(name, this);
                
                NodeList featureNodes = e.getElementsByTagName("feature");
                for (int j = 0; j < featureNodes.getLength(); j++) {
                    Element b = (Element) featureNodes.item(j);
                    f.addDependency(b.getTextContent());
                }
                
                NodeList configNodes = e.getElementsByTagName("config");
                for (int j = 0; j < configNodes.getLength(); j++) {
                    
                    Element c = (Element) configNodes.item(j);
                    String cfgName = c.getAttribute("name");
                    String data = c.getTextContent();
                    Properties properties = new Properties();
                    properties.load(new ByteArrayInputStream(data.getBytes()));
                    f.addProperties(cfgName, properties);
                }
                
                NodeList bundleNodes = e.getElementsByTagName("bundle");
                for (int j = 0; j < bundleNodes.getLength(); j++) {
                    Element b = (Element) bundleNodes.item(j);
                    f.addBundle(extractBundleInfo(b));
                }
                features.put(name, f);
            }
        } catch (SAXException e) {
            throw (IOException) new IOException().initCause(e);
        } catch (ParserConfigurationException e) {
            throw (IOException) new IOException().initCause(e);
        }
        
    }

    protected Bundle extractBundleInfo(Element b) {
        Bundle bundle = new Bundle(b.getAttribute("name"),
                                   b.getAttribute("type"),
                                   b.getAttribute("uri"));
        
        NodeList depNodes = b.getElementsByTagName("depends");
        for (int k = 0; k < depNodes.getLength(); k++) {
            Element d = (Element) depNodes.item(k);
            String depUri = d.getTextContent();
            bundle.addDepUri(depUri);
        }
        return bundle;
    }

    public int getNumFeatures() {
        return features.size();
    }
    
    public Feature getFeature(String featureName) {
        return features.get(featureName);
    }
    
    public List<Feature> getAllFeatures() {
        return new ArrayList<Feature>(features.values());
    }
    
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("features {\n");
        for (String s : features.keySet()) {
            sb.append(features.get(s));
        }
        sb.append("}");
        return sb.toString();
    }

}
