#import <React/RCTConvert.h>
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

RCT_EXPORT_METHOD(setAttribute:(NSString*)key value:(NSString*)value)
{
    BatchUserDataEditor *editor = [BatchUser editor];
    [editor setAttribute:key forKey:value];
    [editor save];
}

RCT_EXPORT_METHOD(setDateAttribute:(NSString*)key value:(double)timestamp)
{
    NSTimeInterval unixTimeStamp = timestamp / 1000.0;
    NSDate *date = [NSDate dateWithTimeIntervalSince1970:unixTimeStamp];
    BatchUserDataEditor *editor = [BatchUser editor];
    [editor setAttribute:date forKey:key];
    [editor save];
}

RCT_EXPORT_METHOD(trackLocation:(NSDictionary*)locationDictionary){
    CLLocationDegrees latitude = [RCTConvert double:locationDictionary[@"latitude"]];
    CLLocationDegrees longitude = [RCTConvert double:locationDictionary[@"longitude"]];
    CLLocation *location = [[CLLocation alloc] initWithLatitude:latitude longitude:longitude];
    [BatchUser trackLocation:location];
}

RCT_REMAP_METHOD(fetchNewNotifications,
                 fetchNewNotificationsWithUserID:(NSString*)userID authKey:(NSString*)authKey resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
{
    BatchInboxFetcher* inboxFetcher = [BatchInbox fetcherForUserIdentifier:userID authenticationKey:authKey];
    [inboxFetcher fetchNewNotifications:^(NSError * _Nullable error, NSArray<BatchInboxNotificationContent *> * _Nullable notifications, BOOL foundNewNotifications, BOOL endReached) {

        if(error) {
            reject(@"BATCH_ERROR", @"Error fetching new notifications", error);
            return;
        }

        NSMutableArray* jsNotifications = [[NSMutableArray alloc] init];
        
        for (BatchInboxNotificationContent* notification in notifications) {
            NSMutableDictionary* jsNotification = [NSMutableDictionary new];
            [jsNotification setObject:notification.title forKey:@"title"];
            [jsNotification setObject:notification.body forKey:@"body"];
            [jsNotification setObject:@([notification.date timeIntervalSince1970] * 1000.0) forKey:@"timestamp"];
            [jsNotification setObject:notification.payload forKey:@"payload"];
            [jsNotifications addObject:jsNotification];
        }

        resolve(jsNotifications);
    }];
}

@end
