final class DOMStringListImpl implements DOMStringList {
    private Vector fStrings;
    DOMStringListImpl() {
        fStrings = new Vector();    
    }
    DOMStringListImpl(Vector params) {
        fStrings = params;    
    }
    DOMStringListImpl(String[] params ) {
        fStrings = new Vector();
        if (params != null) {
            for (int i=0; i < params.length; i++) {
                fStrings.add(params[i]);    
            }
        }
    }
    public String item(int index) {
        try {
            return (String) fStrings.elementAt(index);
        } catch (ArrayIndexOutOfBoundsException e) {
            return null;
        }
    }
    public int getLength() {
        return fStrings.size();
    }
    public boolean contains(String param) {
        return fStrings.contains(param) ;
    }
    public void add(String param) {
        fStrings.add(param);
    }
}
