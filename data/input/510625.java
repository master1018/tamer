public class BuildDalvikSuite {
    public static boolean DEBUG = true;
    private static String JAVASRC_FOLDER = "";
    private static String MAIN_SRC_OUTPUT_FOLDER = "";
    private static String HOSTJUNIT_SRC_OUTPUT_FOLDER = ""; 
    private static String OUTPUT_FOLDER = "";
    private static String COMPILED_CLASSES_FOLDER = "";
    private static String CLASSES_OUTPUT_FOLDER = "";
    private static String HOSTJUNIT_CLASSES_OUTPUT_FOLDER = "";
    private static String CLASS_PATH = "";
    private static String restrictTo = null; 
    private int testClassCnt = 0;
    private int testMethodsCnt = 0;
    private LinkedHashMap<String, List<String>> map = new LinkedHashMap<String,
            List<String>>();
    private class MethodData {
        String methodBody, constraint, title;
    }
    public static void main(String[] args) throws IOException {
        if (args.length > 5) {
            JAVASRC_FOLDER = args[0];
            OUTPUT_FOLDER = args[1];
            CLASS_PATH = args[2];
            MAIN_SRC_OUTPUT_FOLDER = args[3];
            CLASSES_OUTPUT_FOLDER = MAIN_SRC_OUTPUT_FOLDER + "/classes";
            COMPILED_CLASSES_FOLDER = args[4];
            HOSTJUNIT_SRC_OUTPUT_FOLDER = args[5];
            HOSTJUNIT_CLASSES_OUTPUT_FOLDER = HOSTJUNIT_SRC_OUTPUT_FOLDER 
                    + "/classes";
            if (args.length > 6) {
                restrictTo = args[6];
                System.out.println("restricting build to: "+restrictTo);
            }
        } else {
            System.out
                    .println("usage: java-src-folder output-folder classpath "
                           + "generated-main-files compiled_output "
                           + "generated-main-files [restrict-to-opcode]");
            System.exit(-1);
        }
        long start = System.currentTimeMillis();
        BuildDalvikSuite cat = new BuildDalvikSuite();
        cat.compose();
        long end = System.currentTimeMillis();
        System.out.println("elapsed seconds: " + (end - start) / 1000);
    }
    public void compose() throws IOException {
        System.out.println("Collecting all junit tests...");
        new TestRunner() {
            @Override
            protected TestResult createTestResult() {
                return new TestResult() {
                    @Override
                    protected void run(TestCase test) {
                        addToTests(test);
                    }
                };
            }
        }.doRun(AllTests.suite());
        handleTests();
    }
    private void addToTests(TestCase test) {
        String packageName = test.getClass().getPackage().getName();
        packageName = packageName.substring(packageName.lastIndexOf('.'));
        String method = test.getName(); 
        String fqcn = test.getClass().getName(); 
        if (restrictTo != null && !fqcn.contains(restrictTo)) return;
        testMethodsCnt++;
        List<String> li = map.get(fqcn);
        if (li == null) {
            testClassCnt++;
            li = new ArrayList<String>();
            map.put(fqcn, li);
        }
        li.add(method);
    }
    private static final String ctsAllTestsB =
        "package dot.junit;\n" +
        "import junit.framework.Test;\n" +
        "import junit.framework.TestSuite;\n" +
        "public class AllJunitHostTests {\n" +
        "    public static final Test suite() {\n" +
        "        TestSuite suite = new TestSuite(\"CTS Host tests for all " +
        " dalvik vm opcodes\");\n";
    private static final String ctsAllTestsE =
        "    }"+
        "}";
    private static final String curFileDataE = "}\n";
    private String curAllTestsData = ctsAllTestsB;
    private String curJunitFileName = null;
    private String curJunitFileData = "";
    private JavacBuildStep javacHostJunitBuildStep;
    private void flushHostJunitFile() {
        if (curJunitFileName != null) {
        	File toWrite = new File(curJunitFileName);
            String absPath = toWrite.getAbsolutePath();
            javacHostJunitBuildStep.addSourceFile(absPath);
            curJunitFileData+="\n}\n";
            writeToFileMkdir(toWrite, curJunitFileData);
            curJunitFileName = null;
            curJunitFileData = "";
        }
    }
    private void ctsFinish() {
    	flushHostJunitFile();
    	curAllTestsData+="return suite;\n}\n}\n";
    	String allTestsFileName = HOSTJUNIT_SRC_OUTPUT_FOLDER
    	        + "/dot/junit/AllJunitHostTests.java";
    	File toWrite = new File(allTestsFileName);
    	writeToFileMkdir(toWrite, curAllTestsData);
    	javacHostJunitBuildStep.addSourceFile(toWrite.getAbsolutePath());
    	javacHostJunitBuildStep.addSourceFile(new File(
    	        HOSTJUNIT_SRC_OUTPUT_FOLDER + "/dot/junit/DeviceUtil.java").
    	        getAbsolutePath());
    }
    private void openCTSHostFileFor(String pName, String classOnlyName) {
        String sourceName = "JUnit_"+classOnlyName;
        String suiteline = "suite.addTestSuite("+pName+"." + sourceName + 
                ".class);\n";
        curAllTestsData += suiteline;
        flushHostJunitFile();
        curJunitFileName = HOSTJUNIT_SRC_OUTPUT_FOLDER+"/" 
                + pName.replaceAll("\\.","/")+"/"+sourceName+".java";
        curJunitFileData =
            "package "+pName+";\n"+
            "import java.io.IOException;\n"+
            "import junit.framework.TestCase;\n"+
            "import dot.junit.DeviceUtil;\n"+
            "public class "+sourceName+" extends TestCase {\n";
    }
    private String getADBPushJavaLine(String source, String target) {
        return "DeviceUtil.adbPush(\"" + source + "\", \"" + target + "\");";
    }
    private String getADBExecJavaLine(String classpath, String mainclass) {
        return "DeviceUtil.adbExec(\"" + classpath + "\", \"" + 
                mainclass + "\");";
    }
    private void addCTSHostMethod(String pName, String method, MethodData md,
            Set<String> dependentTestClassNames) {
    	curJunitFileData+="public void "+method+ "() throws Exception {\n";
        curJunitFileData+= "    "+getADBPushJavaLine("dot/junit/dexcore.jar",
                "/data/dexcore.jar");
        String mjar = "Main_"+method+".jar";
        String mainJar = "/data/"+mjar;
        String pPath = pName.replaceAll("\\.","/");
        curJunitFileData+= "    "+getADBPushJavaLine(pPath+"/"+mjar, mainJar);
        String cp = "/data/dexcore.jar:"+mainJar;
        for (String depFqcn : dependentTestClassNames) {
            int lastDotPos = depFqcn.lastIndexOf('.');
            String targetName= "/data/"+depFqcn.substring(lastDotPos +1)+".jar";
            String sourceName = depFqcn.replaceAll("\\.", "/")+".jar";
            curJunitFileData+= "    "+getADBPushJavaLine(sourceName, targetName);
            cp+= ":"+targetName;
        }
        String mainclass = pName + ".Main_" + method;
        curJunitFileData+= "    "+getADBExecJavaLine(cp, mainclass);
        curJunitFileData+= "}\n\n"; 
    }    
    private void handleTests() throws IOException {
        System.out.println("collected "+testMethodsCnt+" test methods in " + 
                testClassCnt+" junit test classes");
        String datafileContent = "";
        Set<BuildStep> targets = new TreeSet<BuildStep>();
        javacHostJunitBuildStep = new JavacBuildStep(
        		HOSTJUNIT_CLASSES_OUTPUT_FOLDER, CLASS_PATH);
        JavacBuildStep javacBuildStep = new JavacBuildStep(
                CLASSES_OUTPUT_FOLDER, CLASS_PATH);
        for (Entry<String, List<String>> entry : map.entrySet()) {
            String fqcn = entry.getKey();
            int lastDotPos = fqcn.lastIndexOf('.');
            String pName = fqcn.substring(0, lastDotPos);
            String classOnlyName = fqcn.substring(lastDotPos + 1);
            String instPrefix = "new " + classOnlyName + "()";
            openCTSHostFileFor(pName, classOnlyName);
            List<String> methods = entry.getValue();
            Collections.sort(methods, new Comparator<String>() {
                public int compare(String s1, String s2) {
                    return s1.compareTo(s2);
                }
            });
            for (String method : methods) {
                if (!method.startsWith("test")) {
                    throw new RuntimeException("no test method: " + method);
                }
                MethodData md = parseTestMethod(pName, classOnlyName, method);
                String methodContent = md.methodBody;
                Set<String> dependentTestClassNames = parseTestClassName(pName,
                        classOnlyName, methodContent);
                addCTSHostMethod(pName, method, md, dependentTestClassNames);
                if (dependentTestClassNames.isEmpty()) {
                    continue;
                }
                String content = "
                        + this.getClass().getName()
                        + ", do not change\n"
                        + "package "
                        + pName
                        + ";\n"
                        + "import "
                        + pName
                        + ".d.*;\n"
                        + "import dot.junit.*;\n"
                        + "public class Main_"
                        + method
                        + " extends DxAbstractMain {\n"
                        + "    public static void main(String[] args) "
                        + "throws Exception {"
                        + methodContent + "\n}\n";
                String fileName = getFileName(pName, method, ".java");
                File sourceFile = getFileFromPackage(pName, method);
                File classFile = new File(CLASSES_OUTPUT_FOLDER + "/"
                        + getFileName(pName, method, ".class"));
                writeToFile(sourceFile, content);
                javacBuildStep.addSourceFile(sourceFile.getAbsolutePath());
                BuildStep dexBuildStep = generateDexBuildStep(
                        CLASSES_OUTPUT_FOLDER, getFileName(pName, method, ""));
                targets.add(dexBuildStep);
                char ca = method.charAt("test".length()); 
                String comment;                
                switch (ca) {
                case 'N':
                    comment = "Normal #" + method.substring(5);
                    break;
                case 'B':
                    comment = "Boundary #" + method.substring(5);
                    break;
                case 'E':
                    comment = "Exception #" + method.substring(5);
                    break;
                case 'V':
                    comment = "Verifier #" + method.substring(7);
                    break;
                default:
                    throw new RuntimeException("unknown test abbreviation:"
                            + method + " for " + fqcn);
                }
                String line = pName + ".Main_" + method + ";";
                for (String className : dependentTestClassNames) {
                    line += className + " ";
                }
                String[] pparts = pName.split("\\.");
                String detail = pparts[pparts.length-1];
                String type = pparts[pparts.length-2];
                String description;
                if ("format".equals(type)) {
                    description = "format";
                } else if ("opcodes".equals(type)) {
                    detail = detail.replaceAll("_", "-");
                    detail = detail.replace("-from16", "/from16");
                    detail = detail.replace("-high16", "/high16");
                    detail = detail.replace("-lit8", "/lit8");
                    detail = detail.replace("-lit16", "/lit16");
                    detail = detail.replace("-4", "/4");
                    detail = detail.replace("-16", "/16");
                    detail = detail.replace("-32", "/32");
                    detail = detail.replace("-jumbo", "/jumbo");
                    detail = detail.replace("-range", "/range");
                    detail = detail.replace("-2addr", "/2addr");
                    detail = detail.replace("opc-", "");
                    description = detail;
                } else if ("verify".equals(type)) {
                    description = "verifier";
                } else {
                    description = type + " " + detail;
                }                
                String details = (md.title != null ? md.title : "");
                if (md.constraint != null) {
                    details = " Constraint " + md.constraint + ", " + details;
                }
                if (details.length() != 0) {
                    details = details.substring(0, 1).toUpperCase() 
                            + details.substring(1);
                }
                line += ";" + description + ";" + comment + ";" + details;
                datafileContent += line + "\n";
                generateBuildStepFor(pName, method, dependentTestClassNames,
                        targets);
            }
        }
        ctsFinish();
        File scriptDataDir = new File(OUTPUT_FOLDER + "/data/");
        scriptDataDir.mkdirs();
        writeToFile(new File(scriptDataDir, "scriptdata"), datafileContent);
        if (!javacHostJunitBuildStep.build()) {
            System.out.println("main javac cts-host-hostjunit-classes build " +
                    "step failed");
            System.exit(1);        	
        }
        if (javacBuildStep.build()) {
            for (BuildStep buildStep : targets) {
                if (!buildStep.build()) {
                    System.out.println("building failed. buildStep: " + 
                            buildStep.getClass().getName()+", "+buildStep);
                    System.exit(1);
                }
            }
        } else {
            System.out.println("main javac dalvik-cts-buildutil build step " +
                    "failed");
            System.exit(1);
        }
    }
    private void generateBuildStepFor(String pName, String method,
            Set<String> dependentTestClassNames, Set<BuildStep> targets) {
        for (String dependentTestClassName : dependentTestClassNames) {
            generateBuildStepForDependant(dependentTestClassName, targets);
        }
    }
    private void generateBuildStepForDependant(String dependentTestClassName,
            Set<BuildStep> targets) {
        File sourceFolder = new File(JAVASRC_FOLDER);
        String fileName = dependentTestClassName.replace('.', '/').trim();
        if (new File(sourceFolder, fileName + ".dfh").exists()) {
            BuildStep.BuildFile inputFile = new BuildStep.BuildFile(
                    JAVASRC_FOLDER, fileName + ".dfh");
            BuildStep.BuildFile dexFile = new BuildStep.BuildFile(
                    OUTPUT_FOLDER, fileName + ".dex");
            DFHBuildStep buildStep = new DFHBuildStep(inputFile, dexFile);
            BuildStep.BuildFile jarFile = new BuildStep.BuildFile(
                    OUTPUT_FOLDER, fileName + ".jar");
            JarBuildStep jarBuildStep = new JarBuildStep(dexFile,
                    "classes.dex", jarFile, true);
            jarBuildStep.addChild(buildStep);
            targets.add(jarBuildStep);
            return;
        }
        if (new File(sourceFolder, fileName + ".d").exists()) {
            BuildStep.BuildFile inputFile = new BuildStep.BuildFile(
                    JAVASRC_FOLDER, fileName + ".d");
            BuildStep.BuildFile dexFile = new BuildStep.BuildFile(
                    OUTPUT_FOLDER, fileName + ".dex");
            DasmBuildStep buildStep = new DasmBuildStep(inputFile, dexFile);
            BuildStep.BuildFile jarFile = new BuildStep.BuildFile(
                    OUTPUT_FOLDER, fileName + ".jar");
            JarBuildStep jarBuildStep = new JarBuildStep(dexFile,
                    "classes.dex", jarFile, true);
            jarBuildStep.addChild(buildStep);
            targets.add(jarBuildStep);
            return;
        }
        if (new File(sourceFolder, fileName + ".java").exists()) {
            BuildStep dexBuildStep = generateDexBuildStep(
                    COMPILED_CLASSES_FOLDER, fileName);
            targets.add(dexBuildStep);
            return;
        }
        try {
            if (Class.forName(dependentTestClassName) != null) {
                BuildStep dexBuildStep = generateDexBuildStep(
                        COMPILED_CLASSES_FOLDER, fileName);
                targets.add(dexBuildStep);
                return;
            }
        } catch (ClassNotFoundException e) {
        }
        throw new RuntimeException(
                "neither .dfh,.d,.java file of dependant test class found : "
                        + dependentTestClassName + ";" + fileName);
    }
    private BuildStep generateDexBuildStep(String classFileFolder,
            String classFileName) {
        BuildStep.BuildFile classFile = new BuildStep.BuildFile(
                classFileFolder, classFileName + ".class");
        BuildStep.BuildFile tmpJarFile = new BuildStep.BuildFile(OUTPUT_FOLDER,
                classFileName + "_tmp.jar");
        JarBuildStep jarBuildStep = new JarBuildStep(classFile, classFileName
                + ".class", tmpJarFile, false);
        BuildStep.BuildFile outputFile = new BuildStep.BuildFile(OUTPUT_FOLDER,
                classFileName + ".jar");
        DexBuildStep dexBuildStep = new DexBuildStep(tmpJarFile, outputFile,
                true);
        dexBuildStep.addChild(jarBuildStep);
        return dexBuildStep;
    }
    private Set<String> parseTestClassName(String pName, String classOnlyName,
            String methodSource) {
        Set<String> entries = new HashSet<String>();
        String opcodeName = classOnlyName.substring(5);
        Scanner scanner = new Scanner(methodSource);
        String[] patterns = new String[] {
                "new\\s(T_" + opcodeName + "\\w*)",
                "(T_" + opcodeName + "\\w*)", "new\\s(T\\w*)"};
        String token = null;
        for (String pattern : patterns) {
            token = scanner.findWithinHorizon(pattern, methodSource.length());
            if (token != null) {
                break;
            }
        }
        if (token == null) {
            System.err
                    .println("warning: failed to find dependent test class name: "
                            + pName
                            + ", "
                            + classOnlyName
                            + " in methodSource:\n" + methodSource);
            return entries;
        }
        MatchResult result = scanner.match();
        entries.add((pName + ".d." + result.group(1)).trim());
        Pattern p = Pattern.compile("@uses\\s+(.*)\\s+", Pattern.MULTILINE);
        Matcher m = p.matcher(methodSource);
        while (m.find()) {
            String res = m.group(1);
            entries.add(res.trim());
        }
        return entries;
    }
    private MethodData parseTestMethod(String pname, String classOnlyName,
            String method) {
        String path = pname.replaceAll("\\.", "/");
        String absPath = JAVASRC_FOLDER + "/" + path + "/" + classOnlyName
                + ".java";
        File f = new File(absPath);
        Scanner scanner;
        try {
            scanner = new Scanner(f);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("error while reading to file: "
                    + e.getClass().getName() + ", msg:" + e.getMessage());
        }
        String methodPattern = "public\\s+void\\s+" + method + "[^\\{]+\\{";
        String token = scanner.findWithinHorizon(methodPattern, (int) f
                .length());
        if (token == null) {
            throw new RuntimeException(
                    "cannot find method source of 'public void" + method
                            + "' in file '" + absPath + "'");
        }
        MatchResult result = scanner.match();
        result.start();
        result.end();
        StringBuilder builder = new StringBuilder();
        try {
            FileReader reader = new FileReader(f);
            reader.skip(result.end());
            char currentChar;
            int blocks = 1;
            while ((currentChar = (char) reader.read()) != -1 && blocks > 0) {
                switch (currentChar) {
                case '}': {
                    blocks--;
                    builder.append(currentChar);
                    break;
                }
                case '{': {
                    blocks++;
                    builder.append(currentChar);
                    break;
                }
                default: {
                    builder.append(currentChar);
                    break;
                }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("failed to parse", e);
        }
        Scanner scanner2;
        try {
            scanner2 = new Scanner(f);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("error while reading to file: "
                    + e.getClass().getName() + ", msg:" + e.getMessage());
        }
        String all = new String(FileUtils.readFile(f));
        String commentPattern = "/\\*\\*([^{]*)\\*/\\s*" + methodPattern;
        Pattern p = Pattern.compile(commentPattern, Pattern.DOTALL);
        Matcher m = p.matcher(all);
        String title = null, constraint = null;
        if (m.find()) {
            String res = m.group(1);
            Matcher titleM = Pattern.compile("@title (.*)", Pattern.DOTALL)
                    .matcher(res);
            if (titleM.find()) {
                title = titleM.group(1).replaceAll("\\n     \\*", "");
                title = title.replaceAll("\\n", " ");
                title = title.trim();
            } else {
                System.err.println("warning: no @title found for method "
                        + method + " in " + pname + "," + classOnlyName);
            }
            Matcher constraintM = Pattern.compile("@constraint (.*)").matcher(
                    res);
            if (constraintM.find()) {
                constraint = constraintM.group(1);
                constraint = constraint.trim();
            } else if (method.contains("VFE")) {
                System.err
                        .println("warning: no @constraint for for a VFE method:"
                                + method + " in " + pname + "," + classOnlyName);
            }
        } else {
            System.err.println("warning: no javadoc found for method " + method
                    + " in " + pname + "," + classOnlyName);
        }
        MethodData md = new MethodData();
        md.methodBody = builder.toString();
        md.constraint = constraint;
        md.title = title;
        return md;
    }
    private void writeToFileMkdir(File file, String content) {
	    File parent = file.getParentFile();
	    if (!parent.exists() && !parent.mkdirs()) {
	        throw new RuntimeException("failed to create directory: " + 
	                parent.getAbsolutePath());
	    }
	    writeToFile(file, content);
    }    
    private void writeToFile(File file, String content) {
        try {
            if (file.length() == content.length()) {
                FileReader reader = new FileReader(file);
                char[] charContents = new char[(int) file.length()];
                reader.read(charContents);
                String contents = new String(charContents);
                if (contents.equals(content)) {
                    return;
                }
            }
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(file), "utf-8"));
            bw.write(content);
            bw.close();
        } catch (Exception e) {
            throw new RuntimeException("error while writing to file: "
                    + e.getClass().getName() + ", msg:" + e.getMessage());
        }
    }
    private File getFileFromPackage(String pname, String methodName)
            throws IOException {
        String path = getFileName(pname, methodName, ".java");
        String absPath = MAIN_SRC_OUTPUT_FOLDER + "/" + path;
        File dirPath = new File(absPath);
        File parent = dirPath.getParentFile();
        if (!parent.exists() && !parent.mkdirs()) {
            throw new IOException("failed to create directory: " + absPath);
        }
        return dirPath;
    }
    private String getFileName(String pname, String methodName,
            String extension) {
        String path = pname.replaceAll("\\.", "/");
        return new File(path, "Main_" + methodName + extension).getPath();
    }
}
