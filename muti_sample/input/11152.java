public class GenerateBreakIteratorData {
    private static String outputDir = "" ;
    private static String unicodeData = "UnicodeData.txt";
    private static String rules = "sun.text.resources.BreakIteratorRules";
    private static String language = "";
    private static String country = "";
    private static String valiant = "";
    private static String localeName = "";  
    public static void main(String[] args) {
        processArgs(args);
        CharacterCategory.makeCategoryMap(unicodeData);
        generateFiles();
    }
    private static void generateFiles() {
        String[] classNames;
        ResourceBundle rules, info;
        info =  ResourceBundle.getBundle("sun.text.resources.BreakIteratorInfo",
                                       new Locale(language, country, valiant));
        classNames = info.getStringArray("BreakIteratorClasses");
        rules = ResourceBundle.getBundle("sun.text.resources.BreakIteratorRules",
                                       new Locale(language, country, valiant));
        try {
            info = (ResourceBundle)Class.forName("sun.text.resources.BreakIteratorInfo" + localeName).newInstance();
            Enumeration keys = info.getKeys();
            while (keys.hasMoreElements()) {
                String key = (String)keys.nextElement();
                if (key.equals("CharacterData")) {
                    generateDataFile(info.getString(key),
                                     rules.getString("CharacterBreakRules"),
                                     classNames[0]);
                } else if (key.endsWith("WordData")) {
                    generateDataFile(info.getString(key),
                                     rules.getString("WordBreakRules"),
                                     classNames[1]);
                } else if (key.endsWith("LineData")) {
                    generateDataFile(info.getString(key),
                                     rules.getString("LineBreakRules"),
                                     classNames[2]);
                } else if (key.endsWith("SentenceData")) {
                    generateDataFile(info.getString(key),
                                     rules.getString("SentenceBreakRules"),
                                     classNames[3]);
                }
            }
        }
        catch (Exception e) {
            throw new InternalError(e.toString());
        }
    }
    private static void generateDataFile(String datafile, String rule, String builder) {
        RuleBasedBreakIteratorBuilder bld;
        if (builder.equals("RuleBasedBreakIterator")) {
            bld = new RuleBasedBreakIteratorBuilder(rule);
        } else if (builder.equals("DictionaryBasedBreakIterator")) {
            bld = new DictionaryBasedBreakIteratorBuilder(rule);
        } else {
            throw new IllegalArgumentException("Invalid break iterator class \"" + builder + "\"");
        }
        bld.makeFile(datafile);
    }
    private static void processArgs(String[] args) {
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("-o")) {
                outputDir = args[++i];
            } else if (arg.equals("-spec")) {
                unicodeData = args[++i];
            } else if (arg.equals("-language")) {
                language = args[++i];
            } else if (arg.equals("-country")) {
                country = args[++i];
            } else if (arg.equals("-valiant")) {
                valiant = args[++i];
            } else {
                usage();
            }
        }
        localeName = getLocaleName();
    }
    private static String getLocaleName() {
        if (language.equals("")) {
            if (!country.equals("") || !valiant.equals("")) {
                language = "en";
            } else {
                return "";
            }
        }
        StringBuffer sb = new StringBuffer();
        sb.append('_');
        sb.append(language);
        if (!country.equals("") || !valiant.equals("")) {
            sb.append('_');
            sb.append(country);
            if (!valiant.equals("")) {
                sb.append('_');
                sb.append(valiant);
            }
        }
        return sb.toString();
    }
    private static void usage() {
        System.err.println("Usage: GenerateBreakIteratorData [options]\n" +
        "    -o outputDir                 output directory name\n" +
        "    -spec specname               unicode text filename\n" +
        "  and locale data:\n" +
        "    -lang language               target language name\n" +
        "    -country country             target country name\n" +
        "    -valiant valiant             target valiant name\n"
        );
    }
    static String getOutputDirectory() {
        return outputDir;
    }
}
