import { ConfigPlugin, withInfoPlist, InfoPlist } from '@expo/config-plugins';
import { Props } from './withReactNativeBatch';

export const modifyInfoPlist = (
  infoPlist: InfoPlist,
  props: Props
): InfoPlist => {
  infoPlist.BatchAPIKey = props.iOSApiKey;
  return infoPlist;
};

export const withReactNativeBatchInfoPlist: ConfigPlugin<Props> = (
  config,
  props
) => {
  return withInfoPlist(config, config => {
    config.modResults = modifyInfoPlist(config.modResults, props);
    return config;
  });
};
