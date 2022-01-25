import { ConfigPlugin, withAppDelegate } from '@expo/config-plugins';
import { Props } from './withReactNativeBatch';

const DID_FINISH_LAUNCHING_WITH_OPTIONS_DECLARATION =
  '- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions\n{';

const IMPORT_BATCH = '\n\n#import <RNBatchPush/RNBatch.h>\n';
const REGISTER_BATCH =
  '\n[RNBatch start];\n[BatchUNUserNotificationCenterDelegate registerAsDelegate];\n[BatchUNUserNotificationCenterDelegate sharedInstance].showForegroundNotifications = true;\n';

export const modifyAppDelegate = (contents: string) => {
  contents = contents.replace('\n', IMPORT_BATCH);

  const [beforeDeclaration, afterDeclaration] = contents.split(
    DID_FINISH_LAUNCHING_WITH_OPTIONS_DECLARATION
  );

  const newAfterDeclaration = DID_FINISH_LAUNCHING_WITH_OPTIONS_DECLARATION.concat(
    REGISTER_BATCH
  ).concat(afterDeclaration);

  contents = beforeDeclaration.concat(newAfterDeclaration);
  return contents;
};

export const withReactNativeBatchAppDelegate: ConfigPlugin<{} | void> = config => {
  return withAppDelegate(config, config => {
    config.modResults.contents = modifyAppDelegate(config.modResults.contents);
    return config;
  });
};
