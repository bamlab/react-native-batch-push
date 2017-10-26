#import "RNBatchPush.h"

@implementation RNBatchPush

- (dispatch_queue_t)methodQueue
{
    return dispatch_get_main_queue();
}
RCT_EXPORT_MODULE()

- (id)init {
    self = [super init];

    if (self != nil) {
        NSDictionary *info = [[NSBundle mainBundle] infoDictionary];
        NSString *batchAPIKey = [info objectForKey:@"BatchAPIKey"];
        [Batch startWithAPIKey:batchAPIKey];
    }

    return self;
}

RCT_EXPORT_METHOD(registerForRemoteNotifications)
{
    [BatchPush registerForRemoteNotifications];
}

RCT_EXPORT_METHOD(loginUser:(nullable NSString*)userID)
{
    BatchUserDataEditor *editor = [BatchUser editor];
    [editor setIdentifier:userID];
    [editor save];
}

RCT_EXPORT_METHOD(logoutUser)
{
    BatchUserDataEditor *editor = [BatchUser editor];
    [editor setIdentifier:nil];
    [editor save];
}

RCT_REMAP_METHOD(fetchNewNotifications,
                 fetchNewNotificationsWithUserID:(NSString*)userID authKey:(NSString*)authKey resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
{
    NSArray *events = [[NSArray alloc] init];
    resolve(events);
}

@end
