public class AlertAdapter extends ResourceCursorAdapter {
    public AlertAdapter(Context context, int resource) {
        super(context, resource, null);
    }
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView textView;
        View stripe = view.findViewById(R.id.vertical_stripe);
        int color = cursor.getInt(AlertActivity.INDEX_COLOR);
        stripe.setBackgroundColor(color);
        textView = (TextView) view.findViewById(R.id.event_title);
        textView.setTextColor(color);
        View repeatContainer = view.findViewById(R.id.repeat_icon);
        String rrule = cursor.getString(AlertActivity.INDEX_RRULE);
        if (rrule != null) {
            repeatContainer.setVisibility(View.VISIBLE);
        } else {
            repeatContainer.setVisibility(View.GONE);
        }
        String eventName = cursor.getString(AlertActivity.INDEX_TITLE);
        String location = cursor.getString(AlertActivity.INDEX_EVENT_LOCATION);
        long startMillis = cursor.getLong(AlertActivity.INDEX_BEGIN);
        long endMillis = cursor.getLong(AlertActivity.INDEX_END);
        boolean allDay = cursor.getInt(AlertActivity.INDEX_ALL_DAY) != 0;
        updateView(context, view, eventName, location, startMillis, endMillis, allDay);
    }
    public static void updateView(Context context, View view, String eventName, String location,
            long startMillis, long endMillis, boolean allDay) {
        Resources res = context.getResources();
        TextView textView;
        if (eventName == null || eventName.length() == 0) {
            eventName = res.getString(R.string.no_title_label);
        }
        textView = (TextView) view.findViewById(R.id.event_title);
        textView.setText(eventName);
        String when;
        int flags;
        if (allDay) {
            flags = DateUtils.FORMAT_UTC | DateUtils.FORMAT_SHOW_WEEKDAY |
                    DateUtils.FORMAT_SHOW_DATE;
        } else {
            flags = DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE;
        }
        if (DateFormat.is24HourFormat(context)) {
            flags |= DateUtils.FORMAT_24HOUR;
        }
        when = DateUtils.formatDateRange(context, startMillis, endMillis, flags);
        textView = (TextView) view.findViewById(R.id.when);
        textView.setText(when);
        textView = (TextView) view.findViewById(R.id.where);
        if (location == null || location.length() == 0) {
            textView.setVisibility(View.GONE);
        } else {
            textView.setText(location);
        }
    }
}
