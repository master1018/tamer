public abstract class ContactsSource {
    public String accountType = null;
    public String resPackageName;
    public String summaryResPackageName;
    public int titleRes;
    public int iconRes;
    public boolean readOnly;
    private ArrayList<DataKind> mKinds = Lists.newArrayList();
    private HashMap<String, DataKind> mMimeKinds = Maps.newHashMap();
    public static final int LEVEL_NONE = 0;
    public static final int LEVEL_SUMMARY = 1;
    public static final int LEVEL_MIMETYPES = 2;
    public static final int LEVEL_CONSTRAINTS = 3;
    private int mInflatedLevel = LEVEL_NONE;
    public synchronized boolean isInflated(int inflateLevel) {
        return mInflatedLevel >= inflateLevel;
    }
    public void setInflatedLevel(int inflateLevel) {
        mInflatedLevel = inflateLevel;
    }
    public synchronized void ensureInflated(Context context, int inflateLevel) {
        if (!isInflated(inflateLevel)) {
            inflate(context, inflateLevel);
        }
    }
    protected abstract void inflate(Context context, int inflateLevel);
    public synchronized void invalidateCache() {
        this.mKinds.clear();
        this.mMimeKinds.clear();
        setInflatedLevel(LEVEL_NONE);
    }
    public CharSequence getDisplayLabel(Context context) {
        if (this.titleRes != -1 && this.summaryResPackageName != null) {
            final PackageManager pm = context.getPackageManager();
            return pm.getText(this.summaryResPackageName, this.titleRes, null);
        } else if (this.titleRes != -1) {
            return context.getText(this.titleRes);
        } else {
            return this.accountType;
        }
    }
    public Drawable getDisplayIcon(Context context) {
        if (this.titleRes != -1 && this.summaryResPackageName != null) {
            final PackageManager pm = context.getPackageManager();
            return pm.getDrawable(this.summaryResPackageName, this.iconRes, null);
        } else if (this.titleRes != -1) {
            return context.getResources().getDrawable(this.iconRes);
        } else {
            return null;
        }
    }
    abstract public int getHeaderColor(Context context);
    abstract public int getSideBarColor(Context context);
    private static Comparator<DataKind> sWeightComparator = new Comparator<DataKind>() {
        public int compare(DataKind object1, DataKind object2) {
            return object1.weight - object2.weight;
        }
    };
    public ArrayList<DataKind> getSortedDataKinds() {
        Collections.sort(mKinds, sWeightComparator);
        return mKinds;
    }
    public DataKind getKindForMimetype(String mimeType) {
        return this.mMimeKinds.get(mimeType);
    }
    public DataKind addKind(DataKind kind) {
        kind.resPackageName = this.resPackageName;
        this.mKinds.add(kind);
        this.mMimeKinds.put(kind.mimeType, kind);
        return kind;
    }
    public static class DataKind {
        public String resPackageName;
        public String mimeType;
        public int titleRes;
        public int iconRes;
        public int iconAltRes;
        public int weight;
        public boolean secondary;
        public boolean editable;
        public boolean isList;
        public StringInflater actionHeader;
        public StringInflater actionAltHeader;
        public StringInflater actionBody;
        public boolean actionBodySocial = false;
        public String typeColumn;
        public int typeOverallMax;
        public List<EditType> typeList;
        public List<EditField> fieldList;
        public ContentValues defaultValues;
        public DataKind() {
        }
        public DataKind(String mimeType, int titleRes, int iconRes, int weight, boolean editable) {
            this.mimeType = mimeType;
            this.titleRes = titleRes;
            this.iconRes = iconRes;
            this.weight = weight;
            this.editable = editable;
            this.isList = true;
            this.typeOverallMax = -1;
        }
    }
    public static class EditType {
        public int rawValue;
        public int labelRes;
        public boolean secondary;
        public int specificMax;
        public String customColumn;
        public EditType(int rawValue, int labelRes) {
            this.rawValue = rawValue;
            this.labelRes = labelRes;
            this.specificMax = -1;
        }
        public EditType setSecondary(boolean secondary) {
            this.secondary = secondary;
            return this;
        }
        public EditType setSpecificMax(int specificMax) {
            this.specificMax = specificMax;
            return this;
        }
        public EditType setCustomColumn(String customColumn) {
            this.customColumn = customColumn;
            return this;
        }
        @Override
        public boolean equals(Object object) {
            if (object instanceof EditType) {
                final EditType other = (EditType)object;
                return other.rawValue == rawValue;
            }
            return false;
        }
        @Override
        public int hashCode() {
            return rawValue;
        }
    }
    public static class EditField {
        public String column;
        public int titleRes;
        public int inputType;
        public int minLines;
        public boolean optional;
        public EditField(String column, int titleRes) {
            this.column = column;
            this.titleRes = titleRes;
        }
        public EditField(String column, int titleRes, int inputType) {
            this(column, titleRes);
            this.inputType = inputType;
        }
        public EditField setOptional(boolean optional) {
            this.optional = optional;
            return this;
        }
    }
    public interface StringInflater {
        public CharSequence inflateUsing(Context context, Cursor cursor);
        public CharSequence inflateUsing(Context context, ContentValues values);
    }
}
