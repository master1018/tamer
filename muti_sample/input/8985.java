class CustomMediaSizeName extends MediaSizeName {
    private static ArrayList customStringTable = new ArrayList();
    private static ArrayList customEnumTable = new ArrayList();
    private String choiceName;
    private MediaSizeName mediaName;
    private CustomMediaSizeName(int x) {
        super(x);
    }
    private synchronized static int nextValue(String name) {
      customStringTable.add(name);
      return (customStringTable.size()-1);
    }
    public CustomMediaSizeName(String name) {
        super(nextValue(name));
        customEnumTable.add(this);
        choiceName = null;
        mediaName = null;
    }
    public CustomMediaSizeName(String name, String choice,
                               float width, float length) {
        super(nextValue(name));
        choiceName = choice;
        customEnumTable.add(this);
        mediaName = null;
        try {
            mediaName = MediaSize.findMedia(width, length,
                                            MediaSize.INCH);
        } catch (IllegalArgumentException iae) {
        }
    }
    private static final long serialVersionUID = 7412807582228043717L;
    public String getChoiceName() {
        return choiceName;
    }
    public MediaSizeName getStandardMedia() {
        return mediaName;
    }
    public static MediaSizeName findMedia(Media[] media, float x, float y,
                                          int units) {
        if (x <= 0.0f || y <= 0.0f || units < 1) {
            throw new IllegalArgumentException("args must be +ve values");
        }
        if (media == null || media.length == 0) {
            throw new IllegalArgumentException("args must have valid array of media");
        }
        int size =0;
        MediaSizeName[] msn = new MediaSizeName[media.length];
        for (int i=0; i<media.length; i++) {
            if (media[i] instanceof MediaSizeName) {
                msn[size++] = (MediaSizeName)media[i];
            }
        }
        if (size == 0) {
            return null;
        }
        int match = 0;
        double ls = x * x + y * y;
        double tmp_ls;
        float []dim;
        float diffx = x;
        float diffy = y;
        for (int i=0; i < size ; i++) {
            MediaSize mediaSize = MediaSize.getMediaSizeForName(msn[i]);
            if (mediaSize == null) {
                continue;
            }
            dim = mediaSize.getSize(units);
            if (x == dim[0] && y == dim[1]) {
                match = i;
                break;
            } else {
                diffx = x - dim[0];
                diffy = y - dim[1];
                tmp_ls = diffx * diffx + diffy * diffy;
                if (tmp_ls < ls) {
                    ls = tmp_ls;
                    match = i;
                }
            }
        }
        return msn[match];
    }
    public  Media[] getSuperEnumTable() {
        return (Media[])super.getEnumValueTable();
    }
    protected String[] getStringTable() {
      String[] nameTable = new String[customStringTable.size()];
      return (String[])customStringTable.toArray(nameTable);
    }
    protected EnumSyntax[] getEnumValueTable() {
      MediaSizeName[] enumTable = new MediaSizeName[customEnumTable.size()];
      return (MediaSizeName[])customEnumTable.toArray(enumTable);
    }
}
