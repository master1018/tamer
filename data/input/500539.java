public class Level implements Serializable {
    private static final long serialVersionUID = -8176160795706313070L;
    private static final List<Level> levels = new ArrayList<Level>(9);
    public static final Level OFF = new Level("OFF", Integer.MAX_VALUE); 
    public static final Level SEVERE = new Level("SEVERE", 1000); 
    public static final Level WARNING = new Level("WARNING", 900); 
    public static final Level INFO = new Level("INFO", 800); 
    public static final Level CONFIG = new Level("CONFIG", 700); 
    public static final Level FINE = new Level("FINE", 500); 
    public static final Level FINER = new Level("FINER", 400); 
    public static final Level FINEST = new Level("FINEST", 300); 
    public static final Level ALL = new Level("ALL", Integer.MIN_VALUE); 
    public static Level parse(String name) throws IllegalArgumentException {
        if (name == null) {
            throw new NullPointerException(Messages.getString("logging.1C")); 
        }
        boolean isNameAnInt;
        int nameAsInt;
        try {
            nameAsInt = Integer.parseInt(name);
            isNameAnInt = true;
        } catch (NumberFormatException e) {
            nameAsInt = 0;
            isNameAnInt = false;
        }
        synchronized (levels) {
            for (Level level : levels) {
                if (name.equals(level.getName())) {
                    return level;
                }
            }
            if (isNameAnInt) {
                for (Level level : levels) {
                    if (nameAsInt == level.intValue()) {
                        return level;
                    }
                }
            }
        }
        if (!isNameAnInt) {
            throw new IllegalArgumentException(Messages.getString(
                    "logging.1D", name)); 
        }
        return new Level(name, nameAsInt);
    }
    private final String name;
    private final int value;
    private final String resourceBundleName;
    private transient ResourceBundle rb;
    protected Level(String name, int level) {
        this(name, level, null);
    }
    protected Level(String name, int level, String resourceBundleName) {
        if (name == null) {
            throw new NullPointerException(Messages.getString("logging.1C")); 
        }
        this.name = name;
        this.value = level;
        this.resourceBundleName = resourceBundleName;
        if (resourceBundleName != null) {
            try {
                rb = ResourceBundle.getBundle(resourceBundleName,
                        Locale.getDefault(), VMStack.getCallingClassLoader());
            } catch (MissingResourceException e) {
                rb = null;
            }
        }
        synchronized (levels) {
            levels.add(this);
        }
    }
    public String getName() {
        return this.name;
    }
    public String getResourceBundleName() {
        return this.resourceBundleName;
    }
    public final int intValue() {
        return this.value;
    }
    private Object readResolve() {
        synchronized (levels) {
            for (Level level : levels) {
                if (value != level.value) {
                    continue;
                }
                if (!name.equals(level.name)) {
                    continue;
                }
                if (resourceBundleName == level.resourceBundleName) {
                    return level;
                } else if (resourceBundleName != null
                        && resourceBundleName.equals(level.resourceBundleName)) {
                    return level;
                }
            }
            levels.add(this);
            return this;
        }
    }
    private void readObject(ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        in.defaultReadObject();
        if (resourceBundleName != null) {
            try {
                rb = ResourceBundle.getBundle(resourceBundleName);
            } catch (MissingResourceException e) {
                rb = null;
            }
        }
    }
    public String getLocalizedName() {
        if (rb == null) {
            return name;
        }
        try {
            return rb.getString(name);
        } catch (MissingResourceException e) {
            return name;
        }
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Level)) {
            return false;
        }
        return ((Level) o).intValue() == this.value;
    }
    @Override
    public int hashCode() {
        return this.value;
    }
    @Override
    public final String toString() {
        return this.name;
    }
}
