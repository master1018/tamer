class SwitchData {
    int minValue, maxValue;
    Label defaultLabel = new Label();
    Hashtable tab = new Hashtable();
    Hashtable whereCaseTab = null;
    public Label get(int n) {
        return (Label)tab.get(new Integer(n));
    }
    public Label get(Integer n) {
        return (Label)tab.get(n);
    }
    public void add(int n, Label lbl) {
        if (tab.size() == 0) {
            minValue = n;
            maxValue = n;
        } else {
            if (n < minValue) {
                minValue = n;
            }
            if (n > maxValue) {
                maxValue = n;
            }
        }
        tab.put(new Integer(n), lbl);
    }
    public Label getDefaultLabel() {
        return defaultLabel;
    }
    public synchronized Enumeration sortedKeys() {
        return new SwitchDataEnumeration(tab);
    }
    public void initTableCase() {
        whereCaseTab = new Hashtable();
    }
    public void addTableCase(int index, long where) {
        if (whereCaseTab != null)
            whereCaseTab.put(new Integer(index), new Long(where));
    }
    public void addTableDefault(long where) {
        if (whereCaseTab != null)
            whereCaseTab.put("default", new Long(where));
    }
    public long whereCase(Object key) {
        Long i = (Long) whereCaseTab.get(key);
        return (i == null) ? 0 : i.longValue();
    }
    public boolean getDefault() {
         return (whereCase("default") != 0);
    }
}
class SwitchDataEnumeration implements Enumeration {
    private Integer table[];
    private int current_index = 0;
    SwitchDataEnumeration(Hashtable tab) {
        table = new Integer[tab.size()];
        int i = 0;
        for (Enumeration e = tab.keys() ; e.hasMoreElements() ; ) {
            table[i++] = (Integer)e.nextElement();
        }
        Arrays.sort(table);
        current_index = 0;
    }
    public boolean hasMoreElements() {
        return current_index < table.length;
    }
    public Object nextElement() {
        return table[current_index++];
    }
}
