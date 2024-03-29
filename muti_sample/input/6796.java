public class RecognizedOptions {
    private RecognizedOptions() {}
    public interface OptionHelper {
        void setOut(PrintWriter out);
        void error(String key, Object... args);
        void printVersion();
        void printFullVersion();
        void printHelp();
        void printXhelp();
        void addFile(File f);
        void addClassName(String s);
    }
    public static class GrumpyHelper implements OptionHelper {
        public void setOut(PrintWriter out) {
            throw new IllegalArgumentException();
        }
        public void error(String key, Object... args) {
            throw new IllegalArgumentException(Main.getLocalizedString(key, args));
        }
        public void printVersion() {
            throw new IllegalArgumentException();
        }
        public void printFullVersion() {
            throw new IllegalArgumentException();
        }
        public void printHelp() {
            throw new IllegalArgumentException();
        }
        public void printXhelp() {
            throw new IllegalArgumentException();
        }
        public void addFile(File f) {
            throw new IllegalArgumentException(f.getPath());
        }
        public void addClassName(String s) {
            throw new IllegalArgumentException(s);
        }
    }
    static Set<OptionName> javacOptions = EnumSet.of(
        G,
        G_NONE,
        G_CUSTOM,
        XLINT,
        XLINT_CUSTOM,
        NOWARN,
        VERBOSE,
        DEPRECATION,
        CLASSPATH,
        CP,
        SOURCEPATH,
        BOOTCLASSPATH,
        XBOOTCLASSPATH_PREPEND,
        XBOOTCLASSPATH_APPEND,
        XBOOTCLASSPATH,
        EXTDIRS,
        DJAVA_EXT_DIRS,
        ENDORSEDDIRS,
        DJAVA_ENDORSED_DIRS,
        PROC,
        PROCESSOR,
        PROCESSORPATH,
        D,
        S,
        IMPLICIT,
        ENCODING,
        SOURCE,
        TARGET,
        VERSION,
        FULLVERSION,
        DIAGS,
        HELP,
        A,
        X,
        J,
        MOREINFO,
        WERROR,
        PROMPT,
        DOE,
        PRINTSOURCE,
        WARNUNCHECKED,
        XMAXERRS,
        XMAXWARNS,
        XSTDOUT,
        XPKGINFO,
        XPRINT,
        XPRINTROUNDS,
        XPRINTPROCESSORINFO,
        XPREFER,
        O,
        XJCOV,
        XD,
        AT,
        SOURCEFILE);
    static Set<OptionName> javacFileManagerOptions = EnumSet.of(
        CLASSPATH,
        CP,
        SOURCEPATH,
        BOOTCLASSPATH,
        XBOOTCLASSPATH_PREPEND,
        XBOOTCLASSPATH_APPEND,
        XBOOTCLASSPATH,
        EXTDIRS,
        DJAVA_EXT_DIRS,
        ENDORSEDDIRS,
        DJAVA_ENDORSED_DIRS,
        PROCESSORPATH,
        D,
        S,
        ENCODING,
        SOURCE);
    static Set<OptionName> javacToolOptions = EnumSet.of(
        G,
        G_NONE,
        G_CUSTOM,
        XLINT,
        XLINT_CUSTOM,
        NOWARN,
        VERBOSE,
        DEPRECATION,
        PROC,
        PROCESSOR,
        IMPLICIT,
        SOURCE,
        TARGET,
        A,
        MOREINFO,
        WERROR,
        PROMPT,
        DOE,
        PRINTSOURCE,
        WARNUNCHECKED,
        XMAXERRS,
        XMAXWARNS,
        XPKGINFO,
        XPRINT,
        XPRINTROUNDS,
        XPRINTPROCESSORINFO,
        XPREFER,
        O,
        XJCOV,
        XD);
    static Option[] getJavaCompilerOptions(OptionHelper helper) {
        return getOptions(helper, javacOptions);
    }
    public static Option[] getJavacFileManagerOptions(OptionHelper helper) {
        return getOptions(helper, javacFileManagerOptions);
    }
    public static Option[] getJavacToolOptions(OptionHelper helper) {
        return getOptions(helper, javacToolOptions);
    }
    static Option[] getOptions(OptionHelper helper, Set<OptionName> desired) {
        ListBuffer<Option> options = new ListBuffer<Option>();
        for (Option option : getAll(helper))
            if (desired.contains(option.getName()))
                options.append(option);
        return options.toArray(new Option[options.length()]);
    }
    public static Option[] getAll(final OptionHelper helper) {
        return new Option[] {
        new Option(G,                                           "opt.g"),
        new Option(G_NONE,                                      "opt.g.none") {
            @Override
            public boolean process(Options options, String option) {
                options.put("-g:", "none");
                return false;
            }
        },
        new Option(G_CUSTOM,                                    "opt.g.lines.vars.source",
                Option.ChoiceKind.ANYOF, "lines", "vars", "source"),
        new XOption(XLINT,                                      "opt.Xlint"),
        new XOption(XLINT_CUSTOM,                               "opt.Xlint.suboptlist",
                Option.ChoiceKind.ANYOF, getXLintChoices()),
        new Option(NOWARN,                                      "opt.nowarn") {
            @Override
            public boolean process(Options options, String option) {
                options.put("-Xlint:none", option);
                return false;
            }
        },
        new Option(VERBOSE,                                     "opt.verbose"),
        new Option(DEPRECATION,                                 "opt.deprecation") {
            @Override
            public boolean process(Options options, String option) {
                options.put("-Xlint:deprecation", option);
                return false;
            }
        },
        new Option(CLASSPATH,              "opt.arg.path",      "opt.classpath"),
        new Option(CP,                     "opt.arg.path",      "opt.classpath") {
            @Override
            public boolean process(Options options, String option, String arg) {
                return super.process(options, "-classpath", arg);
            }
        },
        new Option(SOURCEPATH,             "opt.arg.path",      "opt.sourcepath"),
        new Option(BOOTCLASSPATH,          "opt.arg.path",      "opt.bootclasspath") {
            @Override
            public boolean process(Options options, String option, String arg) {
                options.remove("-Xbootclasspath/p:");
                options.remove("-Xbootclasspath/a:");
                return super.process(options, option, arg);
            }
        },
        new XOption(XBOOTCLASSPATH_PREPEND,"opt.arg.path", "opt.Xbootclasspath.p"),
        new XOption(XBOOTCLASSPATH_APPEND, "opt.arg.path", "opt.Xbootclasspath.a"),
        new XOption(XBOOTCLASSPATH,        "opt.arg.path", "opt.bootclasspath") {
            @Override
            public boolean process(Options options, String option, String arg) {
                options.remove("-Xbootclasspath/p:");
                options.remove("-Xbootclasspath/a:");
                return super.process(options, "-bootclasspath", arg);
            }
        },
        new Option(EXTDIRS,                "opt.arg.dirs",      "opt.extdirs"),
        new XOption(DJAVA_EXT_DIRS,        "opt.arg.dirs",      "opt.extdirs") {
            @Override
            public boolean process(Options options, String option, String arg) {
                return super.process(options, "-extdirs", arg);
            }
        },
        new Option(ENDORSEDDIRS,            "opt.arg.dirs",     "opt.endorseddirs"),
        new XOption(DJAVA_ENDORSED_DIRS,    "opt.arg.dirs",     "opt.endorseddirs") {
            @Override
            public boolean process(Options options, String option, String arg) {
                return super.process(options, "-endorseddirs", arg);
            }
        },
        new Option(PROC,                                 "opt.proc.none.only",
                Option.ChoiceKind.ONEOF, "none", "only"),
        new Option(PROCESSOR,           "opt.arg.class.list",   "opt.processor"),
        new Option(PROCESSORPATH,       "opt.arg.path",         "opt.processorpath"),
        new Option(D,                   "opt.arg.directory",    "opt.d"),
        new Option(S,                   "opt.arg.directory",    "opt.sourceDest"),
        new Option(IMPLICIT,                                    "opt.implicit",
                Option.ChoiceKind.ONEOF, "none", "class"),
        new Option(ENCODING,            "opt.arg.encoding",     "opt.encoding"),
        new Option(SOURCE,              "opt.arg.release",      "opt.source") {
            @Override
            public boolean process(Options options, String option, String operand) {
                Source source = Source.lookup(operand);
                if (source == null) {
                    helper.error("err.invalid.source", operand);
                    return true;
                }
                return super.process(options, option, operand);
            }
        },
        new Option(TARGET,              "opt.arg.release",      "opt.target") {
            @Override
            public boolean process(Options options, String option, String operand) {
                Target target = Target.lookup(operand);
                if (target == null) {
                    helper.error("err.invalid.target", operand);
                    return true;
                }
                return super.process(options, option, operand);
            }
        },
        new Option(VERSION,                                     "opt.version") {
            @Override
            public boolean process(Options options, String option) {
                helper.printVersion();
                return super.process(options, option);
            }
        },
        new HiddenOption(FULLVERSION) {
            @Override
            public boolean process(Options options, String option) {
                helper.printFullVersion();
                return super.process(options, option);
            }
        },
        new HiddenOption(DIAGS) {
            @Override
            public boolean process(Options options, String option) {
                Option xd = getOptions(helper, EnumSet.of(XD))[0];
                option = option.substring(option.indexOf('=') + 1);
                String diagsOption = option.contains("%") ?
                    "-XDdiagsFormat=" :
                    "-XDdiags=";
                diagsOption += option;
                if (xd.matches(diagsOption))
                    return xd.process(options, diagsOption);
                else
                    return false;
            }
        },
        new Option(HELP,                                        "opt.help") {
            @Override
            public boolean process(Options options, String option) {
                helper.printHelp();
                return super.process(options, option);
            }
        },
        new Option(A,                "opt.arg.key.equals.value","opt.A") {
            @Override
            String helpSynopsis() {
                hasSuffix = true;
                return super.helpSynopsis();
            }
            @Override
            public boolean matches(String arg) {
                return arg.startsWith("-A");
            }
            @Override
            public boolean hasArg() {
                return false;
            }
            @Override
            public boolean process(Options options, String option) {
                int argLength = option.length();
                if (argLength == 2) {
                    helper.error("err.empty.A.argument");
                    return true;
                }
                int sepIndex = option.indexOf('=');
                String key = option.substring(2, (sepIndex != -1 ? sepIndex : argLength) );
                if (!JavacProcessingEnvironment.isValidOptionName(key)) {
                    helper.error("err.invalid.A.key", option);
                    return true;
                }
                return process(options, option, option);
            }
        },
        new Option(X,                                           "opt.X") {
            @Override
            public boolean process(Options options, String option) {
                helper.printXhelp();
                return super.process(options, option);
            }
        },
        new Option(J,                   "opt.arg.flag",         "opt.J") {
            @Override
            String helpSynopsis() {
                hasSuffix = true;
                return super.helpSynopsis();
            }
            @Override
            public boolean process(Options options, String option) {
                throw new AssertionError
                    ("the -J flag should be caught by the launcher.");
            }
        },
        new HiddenOption(MOREINFO) {
            @Override
            public boolean process(Options options, String option) {
                Type.moreInfo = true;
                return super.process(options, option);
            }
        },
        new Option(WERROR,                                      "opt.Werror"),
        new HiddenOption(COMPLEXINFERENCE),
        new HiddenOption(PROMPT),
        new HiddenOption(DOE),
        new HiddenOption(PRINTSOURCE),
        new HiddenOption(WARNUNCHECKED) {
            @Override
            public boolean process(Options options, String option) {
                options.put("-Xlint:unchecked", option);
                return false;
            }
        },
        new XOption(XMAXERRS,           "opt.arg.number",       "opt.maxerrs"),
        new XOption(XMAXWARNS,          "opt.arg.number",       "opt.maxwarns"),
        new XOption(XSTDOUT,            "opt.arg.file",         "opt.Xstdout") {
            @Override
            public boolean process(Options options, String option, String arg) {
                try {
                    helper.setOut(new PrintWriter(new FileWriter(arg), true));
                } catch (java.io.IOException e) {
                    helper.error("err.error.writing.file", arg, e);
                    return true;
                }
                return super.process(options, option, arg);
            }
        },
        new XOption(XPRINT,                                     "opt.print"),
        new XOption(XPRINTROUNDS,                               "opt.printRounds"),
        new XOption(XPRINTPROCESSORINFO,                        "opt.printProcessorInfo"),
        new XOption(XPREFER,                                    "opt.prefer",
                Option.ChoiceKind.ONEOF, "source", "newer"),
        new XOption(XPKGINFO,                                   "opt.pkginfo",
                Option.ChoiceKind.ONEOF, "always", "legacy", "nonempty"),
        new HiddenOption(O),
        new HiddenOption(XJCOV),
        new HiddenOption(XD) {
            String s;
            @Override
            public boolean matches(String s) {
                this.s = s;
                return s.startsWith(name.optionName);
            }
            @Override
            public boolean process(Options options, String option) {
                s = s.substring(name.optionName.length());
                int eq = s.indexOf('=');
                String key = (eq < 0) ? s : s.substring(0, eq);
                String value = (eq < 0) ? s : s.substring(eq+1);
                options.put(key, value);
                return false;
            }
        },
        new Option(AT,                   "opt.arg.file",         "opt.AT") {
            @Override
            String helpSynopsis() {
                hasSuffix = true;
                return super.helpSynopsis();
            }
            @Override
            public boolean process(Options options, String option) {
                throw new AssertionError
                    ("the @ flag should be caught by CommandLine.");
            }
        },
        new HiddenOption(SOURCEFILE) {
            String s;
            @Override
            public boolean matches(String s) {
                this.s = s;
                return s.endsWith(".java")  
                    || SourceVersion.isName(s);   
            }
            @Override
            public boolean process(Options options, String option) {
                if (s.endsWith(".java") ) {
                    File f = new File(s);
                    if (!f.exists()) {
                        helper.error("err.file.not.found", f);
                        return true;
                    }
                    if (!f.isFile()) {
                        helper.error("err.file.not.file", f);
                        return true;
                    }
                    helper.addFile(f);
                }
                else
                    helper.addClassName(s);
                return false;
            }
        },
    };
    }
    public enum PkgInfo {
        ALWAYS, LEGACY, NONEMPTY;
        public static PkgInfo get(Options options) {
            String v = options.get(XPKGINFO);
            return (v == null
                    ? PkgInfo.LEGACY
                    : PkgInfo.valueOf(v.toUpperCase()));
        }
    }
    private static Map<String,Boolean> getXLintChoices() {
        Map<String,Boolean> choices = new LinkedHashMap<String,Boolean>();
        choices.put("all", false);
        for (Lint.LintCategory c : Lint.LintCategory.values())
            choices.put(c.option, c.hidden);
        for (Lint.LintCategory c : Lint.LintCategory.values())
            choices.put("-" + c.option, c.hidden);
        choices.put("none", false);
        return choices;
    }
}
