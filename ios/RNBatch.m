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
    
    return self;
}

RCT_EXPORT_METHOD(start:(BOOL) doNotDisturb)
{
    NSDictionary *info = [[NSBundle mainBundle] infoDictionary];
    NSString *batchAPIKey = [info objectForKey:@"BatchAPIKey"];
    [BatchMessaging setDoNotDisturb:doNotDisturb];
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

RCT_EXPORT_METHOD(userData_save:(NSArray*)actions)
{
    BatchUserDataEditor *editor = [BatchUser editor];
    for (NSDictionary* action in actions) {
        NSString* type = action[@"type"];
        
        // Set double, long, NSString, bool class values
        if([type isEqualToString:@"setAttribute"]) {
            [editor setAttribute:action[@"value"] forKey:action[@"key"]];
        }
        
        // Handle dates
        // @TODO: prevent date parsing from erroring
        else if([type isEqualToString:@"setDateAttribute"]) {
            double timestamp = [action[@"value"] doubleValue];
            NSTimeInterval unixTimeStamp = timestamp / 1000.0;
            NSDate *date = [NSDate dateWithTimeIntervalSince1970:unixTimeStamp];
            [editor setAttribute:date forKey:action[@"key"]];
        }
        
        else if([type isEqualToString:@"removeAttribute"]) {
            [editor removeAttributeForKey:action[@"key"]];
        }
        
        else if([type isEqualToString:@"clearAttributes"]) {
            [editor clearAttributes];
        }
        
        else if([type isEqualToString:@"setIdentifier"]) {
            [editor setIdentifier:action[@"value"]];
        }
        
        else if([type isEqualToString:@"setLanguage"]) {
            [editor setLanguage:action[@"value"]];
        }
        
        else if([type isEqualToString:@"setRegion"]) {
            [editor setRegion:action[@"value"]];
        }
        
        else if([type isEqualToString:@"addTag"]) {
            [editor addTag:action[@"tag"] inCollection:action[@"collection"]];
        }
        
        else if([type isEqualToString:@"removeTag"]) {
            [editor removeTag:action[@"tag"] fromCollection:action[@"collection"]];
        }
        
        else if([type isEqualToString:@"clearTagCollection"]) {
            [editor clearTagCollection:action[@"collection"]];
        }
        
        else if([type isEqualToString:@"clearTags"]) {
            [editor clearTags];
        }
    }
    [editor save];
}

// Inbox module

RCT_EXPORT_METHOD(inbox_fetchNotifications:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
{
    BatchInboxFetcher* fetcher = [BatchInbox fetcher];
    [fetcher fetchNewNotifications:^(NSError * _Nullable error, NSArray<BatchInboxNotificationContent *> * _Nullable notifications, BOOL foundNewNotifications, BOOL endReached) {
        
        if (error) {
            reject(@"Inbox", @"Failed to fetch new notifications", error);
        }
        
        
    }];
}

// Messaging module

RCT_EXPORT_METHOD(messaging_setNotDisturbed:(BOOL) active)
{
    [BatchMessaging setDoNotDisturb:active];
}

RCT_EXPORT_METHOD(messaging_showPendingMessage) {
    [BatchMessaging showPendingMessage];
}

@end
