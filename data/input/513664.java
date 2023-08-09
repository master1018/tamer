public class GoogleCalendarUriIntentFilter extends Activity {
    private static final int EVENT_INDEX_ID = 0;
    private static final int EVENT_INDEX_START = 1;
    private static final int EVENT_INDEX_END = 2;
    private static final String[] EVENT_PROJECTION = new String[] {
        Events._ID,      
        Events.DTSTART,  
        Events.DTEND,    
    };
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        Intent intent = getIntent();
        if (intent != null) {
            Uri uri = intent.getData();
            if (uri != null) {
                String eid = uri.getQueryParameter("eid");
                if (eid != null) {
                    ContentResolver cr = getContentResolver();
                    String selection = Events.HTML_URI + " LIKE \"%eid=" + eid + "%\"";
                    Cursor eventCursor = managedQuery(Events.CONTENT_URI, EVENT_PROJECTION,
                            selection, null, null);
                    if (eventCursor != null && eventCursor.getCount() > 0) {
                        eventCursor.moveToFirst();
                        int eventId = eventCursor.getInt(EVENT_INDEX_ID);
                        long startMillis = eventCursor.getLong(EVENT_INDEX_START);
                        long endMillis = eventCursor.getLong(EVENT_INDEX_END);
                        int attendeeStatus = ATTENDEE_STATUS_NONE;
                        if ("RESPOND".equals(uri.getQueryParameter("action"))) {
                            try {
                                switch (Integer.parseInt(uri.getQueryParameter("rst"))) {
                                case 1: 
                                    attendeeStatus = ATTENDEE_STATUS_ACCEPTED;
                                    break;
                                case 2: 
                                    attendeeStatus = ATTENDEE_STATUS_DECLINED;
                                    break;
                                case 3: 
                                    attendeeStatus = ATTENDEE_STATUS_TENTATIVE;
                                    break;
                                }
                            } catch (NumberFormatException e) {
                            }
                        }
                        Uri calendarUri = ContentUris.withAppendedId(Events.CONTENT_URI, eventId);
                        intent = new Intent(Intent.ACTION_VIEW, calendarUri);
                        intent.putExtra(EVENT_BEGIN_TIME, startMillis);
                        intent.putExtra(EVENT_END_TIME, endMillis);
                        if (attendeeStatus != ATTENDEE_STATUS_NONE) {
                            intent.putExtra(ATTENDEE_STATUS, attendeeStatus);
                        }
                        startActivity(intent);
                        finish();
                        return;
                    }
                }
            }
            try {
                startNextMatchingActivity(intent);
            } catch (ActivityNotFoundException ex) {
            }
        }
        finish();
    }
}
