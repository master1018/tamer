public final class Bug6204853 {
    public Bug6204853() {
        String srcDir = System.getProperty("test.src", ".");
        try (FileInputStream fis8859_1 =
                 new FileInputStream(new File(srcDir, "Bug6204853.properties"));
             FileInputStream fisUtf8 =
                 new FileInputStream(new File(srcDir, "Bug6204853_Utf8.properties"));
             InputStreamReader isrUtf8 = new InputStreamReader(fisUtf8, "UTF-8"))
        {
            PropertyResourceBundle bundleUtf8 = new PropertyResourceBundle(isrUtf8);
            PropertyResourceBundle bundle = new PropertyResourceBundle(fis8859_1);
            String[] arrayUtf8 = createKeyValueArray(bundleUtf8);
            String[] array = createKeyValueArray(bundle);
            if (!Arrays.equals(arrayUtf8, array)) {
                throw new RuntimeException("PropertyResourceBundle constructed from a UTF-8 encoded property file is not equal to the one constructed from ISO-8859-1 encoded property file.");
            }
        } catch (FileNotFoundException fnfe) {
            throw new RuntimeException(fnfe);
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }
    private final String[] createKeyValueArray(PropertyResourceBundle b) {
        List<String> keyValueList = new ArrayList<String>();
        StringBuilder sb = new StringBuilder();
        for (String key : b.keySet()) {
            sb.setLength(0);
            sb.append(key);
            sb.append(" = ");
            sb.append(b.getString(key));
            keyValueList.add(sb.toString());
        }
        String[] keyValueArray = keyValueList.toArray(new String[0]);
        Arrays.sort(keyValueArray);
        return keyValueArray;
    }
    public final static void main(String[] args) {
        new Bug6204853();
    }
}
