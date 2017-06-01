package org.ekstep.genieservices.profile.db.model;

import org.ekstep.genieservices.commons.bean.Profile;
import org.ekstep.genieservices.commons.db.contract.ProfileEntry;
import org.ekstep.genieservices.commons.db.core.IReadable;
import org.ekstep.genieservices.commons.db.core.IResultSet;
import org.ekstep.genieservices.commons.db.operations.IDBSession;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by swayangjit on 24/5/17.
 */

public class UserProfilesModel implements IReadable {

    private IDBSession mDBSession;
    private List<Profile> mUserProfileList;

    private UserProfilesModel(IDBSession dbSession) {
        this.mDBSession = dbSession;
    }

    public static UserProfilesModel find(IDBSession dbSession) {
        UserProfilesModel userProfilesModel = new UserProfilesModel(dbSession);
        dbSession.read(userProfilesModel);
        return userProfilesModel.getProfileList() != null ? userProfilesModel : null;

    }

    @Override
    public IReadable read(IResultSet resultSet) {
        if (resultSet != null && resultSet.moveToFirst()) {
            mUserProfileList = new ArrayList<>();

            do {
                UserProfileModel userProfileModel = UserProfileModel.build(mDBSession, null);

                userProfileModel.readWithoutMoving(resultSet);

                mUserProfileList.add(userProfileModel.getProfile());
            } while (resultSet.moveToNext());
        }

        return this;
    }

    @Override
    public String getTableName() {
        return ProfileEntry.TABLE_NAME;
    }

    @Override
    public String orderBy() {
        return String.format(Locale.US, "order by %s asc", ProfileEntry.COLUMN_NAME_HANDLE);
    }

    @Override
    public String filterForRead() {
        return "";
    }


    @Override
    public String[] selectionArgsForFilter() {
        return null;
    }

    @Override
    public String limitBy() {
        return "";
    }

    public List<Profile> getProfileList() {
        return mUserProfileList;
    }

}