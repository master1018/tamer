final class PublicMapping {
    String baseStr;
    Hashtable tab = new Hashtable();
    public PublicMapping (String baseStr, String mapFile) throws IOException {
        this.baseStr = baseStr;
        load(new FileInputStream(baseStr+mapFile));
    }
    public void load(InputStream in) throws IOException {
        InputStreamReader reader = new InputStreamReader(in);
        BufferedReader data = new BufferedReader(reader);
        for (String ln = data.readLine() ; ln != null ; ln = data.readLine()) {
            if (ln.startsWith("PUBLIC")) {
                int len = ln.length();
                int i = 6;
                while ((i < len) && (ln.charAt(i) != '"')) i++;
                int j = ++i;
                while ((j < len) && (ln.charAt(j) != '"')) j++;
                String id = ln.substring(i, j);
                i = ++j;
                while ((i < len) && ((ln.charAt(i) == ' ') || (ln.charAt(i) == '\t'))) i++;
                j = i + 1;
                while ((j < len) && (ln.charAt(j) != ' ') && (ln.charAt(j) != '\t')) j++;
                String where = ln.substring(i, j);
                put(id, baseStr + where);
            }
        }
        data.close();
    }
    public void put(String id, String str) {
        tab.put(id, str);
        if (str.endsWith(".dtd")) {
            int i = str.lastIndexOf(File.separator);
            if (i >= 0) {
                tab.put(str.substring(i + 1, str.length() - 4), str);
            }
        }
    }
    public String get(String id) {
        return (String) tab.get(id);
    }
}
