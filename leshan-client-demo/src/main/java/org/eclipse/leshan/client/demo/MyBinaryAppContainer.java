package org.eclipse.leshan.client.demo;

import org.eclipse.leshan.client.resource.BaseInstanceEnabler;
import org.eclipse.leshan.client.servers.ServerIdentity;
import org.eclipse.leshan.core.model.ObjectModel;
import org.eclipse.leshan.core.model.ResourceModel;
import org.eclipse.leshan.core.node.LwM2mResource;
import org.eclipse.leshan.core.node.LwM2mResourceInstance;
import org.eclipse.leshan.core.node.ObjectLink;
import org.eclipse.leshan.core.response.ReadResponse;
import org.eclipse.leshan.core.response.WriteResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.*;

public class MyBinaryAppContainer  extends BaseInstanceEnabler {
    private byte[][] mData;
    private int mDataPriority;
    private Date mDataCreationTime;
    private String mDataDescription;
    private String mDataFormat;
    private int mAppID;


    private static final List<Integer> supportedResources = Arrays.asList(0, 1, 2, 3, 4, 5);

    private static final Logger LOG = LoggerFactory.getLogger(MyBinaryAppContainer.class);

    private enum ResourceID {
        DATA(0),
        DATA_PRIORITY(1),
        DATA_CREATION_TIME(2),
        DATA_DESCRIPTION (3),
        DATA_FORMAT(4),
        APP_ID(5),
        UNKNOWN(-1);

        private final int val;
        ResourceID(int i) {
            val = i;
        }

        public static MyBinaryAppContainer.ResourceID convertID(int resourceID) {
            for (MyBinaryAppContainer.ResourceID id: MyBinaryAppContainer.ResourceID.values()) {
                if (id.val == resourceID) {
                    return id;
                }
            }
            return UNKNOWN;
        }
    }

    public MyBinaryAppContainer() {
        byte[] data1 = "test1".getBytes(StandardCharsets.UTF_8);
        byte[] data2 = "apple".getBytes(StandardCharsets.UTF_8);
        byte[] data3 = "orange".getBytes(StandardCharsets.UTF_8);
        byte[][] datas = new byte[3][];
        datas[0] = data1;
        datas[1] = data2;
        datas[2] = data3;
        Date date = new Date();
        this.mData = datas;
        this.mDataPriority = 1;
        this.mDataCreationTime = new Date();
        this.mDataDescription = "description";
        this.mDataFormat = "plainText";
        this.mAppID = 3;
    }

    public MyBinaryAppContainer(byte[][] data, int dataPriority, Date dataCreationTime, String dataDescription, String dataFormat, int appID) {
        this.mData = data;
        this.mDataPriority = dataPriority;
        this.mDataCreationTime = dataCreationTime;
        this.mDataDescription = dataDescription;
        this.mDataFormat = dataFormat;
        this.mAppID = appID;
    }

    @Override
    public ReadResponse read(ServerIdentity identity, int resourceid) {
        LOG.info("Read on Gateway resource /{}/{}/{}", getModel().id, getId(), resourceid);
        MyBinaryAppContainer.ResourceID id = MyBinaryAppContainer.ResourceID.convertID(resourceid);
        switch (id) {
            case DATA:
                Map<Integer, byte[]> map = new HashMap<Integer, byte[]>();
                map.put(0, mData[0]);
                map.put(1, mData[1]);
                map.put(2, mData[2]);
                return ReadResponse.success(resourceid, map, ResourceModel.Type.OPAQUE);
            case DATA_PRIORITY:
                return ReadResponse.success(resourceid, mDataPriority);
            case DATA_CREATION_TIME:
                return ReadResponse.success(resourceid, mDataCreationTime);
            case DATA_FORMAT:
                return ReadResponse.success(resourceid, mDataFormat);
            case APP_ID:
                return ReadResponse.success(resourceid, mAppID);
            default:
                return super.read(identity, resourceid);
        }
    }

    @Override
    public WriteResponse write(ServerIdentity identity, int resourceid, LwM2mResource value) {
        LOG.info("Write on Firmware resource /{}/{}/{}", getModel().id, getId(), resourceid);
        MyBinaryAppContainer.ResourceID id = MyBinaryAppContainer.ResourceID.convertID(resourceid);
        switch(id) {
            case DATA:
                Map<Integer, LwM2mResourceInstance> map = value.getInstances();
                Set<Integer> keys = map.keySet();
                for (Iterator<Integer> n = keys.iterator(); n.hasNext();)
                {
                    Integer i =n.next();
                    byte[] newValue = (byte[])map.get(i).getValue();
                    if (!newValue.equals(mData[i])) {
                        mData[i] = newValue;
                    }
                    System.out.println(n.next());
                }
                return WriteResponse.success();
        }
        return WriteResponse.notFound();
    }

    @Override
    public List<Integer> getAvailableResourceIds(ObjectModel model) {
        return supportedResources;
    }
}
