# import "RNBatchOpenedNotificationObserver.h"

@implementation RNBatchOpenedNotificationObserver

static dispatch_once_t onceToken;

static NSString *_firstDeeplink = nil;

+ (NSString *)firstDeeplink {
    return _firstDeeplink;
}

+ (void)setFirstDeeplink:(NSString *)newFirstDeeplink {
    if (_firstDeeplink == nil) {
        _firstDeeplink = [newFirstDeeplink copy];
    }
}

+ (nullable NSString *)getInitialDeeplink {
    if (_firstDeeplink != nil) {
        NSString *initialDeeplink = [_firstDeeplink copy];
        _firstDeeplink = nil;
        return initialDeeplink;
    }

    return nil;
}

+ (void)load {
    [[NSNotificationCenter defaultCenter] addObserver:[RNBatchOpenedNotificationObserver class]
                                             selector:@selector(pushOpenedNotification:)
                                                 name:BatchPushOpenedNotification object:nil];
}

+ (void)pushOpenedNotification:(NSNotification *)notification
{
    if (notification.userInfo == nil) {
        return;
    }

    id payload = notification.userInfo[BatchPushOpenedNotificationPayloadKey];
    if (![payload isKindOfClass:NSDictionary.class]) {
        return;
    }

    NSString *deeplink = [BatchPush deeplinkFromUserInfo:payload];
    if (deeplink == nil) {
        return;
    }

    dispatch_once(&onceToken, ^{
        [self setFirstDeeplink: deeplink];
    });
}

@end
