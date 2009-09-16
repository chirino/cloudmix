/**
 *  Copyright (C) 2008 Progress Software, Inc. All rights reserved.
 *  http://fusesource.com
 *
 *  The software in this package is published under the terms of the AGPL license
 *  a copy of which has been included with this distribution in the license.txt file.
 */
package org.fusesource.cloudmix.common.dto;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @version $Revision$
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class FeatureDetails extends IdentifiedType {
    @XmlAttribute
    private String resource;
    @XmlAttribute
    private String minimumInstances = "1";
    @XmlAttribute
    private String maximumInstances = "1";
    @XmlAttribute
    private Boolean ownsMachine = Boolean.FALSE;
    @XmlElement(name = "dependency")
    private List<Dependency> dependencies = new ArrayList<Dependency>();
    @XmlElement(name = "preferredMachine")
    private Set<String> preferredMachines = new HashSet<String>();
    @XmlAttribute
    private String [] packageTypes;
    @XmlAttribute(required = false)
    private String ownedByProfileId;

    public FeatureDetails() {
    }

    public FeatureDetails(String id) {
        super(id);
    }

    public FeatureDetails(String id, String resource) {
        super(id);
        this.resource = resource;
    }
    
    public long getDigest() {
        long rc = 17;

        // need to make sure that the order in which these lists are processed is not significant
        for (Dependency dep : dependencies) {
            // rc = 37 * rc + dep.getDigest() is not good because it is order-significant
            rc += dep.getDigest(); 
        }
        for (String pm : preferredMachines) {
            rc += pm.hashCode();
        }

        rc = 37 * rc + minimumInstances.hashCode();
        rc = 37 * rc + maximumInstances.hashCode();
        rc = 37 * rc + (ownsMachine.booleanValue() ? 1 : 2);
        return rc;
    }

    @Override
    public String toString() {
        return "Feature[id: " + getId()
                    + " min: " + getMinimumInstances()
                    + " max: " + getMaximumInstances()
                    + "]";
    }

    public void addPreferredMachine(String preferredMachine) {
        getPreferredMachines().add(preferredMachine);
    }

    // Fluent API
    //-------------------------------------------------------------------------

    public FeatureDetails depends(FeatureDetails feature) {
        return depends(feature.getId());
    }

    public FeatureDetails depends(String featureId) {
        Dependency answer = new Dependency();
        answer.setFeatureId(featureId);
        dependencies.add(answer);
        return this;
    }

    public FeatureDetails maximumInstances(String aNumber) {
        setMaximumInstances(aNumber);
        return this;
    }

    public FeatureDetails minimumInstances(String aNumber) {
        setMinimumInstances(aNumber);
        return this;
    }

    public FeatureDetails preferredMachine(String preferredMachine) {
        addPreferredMachine(preferredMachine);
        return this;
    }

    public FeatureDetails ownsMachine() {
        setOwnsMachine(Boolean.TRUE);
        return this;
    }

    public boolean isOwnsMachine() {
        return ownsMachine != null && ownsMachine.booleanValue();
    }

    // Properties
    //-------------------------------------------------------------------------

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public List<Dependency> getDependencies() {
        return dependencies;
    }

    /**
     * Sets the dependent features which must be provisioned (to their minimum instance count)
     * before this feature can be provisioned
     */
    public void setDependencies(List<Dependency> dependencies) {
        this.dependencies = dependencies;
    }

    public String getMaximumInstances() {
        return maximumInstances;
    }

    /**
     * Sets the maximum number of instances of the feature as an expression so that it can be
     * dynamic based on the number of agents or instances of some other feature et
     */
    public void setMaximumInstances(String maximumInstances) {
        this.maximumInstances = maximumInstances;
    }

    public String getMinimumInstances() {
        return minimumInstances;
    }

    /**
     * Sets the minimum number of instances of the feature as an expression so that it can be
     * dynamic based on the number of agents or instances of some other feature et
     */
    public void setMinimumInstances(String minimumInstances) {
        this.minimumInstances = minimumInstances;
    }

    public Set<String> getPreferredMachines() {
        return preferredMachines;
    }

    /**
     * Sets list of preferred machine host names on which the feature should be provisioned
     */
    public void setPreferredMachines(Set<String> preferredMachines) {
        this.preferredMachines = preferredMachines;
    }

    public Boolean getOwnsMachine() {
        return ownsMachine;
    }

    /**
     * Sets whether or not this feature owns the entire machine. i.e. an empty agent is required to provision
     * this feature and no other features are allowed to be provisioned on the same agent once this feature is
     * provisioned
     */
    public void setOwnsMachine(Boolean ownsMachine) {
        this.ownsMachine = ownsMachine;
    }

    public String[] getPackageTypes() {
        return packageTypes;
    }

    public void setPackageTypes(String[] packageTypes) {
        this.packageTypes = packageTypes;
    }

    public String getOwnedByProfileId() {
        return ownedByProfileId;
    }

    /**
     * Sets the profile which is the exclusive owner of this feature (for features which are 
     * local only to a single profile) otherwise leave null so that the feature can be used 
     * in any profile.
     *
     * If a feature is associated with a single profile, then deleting a profile should also 
     * delete the associated features. This is used when integration testing, where a profile 
     * is created temporarily, a number of features added for that profile - then the profile 
     * is deleted.
     *
     *
     * @param ownedByProfileId the profile which should own this feature; or null to imply 
     * the feature is used for any profile
     */
    public void setOwnedByProfileId(String ownedByProfileId) {
        this.ownedByProfileId = ownedByProfileId;
    }
}