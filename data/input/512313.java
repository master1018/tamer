public class AgendaByDayAdapter extends BaseAdapter {
    private static final int TYPE_DAY = 0;
    private static final int TYPE_MEETING = 1;
    static final int TYPE_LAST = 2;
    private final Context mContext;
    private final AgendaAdapter mAgendaAdapter;
    private final LayoutInflater mInflater;
    private ArrayList<RowInfo> mRowInfo;
    private int mTodayJulianDay;
    private Time mTmpTime = new Time();
    private Formatter mFormatter;
    private StringBuilder mStringBuilder;
    static class ViewHolder {
        TextView dateView;
    }
    public AgendaByDayAdapter(Context context) {
        mContext = context;
        mAgendaAdapter = new AgendaAdapter(context, R.layout.agenda_item);
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mStringBuilder = new StringBuilder(50);
        mFormatter = new Formatter(mStringBuilder, Locale.getDefault());
    }
    public int getCount() {
        if (mRowInfo != null) {
            return mRowInfo.size();
        }
        return mAgendaAdapter.getCount();
    }
    public Object getItem(int position) {
        if (mRowInfo != null) {
            RowInfo row = mRowInfo.get(position);
            if (row.mType == TYPE_DAY) {
                return row;
            } else {
                return mAgendaAdapter.getItem(row.mData);
            }
        }
        return mAgendaAdapter.getItem(position);
    }
    public long getItemId(int position) {
        if (mRowInfo != null) {
            RowInfo row = mRowInfo.get(position);
            if (row.mType == TYPE_DAY) {
                return -position;
            } else {
                return mAgendaAdapter.getItemId(row.mData);
            }
        }
        return mAgendaAdapter.getItemId(position);
    }
    @Override
    public int getViewTypeCount() {
        return TYPE_LAST;
    }
    @Override
    public int getItemViewType(int position) {
        return mRowInfo != null && mRowInfo.size() > position ?
                mRowInfo.get(position).mType : TYPE_DAY;
    }
    public View getView(int position, View convertView, ViewGroup parent) {
        if ((mRowInfo == null) || (position > mRowInfo.size())) {
            return mAgendaAdapter.getView(position, convertView, parent);
        }
        RowInfo row = mRowInfo.get(position);
        if (row.mType == TYPE_DAY) {
            ViewHolder holder = null;
            View agendaDayView = null;
            if ((convertView != null) && (convertView.getTag() != null)) {
                Object tag = convertView.getTag();
                if (tag instanceof ViewHolder) {
                    agendaDayView = convertView;
                    holder = (ViewHolder) tag;
                }
            }
            if (holder == null) {
                holder = new ViewHolder();
                agendaDayView = mInflater.inflate(R.layout.agenda_day, parent, false);
                holder.dateView = (TextView) agendaDayView.findViewById(R.id.date);
                agendaDayView.setTag(holder);
            }
            Time date = mTmpTime;
            long millis = date.setJulianDay(row.mData);
            int flags = DateUtils.FORMAT_SHOW_YEAR
                    | DateUtils.FORMAT_SHOW_DATE;
            mStringBuilder.setLength(0);
            String dateViewText;
            if (row.mData == mTodayJulianDay) {
                dateViewText = mContext.getString(R.string.agenda_today, DateUtils.formatDateRange(
                        mContext, mFormatter, millis, millis, flags).toString());
            } else {
                flags |= DateUtils.FORMAT_SHOW_WEEKDAY;
                dateViewText = DateUtils.formatDateRange(mContext, mFormatter, millis, millis,
                        flags).toString();
            }
            if (AgendaWindowAdapter.BASICLOG) {
                dateViewText += " P:" + position;
            }
            holder.dateView.setText(dateViewText);
            return agendaDayView;
        } else if (row.mType == TYPE_MEETING) {
            View x = mAgendaAdapter.getView(row.mData, convertView, parent);
            TextView y = ((AgendaAdapter.ViewHolder) x.getTag()).title;
            if (AgendaWindowAdapter.BASICLOG) {
                y.setText(y.getText() + " P:" + position);
            } else {
                y.setText(y.getText());
            }
            return x;
        } else {
            throw new IllegalStateException("Unknown event type:" + row.mType);
        }
    }
    public void clearDayHeaderInfo() {
        mRowInfo = null;
    }
    public void changeCursor(DayAdapterInfo info) {
        calculateDays(info);
        mAgendaAdapter.changeCursor(info.cursor);
    }
    public void calculateDays(DayAdapterInfo dayAdapterInfo) {
        Cursor cursor = dayAdapterInfo.cursor;
        ArrayList<RowInfo> rowInfo = new ArrayList<RowInfo>();
        int prevStartDay = -1;
        Time time = new Time();
        long now = System.currentTimeMillis();
        time.set(now);
        mTodayJulianDay = Time.getJulianDay(now, time.gmtoff);
        LinkedList<MultipleDayInfo> multipleDayList = new LinkedList<MultipleDayInfo>();
        for (int position = 0; cursor.moveToNext(); position++) {
            boolean allDay = cursor.getInt(AgendaWindowAdapter.INDEX_ALL_DAY) != 0;
            int startDay = cursor.getInt(AgendaWindowAdapter.INDEX_START_DAY);
            startDay = Math.max(startDay, dayAdapterInfo.start);
            if (startDay != prevStartDay) {
                if (prevStartDay == -1) {
                    rowInfo.add(new RowInfo(TYPE_DAY, startDay));
                } else {
                    boolean dayHeaderAdded = false;
                    for (int currentDay = prevStartDay + 1; currentDay <= startDay; currentDay++) {
                        dayHeaderAdded = false;
                        Iterator<MultipleDayInfo> iter = multipleDayList.iterator();
                        while (iter.hasNext()) {
                            MultipleDayInfo info = iter.next();
                            if (info.mEndDay < currentDay) {
                                iter.remove();
                                continue;
                            }
                            if (!dayHeaderAdded) {
                                rowInfo.add(new RowInfo(TYPE_DAY, currentDay));
                                dayHeaderAdded = true;
                            }
                            rowInfo.add(new RowInfo(TYPE_MEETING, info.mPosition));
                        }
                    }
                    if (!dayHeaderAdded) {
                        rowInfo.add(new RowInfo(TYPE_DAY, startDay));
                    }
                }
                prevStartDay = startDay;
            }
            rowInfo.add(new RowInfo(TYPE_MEETING, position));
            int endDay = cursor.getInt(AgendaWindowAdapter.INDEX_END_DAY);
            endDay = Math.min(endDay, dayAdapterInfo.end);
            if (endDay > startDay) {
                multipleDayList.add(new MultipleDayInfo(position, endDay));
            }
        }
        if (prevStartDay > 0) {
            for (int currentDay = prevStartDay + 1; currentDay <= dayAdapterInfo.end;
                    currentDay++) {
                boolean dayHeaderAdded = false;
                Iterator<MultipleDayInfo> iter = multipleDayList.iterator();
                while (iter.hasNext()) {
                    MultipleDayInfo info = iter.next();
                    if (info.mEndDay < currentDay) {
                        iter.remove();
                        continue;
                    }
                    if (!dayHeaderAdded) {
                        rowInfo.add(new RowInfo(TYPE_DAY, currentDay));
                        dayHeaderAdded = true;
                    }
                    rowInfo.add(new RowInfo(TYPE_MEETING, info.mPosition));
                }
            }
        }
        mRowInfo = rowInfo;
    }
    private static class RowInfo {
        final int mType;
        final int mData;
        RowInfo(int type, int data) {
            mType = type;
            mData = data;
        }
    }
    private static class MultipleDayInfo {
        final int mPosition;
        final int mEndDay;
        MultipleDayInfo(int position, int endDay) {
            mPosition = position;
            mEndDay = endDay;
        }
    }
    public int findDayPositionNearestTime(Time time) {
        if (mRowInfo == null) {
            return 0;
        }
        long millis = time.toMillis(false );
        int julianDay = Time.getJulianDay(millis, time.gmtoff);
        int minDistance = 1000;  
        int minIndex = 0;
        int len = mRowInfo.size();
        for (int index = 0; index < len; index++) {
            RowInfo row = mRowInfo.get(index);
            if (row.mType == TYPE_DAY) {
                int distance = Math.abs(julianDay - row.mData);
                if (distance == 0) {
                    return index;
                }
                if (distance < minDistance) {
                    minDistance = distance;
                    minIndex = index;
                }
            }
        }
        return minIndex;
    }
    public int findJulianDayFromPosition(int position) {
        if (mRowInfo == null || position < 0) {
            return 0;
        }
        int len = mRowInfo.size();
        if (position >= len) return 0;  
        for (int index = position; index >= 0; index--) {
            RowInfo row = mRowInfo.get(index);
            if (row.mType == TYPE_DAY) {
                return row.mData;
            }
        }
        return 0;
    }
    public int getCursorPosition(int listPos) {
        if (mRowInfo != null && listPos >= 0) {
            RowInfo row = mRowInfo.get(listPos);
            if (row.mType == TYPE_MEETING) {
                return row.mData;
            } else {
                int nextPos = listPos + 1;
                if (nextPos < mRowInfo.size()) {
                    nextPos = getCursorPosition(nextPos);
                    if (nextPos >= 0) {
                        return -nextPos;
                    }
                }
            }
        }
        return Integer.MIN_VALUE;
    }
    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }
    @Override
    public boolean isEnabled(int position) {
        if (mRowInfo != null && position < mRowInfo.size()) {
            RowInfo row = mRowInfo.get(position);
            return row.mType == TYPE_MEETING;
        }
        return true;
    }
}
