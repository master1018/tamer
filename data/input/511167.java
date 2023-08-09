public class UpdaterLogicTest extends TestCase {
    private static class MockUpdaterLogic extends UpdaterLogic {
        private final Package[] mRemotePackages;
        public MockUpdaterLogic(Package[] remotePackages) {
            mRemotePackages = remotePackages;
        }
        @Override
        protected void fetchRemotePackages(ArrayList<Package> remotePkgs,
                RepoSource[] remoteSources) {
            if (mRemotePackages != null) {
                remotePkgs.addAll(Arrays.asList(mRemotePackages));
            }
        }
    }
    public void testFindAddonDependency() throws Exception {
        MockUpdaterLogic mul = new MockUpdaterLogic(null);
        MockPlatformPackage p1 = new MockPlatformPackage(1, 1);
        MockPlatformPackage p2 = new MockPlatformPackage(2, 1);
        MockAddonPackage a1 = new MockAddonPackage(p1, 1);
        MockAddonPackage a2 = new MockAddonPackage(p2, 2);
        ArrayList<ArchiveInfo> out = new ArrayList<ArchiveInfo>();
        ArrayList<Archive> selected = new ArrayList<Archive>();
        ArrayList<Package> remote = new ArrayList<Package>();
        Package[] localPkgs = { p1, a1 };
        ArchiveInfo[] locals = mul.createLocalArchives(localPkgs);
        RepoSource[] sources = null;
        ArchiveInfo fai = mul.findPlatformDependency(a2, out, selected, remote, sources, locals);
        assertNotNull(fai);
        assertNull(fai.getNewArchive());
        assertTrue(fai.isRejected());
        assertEquals(0, out.size());
        Archive p2_archive = p2.getArchives()[0];
        selected.add(p2_archive);
        ArchiveInfo ai2 = mul.findPlatformDependency(a2, out, selected, remote, sources, locals);
        assertNotNull(ai2);
        assertSame(p2_archive, ai2.getNewArchive());
        assertEquals(1, out.size());
        assertSame(p2_archive, out.get(0).getNewArchive());
    }
    public void testFindPlatformDependency() throws Exception {
        MockUpdaterLogic mul = new MockUpdaterLogic(null);
        MockToolPackage t1 = new MockToolPackage(1);
        MockToolPackage t2 = new MockToolPackage(2);
        MockPlatformPackage p2 = new MockPlatformPackage(2, 1, 2);
        ArrayList<ArchiveInfo> out = new ArrayList<ArchiveInfo>();
        ArrayList<Archive> selected = new ArrayList<Archive>();
        ArrayList<Package> remote = new ArrayList<Package>();
        Package[] localPkgs = { t1 };
        ArchiveInfo[] locals = mul.createLocalArchives(localPkgs);
        RepoSource[] sources = null;
        ArchiveInfo fai = mul.findToolsDependency(p2, out, selected, remote, sources, locals);
        assertNotNull(fai);
        assertNull(fai.getNewArchive());
        assertTrue(fai.isRejected());
        assertEquals(0, out.size());
        Archive t2_archive = t2.getArchives()[0];
        selected.add(t2_archive);
        ArchiveInfo ai2 = mul.findToolsDependency(p2, out, selected, remote, sources, locals);
        assertNotNull(ai2);
        assertSame(t2_archive, ai2.getNewArchive());
        assertEquals(1, out.size());
        assertSame(t2_archive, out.get(0).getNewArchive());
    }
}
