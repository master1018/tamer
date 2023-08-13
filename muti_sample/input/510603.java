public class AgendaAdapter extends ResourceCursorAdapter {
    private String mNoTitleLabel;
    private Resources mResources;
    private int mDeclinedColor;
    private Formatter mFormatter;
    private StringBuilder mStringBuilder;
    static class ViewHolder {
        int overLayColor; 
        TextView title;
        TextView when;
        TextView where;
        int calendarColor; 
    }
    public AgendaAdapter(Context context, int resource) {
        super(context, resource, null);
        mResources = context.getResources();
        mNoTitleLabel = mResources.getString(R.string.no_title_label);
        mDeclinedColor = mResources.getColor(R.drawable.agenda_item_declined);
        mStringBuilder = new StringBuilder(50);
        mFormatter = new Formatter(mStringBuilder, Locale.getDefault());
    }
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder holder = null;
        Object tag = view.getTag();
        if (tag instanceof ViewHolder) {
            holder = (ViewHolder) view.getTag();
        }
        if (holder == null) {
            holder = new ViewHolder();
            view.setTag(holder);
            holder.title = (TextView) view.findViewById(R.id.title);
            holder.when = (TextView) view.findViewById(R.id.when);
            holder.where = (TextView) view.findViewById(R.id.where);
        }
        int selfAttendeeStatus = cursor.getInt(AgendaWindowAdapter.INDEX_SELF_ATTENDEE_STATUS);
        if (selfAttendeeStatus == Attendees.ATTENDEE_STATUS_DECLINED) {
            holder.overLayColor = mDeclinedColor;
        } else {
            holder.overLayColor = 0;
        }
        TextView title = holder.title;
        TextView when = holder.when;
        TextView where = holder.where;
        int color = cursor.getInt(AgendaWindowAdapter.INDEX_COLOR);
        holder.calendarColor = color;
        String titleString = cursor.getString(AgendaWindowAdapter.INDEX_TITLE);
        if (titleString == null || titleString.length() == 0) {
            titleString = mNoTitleLabel;
        }
        title.setText(titleString);
        title.setTextColor(color);
        long begin = cursor.getLong(AgendaWindowAdapter.INDEX_BEGIN);
        long end = cursor.getLong(AgendaWindowAdapter.INDEX_END);
        boolean allDay = cursor.getInt(AgendaWindowAdapter.INDEX_ALL_DAY) != 0;
        int flags;
        String whenString;
        if (allDay) {
            flags = DateUtils.FORMAT_UTC;
        } else {
            flags = DateUtils.FORMAT_SHOW_TIME;
        }
        if (DateFormat.is24HourFormat(context)) {
            flags |= DateUtils.FORMAT_24HOUR;
        }
        mStringBuilder.setLength(0);
        whenString = DateUtils.formatDateRange(context, mFormatter, begin, end, flags).toString();
        when.setText(whenString);
        String rrule = cursor.getString(AgendaWindowAdapter.INDEX_RRULE);
        if (rrule != null) {
            when.setCompoundDrawablesWithIntrinsicBounds(null, null,
                    context.getResources().getDrawable(R.drawable.ic_repeat_dark), null);
            when.setCompoundDrawablePadding(5);
        } else {
            when.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
        }
        String whereString = cursor.getString(AgendaWindowAdapter.INDEX_EVENT_LOCATION);
        if (whereString != null && whereString.length() > 0) {
            where.setVisibility(View.VISIBLE);
            where.setText(whereString);
        } else {
            where.setVisibility(View.GONE);
        }
    }
}
