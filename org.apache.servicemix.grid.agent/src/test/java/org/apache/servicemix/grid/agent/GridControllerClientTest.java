package org.apache.servicemix.grid.agent;

import junit.framework.TestCase;

import org.apache.servicemix.grid.common.GridClient;
import org.apache.servicemix.grid.common.dto.ProfileDetails;
import org.easymock.EasyMock;

public class GridControllerClientTest extends TestCase {
    public void testAddNoProfiles() throws Exception {
        GridControllerClient gcc = new GridControllerClient();
        GridClient gc = EasyMock.createMock(GridClient.class);
        // none of these as we dont expect these calls: gc.addProfile(xxx);
        EasyMock.replay(gc);
        
        gcc.setClient(gc);
        assertSame(gc, gcc.getClient());
        
        gcc.addProfiles();
        EasyMock.verify(gc);
    }

    public void testAddProfiles() throws Exception {
        ProfileDetails pd1 = new ProfileDetails("TESTING");
        ProfileDetails pd2 = new ProfileDetails("production");
        
        GridControllerClient gcc = new GridControllerClient();
        GridClient gc = EasyMock.createMock(GridClient.class);
        gc.addProfile(pd1);
        gc.addProfile(pd2);
        EasyMock.replay(gc);
                
        gcc.setClient(gc);
        gcc.addProfiles(pd1, pd2);
        EasyMock.verify(gc);        
    }
}
