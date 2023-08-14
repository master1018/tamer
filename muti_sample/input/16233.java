public abstract class TimeZoneNamesBundle extends OpenListResourceBundle {
    public Object handleGetObject(String key) {
        String[] contents = (String[]) super.handleGetObject(key);
        if (contents == null) {
            return null;
        }
        int clen = contents.length;
        String[] tmpobj = new String[clen+1];
        tmpobj[0] = key;
        for (int i = 0; i < clen; i++) {
            tmpobj[i+1] = contents[i];
        }
        return tmpobj;
    }
    protected Map createMap(int size) {
        return new LinkedHashMap(size);
    }
    protected abstract Object[][] getContents();
}
