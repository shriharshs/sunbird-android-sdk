package org.ekstep.genieservices.content.chained.move;

import org.ekstep.genieservices.ServiceConstants;
import org.ekstep.genieservices.commons.AppContext;
import org.ekstep.genieservices.commons.GenieResponseBuilder;
import org.ekstep.genieservices.commons.bean.GenieResponse;
import org.ekstep.genieservices.commons.chained.IChainable;
import org.ekstep.genieservices.commons.utils.FileUtil;
import org.ekstep.genieservices.content.ContentHandler;
import org.ekstep.genieservices.content.bean.MoveContentContext;

/**
 * Created on 9/18/2017.
 *
 * @author anil
 */
public class DeviceMemoryCheck implements IChainable<Void, MoveContentContext> {

    private static final String TAG = DeviceMemoryCheck.class.getSimpleName();

    private IChainable<Void, MoveContentContext> nextLink;

    @Override
    public GenieResponse<Void> execute(AppContext appContext, MoveContentContext moveContentContext) {
        long deviceUsableSpace = FileUtil.getFreeUsableSpace(moveContentContext.getDestinationFolder());
        long spaceRequired = ContentHandler.getSizeOnDevice(moveContentContext.getContentIds());

        if (!FileUtil.isFreeSpaceAvailable(deviceUsableSpace, spaceRequired, 0)) {
            return GenieResponseBuilder.getErrorResponse(ServiceConstants.ErrorCode.MOVE_FAILED,
                    String.format("%s bytes available free space, required %s bytes.", deviceUsableSpace, spaceRequired),
                    TAG);
        }

        return nextLink.execute(appContext, moveContentContext);
    }

    @Override
    public IChainable<Void, MoveContentContext> then(IChainable<Void, MoveContentContext> link) {
        nextLink = link;
        return link;
    }
}
