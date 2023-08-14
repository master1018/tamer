public class ApplicationInfo extends PackageItemInfo implements Parcelable {
    public String taskAffinity;
    public String permission;
    public String processName;
    public String className;
    public int descriptionRes;    
    public int theme;
    public String manageSpaceActivityName;    
    public String backupAgentName;
    public static final int FLAG_SYSTEM = 1<<0;
    public static final int FLAG_DEBUGGABLE = 1<<1;
    public static final int FLAG_HAS_CODE = 1<<2;
    public static final int FLAG_PERSISTENT = 1<<3;
    public static final int FLAG_FACTORY_TEST = 1<<4;
    public static final int FLAG_ALLOW_TASK_REPARENTING = 1<<5;
    public static final int FLAG_ALLOW_CLEAR_USER_DATA = 1<<6;
    public static final int FLAG_UPDATED_SYSTEM_APP = 1<<7;
    public static final int FLAG_TEST_ONLY = 1<<8;
    public static final int FLAG_SUPPORTS_SMALL_SCREENS = 1<<9;
    public static final int FLAG_SUPPORTS_NORMAL_SCREENS = 1<<10; 
    public static final int FLAG_SUPPORTS_LARGE_SCREENS = 1<<11;
    public static final int FLAG_RESIZEABLE_FOR_SCREENS = 1<<12;
    public static final int FLAG_SUPPORTS_SCREEN_DENSITIES = 1<<13;
    public static final int FLAG_VM_SAFE_MODE = 1<<14;
    public static final int FLAG_ALLOW_BACKUP = 1<<15;
    public static final int FLAG_KILL_AFTER_RESTORE = 1<<16;
    public static final int FLAG_RESTORE_ANY_VERSION = 1<<17;
    public static final int FLAG_EXTERNAL_STORAGE = 1<<18;
    public static final int FLAG_FORWARD_LOCK = 1<<20;
    public static final int FLAG_NATIVE_DEBUGGABLE = 1<<21;
    public int flags = 0;
    public String sourceDir;
    public String publicSourceDir;
    public String[] resourceDirs;
    public String[] sharedLibraryFiles;
    public String dataDir;
    public int uid;
    public int targetSdkVersion;
    public boolean enabled = true;
    public void dump(Printer pw, String prefix) {
        super.dumpFront(pw, prefix);
        if (className != null) {
            pw.println(prefix + "className=" + className);
        }
        if (permission != null) {
            pw.println(prefix + "permission=" + permission);
        }
        pw.println(prefix + "uid=" + uid + " taskAffinity=" + taskAffinity);
        if (theme != 0) {
            pw.println(prefix + "theme=0x" + Integer.toHexString(theme));
        }
        pw.println(prefix + "flags=0x" + Integer.toHexString(flags)
                + " processName=" + processName);
        pw.println(prefix + "sourceDir=" + sourceDir);
        pw.println(prefix + "publicSourceDir=" + publicSourceDir);
        pw.println(prefix + "resourceDirs=" + resourceDirs);
        pw.println(prefix + "dataDir=" + dataDir);
        if (sharedLibraryFiles != null) {
            pw.println(prefix + "sharedLibraryFiles=" + sharedLibraryFiles);
        }
        pw.println(prefix + "enabled=" + enabled + " targetSdkVersion=" + targetSdkVersion);
        if (manageSpaceActivityName != null) {
            pw.println(prefix + "manageSpaceActivityName="+manageSpaceActivityName);
        }
        if (descriptionRes != 0) {
            pw.println(prefix + "description=0x"+Integer.toHexString(descriptionRes));
        }
        super.dumpBack(pw, prefix);
    }
    public static class DisplayNameComparator
            implements Comparator<ApplicationInfo> {
        public DisplayNameComparator(PackageManager pm) {
            mPM = pm;
        }
        public final int compare(ApplicationInfo aa, ApplicationInfo ab) {
            CharSequence  sa = mPM.getApplicationLabel(aa);
            if (sa == null) {
                sa = aa.packageName;
            }
            CharSequence  sb = mPM.getApplicationLabel(ab);
            if (sb == null) {
                sb = ab.packageName;
            }
            return sCollator.compare(sa.toString(), sb.toString());
        }
        private final Collator   sCollator = Collator.getInstance();
        private PackageManager   mPM;
    }
    public ApplicationInfo() {
    }
    public ApplicationInfo(ApplicationInfo orig) {
        super(orig);
        taskAffinity = orig.taskAffinity;
        permission = orig.permission;
        processName = orig.processName;
        className = orig.className;
        theme = orig.theme;
        flags = orig.flags;
        sourceDir = orig.sourceDir;
        publicSourceDir = orig.publicSourceDir;
        resourceDirs = orig.resourceDirs;
        sharedLibraryFiles = orig.sharedLibraryFiles;
        dataDir = orig.dataDir;
        uid = orig.uid;
        targetSdkVersion = orig.targetSdkVersion;
        enabled = orig.enabled;
        manageSpaceActivityName = orig.manageSpaceActivityName;
        descriptionRes = orig.descriptionRes;
    }
    public String toString() {
        return "ApplicationInfo{"
            + Integer.toHexString(System.identityHashCode(this))
            + " " + packageName + "}";
    }
    public int describeContents() {
        return 0;
    }
    public void writeToParcel(Parcel dest, int parcelableFlags) {
        super.writeToParcel(dest, parcelableFlags);
        dest.writeString(taskAffinity);
        dest.writeString(permission);
        dest.writeString(processName);
        dest.writeString(className);
        dest.writeInt(theme);
        dest.writeInt(flags);
        dest.writeString(sourceDir);
        dest.writeString(publicSourceDir);
        dest.writeStringArray(resourceDirs);
        dest.writeStringArray(sharedLibraryFiles);
        dest.writeString(dataDir);
        dest.writeInt(uid);
        dest.writeInt(targetSdkVersion);
        dest.writeInt(enabled ? 1 : 0);
        dest.writeString(manageSpaceActivityName);
        dest.writeString(backupAgentName);
        dest.writeInt(descriptionRes);
    }
    public static final Parcelable.Creator<ApplicationInfo> CREATOR
            = new Parcelable.Creator<ApplicationInfo>() {
        public ApplicationInfo createFromParcel(Parcel source) {
            return new ApplicationInfo(source);
        }
        public ApplicationInfo[] newArray(int size) {
            return new ApplicationInfo[size];
        }
    };
    private ApplicationInfo(Parcel source) {
        super(source);
        taskAffinity = source.readString();
        permission = source.readString();
        processName = source.readString();
        className = source.readString();
        theme = source.readInt();
        flags = source.readInt();
        sourceDir = source.readString();
        publicSourceDir = source.readString();
        resourceDirs = source.readStringArray();
        sharedLibraryFiles = source.readStringArray();
        dataDir = source.readString();
        uid = source.readInt();
        targetSdkVersion = source.readInt();
        enabled = source.readInt() != 0;
        manageSpaceActivityName = source.readString();
        backupAgentName = source.readString();
        descriptionRes = source.readInt();
    }
    public CharSequence loadDescription(PackageManager pm) {
        if (descriptionRes != 0) {
            CharSequence label = pm.getText(packageName, descriptionRes, this);
            if (label != null) {
                return label;
            }
        }
        return null;
    }
    public void disableCompatibilityMode() {
        flags |= (FLAG_SUPPORTS_LARGE_SCREENS | FLAG_SUPPORTS_NORMAL_SCREENS |
                FLAG_SUPPORTS_SMALL_SCREENS | FLAG_RESIZEABLE_FOR_SCREENS |
                FLAG_SUPPORTS_SCREEN_DENSITIES);
    }
    @Override protected Drawable loadDefaultIcon(PackageManager pm) {
        if ((flags & FLAG_EXTERNAL_STORAGE) != 0
                && isPackageUnavailable(pm)) {
            return Resources.getSystem().getDrawable(
                    com.android.internal.R.drawable.sym_app_on_sd_unavailable_icon);
        }
        return pm.getDefaultActivityIcon();
    }
    private boolean isPackageUnavailable(PackageManager pm) {
        try {
            return pm.getPackageInfo(packageName, 0) == null;
        } catch (NameNotFoundException ex) {
            return true;
        }
    }
    @Override protected ApplicationInfo getApplicationInfo() {
        return this;
    }
}
