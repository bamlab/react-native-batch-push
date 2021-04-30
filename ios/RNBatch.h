#if __has_include("RCTBridgeModule.h")
#import "RCTBridgeModule.h"
#else
#import <React/RCTBridgeModule.h>
#endif

@import Batch;

#define PluginVersion "ReactNative/5.3.0"

@interface RNBatch : NSObject <RCTBridgeModule>
+ (void)start: (BOOL)doNotDisturb;
+ (void)start;
@end
