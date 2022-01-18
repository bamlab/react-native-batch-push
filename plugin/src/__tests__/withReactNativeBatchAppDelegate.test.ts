import { modifyAppDelegate } from '../withReactNativeBatchAppDelegate';
import {
  appDelegateExpectedFixture,
  appDelegateFixture,
} from '../fixtures/appDelegate';

describe(modifyAppDelegate, () => {
  it('should modify the AppDelegate', () => {
    const result = modifyAppDelegate(appDelegateFixture);

    expect(result).toEqual(appDelegateExpectedFixture);
  });
});
