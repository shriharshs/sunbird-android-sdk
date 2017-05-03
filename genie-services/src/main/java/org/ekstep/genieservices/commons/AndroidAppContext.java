package org.ekstep.genieservices.commons;

import android.content.Context;

import org.ekstep.genieservices.Constants;
import org.ekstep.genieservices.commons.db.ServiceDbHelper;
import org.ekstep.genieservices.commons.db.cache.IKeyValueStore;
import org.ekstep.genieservices.commons.db.cache.PreferenceWrapper;
import org.ekstep.genieservices.commons.db.operations.IDBSession;
import org.ekstep.genieservices.commons.network.AndroidHttpClient;
import org.ekstep.genieservices.commons.network.AndroidNetworkConnectivity;
import org.ekstep.genieservices.commons.network.IConnectionInfo;
import org.ekstep.genieservices.commons.network.IHttpClient;
import org.ekstep.genieservices.commons.network.auth.BasicAuthenticator;

/**
 * Created on 18/4/17.
 */
public class AndroidAppContext extends AppContext<Context, AndroidLogger> {

    private IDBSession mDBSession;
    private IDBSession mSummarizerDBSession;
    private IConnectionInfo mConnectionInfo;
    private IHttpClient mHttpClient;
    private IKeyValueStore mKeyValueOperation;
    private IDeviceInfo mDeviceInfo;
    private ILocationInfo mLocationInfo;

    private AndroidAppContext(Context context, String appPackage, String key, AndroidLogger logger, String gDataId) {
        super(context, appPackage, key, logger, gDataId);
    }

    public static AppContext buildAppContext(Context context, String appPackage, String key, AndroidLogger logger, String gDataId) {
        AndroidAppContext appContext = new AndroidAppContext(context, appPackage, key, logger, gDataId);
        appContext.setDBSession(ServiceDbHelper.getGSDBSession(appContext));
        appContext.setSummarizerDBSession(ServiceDbHelper.getSummarizerDBSession(appContext));
        appContext.setConnectionInfo(new AndroidNetworkConnectivity(appContext));
        appContext.setHttpClient(new AndroidHttpClient(new BasicAuthenticator()));
        appContext.setKeyValueStore(new PreferenceWrapper(appContext, Constants.SHARED_PREFERENCE_NAME));
        appContext.setDeviceInfo(new DeviceInfo(context));
        appContext.setLocationInfo(new LocationInfo(context));

        return appContext;
    }

    public ILocationInfo getLocationInfo() {
        return mLocationInfo;
    }

    public void setLocationInfo(ILocationInfo mLocationInfo) {
        this.mLocationInfo = mLocationInfo;
    }

    @Override
    public IDBSession getDBSession() {
        return mDBSession;
    }

    @Override
    public IDBSession getSummarizerDBSession() {
        return mSummarizerDBSession;
    }

    @Override
    public IKeyValueStore getKeyValueStore() {
        return mKeyValueOperation;
    }

    private void setKeyValueStore(IKeyValueStore keyValueOperation) {
        this.mKeyValueOperation = keyValueOperation;
    }

    @Override
    public IConnectionInfo getConnectionInfo() {
        return mConnectionInfo;
    }

    private void setConnectionInfo(IConnectionInfo connectionInfo) {
        this.mConnectionInfo = connectionInfo;
    }

    @Override
    public IHttpClient getHttpClient() {
        return mHttpClient;
    }

    private void setHttpClient(IHttpClient client) {
        this.mHttpClient = client;
    }

    @Override
    public IDeviceInfo getDeviceInfo() {
        return mDeviceInfo;
    }

    private void setDeviceInfo(IDeviceInfo deviceInfo) {
        this.mDeviceInfo = deviceInfo;
    }

    @Override
    public Void setDBSession(IDBSession dbSession) {
        this.mDBSession = dbSession;
        return null;
    }

    @Override
    public Void setSummarizerDBSession(IDBSession dbSession) {
        this.mSummarizerDBSession = dbSession;
        return null;
    }

}