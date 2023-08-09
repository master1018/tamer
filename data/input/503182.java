abstract class BaseBuilder extends IncrementalProjectBuilder {
    private final static Pattern sPattern0Line1 = Pattern.compile(
            "^\\s+\\(skipping hidden file\\s'(.*)'\\)$"); 
    private final static Pattern sPattern1Line1 = Pattern.compile(
            "^ERROR\\s+at\\s+line\\s+(\\d+):\\s+(.*)$"); 
    private final static Pattern sPattern1Line2 = Pattern.compile(
            "^\\s+\\(Occurred while parsing\\s+(.*)\\)$");  
    private final static Pattern sPattern2Line1 = Pattern.compile(
            "^ERROR:\\s+(.+)$"); 
    private final static Pattern sPattern2Line2 = Pattern.compile(
            "Defined\\s+at\\s+file\\s+(.+)\\s+line\\s+(\\d+)"); 
    private final static Pattern sPattern3Line1 = Pattern.compile(
            "^(.+)\\sline\\s(\\d+):\\s(.+)$"); 
    private final static Pattern sPattern4Line1 = Pattern.compile(
            "^Error\\s+parsing\\s+XML\\s+file\\s(.+)$"); 
    private final static Pattern sPattern4Line2 = Pattern.compile(
            "^(.+)\\s+at\\s+line\\s+(\\d+)$"); 
    private final static Pattern sPattern5Line1 = Pattern.compile(
            "^(.+?):(\\d+):\\s+WARNING:(.+)$"); 
    private final static Pattern sPattern6Line1 = Pattern.compile(
            "^(.+?):(\\d+):\\s+(.+)$"); 
    private final static Pattern sPattern7Line1 = Pattern.compile(
            "^ERROR:\\s+9-patch\\s+image\\s+(.+)\\s+malformed\\.$"); 
    private final static Pattern sPattern8Line1 = Pattern.compile(
            "^(invalid resource directory name): (.*)$"); 
    private final static Pattern sPattern9Line1 = Pattern.compile(
            "^Invalid configuration: (.+)$"); 
    private SAXParserFactory mParserFactory;
    protected static class BaseDeltaVisitor implements XmlErrorListener {
        protected BaseBuilder mBuilder;
        public boolean mXmlError = false;
        public BaseDeltaVisitor(BaseBuilder builder) {
            mBuilder = builder;
        }
        protected static String[] findMatchingSourceFolder(ArrayList<IPath> sourceFolders,
                String[] pathSegments) {
            for (IPath p : sourceFolders) {
                String[] srcSegments = p.segments();
                boolean valid = true;
                int segmentCount = pathSegments.length;
                for (int i = 0 ; i < segmentCount; i++) {
                    String s1 = pathSegments[i];
                    String s2 = srcSegments[i];
                    if (s1.equalsIgnoreCase(s2) == false) {
                        valid = false;
                        break;
                    }
                }
                if (valid) {
                    return srcSegments;
                }
            }
            return null;
        }
        public void errorFound() {
            mXmlError = true;
        }
    }
    public BaseBuilder() {
        super();
        mParserFactory = SAXParserFactory.newInstance();
        mParserFactory.setNamespaceAware(false);
    }
    protected final void checkXML(IResource resource, BaseDeltaVisitor visitor) {
        if (resource instanceof IFile) {
            IFile file = (IFile)resource;
            removeMarkersFromFile(file, AndroidConstants.MARKER_XML);
            XmlErrorHandler reporter = new XmlErrorHandler(file, visitor);
            try {
                getParser().parse(file.getContents(), reporter);
            } catch (Exception e1) {
            }
        }
    }
    protected final SAXParser getParser() throws ParserConfigurationException,
            SAXException {
        return mParserFactory.newSAXParser();
    }
    protected final IMarker markProject(String markerId, String message, int severity) {
        return BaseProjectHelper.markResource(getProject(), markerId, message, severity);
    }
    protected final void removeMarkersFromFile(IFile file, String markerId) {
        try {
            if (file.exists()) {
                file.deleteMarkers(markerId, true, IResource.DEPTH_ZERO);
            }
        } catch (CoreException ce) {
            String msg = String.format(Messages.Marker_Delete_Error, markerId, file.toString());
            AdtPlugin.printErrorToConsole(getProject(), msg);
        }
    }
    protected final void removeMarkersFromContainer(IContainer folder, String markerId) {
        try {
            if (folder.exists()) {
                folder.deleteMarkers(markerId, true, IResource.DEPTH_INFINITE);
            }
        } catch (CoreException ce) {
            String msg = String.format(Messages.Marker_Delete_Error, markerId, folder.toString());
            AdtPlugin.printErrorToConsole(getProject(), msg);
        }
    }
    protected final static void removeMarkersFromProject(IProject project,
            String markerId) {
        try {
            if (project.exists()) {
                project.deleteMarkers(markerId, true, IResource.DEPTH_INFINITE);
            }
        } catch (CoreException ce) {
            String msg = String.format(Messages.Marker_Delete_Error, markerId, project.getName());
            AdtPlugin.printErrorToConsole(project, msg);
        }
    }
    protected final int grabProcessOutput(final Process process,
            final ArrayList<String> results)
            throws InterruptedException {
        new Thread("") { 
            @Override
            public void run() {
                InputStreamReader is = new InputStreamReader(process.getErrorStream());
                BufferedReader errReader = new BufferedReader(is);
                try {
                    while (true) {
                        String line = errReader.readLine();
                        if (line != null) {
                            results.add(line);
                        } else {
                            break;
                        }
                    }
                } catch (IOException e) {
                }
            }
        }.start();
        new Thread("") { 
            @Override
            public void run() {
                InputStreamReader is = new InputStreamReader(process.getInputStream());
                BufferedReader outReader = new BufferedReader(is);
                IProject project = getProject();
                try {
                    while (true) {
                        String line = outReader.readLine();
                        if (line != null) {
                            AdtPlugin.printBuildToConsole(BuildVerbosity.VERBOSE,
                                    project, line);
                        } else {
                            break;
                        }
                    }
                } catch (IOException e) {
                }
            }
        }.start();
        return process.waitFor();
    }
    protected final boolean parseAaptOutput(ArrayList<String> results,
            IProject project) {
        if (results.size() == 0) {
            return false;
        }
        String osRoot = project.getLocation().toOSString();
        Matcher m;
        for (int i = 0; i < results.size(); i++) {
            String p = results.get(i);
            m = sPattern0Line1.matcher(p);
            if (m.matches()) {
                continue;
            }
            m = sPattern1Line1.matcher(p);
            if (m.matches()) {
                String lineStr = m.group(1);
                String msg = m.group(2);
                m = getNextLineMatcher(results, ++i, sPattern1Line2);
                if (m == null) {
                    return true;
                }
                String location = m.group(1);
                if (checkAndMark(location, lineStr, msg, osRoot, project,
                        AndroidConstants.MARKER_AAPT_COMPILE, IMarker.SEVERITY_ERROR) == false) {
                    return true;
                }
                continue;
            }
            m = sPattern7Line1.matcher(p);
            if (m.matches()) {
                String location = m.group(1);
                String msg = p; 
                if (++i < results.size()) {
                    msg = results.get(i).trim();
                    if (++i < results.size()) {
                        msg = msg + " - " + results.get(i).trim(); 
                        i++;
                    }
                }
                if (checkAndMark(location, null, msg, osRoot, project,
                        AndroidConstants.MARKER_AAPT_COMPILE, IMarker.SEVERITY_ERROR) == false) {
                    return true;
                }
                continue;
            }
            m =  sPattern2Line1.matcher(p);
            if (m.matches()) {
                String msg = m.group(1);
                m = getNextLineMatcher(results, ++i, sPattern2Line2);
                if (m == null) {
                    return true;
                }
                String location = m.group(1);
                String lineStr = m.group(2);
                if (checkAndMark(location, lineStr, msg, osRoot, project,
                        AndroidConstants.MARKER_AAPT_COMPILE, IMarker.SEVERITY_ERROR) == false) {
                    return true;
                }
                continue;
            }
            m = sPattern3Line1.matcher(p);
            if (m.matches()) {
                String location = m.group(1);
                String lineStr = m.group(2);
                String msg = m.group(3);
                if (checkAndMark(location, lineStr, msg, osRoot, project,
                        AndroidConstants.MARKER_AAPT_COMPILE, IMarker.SEVERITY_ERROR) == false) {
                    return true;
                }
                continue;
            }
            m = sPattern4Line1.matcher(p);
            if (m.matches()) {
                String location = m.group(1);
                m = getNextLineMatcher(results, ++i, sPattern4Line2);
                if (m == null) {
                    return true;
                }
                String msg = m.group(1);
                String lineStr = m.group(2);
                if (checkAndMark(location, lineStr, msg, osRoot, project,
                        AndroidConstants.MARKER_AAPT_COMPILE, IMarker.SEVERITY_ERROR) == false) {
                    return true;
                }
                continue;
            }
            m = sPattern5Line1.matcher(p);
            if (m.matches()) {
                String location = m.group(1);
                String lineStr = m.group(2);
                String msg = m.group(3);
                if (checkAndMark(location, lineStr, msg, osRoot, project,
                        AndroidConstants.MARKER_AAPT_COMPILE, IMarker.SEVERITY_WARNING) == false) {
                    return true;
                }
                continue;
            }
            m = sPattern6Line1.matcher(p);
            if (m.matches()) {
                String location = m.group(1);
                String lineStr = m.group(2);
                String msg = m.group(3);
                if (checkAndMark(location, lineStr, msg, osRoot, project,
                        AndroidConstants.MARKER_AAPT_COMPILE, IMarker.SEVERITY_ERROR) == false) {
                    return true;
                }
                continue;
            }
            m = sPattern8Line1.matcher(p);
            if (m.matches()) {
                String location = m.group(2);
                String msg = m.group(1);
                if (checkAndMark(location, null, msg, osRoot, project,
                        AndroidConstants.MARKER_AAPT_COMPILE, IMarker.SEVERITY_ERROR) == false) {
                    return true;
                }
                continue;
            }
            m = sPattern9Line1.matcher(p);
            if (m.matches()) {
                String badConfig = m.group(1);
                String msg = String.format("APK Configuration filter '%1$s' is invalid", badConfig);
                i++;
                if (checkAndMark(null , null, msg, osRoot, project,
                        AndroidConstants.MARKER_AAPT_PACKAGE, IMarker.SEVERITY_ERROR) == false) {
                    return true;
                }
                continue;
            }
            return true;
        }
        return false;
    }
    protected boolean saveProjectStringProperty(String propertyName, String value) {
        IProject project = getProject();
        return ProjectHelper.saveStringProperty(project, propertyName, value);
    }
    protected String loadProjectStringProperty(String propertyName) {
        IProject project = getProject();
        return ProjectHelper.loadStringProperty(project, propertyName);
    }
    protected boolean saveProjectBooleanProperty(String propertyName, boolean value) {
        IProject project = getProject();
        return ProjectHelper.saveStringProperty(project, propertyName, Boolean.toString(value));
    }
    protected boolean loadProjectBooleanProperty(String propertyName, boolean defaultValue) {
        IProject project = getProject();
        return ProjectHelper.loadBooleanProperty(project, propertyName, defaultValue);
    }
    protected boolean saveProjectResourceProperty(String propertyName, IResource resource) {
        return ProjectHelper.saveResourceProperty(getProject(), propertyName, resource);
    }
    protected IResource loadProjectResourceProperty(String propertyName) {
        IProject project = getProject();
        return ProjectHelper.loadResourceProperty(project, propertyName);
    }
    private final  boolean checkAndMark(String location, String lineStr,
            String message, String root, IProject project, String markerId, int severity) {
        if (location != null) {
            File f = new File(location);
            if (f.exists() == false) {
                return false;
            }
        }
        int line = -1; 
        if (lineStr != null) {
            try {
                line = Integer.parseInt(lineStr);
            } catch (NumberFormatException e) {
                return false;
            }
        }
        IResource f2 = project;
        if (location != null) {
            f2 = getResourceFromFullPath(location, root, project);
            if (f2 == null) {
                return false;
            }
        }
        boolean markerAlreadyExists = false;
        try {
            IMarker[] markers = f2.findMarkers(markerId, true, IResource.DEPTH_ZERO);
            for (IMarker marker : markers) {
                int tmpLine = marker.getAttribute(IMarker.LINE_NUMBER, -1);
                if (tmpLine != line) {
                    break;
                }
                int tmpSeverity = marker.getAttribute(IMarker.SEVERITY, -1);
                if (tmpSeverity != severity) {
                    break;
                }
                String tmpMsg = marker.getAttribute(IMarker.MESSAGE, null);
                if (tmpMsg == null || tmpMsg.equals(message) == false) {
                    break;
                }
                markerAlreadyExists = true;
                break;
            }
        } catch (CoreException e) {
        }
        if (markerAlreadyExists == false) {
            BaseProjectHelper.markResource(f2, markerId, message, line, severity);
        }
        return true;
    }
    private final Matcher getNextLineMatcher(ArrayList<String> lines,
            int nextIndex, Pattern pattern) {
        if (nextIndex == lines.size()) {
            return null;
        }
        Matcher m = pattern.matcher(lines.get(nextIndex));
        if (m.matches()) {
           return m;
        }
        return null;
    }
    private IResource getResourceFromFullPath(String filename, String root,
            IProject project) {
        if (filename.startsWith(root)) {
            String file = filename.substring(root.length());
            IResource r = project.findMember(file);
            if (r.exists()) {
                return r;
            }
        }
        return null;
    }
    protected final String[] getExternalJars() {
        IProject project = getProject();
        IJavaProject javaProject = JavaCore.create(project);
        IWorkspaceRoot wsRoot = ResourcesPlugin.getWorkspace().getRoot();
        ArrayList<String> oslibraryList = new ArrayList<String>();
        IClasspathEntry[] classpaths = javaProject.readRawClasspath();
        if (classpaths != null) {
            for (IClasspathEntry e : classpaths) {
                if (e.getEntryKind() == IClasspathEntry.CPE_LIBRARY ||
                        e.getEntryKind() == IClasspathEntry.CPE_VARIABLE) {
                    if (e.getEntryKind() == IClasspathEntry.CPE_VARIABLE) {
                        e = JavaCore.getResolvedClasspathEntry(e);
                    }
                    IPath path = e.getPath();
                    if (AndroidConstants.EXT_JAR.equalsIgnoreCase(path.getFileExtension())) {
                        boolean local = false;
                        IResource resource = wsRoot.findMember(path);
                        if (resource != null && resource.exists() &&
                                resource.getType() == IResource.FILE) {
                            local = true;
                            oslibraryList.add(resource.getLocation().toOSString());
                        }
                        if (local == false) {
                            String osFullPath = path.toOSString();
                            File f = new File(osFullPath);
                            if (f.exists()) {
                                oslibraryList.add(osFullPath);
                            } else {
                                String message = String.format( Messages.Couldnt_Locate_s_Error,
                                        path);
                                AdtPlugin.printBuildToConsole(BuildVerbosity.VERBOSE,
                                        project, message);
                                markProject(AndroidConstants.MARKER_PACKAGING, message,
                                        IMarker.SEVERITY_WARNING);
                            }
                        }
                    }
                }
            }
        }
        return oslibraryList.toArray(new String[oslibraryList.size()]);
    }
    protected void abortOnBadSetup(IJavaProject javaProject) throws CoreException {
        IProject iProject = javaProject.getProject();
        Sdk sdk = Sdk.getCurrent();
        if (sdk == null) {
            stopBuild("SDK is not loaded yet");
        }
        IAndroidTarget target = sdk.getTarget(javaProject.getProject());
        if (target == null) {
            stopBuild("Project target not resolved yet.");
        }
        if (sdk.checkAndLoadTargetData(target, javaProject) != LoadStatus.LOADED) {
            stopBuild("Project target not loaded yet.");
       }
        IMarker[] markers = iProject.findMarkers(AndroidConstants.MARKER_TARGET,
                false , IResource.DEPTH_ZERO);
        if (markers.length > 0) {
            stopBuild("");
        }
        markers = iProject.findMarkers(AndroidConstants.MARKER_ADT, false ,
                IResource.DEPTH_ZERO);
        if (markers.length > 0) {
            stopBuild("");
        }
    }
    protected final void stopBuild(String error, Object... args) throws CoreException {
        throw new CoreException(new Status(IStatus.CANCEL, AdtPlugin.PLUGIN_ID,
                String.format(error, args)));
    }
    protected void removeDerivedResources(IResource resource, IProgressMonitor monitor)
            throws CoreException {
        if (resource.exists()) {
            if (resource.isDerived()) {
                resource.delete(true, new SubProgressMonitor(monitor, 10));
            } else if (resource.getType() == IResource.FOLDER) {
                IFolder folder = (IFolder)resource;
                IResource[] members = folder.members();
                for (IResource member : members) {
                    removeDerivedResources(member, monitor);
                }
            }
        }
    }
}
