public class CalendarApplication extends Application {
    public Event currentEvent = null;
    static class Screen {
        public int id;
        public Screen next;
        public Screen previous;
        public Screen(int id) {
            this.id = id;
            next = this;
            previous = this;
        }
        public void insert(Screen node) {
            node.next = next;
            node.previous = this;
            next.previous = node;
            next = node;
        }
        public void unlink() {
            next.previous = previous;
            previous.next = next;
        }
    }
    public static final int MONTH_VIEW_ID = 0;
    public static final int WEEK_VIEW_ID = 1;
    public static final int DAY_VIEW_ID = 2;
    public static final int AGENDA_VIEW_ID = 3;
    public static final String[] ACTIVITY_NAMES = new String[] {
        MonthActivity.class.getName(),
        WeekActivity.class.getName(),
        DayActivity.class.getName(),
        AgendaActivity.class.getName(),
    };
    @Override
    public void onCreate() {
        super.onCreate();
        CalendarPreferenceActivity.setDefaultValues(this);
    }
}
