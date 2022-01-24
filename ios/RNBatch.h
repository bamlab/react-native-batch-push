#import <React/RCTBridgeModule.h>
#import <React/RCTEventEmitter.h>

@import Batch;

#define PluginVersion "ReactNative/7.0.2"

@interface RNBatch : RCTEventEmitter <RCTBridgeModule, BatchEventDispatcherDelegate>

+ (void)start;

@property (nonatomic, strong) NSMutableDictionary<NSString *, BatchInboxFetcher *> *batchInboxFetcherMap;

@end
