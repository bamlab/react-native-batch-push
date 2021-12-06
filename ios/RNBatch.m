# import <React/RCTConvert.h>
# import "RNBatch.h"
# import "RNBatchOpenedNotificationObserver.h"

@implementation RNBatch
{
    bool hasListeners;
}

+ (BOOL)requiresMainQueueSetup
{
    return NO;
}
- (id)safeNilValue: (id)value
{
    if (value == (id)[NSNull null]) {
        return nil;
    }
    return value;
}
- (void)dealloc
{
    [_batchInboxFetcherMap removeAllObjects];
    _batchInboxFetcherMap = nil;
}
- (instancetype)init
{
    self = [super init];
    _batchInboxFetcherMap = [NSMutableDictionary new];
    [BatchEventDispatcher addDispatcher:self];
    return self;
}

RCT_EXPORT_MODULE()

+ (void)start
{
    setenv("BATCH_PLUGIN_VERSION", PluginVersion, 1);

    NSDictionary *info = [[NSBundle mainBundle] infoDictionary];

    id doNotDisturbEnabled = [info objectForKey:@"BatchDoNotDisturbInitialState"];
    if (doNotDisturbEnabled != nil) {
        [BatchMessaging setDoNotDisturb:[doNotDisturbEnabled boolValue]];
    } else {
        [BatchMessaging setDoNotDisturb:false];
    }

    NSString *batchAPIKey = [info objectForKey:@"BatchAPIKey"];
    [Batch startWithAPIKey:batchAPIKey];
}

RCT_EXPORT_METHOD(optIn:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)
{
    [Batch optIn];
    [RNBatch start];
    resolve([NSNull null]);
}

RCT_EXPORT_METHOD(optOut:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)
{
    [Batch optOut];
    resolve([NSNull null]);
}

RCT_EXPORT_METHOD(optOutAndWipeData:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)
{
    [Batch optOutAndWipeData];
    resolve([NSNull null]);
}

RCT_EXPORT_METHOD(presentDebugViewController)
{
    dispatch_async(dispatch_get_main_queue(), ^{
        UIViewController *debugVC = [Batch debugViewController];
        if (debugVC) {
            [RCTPresentedViewController() presentViewController:debugVC animated:YES completion:nil];
        }
    });
}

// Event Dispatcher

-(void)startObserving {
    hasListeners = YES;
}

-(void)stopObserving {
    hasListeners = NO;
}

- (NSArray<NSString *> *)supportedEvents {
    NSMutableArray *events = [NSMutableArray new];

    for (int i = BatchEventDispatcherTypeNotificationOpen; i < BatchEventDispatcherTypeMessagingWebViewClick; i++) {
        NSString* eventName = [self mapBatchEventDispatcherTypeToRNEvent:i];
        if (eventName != nil) {
            [events addObject:eventName];
        }
    }

    return events;
}

- (void)dispatchEventWithType:(BatchEventDispatcherType)type
                      payload:(nonnull id<BatchEventDispatcherPayload>)payload {
    if (hasListeners) {
        NSString* eventName = [self mapBatchEventDispatcherTypeToRNEvent:type];
        if (eventName != nil) {
            [self sendEventWithName:eventName body:[self dictionaryWithEventDispatcherPayload:payload]];
        }
    }
}

- (nullable NSString *) mapBatchEventDispatcherTypeToRNEvent:(BatchEventDispatcherType)type {
    switch (type) {
        case BatchEventDispatcherTypeNotificationOpen:
            return @"notification_open";
        case BatchEventDispatcherTypeMessagingShow:
            return @"messaging_show";
        case BatchEventDispatcherTypeMessagingClose:
            return @"messaging_close";
        case BatchEventDispatcherTypeMessagingCloseError:
            return @"messaging_close_error";
        case BatchEventDispatcherTypeMessagingAutoClose:
            return @"messaging_auto_close";
        case BatchEventDispatcherTypeMessagingClick:
            return @"messaging_click";
        case BatchEventDispatcherTypeMessagingWebViewClick:
            return @"messaging_webview_click";
    }
}

- (NSDictionary*) dictionaryWithEventDispatcherPayload:(id<BatchEventDispatcherPayload>)payload
{
    NSMutableDictionary *output = [NSMutableDictionary dictionaryWithDictionary:@{
        @"isPositiveAction": @(payload.isPositiveAction),
    }];

    if (payload.deeplink != nil) {
        output[@"deeplink"] = payload.deeplink;
    }

    if (payload.trackingId != nil) {
        output[@"trackingId"] = payload.trackingId;
    }

    if (payload.deeplink != nil) {
        output[@"webViewAnalyticsIdentifier"] = payload.webViewAnalyticsIdentifier;
    }

    if (payload.notificationUserInfo != nil) {
        output[@"pushPayload"] = payload.notificationUserInfo;
    }

    return output;
}

// Push Module

RCT_EXPORT_METHOD(push_registerForRemoteNotifications)
{
    [BatchPush registerForRemoteNotifications];
}

RCT_EXPORT_METHOD(push_requestNotificationAuthorization)
{
    [BatchPush requestNotificationAuthorization];
}

RCT_EXPORT_METHOD(push_requestProvisionalNotificationAuthorization)
{
    [BatchPush requestProvisionalNotificationAuthorization];
}

RCT_EXPORT_METHOD(push_refreshToken)
{
    [BatchPush refreshToken];
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

RCT_EXPORT_METHOD(push_getInitialDeeplink:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
{
    resolve([RNBatchOpenedNotificationObserver getInitialDeeplink]);
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
            [editor setAttribute:[self safeNilValue:action[@"value"]] forKey:action[@"key"]];

        }

        // Handle dates
        // @TODO: prevent date parsing from erroring
        else if([type isEqualToString:@"setDateAttribute"]) {
            double timestamp = [action[@"value"] doubleValue];
            NSTimeInterval unixTimeStamp = timestamp / 1000.0;
            NSDate *date = [NSDate dateWithTimeIntervalSince1970:unixTimeStamp];
            [editor setAttribute:date forKey:action[@"key"]];
        }

        else if([type isEqualToString:@"setURLAttribute"]) {
            NSURL *url = [NSURL URLWithString:[self safeNilValue:action[@"value"]]];
            [editor setAttribute:url forKey:action[@"key"]];
        }

        else if([type isEqualToString:@"removeAttribute"]) {
            [editor removeAttributeForKey:action[@"key"]];
        }

        else if([type isEqualToString:@"clearAttributes"]) {
            [editor clearAttributes];
        }

        else if([type isEqualToString:@"setIdentifier"]) {
            [editor setIdentifier:[self safeNilValue:action[@"value"]]];

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

// Event tracking

RCT_EXPORT_METHOD(userData_trackEvent:(NSString*)name label:(NSString*)label data:(NSDictionary*)serializedEventData)
{
    BatchEventData *batchEventData = nil;

    if ([serializedEventData isKindOfClass:[NSDictionary class]])
    {
        batchEventData = [BatchEventData new];

        if (![serializedEventData isKindOfClass:[NSDictionary class]])
        {
            NSLog(@"RNBatch: Error while tracking event data: event data should be an object or null");
            return;
        }

        NSArray<NSString*>* tags = serializedEventData[@"tags"];
        NSDictionary<NSString*, NSDictionary*>* attributes = serializedEventData[@"attributes"];

        if (![tags isKindOfClass:[NSArray class]])
        {
            NSLog(@"RNBatch: Error while tracking event data: event data.tags should be an array");
            return;
        }
        if (![attributes isKindOfClass:[NSDictionary class]])
        {
            NSLog(@"RNBatch: Error while tracking event data: event data.attributes should be a dictionnary");
            return;
        }

        for (NSString *tag in tags)
        {
            if (![tag isKindOfClass:[NSString class]])
            {
                NSLog(@"RNBatch: Error while tracking event data: event data.tag childrens should all be strings");
                return;
            }
            [batchEventData addTag:tag];
        }

        for (NSString *key in attributes.allKeys)
        {
            NSDictionary *typedAttribute = attributes[key];
            if (![typedAttribute isKindOfClass:[NSDictionary class]])
            {
                NSLog(@"RNBatch: Error while tracking event data: event data.attributes childrens should all be String/Dictionary tuples");
                return;
            }

            NSString *type = typedAttribute[@"type"];
            NSObject *value = typedAttribute[@"value"];

            if ([@"string" isEqualToString:type]) {
                if (![value isKindOfClass:[NSString class]])
                {
                    NSLog(@"RNBatch: Error while tracking event data: event data.attributes: expected string value, got something else");
                    return;
                }
                [batchEventData putString:(NSString*)value forKey:key];
            } else if ([@"boolean" isEqualToString:type]) {
                if (![value isKindOfClass:[NSNumber class]])
                {
                    NSLog(@"RNBatch: Error while tracking event data: event data.attributes: expected number (boolean) value, got something else");
                    return;
                }
                [batchEventData putBool:[(NSNumber*)value boolValue] forKey:key];
            } else if ([@"integer" isEqualToString:type]) {
                if (![value isKindOfClass:[NSNumber class]])
                {
                    NSLog(@"RNBatch: Error while tracking event data: event data.attributes: expected number (integer) value, got something else");
                    return;
                }
                [batchEventData putInteger:[(NSNumber*)value integerValue] forKey:key];
            } else if ([@"float" isEqualToString:type]) {
                if (![value isKindOfClass:[NSNumber class]])
                {
                    NSLog(@"RNBatch: Error while tracking event data: event data.attributes: expected number (float) value, got something else");
                    return;
                }
                [batchEventData putDouble:[(NSNumber*)value doubleValue] forKey:key];
            } else if ([@"date" isEqualToString:type]) {
                if (![value isKindOfClass:[NSNumber class]])
                {
                    NSLog(@"RNBatch: Error while tracking event data: event data.attributes: expected number value, got something else");
                    return;
                }
                NSDate *date = [NSDate dateWithTimeIntervalSince1970:[(NSNumber*)value doubleValue] / 1000.0];
                [batchEventData putDate:date forKey:key];
            } else if ([@"url" isEqualToString:type]) {
                if (![value isKindOfClass:[NSString class]])
                {
                    NSLog(@"RNBatch: Error while tracking event data: event data.attributes: expected string value, got something else");
                    return;
                }
                [batchEventData putURL:[NSURL URLWithString:(NSString*) value] forKey:key];
            } else {
                NSLog(@"RNBatch: Error while tracking event data: Unknown event data.attributes type");
                return;
            }
        }
    }

    [BatchUser trackEvent:name withLabel:label associatedData:batchEventData];
}

RCT_EXPORT_METHOD(userData_trackTransaction:(double)amount data:(NSDictionary*)rawData)
{
    if (rawData && ![rawData isKindOfClass:[NSDictionary class]])
    {
        NSLog(@"RNBatch: trackTransaction data should be an dictionary or nil");
        return;
    }

    [BatchUser trackTransactionWithAmount:amount data:rawData];
}

RCT_EXPORT_METHOD(userData_trackLocation:(NSDictionary*)serializedLocation)
{
    if (![serializedLocation isKindOfClass:[NSDictionary class]] || [serializedLocation count]==0)
    {
        NSLog(@"RNBatch: Empty or null parameters for trackLocation");
        return;
    }

    NSNumber *latitude = serializedLocation[@"latitude"];
    NSNumber *longitude = serializedLocation[@"longitude"];
    NSNumber *date = serializedLocation[@"date"]; // MS
    NSNumber *precision = serializedLocation[@"precision"];

    if (![latitude isKindOfClass:[NSNumber class]])
    {
        NSLog(@"RNBatch: latitude should be a string");
        return;
    }

    if (![longitude isKindOfClass:[NSNumber class]])
    {
        NSLog(@"RNBatch: longitude should be a string");
        return;
    }

    NSTimeInterval ts = 0;

    if (date)
    {
        if ([date isKindOfClass:[NSNumber class]]) {
            ts = [date doubleValue] / 1000.0;
        } else {
            NSLog(@"RNBatch: date should be an object or undefined");
            return;
        }
    }

    NSDate *parsedDate = ts != 0 ? [NSDate dateWithTimeIntervalSince1970:ts] : [NSDate date];

    NSInteger parsedPrecision = 0;
    if (precision)
    {
        if ([precision isKindOfClass:[NSNumber class]]) {
            parsedPrecision = [precision integerValue];
        } else {
            NSLog(@"RNBatch: precision should be an object or undefined");
            return;
        }
    }

    [BatchUser trackLocation:[[CLLocation alloc] initWithCoordinate:CLLocationCoordinate2DMake([latitude doubleValue], [longitude doubleValue])
                                                           altitude:0
                                                 horizontalAccuracy:parsedPrecision
                                                   verticalAccuracy:-1
                                                             course:0
                                                              speed:0
                                                          timestamp:parsedDate]];
}

// Inbox module

- (BatchInboxFetcher*) getFetcherFromOptions:(NSDictionary *) options {
    NSDictionary* userOptions = options[@"user"];

    if (!userOptions) {
        return [BatchInbox fetcher];
    }

    return [BatchInbox fetcherForUserIdentifier:userOptions[@"identifier"] authenticationKey:userOptions[@"authenticationKey"]];
}

RCT_EXPORT_METHOD(inbox_getFetcher:
                  (NSDictionary *) options
                  resolver:
                  (RCTPromiseResolveBlock) resolve
                  rejecter:
                  (RCTPromiseRejectBlock) reject) {

    BatchInboxFetcher* fetcher = [self getFetcherFromOptions:options];

    if (options[@"fetchLimit"]) {
        fetcher.limit = [options[@"fetchLimit"] unsignedIntegerValue];
    }

    if (options[@"maxPageSize"]) {
        fetcher.maxPageSize = [options[@"maxPageSize"] unsignedIntegerValue];
    }

    NSString* fetcherIdentifier = [[NSUUID UUID] UUIDString];
    _batchInboxFetcherMap[fetcherIdentifier] = fetcher;

    resolve(fetcherIdentifier);
}

RCT_EXPORT_METHOD(inbox_fetcher_destroy:
                  (NSString *) fetcherIdentifier
                  resolver:
                  (RCTPromiseResolveBlock) resolve
                  rejecter:
                  (RCTPromiseRejectBlock) reject) {
    [_batchInboxFetcherMap removeObjectForKey:fetcherIdentifier];
    resolve([NSNull null]);
}

RCT_EXPORT_METHOD(inbox_fetcher_hasMore:
                  (NSString *) fetcherIdentifier
                  resolver:
                  (RCTPromiseResolveBlock) resolve
                  rejecter:
                  (RCTPromiseRejectBlock) reject) {
    BatchInboxFetcher* fetcher = _batchInboxFetcherMap[fetcherIdentifier];
    if (!fetcher) {
        reject(@"InboxError", @"FETCHER_NOT_FOUND", nil);
        return;
    }
    resolve(@(!fetcher.endReached));
}

RCT_EXPORT_METHOD(inbox_fetcher_markAllAsRead:
                  (NSString *) fetcherIdentifier
                  resolver:
                  (RCTPromiseResolveBlock) resolve
                  rejecter:
                  (RCTPromiseRejectBlock) reject) {
    BatchInboxFetcher* fetcher = _batchInboxFetcherMap[fetcherIdentifier];
    if (!fetcher) {
        reject(@"InboxError", @"FETCHER_NOT_FOUND", nil);
        return;
    }
    [fetcher markAllNotificationsAsRead];
    resolve([NSNull null]);
}

- (BatchInboxNotificationContent *) findNotificationInList: (NSArray<BatchInboxNotificationContent *> *) allNotifications
                                withNotificationIdentifier: (NSString*) notificationIdentifier {
    NSUInteger notificationIndex = [allNotifications indexOfObjectPassingTest:^BOOL(id currentNotification, NSUInteger idx, BOOL *stop) {
        return ([[(BatchInboxNotificationContent *)currentNotification identifier] isEqualToString:notificationIdentifier]);
    }];

    if (notificationIndex == NSNotFound) {
        return nil;
    }

    return [allNotifications objectAtIndex:notificationIndex];
}

RCT_EXPORT_METHOD(inbox_fetcher_markAsRead:
                  (NSString *) fetcherIdentifier
                  notification:
                  (NSString *) notificationIdentifier
                  resolver:
                  (RCTPromiseResolveBlock) resolve
                  rejecter:
                  (RCTPromiseRejectBlock) reject) {
    BatchInboxFetcher* fetcher = _batchInboxFetcherMap[fetcherIdentifier];
    if (!fetcher) {
        reject(@"InboxError", @"FETCHER_NOT_FOUND", nil);
        return;
    }

    BatchInboxNotificationContent * notification = [self findNotificationInList:[fetcher allFetchedNotifications] withNotificationIdentifier:notificationIdentifier];

    if (!notification) {
        reject(@"InboxError", @"NOTIFICATION_NOT_FOUND", nil);
        return;
    }

    [fetcher markNotificationAsRead:notification];
    resolve([NSNull null]);
}

RCT_EXPORT_METHOD(inbox_fetcher_markAsDeleted:
                  (NSString *) fetcherIdentifier
                  notification:
                  (NSString *) notificationIdentifier
                  resolver:
                  (RCTPromiseResolveBlock) resolve
                  rejecter:
                  (RCTPromiseRejectBlock) reject) {
    BatchInboxFetcher* fetcher = _batchInboxFetcherMap[fetcherIdentifier];
    if (!fetcher) {
        reject(@"InboxError", @"FETCHER_NOT_FOUND", nil);
        return;
    }

    BatchInboxNotificationContent * notification = [self findNotificationInList:[fetcher allFetchedNotifications] withNotificationIdentifier:notificationIdentifier];

    if (!notification) {
        reject(@"InboxError", @"NOTIFICATION_NOT_FOUND", nil);
        return;
    }

    [fetcher markNotificationAsDeleted:notification];
    resolve([NSNull null]);
}


RCT_EXPORT_METHOD(inbox_fetcher_fetchNewNotifications:
                  (NSString *) fetcherIdentifier
                  resolver: (RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)
{
    BatchInboxFetcher* fetcher = _batchInboxFetcherMap[fetcherIdentifier];
    if (!fetcher) {
        reject(@"InboxError", @"FETCHER_NOT_FOUND", nil);
        return;
    }

    [fetcher fetchNewNotifications:^(NSError * _Nullable error, NSArray<BatchInboxNotificationContent *> * _Nullable notifications, BOOL foundNewNotifications, BOOL endReached) {

        if (error) {
            NSString* errorMsg = [NSString stringWithFormat:@"Failed to fetch new notifications %@", [error localizedDescription]];
            reject(@"InboxFetchError", errorMsg, error);
        } else {
            NSMutableArray *formattedNotifications = [NSMutableArray new];
            for (BatchInboxNotificationContent *notification in notifications) {
                [formattedNotifications addObject:[self dictionaryWithNotification:notification]];
            }

            NSMutableDictionary *result = [NSMutableDictionary new];
            result[@"notifications"] = formattedNotifications;
            result[@"endReached"] = @(endReached);
            result[@"foundNewNotifications"] = @(foundNewNotifications);

            resolve(result);
        }

    }];
}

RCT_EXPORT_METHOD(inbox_fetcher_fetchNextPage:
                  (NSString *) fetcherIdentifier
                  resolver: (RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)
{
    BatchInboxFetcher* fetcher = _batchInboxFetcherMap[fetcherIdentifier];
    if (!fetcher) {
        reject(@"InboxError", @"FETCHER_NOT_FOUND", nil);
        return;
    }

    [fetcher fetchNextPage:^(NSError * _Nullable error, NSArray<BatchInboxNotificationContent *> * _Nullable notifications, BOOL endReached) {

        if (error) {
            NSString* errorMsg = [NSString stringWithFormat:@"Failed to fetch new notifications %@", [error localizedDescription]];
            reject(@"InboxFetchError", errorMsg, error);
        } else {
            NSMutableArray *formattedNotifications = [NSMutableArray new];
            for (BatchInboxNotificationContent *notification in notifications) {
                [formattedNotifications addObject:[self dictionaryWithNotification:notification]];
            }

            NSMutableDictionary *result = [NSMutableDictionary new];
            result[@"notifications"] = formattedNotifications;
            result[@"endReached"] = @(endReached);

            resolve(result);
        }

    }];
}

- (NSDictionary*) dictionaryWithNotification:(BatchInboxNotificationContent*)notification
{
    NSNumber *source = [NSNumber numberWithInt:0];
    switch (notification.source) {
        case BatchNotificationSourceCampaign:
            source = [NSNumber numberWithInt:1];
            break;
        case BatchNotificationSourceTransactional:
            source = [NSNumber numberWithInt:2];
            break;
        default:
            break;
    }

    NSString *title = notification.title;

    NSDictionary *output = @{
        @"identifier": notification.identifier,
        @"body": notification.body,
        @"isUnread": @(notification.isUnread),
        @"date": [NSNumber numberWithDouble:notification.date.timeIntervalSince1970 * 1000],
        @"source": source,
        @"payload": notification.payload
    };

    if (title != nil) {
        NSMutableDictionary *mutableOutput = [output mutableCopy];
        mutableOutput[@"title"] = title;
        output = mutableOutput;
    }
    return output;
}

// Messaging module

RCT_EXPORT_METHOD(messaging_setNotDisturbed:(BOOL) active
                  resolver: (RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)
{
    [BatchMessaging setDoNotDisturb:active];
    resolve([NSNull null]);
}

RCT_EXPORT_METHOD(messaging_showPendingMessage:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject) {
    dispatch_async(dispatch_get_main_queue(), ^{
        [BatchMessaging showPendingMessage];
        resolve([NSNull null]);
    });
}

RCT_EXPORT_METHOD(messaging_disableDoNotDisturbAndShowPendingMessage:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject) {
    dispatch_async(dispatch_get_main_queue(), ^{
        [BatchMessaging setDoNotDisturb:false];
        [BatchMessaging showPendingMessage];
        resolve([NSNull null]);
    });
}

RCT_EXPORT_METHOD(messaging_setFontOverride:(nullable NSString*) normalFontName boldFontName:(nullable NSString*) boldFontName italicFontName:(nullable NSString*) italicFontName italicBoldFontName:(nullable NSString*) italicBoldFontName
                  resolver: (RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)
{
    UIFont* normalFont = normalFontName != nil ? [UIFont fontWithName:normalFontName size: 14] : nil;
    UIFont* boldFont = boldFontName != nil ? [UIFont fontWithName:boldFontName size: 14] : nil;
    UIFont* italicFont = italicFontName != nil ? [UIFont fontWithName:italicFontName size: 14] : nil;
    UIFont* italicBoldFont = italicBoldFontName != nil ? [UIFont fontWithName:italicBoldFontName size: 14] : nil;

    [BatchMessaging setFontOverride:normalFont boldFont:boldFont italicFont:italicFont boldItalicFont:italicBoldFont];

    resolve([NSNull null]);
}

@end
