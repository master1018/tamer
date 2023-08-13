public class WebkitTest extends AndroidTestCase {
    private static final String LOGTAG = WebkitTest.class.getName();
    @MediumTest
    public void testDateSorter() throws Exception {
        DateSorter dateSorter = new DateSorter(mContext);
        Date date = new Date();
        for (int i = 0; i < DateSorter.DAY_COUNT; i++) {
            Log.i(LOGTAG, "Boundary " + i + " " + dateSorter.getBoundary(i));
            Log.i(LOGTAG, "Label " + i + " " + dateSorter.getLabel(i));
        }
        Calendar c = Calendar.getInstance();
        long time = c.getTimeInMillis();
        int index;
        Log.i(LOGTAG, "now: " + dateSorter.getIndex(time));
        for (int i = 0; i < 20; i++) {
            time -= 8 * 60 * 60 * 1000; 
            date.setTime(time);
            c.setTime(date);
            index = dateSorter.getIndex(time);
            Log.i(LOGTAG, "time: " + DateFormat.format("yyyy/MM/dd kk:mm:ss", c).toString() +
                    " " + index + " " + dateSorter.getLabel(index));
        }
    }
}
