package org.ekstep.genieservices;

import android.app.Activity;
import android.os.Bundle;

import org.ekstep.genieservices.commons.AndroidAppContext;
import org.ekstep.genieservices.commons.AppContext;
import org.ekstep.genieservices.commons.bean.GenieResponse;
import org.ekstep.genieservices.commons.bean.MasterData;
import org.ekstep.genieservices.commons.bean.PartnerData;
import org.ekstep.genieservices.commons.bean.Profile;
import org.ekstep.genieservices.commons.bean.enums.MasterDataType;
import org.ekstep.genieservices.commons.bean.telemetry.BaseTelemetry;

import java.util.Map;

/**
 * Created by Sneha on 4/26/2017.
 */

public class GenieServiceTestActivity extends Activity {

    private boolean idle = true;
    private GenieService mGenieService;
    private AppContext mAppContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_genie_services_test);

        mGenieService = GenieService.init(getApplicationContext(), getPackageName(), "", "");
        mAppContext = AndroidAppContext.buildAppContext(getApplicationContext(), getPackageName(), "", "org.ekstep");
        GenieServiceDBHelper.init(mAppContext);
    }

    public GenieResponse<MasterData> getMasterData(MasterDataType masterDataType) {
        idle = false;
        GenieResponse<MasterData> genieResponse = mGenieService.getConfigService().getMasterData(masterDataType);
        return genieResponse;
    }

    public GenieResponse<Map<String, Object>> getOrdinals() {
        idle = false;
        GenieResponse<Map<String, Object>> genieResponse = mGenieService.getConfigService().getOrdinals();
        return genieResponse;
    }

    public GenieResponse<Map<String, Object>> getResourceBundle(String languageIdentifier) {
        idle = false;
        GenieResponse<Map<String, Object>> genieResponse = mGenieService.getConfigService().getResourceBundle(languageIdentifier);
        return genieResponse;
    }

    public GenieResponse<Profile> createUserProfile(Profile userProfile) {
        idle = false;
        GenieResponse<Profile> response = mGenieService.getUserProfileService().createUserProfile(userProfile);
        return response;
    }

    public GenieResponse deleteUserProfile(String uid) {
        idle = false;
        GenieResponse response = mGenieService.getUserProfileService().deleteUser(uid);
        return response;
    }

    public GenieResponse<Profile> getAnonymousUser() {
        idle = false;
        GenieResponse<Profile> genieResponse = mGenieService.getUserProfileService().getAnonymousUser();
        return genieResponse;
    }

    public GenieResponse<String> setAnonymousUser() {
        idle = false;
        GenieResponse<String> genieResponse = mGenieService.getUserProfileService().setAnonymousUser();
        return genieResponse;
    }

    public GenieResponse<Profile> getCurrentUser() {
        idle = false;
        GenieResponse<Profile> genieResponse = mGenieService.getUserProfileService().getCurrentUser();
        return genieResponse;
    }

    public GenieResponse setCurrentUser(String uid) {
        idle = false;
        GenieResponse response = mGenieService.getUserProfileService().setCurrentUser(uid);
        return response;
    }

    public GenieResponse<Profile> updateUserProfile(Profile profile) {
        idle = false;
        GenieResponse<Profile> response = mGenieService.getUserProfileService().updateUserProfile(profile);
        return response;
    }

    public GenieResponse saveTelemetry(BaseTelemetry event) {
        idle = false;
        GenieResponse response = mGenieService.getTelemetryService().saveTelemetry(event);
        return response;
    }

    public GenieResponse saveTelemetry(String eventString) {
        idle = false;
        GenieResponse response = mGenieService.getTelemetryService().saveTelemetry(eventString);
        return response;
    }

    public GenieResponse registerPartner(PartnerData partnerData) {
        idle = false;
        GenieResponse response = mGenieService.getPartnerService().registerPartner(partnerData);
        return response;
    }

    public void setIdle() {
        idle = true;
    }

    public boolean isIdle() {
        return idle;
    }

}
