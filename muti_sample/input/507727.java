public class Options {
    public Options() {
    }
    public static int optionLength(String option) {
        String opt = option.toLowerCase();
        if (opt.equals("-authorid"))  return 2;
        if (opt.equals("-versionid")) return 2;
        if (opt.equals("-d"))         return 2;
        if (opt.equals("-classlist")) return 1;
        if (opt.equals("-title"))     return 2;
        if (opt.equals("-docletid"))  return 1;
        if (opt.equals("-evident"))   return 2;
        if (opt.equals("-skippkg"))   return 2;
        if (opt.equals("-skipclass")) return 2;
        if (opt.equals("-execdepth")) return 2;
        if (opt.equals("-help"))      return 1;
        if (opt.equals("-version"))      return 1;
        if (opt.equals("-package"))   return 1;
        if (opt.equals("-protected")) return 1;
        if (opt.equals("-public"))    return 1;
        if (opt.equals("-private"))   return 1;
        if (opt.equals("-sourcepath")) return 2;
        if (opt.equals("-apiname"))    return 2;
        if (opt.equals("-oldapi"))    return 2;
        if (opt.equals("-newapi"))    return 2;
        if (opt.equals("-apidir"))       return 2;
        if (opt.equals("-oldapidir"))    return 2;
        if (opt.equals("-newapidir"))    return 2;
        if (opt.equals("-usercommentsdir"))    return 2;
        if (opt.equals("-excludeclass"))    return 2;
        if (opt.equals("-excludemember"))    return 2;
        if (opt.equals("-firstsentence"))    return 1;
        if (opt.equals("-docchanges"))    return 1;
        if (opt.equals("-packagesonly"))    return 1;
        if (opt.equals("-showallchanges"))    return 1;
        if (opt.equals("-javadocnew"))    return 2;
        if (opt.equals("-javadocold"))    return 2;
        if (opt.equals("-baseuri"))    return 2;
        if (opt.equals("-nosuggest"))    return 2;
        if (opt.equals("-checkcomments"))    return 1;
        if (opt.equals("-retainnonprinting"))    return 1;
        if (opt.equals("-excludetag"))    return 2;
        if (opt.equals("-stats"))    return 1;
        if (opt.equals("-windowtitle"))    return 2;
        if (opt.equals("-doctitle"))    return 2;
        return 0;
    }
    public static boolean validOptions(String[][] options,
                                       DocErrorReporter reporter) {
        final DocErrorReporter errOut = reporter;
        class ErrorHandler {
            boolean noErrorsFound = true;
            void msg(String msg) {
                noErrorsFound = false;
                errOut.printError(msg);
            }
        }
        ErrorHandler err = new ErrorHandler();
        if (trace)
            System.out.println("Command line arguments: "); 
        for (int i = 0; i < options.length; i++) {
            for (int j = 0; j < options[i].length; j++) {
                Options.cmdOptions += " " + options[i][j];
                if (trace)
                    System.out.print(" " + options[i][j]); 
            }            
        }
        if (trace)
            System.out.println(); 
        for (int i = 0; i < options.length; i++) {
            if (options[i][0].toLowerCase().equals("-apiname")) {
                if (options[i].length < 2) {
                    err.msg("No version identifier specified after -apiname option.");
                } else if (JDiff.compareAPIs) {
                    err.msg("Use the -apiname option, or the -oldapi and -newapi options, but not both.");
                } else { 
                    String filename = options[i][1];
                    RootDocToXML.apiIdentifier = filename;
                    filename = filename.replace(' ', '_');
                    RootDocToXML.outputFileName =  filename + ".xml";
                    JDiff.writeXML = true;
                    JDiff.compareAPIs = false;
                }
                continue;
            }
            if (options[i][0].toLowerCase().equals("-apidir")) {
                if (options[i].length < 2) {
                    err.msg("No directory specified after -apidir option.");
                } else {
		    RootDocToXML.outputDirectory = options[i][1];
                }
                continue;
            }
	    if (options[i][0].toLowerCase().equals("-oldapi")) {
                if (options[i].length < 2) {
                    err.msg("No version identifier specified after -oldapi option.");
                } else if (JDiff.writeXML) {
                    err.msg("Use the -apiname or -oldapi option, but not both.");
                } else { 
                    String filename = options[i][1];
                    filename = filename.replace(' ', '_');
                    JDiff.oldFileName =  filename + ".xml";
                    JDiff.writeXML = false;
                    JDiff.compareAPIs = true;
                }
                continue;
            }
            if (options[i][0].toLowerCase().equals("-oldapidir")) {
                if (options[i].length < 2) {
                    err.msg("No directory specified after -oldapidir option.");
                } else { 
                	JDiff.oldDirectory = options[i][1];
                }
                continue;
            }
            if (options[i][0].toLowerCase().equals("-newapi")) {
                if (options[i].length < 2) {
                    err.msg("No version identifier specified after -newapi option.");
                } else if (JDiff.writeXML) {
                    err.msg("Use the -apiname or -newapi option, but not both.");
                } else { 
                    String filename = options[i][1];
                    filename = filename.replace(' ', '_');
                    JDiff.newFileName =  filename + ".xml";
                    JDiff.writeXML = false;
                    JDiff.compareAPIs = true;
                }
                continue;
            }
            if (options[i][0].toLowerCase().equals("-newapidir")) {
                if (options[i].length < 2) {
                    err.msg("No directory specified after -newapidir option.");
                } else { 
                	JDiff.newDirectory = options[i][1];
                }
                continue;
            }
            if (options[i][0].toLowerCase().equals("-usercommentsdir")) {
                if (options[i].length < 2) {
                    err.msg("Android: No directory specified after -usercommentsdir option.");
                } else { 
                    HTMLReportGenerator.commentsDir = options[i][1];
                }
                continue;
            }
            if (options[i][0].toLowerCase().equals("-d")) {
                if (options[i].length < 2) {
                    err.msg("No directory specified after -d option.");
                } else {
                    HTMLReportGenerator.outputDir = options[i][1];
                }
                continue;
            }
            if (options[i][0].toLowerCase().equals("-javadocnew")) {
                if (options[i].length < 2) {
                    err.msg("No location specified after -javadocnew option.");
                } else {
                    HTMLReportGenerator.newDocPrefix = options[i][1];
                }
                continue;
            }
            if (options[i][0].toLowerCase().equals("-javadocold")) {
                if (options[i].length < 2) {
                    err.msg("No location specified after -javadocold option.");
                } else {
                    HTMLReportGenerator.oldDocPrefix = options[i][1];
                }
                continue;
            }
            if (options[i][0].toLowerCase().equals("-baseuri")) {
                if (options[i].length < 2) {
                    err.msg("No base location specified after -baseURI option.");
                } else {
                    RootDocToXML.baseURI = options[i][1];
                }
                continue;
            }
            if (options[i][0].toLowerCase().equals("-excludeclass")) {
                if (options[i].length < 2) {
                    err.msg("No level (public|protected|package|private) specified after -excludeclass option.");
                } else { 
                    String level = options[i][1];
                    if (level.compareTo("public") != 0 &&
                        level.compareTo("protected") != 0 &&
                        level.compareTo("package") != 0 &&
                        level.compareTo("private") != 0) {
                        err.msg("Level specified after -excludeclass option must be one of (public|protected|package|private).");
                    } else {
                        RootDocToXML.classVisibilityLevel = level;
                    }
                }
                continue;
            }
            if (options[i][0].toLowerCase().equals("-excludemember")) {
                if (options[i].length < 2) {
                    err.msg("No level (public|protected|package|private) specified after -excludemember option.");
                } else { 
                    String level = options[i][1];
                    if (level.compareTo("public") != 0 &&
                        level.compareTo("protected") != 0 &&
                        level.compareTo("package") != 0 &&
                        level.compareTo("private") != 0) {
                        err.msg("Level specified after -excludemember option must be one of (public|protected|package|private).");
                    } else {
                        RootDocToXML.memberVisibilityLevel = level;
                    }
                }
                continue;
            }
            if (options[i][0].toLowerCase().equals("-firstsentence")) {
                RootDocToXML.saveAllDocs = false;
                continue;
            }
            if (options[i][0].toLowerCase().equals("-docchanges")) {
                HTMLReportGenerator.reportDocChanges = true;
                Diff.noDocDiffs = false;
                continue;
            }
            if (options[i][0].toLowerCase().equals("-packagesonly")) {
                RootDocToXML.packagesOnly = true;
                continue;
            }
            if (options[i][0].toLowerCase().equals("-showallchanges")) {
                Diff.showAllChanges = true;
                continue;
            }
            if (options[i][0].toLowerCase().equals("-nosuggest")) {
                if (options[i].length < 2) {
                    err.msg("No level (all|remove|add|change) specified after -nosuggest option.");
                } else { 
                    String level = options[i][1];
                    if (level.compareTo("all") != 0 &&
                        level.compareTo("remove") != 0 &&
                        level.compareTo("add") != 0 &&
                        level.compareTo("change") != 0) {
                        err.msg("Level specified after -nosuggest option must be one of (all|remove|add|change).");
                    } else {
                        if (level.compareTo("removal") == 0)
                            HTMLReportGenerator.noCommentsOnRemovals = true;
                        else if (level.compareTo("add") == 0)
                            HTMLReportGenerator.noCommentsOnAdditions = true;
                        else if (level.compareTo("change") == 0)
                            HTMLReportGenerator.noCommentsOnChanges = true;
                        else if (level.compareTo("all") == 0) {
                            HTMLReportGenerator.noCommentsOnRemovals = true;
                            HTMLReportGenerator.noCommentsOnAdditions = true;
                            HTMLReportGenerator.noCommentsOnChanges = true;
                        }
                    }
                }
                continue;
            }
            if (options[i][0].toLowerCase().equals("-checkcomments")) {
                APIHandler.checkIsSentence = true;
                continue;
            }
            if (options[i][0].toLowerCase().equals("-retainnonprinting")) {
                RootDocToXML.stripNonPrintables = false;
                continue;
            }
            if (options[i][0].toLowerCase().equals("-excludetag")) {
                if (options[i].length < 2) {
                    err.msg("No exclude tag specified after -excludetag option.");
                } else { 
                    RootDocToXML.excludeTag = options[i][1];
                    RootDocToXML.excludeTag = RootDocToXML.excludeTag.trim();
                    RootDocToXML.doExclude = true;
                }
                continue;
            }
            if (options[i][0].toLowerCase().equals("-stats")) {
                HTMLReportGenerator.doStats = true;
                continue;
            }
            if (options[i][0].toLowerCase().equals("-doctitle")) {
                if (options[i].length < 2) {
                    err.msg("No HTML text specified after -doctitle option.");
                } else { 
                    HTMLReportGenerator.docTitle = options[i][1];
                }
                continue;
            }
            if (options[i][0].toLowerCase().equals("-windowtitle")) {
                if (options[i].length < 2) {
                    err.msg("No text specified after -windowtitle option.");
                } else { 
                    HTMLReportGenerator.windowTitle = options[i][1];
                }
                continue;
            }
            if (options[i][0].toLowerCase().equals("-version")) {
                System.out.println("JDiff version: " + JDiff.version);
                System.exit(0);
            }
            if (options[i][0].toLowerCase().equals("-help")) {
                usage();
                System.exit(0);
            }
        }
        if (!JDiff.writeXML && !JDiff.compareAPIs) {
            err.msg("First use the -apiname option to generate an XML file for one API.");
            err.msg("Then use the -apiname option again to generate another XML file for a different version of the API.");
            err.msg("Finally use the -oldapi option and -newapi option to generate a report about how the APIs differ.");
        }
        return err.noErrorsFound;
    }
    public static void usage() {
        System.err.println("JDiff version: " + JDiff.version);
        System.err.println("");
        System.err.println("Valid JDiff arguments:");
        System.err.println("");
        System.err.println("  -apiname <Name of a version>");
        System.err.println("  -oldapi <Name of a version>");
        System.err.println("  -newapi <Name of a version>");
        System.err.println("  Optional Arguments");
        System.err.println();
        System.err.println("  -d <directory> Destination directory for output HTML files");
        System.err.println("  -oldapidir <directory> Location of the XML file for the old API");
        System.err.println("  -newapidir <directory> Location of the XML file for the new API");
        System.err.println("  -sourcepath <location of Java source files>");
        System.err.println("  -javadocnew <location of existing Javadoc files for the new API>");
        System.err.println("  -javadocold <location of existing Javadoc files for the old API>");
        System.err.println("  -usercommentsdir <directory> Path to dir containing the user_comments* file(s)");
        System.err.println("  -baseURI <base> Use \"base\" as the base location of the various DTDs and Schemas used by JDiff");
        System.err.println("  -excludeclass [public|protected|package|private] Exclude classes which are not public, protected etc");
        System.err.println("  -excludemember [public|protected|package|private] Exclude members which are not public, protected etc");
        System.err.println("  -firstsentence Save only the first sentence of each comment block with the API.");
        System.err.println("  -docchanges Report changes in Javadoc comments between the APIs");
        System.err.println("  -nosuggest [all|remove|add|change] Do not add suggested comments to all, or the removed, added or chabged sections");
        System.err.println("  -checkcomments Check that comments are sentences");
        System.err.println("  -stripnonprinting Remove non-printable characters from comments.");
        System.err.println("  -excludetag <tag> Define the Javadoc tag which implies exclusion");
        System.err.println("  -stats Generate statistical output");
        System.err.println("  -help       (generates this output)");
        System.err.println("");
        System.err.println("For more help, see jdiff.html");
    }
    public static String cmdOptions = "";
    private static boolean trace = false;
}
