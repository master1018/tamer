public class ProcessedMessages {
    public static final int DEFAULT_SIZE = 20;
    public class Info {
        private int what;
        private HierarchicalState state;
        private HierarchicalState orgState;
        Info(Message message, HierarchicalState state, HierarchicalState orgState) {
            update(message, state, orgState);
        }
        public void update(Message message, HierarchicalState state, HierarchicalState orgState) {
            this.what = message.what;
            this.state = state;
            this.orgState = orgState;
        }
        public int getWhat() {
            return what;
        }
        public HierarchicalState getState() {
            return state;
        }
        public HierarchicalState getOriginalState() {
            return orgState;
        }
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("what=");
            sb.append(what);
            sb.append(" state=");
            sb.append(cn(state));
            sb.append(" orgState=");
            sb.append(cn(orgState));
            return sb.toString();
        }
        private String cn(Object n) {
            if (n == null) {
                return "null";
            } else {
                String name = n.getClass().getName();
                int lastDollar = name.lastIndexOf('$');
                return name.substring(lastDollar + 1);
            }
        }
    }
    private Vector<Info> mMessages = new Vector<Info>();
    private int mMaxSize = DEFAULT_SIZE;
    private int mOldestIndex = 0;
    private int mCount = 0;
    ProcessedMessages() {
    }
    ProcessedMessages(int maxSize) {
        setSize(maxSize);
    }
    void setSize(int maxSize) {
        mMaxSize = maxSize;
        mCount = 0;
        mMessages.clear();
    }
    int size() {
        return mMessages.size();
    }
    int count() {
        return mCount;
    }
    Info get(int index) {
        int nextIndex = mOldestIndex + index;
        if (nextIndex >= mMaxSize) {
            nextIndex -= mMaxSize;
        }
        if (nextIndex >= size()) {
            return null;
        } else {
            return mMessages.get(nextIndex);
        }
    }
    void add(Message message, HierarchicalState state, HierarchicalState orgState) {
        mCount += 1;
        if (mMessages.size() < mMaxSize) {
            mMessages.add(new Info(message, state, orgState));
        } else {
            Info info = mMessages.get(mOldestIndex);
            mOldestIndex += 1;
            if (mOldestIndex >= mMaxSize) {
                mOldestIndex = 0;
            }
            info.update(message, state, orgState);
        }
    }
}
