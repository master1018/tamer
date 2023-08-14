public class Main {
    public static void main(String[] args) {
        Log log = new Log();
        ArrayList<String> osJarPath = new ArrayList<String>();
        String[] osDestJar = { null };
        if (!processArgs(log, args, osJarPath, osDestJar)) {
            log.error("Usage: layoutlib_create [-v] output.jar input.jar ...");
            System.exit(1);
        }
        log.info("Output: %1$s", osDestJar[0]);
        for (String path : osJarPath) {
            log.info("Input :      %1$s", path);
        }
        try {
            AsmGenerator agen = new AsmGenerator(log, osDestJar[0],
                    CreateInfo.INJECTED_CLASSES,
                    CreateInfo.OVERRIDDEN_METHODS,
                    CreateInfo.RENAMED_CLASSES,
                    CreateInfo.REMOVED_METHODS
            );
            AsmAnalyzer aa = new AsmAnalyzer(log, osJarPath, agen,
                    new String[] { "android.view.View" },   
                    new String[] {                          
                        "android.*", 
                        "android.util.*",
                        "com.android.internal.util.*",
                        "android.view.*",
                        "android.widget.*",
                        "com.android.internal.widget.*",
                        "android.text.**",
                        "android.graphics.*",
                        "android.graphics.drawable.*",
                        "android.content.*",
                        "android.content.res.*",
                        "org.apache.harmony.xml.*",
                        "com.android.internal.R**",
                        "android.pim.*", 
                        "android.os.*",  
                        });
            aa.analyze();
            agen.generate();
            Set<String> notRenamed = agen.getClassesNotRenamed();
            if (notRenamed.size() > 0) {
                log.error(
                  "ERROR when running layoutlib_create: the following classes are referenced\n" +
                  "by tools/layoutlib/create but were not actually found in the input JAR files.\n" +
                  "This may be due to some platform classes having been renamed.");
                for (String fqcn : notRenamed) {
                    log.error("- Class not found: %s", fqcn.replace('/', '.'));
                }
                for (String path : osJarPath) {
                    log.info("- Input JAR : %1$s", path);
                }
                System.exit(1);
            }
            System.exit(0);
        } catch (IOException e) {
            log.exception(e, "Failed to load jar");
        } catch (LogAbortException e) {
            e.error(log);
        }
        System.exit(1);
    }
    private static boolean processArgs(Log log, String[] args,
            ArrayList<String> osJarPath, String[] osDestJar) {
        for (int i = 0; i < args.length; i++) {
            String s = args[i];
            if (s.equals("-v")) {
                log.setVerbose(true);
            } else if (!s.startsWith("-")) {
                if (osDestJar[0] == null) {
                    osDestJar[0] = s;
                } else {
                    osJarPath.add(s);
                }
            } else {
                log.error("Unknow argument: %s", s);
                return false;
            }
        }
        if (osJarPath.isEmpty()) {
            log.error("Missing parameter: path to input jar");
            return false;
        }
        if (osDestJar[0] == null) {
            log.error("Missing parameter: path to output jar");
            return false;
        }
        return true;
    }
}
