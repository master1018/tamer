public class JDiff extends Doclet {
    public static LanguageVersion languageVersion(){
      return LanguageVersion.JAVA_1_5;
    } 
    public static boolean start(RootDoc root) {
        if (root != null)
            System.out.println("JDiff: doclet started ...");
        JDiff jd = new JDiff();
        return jd.startGeneration(root);
    }
    protected boolean startGeneration(RootDoc newRoot) {
        long startTime = System.currentTimeMillis();
        if (writeXML) {
            RootDocToXML.writeXML(newRoot);           
        }
        if (compareAPIs) {
	    String tempOldFileName = oldFileName;
	    if (oldDirectory != null) {
		tempOldFileName = oldDirectory;
		if (!tempOldFileName.endsWith(JDiff.DIR_SEP)) {
		    tempOldFileName += JDiff.DIR_SEP;
		}
		tempOldFileName += oldFileName;
	    }
            File f = new File(tempOldFileName);
            if (!f.exists()) {
                System.out.println("Error: file '" + tempOldFileName + "' does not exist for the old API");
                return false;
            }
	    String tempNewFileName = newFileName;
            if (newDirectory != null) {
		tempNewFileName = newDirectory;
		if (!tempNewFileName.endsWith(JDiff.DIR_SEP)) {
		    tempNewFileName += JDiff.DIR_SEP;
		}
		tempNewFileName += newFileName;
            }
            f = new File(tempNewFileName);
            if (!f.exists()) {
                System.out.println("Error: file '" + tempNewFileName + "' does not exist for the new API");
                return false;
            }
            System.out.print("JDiff: reading the old API in from file '" + tempOldFileName + "'...");
            API oldAPI = XMLToAPI.readFile(tempOldFileName, false, oldFileName);
            System.out.print("JDiff: reading the new API in from file '" + tempNewFileName + "'...");
            API newAPI = XMLToAPI.readFile(tempNewFileName, true, newFileName);
            APIComparator comp = new APIComparator();
            comp.compareAPIs(oldAPI, newAPI);
            int suffix = oldFileName.lastIndexOf('.');
            String commentsFileName = "user_comments_for_" + oldFileName.substring(0, suffix);
            suffix = newFileName.lastIndexOf('.');
            commentsFileName += "_to_" + newFileName.substring(0, suffix) + ".xml";
            commentsFileName = commentsFileName.replace(' ', '_');
                if (HTMLReportGenerator.commentsDir !=null) {
                  commentsFileName = HTMLReportGenerator.commentsDir + DIR_SEP + commentsFileName;
                } else if (HTMLReportGenerator.outputDir != null) {
                  commentsFileName = HTMLReportGenerator.outputDir + DIR_SEP + commentsFileName;
                }
            System.out.println("JDiff: reading the comments in from file '" + commentsFileName + "'...");
            Comments existingComments = Comments.readFile(commentsFileName);
            if (existingComments == null)
                System.out.println(" (the comments file will be created)");
            HTMLReportGenerator reporter = new HTMLReportGenerator();
            reporter.generate(comp, existingComments);
            Comments newComments = reporter.getNewComments();
            Comments.noteDifferences(existingComments, newComments);
            System.out.println("JDiff: writing the comments out to file '" + commentsFileName + "'...");
            Comments.writeFile(commentsFileName, newComments);
        }
        System.out.print("JDiff: finished (took " + (System.currentTimeMillis() - startTime)/1000 + "s");
        if (writeXML)
            System.out.println(", not including scanning the source files)."); 
        else if (compareAPIs)
            System.out.println(").");
       return true;
    }
    public static int optionLength(String option) {
        return Options.optionLength(option);
    }
    public static boolean validOptions(String[][] options, 
                                       DocErrorReporter reporter) {
        return Options.validOptions(options, reporter);
    }
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Looking for a local 'build.xml' configuration file");
        } else if (args.length == 1) {
            if (args[0].compareTo("-help") == 0 ||
                args[0].compareTo("-h") == 0 ||
                args[0].compareTo("?") == 0) {
                showUsage();
            } else if (args[0].compareTo("-version") == 0) {
                System.out.println("JDiff version: " + JDiff.version);
            }
            return;
        }
        int rc = runAnt(args);
        return;
    }
    public static void showUsage() {
        System.out.println("usage: java jdiff.JDiff [-version] [-buildfile <XML configuration file>]");
        System.out.println("If no build file is specified, the local build.xml file is used.");
    }
    public static int runAnt(String[] args) {
        String className = null;
        Class c = null;
        try {
            className = "org.apache.tools.ant.Main";
            c = Class.forName(className);
        } catch (ClassNotFoundException e1) {
            System.err.println("Error: ant.jar not found on the classpath");
            return -1;
        }
        try {
            Class[] methodArgTypes = new Class[1];
            methodArgTypes[0] = args.getClass();
            Method mainMethod = c.getMethod("main", methodArgTypes);
            Object[] methodArgs = new Object[1];
            methodArgs[0] = args;
            Integer res = (Integer)mainMethod.invoke(null, methodArgs);
            System.gc(); 
            return res.intValue();
        } catch (NoSuchMethodException e2) {
            System.err.println("Error: method \"main\" not found");
            e2.printStackTrace();
        } catch (IllegalAccessException e4) {
            System.err.println("Error: class not permitted to be instantiated");
            e4.printStackTrace();
        } catch (InvocationTargetException e5) {
            System.err.println("Error: method \"main\" could not be invoked");
            e5.printStackTrace();
        } catch (Exception e6) {
            System.err.println("Error: ");
            e6.printStackTrace();
        }
        System.gc(); 
        return -1;
    }
    static String oldFileName = "old_java.xml";
    static String oldDirectory = null;
    static String newFileName = "new_java.xml";
    static String newDirectory = null;
    static boolean writeXML = false;
    static boolean compareAPIs = false;
    static String DIR_SEP = System.getProperty("file.separator");
    static final String jDiffLocation = "http:
    static final String authorEmail = "mdoar@pobox.com";
    static final String jDiffDescription = "JDiff is a Javadoc doclet which generates an HTML report of all the packages, classes, constructors, methods, and fields which have been removed, added or changed in any way, including their documentation, when two APIs are compared.";
    static final String jDiffKeywords = "diff, jdiff, javadiff, java diff, java difference, API difference, difference between two APIs, API diff, Javadoc, doclet";
    static final String version = "1.1.0";
    static String javaVersion = System.getProperty("java.version");
    private static boolean trace = false;
} 
