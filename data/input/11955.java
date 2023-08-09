public class WindowsAttachProvider extends HotSpotAttachProvider {
    public WindowsAttachProvider() {
        String os = System.getProperty("os.name");
        if (os.startsWith("Windows 9") || os.equals("Windows Me")) {
            throw new RuntimeException(
                "This provider is not supported on this version of Windows");
        }
        String arch = System.getProperty("os.arch");
        if (!arch.equals("x86") && !arch.equals("amd64")) {
            throw new RuntimeException(
                "This provider is not supported on this processor architecture");
        }
    }
    public String name() {
        return "sun";
    }
    public String type() {
        return "windows";
    }
    public VirtualMachine attachVirtualMachine(String vmid)
        throws AttachNotSupportedException, IOException
    {
        checkAttachPermission();
        testAttachable(vmid);
        return new WindowsVirtualMachine(this, vmid);
    }
    public List<VirtualMachineDescriptor> listVirtualMachines() {
        if (isTempPathSecure()) {
            return super.listVirtualMachines();
        } else {
            return listJavaProcesses();
        }
    }
    private static boolean isTempPathSecure() {
        if (!wasTempPathChecked) {
            synchronized (WindowsAttachProvider.class) {
                if (!wasTempPathChecked) {
                    String temp = tempPath();
                    if ((temp != null) && (temp.length() >= 3) &&
                        (temp.charAt(1) == ':') && (temp.charAt(2) == '\\'))
                    {
                        long flags = volumeFlags(temp.substring(0, 3));
                        isTempPathSecure = ((flags & FS_PERSISTENT_ACLS) != 0);
                    }
                    wasTempPathChecked = true;
                }
            }
        }
        return isTempPathSecure;
    }
    private static final long FS_PERSISTENT_ACLS = 0x8L;
    private static volatile boolean wasTempPathChecked;
    private static boolean isTempPathSecure;
    private static native String tempPath();
    private static native long volumeFlags(String volume);
    private List<VirtualMachineDescriptor> listJavaProcesses() {
        ArrayList<VirtualMachineDescriptor> list =
            new ArrayList<VirtualMachineDescriptor>();
        String host = "localhost";
        try {
            host = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException uhe) {
        }
        int processes[] = new int[1024];
        int count = enumProcesses(processes, processes.length);
        for (int i=0; i<count; i++) {
            if (isLibraryLoadedByProcess("jvm.dll", processes[i])) {
                String pid = Integer.toString(processes[i]);
                try {
                    new WindowsVirtualMachine(this, pid).detach();
                    String name = pid + "@" + host;
                    list.add(new HotSpotVirtualMachineDescriptor(this, pid, name));
                } catch (AttachNotSupportedException x) {
                } catch (IOException ioe) {
                }
            }
        }
        return list;
    }
    private static native int enumProcesses(int[] processes, int max);
    private static native boolean isLibraryLoadedByProcess(String library,
                                                           int processId);
    static {
        System.loadLibrary("attach");
    }
}
