public class ResourceBundleTest extends RBTestFmwk {
    public static void main(String[] args) throws Exception {
        new ResourceBundleTest().run(args);
    }
    public ResourceBundleTest() {
        makePropertiesFile();
    }
    public void TestResourceBundle() {
        Locale  saveDefault = Locale.getDefault();
        Locale.setDefault(new Locale("fr", "FR"));
        ResourceBundle  bundle = ResourceBundle.getBundle("TestResource");
        if (!bundle.getClass().getName().equals("TestResource_fr"))
            errln("Expected TestResource_fr, got " + bundle.getClass().getName());
        String  test1 = bundle.getString("Time");
        if (!test1.equals("Time keeps on slipping..."))
            errln("TestResource_fr returned wrong value for \"Time\":  got " + test1);
        test1 = bundle.getString("For");
        if (!test1.equals("Four score and seven years ago..."))
            errln("TestResource_fr returned wrong value for \"For\":  got " + test1);
        String[] test2 = bundle.getStringArray("All");
        if (test2.length != 4)
            errln("TestResource_fr returned wrong number of elements for \"All\": got " + test2.length);
        else if (!test2[0].equals("'Twas brillig, and the slithy toves") ||
                 !test2[1].equals("Did gyre and gimble in the wabe.") ||
                 !test2[2].equals("All mimsy were the borogoves,") ||
                 !test2[3].equals("And the mome raths outgrabe."))
            errln("TestResource_fr returned the wrong value for one of the elements in \"All\"");
        Object  test3 = bundle.getObject("Good");
        if (test3 == null || test3.getClass() != Integer.class)
            errln("TestResource_fr returned an object of the wrong class for \"Good\"");
        else if (((Integer)test3).intValue() != 3)
            errln("TestResource_fr returned the wrong value for \"Good\": got " + test3);
        test2 = bundle.getStringArray("Men");
        if (test2.length != 3)
            errln("TestResource_fr returned wrong number of elements for \"Men\": got " + test2.length);
        else if (!test2[0].equals("1") ||
                 !test2[1].equals("2") ||
                 !test2[2].equals("C"))
            errln("TestResource_fr returned the wrong value for one of the elements in \"All\"");
        try {
            test3 = bundle.getObject("Is");
            errln("TestResource_fr returned a value for \"Is\" when it shouldn't: got " + test3);
        }
        catch (MissingResourceException e) {
        }
        String[] keys = { "Now", "Time", "For", "All", "Good", "Men", "Come" };
        checkKeys(bundle.getKeys(),  keys);
        Locale.setDefault(saveDefault);
    }
    public void TestListResourceBundle() {
        ResourceBundle  bundle = ResourceBundle.getBundle("TestResource",
                            new Locale("be", "BY"));
        if (!bundle.getClass().getName().equals("TestResource"))
            errln("Expected TestResource, got " + bundle.getClass().getName());
        doListResourceBundleTest(bundle);
    }
    public void TestEmptyListResourceBundle() {
        ResourceBundle bundle = ResourceBundle.getBundle("TestResource",
                            new Locale("it", "IT"));
        doListResourceBundleTest(bundle);
    }
    private void doListResourceBundleTest(ResourceBundle bundle) {
        String  test1 = bundle.getString("Now");
        if (!test1.equals("Now is the time for all..."))
            errln("TestResource returned wrong value for \"Now\":  got " + test1);
        test1 = bundle.getString("Time");
        if (!test1.equals("Howdy Doody Time!"))
            errln("TestResource returned wrong value for \"Time\":  got " + test1);
        test1 = bundle.getString("Come");
        if (!test1.equals("Come into my parlor..."))
            errln("TestResource returned wrong value for \"Come\":  got " + test1);
        Object  test3 = bundle.getObject("Good");
        if (test3 == null || test3.getClass() != Integer.class)
            errln("TestResource returned an object of the wrong class for \"Good\"");
        else if (((Integer)test3).intValue() != 27)
            errln("TestResource returned the wrong value for \"Good\": got " + test3);
        String[] test2 = bundle.getStringArray("Men");
        if (test2.length != 3)
            errln("TestResource returned wrong number of elements for \"Men\": got " + test2.length);
        else if (!test2[0].equals("1") ||
                 !test2[1].equals("2") ||
                 !test2[2].equals("C"))
            errln("TestResource returned the wrong value for one of the elements in \"All\"");
        try {
            test3 = bundle.getObject("All");
            errln("TestResource_en returned a value for \"All\" when it shouldn't: got " + test3);
        }
        catch (MissingResourceException e) {
        }
        String[] keys = { "Now", "Time", "Good", "Men", "Come" };
        checkKeys(bundle.getKeys(), keys);
    }
    public void TestPropertyResourceBundle() {
        ResourceBundle  bundle = ResourceBundle.getBundle("TestResource",
                            new Locale("es", "ES"));
        String  test = bundle.getString("Now");
        if (!test.equals("How now brown cow"))
            errln("TestResource_es returned wrong value for \"Now\":  got " + test);
        test = bundle.getString("Is");
        if (!test.equals("Is there a dog?"))
            errln("TestResource_es returned wrong value for \"Is\":  got " + test);
        test = bundle.getString("The");
        if (!test.equals("The rain in Spain"))
            errln("TestResource_es returned wrong value for \"The\":  got " + test);
        test = bundle.getString("Time");
        if (!test.equals("Time marches on..."))
            errln("TestResource_es returned wrong value for \"Time\":  got " + test);
        String[] test2 = bundle.getStringArray("Men");
        if (test2.length != 3)
            errln("TestResource returned wrong number of elements for \"Men\": got " + test2.length);
        else if (!test2[0].equals("1") ||
                 !test2[1].equals("2") ||
                 !test2[2].equals("C"))
            errln("TestResource returned the wrong value for one of the elements in \"All\"");
        try {
            test = bundle.getString("All");
            errln("TestResource_es returned a value for \"All\" when it shouldn't: got " + test);
        }
        catch (MissingResourceException e) {
        }
        String[] keys = { "Now", "Is", "The", "Time", "Good", "Men", "Come" };
        checkKeys(bundle.getKeys(), keys);
    }
    public void TestGetLocale() {
        ResourceBundle test = ResourceBundle.getBundle("TestResource",
                        new Locale("fr", "CH", ""));
        Locale locale = test.getLocale();
        if (!(locale.getLanguage().equals("fr")) || !(locale.getCountry().equals("CH")))
            errln("Actual locale for TestResource_fr_CH should have been fr_CH, got " + locale);
        test = ResourceBundle.getBundle("TestResource",
                        new Locale("fr", "BE", ""));
        locale = test.getLocale();
        if (!(locale.getLanguage().equals("fr")) || !(locale.getCountry().equals("")))
            errln("Actual locale for TestResource_fr_BE should have been fr, got " + locale);
        test = ResourceBundle.getBundle("TestResource",
                        new Locale("iw", "IL", ""));
        locale = test.getLocale();
        if (!(locale.getLanguage().equals("")) || !(locale.getCountry().equals("")))
            errln("Actual locale for TestResource_iw_IL should have been the root locale, got "
                            + locale);
    }
    public void TestNonSubclass() {
        Object test1 = ResourceBundle.getBundle("FakeTestResource",
                Locale.US);
        if (!(test1 instanceof ResourceBundle))
            errln("Got back a " + test1.getClass().getName() + " instead of a PropertyResourceBundle when looking for FakeTestResource.");
        ResourceBundle test = (ResourceBundle)test1;
        String message = test.getString("message");
        if (!message.equals("Hello!"))
            errln("Supposedly found FakeTestResource.properties, but it had the wrong contents.");
    }
    public void TestErrorMessage() {
        final String className = "TestResource";
        final String keyName = "DontGetThis";
        ResourceBundle bundle = ResourceBundle.getBundle(className,
                            new Locale("it", "IT"));
        try {
            Object o = bundle.getObject(keyName);
            errln(bundle.getClass().getName()+" returned a value for tag \""+keyName+"\" when it should have thrown an exception.  It returned "+o);
        } catch (MissingResourceException e) {
            String message = e.getMessage();
            boolean found = false;
            if (message.indexOf(className) < 0) {
                    errln("MissingResourceException error message did not contain class name.");
            }
            if (message.indexOf(keyName) < 0) {
                    errln("MissingResourceException error message did not contain resource key name.");
            }
        }
    }
    private void makePropertiesFile() {
        try {
            String classesDir = System.getProperty("test.classes", ".");
            File    file = new File(classesDir, "TestResource_es.properties");
            if (!file.exists()) {
                FileOutputStream stream = new FileOutputStream(file);
                Properties  props = new Properties();
                props.put("Now", "How now brown cow");
                props.put("Is", "Is there a dog?");
                props.put("The", "The rain in Spain");
                props.put("Time", "Time marches on...");
                props.save(stream, "Test property list");
                stream.close();
            }
            file = new File(classesDir, "FakeTestResource.properties");
            if (!file.exists()) {
                FileOutputStream stream = new FileOutputStream(file);
                Properties props = new Properties();
                props.put("message", "Hello!");
                props.save(stream, "Test property list");
                stream.close();
            }
        }
        catch (java.io.IOException e) {
            errln("Got exception: " + e);
        }
    }
    private void checkKeys(Enumeration testKeys, String[] expectedKeys) {
        Hashtable   hash = new Hashtable();
        String      element;
        int         elementCount = 0;
        for (int i=0; i < expectedKeys.length; i++)
            hash.put(expectedKeys[i], expectedKeys[i]);
        while (testKeys.hasMoreElements()) {
            element = (String)testKeys.nextElement();
            elementCount++;
            if (!hash.containsKey(element))
                errln(element + " missing from key list.");
        }
        if (elementCount != expectedKeys.length)
            errln("Wrong number of elements in key list: expected " + expectedKeys.length +
                " got " + elementCount);
    }
}
