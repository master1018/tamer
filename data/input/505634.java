public class TimeListImpl implements TimeList {
    private final ArrayList<Time> mTimes;
    TimeListImpl(ArrayList<Time> times) {
        mTimes = times;
    }
    public int getLength() {
        return mTimes.size();
    }
    public Time item(int index) {
        Time time = null;
        try {
            time = mTimes.get(index);
        } catch (IndexOutOfBoundsException e) {
        }
        return time;
    }
}
