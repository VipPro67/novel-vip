import { clsx, type ClassValue } from "clsx";
import { twMerge } from "tailwind-merge";

const RELATIVE_TIME_DIVISIONS: Array<{ amount: number; unit: Intl.RelativeTimeFormatUnit }> = [
  { amount: 60, unit: "second" },
  { amount: 60, unit: "minute" },
  { amount: 24, unit: "hour" },
  { amount: 7, unit: "day" },
  { amount: 4.34524, unit: "week" },
  { amount: 12, unit: "month" },
  { amount: Number.POSITIVE_INFINITY, unit: "year" },
];

const RELATIVE_TIME_FORMATTER =
  typeof Intl !== "undefined" && typeof Intl.RelativeTimeFormat !== "undefined"
    ? new Intl.RelativeTimeFormat(undefined, { numeric: "auto" })
    : null;

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}

export function formatRelativeTime(dateInput: string | number | Date | null | undefined): string {
  if (!dateInput) {
    return "";
  }

  const date = new Date(dateInput);
  if (Number.isNaN(date.getTime())) {
    return "";
  }

  const formatter = RELATIVE_TIME_FORMATTER;
  const diffInSeconds = (date.getTime() - Date.now()) / 1000;

  if (!formatter) {
    return date.toLocaleString();
  }

  let duration = diffInSeconds;
  for (const division of RELATIVE_TIME_DIVISIONS) {
    if (Math.abs(duration) < division.amount) {
      return formatter.format(Math.round(duration), division.unit);
    }
    duration /= division.amount;
  }

  return "";
}
