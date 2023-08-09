public class EventRecurrenceFormatter
{
    public static String getRepeatString(Resources r, EventRecurrence recurrence) {
        switch (recurrence.freq) {
            case EventRecurrence.DAILY:
                return r.getString(R.string.daily);
            case EventRecurrence.WEEKLY: {
                if (recurrence.repeatsOnEveryWeekDay()) {
                    return r.getString(R.string.every_weekday);
                } else {
                    String format = r.getString(R.string.weekly);
                    StringBuilder days = new StringBuilder();
                    int count = recurrence.bydayCount - 1;
                    if (count >= 0) {
                        for (int i = 0 ; i < count ; i++) {
                            days.append(dayToString(recurrence.byday[i]));
                            days.append(",");
                        }
                        days.append(dayToString(recurrence.byday[count]));
                        return String.format(format, days.toString());
                    }
                    if (recurrence.startDate == null) {
                        return null;
                    }
                    int day = EventRecurrence.timeDay2Day(recurrence.startDate.weekDay);
                    return String.format(format, dayToString(day));
                }
            }
            case EventRecurrence.MONTHLY: {
                return r.getString(R.string.monthly);
            }
            case EventRecurrence.YEARLY:
                return r.getString(R.string.yearly_plain);
        }
        return null;
    }
    private static String dayToString(int day) {
        return DateUtils.getDayOfWeekString(dayToUtilDay(day), DateUtils.LENGTH_LONG);
    }
    private static int dayToUtilDay(int day) {
        switch (day) {
        case EventRecurrence.SU: return Calendar.SUNDAY;
        case EventRecurrence.MO: return Calendar.MONDAY;
        case EventRecurrence.TU: return Calendar.TUESDAY;
        case EventRecurrence.WE: return Calendar.WEDNESDAY;
        case EventRecurrence.TH: return Calendar.THURSDAY;
        case EventRecurrence.FR: return Calendar.FRIDAY;
        case EventRecurrence.SA: return Calendar.SATURDAY;
        default: throw new IllegalArgumentException("bad day argument: " + day);
        }
    }
}
