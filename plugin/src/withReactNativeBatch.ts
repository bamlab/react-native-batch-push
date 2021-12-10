import { ConfigPlugin, createRunOncePlugin } from '@expo/config-plugins';
import {
  withClassPath,
  withApplyPlugin,
  withGoogleServicesFile,
} from '@expo/config-plugins/build/android/GoogleServices';
import { withReactNativeBatchMainActivity } from './withReactNativeBatchMainActivity';
import { withReactNativeBatchAppBuildGradle } from './withReactNativeBatchAppBuildGradle';
import { withReactNativeBatchProjectBuildGradle } from './withReactNativeBatchProjectBuildGradle';

export type Props = { apiKey: string };
/**
 * Apply react-native-batch configuration for Expo SDK 42 projects.
 */
const withReactNativeBatch: ConfigPlugin<Props | void> = (config, props) => {
  const _props = props || { apiKey: '' };

  let newConfig = withGoogleServicesFile(config);
  newConfig = withClassPath(newConfig);
  newConfig = withApplyPlugin(newConfig);
  newConfig = withReactNativeBatchAppBuildGradle(newConfig, _props);
  newConfig = withReactNativeBatchMainActivity(newConfig);
  newConfig = withReactNativeBatchProjectBuildGradle(newConfig);
  // Return the modified config.
  return newConfig;
};

const pkg = {
  // Prevent this plugin from being run more than once.
  // This pattern enables users to safely migrate off of this
  // out-of-tree `@config-plugins/react-native-batch` to a future
  // upstream plugin in `react-native-batch`
  name: '@bam.tech/react-native-batch',
  // Indicates that this plugin is dangerously linked to a module,
  // and might not work with the latest version of that module.
  version: 'UNVERSIONED',
};

export default createRunOncePlugin(withReactNativeBatch, pkg.name, pkg.version);
