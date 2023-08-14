public class RepoSourcesAdapter {
    private final UpdaterData mUpdaterData;
    public static class RepoSourceError implements IDescription {
        private final RepoSource mSource;
        public RepoSourceError(RepoSource source) {
            mSource = source;
        }
        public String getLongDescription() {
            return mSource.getLongDescription();
        }
        public String getShortDescription() {
            return mSource.getFetchError();
        }
    }
    public static class RepoSourceEmpty implements IDescription {
        private final RepoSource mSource;
        private final boolean mEmptyBecauseOfUpdateOnly;
        public RepoSourceEmpty(RepoSource source, boolean emptyBecauseOfUpdateOnly) {
            mSource = source;
            mEmptyBecauseOfUpdateOnly = emptyBecauseOfUpdateOnly;
        }
        public String getLongDescription() {
            return mSource.getLongDescription();
        }
        public String getShortDescription() {
            if (mEmptyBecauseOfUpdateOnly) {
                return "Some packages were found but are not compatible updates.";
            } else {
                return "No packages found";
            }
        }
    }
    public RepoSourcesAdapter(UpdaterData updaterData) {
        mUpdaterData = updaterData;
    }
    public ILabelProvider getLabelProvider() {
        return new ViewerLabelProvider();
    }
    public IContentProvider getContentProvider() {
        return new TreeContentProvider();
    }
    private class ViewerLabelProvider extends LabelProvider {
        @Override
        public Image getImage(Object element) {
            ImageFactory imgFactory = mUpdaterData.getImageFactory();
            if (imgFactory != null) {
                return imgFactory.getImageForObject(element);
            }
            return super.getImage(element);
        }
        @Override
        public String getText(Object element) {
            if (element instanceof IDescription) {
                return ((IDescription) element).getShortDescription();
            }
            return super.getText(element);
        }
    }
    private class TreeContentProvider implements ITreeContentProvider {
        public void dispose() {
        }
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            assert newInput == RepoSourcesAdapter.this;
        }
        public Object[] getElements(Object inputElement) {
            return getChildren(inputElement);
        }
        public Object[] getChildren(Object parentElement) {
            if (parentElement == RepoSourcesAdapter.this) {
                return mUpdaterData.getSources().getSources();
            } else if (parentElement instanceof RepoSource) {
                return getRepoSourceChildren((RepoSource) parentElement);
            } else if (parentElement instanceof Package) {
                return getPackageChildren((Package) parentElement);
            }
            return new Object[0];
        }
        private Object[] getRepoSourceChildren(final RepoSource source) {
            Package[] packages = source.getPackages();
            if (packages == null && source.getFetchError() == null) {
                final boolean forceHttp = mUpdaterData.getSettingsController().getForceHttp();
                mUpdaterData.getTaskFactory().start("Loading Source", new ITask() {
                    public void run(ITaskMonitor monitor) {
                        source.load(monitor, forceHttp);
                    }
                });
                packages = source.getPackages();
            }
            boolean wasEmptyBeforeFilter = (packages == null || packages.length == 0);
            if (packages != null && mUpdaterData.getSettingsController().getShowUpdateOnly()) {
                packages = filteredPackages(packages);
            }
            if (packages != null && packages.length == 0) {
                packages = null;
            }
            ArrayList<Object> results = new ArrayList<Object>();
            if (source.getFetchError() != null) {
                results.add(new RepoSourceError(source));
            }
            if (packages != null) {
                for (Package p : packages) {
                    results.add(p);
                }
            } else {
                results.add(new RepoSourceEmpty(source, !wasEmptyBeforeFilter));
            }
            return results.toArray();
        }
        private Object[] getPackageChildren(Package pkg) {
            Archive[] archives = pkg.getArchives();
            if (mUpdaterData.getSettingsController().getShowUpdateOnly()) {
                for (Archive archive : archives) {
                    if (archive.isCompatible()) {
                        return new Object[] { archive };
                    }
                }
            }
            return archives;
        }
        public Object getParent(Object element) {
            if (element instanceof RepoSource) {
                return RepoSourcesAdapter.this;
            } else if (element instanceof Package) {
                return ((Package) element).getParentSource();
            } else if (element instanceof Archive) {
                return ((Archive) element).getParentPackage();
            }
            return null;
        }
        public boolean hasChildren(Object element) {
            return element instanceof RepoSource || element instanceof Package;
        }
    }
    private Package[] filteredPackages(Package[] remotePackages) {
        Package[] installedPackages = mUpdaterData.getInstalledPackage();
        ArrayList<Package> filteredList = new ArrayList<Package>();
        for (Package remotePkg : remotePackages) {
            boolean newPkg = true;
            if (remotePkg.isObsolete()) {
                continue;
            }
            if (remotePkg.hasCompatibleArchive()) {
                for (Package installedPkg : installedPackages) {
                    UpdateInfo info = installedPkg.canBeUpdatedBy(remotePkg);
                    if (info == UpdateInfo.UPDATE) {
                        filteredList.add(remotePkg);
                        newPkg = false;
                        break; 
                    } else if (info != UpdateInfo.INCOMPATIBLE) {
                        newPkg = false;
                        break; 
                    }
                }
                if (newPkg) {
                    filteredList.add(remotePkg);
                }
            }
        }
        return filteredList.toArray(new Package[filteredList.size()]);
    }
}
