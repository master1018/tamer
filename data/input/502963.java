public class HardwareProperties {
    private final static Pattern PATTERN_PROP = Pattern.compile(
    "^([a-zA-Z0-9._-]+)\\s*=\\s*(.*)\\s*$");
    private final static String HW_PROP_NAME = "name";
    private final static String HW_PROP_TYPE = "type";
    private final static String HW_PROP_DEFAULT = "default";
    private final static String HW_PROP_ABSTRACT = "abstract";
    private final static String HW_PROP_DESC = "description";
    private final static String BOOLEAN_YES = "yes";
    private final static String BOOLEAN_NO = "no";
    public final static String[] BOOLEAN_VALUES = new String[] { BOOLEAN_YES, BOOLEAN_NO };
    public final static Pattern DISKSIZE_PATTERN = Pattern.compile("\\d+[MK]B");
    public enum ValueType {
        INTEGER("integer"),
        BOOLEAN("boolean"),
        DISKSIZE("diskSize");
        private String mValue;
        ValueType(String value) {
            mValue = value;
        }
        public String getValue() {
            return mValue;
        }
        public static ValueType getEnum(String value) {
            for (ValueType type : values()) {
                if (type.mValue.equals(value)) {
                    return type;
                }
            }
            return null;
        }
    }
    public static final class HardwareProperty {
        private String mName;
        private ValueType mType;
        private String mDefault;
        private String mAbstract;
        private String mDescription;
        public String getName() {
            return mName;
        }
        public ValueType getType() {
            return mType;
        }
        public String getDefault() {
            return mDefault;
        }
        public String getAbstract() {
            return mAbstract;
        }
        public String getDescription() {
            return mDescription;
        }
    }
    public static Map<String, HardwareProperty> parseHardwareDefinitions(File file, ISdkLog log) {
        try {
            FileInputStream fis = new FileInputStream(file);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
            Map<String, HardwareProperty> map = new HashMap<String, HardwareProperty>();
            String line = null;
            HardwareProperty prop = null;
            while ((line = reader.readLine()) != null) {
                if (line.length() > 0 && line.charAt(0) != '#') {
                    Matcher m = PATTERN_PROP.matcher(line);
                    if (m.matches()) {
                        String valueName = m.group(1);
                        String value = m.group(2);
                        if (HW_PROP_NAME.equals(valueName)) {
                            prop = new HardwareProperty();
                            prop.mName = value;
                            map.put(prop.mName, prop);
                        }
                        if (prop == null) {
                            log.warning("Error parsing '%1$s': missing '%2$s'",
                                    file.getAbsolutePath(), HW_PROP_NAME);
                            return null;
                        }
                        if (HW_PROP_TYPE.equals(valueName)) {
                            prop.mType = ValueType.getEnum(value);
                        } else if (HW_PROP_DEFAULT.equals(valueName)) {
                            prop.mDefault = value;
                        } else if (HW_PROP_ABSTRACT.equals(valueName)) {
                            prop.mAbstract = value;
                        } else if (HW_PROP_DESC.equals(valueName)) {
                            prop.mDescription = value;
                        }
                    } else {
                        log.warning("Error parsing '%1$s': \"%2$s\" is not a valid syntax",
                                file.getAbsolutePath(), line);
                        return null;
                    }
                }
            }
            return map;
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
            log.warning("Error parsing '%1$s': %2$s.", file.getAbsolutePath(),
                        e.getMessage());
        }
        return null;
    }
    public static int getBooleanValueIndex(String value) {
        if (BOOLEAN_YES.equals(value)) {
            return 0;
        } else if (BOOLEAN_NO.equals(value)) {
            return 1;
        }
        return -1;
    }
}
