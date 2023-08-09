public abstract class ListResourceBundle extends ResourceBundle {
    HashMap<String, Object> table;
    public ListResourceBundle() {
        super();
    }
    protected abstract Object[][] getContents();
    @Override
    public Enumeration<String> getKeys() {
        initializeTable();
        if (parent != null) {
            return new Enumeration<String>() {
                Iterator<String> local = table.keySet().iterator();
                Enumeration<String> pEnum = parent.getKeys();
                String nextElement;
                private boolean findNext() {
                    if (nextElement != null) {
                        return true;
                    }
                    while (pEnum.hasMoreElements()) {
                        String next = pEnum.nextElement();
                        if (!table.containsKey(next)) {
                            nextElement = next;
                            return true;
                        }
                    }
                    return false;
                }
                public boolean hasMoreElements() {
                    if (local.hasNext()) {
                        return true;
                    }
                    return findNext();
                }
                public String nextElement() {
                    if (local.hasNext()) {
                        return local.next();
                    }
                    if (findNext()) {
                        String result = nextElement;
                        nextElement = null;
                        return result;
                    }
                    return pEnum.nextElement();
                }
            };
        } else {
            return new Enumeration<String>() {
                Iterator<String> it = table.keySet().iterator();
                public boolean hasMoreElements() {
                    return it.hasNext();
                }
                public String nextElement() {
                    return it.next();
                }
            };
        }
    }
    @Override
    public final Object handleGetObject(String key) {
        initializeTable();
        if (key == null) {
            throw new NullPointerException();
        }
        return table.get(key);
    }
    private synchronized void initializeTable() {
        if (table == null) {
            Object[][] contents = getContents();
            table = new HashMap<String, Object>(contents.length / 3 * 4 + 3);
            for (Object[] content : contents) {
                if (content[0] == null || content[1] == null) {
                    throw new NullPointerException();
                }
                table.put((String) content[0], content[1]);
            }
        }
    }
}
