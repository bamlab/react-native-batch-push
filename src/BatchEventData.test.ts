import { BatchEventData, Consts } from './BatchEventData';
import * as Logger from './helpers/Logger';

describe('BatchEventData', () => {
  afterEach(() => {
    jest.clearAllMocks();
  });

  it(`handles less than or equal ${Consts.EventDataMaxTags} tags`, () => {
    const batchEventData = new BatchEventData();
    const spy = jest.spyOn(Logger, 'Log');

    for (let i = 1; i <= Consts.EventDataMaxTags; i++) {
      batchEventData.addTag(`tag ${i}`);
    }

    expect(spy).not.toHaveBeenCalled();
  });
  it(`handles less than or equal ${Consts.EventDataMaxValues} attributes`, () => {
    const batchEventData = new BatchEventData();
    const spy = jest.spyOn(Logger, 'Log');

    for (let i = 1; i <= Consts.EventDataMaxValues; i++) {
      batchEventData.put(`key_${i}`, 'value');
    }

    expect(spy).not.toHaveBeenCalled();
  });
  it(`skips other tags after the first ${Consts.EventDataMaxTags}`, () => {
    const batchEventData = new BatchEventData();
    const spy = jest.spyOn(Logger, 'Log');

    for (let i = 1; i <= Consts.EventDataMaxTags; i++) {
      batchEventData.addTag(`tag ${i}`);
    }

    batchEventData.addTag('too much');

    expect(spy).toHaveBeenCalled();
  });
  it(`skips other attributes after the first ${Consts.EventDataMaxValues}`, () => {
    const batchEventData = new BatchEventData();
    const spy = jest.spyOn(Logger, 'Log');

    for (let i = 1; i <= Consts.EventDataMaxValues; i++) {
      batchEventData.put(`key_${i}`, 'value');
    }

    batchEventData.put('too_much', 'value');

    expect(spy).toHaveBeenCalled();
  });
  it(`handles an url attribute`, () => {
    const batchEventData = new BatchEventData();
    const spy = jest.spyOn(Logger, 'Log');

    batchEventData.putURL('test_url', 'https://batch.com');

    expect(spy).not.toHaveBeenCalled();
  });
  it(`skips a too long url attribute`, () => {
    const batchEventData = new BatchEventData();
    const spy = jest.spyOn(Logger, 'Log');

    batchEventData.putURL(
      'test_url',
      `https://batch.com?${Array(2048)
        .fill(1)
        .join()}`
    );

    expect(spy).toHaveBeenCalled();
  });
});
