@import Batch;

@interface RNBatchOpenedNotificationObserver : NSObject

@property(class, nonatomic, copy) NSString * _Nullable firstDeeplink;

+ (nullable NSString *)getInitialDeeplink;

@end
