import { pushDependencies } from '../withReactNativeBatchAppBuildGradle';
import {
  buildGradleFixture,
  buildGradleExpectedFixture,
} from '../fixtures/buildGradle';

describe(pushDependencies, () => {
  it('should push depedencies in the App ProjetGradle file', () => {
    const result = pushDependencies(buildGradleFixture, {
      iOSApiKey: 'FAKE_IOS_API_KEY',
      androidApiKey: 'FAKE_ANDROID_API_KEY',
    });

    expect(result).toEqual(buildGradleExpectedFixture);
  });
});
