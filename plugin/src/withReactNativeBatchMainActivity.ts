import { ConfigPlugin, withMainActivity } from '@expo/config-plugins';

export const modifyMainActivity = (content: string): string => {
  let newContent = content;

  if (!newContent.includes('import android.content.Intent;')) {
    newContent = content.replace(
      'import com.facebook.react.ReactActivity;',
      `import com.facebook.react.ReactActivity;
import android.content.Intent;
import com.batch.android.Batch;`
    );
  } else {
    newContent = content.replace(
      'import com.facebook.react.ReactActivity;',
      `import com.facebook.react.ReactActivity;
      import com.batch.android.Batch;`
    );
  }

  if (!newContent.includes('onNewIntent(')) {
    let lastBracketIndex = newContent.lastIndexOf('}');

    const start = newContent.substring(0, lastBracketIndex);
    const end = newContent.substring(lastBracketIndex);

    newContent =
      start +
      `\n  @Override
  public void onNewIntent(Intent intent)
  {
      Batch.onNewIntent(this, intent);
      super.onNewIntent(intent);
  }\n\n` +
      end;
  } else {
    newContent = newContent.replace(
      'super.onNewIntent(intent);',
      `Batch.onNewIntent(this, intent);
    super.onNewIntent(intent);`
    );
  }

  return newContent;
};

export const withReactNativeBatchMainActivity: ConfigPlugin<{} | void> = config => {
  const newConfig = withMainActivity(config, config => {
    const content = config.modResults.contents;
    const newContents = modifyMainActivity(content);

    return {
      ...config,
      modResults: {
        ...config.modResults,
        contents: newContents,
      },
    };
  });

  return newConfig;
};
