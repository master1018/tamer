public class RulesEngine {
    private static final String FD_GSCRIPTS = "gscripts";                       
    private static final String SCRIPT_EXT = ".groovy";                         
    private static final String SCRIPT_PACKAGE = "com.android.adt.gscripts";    
    private final GroovyClassLoader mClassLoader;
    private final IProject mProject;
    private final Map<Object, IViewRule> mRulesCache = new HashMap<Object, IViewRule>();
    private ProjectFolderListener mProjectFolderListener;
    public RulesEngine(IProject project) {
        mProject = project;
        ClassLoader cl = getClass().getClassLoader();
        CompilerConfiguration cc = new CompilerConfiguration();
        cc.setDefaultScriptExtension(SCRIPT_EXT);
        mClassLoader = new GreGroovyClassLoader(cl, cc);
        IResource f = project.findMember(FD_GSCRIPTS);
        if ((f instanceof IFolder) && f.exists()) {
            URI uri = ((IFolder) f).getLocationURI();
            try {
                URL url = uri.toURL();
                mClassLoader.addURL(url);
            } catch (MalformedURLException e) {
            }
        }
        mProjectFolderListener = new ProjectFolderListener();
        GlobalProjectMonitor.getMonitor().addFolderListener(
                mProjectFolderListener,
                IResourceDelta.ADDED | IResourceDelta.REMOVED | IResourceDelta.CHANGED);
    }
    public void dispose() {
        if (mProjectFolderListener != null) {
            GlobalProjectMonitor.getMonitor().removeFolderListener(mProjectFolderListener);
            mProjectFolderListener = null;
        }
        clearCache();
    }
    public String callGetDisplayName(UiViewElementNode element) {
        IViewRule rule = loadRule(element);
        if (rule != null) {
            try {
                return rule.getDisplayName();
            } catch (Exception e) {
                logError("%s.getDisplayName() failed: %s",
                        rule.getClass().getSimpleName(),
                        e.toString());
            }
        }
        return null;
    }
    public void callOnSelected(IGraphics gc, NodeProxy selectedNode,
            String displayName, boolean isMultipleSelection) {
        IViewRule rule = loadRule(selectedNode.getNode());
        if (rule != null) {
            try {
                rule.onSelected(gc, selectedNode, displayName, isMultipleSelection);
            } catch (Exception e) {
                logError("%s.onSelected() failed: %s",
                        rule.getClass().getSimpleName(),
                        e.toString());
            }
        }
    }
    public void callOnChildSelected(IGraphics gc, NodeProxy parentNode, NodeProxy childNode) {
        IViewRule rule = loadRule(parentNode.getNode());
        if (rule != null) {
            try {
                rule.onChildSelected(gc, parentNode, childNode);
            } catch (Exception e) {
                logError("%s.onChildSelected() failed: %s",
                        rule.getClass().getSimpleName(),
                        e.toString());
            }
        }
    }
    public DropFeedback callOnDropEnter(NodeProxy targetNode, String fqcn) {
        IViewRule rule = loadRule(targetNode.getNode());
        if (rule != null) {
            try {
                return rule.onDropEnter(targetNode, fqcn);
            } catch (Exception e) {
                logError("%s.onDropEnter() failed: %s",
                        rule.getClass().getSimpleName(),
                        e.toString());
            }
        }
        return null;
    }
    public DropFeedback callOnDropMove(NodeProxy targetNode,
            String fqcn,
            DropFeedback feedback,
            Point where) {
        IViewRule rule = loadRule(targetNode.getNode());
        if (rule != null) {
            try {
                return rule.onDropMove(targetNode, fqcn, feedback, where);
            } catch (Exception e) {
                logError("%s.onDropMove() failed: %s",
                        rule.getClass().getSimpleName(),
                        e.toString());
            }
        }
        return null;
    }
    public void callOnDropLeave(NodeProxy targetNode, String fqcn, DropFeedback feedback) {
        IViewRule rule = loadRule(targetNode.getNode());
        if (rule != null) {
            try {
                rule.onDropLeave(targetNode, fqcn, feedback);
            } catch (Exception e) {
                logError("%s.onDropLeave() failed: %s",
                        rule.getClass().getSimpleName(),
                        e.toString());
            }
        }
    }
    public void callOnDropped(NodeProxy targetNode,
            String fqcn,
            DropFeedback feedback,
            Point where) {
        IViewRule rule = loadRule(targetNode.getNode());
        if (rule != null) {
            try {
                rule.onDropped(targetNode, fqcn, feedback, where);
            } catch (Exception e) {
                logError("%s.onDropped() failed: %s",
                        rule.getClass().getSimpleName(),
                        e.toString());
            }
        }
    }
    public void callDropFeedbackPaint(IGraphics gc, NodeProxy targetNode, DropFeedback feedback) {
        if (gc != null && feedback != null && feedback.paintClosure != null) {
            try {
                feedback.paintClosure.call(new Object[] { gc, targetNode, feedback });
            } catch (Exception e) {
                logError("DropFeedback.paintClosure failed: %s",
                        e.toString());
            }
        }
    }
    private class ProjectFolderListener implements IFolderListener {
        public void folderChanged(IFolder folder, int kind) {
            if (folder.getProject() == mProject &&
                    FD_GSCRIPTS.equals(folder.getName())) {
                clearCache();
            }
        }
    }
    private void clearCache() {
        HashSet<IViewRule> rules = new HashSet<IViewRule>(mRulesCache.values());
        mRulesCache.clear();
        for (IViewRule rule : rules) {
            if (rule != null) {
                try {
                    rule.onDispose();
                } catch (Exception e) {
                    logError("%s.onDispose() failed: %s",
                            rule.getClass().getSimpleName(),
                            e.toString());
                }
            }
        }
    }
    private IViewRule loadRule(UiViewElementNode element) {
        if (element == null) {
            return null;
        } else {
            ElementDescriptor d = element.getDescriptor();
            if (d == null || !(d instanceof ViewElementDescriptor)) {
                return null;
            }
        }
        String targetFqcn = null;
        ViewElementDescriptor targetDesc = (ViewElementDescriptor) element.getDescriptor();
        IViewRule rule = mRulesCache.get(targetDesc);
        if (rule != null || mRulesCache.containsKey(targetDesc)) {
            return rule;
        }
        for (ViewElementDescriptor desc = targetDesc;
                desc != null;
                desc = desc.getSuperClassDesc()) {
            String fqcn = desc.getFullClassName();
            if (fqcn == null) {
                return null;
            }
            if (targetFqcn == null) {
                targetFqcn = fqcn;
            }
            rule = loadRule(fqcn, targetFqcn);
            if (rule != null) {
                return rule;
            }
        }
        mRulesCache.put(targetDesc, null);
        return null;
    }
    private IViewRule loadRule(String realFqcn, String targetFqcn) {
        if (realFqcn == null || targetFqcn == null) {
            return null;
        }
        IViewRule rule = mRulesCache.get(realFqcn);
        if (rule != null || mRulesCache.containsKey(realFqcn)) {
            return rule;
        }
        String filename = realFqcn + SCRIPT_EXT;
        try {
            InputStream is = AdtPlugin.readEmbeddedFileAsStream(
                    FD_GSCRIPTS + AndroidConstants.WS_SEP + filename);
            rule = loadStream(is, realFqcn, "ADT");     
            if (rule != null) {
                return initializeRule(rule, targetFqcn);
            }
        } catch (Exception e) {
            logError("load rule error (%s): %s", filename, e.toString());
        }
        IResource r = mProject.findMember(FD_GSCRIPTS);
        if (r != null && r.getType() == IResource.FOLDER) {
            r = ((IFolder) r).findMember(filename);
            if (r != null && r.getType() == IResource.FILE) {
                try {
                    InputStream is = ((IFile) r).getContents();
                    rule = loadStream(is, realFqcn, mProject.getName());
                    if (rule != null) {
                        return initializeRule(rule, targetFqcn);
                    }
                } catch (Exception e) {
                    logError("load rule error (%s): %s", filename, e.getMessage());
                }
            }
        }
        mRulesCache.put(realFqcn, null);
        return null;
    }
    private IViewRule initializeRule(IViewRule rule, String targetFqcn) {
        try {
            if (rule.onInitialize(targetFqcn)) {
                mRulesCache.put(targetFqcn, rule);
                return rule;
            } else {
                rule.onDispose();
            }
        } catch (Exception e) {
            logError("%s.onInit() failed: %s",
                    rule.getClass().getSimpleName(),
                    e.toString());
        }
        return null;
    }
    private IViewRule loadStream(InputStream is, String fqcn, String codeBase) {
        try {
            if (is == null) {
                return null;
            }
            InputStreamReader reader = new InputStreamReader(is, Charset.forName("UTF-8"));
            GroovyCodeSource source = new GroovyCodeSource(reader, fqcn, codeBase);
            Class<?> c = mClassLoader.parseClass(source);
            return (IViewRule) c.newInstance();
        } catch (CompilationFailedException e) {
            logError("Compilation error in %1$s:%2$s.groovy: %3$s", codeBase, fqcn, e.toString());
        } catch (ClassCastException e) {
            logError("Script %1$s:%2$s.groovy does not implement IViewRule", codeBase, fqcn);
        } catch (Exception e) {
            logError("Failed to use %1$s:%2$s.groovy: %3$s", codeBase, fqcn, e.toString());
        }
        return null;
    }
    private void logError(String format, Object...params) {
        String s = String.format(format, params);
        AdtPlugin.printErrorToConsole(mProject, s);
    }
    private static class GreGroovyClassLoader extends GroovyClassLoader {
        public GreGroovyClassLoader(ClassLoader cl, CompilerConfiguration cc) {
            super(cl, cc);
            final GroovyResourceLoader resLoader = getResourceLoader();
            setResourceLoader(new GroovyResourceLoader() {
                public URL loadGroovySource(String filename) throws MalformedURLException {
                    URL url = resLoader.loadGroovySource(filename);
                    if (url == null) {
                        String p = SCRIPT_PACKAGE + ".";      
                        if (filename.startsWith(p)) {
                            filename = filename.substring(p.length());
                            url = AdtPlugin.getEmbeddedFileUrl(
                                    AndroidConstants.WS_SEP +
                                    FD_GSCRIPTS +
                                    AndroidConstants.WS_SEP +
                                    filename +
                                    SCRIPT_EXT);
                        }
                    }
                    return url;
                }
            });
        }
        @Override
        protected CompilationUnit createCompilationUnit(
                CompilerConfiguration config,
                CodeSource source) {
            return new GreCompilationUnit(config, source, this);
        }
    }
    private static class GreCompilationUnit extends CompilationUnit {
        public GreCompilationUnit(
                CompilerConfiguration config,
                CodeSource source,
                GroovyClassLoader loader) {
            super(config, source, loader);
            SourceUnitOperation op = new SourceUnitOperation() {
                @Override
                public void call(SourceUnit source) throws CompilationFailedException {
                    String p = IViewRule.class.getPackage().getName();
                    source.getAST().addStarImport(p + ".");  
                }
            };
            addPhaseOperation(op, Phases.CONVERSION);
        }
    }
}
