public class ApkCheck {
    private static ApiList sCurrentApk;
    private static boolean sShowWarnings = false;
    private static boolean sShowErrors = true;
    private static HashSet<String> sIgnorablePackages = new HashSet<String>();
    public static void main(String[] args) {
        ApiList apiDescr = new ApiList("public-api");
        if (args.length < 2) {
            usage();
            return;
        }
        int idx;
        for (idx = 0; idx < args.length; idx++) {
            if (args[idx].equals("--help")) {
                usage();
                return;
            } else if (args[idx].startsWith("--uses-library=")) {
                String libName = args[idx].substring(args[idx].indexOf('=')+1);
                if ("BUILTIN".equals(libName)) {
                    Reader reader = Builtin.getReader();
                    if (!parseXml(apiDescr, reader, "BUILTIN"))
                        return;
                } else {
                    if (!parseApiDescr(apiDescr, libName))
                        return;
                }
            } else if (args[idx].startsWith("--ignore-package=")) {
                String pkgName = args[idx].substring(args[idx].indexOf('=')+1);
                sIgnorablePackages.add(pkgName);
            } else if (args[idx].equals("--warn")) {
                sShowWarnings = true;
            } else if (args[idx].equals("--no-warn")) {
                sShowWarnings = false;
            } else if (args[idx].equals("--error")) {
                sShowErrors = true;
            } else if (args[idx].equals("--no-error")) {
                sShowErrors = false;
            } else if (args[idx].startsWith("--")) {
                if (args[idx].equals("--")) {
                    idx++;
                    break;
                } else {
                    System.err.println("ERROR: unknown option " +
                        args[idx] + " (use \"--help\" for usage info)");
                    return;
                }
            } else {
                break;
            }
        }
        if (idx > args.length - 2) {
            usage();
            return;
        }
        if (!parseApiDescr(apiDescr, args[idx++]))
            return;
        sCurrentApk = apiDescr;
        flattenInherited(apiDescr);
        for ( ; idx < args.length; idx++) {
            ApiList apkDescr = new ApiList(args[idx]);
            sCurrentApk = apkDescr;
            boolean success = parseApiDescr(apkDescr, args[idx]);
            if (!success) {
                if (idx < args.length-1)
                    System.err.println("Skipping...");
                continue;
            }
            check(apiDescr, apkDescr);
            System.out.println(args[idx] + ": summary: " +
                apkDescr.getErrorCount() + " errors, " +
                apkDescr.getWarningCount() + " warnings\n");
        }
    }
    static void usage() {
        System.err.println("Android APK checker v1.0");
        System.err.println("Copyright (C) 2010 The Android Open Source Project\n");
        System.err.println("Usage: apkcheck [options] public-api.xml apk1.xml ...\n");
        System.err.println("Options:");
        System.err.println("  --help                  show this message");
        System.err.println("  --uses-library=lib.xml  load additional public API list");
        System.err.println("  --ignore-package=pkg    don't show errors for references to this package");
        System.err.println("  --[no-]warn             enable or disable display of warnings");
        System.err.println("  --[no-]error            enable or disable display of errors");
    }
    static boolean parseApiDescr(ApiList apiList, String fileName) {
        boolean result = false;
        try {
            FileReader fileReader = new FileReader(fileName);
            result = parseXml(apiList, fileReader, fileName);
            fileReader.close();
        } catch (IOException ioe) {
            System.err.println("Error opening " + fileName);
        }
        return result;
    }
    static boolean parseXml(ApiList apiList, Reader reader,
            String fileName) {
        try {
            XMLReader xmlReader = XMLReaderFactory.createXMLReader();
            ApiDescrHandler handler = new ApiDescrHandler(apiList);
            xmlReader.setContentHandler(handler);
            xmlReader.setErrorHandler(handler);
            xmlReader.parse(new InputSource(reader));
            return true;
        } catch (SAXParseException ex) {
            System.err.println("Error parsing " + fileName + " line " +
                ex.getLineNumber() + ": " + ex.getMessage());
        } catch (Exception ex) {
            System.err.println("Error while reading " + fileName + ": " +
                ex.getMessage());
            ex.printStackTrace();
        }
        return false;
    }
    static void flattenInherited(ApiList pubList) {
        Iterator<PackageInfo> pkgIter = pubList.getPackageIterator();
        while (pkgIter.hasNext()) {
            PackageInfo pubPkgInfo = pkgIter.next();
            Iterator<ClassInfo> classIter = pubPkgInfo.getClassIterator();
            while (classIter.hasNext()) {
                ClassInfo pubClassInfo = classIter.next();
                pubClassInfo.flattenClass(pubList);
            }
        }
    }
    static boolean check(ApiList pubList, ApiList apkDescr) {
        Iterator<PackageInfo> pkgIter = apkDescr.getPackageIterator();
        while (pkgIter.hasNext()) {
            PackageInfo apkPkgInfo = pkgIter.next();
            PackageInfo pubPkgInfo = pubList.getPackage(apkPkgInfo.getName());
            boolean badPackage = false;
            if (pubPkgInfo == null) {
                badPackage = true;
            }
            Iterator<ClassInfo> classIter = apkPkgInfo.getClassIterator();
            while (classIter.hasNext()) {
                ClassInfo apkClassInfo = classIter.next();
                if (badPackage) {
                    if (isIgnorable(apkPkgInfo)) {
                        apkWarning("Ignoring class ref: " +
                            apkPkgInfo.getName() + "." + apkClassInfo.getName());
                    } else {
                        apkError("Illegal class ref: " +
                            apkPkgInfo.getName() + "." + apkClassInfo.getName());
                    }
                } else {
                    checkClass(pubPkgInfo, apkClassInfo);
                }
            }
        }
        return true;
    }
    static boolean checkClass(PackageInfo pubPkgInfo, ClassInfo classInfo) {
        ClassInfo pubClassInfo = pubPkgInfo.getClass(classInfo.getName());
        if (pubClassInfo == null) {
            if (isIgnorable(pubPkgInfo)) {
                apkWarning("Ignoring class ref: " +
                    pubPkgInfo.getName() + "." + classInfo.getName());
            } else if (classInfo.hasNoFieldMethod()) {
                apkWarning("Hidden class referenced: " +
                    pubPkgInfo.getName() + "." + classInfo.getName());
            } else {
                apkError("Illegal class ref: " +
                    pubPkgInfo.getName() + "." + classInfo.getName());
            }
            return false;
        }
        Iterator<FieldInfo> fieldIter = classInfo.getFieldIterator();
        while (fieldIter.hasNext()) {
            FieldInfo apkFieldInfo = fieldIter.next();
            String nameAndType = apkFieldInfo.getNameAndType();
            FieldInfo pubFieldInfo = pubClassInfo.getField(nameAndType);
            if (pubFieldInfo == null) {
                if (pubClassInfo.isEnum()) {
                    apkWarning("Enum field ref: " + pubPkgInfo.getName() +
                        "." + classInfo.getName() + "." + nameAndType);
                } else {
                    apkError("Illegal field ref: " + pubPkgInfo.getName() +
                        "." + classInfo.getName() + "." + nameAndType);
                }
            }
        }
        Iterator<MethodInfo> methodIter = classInfo.getMethodIterator();
        while (methodIter.hasNext()) {
            MethodInfo apkMethodInfo = methodIter.next();
            String nameAndDescr = apkMethodInfo.getNameAndDescriptor();
            MethodInfo pubMethodInfo = pubClassInfo.getMethod(nameAndDescr);
            if (pubMethodInfo == null) {
                pubMethodInfo = pubClassInfo.getMethodIgnoringReturn(nameAndDescr);
                if (pubMethodInfo == null) {
                    if (pubClassInfo.isAnnotation()) {
                        apkWarning("Annotation method ref: " +
                            pubPkgInfo.getName() + "." + classInfo.getName() +
                            "." + nameAndDescr);
                    } else {
                        apkError("Illegal method ref: " + pubPkgInfo.getName() +
                            "." + classInfo.getName() + "." + nameAndDescr);
                    }
                } else {
                    apkWarning("Possibly covariant method ref: " +
                        pubPkgInfo.getName() + "." + classInfo.getName() +
                        "." + nameAndDescr);
                }
            }
        }
        return true;
    }
    static boolean isIgnorable(PackageInfo pkgInfo) {
        return sIgnorablePackages.contains(pkgInfo.getName());
    }
    public static void apkWarning(String msg) {
        if (sShowWarnings) {
            System.out.println("(warn) " + sCurrentApk.getDebugString() +
                ": " + msg);
        }
        sCurrentApk.incrWarnings();
    }
    public static void apkError(String msg) {
        if (sShowErrors) {
            System.out.println(sCurrentApk.getDebugString() + ": " + msg);
        }
        sCurrentApk.incrErrors();
    }
    private static void dumpApi(ApiList apiList) {
        Iterator<PackageInfo> iter = apiList.getPackageIterator();
        while (iter.hasNext()) {
            PackageInfo pkgInfo = iter.next();
            dumpPackage(pkgInfo);
        }
    }
    private static void dumpPackage(PackageInfo pkgInfo) {
        Iterator<ClassInfo> iter = pkgInfo.getClassIterator();
        System.out.println("PACKAGE " + pkgInfo.getName());
        while (iter.hasNext()) {
            ClassInfo classInfo = iter.next();
            dumpClass(classInfo);
        }
    }
    private static void dumpClass(ClassInfo classInfo) {
        System.out.println(" CLASS " + classInfo.getName());
        Iterator<FieldInfo> fieldIter = classInfo.getFieldIterator();
        while (fieldIter.hasNext()) {
            FieldInfo fieldInfo = fieldIter.next();
            dumpField(fieldInfo);
        }
        Iterator<MethodInfo> methIter = classInfo.getMethodIterator();
        while (methIter.hasNext()) {
            MethodInfo methInfo = methIter.next();
            dumpMethod(methInfo);
        }
    }
    private static void dumpMethod(MethodInfo methInfo) {
        System.out.println("  METHOD " + methInfo.getNameAndDescriptor());
    }
    private static void dumpField(FieldInfo fieldInfo) {
        System.out.println("  FIELD " + fieldInfo.getNameAndType());
    }
}
