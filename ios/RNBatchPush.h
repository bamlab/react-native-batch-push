#if __has_include("RCTBridgeModule.h")
#import "RCTBridgeModule.h"
#else
#import <React/RCTBridgeModule.h>
#endif

@import Batch;

@interface RNBatchPush : NSObject <RCTBridgeModule>

@end
