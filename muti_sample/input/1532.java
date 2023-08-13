public class JrmpGenerator implements Generator {
    private static final Map<String,StubVersion> versionOptions =
        new HashMap<String,StubVersion>();
    static {
        versionOptions.put("-v1.1", StubVersion.V1_1);
        versionOptions.put("-vcompat", StubVersion.VCOMPAT);
        versionOptions.put("-v1.2", StubVersion.V1_2);
    }
    private static final Set<String> bootstrapClassNames =
        new HashSet<String>();
    static {
        bootstrapClassNames.add("java.lang.Exception");
        bootstrapClassNames.add("java.rmi.Remote");
        bootstrapClassNames.add("java.rmi.RemoteException");
        bootstrapClassNames.add("java.lang.RuntimeException");
    };
    private StubVersion version = StubVersion.V1_2;     
    public JrmpGenerator() { }
    public boolean parseArgs(String[] args, Main main) {
        String explicitVersion = null;
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (versionOptions.containsKey(arg)) {
                if (explicitVersion != null && !explicitVersion.equals(arg)) {
                    main.error("rmic.cannot.use.both", explicitVersion, arg);
                    return false;
                }
                explicitVersion = arg;
                version = versionOptions.get(arg);
                args[i] = null;
            }
        }
        return true;
    }
    public Class<? extends BatchEnvironment> envClass() {
        return BatchEnvironment.class;
    }
    public Set<String> bootstrapClassNames() {
        return Collections.unmodifiableSet(bootstrapClassNames);
    }
    public void generate(BatchEnvironment env,
                         ClassDoc inputClass,
                         File destDir)
    {
        RemoteClass remoteClass = RemoteClass.forClass(env, inputClass);
        if (remoteClass == null) {
            return;     
        }
        StubSkeletonWriter writer =
            new StubSkeletonWriter(env, remoteClass, version);
        File stubFile = sourceFileForClass(writer.stubClassName(), destDir);
        try {
            IndentingWriter out = new IndentingWriter(
                new OutputStreamWriter(new FileOutputStream(stubFile)));
            writer.writeStub(out);
            out.close();
            if (env.verbose()) {
                env.output(Resources.getText("rmic.wrote",
                                             stubFile.getPath()));
            }
            env.addGeneratedFile(stubFile);
        } catch (IOException e) {
            env.error("rmic.cant.write", stubFile.toString());
            return;
        }
        File skeletonFile =
            sourceFileForClass(writer.skeletonClassName(), destDir);
        if (version == StubVersion.V1_1 ||
            version == StubVersion.VCOMPAT)
        {
            try {
                IndentingWriter out = new IndentingWriter(
                    new OutputStreamWriter(
                        new FileOutputStream(skeletonFile)));
                writer.writeSkeleton(out);
                out.close();
                if (env.verbose()) {
                    env.output(Resources.getText("rmic.wrote",
                                                 skeletonFile.getPath()));
                }
                env.addGeneratedFile(skeletonFile);
            } catch (IOException e) {
                env.error("rmic.cant.write", skeletonFile.toString());
                return;
            }
        } else {
            File skeletonClassFile =
                classFileForClass(writer.skeletonClassName(), destDir);
            skeletonFile.delete();      
            skeletonClassFile.delete();
        }
    }
    private File sourceFileForClass(String binaryName, File destDir) {
        return fileForClass(binaryName, destDir, ".java");
    }
    private File classFileForClass(String binaryName, File destDir) {
        return fileForClass(binaryName, destDir, ".class");
    }
    private File fileForClass(String binaryName, File destDir, String ext) {
        int i = binaryName.lastIndexOf('.');
        String classFileName = binaryName.substring(i + 1) + ext;
        if (i != -1) {
            String packageName = binaryName.substring(0, i);
            String packagePath = packageName.replace('.', File.separatorChar);
            File packageDir = new File(destDir, packagePath);
            if (!packageDir.exists()) {
                packageDir.mkdirs();
            }
            return new File(packageDir, classFileName);
        } else {
            return new File(destDir, classFileName);
        }
    }
}
