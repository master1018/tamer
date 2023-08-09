public abstract class ContactEntryAdapter<E extends ContactEntryAdapter.Entry>
        extends BaseAdapter {
    protected ArrayList<ArrayList<E>> mSections;
    protected LayoutInflater mInflater;
    protected Context mContext;
    protected boolean mSeparators;
    public static class Entry {
        public int type = -1;
        public String label;
        public String data;
        public Uri uri;
        public long id = 0;
        public long contactId;
        public int maxLines = 1;
        public String mimetype;
        protected void writeToParcel(Parcel p) {
            p.writeInt(type);
            p.writeString(label);
            p.writeString(data);
            p.writeParcelable(uri, 0);
            p.writeLong(id);
            p.writeInt(maxLines);
            p.writeString(mimetype);
        }
        protected void readFromParcel(Parcel p) {
            final ClassLoader loader = getClass().getClassLoader();
            type = p.readInt();
            label = p.readString();
            data = p.readString();
            uri = p.readParcelable(loader);
            id = p.readLong();
            maxLines = p.readInt();
            mimetype = p.readString();
        }
    }
    ContactEntryAdapter(Context context, ArrayList<ArrayList<E>> sections, boolean separators) {
        mContext = context;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mSections = sections;
        mSeparators = separators;
    }
    public final void setSections(ArrayList<ArrayList<E>> sections, boolean separators) {
        mSections = sections;
        mSeparators = separators;
        notifyDataSetChanged();
    }
    public final int setSections(ArrayList<ArrayList<E>> sections, E entry) {
        mSections = sections;
        notifyDataSetChanged();
        int numSections = mSections.size();
        int position = 0;
        for (int i = 0; i < numSections; i++) {
            ArrayList<E> section = mSections.get(i);
            int sectionSize = section.size();
            for (int j = 0; j < sectionSize; j++) {
                E e = section.get(j);
                if (e.equals(entry)) {
                    position += j;
                    return position;
                }
            }
            position += sectionSize;
        }
        return -1;
    }
    public final int getCount() {
        return countEntries(mSections, mSeparators);
    }
    @Override
    public final boolean areAllItemsEnabled() {
        return mSeparators == false;
    }
    @Override
    public final boolean isEnabled(int position) {
        if (!mSeparators) {
            return true;
        }
        int numSections = mSections.size();
        for (int i = 0; i < numSections; i++) {
            ArrayList<E> section = mSections.get(i);
            int sectionSize = section.size();
            if (sectionSize == 1) {
                continue;
            }
            if (position == 0) {
                return false;
            }
            position -= sectionSize;
        }
        return true;
    }
    public final Object getItem(int position) {
        return getEntry(mSections, position, mSeparators);
    }
    public final static <T extends Entry> T getEntry(ArrayList<ArrayList<T>> sections,
            int position, boolean separators) {
        int numSections = sections.size();
        for (int i = 0; i < numSections; i++) {
            ArrayList<T> section = sections.get(i);
            int sectionSize = section.size();
            if (separators && sectionSize == 1) {
                continue;
            }
            if (position < section.size()) {
                return section.get(position);
            }
            position -= section.size();
        }
        return null;
    }
    public static <T extends Entry> int countEntries(ArrayList<ArrayList<T>> sections,
            boolean separators) {
        int count = 0;
        int numSections = sections.size();
        for (int i = 0; i < numSections; i++) {
            ArrayList<T> section = sections.get(i);
            int sectionSize = section.size();
            if (separators && sectionSize == 1) {
                continue;
            }
            count += sections.get(i).size();
        }
        return count;
    }
    public final long getItemId(int position) {
        Entry entry = getEntry(mSections, position, mSeparators);
        if (entry != null) {
            return entry.id;
        } else {
            return -1;
        }
    }
    public View getView(int position, View convertView, ViewGroup parent) {
        View v;
        if (convertView == null) {
            v = newView(position, parent);
        } else {
            v = convertView;
        }
        bindView(v, getEntry(mSections, position, mSeparators));
        return v;
    }
    protected abstract View newView(int position, ViewGroup parent);
    protected abstract void bindView(View view, E entry);
}
