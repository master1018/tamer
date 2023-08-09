public class LocalSdkParser {
    private Package[] mPackages;
    public LocalSdkParser() {
    }
    public Package[] getPackages() {
        return mPackages;
    }
    public void clearPackages() {
        mPackages = null;
    }
    public Package[] parseSdk(String osSdkRoot, SdkManager sdkManager, ISdkLog log) {
        ArrayList<Package> packages = new ArrayList<Package>();
        HashSet<File> visited = new HashSet<File>();
        File dir = new File(osSdkRoot, SdkConstants.FD_DOCS);
        Package pkg = scanDoc(dir, log);
        if (pkg != null) {
            packages.add(pkg);
            visited.add(dir);
        }
        dir = new File(osSdkRoot, SdkConstants.FD_TOOLS);
        pkg = scanTools(dir, log);
        if (pkg != null) {
            packages.add(pkg);
            visited.add(dir);
        }
        File samplesRoot = new File(osSdkRoot, SdkConstants.FD_SAMPLES);
        for(IAndroidTarget target : sdkManager.getTargets()) {
            Properties props = parseProperties(new File(target.getLocation(),
                    SdkConstants.FN_SOURCE_PROP));
            try {
                if (target.isPlatform()) {
                    pkg = new PlatformPackage(target, props);
                    if (samplesRoot.isDirectory()) {
                        File samplesDir = new File(target.getPath(IAndroidTarget.SAMPLES));
                        if (samplesDir.exists() && samplesDir.getParentFile().equals(samplesRoot)) {
                            Properties samplesProps = parseProperties(
                                    new File(samplesDir, SdkConstants.FN_SOURCE_PROP));
                            if (samplesProps != null) {
                                SamplePackage pkg2 = new SamplePackage(target, samplesProps);
                                packages.add(pkg2);
                            }
                            visited.add(samplesDir);
                        }
                    }
                } else {
                    pkg = new AddonPackage(target, props);
                }
            } catch (Exception e) {
                log.error(e, null);
            }
            if (pkg != null) {
                packages.add(pkg);
                visited.add(new File(target.getLocation()));
            }
        }
        scanMissingSamples(osSdkRoot, visited, packages, log);
        scanExtras(osSdkRoot, visited, packages, log);
        Collections.sort(packages);
        mPackages = packages.toArray(new Package[packages.size()]);
        return mPackages;
    }
    private void scanExtras(String osSdkRoot,
            HashSet<File> visited,
            ArrayList<Package> packages,
            ISdkLog log) {
        File root = new File(osSdkRoot);
        if (!root.isDirectory()) {
            return;
        }
        for (File dir : root.listFiles()) {
            if (dir.isDirectory() && !visited.contains(dir)) {
                Properties props = parseProperties(new File(dir, SdkConstants.FN_SOURCE_PROP));
                if (props != null) {
                    try {
                        ExtraPackage pkg = new ExtraPackage(
                                null,                       
                                props,                      
                                dir.getName(),              
                                0,                          
                                null,                       
                                "Tools",                    
                                null,                       
                                Os.getCurrentOs(),          
                                Arch.getCurrentArch(),      
                                dir.getPath()               
                                );
                        if (pkg.isPathValid()) {
                            packages.add(pkg);
                            visited.add(dir);
                        }
                    } catch (Exception e) {
                        log.error(e, null);
                    }
                }
            }
        }
    }
    private void scanMissingSamples(String osSdkRoot,
            HashSet<File> visited,
            ArrayList<Package> packages,
            ISdkLog log) {
        File root = new File(osSdkRoot);
        root = new File(root, SdkConstants.FD_SAMPLES);
        if (!root.isDirectory()) {
            return;
        }
        for (File dir : root.listFiles()) {
            if (dir.isDirectory() && !visited.contains(dir)) {
                Properties props = parseProperties(new File(dir, SdkConstants.FN_SOURCE_PROP));
                if (props != null) {
                    try {
                        SamplePackage pkg = new SamplePackage(dir.getAbsolutePath(), props);
                        packages.add(pkg);
                        visited.add(dir);
                    } catch (Exception e) {
                        log.error(e, null);
                    }
                }
            }
        }
    }
    private Package scanTools(File toolFolder, ISdkLog log) {
        Properties props = parseProperties(new File(toolFolder, SdkConstants.FN_SOURCE_PROP));
        Set<String> names = new HashSet<String>();
        for (File file : toolFolder.listFiles()) {
            names.add(file.getName());
        }
        if (!names.contains(SdkConstants.FN_ADB) ||
                !names.contains(SdkConstants.androidCmdName()) ||
                !names.contains(SdkConstants.FN_EMULATOR)) {
            return null;
        }
        try {
            ToolPackage pkg = new ToolPackage(
                    null,                       
                    props,                      
                    0,                          
                    null,                       
                    "Tools",                    
                    null,                       
                    Os.getCurrentOs(),          
                    Arch.getCurrentArch(),      
                    toolFolder.getPath()        
                    );
            return pkg;
        } catch (Exception e) {
            log.error(e, null);
        }
        return null;
    }
    private Package scanDoc(File docFolder, ISdkLog log) {
        Properties props = parseProperties(new File(docFolder, SdkConstants.FN_SOURCE_PROP));
        if (new File(docFolder, "index.html").isFile()) {
            try {
                DocPackage pkg = new DocPackage(
                        null,                       
                        props,                      
                        0,                          
                        null,                       
                        0,                          
                        null,                       
                        null,                       
                        null,                       
                        Os.getCurrentOs(),          
                        Arch.getCurrentArch(),      
                        docFolder.getPath()         
                        );
                return pkg;
            } catch (Exception e) {
                log.error(e, null);
            }
        }
        return null;
    }
    private Properties parseProperties(File propsFile) {
        FileInputStream fis = null;
        try {
            if (propsFile.exists()) {
                fis = new FileInputStream(propsFile);
                Properties props = new Properties();
                props.load(fis);
                if (props.size() > 0) {
                    return props;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                }
            }
        }
        return null;
    }
}
