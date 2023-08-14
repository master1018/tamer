public class ContactsSectionIndexer implements SectionIndexer {
    private final String[] mSections;
    private final int[] mPositions;
    private final int mCount;
    public ContactsSectionIndexer(String[] sections, int[] counts) {
        if (sections == null || counts == null) {
            throw new NullPointerException();
        }
        if (sections.length != counts.length) {
            throw new IllegalArgumentException(
                    "The sections and counts arrays must have the same length");
        }
        this.mSections = sections;
        mPositions = new int[counts.length];
        int position = 0;
        for (int i = 0; i < counts.length; i++) {
            if (mSections[i] == null) {
                mSections[i] = " ";
            } else {
                mSections[i] = mSections[i].trim();
            }
            mPositions[i] = position;
            position += counts[i];
        }
        mCount = position;
    }
    public Object[] getSections() {
        return mSections;
    }
    public int getPositionForSection(int section) {
        if (section < 0 || section >= mSections.length) {
            return -1;
        }
        return mPositions[section];
    }
    public int getSectionForPosition(int position) {
        if (position < 0 || position >= mCount) {
            return -1;
        }
        int index = Arrays.binarySearch(mPositions, position);
        return index >= 0 ? index : -index - 2;
    }
}
