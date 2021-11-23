#if __has_include("RCTBridgeModule.h")
#import "RCTBridgeModule.h"
#else
#import <React/RCTBridgeModule.h>
#endif

#if __has_include("RCTEventEmitter.h")
#import "RCTEventEmitter.h"
#else
#import <React/RCTEventEmitter.h>
#endif

@import Batch;

#define PluginVersion "ReactNative/6.0.2"

@interface RNBatch : RCTEventEmitter <RCTBridgeModule, BatchEventDispatcherDelegate>

+ (void)start;

@property (nonatomic, strong) NSMutableDictionary<NSString *, BatchInboxFetcher *> *batchInboxFetcherMap;

@end
