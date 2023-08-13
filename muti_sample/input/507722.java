class UpdaterLogic {
    public ArrayList<ArchiveInfo> computeUpdates(
            Collection<Archive> selectedArchives,
            RepoSources sources,
            Package[] localPkgs) {
        ArrayList<ArchiveInfo> archives = new ArrayList<ArchiveInfo>();
        ArrayList<Package> remotePkgs = new ArrayList<Package>();
        RepoSource[] remoteSources = sources.getSources();
        ArchiveInfo[] localArchives = createLocalArchives(localPkgs);
        if (selectedArchives == null) {
            selectedArchives = findUpdates(localArchives, remotePkgs, remoteSources);
        }
        for (Archive a : selectedArchives) {
            insertArchive(a,
                    archives,
                    selectedArchives,
                    remotePkgs,
                    remoteSources,
                    localArchives,
                    false );
        }
        return archives;
    }
    public void addNewPlatforms(ArrayList<ArchiveInfo> archives,
            RepoSources sources,
            Package[] localPkgs) {
        ArchiveInfo[] localArchives = createLocalArchives(localPkgs);
        float currentPlatformScore = 0;
        float currentSampleScore = 0;
        float currentAddonScore = 0;
        float currentDocScore = 0;
        HashMap<String, Float> currentExtraScore = new HashMap<String, Float>();
        for (Package p : localPkgs) {
            int rev = p.getRevision();
            int api = 0;
            boolean isPreview = false;
            if (p instanceof IPackageVersion) {
                AndroidVersion vers = ((IPackageVersion) p).getVersion();
                api = vers.getApiLevel();
                isPreview = vers.isPreview();
            }
            float score = api * 10 + (isPreview ? 1 : 0) + rev/100.f;
            if (p instanceof PlatformPackage) {
                currentPlatformScore = Math.max(currentPlatformScore, score);
            } else if (p instanceof SamplePackage) {
                currentSampleScore = Math.max(currentSampleScore, score);
            } else if (p instanceof AddonPackage) {
                currentAddonScore = Math.max(currentAddonScore, score);
            } else if (p instanceof ExtraPackage) {
                currentExtraScore.put(((ExtraPackage) p).getPath(), score);
            } else if (p instanceof DocPackage) {
                currentDocScore = Math.max(currentDocScore, score);
            }
        }
        RepoSource[] remoteSources = sources.getSources();
        ArrayList<Package> remotePkgs = new ArrayList<Package>();
        fetchRemotePackages(remotePkgs, remoteSources);
        Package suggestedDoc = null;
        for (Package p : remotePkgs) {
            int rev = p.getRevision();
            int api = 0;
            boolean isPreview = false;
            if (p instanceof  IPackageVersion) {
                AndroidVersion vers = ((IPackageVersion) p).getVersion();
                api = vers.getApiLevel();
                isPreview = vers.isPreview();
            }
            float score = api * 10 + (isPreview ? 1 : 0) + rev/100.f;
            boolean shouldAdd = false;
            if (p instanceof PlatformPackage) {
                shouldAdd = score > currentPlatformScore;
            } else if (p instanceof SamplePackage) {
                shouldAdd = score > currentSampleScore;
            } else if (p instanceof AddonPackage) {
                shouldAdd = score > currentAddonScore;
            } else if (p instanceof ExtraPackage) {
                String key = ((ExtraPackage) p).getPath();
                shouldAdd = !currentExtraScore.containsKey(key) ||
                    score > currentExtraScore.get(key).floatValue();
            } else if (p instanceof DocPackage) {
                if (score > currentDocScore) {
                    suggestedDoc = p;
                    currentDocScore = score;
                }
            }
            if (shouldAdd) {
                for (Archive a : p.getArchives()) {
                    if (a.isCompatible()) {
                        insertArchive(a,
                                archives,
                                null ,
                                remotePkgs,
                                remoteSources,
                                localArchives,
                                true );
                    }
                }
            }
        }
        if (suggestedDoc != null) {
            for (Archive a : suggestedDoc.getArchives()) {
                if (a.isCompatible()) {
                    insertArchive(a,
                            archives,
                            null ,
                            remotePkgs,
                            remoteSources,
                            localArchives,
                            true );
                }
            }
        }
    }
    protected ArchiveInfo[] createLocalArchives(Package[] localPkgs) {
        if (localPkgs != null) {
            ArrayList<ArchiveInfo> list = new ArrayList<ArchiveInfo>();
            for (Package p : localPkgs) {
                for (Archive a : p.getArchives()) {
                    if (a != null && a.isCompatible()) {
                        list.add(new LocalArchiveInfo(a));
                    }
                }
            }
            return list.toArray(new ArchiveInfo[list.size()]);
        }
        return new ArchiveInfo[0];
    }
    private Collection<Archive> findUpdates(ArchiveInfo[] localArchives,
            ArrayList<Package> remotePkgs,
            RepoSource[] remoteSources) {
        ArrayList<Archive> updates = new ArrayList<Archive>();
        fetchRemotePackages(remotePkgs, remoteSources);
        for (ArchiveInfo ai : localArchives) {
            Archive na = ai.getNewArchive();
            if (na == null) {
                continue;
            }
            Package localPkg = na.getParentPackage();
            for (Package remotePkg : remotePkgs) {
                if (localPkg.canBeUpdatedBy(remotePkg) == UpdateInfo.UPDATE) {
                    for (Archive a : remotePkg.getArchives()) {
                        if (a.isCompatible()) {
                            updates.add(a);
                            break;
                        }
                    }
                }
            }
        }
        return updates;
    }
    private ArchiveInfo insertArchive(Archive archive,
            ArrayList<ArchiveInfo> outArchives,
            Collection<Archive> selectedArchives,
            ArrayList<Package> remotePkgs,
            RepoSource[] remoteSources,
            ArchiveInfo[] localArchives,
            boolean automated) {
        Package p = archive.getParentPackage();
        Archive updatedArchive = null;
        for (ArchiveInfo ai : localArchives) {
            Archive a = ai.getNewArchive();
            if (a != null) {
                Package lp = a.getParentPackage();
                if (lp.canBeUpdatedBy(p) == UpdateInfo.UPDATE) {
                    updatedArchive = a;
                }
            }
        }
        ArchiveInfo[] deps = findDependency(p,
                outArchives,
                selectedArchives,
                remotePkgs,
                remoteSources,
                localArchives);
        ArchiveInfo ai = null;
        for (ArchiveInfo ai2 : outArchives) {
            Archive a2 = ai2.getNewArchive();
            if (a2 != null && a2.getParentPackage().sameItemAs(archive.getParentPackage())) {
                ai = ai2;
                break;
            }
        }
        if (ai == null) {
            ai = new ArchiveInfo(
                archive,        
                updatedArchive, 
                deps            
                );
            outArchives.add(ai);
        }
        if (deps != null) {
            for (ArchiveInfo d : deps) {
                d.addDependencyFor(ai);
            }
        }
        return ai;
    }
    private ArchiveInfo[] findDependency(Package pkg,
            ArrayList<ArchiveInfo> outArchives,
            Collection<Archive> selectedArchives,
            ArrayList<Package> remotePkgs,
            RepoSource[] remoteSources,
            ArchiveInfo[] localArchives) {
        ArrayList<ArchiveInfo> list = new ArrayList<ArchiveInfo>();
        if (pkg instanceof IPlatformDependency) {
            ArchiveInfo ai = findPlatformDependency(
                    (IPlatformDependency) pkg,
                    outArchives,
                    selectedArchives,
                    remotePkgs,
                    remoteSources,
                    localArchives);
            if (ai != null) {
                list.add(ai);
            }
        }
        if (pkg instanceof IMinToolsDependency) {
            ArchiveInfo ai = findToolsDependency(
                    (IMinToolsDependency) pkg,
                    outArchives,
                    selectedArchives,
                    remotePkgs,
                    remoteSources,
                    localArchives);
            if (ai != null) {
                list.add(ai);
            }
        }
        if (pkg instanceof IMinApiLevelDependency) {
            ArchiveInfo ai = findMinApiLevelDependency(
                    (IMinApiLevelDependency) pkg,
                    outArchives,
                    selectedArchives,
                    remotePkgs,
                    remoteSources,
                    localArchives);
            if (ai != null) {
                list.add(ai);
            }
        }
        if (list.size() > 0) {
            return list.toArray(new ArchiveInfo[list.size()]);
        }
        return null;
    }
    protected ArchiveInfo findToolsDependency(
            IMinToolsDependency pkg,
            ArrayList<ArchiveInfo> outArchives,
            Collection<Archive> selectedArchives,
            ArrayList<Package> remotePkgs,
            RepoSource[] remoteSources,
            ArchiveInfo[] localArchives) {
        int rev = pkg.getMinToolsRevision();
        if (rev == MinToolsPackage.MIN_TOOLS_REV_NOT_SPECIFIED) {
            return null;
        }
        for (ArchiveInfo ai : localArchives) {
            Archive a = ai.getNewArchive();
            if (a != null) {
                Package p = a.getParentPackage();
                if (p instanceof ToolPackage) {
                    if (((ToolPackage) p).getRevision() >= rev) {
                        return null;
                    }
                }
            }
        }
        for (ArchiveInfo ai : outArchives) {
            Archive a = ai.getNewArchive();
            if (a != null) {
                Package p = a.getParentPackage();
                if (p instanceof ToolPackage) {
                    if (((ToolPackage) p).getRevision() >= rev) {
                        return ai;
                    }
                }
            }
        }
        if (selectedArchives != null) {
            for (Archive a : selectedArchives) {
                Package p = a.getParentPackage();
                if (p instanceof ToolPackage) {
                    if (((ToolPackage) p).getRevision() >= rev) {
                        return insertArchive(a,
                                outArchives,
                                selectedArchives,
                                remotePkgs,
                                remoteSources,
                                localArchives,
                                true );
                    }
                }
            }
        }
        fetchRemotePackages(remotePkgs, remoteSources);
        for (Package p : remotePkgs) {
            if (p instanceof ToolPackage) {
                if (((ToolPackage) p).getRevision() >= rev) {
                    for (Archive a : p.getArchives()) {
                        if (a.isCompatible()) {
                            return insertArchive(a,
                                    outArchives,
                                    selectedArchives,
                                    remotePkgs,
                                    remoteSources,
                                    localArchives,
                                    true );
                        }
                    }
                }
            }
        }
        return new MissingToolArchiveInfo(rev);
    }
    protected ArchiveInfo findPlatformDependency(
            IPlatformDependency pkg,
            ArrayList<ArchiveInfo> outArchives,
            Collection<Archive> selectedArchives,
            ArrayList<Package> remotePkgs,
            RepoSource[] remoteSources,
            ArchiveInfo[] localArchives) {
        AndroidVersion v = pkg.getVersion();
        for (ArchiveInfo ai : localArchives) {
            Archive a = ai.getNewArchive();
            if (a != null) {
                Package p = a.getParentPackage();
                if (p instanceof PlatformPackage) {
                    if (v.equals(((PlatformPackage) p).getVersion())) {
                        return null;
                    }
                }
            }
        }
        for (ArchiveInfo ai : outArchives) {
            Archive a = ai.getNewArchive();
            if (a != null) {
                Package p = a.getParentPackage();
                if (p instanceof PlatformPackage) {
                    if (v.equals(((PlatformPackage) p).getVersion())) {
                        return ai;
                    }
                }
            }
        }
        if (selectedArchives != null) {
            for (Archive a : selectedArchives) {
                Package p = a.getParentPackage();
                if (p instanceof PlatformPackage) {
                    if (v.equals(((PlatformPackage) p).getVersion())) {
                        return insertArchive(a,
                                outArchives,
                                selectedArchives,
                                remotePkgs,
                                remoteSources,
                                localArchives,
                                true );
                    }
                }
            }
        }
        fetchRemotePackages(remotePkgs, remoteSources);
        for (Package p : remotePkgs) {
            if (p instanceof PlatformPackage) {
                if (v.equals(((PlatformPackage) p).getVersion())) {
                    for (Archive a : p.getArchives()) {
                        if (a.isCompatible()) {
                            return insertArchive(a,
                                    outArchives,
                                    selectedArchives,
                                    remotePkgs,
                                    remoteSources,
                                    localArchives,
                                    true );
                        }
                    }
                }
            }
        }
        return new MissingPlatformArchiveInfo(pkg.getVersion());
    }
    protected ArchiveInfo findMinApiLevelDependency(
            IMinApiLevelDependency pkg,
            ArrayList<ArchiveInfo> outArchives,
            Collection<Archive> selectedArchives,
            ArrayList<Package> remotePkgs,
            RepoSource[] remoteSources,
            ArchiveInfo[] localArchives) {
        int api = pkg.getMinApiLevel();
        if (api == ExtraPackage.MIN_API_LEVEL_NOT_SPECIFIED) {
            return null;
        }
        for (ArchiveInfo ai : localArchives) {
            Archive a = ai.getNewArchive();
            if (a != null) {
                Package p = a.getParentPackage();
                if (p instanceof PlatformPackage) {
                    if (((PlatformPackage) p).getVersion().isGreaterOrEqualThan(api)) {
                        return null;
                    }
                }
            }
        }
        int foundApi = 0;
        ArchiveInfo foundAi = null;
        for (ArchiveInfo ai : outArchives) {
            Archive a = ai.getNewArchive();
            if (a != null) {
                Package p = a.getParentPackage();
                if (p instanceof PlatformPackage) {
                    if (((PlatformPackage) p).getVersion().isGreaterOrEqualThan(api)) {
                        if (api > foundApi) {
                            foundApi = api;
                            foundAi = ai;
                        }
                    }
                }
            }
        }
        if (foundAi != null) {
            return foundAi;
        }
        foundApi = 0;
        Archive foundArchive = null;
        if (selectedArchives != null) {
            for (Archive a : selectedArchives) {
                Package p = a.getParentPackage();
                if (p instanceof PlatformPackage) {
                    if (((PlatformPackage) p).getVersion().isGreaterOrEqualThan(api)) {
                        if (api > foundApi) {
                            foundApi = api;
                            foundArchive = a;
                        }
                    }
                }
            }
        }
        fetchRemotePackages(remotePkgs, remoteSources);
        for (Package p : remotePkgs) {
            if (p instanceof PlatformPackage) {
                if (((PlatformPackage) p).getVersion().isGreaterOrEqualThan(api)) {
                    if (api > foundApi) {
                        for (Archive a : p.getArchives()) {
                            if (a.isCompatible()) {
                                foundApi = api;
                                foundArchive = a;
                            }
                        }
                    }
                }
            }
        }
        if (foundArchive != null) {
            return insertArchive(foundArchive,
                    outArchives,
                    selectedArchives,
                    remotePkgs,
                    remoteSources,
                    localArchives,
                    true );
        }
        return new MissingPlatformArchiveInfo(new AndroidVersion(api, null ));
    }
    protected void fetchRemotePackages(ArrayList<Package> remotePkgs, RepoSource[] remoteSources) {
        if (remotePkgs.size() > 0) {
            return;
        }
        for (RepoSource remoteSrc : remoteSources) {
            Package[] pkgs = remoteSrc.getPackages();
            if (pkgs != null) {
                nextPackage: for (Package pkg : pkgs) {
                    for (Archive a : pkg.getArchives()) {
                        if (a.isCompatible()) {
                            remotePkgs.add(pkg);
                            continue nextPackage;
                        }
                    }
                }
            }
        }
    }
    private static class LocalArchiveInfo extends ArchiveInfo {
        public LocalArchiveInfo(Archive localArchive) {
            super(localArchive, null , null );
        }
        @Override
        public boolean isAccepted() {
            return true;
        }
        @Override
        public boolean isRejected() {
            return false;
        }
    }
    private static class MissingPlatformArchiveInfo extends ArchiveInfo {
        private final AndroidVersion mVersion;
        public MissingPlatformArchiveInfo(AndroidVersion version) {
            super(null , null , null );
            mVersion = version;
        }
        @Override
        public boolean isAccepted() {
            return false;
        }
        @Override
        public boolean isRejected() {
            return true;
        }
        @Override
        public String getShortDescription() {
            return String.format("Missing SDK Platform Android%1$s, API %2$d",
                    mVersion.isPreview() ? " Preview" : "",
                    mVersion.getApiLevel());
        }
    }
    private static class MissingToolArchiveInfo extends ArchiveInfo {
        private final int mRevision;
        public MissingToolArchiveInfo(int revision) {
            super(null , null , null );
            mRevision = revision;
        }
        @Override
        public boolean isAccepted() {
            return false;
        }
        @Override
        public boolean isRejected() {
            return true;
        }
        @Override
        public String getShortDescription() {
            return String.format("Missing Android SDK Tools, revision %1$d", mRevision);
        }
    }
}
