public class DocletFactory implements IApiLoader {
    public IApi loadApi(String name, Visibility visibility,
            Set<String> fileNames, Set<String> packageNames) throws
            IOException {
        for (String packageName : packageNames) {
            if (packageName.length() == 0)
                throw new IllegalArgumentException(
                        "default package not supported by DocletFactory");
        }
        StringBuffer buf = new StringBuffer();
        for (String filename : fileNames) {
            buf.append(filename);
            buf.append(":");
        }
        String sourcepath = buf.substring(0, buf.length() - 1);
        RootDoc root = getRootDoc(visibility, sourcepath, packageNames);
        DocletToSigConverter converter = new DocletToSigConverter();
        IApi api = converter.convertDocletRoot(name, root, visibility,
                packageNames);
        return api;
    }
    private static RootDoc getRootDoc(Visibility visibility, String sourcepath,
            java.util.Set<String> packages) throws IOException {
        long accessModifier = 0;
        switch (visibility) {
        case PRIVATE:
            accessModifier |= com.sun.tools.javac.code.Flags.PRIVATE; 
        case PACKAGE:                                              
            accessModifier |= com.sun.tools.javadoc.ModifierFilter.PACKAGE;
        case PROTECTED:
            accessModifier |= com.sun.tools.javac.code.Flags.PROTECTED; 
        case PUBLIC:
            accessModifier |= com.sun.tools.javac.code.Flags.PUBLIC; 
        }
        ModifierFilter showAccess = new ModifierFilter(accessModifier);
        boolean breakiterator = false;
        boolean quiet = false;
        boolean legacy = false;
        boolean docClasses = false;
        String docLocale = "";
        String encoding = null;
        ListBuffer<String> javaNames = new ListBuffer<String>();
        for (String p : packages)
            javaNames.append(p);
        ListBuffer<String[]> options = new ListBuffer<String[]>();
        options.append(new String[] {"-sourcepath", sourcepath});
        ListBuffer<String> subPackages = new ListBuffer<String>();
        ListBuffer<String> excludedPackages = new ListBuffer<String>();
        Context context = new Context();
        Options compOpts = Options.instance(context);
        compOpts.put("-sourcepath", sourcepath);
        Constructor<Messager> c;
        try {
            c = Messager.class.getDeclaredConstructor(Context.class,
                    String.class, PrintWriter.class, PrintWriter.class,
                    PrintWriter.class);
            c.setAccessible(true);
            PrintWriter err = new PrintWriter(new StringWriter());
            PrintWriter warn = new PrintWriter(new StringWriter());
            PrintWriter notice = new PrintWriter(new StringWriter());
            c.newInstance(context, "SigTest", err, warn, notice);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        JavadocTool comp = JavadocTool.make0(context);
        RootDocImpl root = comp.getRootDocImpl(docLocale, encoding, showAccess,
                javaNames.toList(), options.toList(), breakiterator,
                subPackages.toList(), excludedPackages.toList(), docClasses,
                legacy, quiet);
        return root;
    }
}
