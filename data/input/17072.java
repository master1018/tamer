public final class TestSerialization implements PropertyChangeListener, Serializable {
    private static final String NAME = "property";
    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            String file = System.getProperty("java.version") + ".ser";
            serialize(file, create());
        }
        else {
            byte[] array = serialize(create());
            for (String file : args) {
                check(deserialize(file));
                check(array, read(file));
            }
        }
    }
    private static PropertyChangeSupport create() {
        PropertyChangeSupport pcs = new PropertyChangeSupport(TestSerialization.class);
        pcs.addPropertyChangeListener(new TestSerialization(0));
        pcs.addPropertyChangeListener(NAME, new TestSerialization(1));
        return pcs;
    }
    private static void check(PropertyChangeSupport pcs) {
        PropertyChangeListener[] namedListeners = pcs.getPropertyChangeListeners(NAME);
        check(namedListeners, 1);
        check(namedListeners[0], 1);
        PropertyChangeListener[] allListeners = pcs.getPropertyChangeListeners();
        check(allListeners, 2);
        check(allListeners[0], 0);
        check(allListeners[1], 1, NAME);
    }
    private static void check(byte[] a1, byte[] a2) {
        int length = a1.length;
        if (length != a2.length)
            throw new Error("Different file sizes: " + length + " != " + a2.length);
        for (int i = 0; i < length; i++)
            if (a1[i] != a2[i])
                throw new Error("Different bytes at " + i + " position");
    }
    private static void check(PropertyChangeListener[] array, int length) {
        if (length != array.length)
            throw new Error("Unexpected amount of listeners: " + array.length);
    }
    private static void check(PropertyChangeListener listener, int index) {
        if (!(listener instanceof TestSerialization))
            throw new Error("Unexpected listener: " + listener);
        TestSerialization object = (TestSerialization)listener;
        if (index != object.index)
            throw new Error("Unexpected index: " + index + " != " + object.index);
    }
    private static void check(PropertyChangeListener listener, int index, String name) {
        if (!(listener instanceof PropertyChangeListenerProxy))
            throw new Error("Unexpected listener: " + listener);
        PropertyChangeListenerProxy object = (PropertyChangeListenerProxy)listener;
        if (!name.equals(object.getPropertyName()))
            throw new Error("Unexpected name: " + name + " != " + object.getPropertyName());
        check((PropertyChangeListener)object.getListener(), index);
    }
    private static byte[] read(String file) throws Exception {
        FileInputStream stream = null;
        try {
            stream = new FileInputStream(new File(System.getProperty("test.src", "."), file));
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            for (int i = stream.read(); i != -1; i = stream.read())
                out.write(i);
            return out.toByteArray();
        }
        finally {
            if (stream != null)
                stream.close();
        }
    }
    private static PropertyChangeSupport deserialize(String file) throws Exception {
        ObjectInputStream stream = null;
        try {
            stream = new ObjectInputStream(new FileInputStream(new File(System.getProperty("test.src", "."), file)));
            return (PropertyChangeSupport)stream.readObject();
        }
        finally {
            if (stream != null)
                stream.close();
        }
    }
    private static void serialize(String file, PropertyChangeSupport pcs) throws Exception {
        ObjectOutputStream stream = null;
        try {
            stream = new ObjectOutputStream(new FileOutputStream(new File(System.getProperty("test.src", "."), file)));
            stream.writeObject(pcs);
        }
        finally {
            if (stream != null)
                stream.close();
        }
    }
    private static byte[] serialize(PropertyChangeSupport pcs) throws Exception {
        ObjectOutputStream stream = null;
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            stream = new ObjectOutputStream(out);
            stream.writeObject(pcs);
            return out.toByteArray();
        }
        finally {
            if (stream != null)
                stream.close();
        }
    }
    private int index;
    public TestSerialization(int index) {
        this.index = index;
    }
    public int getIndex() {
        return this.index;
    }
    public void propertyChange(PropertyChangeEvent event) {
    }
}
