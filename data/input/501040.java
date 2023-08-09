class SdkCommandLine extends CommandLineProcessor {
    public final static String VERB_LIST   = "list";
    public final static String VERB_CREATE = "create";
    public final static String VERB_MOVE   = "move";
    public final static String VERB_DELETE = "delete";
    public final static String VERB_UPDATE = "update";
    public static final String OBJECT_SDK          = "sdk";
    public static final String OBJECT_AVD          = "avd";
    public static final String OBJECT_AVDS         = "avds";
    public static final String OBJECT_TARGET       = "target";
    public static final String OBJECT_TARGETS      = "targets";
    public static final String OBJECT_PROJECT      = "project";
    public static final String OBJECT_TEST_PROJECT = "test-project";
    public static final String OBJECT_LIB_PROJECT  = "lib-project";
    public static final String OBJECT_ADB          = "adb";
    public static final String ARG_ALIAS        = "alias";
    public static final String ARG_ACTIVITY     = "activity";
    public static final String KEY_ACTIVITY     = ARG_ACTIVITY;
    public static final String KEY_PACKAGE      = "package";
    public static final String KEY_MODE         = "mode";
    public static final String KEY_TARGET_ID    = OBJECT_TARGET;
    public static final String KEY_NAME         = "name";
    public static final String KEY_LIBRARY      = "library";
    public static final String KEY_PATH         = "path";
    public static final String KEY_FILTER       = "filter";
    public static final String KEY_SKIN         = "skin";
    public static final String KEY_SDCARD       = "sdcard";
    public static final String KEY_FORCE        = "force";
    public static final String KEY_RENAME       = "rename";
    public static final String KEY_SUBPROJECTS  = "subprojects";
    public static final String KEY_MAIN_PROJECT = "main";
    private final static String[][] ACTIONS = {
            { VERB_LIST, NO_VERB_OBJECT,
                "Lists existing targets or virtual devices." },
            { VERB_LIST, OBJECT_AVD,
                "Lists existing Android Virtual Devices.",
                OBJECT_AVDS },
            { VERB_LIST, OBJECT_TARGET,
                "Lists existing targets.",
                OBJECT_TARGETS },
            { VERB_CREATE, OBJECT_AVD,
                "Creates a new Android Virtual Device." },
            { VERB_MOVE, OBJECT_AVD,
                "Moves or renames an Android Virtual Device." },
            { VERB_DELETE, OBJECT_AVD,
                "Deletes an Android Virtual Device." },
            { VERB_UPDATE, OBJECT_AVD,
                "Updates an Android Virtual Device to match the folders of a new SDK." },
            { VERB_CREATE, OBJECT_PROJECT,
                "Creates a new Android Project." },
            { VERB_UPDATE, OBJECT_PROJECT,
                "Updates an Android Project (must have an AndroidManifest.xml)." },
            { VERB_CREATE, OBJECT_TEST_PROJECT,
                "Creates a new Android Test Project." },
            { VERB_UPDATE, OBJECT_TEST_PROJECT,
                "Updates an Android Test Project (must have an AndroidManifest.xml)." },
            { VERB_CREATE, OBJECT_LIB_PROJECT,
                "Creates a new Android Library Project." },
            { VERB_UPDATE, OBJECT_LIB_PROJECT,
                "Updates an Android Library Project (must have an AndroidManifest.xml)." },
            { VERB_UPDATE, OBJECT_ADB,
                "Updates adb to support the USB devices declared in the SDK add-ons." },
            { VERB_UPDATE, OBJECT_SDK,
                "Updates the SDK by suggesting new platforms to install if available." }
        };
    public SdkCommandLine(ISdkLog logger) {
        super(logger, ACTIONS);
        define(Mode.STRING, false,
                VERB_CREATE, OBJECT_AVD, "p", KEY_PATH,
                "Location path of the directory where the new AVD will be created", null);
        define(Mode.STRING, true,
                VERB_CREATE, OBJECT_AVD, "n", KEY_NAME,
                "Name of the new AVD", null);
        define(Mode.STRING, true,
                VERB_CREATE, OBJECT_AVD, "t", KEY_TARGET_ID,
                "Target id of the new AVD", null);
        define(Mode.STRING, false,
                VERB_CREATE, OBJECT_AVD, "s", KEY_SKIN,
                "Skin of the new AVD", null);
        define(Mode.STRING, false,
                VERB_CREATE, OBJECT_AVD, "c", KEY_SDCARD,
                "Path to a shared SD card image, or size of a new sdcard for the new AVD", null);
        define(Mode.BOOLEAN, false,
                VERB_CREATE, OBJECT_AVD, "f", KEY_FORCE,
                "Force creation (override an existing AVD)", false);
        define(Mode.STRING, true,
                VERB_DELETE, OBJECT_AVD, "n", KEY_NAME,
                "Name of the AVD to delete", null);
        define(Mode.STRING, true,
                VERB_MOVE, OBJECT_AVD, "n", KEY_NAME,
                "Name of the AVD to move or rename", null);
        define(Mode.STRING, false,
                VERB_MOVE, OBJECT_AVD, "r", KEY_RENAME,
                "New name of the AVD to rename", null);
        define(Mode.STRING, false,
                VERB_MOVE, OBJECT_AVD, "p", KEY_PATH,
                "New location path of the directory where to move the AVD", null);
        define(Mode.STRING, true,
                VERB_UPDATE, OBJECT_AVD, "n", KEY_NAME,
                "Name of the AVD to update", null);
        define(Mode.STRING, true,
                VERB_CREATE, OBJECT_PROJECT,
                "p", KEY_PATH,
                "Location path of new project", null);
        define(Mode.STRING, true,
                VERB_CREATE, OBJECT_PROJECT, "t", KEY_TARGET_ID,
                "Target id of the new project", null);
        define(Mode.STRING, true,
                VERB_CREATE, OBJECT_PROJECT, "k", KEY_PACKAGE,
                "Package name", null);
        define(Mode.STRING, true,
                VERB_CREATE, OBJECT_PROJECT, "a", KEY_ACTIVITY,
                "Activity name", null);
        define(Mode.STRING, false,
                VERB_CREATE, OBJECT_PROJECT, "n", KEY_NAME,
                "Project name", null);
        define(Mode.STRING, true,
                VERB_CREATE, OBJECT_TEST_PROJECT,
                "p", KEY_PATH,
                "Location path of new project", null);
        define(Mode.STRING, false,
                VERB_CREATE, OBJECT_TEST_PROJECT, "n", KEY_NAME,
                "Project name", null);
        define(Mode.STRING, true,
                VERB_CREATE, OBJECT_TEST_PROJECT, "m", KEY_MAIN_PROJECT,
                "Location path of the project to test, relative to the new project", null);
        define(Mode.STRING, true,
                VERB_CREATE, OBJECT_LIB_PROJECT,
                "p", KEY_PATH,
                "Location path of new project", null);
        define(Mode.STRING, true,
                VERB_CREATE, OBJECT_LIB_PROJECT, "t", KEY_TARGET_ID,
                "Target id of the new project", null);
        define(Mode.STRING, false,
                VERB_CREATE, OBJECT_LIB_PROJECT, "n", KEY_NAME,
                "Project name", null);
        define(Mode.STRING, true,
                VERB_CREATE, OBJECT_LIB_PROJECT, "k", KEY_PACKAGE,
                "Package name", null);
        define(Mode.STRING, true,
                VERB_UPDATE, OBJECT_PROJECT,
                "p", KEY_PATH,
                "Location path of the project", null);
        define(Mode.STRING, false,
                VERB_UPDATE, OBJECT_PROJECT,
                "t", KEY_TARGET_ID,
                "Target id to set for the project", null);
        define(Mode.STRING, false,
                VERB_UPDATE, OBJECT_PROJECT,
                "n", KEY_NAME,
                "Project name", null);
        define(Mode.BOOLEAN, false,
                VERB_UPDATE, OBJECT_PROJECT,
                "s", KEY_SUBPROJECTS,
                "Also update any projects in sub-folders, such as test projects.", false);
        define(Mode.STRING, false,
                VERB_UPDATE, OBJECT_PROJECT,
                "l", KEY_LIBRARY,
                "Location path of an Android Library to add, relative to the main project", null);
        define(Mode.STRING, true,
                VERB_UPDATE, OBJECT_TEST_PROJECT,
                "p", KEY_PATH,
                "Location path of the project", null);
        define(Mode.STRING, true,
                VERB_UPDATE, OBJECT_TEST_PROJECT,
                "m", KEY_MAIN_PROJECT,
                "Location path of the project to test, relative to the new project", null);
        define(Mode.STRING, true,
                VERB_UPDATE, OBJECT_LIB_PROJECT,
                "p", KEY_PATH,
                "Location path of the project", null);
        define(Mode.STRING, false,
                VERB_UPDATE, OBJECT_LIB_PROJECT,
                "t", KEY_TARGET_ID,
                "Target id to set for the project", null);
        define(Mode.STRING, false,
                VERB_UPDATE, OBJECT_LIB_PROJECT,
                "l", KEY_LIBRARY,
                "Location path of an Android Library to add, relative to the main project", null);
    }
    @Override
    public boolean acceptLackOfVerb() {
        return true;
    }
    public String getParamLocationPath() {
        return (String) getValue(null, null, KEY_PATH);
    }
    public String getParamTargetId() {
        return (String) getValue(null, null, KEY_TARGET_ID);
    }
    public String getParamName() {
        return (String) getValue(null, null, KEY_NAME);
    }
    public String getParamSkin() {
        return (String) getValue(null, null, KEY_SKIN);
    }
    public String getParamSdCard() {
        return (String) getValue(null, null, KEY_SDCARD);
    }
    public boolean getFlagForce() {
        return ((Boolean) getValue(null, null, KEY_FORCE)).booleanValue();
    }
    public String getParamMoveNewName() {
        return (String) getValue(VERB_MOVE, null, KEY_RENAME);
    }
    public String getParamProjectPackage(String directObject) {
        return ((String) getValue(null, directObject, KEY_PACKAGE));
    }
    public String getParamProjectActivity() {
        return ((String) getValue(null, OBJECT_PROJECT, KEY_ACTIVITY));
    }
    public String getParamProjectLibrary(String directObject) {
        return ((String) getValue(null, directObject, KEY_LIBRARY));
    }
    public boolean getParamSubProject() {
        return ((Boolean) getValue(null, OBJECT_PROJECT, KEY_SUBPROJECTS)).booleanValue();
    }
    public String getParamTestProjectMain() {
        return ((String) getValue(null, null, KEY_MAIN_PROJECT));
    }
}
