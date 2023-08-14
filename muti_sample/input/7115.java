class CustomMediaTray extends MediaTray {
    private static ArrayList customStringTable = new ArrayList();
    private static ArrayList customEnumTable = new ArrayList();
    private String choiceName;
    private CustomMediaTray(int x) {
        super(x);
    }
    private synchronized static int nextValue(String name) {
      customStringTable.add(name);
      return (customStringTable.size()-1);
    }
    public CustomMediaTray(String name, String choice) {
        super(nextValue(name));
        choiceName = choice;
        customEnumTable.add(this);
    }
    private static final long serialVersionUID = 1019451298193987013L;
    public String getChoiceName() {
        return choiceName;
    }
    public Media[] getSuperEnumTable() {
      return (Media[])super.getEnumValueTable();
    }
    protected String[] getStringTable() {
      String[] nameTable = new String[customStringTable.size()];
      return (String[])customStringTable.toArray(nameTable);
    }
    protected EnumSyntax[] getEnumValueTable() {
      MediaTray[] enumTable = new MediaTray[customEnumTable.size()];
      return (MediaTray[])customEnumTable.toArray(enumTable);
    }
}
