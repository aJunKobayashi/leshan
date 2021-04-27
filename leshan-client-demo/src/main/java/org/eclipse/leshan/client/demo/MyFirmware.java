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
        switch (resourceid) {
            case 0:
                byte[] byteArray = new byte[mPackage.size()];
                for (int i = 0; i < mPackage.size(); i++) {
                    byteArray[i] = mPackage.get(i);
                }
                return ReadResponse.success(resourceid, byteArray);
            case 1:
                return ReadResponse.success(resourceid, mPackageURI);
            case 3:
                return ReadResponse.success(resourceid, mState);
            case 5:
                return ReadResponse.success(resourceid, mUpdateResult);
            case 6:
                return ReadResponse.success(resourceid, mPkgName);
            case 7:
                return ReadResponse.success(resourceid, mPkgVersion);
            case 9:
                return ReadResponse.success(resourceid, mFirmwareUpdateDeliveryMethod);
            default:
                return super.read(identity, resourceid);
        }
    }

    class UpdateTask extends TimerTask
    {
        public void run()
        {

        }
    }

    public void updateDownloadingState() {
        final MyFirmware self = this;
        new Timer().schedule(new TimerTask(){
            @Override
            public void run() {
                LOG.info("Read on Firmware State is changed to Downloading");
                self.mState = 1;
                fireResourcesChange(3);
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
                self.mState = 2;
                fireResourcesChange(3);
            }
        }, 30_000);
    }

    @Override
    public WriteResponse write(ServerIdentity identity, int resourceid, LwM2mResource value) {
        LOG.info("Write on Firmware resource /{}/{}/{}", getModel().id, getId(), resourceid);
        switch(resourceid) {
            case 1:
                mPackageURI = value.getValue().toString();
                updateDownloadingState();
                return WriteResponse.success();
        }
        return WriteResponse.notFound();
    }


    @Override
    public ExecuteResponse execute(ServerIdentity identity, int resourceid, String params)  {
        LOG.info("Execute on Firmware resource /{}/{}/{}", getModel().id, getId(), resourceid);
        switch (resourceid) {
            case 2:
                try {
                    Thread.sleep(10_000);
                    mUpdateResult = 1;
                    mState = 0;
                    mPkgName = "Updated LeshanPkg";
                    mPkgVersion = "2.0.0";
                    fireResourcesChange(3, 5, 6, 7);
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

