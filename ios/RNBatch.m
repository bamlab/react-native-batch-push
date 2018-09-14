# import <React/RCTConvert.h>
# import "RNBatch.h"

@implementation RNBatch

- (dispatch_queue_t)methodQueue
{
    return dispatch_get_main_queue();
}
RCT_EXPORT_MODULE()

- (id)init {
    self = [super init];
    
//    if (self != nil) {
//        NSDictionary *info = [[NSBundle mainBundle] infoDictionary];
//        NSString *batchAPIKey = [info objectForKey:@"BatchAPIKey"];
//        [Batch startWithAPIKey:batchAPIKey];
//    }
    
    return self;
}

RCT_EXPORT_METHOD(start)
{
    NSDictionary *info = [[NSBundle mainBundle] infoDictionary];
    NSString *batchAPIKey = [info objectForKey:@"BatchAPIKey"];
    [Batch startWithAPIKey:batchAPIKey];
}

RCT_EXPORT_METHOD(optIn)
{
    [Batch optIn];
}

RCT_EXPORT_METHOD(optOut)
{
    [Batch optOut];
}

RCT_EXPORT_METHOD(optOutAndWipeData)
{
    [Batch optOutAndWipeData];
}

// Push Module

RCT_EXPORT_METHOD(push_registerForRemoteNotifications)
{
    [BatchPush registerForRemoteNotifications];
}

RCT_EXPORT_METHOD(push_clearBadge)
{
    [BatchPush clearBadge];
}

RCT_EXPORT_METHOD(push_dismissNotifications)
{
    [BatchPush dismissNotifications];
}

RCT_EXPORT_METHOD(push_getLastKnownPushToken:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
{
    NSString* lastKnownPushToken = [BatchPush lastKnownPushToken];
    resolve(lastKnownPushToken);
}

// User module

RCT_EXPORT_METHOD(userData_getInstallationId:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
{
    NSString* installationId = [BatchUser installationID];
    resolve(installationId);
}

RCT_EXPORT_METHOD(userData_setLanguage:(NSString*)locale)
{
    BatchUserDataEditor *editor = [BatchUser editor];
    [editor setLanguage:locale];
    [editor save];
}

RCT_EXPORT_METHOD(userData_setIdentifier:(NSString*)identifier)
{
    BatchUserDataEditor *editor = [BatchUser editor];
    [editor setIdentifier:identifier];
    [editor save];
}

@end
