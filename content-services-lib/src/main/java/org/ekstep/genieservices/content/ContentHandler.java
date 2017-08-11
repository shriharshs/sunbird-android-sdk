package org.ekstep.genieservices.content;

import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;

import org.ekstep.genieservices.IConfigService;
import org.ekstep.genieservices.IContentFeedbackService;
import org.ekstep.genieservices.IUserService;
import org.ekstep.genieservices.ServiceConstants;
import org.ekstep.genieservices.commons.AppContext;
import org.ekstep.genieservices.commons.bean.Content;
import org.ekstep.genieservices.commons.bean.ContentAccess;
import org.ekstep.genieservices.commons.bean.ContentAccessFilterCriteria;
import org.ekstep.genieservices.commons.bean.ContentData;
import org.ekstep.genieservices.commons.bean.ContentFeedback;
import org.ekstep.genieservices.commons.bean.ContentFeedbackFilterCriteria;
import org.ekstep.genieservices.commons.bean.ContentFilterCriteria;
import org.ekstep.genieservices.commons.bean.ContentImportRequest;
import org.ekstep.genieservices.commons.bean.ContentListing;
import org.ekstep.genieservices.commons.bean.ContentListingCriteria;
import org.ekstep.genieservices.commons.bean.ContentListingSection;
import org.ekstep.genieservices.commons.bean.ContentSearchCriteria;
import org.ekstep.genieservices.commons.bean.ContentSearchFilter;
import org.ekstep.genieservices.commons.bean.ContentSortCriteria;
import org.ekstep.genieservices.commons.bean.ContentVariant;
import org.ekstep.genieservices.commons.bean.FilterValue;
import org.ekstep.genieservices.commons.bean.GenieResponse;
import org.ekstep.genieservices.commons.bean.MasterData;
import org.ekstep.genieservices.commons.bean.MasterDataValues;
import org.ekstep.genieservices.commons.bean.RecommendedContentRequest;
import org.ekstep.genieservices.commons.bean.RelatedContentRequest;
import org.ekstep.genieservices.commons.bean.UserSession;
import org.ekstep.genieservices.commons.bean.enums.MasterDataType;
import org.ekstep.genieservices.commons.bean.enums.SearchType;
import org.ekstep.genieservices.commons.bean.enums.SortOrder;
import org.ekstep.genieservices.commons.db.contract.ContentAccessEntry;
import org.ekstep.genieservices.commons.db.contract.ContentEntry;
import org.ekstep.genieservices.commons.db.operations.IDBSession;
import org.ekstep.genieservices.commons.utils.CollectionUtil;
import org.ekstep.genieservices.commons.utils.DateUtil;
import org.ekstep.genieservices.commons.utils.FileUtil;
import org.ekstep.genieservices.commons.utils.GsonUtil;
import org.ekstep.genieservices.commons.utils.Logger;
import org.ekstep.genieservices.commons.utils.StringUtil;
import org.ekstep.genieservices.content.db.model.ContentListingModel;
import org.ekstep.genieservices.content.db.model.ContentModel;
import org.ekstep.genieservices.content.db.model.ContentsModel;
import org.ekstep.genieservices.content.network.ContentDetailsAPI;
import org.ekstep.genieservices.content.network.ContentListingAPI;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;

/**
 * Created on 5/23/2017.
 *
 * @author anil
 */
public class ContentHandler {

    public static final String KEY_VISIBILITY = "visibility";
    private static final String TAG = ContentHandler.class.getSimpleName();

    private static final String KEY_IDENTIFIER = "identifier";
    private static final String KEY_PKG_VERSION = "pkgVersion";
    private static final String KEY_ARTIFACT_URL = "artifactUrl";
    private static final String KEY_APP_ICON = "appIcon";
    private static final String KEY_POSTER_IMAGE = "posterImage";
    private static final String KEY_GRAY_SCALE_APP_ICON = "grayScaleAppIcon";
    private static final String KEY_STATUS = "status";
    private static final String KEY_EXPIRES = "expires";
    private static final String KEY_OBJECT_TYPE = "objectType";
    private static final String KEY_VARIANTS = "variants";
    private static final String KEY_DOWNLOAD_URL = "downloadUrl";
    private static final String KEY_COMPATIBILITY_LEVEL = "compatibilityLevel";
    private static final String KEY_MIME_TYPE = "mimeType";
    private static final String KEY_ARTIFACT_MIME_TYPE = "artifactMimeType";
    private static final Object AUDIENCE_KEY = "audience";
    private static final String KEY_LAST_UPDATED_ON = "lastUpdatedOn";
    private static final String KEY_PRE_REQUISITES = "pre_requisites";
    private static final String KEY_CHILDREN = "children";
    private static final String KEY_CONTENT_TYPE = "contentType";
    private static final String KEY_NAME = "name";

    private static final String KEY_CONTENT_METADATA = "contentMetadata";
    private static final String KEY_VIRALITY_METADATA = "virality";
    private static final String KEY_TRANSFER_COUNT = "transferCount";
    private static final String KEY_ORIGIN = "origin";

    private static final int DEFAULT_PACKAGE_VERSION = -1;
    private static final int INITIAL_VALUE_FOR_TRANSFER_COUNT = 0;
    private static final int MAX_CONTENT_NAME = 30;

    // TODO: 02-03-2017 : We can remove this later after few release
    public static int defaultCompatibilityLevel = 1;

    public static ContentModel convertContentMapToModel(IDBSession dbSession, Map contentData, String manifestVersion) {
        String identifier = readIdentifier(contentData);

        String localData = null;
        String serverData = null;
        String serverLastUpdatedOn = null;
        if (StringUtil.isNullOrEmpty(manifestVersion)) {
            serverData = GsonUtil.toJson(contentData);
            serverLastUpdatedOn = serverLastUpdatedOn(contentData);
        } else {
            localData = GsonUtil.toJson(contentData);
        }

        String mimeType = readMimeType(contentData);
        String contentType = readContentType(contentData);
        String visibility = readVisibility(contentData);
        String audience = readAudience(contentData);

        ContentModel contentModel = ContentModel.build(dbSession, identifier, serverData, serverLastUpdatedOn,
                manifestVersion, localData, mimeType, contentType, visibility, audience);

        return contentModel;
    }

    public static String readIdentifier(Map contentData) {
        if (contentData.containsKey(KEY_IDENTIFIER)) {
            return (String) contentData.get(KEY_IDENTIFIER);
        }
        return null;
    }

    public static String readMimeType(Map contentData) {
        if (contentData.containsKey(KEY_MIME_TYPE)) {
            return (String) contentData.get(KEY_MIME_TYPE);
        }
        return null;
    }

    public static String readArtifactMimeType(Map contentData) {
        if (contentData.containsKey(KEY_ARTIFACT_MIME_TYPE)) {
            return (String) contentData.get(KEY_ARTIFACT_MIME_TYPE);
        }
        return null;
    }

    public static String readContentType(Map contentData) {
        if (contentData.containsKey(KEY_CONTENT_TYPE)) {
            String contentType = (String) contentData.get(KEY_CONTENT_TYPE);
            if (!StringUtil.isNullOrEmpty(contentType)) {
                contentType = contentType.toLowerCase();
            }
            return contentType;
        }
        return null;
    }

    private static String readName(Map contentData) {
        if (contentData.containsKey(KEY_NAME)) {
            return (String) contentData.get(KEY_NAME);
        }
        return null;
    }

    public static String readVisibility(Map contentData) {
        if (contentData.containsKey(KEY_VISIBILITY)) {
            return (String) contentData.get(KEY_VISIBILITY);
        }
        return ContentConstants.Visibility.DEFAULT;
    }

    public static String readAudience(Map contentData) {
        ArrayList<String> audienceList = null;
        if (contentData.containsKey(AUDIENCE_KEY)) {
            Object o = contentData.get(AUDIENCE_KEY);
            if (o instanceof String) {
                audienceList = new ArrayList<>();
                audienceList.add((String) o);
            } else {
                audienceList = (ArrayList<String>) contentData.get(AUDIENCE_KEY);
            }
        }
        if (audienceList == null) {
            audienceList = new ArrayList<>();
        }
        if (audienceList.isEmpty()) {
            audienceList.add("Learner");
        }
        Collections.sort(audienceList);
        return StringUtil.join(",", audienceList);
    }

    public static String readArtifactUrl(Map contentData) {
        if (contentData.containsKey(KEY_ARTIFACT_URL)) {
            return (String) contentData.get(KEY_ARTIFACT_URL);
        }
        return null;
    }

    public static String readAppIcon(Map contentData) {
        if (contentData.containsKey(KEY_APP_ICON)) {
            return (String) contentData.get(KEY_APP_ICON);
        }
        return null;
    }

    public static String readPosterImage(Map contentData) {
        if (contentData.containsKey(KEY_POSTER_IMAGE)) {
            return (String) contentData.get(KEY_POSTER_IMAGE);
        }
        return null;
    }

    public static String readGrayScaleAppIcon(Map contentData) {
        if (contentData.containsKey(KEY_GRAY_SCALE_APP_ICON)) {
            return (String) contentData.get(KEY_GRAY_SCALE_APP_ICON);
        }
        return null;
    }

    public static Double readCompatibilityLevel(Map contentData) {
        return (contentData.get(KEY_COMPATIBILITY_LEVEL) != null) ? (Double) contentData.get(KEY_COMPATIBILITY_LEVEL) : defaultCompatibilityLevel;
    }

    public static String readStatus(Map contentData) {
        if (contentData.containsKey(KEY_STATUS)) {
            return (String) contentData.get(KEY_STATUS);
        }
        return null;
    }

    public static boolean isDraftContent(String status) {
        return !StringUtil.isNullOrEmpty(status) && status.equalsIgnoreCase(ContentConstants.ContentStatus.DRAFT);
    }

    public static String readExpiryDate(Map contentData) {
        if (contentData.containsKey(KEY_EXPIRES)) {
            return (String) contentData.get(KEY_EXPIRES);
        }
        return null;
    }

    public static boolean isExpired(String expiryDate) {
        if (!StringUtil.isNullOrEmpty(expiryDate)) {
            long millis = DateUtil.getTime(expiryDate);
            if (System.currentTimeMillis() > millis) {
                return true;
            }
        }
        return false;
    }

    public static String readObjectType(Map contentData) {
        if (contentData.containsKey(KEY_OBJECT_TYPE)) {
            return (String) contentData.get(KEY_OBJECT_TYPE);
        }
        return null;
    }

    private static String serverLastUpdatedOn(Map serverData) {
        return (String) serverData.get(KEY_LAST_UPDATED_ON);
    }

    private static Double readPkgVersion(String localData) {
        return readPkgVersion(GsonUtil.fromJson(localData, Map.class));
    }

    public static Double readPkgVersion(Map contentData) {
        return (Double) contentData.get(KEY_PKG_VERSION);
    }

    public static boolean hasPreRequisites(String localData) {
        return GsonUtil.fromJson(localData, Map.class).get(KEY_PRE_REQUISITES) != null;
    }

    public static boolean hasPreRequisites(Map contentData) {
        return contentData.get(KEY_PRE_REQUISITES) != null;
    }

    private static List<Map<String, Object>> readPreRequisites(Map contentData) {
        if (contentData.containsKey(KEY_PRE_REQUISITES)) {
            return (List<Map<String, Object>>) contentData.get(KEY_PRE_REQUISITES);
        }
        return null;
    }

    private static List<String> getPreRequisitesIdentifiers(String localData) {
        return getPreRequisitesIdentifiers(GsonUtil.fromJson(localData, Map.class));
    }

    public static List<String> getPreRequisitesIdentifiers(Map contentData) {
        List<String> childIdentifiers = new ArrayList<>();

        List<Map<String, Object>> preRequisites = readPreRequisites(contentData);
        if (preRequisites != null) {
            for (Map child : preRequisites) {
                String childIdentifier = readIdentifier(child);
                childIdentifiers.add(childIdentifier);
            }
        }

        // Return the pre_requisites in DB
        return childIdentifiers;
    }

    public static boolean hasChildren(String localData) {
        return GsonUtil.fromJson(localData, Map.class).get(KEY_CHILDREN) != null;
    }

    public static boolean hasChildren(Map contentData) {
        return contentData.get(KEY_CHILDREN) != null;
    }

    private static List<Map<String, Object>> readChildren(Map contentData) {
        if (contentData.containsKey(KEY_CHILDREN)) {
            return (List<Map<String, Object>>) contentData.get(KEY_CHILDREN);
        }
        return null;
    }

    private static List<String> getChildContentsIdentifiers(String localData) {
        return getChildContentsIdentifiers(GsonUtil.fromJson(localData, Map.class));
    }

    public static List<String> getChildContentsIdentifiers(Map contentData) {
        List<String> childIdentifiers = new ArrayList<>();

        List<Map<String, Object>> children = readChildren(contentData);
        if (children != null) {
            for (Map child : children) {
                String childIdentifier = readIdentifier(child);
                childIdentifiers.add(childIdentifier);
            }
        }

        // Return the childrenInDB
        return childIdentifiers;
    }

    public static Map fetchContentDetailsFromServer(AppContext appContext, String contentIdentifier) {
        ContentDetailsAPI api = new ContentDetailsAPI(appContext, contentIdentifier);
        GenieResponse apiResponse = api.get();

        if (apiResponse.getStatus()) {
            String body = apiResponse.getResult().toString();

            Map map = GsonUtil.fromJson(body, Map.class);
            Map result = (Map) map.get("result");

            return (Map) result.get("content");
        }

        return null;
    }

    public static void refreshContentDetailsFromServer(final AppContext appContext, final String contentIdentifier, final ContentModel existingContentModel) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Map contentData = fetchContentDetailsFromServer(appContext, contentIdentifier);

                if (contentData != null) {
                    existingContentModel.setServerData(GsonUtil.toJson(contentData));
                    existingContentModel.setServerLastUpdatedOn(serverLastUpdatedOn(contentData));
                    existingContentModel.setAudience(readAudience(contentData));
                    existingContentModel.update();
                }
            }
        }).start();
    }

    public static Content convertContentModelToBean(ContentModel contentModel) {
        Content content = new Content();
        content.setIdentifier(contentModel.getIdentifier());
        ContentData localData = null, serverData = null;
        String artifactMimeType = null;
        if (contentModel.getLocalData() != null) {
            localData = GsonUtil.fromJson(contentModel.getLocalData(), ContentData.class);
            localData.setVariants(getContentVariants(contentModel.getLocalData()));
            content.setContentData(localData);

            artifactMimeType = localData.getArtifactMimeType();
        } else if (contentModel.getServerData() != null) {
            serverData = GsonUtil.fromJson(contentModel.getServerData(), ContentData.class);
            serverData.setVariants(getContentVariants(contentModel.getServerData()));
            content.setContentData(serverData);
        }

        content.setUpdateAvailable(isUpdateAvailable(serverData, localData));
        content.setMimeType(contentModel.getMimeType());
        content.setBasePath(contentModel.getPath());
        content.setContentType(contentModel.getContentType());
        boolean isAvailableLocally = isAvailableLocally(contentModel.getContentState(), artifactMimeType);
        content.setAvailableLocally(isAvailableLocally);
        content.setReferenceCount(contentModel.getRefCount());

        long contentCreationTime = 0;
        String localLastUpdatedTime = contentModel.getLocalLastUpdatedTime();
        if (!StringUtil.isNullOrEmpty(localLastUpdatedTime)) {
            contentCreationTime = DateUtil.getTime(localLastUpdatedTime);
        }
        content.setLastUpdatedTime(contentCreationTime);

        return content;
    }

    private static List<ContentVariant> getContentVariants(String dataJson) {
        List<ContentVariant> contentVariantList = new ArrayList<>();

        Map<String, Object> dataMap = GsonUtil.fromJson(dataJson, Map.class);
        ContentVariant spineContentVariant = getVariant(dataMap, "spine");
        if (spineContentVariant != null) contentVariantList.add(spineContentVariant);
        return contentVariantList;
    }

    private static ContentVariant getVariant(Map contentDataMap, String variantName) {
        Object variants = contentDataMap.get(KEY_VARIANTS);
        ContentVariant contentVariant = null;
        if (variants != null) {
            Map variantData;
            if (variants instanceof Map) {
                variantData = (Map) variants;
            } else {
                variantData = GsonUtil.fromJson(((String) variants).replace("\\", ""), Map.class);
            }

            if (variantData.get(variantName) != null) {
                Map spineData = (Map) variantData.get(variantName);
                contentVariant = new ContentVariant(variantName, spineData.get("ecarUrl").toString(), spineData.get("size").toString());
            }
        }
        return contentVariant;
    }

    public static String getCurrentUserId(IUserService userService) {
        if (userService != null) {
            UserSession userSession = userService.getCurrentUserSession().getResult();
            if (userSession != null) {
                return userSession.getUid();
            }
        }
        return null;
    }

    public static List<ContentFeedback> getContentFeedback(IContentFeedbackService contentFeedbackService, String contentIdentifier, String uid) {
        if (contentFeedbackService != null) {
            ContentFeedbackFilterCriteria.Builder builder = new ContentFeedbackFilterCriteria.Builder().byUser(uid).forContent(contentIdentifier);
            return contentFeedbackService.getFeedback(builder.build()).getResult();
        }
        return null;
    }

    public static List<ContentAccess> getContentAccess(IUserService userService, String contentIdentifier, String uid) {
        if (userService != null) {
            ContentAccessFilterCriteria.Builder builder = new ContentAccessFilterCriteria.Builder().byUser(uid).forContent(contentIdentifier);
            return userService.getAllContentAccess(builder.build()).getResult();
        }
        return null;
    }

    private static boolean isUpdateAvailable(ContentData serverData, ContentData localData) {
        float lVersion = DEFAULT_PACKAGE_VERSION;
        float sVersion = DEFAULT_PACKAGE_VERSION;

        if (serverData != null && !StringUtil.isNullOrEmpty(serverData.getPkgVersion())) {
            sVersion = Float.valueOf(serverData.getPkgVersion());
        }

        if (localData != null && !StringUtil.isNullOrEmpty(localData.getPkgVersion())) {
            lVersion = Float.valueOf(localData.getPkgVersion());
        }

        return sVersion > 0 && lVersion > 0 && sVersion > lVersion;
    }

    public static boolean isAvailableLocally(int contentState) {
        return contentState == ContentConstants.State.ARTIFACT_AVAILABLE;
    }

    private static boolean isAvailableLocally(int contentState, String artifactMimeType) {
        return (contentState == ContentConstants.State.ARTIFACT_AVAILABLE)
                || (contentState == ContentConstants.State.ONLY_SPINE && ContentConstants.ArtifactMimeType.CONTENT_WITHOUT_ARTIFACT.equals(artifactMimeType));
    }

    private static String[] getDefaultContentTypes() {
        return new String[]{"Story", "Worksheet", "Collection", "Game", "TextBook"};
    }

    public static List<ContentModel> getAllLocalContent(IDBSession dbSession, ContentFilterCriteria criteria) {
        String uid = null;
        String[] contentTypes;
        if (criteria != null) {
            uid = criteria.getUid();
            contentTypes = criteria.getContentTypes();
        } else {
            contentTypes = getDefaultContentTypes();
        }
        String contentTypesStr = StringUtil.join("','", contentTypes);

        StringBuilder audienceFilterBuilder = new StringBuilder();
        if (criteria != null && criteria.getAudience() != null && criteria.getAudience().length > 0) {
            for (String audience : criteria.getAudience()) {
                audienceFilterBuilder.append(audienceFilterBuilder.length() > 0 ? " or " : "");
                audienceFilterBuilder.append(String.format(Locale.US, "c.%s like '%%%s%%'", ContentEntry.COLUMN_NAME_AUDIENCE, audience));
            }
        }

        String contentTypeFilter = String.format(Locale.US, "c.%s in ('%s')", ContentEntry.COLUMN_NAME_CONTENT_TYPE, contentTypesStr.toLowerCase());
        String contentVisibilityFilter = String.format(Locale.US, "c.%s = '%s'", ContentEntry.COLUMN_NAME_VISIBILITY, ContentConstants.Visibility.DEFAULT);
        String artifactAvailabilityFilter = String.format(Locale.US, "c.%s = '%s'", ContentEntry.COLUMN_NAME_CONTENT_STATE, ContentConstants.State.ARTIFACT_AVAILABLE);
        String filter = audienceFilterBuilder.length() == 0
                ? String.format(Locale.US, "WHERE (%s AND %s AND %s)", contentVisibilityFilter, artifactAvailabilityFilter, contentTypeFilter)
                : String.format(Locale.US, "WHERE (%s AND %s AND %s AND (%s))", contentVisibilityFilter, artifactAvailabilityFilter, contentTypeFilter, audienceFilterBuilder.toString());

        String orderBy = String.format(Locale.US, "ORDER BY ca.%s desc, c.%s desc, c.%s desc", ContentAccessEntry.COLUMN_NAME_EPOCH_TIMESTAMP, ContentEntry.COLUMN_NAME_LOCAL_LAST_UPDATED_ON, ContentEntry.COLUMN_NAME_SERVER_LAST_UPDATED_ON);

        String query;
        if (uid != null) {
            query = String.format(Locale.US, "SELECT c.* FROM  %s c LEFT JOIN %s ca ON c.%s = ca.%s AND ca.%s = '%s' %s %s;",
                    ContentEntry.TABLE_NAME, ContentAccessEntry.TABLE_NAME, ContentEntry.COLUMN_NAME_IDENTIFIER, ContentAccessEntry.COLUMN_NAME_CONTENT_IDENTIFIER, ContentAccessEntry.COLUMN_NAME_UID, uid,
                    filter, orderBy);
        } else {
            query = String.format(Locale.US, "SELECT c.* FROM  %s c %s;",
                    ContentEntry.TABLE_NAME, filter);
        }

        List<ContentModel> contentModelListInDB;
        ContentsModel contentsModel = ContentsModel.findWithCustomQuery(dbSession, query);
        if (contentsModel != null) {
            contentModelListInDB = contentsModel.getContentModelList();
        } else {
            contentModelListInDB = new ArrayList<>();
        }

        return contentModelListInDB;
    }

    public static List<ContentModel> getAllLocalContentModel(IDBSession dbSession) {
        String contentVisibilityFilter = String.format(Locale.US, "%s = '%s'", ContentEntry.COLUMN_NAME_VISIBILITY, ContentConstants.Visibility.DEFAULT);
        // For hiding the non compatible imported content, which visibility is DEFAULT.
        String artifactAvailabilityFilter = String.format(Locale.US, "%s = '%s'", ContentEntry.COLUMN_NAME_CONTENT_STATE, ContentConstants.State.ARTIFACT_AVAILABLE);

        String filter = String.format(Locale.US, " where (%s AND %s)", contentVisibilityFilter, artifactAvailabilityFilter);

        List<ContentModel> contentModelListInDB;
        ContentsModel contentsModel = ContentsModel.find(dbSession, filter);
        if (contentsModel != null) {
            contentModelListInDB = contentsModel.getContentModelList();
        } else {
            contentModelListInDB = new ArrayList<>();
        }

        return contentModelListInDB;
    }

    public static void deleteOrUpdateContent(ContentModel contentModel, boolean isChildItems, boolean isChildContent) {
        int refCount = contentModel.getRefCount();
        int contentState;
        String visibility = contentModel.getVisibility();

        if (isChildContent) {
            // If visibility is Default it means this content was visible in my downloads.
            // After deleting artifact for this content it should not visible as well so reduce the refCount also for this.
            if (refCount > 1 && ContentConstants.Visibility.DEFAULT.equalsIgnoreCase(contentModel.getVisibility())) {
                refCount = refCount - 1;

                // Update visibility
                visibility = ContentConstants.Visibility.PARENT;
            }

            // Update the contentState
            // Do not update the content state if mimeType is "application/vnd.ekstep.content-collection"
            if (ContentConstants.MimeType.COLLECTION.equals(contentModel.getMimeType())) {
                contentState = ContentConstants.State.ARTIFACT_AVAILABLE;
            } else {
                contentState = ContentConstants.State.ONLY_SPINE;
            }

        } else {
            // TODO: This check should be before updating the existing refCount.
            // Do not update the content state if mimeType is "application/vnd.ekstep.content-collection" and refCount is more than 1.
            if (ContentConstants.MimeType.COLLECTION.equals(contentModel.getMimeType()) && refCount > 1) {
                contentState = ContentConstants.State.ARTIFACT_AVAILABLE;
            } else if (refCount > 1 && isChildItems) {  //contentModel.isVisibilityDefault() &&
                // Visibility will remain Default only.

                contentState = ContentConstants.State.ARTIFACT_AVAILABLE;
            } else {

                // Set the visibility to Parent so that this content will not visible in My contents / Downloads section.
                // Update visibility
                if (ContentConstants.Visibility.DEFAULT.equalsIgnoreCase(contentModel.getVisibility())) {
                    visibility = ContentConstants.Visibility.PARENT;
                }

                contentState = ContentConstants.State.ONLY_SPINE;
            }

            refCount = refCount - 1;
        }

        // if there are no entry in DB for any content then on this case contentModel.getPath() will be null
        if (contentModel.getPath() != null) {
            if (contentState == ContentConstants.State.ONLY_SPINE) {
                FileUtil.rm(new File(contentModel.getPath()), contentModel.getIdentifier());
            }

            contentModel.setVisibility(visibility);
            // Update the refCount
            contentModel.addOrUpdateRefCount(refCount);
            contentModel.addOrUpdateContentState(contentState);

            contentModel.update();
        }
    }

    public static void deleteAllPreRequisites(AppContext appContext, ContentModel contentModel, boolean isChildContent) {
        List<String> preRequisitesIdentifier = getPreRequisitesIdentifiers(contentModel.getLocalData());
        List<ContentModel> contentModelListInDB = findAllContentsWithIdentifiers(appContext.getDBSession(), preRequisitesIdentifier);

        if (contentModelListInDB != null) {
            for (ContentModel c : contentModelListInDB) {
                deleteOrUpdateContent(c, true, isChildContent);
            }
        }
    }

    public static void deleteAllChild(AppContext appContext, ContentModel contentModel, boolean isChildContent) {
        Queue<ContentModel> queue = new LinkedList<>();

        queue.add(contentModel);

        ContentModel node;
        while (!queue.isEmpty()) {
            node = queue.remove();

            if (hasChildren(node.getLocalData())) {
                List<String> childContentsIdentifiers = getChildContentsIdentifiers(node.getLocalData());
                List<ContentModel> contentModelListInDB = findAllContentsWithIdentifiers(appContext.getDBSession(), childContentsIdentifiers);
                if (contentModelListInDB != null) {
                    queue.addAll(contentModelListInDB);
                }
            }

            // Deleting only child content
            if (!contentModel.getIdentifier().equalsIgnoreCase(node.getIdentifier())) {
                deleteOrUpdateContent(node, true, isChildContent);
            }
        }
    }

    private static List<ContentModel> findAllContentsWithIdentifiers(IDBSession dbSession, List<String> identifiers) {
        String filter = String.format(Locale.US, " where %s in ('%s') ", ContentEntry.COLUMN_NAME_IDENTIFIER, StringUtil.join("','", identifiers));

        List<ContentModel> contentModelListInDB = null;
        ContentsModel contentsModel = ContentsModel.find(dbSession, filter);
        if (contentsModel != null) {
            contentModelListInDB = contentsModel.getContentModelList();
        }

        return contentModelListInDB;
    }

    /**
     * Is compatible to current version of Genie.
     *
     * @return true if compatible else false.
     */
    public static boolean isCompatible(AppContext appContext, Double compatibilityLevel) {
        return (compatibilityLevel >= appContext.getParams().getInt(ServiceConstants.Params.MIN_COMPATIBILITY_LEVEL))
                && (compatibilityLevel <= appContext.getParams().getInt(ServiceConstants.Params.MAX_COMPATIBILITY_LEVEL));
    }

    public static boolean isImportFileExist(ContentModel oldContentModel, Map newContentData) {
        if (oldContentModel == null || newContentData == null || newContentData.isEmpty()) {
            return false;
        }

        boolean isExist = false;
        try {
            String oldIdentifier = oldContentModel.getIdentifier();
            String newIdentifier = readIdentifier(newContentData);
            String oldVisibility = oldContentModel.getVisibility();
            String newVisibility = readVisibility(newContentData);

            if (oldIdentifier.equalsIgnoreCase(newIdentifier) && oldVisibility.equalsIgnoreCase(newVisibility)) {
                isExist = readPkgVersion(oldContentModel.getLocalData()) >= readPkgVersion(newContentData);
            }
        } catch (Exception e) {
            Logger.e(TAG, "isImportFileExist", e);
        }

        return isExist;
    }

    /**
     * To Check whether the content is exist or not.
     *
     * @param oldContent    Old ContentModel
     * @param newIdentifier New content identifier
     * @return True - if file exists, False- does not exists
     */
    public static boolean isContentExist(ContentModel oldContent, String newIdentifier, Double newPkgVersion) {
        if (oldContent == null) {
            return false;
        }

        boolean isExist = false;
        try {
            String oldIdentifier = oldContent.getIdentifier();
            if (oldIdentifier.equalsIgnoreCase(newIdentifier)) {
                if ((readPkgVersion(oldContent.getLocalData()) < newPkgVersion) // If old content's pkgVersion is less than the new content then return false.
                        || oldContent.getContentState() != ContentConstants.State.ARTIFACT_AVAILABLE) {  //  If content_state is other than artifact available then also return  false.
                    isExist = false;
                } else {
                    isExist = true;
                }
            }
        } catch (Exception e) {
            Logger.e(TAG, "isContentExist", e);
        }

        return isExist;
    }

    public static String readOriginFromContentMap(Map contentData) {
        try {
            Map contentMetadataMap = (Map) contentData.get(KEY_CONTENT_METADATA);
            if (contentMetadataMap != null) {
                Map viralityMetadataMap = (Map) contentMetadataMap.get(KEY_VIRALITY_METADATA);
                return (String) viralityMetadataMap.get(KEY_ORIGIN);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static int readTransferCountFromContentMap(Map contentData) {
        try {
            Map contentMetadataMap = (Map) contentData.get(KEY_CONTENT_METADATA);
            if (contentMetadataMap != null) {
                Map viralityMetadataMap = (Map) contentMetadataMap.get(KEY_VIRALITY_METADATA);
                String count = String.valueOf(viralityMetadataMap.get(KEY_TRANSFER_COUNT));
                return Double.valueOf(count).intValue();
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static void addOrUpdateViralityMetadata(Map<String, Object> localDataMap, String origin) {
        if (isContentMetadataAbsent(localDataMap)) {
            Map<String, Object> viralityMetadata = new HashMap<>();
            viralityMetadata.put(KEY_ORIGIN, origin);
            viralityMetadata.put(KEY_TRANSFER_COUNT, INITIAL_VALUE_FOR_TRANSFER_COUNT);

            Map<String, Object> contentMetadata = new HashMap<>();
            contentMetadata.put(KEY_VIRALITY_METADATA, viralityMetadata);

            localDataMap.put(KEY_CONTENT_METADATA, contentMetadata);
        } else if (isContentMetadataPresentWithoutViralityMetadata(localDataMap)) {
            Map<String, Object> viralityMetadata = new HashMap<>();
            viralityMetadata.put(KEY_ORIGIN, origin);
            viralityMetadata.put(KEY_TRANSFER_COUNT, INITIAL_VALUE_FOR_TRANSFER_COUNT);

            ((Map<String, Object>) localDataMap.get(KEY_CONTENT_METADATA)).put(KEY_VIRALITY_METADATA, viralityMetadata);
        } else {
            Map<String, Object> viralityMetadata = (Map<String, Object>) ((Map<String, Object>) localDataMap.get(KEY_CONTENT_METADATA)).get(KEY_VIRALITY_METADATA);
            viralityMetadata.put(KEY_TRANSFER_COUNT, transferCount(viralityMetadata) + 1);
        }
    }

    public static void addViralityMetadataIfMissing(Map<String, Object> localDataMap, String origin) {
        if (localDataMap.get(KEY_CONTENT_METADATA) == null) {
            localDataMap.put(KEY_CONTENT_METADATA, new HashMap<String, Object>());
        }

        Map<String, Object> contentMetadata = (Map<String, Object>) localDataMap.get(KEY_CONTENT_METADATA);
        if (contentMetadata.get(KEY_VIRALITY_METADATA) == null) {
            contentMetadata.put(KEY_VIRALITY_METADATA, new HashMap<String, Object>());
        }

        Map<String, Object> viralityMetadata = (Map<String, Object>) contentMetadata.get(KEY_VIRALITY_METADATA);
        if (viralityMetadata.get(KEY_ORIGIN) == null) {
            viralityMetadata.put(KEY_ORIGIN, origin);
        }
        if (viralityMetadata.get(KEY_TRANSFER_COUNT) == null) {
            viralityMetadata.put(KEY_TRANSFER_COUNT, INITIAL_VALUE_FOR_TRANSFER_COUNT);
        }
    }

    private static boolean isContentMetadataAbsent(Map<String, Object> localDataMap) {
        return localDataMap.get(KEY_CONTENT_METADATA) == null;
    }

    private static boolean isContentMetadataPresentWithoutViralityMetadata(Map<String, Object> localDataMap) {
        return ((Map<String, Object>) localDataMap.get(KEY_CONTENT_METADATA)).get(KEY_VIRALITY_METADATA) == null;
    }

    private static int transferCount(Map<String, Object> viralityMetadata) {
        try {
            Double transferCount = (Double) viralityMetadata.get(KEY_TRANSFER_COUNT);
            return transferCount.intValue();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static List<ContentModel> getSortedChildrenList(IDBSession dbSession, String localData, int childContents) {
        Map<String, Object> localDataMap = GsonUtil.fromJson(localData, Map.class);
        List<Map> children = (List<Map>) localDataMap.get("children");
        List<ContentModel> contentModelListInDB = null;
        if (children != null && children.size() > 0) {
            // Sort by index of child content
            Comparator<Map> comparator = new Comparator<Map>() {
                @Override
                public int compare(Map left, Map right) {
                    double leftIndex = Double.valueOf(left.get("index").toString());
                    double rightIndex = Double.valueOf(right.get("index").toString());
                    return (int) (leftIndex - rightIndex); // use your logic
                }
            };
            Collections.sort(children, comparator);
            List<String> childIdentifiers = new ArrayList<>();
            StringBuilder whenAndThen = new StringBuilder();
            int i = 0;

            for (Map contentChild : children) {
                childIdentifiers.add(contentChild.get("identifier").toString());
                whenAndThen.append(String.format(Locale.US, " WHEN '%s' THEN %s ", contentChild.get("identifier").toString(), i));
                i++;
            }

            String orderBy = "";
            if (i > 0) {
                orderBy = String.format(Locale.US, " ORDER BY CASE %s %s END", ContentEntry.COLUMN_NAME_IDENTIFIER, whenAndThen.toString());
            }

            String filter;
            switch (childContents) {
                case ContentConstants.ChildContents.DOWNLOADED:
                    filter = String.format(Locale.US, " AND %s = '%s'", ContentEntry.COLUMN_NAME_CONTENT_STATE, ContentConstants.State.ARTIFACT_AVAILABLE);
                    break;

                case ContentConstants.ChildContents.SPINE:
                    filter = String.format(Locale.US, " AND %s = '%s'", ContentEntry.COLUMN_NAME_CONTENT_STATE, ContentConstants.State.ONLY_SPINE);
                    break;

                case ContentConstants.ChildContents.ALL:
                default:
                    filter = "";
                    break;
            }

            String query = String.format(Locale.US, "Select * from %s where %s in ('%s') %s %s",
                    ContentEntry.TABLE_NAME, ContentEntry.COLUMN_NAME_IDENTIFIER, StringUtil.join("','", childIdentifiers), filter, orderBy);
            ContentsModel contentsModel = ContentsModel.findWithCustomQuery(dbSession, query);
            if (contentsModel != null) {
                contentModelListInDB = contentsModel.getContentModelList();
            }
        }

        if (contentModelListInDB == null) {
            contentModelListInDB = new ArrayList<>();
        }

        return contentModelListInDB;
    }

    public static Map<String, Object> getSearchRequest(AppContext appContext, ContentImportRequest importRequest) {
        Map<String, Object> filterMap = new HashMap<>();
        filterMap.put("compatibilityLevel", getCompatibilityLevelFilter(appContext));
        filterMap.put("identifier", importRequest.getContentIds());
        filterMap.put("objectType", Collections.singletonList("Content"));

        HashMap<String, Object> requestMap = new HashMap<>();
        requestMap.put("filters", filterMap);
        requestMap.put("fields", Arrays.asList(KEY_DOWNLOAD_URL, KEY_VARIANTS, KEY_MIME_TYPE));

        return requestMap;
    }

    public static Map<String, Object> getSearchContentRequest(AppContext appContext, IConfigService configService, ContentSearchCriteria criteria) {
        HashMap<String, Object> requestMap = new HashMap<>();
        requestMap.put("query", criteria.getQuery());
        requestMap.put("limit", criteria.getLimit());
        requestMap.put("mode", criteria.getMode());
        requestMap.put("facets", Arrays.asList(criteria.getFacets()));
        addSortCriteria(requestMap, criteria);
        if (SearchType.SEARCH.equals(criteria.getSearchType())) {
            requestMap.put("filters", getSearchRequest(appContext, configService, criteria));
        } else {
            requestMap.put("filters", getFilterRequest(appContext, criteria));
        }
        return requestMap;
    }

    private static void addSortCriteria(Map requestMap, ContentSearchCriteria criteria) {
        Map<String, String> sortMap = new HashMap<>();
        List<ContentSortCriteria> sortCriterias = criteria.getSortCriteria();
        if (sortCriterias != null && sortCriterias.size() > 0) {
            //TODO for now only handling the first sort criteria. As of now only one criteria is used in the system and the list has been kept for future compatibility.
            sortMap.put(sortCriterias.get(0).getSortAttribute(), sortCriterias.get(0).getSortOrder().getValue());
            requestMap.put("sort_by", sortMap);
        }
    }

    private static Map<String, Object> getSearchRequest(AppContext appContext, IConfigService configService, ContentSearchCriteria criteria) {
        Map<String, Object> filterMap = new HashMap<>();

        // Populating implicit search criteria.
        filterMap.put("compatibilityLevel", getCompatibilityLevelFilter(appContext));
        filterMap.put("status", Arrays.asList(criteria.getContentStatusArray()));
        filterMap.put("objectType", Collections.singletonList("Content"));
        filterMap.put("contentType", Arrays.asList(criteria.getContentTypes()));

        //Add filters for criteria attributes
        // Add subject filter
        if (criteria.getAge() > 0)
            applyListingFilter(configService, MasterDataType.AGEGROUP, String.valueOf(criteria.getAge()), filterMap);

        // Add board filter
        if (!StringUtil.isNullOrEmpty(criteria.getBoard()))
            applyListingFilter(configService, MasterDataType.BOARD, criteria.getBoard(), filterMap);

        // Add medium filter
        if (!StringUtil.isNullOrEmpty(criteria.getMedium()))
            applyListingFilter(configService, MasterDataType.MEDIUM, criteria.getMedium(), filterMap);

        // Add standard filter
        if (criteria.getGrade() > 0)
            applyListingFilter(configService, MasterDataType.GRADELEVEL, String.valueOf(criteria.getGrade()), filterMap);

        String[] audienceArr = criteria.getAudience();
        if (audienceArr != null && audienceArr.length > 0) {
            for (String audience : audienceArr) {
                applyListingFilter(configService, MasterDataType.AUDIENCE, audience, filterMap);
            }
        }

        String[] channelArr = criteria.getChannel();
        if (channelArr != null && channelArr.length > 0) {
            for (String channel : channelArr) {
                applyListingFilter(configService, MasterDataType.CHANNEL, channel, filterMap);
            }
        }

        return filterMap;
    }

    private static Map<String, Object> getFilterRequest(AppContext appContext, ContentSearchCriteria criteria) {
        Map<String, Object> filterMap = new HashMap<>();
        filterMap.put("compatibilityLevel", getCompatibilityLevelFilter(appContext));
        if (criteria.getFacetFilters() != null) {
            for (ContentSearchFilter filter : criteria.getFacetFilters()) {
                List<String> filterValueList = new ArrayList<>();
                for (FilterValue filterValue : filter.getValues()) {
                    if (filterValue.isApply()) {
                        filterValueList.add(filterValue.getName());
                    }
                }

                if (!filterValueList.isEmpty()) {
                    filterMap.put(filter.getName(), filterValueList);
                }
            }
        }

        if (criteria.getImpliedFilters() != null) {
            for (ContentSearchFilter filter : criteria.getImpliedFilters()) {
                List<String> filterValueList = new ArrayList<>();
                for (FilterValue filterValue : filter.getValues()) {
                    if (filterValue.isApply()) {
                        filterValueList.add(filterValue.getName());
                    }
                }

                if (!filterValueList.isEmpty()) {
                    filterMap.put(filter.getName(), filterValueList);
                }
            }
        }

        if (!filterMap.containsKey("contentType")) {
            filterMap.put("contentType", Arrays.asList(criteria.getContentTypes()));
        }

        return filterMap;
    }

    private static Map<String, Integer> getCompatibilityLevelFilter(AppContext appContext) {
        Map<String, Integer> compatibilityLevelMap = new HashMap<>();
        compatibilityLevelMap.put("min", appContext.getParams().getInt(ServiceConstants.Params.MIN_COMPATIBILITY_LEVEL));
        compatibilityLevelMap.put("max", appContext.getParams().getInt(ServiceConstants.Params.MAX_COMPATIBILITY_LEVEL));
        return compatibilityLevelMap;
    }

    private static void addFiltersIfNotAvailable(Map<String, Object> filterMap, String key, List<String> values) {
        if (filterMap.isEmpty() || filterMap.get(key) == null) {
            filterMap.put(key, values);
        }
    }

    private static void applyListingFilter(IConfigService configService, MasterDataType masterDataType, String propertyValue, Map<String, Object> filterMap) {
        if (configService != null && propertyValue != null) {
            String property = masterDataType.getValue();

            if (masterDataType == MasterDataType.AGEGROUP) {
                masterDataType = MasterDataType.AGE;
            }

            GenieResponse<MasterData> masterDataResponse = configService.getMasterData(masterDataType);

            if (masterDataResponse.getStatus()) {
                MasterData masterData = masterDataResponse.getResult();

                for (MasterDataValues masterDataValues : masterData.getValues()) {
                    if (masterDataValues.getTelemetry().equals(propertyValue)) {
                        Map<String, Object> searchMap = masterDataValues.getSearch();
                        Map filtersMap = (Map) searchMap.get("filters");
                        if (filtersMap != null) {
                            Iterator it = filtersMap.entrySet().iterator();
                            while (it.hasNext()) {
                                Set values = new HashSet();
                                Map.Entry entry = (Map.Entry) it.next();
                                List filterMapValue = (List) filterMap.get(entry.getKey());
                                if (filterMapValue != null) {
                                    values.addAll(filterMapValue);
                                }
                                values.addAll((List) filtersMap.get(entry.getKey()));
                                filterMap.put(entry.getKey().toString(), new ArrayList<>(values));
                            }
                        }
                        break;
                    }
                }
            }
        }
    }

    public static ContentSearchCriteria createFilterCriteria(IConfigService configService, ContentSearchCriteria previousCriteria, List<Map<String, Object>> facets, Map<String, Object> appliedFilterMap) {
        List<ContentSearchFilter> facetFilters = new ArrayList<>();
        ContentSearchCriteria.FilterBuilder filterBuilder = new ContentSearchCriteria.FilterBuilder();
        filterBuilder.query(previousCriteria.getQuery())
                .limit(previousCriteria.getLimit())
                .contentTypes(previousCriteria.getContentTypes())
                .sort(previousCriteria.getSortCriteria() == null ? new ArrayList<ContentSortCriteria>() : previousCriteria.getSortCriteria());

        if ("soft".equals(previousCriteria.getMode())) {
            filterBuilder.softFilters();
        }

        if (facets == null) {
            filterBuilder.facetFilters(facetFilters);
            return filterBuilder.build();
        }

        Map<String, Object> ordinalsMap = new HashMap<>();
        if (configService != null) {
            GenieResponse<Map<String, Object>> ordinalsResponse = configService.getOrdinals();
            if (ordinalsResponse.getStatus()) {
                ordinalsMap = ordinalsResponse.getResult();
            }
        }

        for (Map<String, Object> facetMap : facets) {
            ContentSearchFilter filter = new ContentSearchFilter();
            String facetName = null;
            if (facetMap.containsKey("name")) {
                facetName = (String) facetMap.get("name");
            }

            List<Map<String, Object>> facetValues = null;
            if (facetMap.containsKey("values")) {
                facetValues = (List<Map<String, Object>>) facetMap.get("values");
            }

            List<String> facetOrder = null;
            if (ordinalsMap.containsKey(facetName)) {
                facetOrder = (List<String>) ordinalsMap.get(facetName);
            }

            List<String> appliedFilter = null;
            if (appliedFilterMap != null) {
                appliedFilter = (List<String>) appliedFilterMap.get(facetName);
            }

            List<FilterValue> values = getSortedFilterValuesWithAppliedFilters(facetValues, facetOrder, appliedFilter);

            if (!StringUtil.isNullOrEmpty(facetName)) {
                filter.setName(facetName);
                filter.setValues(values);

                // Set the filter
                facetFilters.add(filter);
            }

            appliedFilterMap.remove(facetName);
        }
        filterBuilder.facetFilters(facetFilters);
        filterBuilder.impliedFilters(mapFilterValues(appliedFilterMap));
        return filterBuilder.build();
    }

    private static List<FilterValue> getSortedFilterValuesWithAppliedFilters(List<Map<String, Object>> facetValues, List<String> facetOrder, List<String> appliedFilter) {
        Map<Integer, FilterValue> map = new TreeMap<>();

        for (Map<String, Object> valueMap : facetValues) {
            String name = (String) valueMap.get("name");
            int index = indexOf(facetOrder, name);

            boolean applied = false;
            if (appliedFilter != null && appliedFilter.contains(name)) {
                applied = true;
            }

            FilterValue filterValue = new FilterValue();
            filterValue.setName(name);
            filterValue.setApply(applied);
            if (valueMap.containsKey("count")) {
                Double count = (Double) valueMap.get("count");
                filterValue.setCount(count.intValue());
            }

            map.put(index, filterValue);
        }

        List<FilterValue> valuesList = new ArrayList<>(map.values());
        return valuesList;
    }

    private static int indexOf(List<String> facetsOrder, String key) {
        if (!CollectionUtil.isNullOrEmpty(facetsOrder) && !StringUtil.isNullOrEmpty(key)) {
            for (int i = 0; i < facetsOrder.size(); i++) {
                if (key.equalsIgnoreCase(facetsOrder.get(i))) {
                    return i;
                }
            }
        }

        return -1;
    }

    public static HashMap<String, Object> getRecommendedContentRequest(RecommendedContentRequest request, String did) {
        HashMap<String, Object> contextMap = new HashMap<>();
        contextMap.put("did", did);
        contextMap.put("dlang", request.getLanguage());

        HashMap<String, Object> requestMap = new HashMap<>();
        requestMap.put("context", contextMap);
        requestMap.put("limit", request.getLimit());

        return requestMap;
    }

    public static HashMap<String, Object> getRelatedContentRequest(RelatedContentRequest request, String did) {
        String dlang = "";
        String uid = "";
        if (!StringUtil.isNullOrEmpty(request.getUid())) {
            uid = request.getUid();
        }
        if (!StringUtil.isNullOrEmpty(request.getLanguage())) {
            dlang = request.getLanguage();
        }

        HashMap<String, Object> contextMap = new HashMap<>();
        contextMap.put("contentid", request.getContentId());
        contextMap.put("uid", uid);
        contextMap.put("dlang", dlang);
        contextMap.put("did", did);

        HashMap<String, Object> requestMap = new HashMap<>();
        requestMap.put("context", contextMap);
        requestMap.put("limit", request.getLimit());

        return requestMap;
    }

    public static void refreshContentListingFromServer(final AppContext appContext, final IConfigService configService, final ContentListingCriteria contentListingCriteria, final String did) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                fetchContentListingFromServer(appContext, configService, contentListingCriteria, did);
            }
        }).start();
    }

    public static String fetchContentListingFromServer(AppContext appContext, IConfigService configService, ContentListingCriteria contentListingCriteria, String did) {
        Map<String, Object> requestMap = getContentListingRequest(appContext, configService, contentListingCriteria, did);
        ContentListingAPI api = new ContentListingAPI(appContext, contentListingCriteria.getContentListingId(), requestMap);
        GenieResponse apiResponse = api.post();
        String jsonStr = null;
        if (apiResponse.getStatus()) {
            jsonStr = apiResponse.getResult().toString();
            saveContentListingDataInDB(appContext, contentListingCriteria, jsonStr);
        }
        return jsonStr;
    }

    private static Map<String, Object> getContentListingRequest(AppContext appContext, IConfigService configService, ContentListingCriteria contentListingCriteria, String did) {
        HashMap<String, Object> contextMap = new HashMap<>();

        if (!StringUtil.isNullOrEmpty(contentListingCriteria.getUid())) {
            contextMap.put("uid", contentListingCriteria.getUid());
        }
        if (!StringUtil.isNullOrEmpty(contentListingCriteria.getLanguage())) {
            contextMap.put("dlang", contentListingCriteria.getLanguage());
        }
        contextMap.put("did", did);
        contextMap.put("contentid", "");

        Map<String, Object> filterMap = new HashMap<>();
        filterMap.put("compatibilityLevel", getCompatibilityLevelFilter(appContext));

        // Add subject filter
        if (!StringUtil.isNullOrEmpty(contentListingCriteria.getSubject()))
            applyListingFilter(configService, MasterDataType.SUBJECT, contentListingCriteria.getSubject(), filterMap);

        if (contentListingCriteria.getAge() > 0)
            applyListingFilter(configService, MasterDataType.AGEGROUP, String.valueOf(contentListingCriteria.getAge()), filterMap);

        // Add board filter
        if (!StringUtil.isNullOrEmpty(contentListingCriteria.getBoard()))
            applyListingFilter(configService, MasterDataType.BOARD, contentListingCriteria.getBoard(), filterMap);

        // Add medium filter
        if (!StringUtil.isNullOrEmpty(contentListingCriteria.getMedium()))
            applyListingFilter(configService, MasterDataType.MEDIUM, contentListingCriteria.getMedium(), filterMap);

        // Add standard filter
        if (contentListingCriteria.getGrade() > 0)
            applyListingFilter(configService, MasterDataType.GRADELEVEL, String.valueOf(contentListingCriteria.getGrade()), filterMap);

        String[] audienceArr = contentListingCriteria.getAudience();
        if (audienceArr != null && audienceArr.length > 0) {
            for (String audience : audienceArr) {
                applyListingFilter(configService, MasterDataType.AUDIENCE, audience, filterMap);
            }
        }

        String[] channelArr = contentListingCriteria.getChannel();
        if (channelArr != null && channelArr.length > 0) {
            for (String channel : channelArr) {
                applyListingFilter(configService, MasterDataType.CHANNEL, channel, filterMap);
            }
        }

        HashMap<String, Object> requestMap = new HashMap<>();
        requestMap.put("context", contextMap);
        requestMap.put("filters", filterMap);

        return requestMap;
    }

    private static void saveContentListingDataInDB(AppContext appContext, ContentListingCriteria contentListingCriteria, String jsonStr) {
        if (jsonStr == null) {
            return;
        }

        long expiryTime = DateUtil.getEpochTime() + ContentConstants.CACHE_TIMEOUT_HOME_CONTENT;

        ContentListingModel contentListingModelInDB = ContentListingModel.find(appContext.getDBSession(), contentListingCriteria);
        if (contentListingModelInDB != null) {
            contentListingModelInDB.delete();
        }
        ContentListingModel contentListingModel = ContentListingModel.build(appContext.getDBSession(), contentListingCriteria, jsonStr, expiryTime);
        contentListingModel.save();
    }

    public static ContentListing getContentListingResult(ContentListingCriteria contentListingCriteria, String jsonStr) {
        ContentListing contentListing = null;

        LinkedTreeMap map = GsonUtil.fromJson(jsonStr, LinkedTreeMap.class);
        LinkedTreeMap responseParams = (LinkedTreeMap) map.get("params");
        LinkedTreeMap result = (LinkedTreeMap) map.get("result");

        String responseMessageId = null;
        if (responseParams.containsKey("resmsgid")) {
            responseMessageId = (String) responseParams.get("resmsgid");
        }

        if (result != null) {
            contentListing = new ContentListing();
            contentListing.setContentListingId(contentListingCriteria.getContentListingId());
            contentListing.setResponseMessageId(responseMessageId);
            if (result.containsKey("page")) {
                contentListing.setContentListingSections(getSectionsFromPageMap((Map<String, Object>) result.get("page")));
            }
        }

        return contentListing;
    }

    private static List<ContentListingSection> getSectionsFromPageMap(Map<String, Object> pageMap) {
        List<ContentListingSection> contentListingSectionList = new ArrayList<>();

        List<Map<String, Object>> sections = (List<Map<String, Object>>) pageMap.get("sections");
        for (Map<String, Object> sectionMap : sections) {
            String contentDataList = null;
            if (sectionMap.containsKey("contents")) {
                contentDataList = GsonUtil.toJson(sectionMap.get("contents"));
            }

            ContentSearchCriteria contentSearchCriteria = null;
            Map<String, Object> searchMap = (Map<String, Object>) sectionMap.get("search");
            if (searchMap != null) {
                ContentSearchCriteria.FilterBuilder builder = new ContentSearchCriteria.FilterBuilder();
                if (searchMap.containsKey("query")) {
                    builder.query((String) searchMap.get("query"));
                }

                if (searchMap.containsKey("mode") && "soft".equals(searchMap.get("mode"))) {
                    builder.softFilters();
                }

                if (searchMap.containsKey("sort_by")) {
                    Map sortMap = (Map) searchMap.get("sort_by");
                    if (sortMap != null) {
                        Iterator it = sortMap.entrySet().iterator();
                        List<ContentSortCriteria> sortCriterias = new ArrayList<>();
                        while (it.hasNext()) {
                            Map.Entry keyValue = (Map.Entry) it.next();
                            ContentSortCriteria criteria = new ContentSortCriteria(keyValue.getKey().toString(), SortOrder.valueOf(keyValue.getValue().toString().toUpperCase()));
                            sortCriterias.add(criteria);
                        }
                        builder.sort(sortCriterias);
                    }
                } else {
                    builder.sort(new ArrayList<ContentSortCriteria>());
                }

                if (searchMap.containsKey("filters")) {
                    Map filtersMap = (Map) searchMap.get("filters");
                    if (filtersMap.containsKey("contentType")) {
                        ArrayList<String> contentType = (ArrayList<String>) filtersMap.get("contentType");
                        builder.contentTypes(contentType.toArray(new String[contentType.size()]));
                    }

                    builder.impliedFilters(mapFilterValues(filtersMap));
                }

                if (searchMap.containsKey("facets")) {
                    ArrayList<String> facets = (ArrayList<String>) searchMap.get("facets");
                    builder.facets(facets.toArray(new String[facets.size()]));
                }

                contentSearchCriteria = builder.build();
            }

            ContentListingSection contentListingSection = new ContentListingSection();
            contentListingSection.setResponseMessageId((String) sectionMap.get("resmsgid"));
            contentListingSection.setApiId((String) sectionMap.get("apiid"));
            contentListingSection.setSectionName(((Map) ((Map) sectionMap.get("display")).get("name")).get("en").toString());

            if (!StringUtil.isNullOrEmpty(contentDataList)) {
                Type type = new TypeToken<List<ContentData>>() {
                }.getType();
                List<ContentData> contentData = GsonUtil.getGson().fromJson(contentDataList, type);
                contentListingSection.setContentDataList(contentData);
            }
            contentListingSection.setContentSearchCriteria(contentSearchCriteria);

            contentListingSectionList.add(contentListingSection);
        }

        return contentListingSectionList;
    }

    private static List<ContentSearchFilter> mapFilterValues(Map filtersMap) {
        List<ContentSearchFilter> filters = new ArrayList<>();
        if (filtersMap != null && !filtersMap.isEmpty()) {
            Iterator it = filtersMap.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                String key = pair.getKey().toString();
                Object value = pair.getValue();

                if (value instanceof List) {
                    List<FilterValue> values = new ArrayList<>();
                    List<String> valueList = (List<String>) value;
                    for (String v : valueList) {
                        FilterValue filterValue = new FilterValue();
                        filterValue.setName(v);
                        filterValue.setApply(true);

                        values.add(filterValue);
                    }

                    ContentSearchFilter contentSearchFilter = new ContentSearchFilter();
                    contentSearchFilter.setName(key);
                    contentSearchFilter.setValues(values);

                    filters.add(contentSearchFilter);
                } else {
//                  TODO: No change required here. Best of genie will be removed soon and so we dont have to handle the genieScore. Also compatability level gets auto added into every search.
//                  key.equals("compatibilityLevel") && key.equals("genieScore")
//                  String[] stringArray = mFilterMap.get(values.getName());
//                  filterSet.addAll(Arrays.asList(stringArray));
                }
            }
        }
        return filters;
    }

    public static String getDownloadUrl(Map<String, Object> dataMap) {
        String downloadUrl = null;

        if (ContentConstants.MimeType.COLLECTION.equals(readMimeType(dataMap))) {
            ContentVariant spineContentVariant = getVariant(dataMap, "spine");
            if (spineContentVariant != null) {
                downloadUrl = spineContentVariant.getEcarUrl();
            }
        }

        if (StringUtil.isNullOrEmpty(downloadUrl)) {
            downloadUrl = (String) dataMap.get(KEY_DOWNLOAD_URL);
        }

        if (downloadUrl != null) {
            downloadUrl = downloadUrl.trim();
        }

        return downloadUrl;
    }

    public static List<ContentModel> getContentModelToExport(IDBSession dbSession, List<String> contentIds) {
        List<ContentModel> contentModelToExport = new ArrayList<>();
        Queue<ContentModel> queue = new LinkedList<>();

        List<ContentModel> contentModelList = findAllContentsWithIdentifiers(dbSession, contentIds);
        if (contentModelList != null) {
            queue.addAll(contentModelList);
        }

        ContentModel node;
        while (!queue.isEmpty()) {
            node = queue.remove();

            if (hasChildren(node.getLocalData())) {
                List<String> childContentsIdentifiers = getChildContentsIdentifiers(node.getLocalData());
                List<ContentModel> contentModelListInDB = findAllContentsWithIdentifiers(dbSession, childContentsIdentifiers);
                if (contentModelListInDB != null) {
                    queue.addAll(contentModelListInDB);
                }
            } else if (hasPreRequisites(node.getLocalData())) {
                List<String> preRequisitesIdentifiers = getPreRequisitesIdentifiers(node.getLocalData());
                List<ContentModel> preRequisitesListInDB = findAllContentsWithIdentifiers(dbSession, preRequisitesIdentifiers);
                if (preRequisitesListInDB != null) {
                    queue.addAll(preRequisitesListInDB);
                }
            }

            contentModelToExport.add(node);
        }

        return contentModelToExport;
    }

    public static List<ContentModel> deDupe(List<ContentModel> contentModelList) {
        Set<ContentModel> uniqueContentModels = new LinkedHashSet<>(contentModelList);

        return new ArrayList<>(uniqueContentModels);
    }

    public static String getExportedFileName(List<ContentModel> contentModelList) {
        String fileName = "blank.ecar";
        ContentModel firstContent = null;
        int rootContents = 0;

        if (contentModelList.size() > 0) {
            firstContent = contentModelList.get(0);
        }

        String appendName = "";

        for (ContentModel contentModel : contentModelList) {
            if (ContentConstants.Visibility.DEFAULT.equalsIgnoreCase(contentModel.getVisibility())) {
                rootContents++;
            }
        }

        if (rootContents > 1) {
            appendName = String.format(Locale.US, "+%s", rootContents - 1);
        }

        if (firstContent != null) {
            String name = readName(GsonUtil.fromJson(firstContent.getLocalData(), Map.class));
            if (!StringUtil.isNullOrEmpty(name) && name.length() > MAX_CONTENT_NAME) {
                name = name.substring(0, MAX_CONTENT_NAME - 3) + "...";
            }

            double pkgVersion;
            try {
                pkgVersion = readPkgVersion(firstContent.getLocalData());
            } catch (Exception e) {
                pkgVersion = 0;
            }

            fileName = String.format(Locale.US, "%s-v%s%s.ecar", name, "" + pkgVersion, appendName);
        }

        return fileName;
    }

}
