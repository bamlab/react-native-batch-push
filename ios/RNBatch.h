#if __has_include("React/RCTBridgeModule.h")
  #import <React/RCTBridgeModule.h>
#else
  #import "RCTBridgeModule.h"
#endif

#if __has_include("React/RCTEventEmitter.h")
  #import <React/RCTEventEmitter.h>
#else
  #import "RCTEventEmitter.h"
#endif

@import Batch;

#define PluginVersion "ReactNative/7.0.3"

@interface RNBatch : RCTEventEmitter <RCTBridgeModule, BatchEventDispatcherDelegate>

+ (void)start;

@property (nonatomic, strong) NSMutableDictionary<NSString *, BatchInboxFetcher *> *batchInboxFetcherMap;

@end
