public class DateSorter {
    private static final String LOGTAG = "webkit";
    public static final int DAY_COUNT = 5;
    private long [] mBins = new long[DAY_COUNT-1];
    private String [] mLabels = new String[DAY_COUNT];
    private static final int NUM_DAYS_AGO = 7;
    public DateSorter(Context context) {
        Resources resources = context.getResources();
        Calendar c = Calendar.getInstance();
        beginningOfDay(c);
        mBins[0] = c.getTimeInMillis(); 
        c.add(Calendar.DAY_OF_YEAR, -1);
        mBins[1] = c.getTimeInMillis();  
        c.add(Calendar.DAY_OF_YEAR, -(NUM_DAYS_AGO - 1));
        mBins[2] = c.getTimeInMillis();  
        c.add(Calendar.DAY_OF_YEAR, NUM_DAYS_AGO); 
        c.add(Calendar.MONTH, -1);
        mBins[3] = c.getTimeInMillis();  
        mLabels[0] = context.getText(com.android.internal.R.string.today).toString();
        mLabels[1] = context.getText(com.android.internal.R.string.yesterday).toString();
        int resId = com.android.internal.R.plurals.last_num_days;
        String format = resources.getQuantityString(resId, NUM_DAYS_AGO);
        mLabels[2] = String.format(format, NUM_DAYS_AGO);
        mLabels[3] = context.getString(com.android.internal.R.string.last_month);
        mLabels[4] = context.getString(com.android.internal.R.string.older);
    }
    public int getIndex(long time) {
        int lastDay = DAY_COUNT - 1;
        for (int i = 0; i < lastDay; i++) {
            if (time > mBins[i]) return i;
        }
        return lastDay;
    }
    public String getLabel(int index) {
        if (index < 0 || index >= DAY_COUNT) return "";
        return mLabels[index];
    }
    public long getBoundary(int index) {
        int lastDay = DAY_COUNT - 1;
        if (index < 0 || index > lastDay) index = 0;
        if (index == lastDay) return Long.MIN_VALUE;
        return mBins[index];
    }
    private void beginningOfDay(Calendar c) {
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
    }
}
