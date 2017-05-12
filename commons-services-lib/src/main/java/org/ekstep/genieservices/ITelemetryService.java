package org.ekstep.genieservices;

import org.ekstep.genieservices.commons.bean.GenieResponse;
import org.ekstep.genieservices.commons.bean.telemetry.BaseTelemetry;

/**
 * Created by swayangjit on 10/5/17.
 */

public interface ITelemetryService {

    /**
     * This api will save the telemetry details passed to it as String.
     * <p>
     * <p> On successful saving the telemetry, the response will return status as TRUE and with "Event Saved Successfully" message.
     * <p>
     * <p>On failing to save the telemetry details, the response will return status as FALSE and the error be the following:
     * <p>PROCESSING_ERROR
     *
     * @param eventString
     * @return {@link GenieResponse<Void>}
     */
    GenieResponse<Void> saveTelemetry(String eventString);

    /**
     * This api will save the telemetry details passed to it as {@link BaseTelemetry}.
     * <p>
     * <p> On successful saving the telemetry, the response will return status as TRUE and with "Event Saved Successfully" message.
     * <p>
     * <p>On failing to save the telemetry details, the response will return status as FALSE and the error be the following:
     * <p>PROCESSING_ERROR
     *
     * @param event
     * @return {@link GenieResponse<Void>}
     */
    GenieResponse<Void> saveTelemetry(BaseTelemetry event);

}
