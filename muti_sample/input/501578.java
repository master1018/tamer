public class SampleProjectTest extends SdkTestCase {
    private static final Logger sLogger = Logger.getLogger(SampleProjectTest.class.getName());
    public void testSamples() throws CoreException {
        IAndroidTarget[] targets = getSdk().getTargets();
        for (IAndroidTarget target : targets) {
            doTestSamplesForTarget(target);
        }
    }
    private void doTestSamplesForTarget(IAndroidTarget target) throws CoreException {
        String path = target.getPath(IAndroidTarget.SAMPLES);
        File samples = new File(path);
        if (samples.isDirectory()) {
            File[] files = samples.listFiles();
            for (File file : files) {
                if (file.isDirectory()) {
                    doTestSampleProject(file.getName(), file.getAbsolutePath(), target);
                }
            }
        }
    }
    private void doTestSampleProject(String name, String path, IAndroidTarget target)
             throws CoreException {
        IProject iproject = null;
        try {
            sLogger.log(Level.INFO, String.format("Testing sample %s for target %s", name,
                    target.getName()));
            prepareProject(path, target);
            final StubProjectWizard newProjCreator = new StubProjectWizard(
                    name, path, target);
            newProjCreator.init(null, null);
            Display.getDefault().syncExec(new Runnable() {
                public void run() {
                    newProjCreator.performFinish();
                }
            });
            iproject = validateProjectExists(name);
            validateNoProblems(iproject);
        }
        catch (CoreException e) {
            sLogger.log(Level.SEVERE,
                    String.format("Unexpected exception when creating sample project %s " +
                            "for target %s", name, target.getName()));
            throw e;
        } finally {
            if (iproject != null) {
                iproject.delete(false, true, new NullProgressMonitor());
            }
        }
    }
    private void prepareProject(String path, IAndroidTarget target) {
        if (target.getVersion().isPreview()) {
            final String manifestPath = path + File.separatorChar +
                    AndroidConstants.FN_ANDROID_MANIFEST;
            AndroidManifestWriter manifestWriter =
                AndroidManifestWriter.parse(manifestPath);
            assertNotNull(String.format("could not read manifest %s", manifestPath),
                    manifestWriter);
            assertTrue(manifestWriter.setMinSdkVersion(target.getVersion().getApiString()));
        }
    }
    private IProject validateProjectExists(String name) {
        IProject iproject = getIProject(name);
        assertTrue(String.format("%s project not created", name), iproject.exists());
        assertTrue(String.format("%s project not opened", name), iproject.isOpen());
        return iproject;
    }
    private IProject getIProject(String name) {
        IProject iproject = ResourcesPlugin.getWorkspace().getRoot()
                .getProject(name);
        return iproject;
    }
    private void validateNoProblems(IProject iproject) throws CoreException {
        waitForBuild(iproject);
        boolean hasErrors = false;
        StringBuilder failureBuilder = new StringBuilder(String.format("%s project has errors:",
                iproject.getName()));
        IMarker[] markers = iproject.findMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE);
        if (markers != null && markers.length > 0) {
            for (IMarker m : markers) {
                int s = m.getAttribute(IMarker.SEVERITY, -1);
                if (s == IMarker.SEVERITY_ERROR) {
                    hasErrors = true;
                    failureBuilder.append("\n");
                    failureBuilder.append(m.getAttribute(IMarker.MESSAGE, ""));
                }
            }
        }
        assertFalse(failureBuilder.toString(), hasErrors);
    }
    private void waitForBuild(final IProject iproject) {
        final BuiltProjectDeltaVisitor deltaVisitor = new BuiltProjectDeltaVisitor(iproject);
        IResourceChangeListener newBuildListener = new IResourceChangeListener() {
            public void resourceChanged(IResourceChangeEvent event) {
                try {
                    event.getDelta().accept(deltaVisitor);
                }
                catch (CoreException e) {
                    fail();
                }
            }
        };
        iproject.getWorkspace().addResourceChangeListener(newBuildListener,
          IResourceChangeEvent.POST_BUILD);
        final int maxWait = 1200;
        for (int i=0; i < maxWait; i++) {
            if (deltaVisitor.isProjectBuilt()) {
                return;
            }
            try {
                Thread.sleep(50);
            }
            catch (InterruptedException e) {
            }
           if (Display.getCurrent() != null) {
               Display.getCurrent().readAndDispatch();
           }
        }
        sLogger.log(Level.SEVERE, "expected build event never happened?");
        fail(String.format("Expected build event never happened for %s", iproject.getName()));
    }
    private class BuiltProjectDeltaVisitor implements IResourceDeltaVisitor {
        private IProject mIProject;
        private boolean  mIsBuilt;
        public BuiltProjectDeltaVisitor(IProject iproject) {
            mIProject = iproject;
            mIsBuilt = false;
        }
        public boolean visit(IResourceDelta delta) {
            if (mIProject.equals(delta.getResource())) {
                setBuilt(true);
                return false;
            }
            return true;
        }
        private synchronized void setBuilt(boolean b) {
            mIsBuilt = b;
        }
        public synchronized boolean isProjectBuilt() {
            return mIsBuilt;
        }
    }
}
