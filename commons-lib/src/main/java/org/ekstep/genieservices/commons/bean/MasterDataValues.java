package org.ekstep.genieservices.commons.bean;

import org.ekstep.genieservices.commons.utils.GsonUtil;

import java.util.Map;

/**
 * This class holds the details about each of the master data values.
 *
 */
public class MasterDataValues {

    private String label;
    private String value;
    private String telemetry;
    private String description;
    private Map<String, Object> search;

    public String getLabel() {
        return label;
    }

    public String getValue() {
        return value;
    }

    public String getTelemetry() {
        return telemetry;
    }

    public String getDescription() {
        return description;
    }

    public Map<String, Object> getSearch() {
        return search;
    }

    @Override
    public String toString() {
        return GsonUtil.toJson(this);
    }
}
