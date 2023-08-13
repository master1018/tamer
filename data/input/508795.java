public class AndroidLaunchConfiguration {
    public int mLaunchAction = LaunchConfigDelegate.DEFAULT_LAUNCH_ACTION;
    public enum TargetMode {
        AUTO(true),
        MANUAL(false);
        private boolean mValue;
        TargetMode(boolean value) {
            mValue = value;
        }
        public boolean getValue() {
            return mValue;
        }
        public static TargetMode getMode(boolean value) {
            for (TargetMode mode : values()) {
                if (mode.mValue == value) {
                    return mode;
                }
            }
            return null;
        }
    }
    public TargetMode mTargetMode = LaunchConfigDelegate.DEFAULT_TARGET_MODE;
    public boolean mWipeData = LaunchConfigDelegate.DEFAULT_WIPE_DATA;
    public boolean mNoBootAnim = LaunchConfigDelegate.DEFAULT_NO_BOOT_ANIM;
    public String mAvdName = null;
    public String mNetworkSpeed = EmulatorConfigTab.getSpeed(
            LaunchConfigDelegate.DEFAULT_SPEED);
    public String mNetworkDelay = EmulatorConfigTab.getDelay(
            LaunchConfigDelegate.DEFAULT_DELAY);
    public String mEmulatorCommandLine;
    public void set(ILaunchConfiguration config) {
        try {
            mLaunchAction = config.getAttribute(LaunchConfigDelegate.ATTR_LAUNCH_ACTION,
                    mLaunchAction);
        } catch (CoreException e1) {
        }
        try {
            boolean value = config.getAttribute(LaunchConfigDelegate.ATTR_TARGET_MODE,
                    mTargetMode.getValue());
            mTargetMode = TargetMode.getMode(value);
        } catch (CoreException e) {
        }
        try {
            mAvdName = config.getAttribute(LaunchConfigDelegate.ATTR_AVD_NAME, mAvdName);
        } catch (CoreException e) {
        }
        int index = LaunchConfigDelegate.DEFAULT_SPEED;
        try {
            index = config.getAttribute(LaunchConfigDelegate.ATTR_SPEED, index);
        } catch (CoreException e) {
        }
        mNetworkSpeed = EmulatorConfigTab.getSpeed(index);
        index = LaunchConfigDelegate.DEFAULT_DELAY;
        try {
            index = config.getAttribute(LaunchConfigDelegate.ATTR_DELAY, index);
        } catch (CoreException e) {
        }
        mNetworkDelay = EmulatorConfigTab.getDelay(index);
        try {
            mEmulatorCommandLine = config.getAttribute(
                    LaunchConfigDelegate.ATTR_COMMANDLINE, ""); 
        } catch (CoreException e) {
        }
        try {
            mWipeData = config.getAttribute(LaunchConfigDelegate.ATTR_WIPE_DATA, mWipeData);
        } catch (CoreException e) {
        }
        try {
            mNoBootAnim = config.getAttribute(LaunchConfigDelegate.ATTR_NO_BOOT_ANIM,
                                              mNoBootAnim);
        } catch (CoreException e) {
        }
    }
}
