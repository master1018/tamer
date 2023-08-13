public class GestureStore {
    public static final int SEQUENCE_INVARIANT = 1;
    public static final int SEQUENCE_SENSITIVE = 2;
    public static final int ORIENTATION_INVARIANT = 1;
    public static final int ORIENTATION_SENSITIVE = 2;
    static final int ORIENTATION_SENSITIVE_4 = 4;
    static final int ORIENTATION_SENSITIVE_8 = 8;
    private static final short FILE_FORMAT_VERSION = 1;
    private static final boolean PROFILE_LOADING_SAVING = false;
    private int mSequenceType = SEQUENCE_SENSITIVE;
    private int mOrientationStyle = ORIENTATION_SENSITIVE;
    private final HashMap<String, ArrayList<Gesture>> mNamedGestures =
            new HashMap<String, ArrayList<Gesture>>();
    private Learner mClassifier;
    private boolean mChanged = false;
    public GestureStore() {
        mClassifier = new InstanceLearner();
    }
    public void setOrientationStyle(int style) {
        mOrientationStyle = style;
    }
    public int getOrientationStyle() {
        return mOrientationStyle;
    }
    public void setSequenceType(int type) {
        mSequenceType = type;
    }
    public int getSequenceType() {
        return mSequenceType;
    }
    public Set<String> getGestureEntries() {
        return mNamedGestures.keySet();
    }
    public ArrayList<Prediction> recognize(Gesture gesture) {
        Instance instance = Instance.createInstance(mSequenceType,
                mOrientationStyle, gesture, null);
        return mClassifier.classify(mSequenceType, mOrientationStyle, instance.vector);
    }
    public void addGesture(String entryName, Gesture gesture) {
        if (entryName == null || entryName.length() == 0) {
            return;
        }
        ArrayList<Gesture> gestures = mNamedGestures.get(entryName);
        if (gestures == null) {
            gestures = new ArrayList<Gesture>();
            mNamedGestures.put(entryName, gestures);
        }
        gestures.add(gesture);
        mClassifier.addInstance(
                Instance.createInstance(mSequenceType, mOrientationStyle, gesture, entryName));
        mChanged = true;
    }
    public void removeGesture(String entryName, Gesture gesture) {
        ArrayList<Gesture> gestures = mNamedGestures.get(entryName);
        if (gestures == null) {
            return;
        }
        gestures.remove(gesture);
        if (gestures.isEmpty()) {
            mNamedGestures.remove(entryName);
        }
        mClassifier.removeInstance(gesture.getID());
        mChanged = true;
    }
    public void removeEntry(String entryName) {
        mNamedGestures.remove(entryName);
        mClassifier.removeInstances(entryName);
        mChanged = true;
    }
    public ArrayList<Gesture> getGestures(String entryName) {
        ArrayList<Gesture> gestures = mNamedGestures.get(entryName);
        if (gestures != null) {
            return new ArrayList<Gesture>(gestures);
        } else {
            return null;
        }
    }
    public boolean hasChanged() {
        return mChanged;
    }
    public void save(OutputStream stream) throws IOException {
        save(stream, false);
    }
    public void save(OutputStream stream, boolean closeStream) throws IOException {
        DataOutputStream out = null;
        try {
            long start;
            if (PROFILE_LOADING_SAVING) {
                start = SystemClock.elapsedRealtime();
            }
            final HashMap<String, ArrayList<Gesture>> maps = mNamedGestures;
            out = new DataOutputStream((stream instanceof BufferedOutputStream) ? stream :
                    new BufferedOutputStream(stream, GestureConstants.IO_BUFFER_SIZE));
            out.writeShort(FILE_FORMAT_VERSION);
            out.writeInt(maps.size());
            for (Map.Entry<String, ArrayList<Gesture>> entry : maps.entrySet()) {
                final String key = entry.getKey();
                final ArrayList<Gesture> examples = entry.getValue();
                final int count = examples.size();
                out.writeUTF(key);
                out.writeInt(count);
                for (int i = 0; i < count; i++) {
                    examples.get(i).serialize(out);
                }
            }
            out.flush();
            if (PROFILE_LOADING_SAVING) {
                long end = SystemClock.elapsedRealtime();
                Log.d(LOG_TAG, "Saving gestures library = " + (end - start) + " ms");
            }
            mChanged = false;
        } finally {
            if (closeStream) GestureUtils.closeStream(out);
        }
    }
    public void load(InputStream stream) throws IOException {
        load(stream, false);
    }
    public void load(InputStream stream, boolean closeStream) throws IOException {
        DataInputStream in = null;
        try {
            in = new DataInputStream((stream instanceof BufferedInputStream) ? stream :
                    new BufferedInputStream(stream, GestureConstants.IO_BUFFER_SIZE));
            long start;
            if (PROFILE_LOADING_SAVING) {
                start = SystemClock.elapsedRealtime();
            }
            final short versionNumber = in.readShort();
            switch (versionNumber) {
                case 1:
                    readFormatV1(in);
                    break;
            }
            if (PROFILE_LOADING_SAVING) {
                long end = SystemClock.elapsedRealtime();
                Log.d(LOG_TAG, "Loading gestures library = " + (end - start) + " ms");
            }
        } finally {
            if (closeStream) GestureUtils.closeStream(in);
        }
    }
    private void readFormatV1(DataInputStream in) throws IOException {
        final Learner classifier = mClassifier;
        final HashMap<String, ArrayList<Gesture>> namedGestures = mNamedGestures;
        namedGestures.clear();
        final int entriesCount = in.readInt();
        for (int i = 0; i < entriesCount; i++) {
            final String name = in.readUTF();
            final int gestureCount = in.readInt();
            final ArrayList<Gesture> gestures = new ArrayList<Gesture>(gestureCount);
            for (int j = 0; j < gestureCount; j++) {
                final Gesture gesture = Gesture.deserialize(in);
                gestures.add(gesture);
                classifier.addInstance(
                        Instance.createInstance(mSequenceType, mOrientationStyle, gesture, name));
            }
            namedGestures.put(name, gestures);
        }
    }
    Learner getLearner() {
        return mClassifier;
    }
}
