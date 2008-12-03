package org.apache.servicemix.grid.agent;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.xml.sax.SAXException;

public class FeatureList implements Serializable {
    
    private static final long serialVersionUID = -563410326809040533L;

    private static final String JBI_TYPE = "jbi";

    private static final String JBI_URL_PREFIX = "jbi:";
    
    private Map<String, Feature> features;

    public FeatureList(URL url, String credentials) throws IOException {
        load(url, credentials);
    }

    public FeatureList(String url, String credentials) throws IOException {
        
        load(new URL(url), credentials);
    }
    
    // Based on SMX4 org.apache.servicemix.gshell.features.internal.RepositoryImpl.load()        
    private void load(URL url, String credentials) throws IOException {

        try {
            URLConnection conn = url.openConnection();
            if (credentials != null) {
                conn.addRequestProperty("Authorization", credentials);
            }
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
                    System.out.println(" ===>> loading property batch: " + cfgName);
                    String data = c.getTextContent();
                    Properties properties = new Properties();
                    properties.load(new ByteArrayInputStream(data.getBytes()));
                    f.addProperties(cfgName, properties);
                }
                
                NodeList bundleNodes = e.getElementsByTagName("bundle");
                for (int j = 0; j < bundleNodes.getLength(); j++) {
                    Element b = (Element) bundleNodes.item(j);
                    String uri = b.getTextContent();
                    Bundle bundle = new Bundle(b.getAttribute("name"), b.getAttribute("type"), uri);
                    f.addBundle(bundle);
                }
                features.put(name, f);
            }
        } catch (SAXException e) {
            throw (IOException) new IOException().initCause(e);
        } catch (ParserConfigurationException e) {
            throw (IOException) new IOException().initCause(e);
        }
        
    }

    public int getNumFeatures() {
        return features.size();
    }
    
    public Feature getFeature(String featureName) {
        return features.get(featureName);
    }
    
    public String toServiceMix4Doc() {
        StringBuilder sb = new StringBuilder().append("<features>\n");
        for (Feature feature : features.values()) {
            sb.append("  <feature name=\"")
                  .append(feature.getName()).append("\">\n");
            for (String pn : feature.getPropertyNames()) {
                sb.append("    <config name=\"")
                    .append(pn).append("\">\n");
                Properties props = feature.getProperties(pn);
                for (Object o : props.keySet()) {
                    sb.append("      ").append(o)
                      .append(" = ").append(props.get(o)).append("\n");
                }
                sb.append("    </config>\n");
            }
            for (Bundle b : feature.getBundles()) {
                sb.append("    <bundle>");
                String type = b.getType();
                if (JBI_TYPE.equals(type)) {
                    sb.append(JBI_URL_PREFIX);
                }
                sb.append(b.getUri()).append("</bundle>\n");                
            }
                    
            sb.append("  </feature>\n");
        }
        sb.append("</features>\n");
        return sb.toString();
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
