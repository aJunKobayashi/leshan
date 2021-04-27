package org.eclipse.leshan.client.demo;

import org.eclipse.leshan.client.resource.BaseInstanceEnabler;
import org.eclipse.leshan.client.servers.ServerIdentity;
import org.eclipse.leshan.core.model.ObjectModel;
import org.eclipse.leshan.core.node.LwM2mResource;
import org.eclipse.leshan.core.response.ExecuteResponse;
import org.eclipse.leshan.core.response.ReadResponse;
import org.eclipse.leshan.core.response.WriteResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.*;


public class MyFirmware extends BaseInstanceEnabler {

    private static final Logger LOG = LoggerFactory.getLogger(MyFirmware.class);

    private static final List<Integer> supportedResources = Arrays.asList(0, 1, 2, 3, 5, 9);
    private static final Random RANDOM = new Random();

    private ArrayList<Byte> mPackage;
    private String mPackageURI;
    private int mState;
    private int mUpdateResult;
    private int mFirmwareUpdateDeliveryMethod;

    private String mPkgName = "leshanPkg";
    private String mPkgVersion = "1.0.9";

    private enum ResourceID {
        PACKAGE(0),
        PACKAGE_URI(1),
        UPDATE(2),
        STATE(3),
        UPDATE_RESULT(5),
        PACKAGE_NAME(6),
        PACKAGE_VERSION(7),
        FIRMWARE_UPDATE_DELIVERY_METHOD(9),
        UNKNOWN(-1);

        private final int val;
        ResourceID(int i) {
            val = i;
        }

        public static ResourceID convertID(int resourceID) {
            for (ResourceID id: ResourceID.values()) {
                if (id.val == resourceID) {
                    return id;
                }
            }
            return UNKNOWN;
        }
    }

    private enum StateEnum {
        IDLE(0),
        DOWNLOADING(1),
        DOWNLOADED(2),
        UPDATING(3);

        public final int val;
        StateEnum(int i) {
            val = i;
        }
    }

    private enum UpdateResultEnum {
        Initial(0),
        FirmwareUpdated(1),
        NotEnoughMemory(2),
        ConnectionLost(4),
        IntegrityCheckError(5),
        UnsupportedPackageType(6),
        InvalidUrl(7),
        FirmwareUpdateFailed(8),
        UnsupportedProtocol(9);

        public final int val;
        UpdateResultEnum(int i) {
            val = i;
        }
    }



    public MyFirmware() {
        this(new ArrayList<Byte>(), "", 0, 0, 2);
    }

    public MyFirmware(ArrayList<Byte> _package, String packageURI, int state, int updateResult, int firmwareUpdateDeliveryMethod) {
        mPackage = _package;
        mPackageURI = packageURI;
        mState = state;
        mUpdateResult = updateResult;
        mFirmwareUpdateDeliveryMethod = firmwareUpdateDeliveryMethod;
    }


    @Override
    public ReadResponse read(ServerIdentity identity, int resourceid) {
        LOG.info("Read on Firmware resource /{}/{}/{}", getModel().id, getId(), resourceid);
        ResourceID id = ResourceID.convertID(resourceid);
        switch (id) {
            case PACKAGE:
                byte[] byteArray = new byte[mPackage.size()];
                for (int i = 0; i < mPackage.size(); i++) {
                    byteArray[i] = mPackage.get(i);
                }
                return ReadResponse.success(resourceid, byteArray);
            case PACKAGE_URI:
                return ReadResponse.success(resourceid, mPackageURI);
            case STATE:
                return ReadResponse.success(resourceid, mState);
            case UPDATE_RESULT:
                return ReadResponse.success(resourceid, mUpdateResult);
            case PACKAGE_NAME:
                return ReadResponse.success(resourceid, mPkgName);
            case PACKAGE_VERSION:
                return ReadResponse.success(resourceid, mPkgVersion);
            case FIRMWARE_UPDATE_DELIVERY_METHOD:
                return ReadResponse.success(resourceid, mFirmwareUpdateDeliveryMethod);
            default:
                return super.read(identity, resourceid);
        }
    }

    public void updateDownloadingState() {
        final MyFirmware self = this;
        new Timer().schedule(new TimerTask(){
            @Override
            public void run() {
                LOG.info("Read on Firmware State is changed to Downloading");
                self.mState = StateEnum.DOWNLOADING.val;
                fireResourcesChange(ResourceID.STATE.val);
                self.updateDownloadedState();
            }
        }, 1_000);
    }

    public void updateDownloadedState() {
        final MyFirmware self = this;
        new Timer().schedule(new TimerTask(){
            @Override
            public void run() {
                LOG.info("Read on Firmware State is changed to Downloaded");
                self.mState = StateEnum.DOWNLOADED.val;
                fireResourcesChange(ResourceID.STATE.val);
            }
        }, 30_000);
    }

    @Override
    public WriteResponse write(ServerIdentity identity, int resourceid, LwM2mResource value) {
        LOG.info("Write on Firmware resource /{}/{}/{}", getModel().id, getId(), resourceid);
        ResourceID id = ResourceID.convertID(resourceid);
        switch(id) {
            case PACKAGE_URI:
                mPackageURI = value.getValue().toString();
                updateDownloadingState();
                return WriteResponse.success();
        }
        return WriteResponse.notFound();
    }


    @Override
    public ExecuteResponse execute(ServerIdentity identity, int resourceid, String params)  {
        LOG.info("Execute on Firmware resource /{}/{}/{}", getModel().id, getId(), resourceid);
        ResourceID id = ResourceID.convertID(resourceid);
        switch (id) {
            case UPDATE:
                try {
                    Thread.sleep(10_000);
                    mUpdateResult = UpdateResultEnum.FirmwareUpdated.val;
                    mState = StateEnum.IDLE.val;
                    mPkgName = "Updated LeshanPkg";
                    mPkgVersion = "2.0.0";
                    fireResourcesChange(ResourceID.STATE.val, ResourceID.UPDATE_RESULT.val, ResourceID.PACKAGE_NAME.val, ResourceID.PACKAGE_VERSION.val);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return ExecuteResponse.success();
        }
        return ExecuteResponse.notFound();
    }

    @Override
    public List<Integer> getAvailableResourceIds(ObjectModel model) {
        return supportedResources;
    }
}

