public class LayoutAnalyzer {
    private static final String RULES_PREFIX = "rules/";
    private final XmlDocumentBuilder mBuilder = new XmlDocumentBuilder();
    private final List<Rule> mRules = new ArrayList<Rule>();
    public LayoutAnalyzer() {
        loadRules();
    }
    private void loadRules() {
        ClassLoader parent = getClass().getClassLoader();
        GroovyClassLoader loader = new GroovyClassLoader(parent);
        GroovyShell shell = new GroovyShell(loader);
        URL jar = getClass().getProtectionDomain().getCodeSource().getLocation();
        ZipFile zip = null;
        try {
            zip = new ZipFile(new File(jar.toURI()));
            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (!entry.isDirectory() && entry.getName().startsWith(RULES_PREFIX)) {
                    loadRule(shell, entry.getName(), zip.getInputStream(entry));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } finally {
            try {
                if (zip != null) zip.close();
            } catch (IOException e) {
            }
        }
    }
    private void loadRule(GroovyShell shell, String name, InputStream stream) {
        try {
            Script script = shell.parse(stream);
            mRules.add(new GroovyRule(name, script));
        } catch (Exception e) {
            System.err.println("Could not load rule " + name + ":");
            e.printStackTrace();
        } finally {
            IOUtilities.close(stream);
        }
    }
    public void addRule(Rule rule) {
        if (rule == null) {
            throw new IllegalArgumentException("A rule must be non-null");
        }
        mRules.add(rule);
    }
    public LayoutAnalysis analyze(File file) {
        if (file != null && file.exists()) {
            InputStream in = null;
            try {
                in = new FileInputStream(file);
                return analyze(file.getPath(), in);
            } catch (FileNotFoundException e) {
            } finally {
                IOUtilities.close(in);
            }
        }
        return LayoutAnalysis.ERROR;
    }
    public LayoutAnalysis analyze(InputStream stream) {
        return analyze("<unknown>", stream);
    }
    private LayoutAnalysis analyze(String name, InputStream stream) {
         try {
             Document document = mBuilder.parse(stream);
             return analyze(name, document);
         } catch (SAXException e) {
         } catch (IOException e) {
         }
         return LayoutAnalysis.ERROR;
    }
    public LayoutAnalysis analyze(String content) {
         return analyze("<unknown>", content);
    }
    public LayoutAnalysis analyze(String name, String content) {
         try {
             Document document = mBuilder.parse(content);
             return analyze(name, document);
         } catch (SAXException e) {
         } catch (IOException e) {
         }
         return LayoutAnalysis.ERROR;
    }
    public LayoutAnalysis analyze(Document document) {
        return analyze("<unknown>", document);
    }
    public LayoutAnalysis analyze(String name, Document document) {
        LayoutAnalysis analysis = new LayoutAnalysis(name);
        try {
            Element root = document.getDocumentElement();
            analyze(analysis, root);
        } finally {
            analysis.validate();
        }
        return analysis;        
    }
    private void analyze(LayoutAnalysis analysis, Node node) {
        NodeList list = node.getChildNodes();
        int count = list.getLength();
        applyRules(analysis, node);
        for (int i = 0; i < count; i++) {
            Node child = list.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                analyze(analysis, child);
            }
        }
    }
    private void applyRules(LayoutAnalysis analysis, Node node) {
        analysis.setCurrentNode(node);
        for (Rule rule : mRules) {
            rule.run(analysis, node);
        }
    }
}
