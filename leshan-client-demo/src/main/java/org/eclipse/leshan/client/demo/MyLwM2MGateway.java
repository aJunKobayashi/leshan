package org.eclipse.leshan.client.demo;

import org.eclipse.leshan.client.resource.BaseInstanceEnabler;
import org.eclipse.leshan.client.servers.ServerIdentity;
import org.eclipse.leshan.core.model.ObjectModel;
import org.eclipse.leshan.core.node.ObjectLink;
import org.eclipse.leshan.core.response.ReadResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

public class MyLwM2MGateway  extends BaseInstanceEnabler {
    private String mDeviceID;
    private String mPrefix;
    private ObjectLink mRoutingTableEntry;

    private static final List<Integer> supportedResources = Arrays.asList(0, 1, 2);

    private static final Logger LOG = LoggerFactory.getLogger(MyLwM2MGateway.class);

    private enum ResourceID {
        DEVICE_ID(0),
        PREFIX(1),
        ROUTING_TABLE_ENTRY(2),
        UNKNOWN(-1);

        private final int val;
        ResourceID(int i) {
            val = i;
        }

        public static MyLwM2MGateway.ResourceID convertID(int resourceID) {
            for (MyLwM2MGateway.ResourceID id: MyLwM2MGateway.ResourceID.values()) {
                if (id.val == resourceID) {
                    return id;
                }
            }
            return UNKNOWN;
        }
    }

    public MyLwM2MGateway() {
        this("testDeviceID", "testPrefix", new ObjectLink(3, 4));
    }

    public MyLwM2MGateway(String deviceID, String prefix, ObjectLink routingTableEntry) {
        this.mDeviceID = deviceID;
        this.mPrefix = prefix;
        this.mRoutingTableEntry = routingTableEntry;
    }

    @Override
    public ReadResponse read(ServerIdentity identity, int resourceid) {
        LOG.info("Read on Gateway resource /{}/{}/{}", getModel().id, getId(), resourceid);
        MyLwM2MGateway.ResourceID id = MyLwM2MGateway.ResourceID.convertID(resourceid);
        switch (id) {
            case DEVICE_ID:
                return ReadResponse.success(resourceid, mDeviceID);
            case PREFIX:
                return ReadResponse.success(resourceid, mPrefix);
            case ROUTING_TABLE_ENTRY:
                return ReadResponse.success(resourceid, mRoutingTableEntry);
            default:
                return super.read(identity, resourceid);
        }
    }

    @Override
    public List<Integer> getAvailableResourceIds(ObjectModel model) {
        return supportedResources;
    }
}
