import { Log } from './helpers/Logger';
import { isString, isNumber, isBoolean } from './helpers/TypeHelpers';

export const Consts = {
  AttributeKeyRegexp: /^[a-zA-Z0-9_]{1,30}$/,
  EventDataMaxTags: 10,
  EventDataMaxValues: 15,
  EventDataStringMaxLength: 64,
  EventDataURLMaxLength: 2048,
};

export enum TypedEventAttributeType {
  String = 'string',
  Boolean = 'boolean',
  Integer = 'integer',
  Float = 'float',
  URL = 'url',
}

export interface ITypedEventAttribute {
  type: TypedEventAttributeType;
  value: string | boolean | number;
}

export class BatchEventData {
  private _tags: { [key: string]: true }; // tslint:disable-line
  private _attributes: { [key: string]: ITypedEventAttribute }; // tslint:disable-line

  constructor() {
    this._tags = {};
    this._attributes = {};
  }

  public addTag(tag: string): BatchEventData {
    if (typeof tag === 'undefined') {
      Log(false, 'BatchEventData - A tag is required');
      return this;
    }

    if (isString(tag)) {
      if (tag.length === 0 || tag.length > Consts.EventDataStringMaxLength) {
        Log(
          false,
          "BatchEventData - Tags can't be empty or longer than " +
            Consts.EventDataStringMaxLength +
            " characters. Ignoring tag '" +
            tag +
            "'."
        );
        return this;
      }
    } else {
      Log(false, 'BatchEventData - Tag argument must be a string');
      return this;
    }

    if (Object.keys(this._tags).length >= Consts.EventDataMaxTags) {
      Log(
        false,
        'BatchEventData - Event data cannot hold more than ' +
          Consts.EventDataMaxTags +
          " tags. Ignoring tag: '" +
          tag +
          "'"
      );
      return this;
    }

    this._tags[tag.toLowerCase()] = true;

    return this;
  }

  private checkBeforePuttingAttribute(key: string, value: any): void {
    if (!isString(key)) {
      Log(false, 'BatchEventData - Key must be a string');
      throw new Error();
    }

    if (!Consts.AttributeKeyRegexp.test(key || '')) {
      Log(
        false,
        "BatchEventData - Invalid key. Please make sure that the key is made of letters, underscores and numbers only (a-zA-Z0-9_). It also can't be longer than 30 characters. Ignoring attribute '" +
          key +
          "'"
      );
      throw new Error();
    }

    if (typeof value === 'undefined' || value === null) {
      Log(false, 'BatchEventData - Value cannot be undefined or null');
      throw new Error();
    }

    if (
      Object.keys(this._attributes).length >= Consts.EventDataMaxValues &&
      !this._attributes.hasOwnProperty(key)
    ) {
      Log(
        false,
        'BatchEventData - Event data cannot hold more than ' +
          Consts.EventDataMaxValues +
          " attributes. Ignoring attribute: '" +
          key +
          "'"
      );
      throw new Error();
    }
  }

  private prepareAttributeKey(key: string) {
    return key.toLowerCase();
  }

  public putURL(key: string, url: string): BatchEventData {
    key = this.prepareAttributeKey(key);
    try {
      this.checkBeforePuttingAttribute(key, url);
    } catch {
      return this;
    }

    if (url.length > Consts.EventDataURLMaxLength) {
      Log(
        false,
        "BatchEventData - Event data can't be longer than " +
          Consts.EventDataURLMaxLength +
          " characters. Ignoring event data value '" +
          url +
          "'."
      );
      return this;
    }

    this._attributes[key] = {
      type: TypedEventAttributeType.URL,
      value: url,
    };

    return this;
  }

  public put(key: string, value: string | number | boolean): BatchEventData {
    key = this.prepareAttributeKey(key);

    try {
      this.checkBeforePuttingAttribute(key, value);
    } catch {
      return this;
    }

    let typedAttrValue: ITypedEventAttribute | undefined;

    if (isString(value)) {
      typedAttrValue = {
        type: TypedEventAttributeType.String,
        value,
      };
    } else if (isNumber(value)) {
      typedAttrValue = {
        type:
          value % 1 === 0
            ? TypedEventAttributeType.Integer
            : TypedEventAttributeType.Float,
        value,
      };
    } else if (isBoolean(value)) {
      typedAttrValue = {
        type: TypedEventAttributeType.Boolean,
        value,
      };
    } else {
      Log(
        false,
        'BatchEventData - Invalid attribute value type. Must be a string, number or boolean'
      );
      return this;
    }

    if (typedAttrValue) {
      this._attributes[key] = typedAttrValue;
    }

    return this;
  }

  protected _toInternalRepresentation() {
    return {
      attributes: this._attributes,
      tags: Object.keys(this._tags),
    };
  }
}
