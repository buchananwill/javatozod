import { addDays, setHours, setMinutes, setSeconds } from 'date-fns/fp';

export const PROJECT_EPOCH_DATE_TIME = new Date('2000-01-01T00:00:00.000Z');

export const REGEX_TIME = /^([01]\d|2[0-3]):([0-5]\d):([0-5]\d)$/;

export const REGEX_DATE =
  /^(?:19|20)\d\d-(0[1-9]|1[0-2])-(0[1-9]|[12][0-9]|3[01])$/;

const parseTen = (stringNumber: string) => parseInt(stringNumber, 10);

export function timeToDateTime(stringTime: string): Date {
  const time = stringTime.split(':').map(parseTen);
  if (time.length == 2) {
    time.push(0);
  }
  let epochalDateTime = new Date(PROJECT_EPOCH_DATE_TIME);
  const hours = setHours(time[0]);
  const minutes = setMinutes(time[1]);
  const seconds = setSeconds(time[2]);
  return hours(minutes(seconds(epochalDateTime)));
}

export function timeToZeroIndexedEpochalDateTime(
  stringTime: string,
  zeroIndexedDayNumber: number
): Date {
  const time = stringTime.split(':').map(parseTen);
  const plusDays = addDays(zeroIndexedDayNumber);
  return plusDays(timeToDateTime(stringTime));
}

export const DayOfWeek = {
  MONDAY: 'Monday',
  TUESDAY: 'Tuesday',
  WEDNESDAY: 'Wednesday',
  THURSDAY: 'Thursday',
  FRIDAY: 'Friday',
  SATURDAY: 'Saturday',
  SUNDAY: 'Sunday'
};
