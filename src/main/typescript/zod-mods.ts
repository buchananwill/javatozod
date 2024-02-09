import { z } from 'zod';
import { isValid, parseISO } from 'date-fns';
import { DayOfWeek, REGEX_DATE, REGEX_TIME } from './date-and-time';

const days = DayOfWeek;

export const zDateOnly = z
  .string()
  .regex(REGEX_DATE)
  .refine((arg) => (isValid(parseISO(arg)) ? arg : false));

export const zTimeOnly = z.string().regex(REGEX_TIME);

export const zDayOfWeek = z
  .string()
  .refine((arg) => Object.keys(days).includes(arg));

