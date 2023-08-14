public class AgendaListView extends ListView implements OnItemClickListener {
    private static final String TAG = "AgendaListView";
    private static final boolean DEBUG = false;
    private AgendaWindowAdapter mWindowAdapter;
    private AgendaActivity mAgendaActivity;
    private DeleteEventHelper mDeleteEventHelper;
    public AgendaListView(AgendaActivity agendaActivity) {
        super(agendaActivity, null);
        mAgendaActivity = agendaActivity;
        setOnItemClickListener(this);
        setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        setVerticalScrollBarEnabled(false);
        mWindowAdapter = new AgendaWindowAdapter(agendaActivity, this);
        setAdapter(mWindowAdapter);
        mDeleteEventHelper =
            new DeleteEventHelper(agendaActivity, false );
    }
    @Override protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mWindowAdapter.close();
    }
    public void onItemClick(AdapterView<?> a, View v, int position, long id) {
        if (id != -1) {
            EventInfo event = mWindowAdapter.getEventByPosition(position);
            if (event != null) {
                Uri uri = ContentUris.withAppendedId(Events.CONTENT_URI, event.id);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                intent.putExtra(Calendar.EVENT_BEGIN_TIME, event.begin);
                intent.putExtra(Calendar.EVENT_END_TIME, event.end);
                mAgendaActivity.startActivity(intent);
            }
        }
    }
    public void goTo(Time time, boolean forced) {
        mWindowAdapter.refresh(time, forced);
    }
    public void refresh(boolean forced) {
        Time time = new Time();
        long goToTime = getFirstVisibleTime();
        if (goToTime <= 0) {
            goToTime = System.currentTimeMillis();
        }
        time.set(goToTime);
        mWindowAdapter.refresh(time, forced);
    }
    public void deleteSelectedEvent() {
        int position = getSelectedItemPosition();
        EventInfo event = mWindowAdapter.getEventByPosition(position);
        if (event != null) {
            mDeleteEventHelper.delete(event.begin, event.end, event.id, -1);
        }
    }
    @Override
    public int getFirstVisiblePosition() {
       View v = getFirstVisibleView();
       if (v != null) {
           if (DEBUG) {
               Log.v(TAG, "getFirstVisiblePosition: " + AgendaWindowAdapter.getViewTitle(v));
           }
           return getPositionForView(v);
       }
       return -1;
    }
    public View getFirstVisibleView() {
        Rect r = new Rect();
        int childCount = getChildCount();
        for (int i = 0; i < childCount; ++i) {
            View listItem = getChildAt(i);
            listItem.getLocalVisibleRect(r);
            if (r.top >= 0) { 
                return listItem;
            }
        }
        return null;
    }
    public long getSelectedTime() {
        int position = getSelectedItemPosition();
        if (position >= 0) {
            EventInfo event = mWindowAdapter.getEventByPosition(position);
            if (event != null) {
                return event.begin;
            }
        }
        return getFirstVisibleTime();
    }
    public long getFirstVisibleTime() {
        int position = getFirstVisiblePosition();
        if (DEBUG) {
            Log.v(TAG, "getFirstVisiblePosition = " + position);
        }
        EventInfo event = mWindowAdapter.getEventByPosition(position);
        if (event != null) {
            return event.begin;
        }
        return 0;
    }
    public void shiftSelection(int offset) {
        shiftPosition(offset);
        int position = getSelectedItemPosition();
        if (position != INVALID_POSITION) {
            setSelectionFromTop(position + offset, 0);
        }
    }
    private void shiftPosition(int offset) {
        if (DEBUG) {
            Log.v(TAG, "Shifting position "+ offset);
        }
        View firstVisibleItem = getFirstVisibleView();
        if (firstVisibleItem != null) {
            Rect r = new Rect();
            firstVisibleItem.getLocalVisibleRect(r);
            int position = getPositionForView(firstVisibleItem);
            setSelectionFromTop(position + offset, r.top > 0 ? -r.top : r.top);
            if (DEBUG) {
                if (firstVisibleItem.getTag() instanceof AgendaAdapter.ViewHolder) {
                    ViewHolder viewHolder = (AgendaAdapter.ViewHolder)firstVisibleItem.getTag();
                    Log.v(TAG, "Shifting from " + position + " by " + offset + ". Title "
                            + viewHolder.title.getText());
                } else if (firstVisibleItem.getTag() instanceof AgendaByDayAdapter.ViewHolder) {
                    AgendaByDayAdapter.ViewHolder viewHolder =
                        (AgendaByDayAdapter.ViewHolder)firstVisibleItem.getTag();
                    Log.v(TAG, "Shifting from " + position + " by " + offset + ". Date  "
                            + viewHolder.dateView.getText());
                } else if (firstVisibleItem instanceof TextView) {
                    Log.v(TAG, "Shifting: Looking at header here. " + getSelectedItemPosition());
                }
            }
        } else if (getSelectedItemPosition() >= 0) {
            if (DEBUG) {
                Log.v(TAG, "Shifting selection from " + getSelectedItemPosition() + " by " + offset);
            }
            setSelection(getSelectedItemPosition() + offset);
        }
    }
    public void setHideDeclinedEvents(boolean hideDeclined) {
        mWindowAdapter.setHideDeclinedEvents(hideDeclined);
    }
    public void onResume() {
        mWindowAdapter.notifyDataSetChanged();
    }
    public void onPause() {
        mWindowAdapter.notifyDataSetInvalidated();
    }
}
