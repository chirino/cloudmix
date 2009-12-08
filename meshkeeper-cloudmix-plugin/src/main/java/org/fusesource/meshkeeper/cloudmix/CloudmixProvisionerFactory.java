/**
 *  Copyright (C) 2009 Progress Software, Inc. All rights reserved.
 *  http://fusesource.com
 *
 *  The software in this package is published under the terms of the AGPL license
 *  a copy of which has been included with this distribution in the license.txt file.
 */
package org.fusesource.meshkeeper.cloudmix;

import org.fusesource.meshkeeper.distribution.provisioner.Provisioner;
import org.fusesource.meshkeeper.distribution.provisioner.ProvisionerFactory;

/**
 * CloudmixProvisionerFactory
 * <p>
 * Description:
 * </p>
 * 
 * @author cmacnaug
 * @version 1.0
 */
public class CloudmixProvisionerFactory extends ProvisionerFactory {

    /*
     * (non-Javadoc)
     * 
     * @seeorg.fusesource.meshkeeper.distribution.registry.RegistryFactory#
     * createRegistry(java.lang.String)
     */
    @Override
    protected Provisioner createPlugin(String uri) throws Exception {
        CloudMixProvisioner provisioner = new CloudMixProvisioner();
        if (uri.length() > 0) {
            provisioner.setDeploymentUri(uri);
        }
        return provisioner;

    }
}
