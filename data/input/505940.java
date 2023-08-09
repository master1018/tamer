public class ToolPackage extends Package {
    ToolPackage(RepoSource source, Node packageNode, Map<String,String> licenses) {
        super(source, packageNode, licenses);
    }
    ToolPackage(
            RepoSource source,
            Properties props,
            int revision,
            String license,
            String description,
            String descUrl,
            Os archiveOs,
            Arch archiveArch,
            String archiveOsPath) {
        super(source,
                props,
                revision,
                license,
                description,
                descUrl,
                archiveOs,
                archiveArch,
                archiveOsPath);
    }
    @Override
    public String getShortDescription() {
        return String.format("Android SDK Tools, revision %1$d%2$s",
                getRevision(),
                isObsolete() ? " (Obsolete)" : "");
    }
    @Override
    public String getLongDescription() {
        String s = getDescription();
        if (s == null || s.length() == 0) {
            s = getShortDescription();
        }
        if (s.indexOf("revision") == -1) {
            s += String.format("\nRevision %1$d%2$s",
                    getRevision(),
                    isObsolete() ? " (Obsolete)" : "");
        }
        return s;
    }
    @Override
    public File getInstallFolder(String osSdkRoot, String suggestedDir, SdkManager sdkManager) {
        return new File(osSdkRoot, SdkConstants.FD_TOOLS);
    }
    @Override
    public boolean sameItemAs(Package pkg) {
        return pkg instanceof ToolPackage;
    }
    @Override
    public void postInstallHook(Archive archive, ITaskMonitor monitor, File installFolder) {
        super.postInstallHook(archive, monitor, installFolder);
        if (installFolder == null) {
            return;
        }
        File libDir = new File(installFolder, SdkConstants.FD_LIB);
        if (!libDir.isDirectory()) {
            return;
        }
        String scriptName = "post_tools_install";   
        String shell = "";
        if (SdkConstants.currentPlatform() == SdkConstants.PLATFORM_WINDOWS) {
            shell = "cmd.exe /c ";
            scriptName += ".bat";                   
        } else {
            scriptName += ".sh";                    
        }
        File scriptFile = new File(libDir, scriptName);
        if (!scriptFile.isFile()) {
            return;
        }
        Process proc;
        int status = -1;
        try {
            proc = Runtime.getRuntime().exec(
                    shell + scriptName, 
                    null,       
                    libDir);    
            status = grabProcessOutput(proc, monitor, scriptName);
        } catch (Exception e) {
            monitor.setResult("Exception: %s", e.toString());
        }
        if (status != 0) {
            monitor.setResult("Failed to execute %s", scriptName);
            return;
        }
    }
    private int grabProcessOutput(final Process process,
            final ITaskMonitor monitor,
            final String scriptName)
                throws InterruptedException {
        Thread t1 = new Thread("") { 
            @Override
            public void run() {
                InputStreamReader is = new InputStreamReader(process.getErrorStream());
                BufferedReader errReader = new BufferedReader(is);
                try {
                    while (true) {
                        String line = errReader.readLine();
                        if (line != null) {
                            monitor.setResult("[%1$s] Error: %2$s", scriptName, line);
                        } else {
                            break;
                        }
                    }
                } catch (IOException e) {
                }
            }
        };
        Thread t2 = new Thread("") { 
            @Override
            public void run() {
                InputStreamReader is = new InputStreamReader(process.getInputStream());
                BufferedReader outReader = new BufferedReader(is);
                try {
                    while (true) {
                        String line = outReader.readLine();
                        if (line != null) {
                            monitor.setResult("[%1$s] %2$s", scriptName, line);
                        } else {
                            break;
                        }
                    }
                } catch (IOException e) {
                }
            }
        };
        t1.start();
        t2.start();
        return process.waitFor();
    }
}
