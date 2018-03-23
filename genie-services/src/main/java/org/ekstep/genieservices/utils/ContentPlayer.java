package org.ekstep.genieservices.utils;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import org.ekstep.genieservices.GenieService;
import org.ekstep.genieservices.IUserService;
import org.ekstep.genieservices.ServiceConstants;
import org.ekstep.genieservices.commons.AppContext;
import org.ekstep.genieservices.commons.IParams;
import org.ekstep.genieservices.commons.IPlayerConfig;
import org.ekstep.genieservices.commons.bean.Content;
import org.ekstep.genieservices.commons.bean.ContentData;
import org.ekstep.genieservices.commons.bean.CorrelationData;
import org.ekstep.genieservices.commons.bean.HierarchyInfo;
import org.ekstep.genieservices.commons.bean.UserSession;
import org.ekstep.genieservices.commons.utils.GsonUtil;
import org.ekstep.genieservices.commons.utils.Logger;
import org.ekstep.genieservices.commons.utils.ReflectionUtil;
import org.ekstep.genieservices.tag.cache.TelemetryTagCache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created on 1/6/17.
 *
 * @author swayangjit
 */
public class ContentPlayer {

    private static final String TAG = ContentPlayer.class.getSimpleName();

    private static ContentPlayer sContentPlayer;
    private AppContext mAppContext;
    private IUserService mUserService;
    private String mQualifier;
    private IPlayerConfig playerConfig;

    private ContentPlayer(AppContext appContext, String qualifier, IPlayerConfig playerConfig) {
        this.mQualifier = qualifier;
        this.playerConfig = playerConfig;
        this.mAppContext = appContext;
        this.mUserService = GenieService.getService().getUserService();
    }

    public static void init(AppContext appContext) {
        if (sContentPlayer == null) {
            IPlayerConfig playerConfig = null;
            String playerConfigClass = appContext.getParams().getString(ServiceConstants.Params.PLAYER_CONFIG);
            String qualifier = appContext.getParams().getString(IParams.Key.APPLICATION_ID);
            if (playerConfigClass != null) {
                Class<?> classInstance = ReflectionUtil.getClass(playerConfigClass);
                if (classInstance != null) {
                    playerConfig = (IPlayerConfig) ReflectionUtil.getInstance(classInstance);
                }
                sContentPlayer = new ContentPlayer(appContext, qualifier, playerConfig);
            }

        }
    }

    public static void play(Context context, Content content, Map<String, String> rollup) {
        ContentData contentData = content.getContentData();
        if (sContentPlayer.mQualifier == null) {
            Toast.makeText(context, "App qualifier not found", Toast.LENGTH_SHORT).show();
            return;
        }
        if (sContentPlayer.playerConfig == null) {
            Toast.makeText(context, "Implement IPlayerConfig and define in build.config", Toast.LENGTH_SHORT).show();
            Logger.e(TAG, "Implement IPlayerConfig and define in build.config");
            return;
        }

        Intent intent = sContentPlayer.playerConfig.getPlayerIntent(context, content);
        if (intent == null) {
            return;
        }

        UserSession currentUserSession = null;
        String sid = null;
        String uid = null;
        if (sContentPlayer.mUserService != null) {
            currentUserSession = sContentPlayer.mUserService.getCurrentUserSession().getResult();
        }
        if (currentUserSession != null && currentUserSession.isValid()) {
            sid = currentUserSession.getSid();
            uid = currentUserSession.getUid();
        }

        Map<String, Object> bundleMap = new HashMap<>();
        Map<String, Object> contextMap = new HashMap<>();
        contextMap.put("origin", "Genie");
        Map<String, String> languageInfoMap = (Map<String, String>) intent.getSerializableExtra("languageInfo");
        contextMap.put("languageInfo", languageInfoMap != null ? GsonUtil.toJson(languageInfoMap) : "");
        contextMap.put("appQualifier", sContentPlayer.mQualifier);
        contextMap.put("tags", TelemetryTagCache.activeTags(sContentPlayer.mAppContext));
        contextMap.put("rollup", rollup != null ? rollup : "");
        contextMap.put("basepath", content.getBasePath());
        contextMap.put("mode", "play");
        contextMap.put("contentId", content.getIdentifier());
        contextMap.put("sid", sid != null ? sid : "");
        contextMap.put("did", sContentPlayer.mAppContext.getDeviceInfo().getDeviceID());

        Map<String, Object> actorMap = new HashMap<>();
        actorMap.put("id", uid != null ? uid : "");
        actorMap.put("type", "User");
        contextMap.put("actor", actorMap);

        contextMap.put("channel", sContentPlayer.mAppContext.getParams().getString(IParams.Key.CHANNEL_ID));
        Map<String, Object> pDataMap = new HashMap<>();
        pDataMap.put("id", sContentPlayer.mAppContext.getParams().getString(IParams.Key.PRODUCER_ID));
        pDataMap.put("ver", sContentPlayer.mAppContext.getParams().getString(IParams.Key.VERSION_NAME));
        pDataMap.put("pid", sContentPlayer.mAppContext.getParams().getString(IParams.Key.PRODUCER_UNIQUE_ID));
        contextMap.put("pdata", pDataMap);

        contextMap.put("cdata", createcDataList(content.getHierarchyInfo()));

        bundleMap.put("context", contextMap);

        Map<String, Object> configMap = (Map<String, Object>) intent.getSerializableExtra("config");
        if (configMap != null) {
            bundleMap.put("config", configMap);
        }

        bundleMap.put("metadata", contentData);
        intent.putExtra("playerConfig", GsonUtil.toJson(bundleMap));
        intent.addCategory(Intent.CATEGORY_LAUNCHER);

        context.startActivity(intent);
    }

    private static Map createHierarchyData(List<HierarchyInfo> hierarchyInfo) {
        Map hierarchyData = new HashMap();
        String id = "";
        String identifierType = null;
        for (HierarchyInfo infoItem : hierarchyInfo) {
            if (identifierType == null) {
                identifierType = infoItem.getContentType();
            }
            id += id.length() == 0 ? "" : "/";
            id += infoItem.getIdentifier();
        }
        hierarchyData.put("id", id);
        hierarchyData.put("type", identifierType);
        return hierarchyData;
    }

    private static List<CorrelationData> createcDataList(List<HierarchyInfo> hierarchyInfo) {
        List<CorrelationData> correlationDataList = new ArrayList<>();
        if (hierarchyInfo != null) {
            for (HierarchyInfo infoItem : hierarchyInfo) {
                correlationDataList.add(new CorrelationData(infoItem.getIdentifier(), infoItem.getContentType()));
            }
        }
        return correlationDataList;


    }
}
