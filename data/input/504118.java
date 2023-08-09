public final class ApkInstallManager {
    private final static ApkInstallManager sThis = new ApkInstallManager();
    private final static class ApkInstall {
        public ApkInstall(IProject project, String packageName, IDevice device) {
            this.project = project;
            this.packageName = packageName;
            this.device = device;
        }
        @Override
        public boolean equals(Object obj) {
            if (obj instanceof ApkInstall) {
                ApkInstall apkObj = (ApkInstall)obj;
                return (device == apkObj.device && project.equals(apkObj.project) &&
                        packageName.equals(apkObj.packageName));
            }
            return false;
        }
        @Override
        public int hashCode() {
            return (device.getSerialNumber() + project.getName() + packageName).hashCode();
        }
        final IProject project;
        final String packageName;
        final IDevice device;
    }
    private final static class PmReceiver extends MultiLineReceiver {
        boolean foundPackage = false;
        @Override
        public void processNewLines(String[] lines) {
            if (foundPackage == false) { 
                for (String line : lines) {
                    if (line.startsWith("package:/")) {
                        foundPackage = true;
                        break;
                    }
                }
            }
        }
        public boolean isCancelled() {
            return false;
        }
    }
    private final HashSet<ApkInstall> mInstallList = new HashSet<ApkInstall>();
    public static ApkInstallManager getInstance() {
        return sThis;
    }
    public void registerInstallation(IProject project, String packageName, IDevice device) {
        synchronized (mInstallList) {
            mInstallList.add(new ApkInstall(project, packageName, device));
        }
    }
    public boolean isApplicationInstalled(IProject project, String packageName, IDevice device) {
        synchronized (mInstallList) {
            ApkInstall found = null;
            for (ApkInstall install : mInstallList) {
                if (project.equals(install.project) && packageName.equals(install.packageName) &&
                        device == install.device) {
                    found = install;
                    break;
                }
            }
            if (found != null) {
                try {
                    PmReceiver receiver = new PmReceiver();
                    found.device.executeShellCommand("pm path " + packageName, receiver);
                    if (receiver.foundPackage == false) {
                        mInstallList.remove(found);
                    }
                    return receiver.foundPackage;
                } catch (IOException e) {
                    return false;
                }
            }
        }
        return false;
    }
    public void resetInstallationFor(IProject project) {
        synchronized (mInstallList) {
            Iterator<ApkInstall> iterator = mInstallList.iterator();
            while (iterator.hasNext()) {
                ApkInstall install = iterator.next();
                if (install.project.equals(project)) {
                    iterator.remove();
                }
            }
        }
    }
    private ApkInstallManager() {
        AndroidDebugBridge.addDeviceChangeListener(mDeviceChangeListener);
        AndroidDebugBridge.addDebugBridgeChangeListener(mDebugBridgeListener);
        GlobalProjectMonitor.getMonitor().addProjectListener(mProjectListener);
    }
    private IDebugBridgeChangeListener mDebugBridgeListener = new IDebugBridgeChangeListener() {
        public void bridgeChanged(AndroidDebugBridge bridge) {
            synchronized (mInstallList) {
                mInstallList.clear();
            }
        }
    };
    private IDeviceChangeListener mDeviceChangeListener = new IDeviceChangeListener() {
        public void deviceDisconnected(IDevice device) {
            synchronized (mInstallList) {
                Iterator<ApkInstall> iterator = mInstallList.iterator();
                while (iterator.hasNext()) {
                    ApkInstall install = iterator.next();
                    if (install.device == device) {
                        iterator.remove();
                    }
                }
            }
        }
        public void deviceChanged(IDevice device, int changeMask) {
        }
        public void deviceConnected(IDevice device) {
        }
    };
    private IProjectListener mProjectListener = new IProjectListener() {
        public void projectClosed(IProject project) {
            resetInstallationFor(project);
        }
        public void projectDeleted(IProject project) {
            resetInstallationFor(project);
        }
        public void projectOpened(IProject project) {
        }
        public void projectOpenedWithWorkspace(IProject project) {
        }
        public void projectRenamed(IProject project, IPath from) {
        }
    };
}
