public class TestResource_fr extends ResourceBundle {
    public TestResource_fr() {
    }
    public Object handleGetObject(String key) throws MissingResourceException {
        if (key.equals("Time"))
            return "Time keeps on slipping...";
        else if (key.equals("For"))
            return "Four score and seven years ago...";
        else if (key.equals("All")) {
            String[] values = {
                "'Twas brillig, and the slithy toves",
                "Did gyre and gimble in the wabe.",
                "All mimsy were the borogoves,",
                "And the mome raths outgrabe."
            };
            return values;
        }
        else if (key.equals("Good"))
            return new Integer(3);
        else
            return null;
    }
    public Enumeration getKeys() {
        Hashtable keys = new Hashtable();
        keys.put("Time", "Time");
        keys.put("For", "For");
        keys.put("All", "All");
        keys.put("Good", "Good");
        Enumeration parentKeys = parent.getKeys();
        while (parentKeys.hasMoreElements()) {
            Object  elt = parentKeys.nextElement();
            keys.put(elt, elt);
        }
        return keys.elements();
    }
}
