public class JdbcRowSetResourceBundle implements Serializable {
    private static String fileName;
    private transient PropertyResourceBundle propResBundle;
    private static volatile JdbcRowSetResourceBundle jpResBundle;
    private static final String PROPERTIES = "properties";
    private static final String UNDERSCORE = "_";
    private static final String DOT = ".";
    private static final String SLASH = "/";
    private static final String PATH = "com/sun/rowset/RowSetResourceBundle";
    private JdbcRowSetResourceBundle () throws IOException {
        Locale locale = Locale.getDefault();
         propResBundle = (PropertyResourceBundle) ResourceBundle.getBundle(PATH,
                           locale, Thread.currentThread().getContextClassLoader());
   }
    public static JdbcRowSetResourceBundle getJdbcRowSetResourceBundle()
    throws IOException {
         if(jpResBundle == null){
             synchronized(JdbcRowSetResourceBundle.class) {
                if(jpResBundle == null){
                    jpResBundle = new JdbcRowSetResourceBundle();
                } 
             } 
         } 
         return jpResBundle;
    }
    public Enumeration getKeys() {
       return propResBundle.getKeys();
    }
    public Object handleGetObject(String key) {
       return propResBundle.handleGetObject(key);
    }
    static final long serialVersionUID = 436199386225359954L;
}
