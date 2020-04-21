import { NativeModules } from 'react-native';
const RNBatch = NativeModules.RNBatch;

interface IUserSettingsSetAttributeAction {
  type: 'setAttribute';
  key: string;
  value: string | boolean | number | null;
}

interface IUserSettingsRemoveAttributeAction {
  type: 'removeAttribute';
  key: string;
}

interface IUserSettingsClearAttributesAction {
  type: 'clearAttributes';
}

interface IUserSettingsSetDateAttributeAction {
  type: 'setDateAttribute';
  key: string;
  value: number;
}

interface IUserSettingsSetLanguageAction {
  type: 'setLanguage';
  value?: string;
}

interface IUserSettingsSetRegionAction {
  type: 'setRegion';
  value?: string;
}

interface IUserSettingsSetIdentifierAction {
  type: 'setIdentifier';
  value: string | null;
}

interface IUserSettingsAddTagAction {
  type: 'addTag';
  collection: string;
  tag: string;
}

interface IUserSettingsRemoveTagAction {
  type: 'removeTag';
  collection: string;
  tag: string;
}

interface IUserSettingsClearTagCollectionAction {
  type: 'clearTagCollection';
  collection: string;
}

interface IUserSettingsClearTagsAction {
  type: 'clearTags';
}

type IUserSettingsAction =
  | IUserSettingsSetAttributeAction
  | IUserSettingsRemoveAttributeAction
  | IUserSettingsClearAttributesAction
  | IUserSettingsSetDateAttributeAction
  | IUserSettingsSetIdentifierAction
  | IUserSettingsSetLanguageAction
  | IUserSettingsSetRegionAction
  | IUserSettingsAddTagAction
  | IUserSettingsRemoveTagAction
  | IUserSettingsClearTagsAction
  | IUserSettingsClearTagCollectionAction;

type IUserSettingsActions = IUserSettingsAction[];

/**
 * Editor class used to create and save user tags and attributes
 */
export class BatchUserEditor {
  private _settings: IUserSettingsActions;

  public constructor(settings: IUserSettingsActions = []) {
    this._settings = settings;
  }

  private addAction(action: IUserSettingsAction) {
    return new BatchUserEditor([...this._settings, action]);
  }

  public setAttribute(key: string, value: string | boolean | number | null) {
    return this.addAction({
      type: 'setAttribute',
      key,
      value,
    });
  }

  public setDateAttribute(key: string, value: number) {
    return this.addAction({
      type: 'setDateAttribute',
      key,
      value,
    });
  }

  public removeAttribute(key: string) {
    return this.addAction({
      type: 'removeAttribute',
      key,
    });
  }

  public clearAttributes() {
    return this.addAction({
      type: 'clearAttributes',
    });
  }

  public setIdentifier(value: string | null) {
    return this.addAction({
      type: 'setIdentifier',
      value,
    });
  }

  public setLanguage(value?: string) {
    return this.addAction({
      type: 'setLanguage',
      value,
    });
  }

  public setRegion(value?: string) {
    return this.addAction({
      type: 'setRegion',
      value,
    });
  }

  public addTag(collection: string, tag: string) {
    return this.addAction({
      type: 'addTag',
      collection,
      tag,
    });
  }

  public removeTag(collection: string, tag: string) {
    return this.addAction({
      type: 'removeTag',
      collection,
      tag,
    });
  }

  public clearTagCollection(collection: string) {
    return this.addAction({
      type: 'clearTagCollection',
      collection,
    });
  }

  public clearTags() {
    return this.addAction({
      type: 'clearTags',
    });
  }

  public save(): void {
    RNBatch.userData_save(this._settings);
  }
}
