@TestTargetClass(Serializable.class) 
public class SerializationStressTest extends junit.framework.TestCase implements
        Serializable {
    static final String FOO = "foo";
    static final String MSG_TEST_FAILED = "Failed to write/read/assertion checking: ";
    protected static final boolean DEBUG = false;
    protected static boolean xload = false;
    protected static boolean xdump = false;
    protected static String xFileName = null;
    protected transient int dumpCount = 0;
    protected transient ObjectInputStream ois;
    protected transient ObjectOutputStream oos;
    protected transient ByteArrayOutputStream bao;
    protected void t_MixPrimitivesAndObjects() throws IOException,
            ClassNotFoundException {
        int i = 7;
        String s1 = "string 1";
        String s2 = "string 2";
        byte[] bytes = { 1, 2, 3 };
        oos.writeInt(i);
        oos.writeObject(s1);
        oos.writeUTF(s2);
        oos.writeObject(bytes);
        oos.close();
        try {
            ois = new ObjectInputStream(loadStream());
            int j = ois.readInt();
            assertTrue("Wrong int :" + j, i == j);
            String l1 = (String) ois.readObject();
            assertTrue("Wrong obj String :" + l1, s1.equals(l1));
            String l2 = (String) ois.readUTF();
            assertTrue("Wrong UTF String :" + l2, s2.equals(l2));
            byte[] bytes2 = (byte[]) ois.readObject();
            assertTrue("Wrong byte[]", Arrays.equals(bytes, bytes2));
        } finally {
            ois.close();
        }
    }
    static final Map TABLE = new Hashtable();
    static final Map MAP = new HashMap();
    static final SortedMap TREE = new TreeMap();
    static final LinkedHashMap LINKEDMAP = new LinkedHashMap();
    static final LinkedHashSet LINKEDSET = new LinkedHashSet();
    static final IdentityHashMap IDENTITYMAP = new IdentityHashMap();
    static final List ALIST = Arrays.asList(new String[] { "a", "list", "of",
            "strings" });
    static final List LIST = new ArrayList(ALIST);
    static final Set SET = new HashSet(Arrays.asList(new String[] { "one",
            "two", "three" }));
    static final Permission PERM = new PropertyPermission("file.encoding",
            "write");
    static final PermissionCollection PERMCOL = PERM.newPermissionCollection();
    static final SortedSet SORTSET = new TreeSet(Arrays.asList(new String[] {
            "one", "two", "three" }));
    static final java.text.DateFormat DATEFORM = java.text.DateFormat
            .getInstance();
    static final java.text.ChoiceFormat CHOICE = new java.text.ChoiceFormat(
            "1#one|2#two|3#three");
    static final java.text.NumberFormat NUMBERFORM = java.text.NumberFormat
            .getInstance();
    static final java.text.MessageFormat MESSAGE = new java.text.MessageFormat(
            "the time: {0,time} and date {0,date}");
    static final LinkedList LINKEDLIST = new LinkedList(Arrays
            .asList(new String[] { "a", "linked", "list", "of", "strings" }));
    static final SimpleTimeZone TIME_ZONE = new SimpleTimeZone(3600000,
            "S-TEST");
    static final Calendar CALENDAR = new GregorianCalendar(TIME_ZONE);
    static Exception INITIALIZE_EXCEPTION = null;
    static {
        try {
            TABLE.put("one", "1");
            TABLE.put("two", "2");
            TABLE.put("three", "3");
            MAP.put("one", "1");
            MAP.put("two", "2");
            MAP.put("three", "3");
            LINKEDMAP.put("one", "1");
            LINKEDMAP.put("two", "2");
            LINKEDMAP.put("three", "3");
            IDENTITYMAP.put("one", "1");
            IDENTITYMAP.put("two", "2");
            IDENTITYMAP.put("three", "3");
            LINKEDSET.add("one");
            LINKEDSET.add("two");
            LINKEDSET.add("three");
            TREE.put("one", "1");
            TREE.put("two", "2");
            TREE.put("three", "3");
            PERMCOL.add(PERM);
            CALENDAR.setTimeZone(new SimpleTimeZone(0, "GMT"));
            CALENDAR.set(1999, Calendar.JUNE, 23, 15, 47, 13);
            CALENDAR.set(Calendar.MILLISECOND, 553);
            DATEFORM.setCalendar(CALENDAR);
            java.text.DateFormatSymbols symbols = new java.text.DateFormatSymbols();
            symbols.setZoneStrings(new String[][] { { "a", "b", "c", "d" },
                    { "e", "f", "g", "h" } });
            ((java.text.SimpleDateFormat) DATEFORM).setDateFormatSymbols(symbols);
            DATEFORM.setNumberFormat(new java.text.DecimalFormat("#.#;'-'#.#"));
            DATEFORM.setTimeZone(TimeZone.getTimeZone("EST"));
            ((java.text.DecimalFormat) NUMBERFORM).applyPattern("#.#;'-'#.#");
            MESSAGE.setFormat(0, DATEFORM);
            MESSAGE.setFormat(1, DATEFORM);
        } catch (Exception e) {
            INITIALIZE_EXCEPTION = e;
        }
    }
    public SerializationStressTest() {
    }
    public SerializationStressTest(String name) {
        super(name);
    }
    public String getDumpName() {
        return getName() + dumpCount;
    }
    protected void dump(Object o) throws IOException, ClassNotFoundException {
        if (dumpCount > 0)
            setUp();
        try {
            oos.writeObject(o);
        } finally {
            oos.close();
        }
    }
    protected Object dumpAndReload(Object o) throws IOException,
            ClassNotFoundException {
        dump(o);
        return reload();
    }
    protected InputStream loadStream() throws IOException {
        if (xload || xdump) {
            return new FileInputStream(xFileName + "-" + getDumpName() + ".ser");
        } else {
            return new ByteArrayInputStream(bao.toByteArray());
        }
    }
    protected Object reload() throws IOException, ClassNotFoundException {
        ois = new ObjectInputStream(loadStream());
        dumpCount++;
        try {
            return ois.readObject();
        } finally {
            ois.close();
        }
    }
    protected void setUp() {
        if (INITIALIZE_EXCEPTION != null) {
            throw new ExceptionInInitializerError(INITIALIZE_EXCEPTION);
        }
        try {
            if (xdump) {
                oos = new ObjectOutputStream(new FileOutputStream(xFileName
                        + "-" + getDumpName() + ".ser"));
            } else {
                oos = new ObjectOutputStream(bao = new ByteArrayOutputStream());
            }
        } catch (Exception e) {
            fail("Exception thrown during setup : " + e.getMessage());
        }
    }
    protected void tearDown() {
        if (oos != null) {
            try {
                oos.close();
            } catch (Exception e) {
            }
        }
    }
}
