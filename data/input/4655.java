public class Win32MediaTray extends MediaTray {
    static final Win32MediaTray ENVELOPE_MANUAL = new Win32MediaTray(0,
                                                      6); 
    static final Win32MediaTray AUTO = new Win32MediaTray(1,
                                                      7); 
    static final Win32MediaTray TRACTOR = new Win32MediaTray(2,
                                                      8); 
    static final Win32MediaTray SMALL_FORMAT = new Win32MediaTray(3,
                                                      9); 
    static final Win32MediaTray LARGE_FORMAT = new Win32MediaTray(4,
                                                      10); 
    static final Win32MediaTray FORMSOURCE = new Win32MediaTray(5,
                                                      15); 
    private static ArrayList winStringTable = new ArrayList();
    private static ArrayList winEnumTable = new ArrayList();
    public int winID;
    private Win32MediaTray(int value, int id) {
        super (value);
        winID = id;
    }
    private synchronized static int nextValue(String name) {
      winStringTable.add(name);
      return (getTraySize()-1);
    }
    protected Win32MediaTray(int id, String name) {
        super (nextValue(name));
        winID = id;
        winEnumTable.add(this);
    }
    private static final String[] myStringTable ={
        "Manual-Envelope",
        "Automatic-Feeder",
        "Tractor-Feeder",
        "Small-Format",
        "Large-Format",
        "Form-Source",
    };
    private static final MediaTray[] myEnumValueTable = {
        ENVELOPE_MANUAL,
        AUTO,
        TRACTOR,
        SMALL_FORMAT,
        LARGE_FORMAT,
        FORMSOURCE,
    };
    protected static int getTraySize() {
      return (myStringTable.length+winStringTable.size());
    }
    protected String[] getStringTable() {
      ArrayList completeList = new ArrayList();
      for (int i=0; i < myStringTable.length; i++) {
        completeList.add(myStringTable[i]);
      }
      completeList.addAll(winStringTable);
      String[] nameTable = new String[completeList.size()];
      return (String[])completeList.toArray(nameTable);
    }
    protected EnumSyntax[] getEnumValueTable() {
      ArrayList completeList = new ArrayList();
      for (int i=0; i < myEnumValueTable.length; i++) {
        completeList.add(myEnumValueTable[i]);
      }
      completeList.addAll(winEnumTable);
      MediaTray[] enumTable = new MediaTray[completeList.size()];
      return (MediaTray[])completeList.toArray(enumTable);
    }
}
