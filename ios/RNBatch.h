#if __has_include("RCTBridgeModule.h")
#import "RCTBridgeModule.h"
#else
#import <React/RCTBridgeModule.h>
#endif

@import Batch;

#define PluginVersion "ReactNative/6.0.0-rc.1"

@interface RNBatch : NSObject <RCTBridgeModule>
+ (void)start: (BOOL)doNotDisturb;
+ (void)start;
@end
