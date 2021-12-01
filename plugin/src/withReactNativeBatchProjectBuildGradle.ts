import { ConfigPlugin, withProjectBuildGradle } from '@expo/config-plugins';

export const modifyBuildScript = (contents: string): string => {
  let newContents = contents;

  const extRegex = 'ext {';
  const extIndex = newContents.indexOf(extRegex);
  const extStartIndex = extIndex + extRegex.length;

  newContents =
    newContents.substring(0, extStartIndex) +
    `\nbatchSdkVersion = '1.17+'` +
    newContents.substring(extStartIndex);

  return newContents;
};

export const withReactNativeBatchProjectBuildGradle: ConfigPlugin<{} | void> = config => {
  return withProjectBuildGradle(config, async conf => {
    const content = conf.modResults.contents;
    let newContents = modifyBuildScript(content);

    return {
      ...conf,
      modResults: {
        ...conf.modResults,
        contents: newContents,
      },
    };
  });
};
