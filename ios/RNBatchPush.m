@import Batch;
#import "RNBatchPush.h"

@implementation RNBatchPush

- (dispatch_queue_t)methodQueue
{
    return dispatch_get_main_queue();
}
RCT_EXPORT_MODULE()

RCT_EXPORT_METHOD(registerForRemoteNotifications)
{
  [BatchPush registerForRemoteNotifications];
}

RCT_EXPORT_METHOD(setCustomUserID:(nullable NSString*)userID)
{
  BatchUserDataEditor *editor = [BatchUser editor];
  [editor setIdentifier:userID];
  [editor save];
}

@end
