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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;


public class MySoftware extends BaseInstanceEnabler {

    private static final Logger LOG = LoggerFactory.getLogger(MySoftware.class);

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
        PACKAGE_NAME(0),
        PACKAGE_VERSION(1),
        PACKAGE(2),
        PACKAGE_URI(3),
        INSTALL(4),
        UPDATE_STATE(7),
        UPDATE_RESULT(9),
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
        DELIVERED(3);

        public final int val;
        StateEnum(int i) {
            val = i;
        }
    }

    private enum UpdateResultEnum {
        Initial(0),
        Downloaded(1),
        SoftwareSuccessfullyInstalled(2);
        public final int val;
        UpdateResultEnum(int i) {
            val = i;
        }
    }



    public MySoftware() {
        this(new ArrayList<Byte>(), "", 0, 0, 2);
    }

    public MySoftware(ArrayList<Byte> _package, String packageURI, int state, int updateResult, int firmwareUpdateDeliveryMethod) {
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
            case UPDATE_STATE:
                return ReadResponse.success(resourceid, mState);
            case UPDATE_RESULT:
                return ReadResponse.success(resourceid, mUpdateResult);
            case PACKAGE_NAME:
                return ReadResponse.success(resourceid, mPkgName);
            case PACKAGE_VERSION:
                return ReadResponse.success(resourceid, mPkgVersion);
            default:
                return super.read(identity, resourceid);
        }
    }

    public void updateDownloadingState() {
        final MySoftware self = this;
        new Timer().schedule(new TimerTask(){
            @Override
            public void run() {
                LOG.info("Read on Firmware State is changed to Downloading");
                self.mState = StateEnum.DOWNLOADING.val;
                fireResourcesChange(ResourceID.UPDATE_STATE.val);
                self.updateDownloadedState();
                self.downloadFirmwareImage();

            }
        }, 1_000);
    }

    private void downloadFirmwareImage() {
        // URLを作成してGET通信を行う
        try {
            URL url = new URL(this.mPackageURI);
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod("GET");
            http.connect();

// サーバーからのレスポンスを標準出力へ出す
            BufferedReader reader = new BufferedReader(new InputStreamReader(http.getInputStream()));
            String xml = "", line = "";
            while ((line = reader.readLine()) != null)
                xml += line;
            System.out.println(xml);
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateDownloadedState() {
        final MySoftware self = this;
        new Timer().schedule(new TimerTask(){
            @Override
            public void run() {
                LOG.info("Read on Firmware State is changed to Downloaded");
                self.mState = StateEnum.DELIVERED.val;
                self.mUpdateResult = UpdateResultEnum.Initial.val;
                fireResourcesChange(ResourceID.UPDATE_STATE.val, ResourceID.UPDATE_RESULT.val);

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
            case INSTALL:
                try {

//                    Thread.sleep(10_000);
                    final MySoftware self = this;
                    new Timer().schedule(new TimerTask(){
                        @Override
                        public void run() {
                            mUpdateResult = UpdateResultEnum.SoftwareSuccessfullyInstalled.val;
                            mState = StateEnum.IDLE.val;
                            mPkgName = "Updated LeshanPkg";
                            mPkgVersion = "2.0.0";
                            fireResourcesChange(ResourceID.UPDATE_STATE.val, ResourceID.UPDATE_RESULT.val, ResourceID.PACKAGE_NAME.val, ResourceID.PACKAGE_VERSION.val);
                        }
                    }, 10_000);
                } catch (Exception e) {
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

