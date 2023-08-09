public class ProjectResources implements IResourceRepository {
    private final static int DYNAMIC_ID_SEED_START = 0; 
    private final HashMap<ResourceFolderType, List<ResourceFolder>> mFolderMap =
        new HashMap<ResourceFolderType, List<ResourceFolder>>();
    private final HashMap<ResourceType, List<ProjectResourceItem>> mResourceMap =
        new HashMap<ResourceType, List<ProjectResourceItem>>();
    private Map<String, Map<String, Integer>> mResourceValueMap;
    private Map<Integer, String[]> mResIdValueToNameMap;
    private Map<IntArrayWrapper, String> mStyleableValueToNameMap;
    private final Map<String, Integer> mDynamicIds = new HashMap<String, Integer>();
    private int mDynamicSeed = DYNAMIC_ID_SEED_START;
    private final ArrayList<IdResourceItem> mIdResourceList = new ArrayList<IdResourceItem>();
    private final boolean mIsFrameworkRepository;
    private final IProject mProject;
    private final IntArrayWrapper mWrapper = new IntArrayWrapper(null);
    public ProjectResources(IProject project) {
        mIsFrameworkRepository = false;
        mProject = project;
    }
    public ProjectResources() {
        mIsFrameworkRepository = true;
        mProject = null;
    }
    public boolean isSystemRepository() {
        return mIsFrameworkRepository;
    }
    protected ResourceFolder add(ResourceFolderType type, FolderConfiguration config,
            IAbstractFolder folder) {
        List<ResourceFolder> list = mFolderMap.get(type);
        if (list == null) {
            list = new ArrayList<ResourceFolder>();
            ResourceFolder cf = new ResourceFolder(type, config, folder, mIsFrameworkRepository);
            list.add(cf);
            mFolderMap.put(type, list);
            return cf;
        }
        for (ResourceFolder cFolder : list) {
            if (cFolder.mConfiguration.equals(config)) {
                cFolder.mFolder = folder;
                return cFolder;
            }
        }
        ResourceFolder cf = new ResourceFolder(type, config, folder, mIsFrameworkRepository);
        list.add(cf);
        return cf;
    }
    protected ResourceFolder removeFolder(ResourceFolderType type, IFolder folder) {
        List<ResourceFolder> list = mFolderMap.get(type);
        if (list != null) {
            int count = list.size();
            for (int i = 0 ; i < count ; i++) {
                ResourceFolder resFolder = list.get(i);
                IFolderWrapper wrapper = (IFolderWrapper) resFolder.getFolder();
                if (wrapper.getIFolder().equals(folder)) {
                    list.remove(i);
                    if (list.size() > 0) {
                        list.get(0).touch();
                    } else {
                        ResourceType[] resTypes = FolderTypeRelationship.getRelatedResourceTypes(
                                type);
                        for (ResourceType resType : resTypes) {
                            ResourceFolderType[] folderTypes =
                                FolderTypeRelationship.getRelatedFolders(resType);
                            for (ResourceFolderType folderType : folderTypes) {
                                List<ResourceFolder> resFolders = mFolderMap.get(folderType);
                                if (resFolders != null && resFolders.size() > 0) {
                                    resFolders.get(0).touch();
                                    break;
                                }
                            }
                        }
                    }
                    return resFolder;
                }
            }
        }
        return null;
    }
    public List<ResourceFolder> getFolders(ResourceFolderType type) {
        return mFolderMap.get(type);
    }
    public ResourceType[] getAvailableResourceTypes() {
        ArrayList<ResourceType> list = new ArrayList<ResourceType>();
        for (ResourceFolderType folderType : mFolderMap.keySet()) {
            ResourceType types[] = FolderTypeRelationship.getRelatedResourceTypes(folderType);
            if (types.length == 1) {
                if (list.indexOf(types[0]) == -1) {
                    list.add(types[0]);
                }
            } else {
                List<ResourceFolder> folders = mFolderMap.get(folderType);
                if (folders != null) {
                    for (ResourceFolder folder : folders) {
                        Collection<ResourceType> folderContent = folder.getResourceTypes();
                        for (ResourceType folderResType : folderContent) {
                            if (list.indexOf(folderResType) == -1) {
                                list.add(folderResType);
                            }
                        }
                    }
                }
            }
        }
        if (list.indexOf(ResourceType.ID) == -1 && mResourceValueMap != null) {
            Map<String, Integer> map = mResourceValueMap.get(ResourceType.ID.getName());
            if (map != null && map.size() > 0) {
                list.add(ResourceType.ID);
            }
        }
        Collections.sort(list);
        return list.toArray(new ResourceType[list.size()]);
    }
    public ProjectResourceItem[] getResources(ResourceType type) {
        checkAndUpdate(type);
        if (type == ResourceType.ID) {
            synchronized (mIdResourceList) {
                return mIdResourceList.toArray(new ProjectResourceItem[mIdResourceList.size()]);
            }
        }
        List<ProjectResourceItem> items = mResourceMap.get(type);
        return items.toArray(new ProjectResourceItem[items.size()]);
    }
    public boolean hasResources(ResourceType type) {
        checkAndUpdate(type);
        if (type == ResourceType.ID) {
            synchronized (mIdResourceList) {
                return mIdResourceList.size() > 0;
            }
        }
        List<ProjectResourceItem> items = mResourceMap.get(type);
        return (items != null && items.size() > 0);
    }
    public ResourceFolder getResourceFolder(IFolder folder) {
        for (List<ResourceFolder> list : mFolderMap.values()) {
            for (ResourceFolder resFolder : list) {
                IFolderWrapper wrapper = (IFolderWrapper) resFolder.getFolder();
                if (wrapper.getIFolder().equals(folder)) {
                    return resFolder;
                }
            }
        }
        return null;
    }
    public ResourceFile getMatchingFile(String name, ResourceFolderType type,
            FolderConfiguration config) {
        List<ResourceFolder> folders = mFolderMap.get(type);
        ArrayList<ResourceFolder> matchingFolders = new ArrayList<ResourceFolder>();
        for (int i = 0 ; i < folders.size(); i++) {
            ResourceFolder folder = folders.get(i);
            if (folder.hasFile(name) == true) {
                matchingFolders.add(folder);
            }
        }
        Resource match = findMatchingConfiguredResource(matchingFolders, config);
        if (match instanceof ResourceFolder) {
            return ((ResourceFolder)match).getFile(name);
        }
        return null;
    }
    public Map<String, Map<String, IResourceValue>> getConfiguredResources(
            FolderConfiguration referenceConfig) {
        Map<String, Map<String, IResourceValue>> map =
            new HashMap<String, Map<String, IResourceValue>>();
        if (mProject != null) {
            ProjectState state = Sdk.getProjectState(mProject);
            if (state != null) {
                IProject[] libraries = state.getLibraryProjects();
                ResourceManager resMgr = ResourceManager.getInstance();
                final int libCount = libraries != null ? libraries.length : 0;
                for (int i = libCount - 1 ; i >= 0 ; i--) {
                    IProject library = libraries[i];
                    ProjectResources libRes = resMgr.getProjectResources(library);
                    if (libRes != null) {
                        libRes.loadAll();
                        Map<String, Map<String, IResourceValue>> libMap =
                                libRes.getConfiguredResources(referenceConfig);
                        for (Entry<String, Map<String, IResourceValue>> entry : libMap.entrySet()) {
                            Map<String, IResourceValue> tempMap = map.get(entry.getKey());
                            if (tempMap == null) {
                                map.put(entry.getKey(), entry.getValue());
                            } else {
                                tempMap.putAll(entry.getValue());
                            }
                        }
                    }
                }
            }
        }
        if (mIdResourceList.size() > 0) {
            String idType = ResourceType.ID.getName();
            Map<String, IResourceValue> idMap = map.get(idType);
            if (idMap == null) {
                idMap = new HashMap<String, IResourceValue>();
                map.put(idType, idMap);
            }
            for (IdResourceItem id : mIdResourceList) {
                idMap.put(id.getName(), new ResourceValue(idType, id.getName(),
                        mIsFrameworkRepository));
            }
        }
        Set<ResourceType> keys = mResourceMap.keySet();
        for (ResourceType key : keys) {
            if (key != ResourceType.ID) {
                Map<String, IResourceValue> localResMap = getConfiguredResource(key,
                        referenceConfig);
                String resName = key.getName();
                Map<String, IResourceValue> resMap = map.get(resName);
                if (resMap == null) {
                    map.put(resName, localResMap);
                } else {
                    resMap.putAll(localResMap);
                }
            }
        }
        return map;
    }
    public void loadAll() {
        ResourceType[] types = getAvailableResourceTypes();
        for (ResourceType type: types) {
            checkAndUpdate(type);
        }
    }
    public String[] resolveResourceValue(int id) {
        if (mResIdValueToNameMap != null) {
            return mResIdValueToNameMap.get(id);
        }
        return null;
    }
    public String resolveResourceValue(int[] id) {
        if (mStyleableValueToNameMap != null) {
            mWrapper.set(id);
            return mStyleableValueToNameMap.get(mWrapper);
        }
        return null;
    }
    public Integer getResourceValue(String type, String name) {
        if (mResourceValueMap != null) {
            Map<String, Integer> map = mResourceValueMap.get(type);
            if (map != null) {
                Integer value = map.get(name);
                if (value == null && ResourceType.ID.getName().equals(type)) {
                    return getDynamicId(name);
                }
                return value;
            } else if (ResourceType.ID.getName().equals(type)) {
                return getDynamicId(name);
            }
        }
        return null;
    }
    public SortedSet<String> getLanguages() {
        SortedSet<String> set = new TreeSet<String>();
        Collection<List<ResourceFolder>> folderList = mFolderMap.values();
        for (List<ResourceFolder> folderSubList : folderList) {
            for (ResourceFolder folder : folderSubList) {
                FolderConfiguration config = folder.getConfiguration();
                LanguageQualifier lang = config.getLanguageQualifier();
                if (lang != null) {
                    set.add(lang.getStringValue());
                }
            }
        }
        return set;
    }
    public SortedSet<String> getRegions(String currentLanguage) {
        SortedSet<String> set = new TreeSet<String>();
        Collection<List<ResourceFolder>> folderList = mFolderMap.values();
        for (List<ResourceFolder> folderSubList : folderList) {
            for (ResourceFolder folder : folderSubList) {
                FolderConfiguration config = folder.getConfiguration();
                LanguageQualifier lang = config.getLanguageQualifier();
                if (lang != null && lang.getStringValue().equals(currentLanguage)) {
                    RegionQualifier region = config.getRegionQualifier();
                    if (region != null) {
                        set.add(region.getStringValue());
                    }
                }
            }
        }
        return set;
    }
    public void resetDynamicIds() {
        synchronized (mDynamicIds) {
            mDynamicIds.clear();
            mDynamicSeed = DYNAMIC_ID_SEED_START;
        }
    }
    private Map<String, IResourceValue> getConfiguredResource(ResourceType type,
            FolderConfiguration referenceConfig) {
        List<ProjectResourceItem> items = mResourceMap.get(type);
        HashMap<String, IResourceValue> map = new HashMap<String, IResourceValue>();
        for (ProjectResourceItem item : items) {
            List<ResourceFile> list = item.getSourceFileList();
            Resource match = findMatchingConfiguredResource(list, referenceConfig);
            if (match instanceof ResourceFile) {
                ResourceFile matchResFile = (ResourceFile)match;
                IResourceValue value = matchResFile.getValue(type, item.getName());
                if (value != null) {
                    map.put(item.getName(), value);
                }
            }
        }
        return map;
    }
    private Resource findMatchingConfiguredResource(List<? extends Resource> resources,
            FolderConfiguration referenceConfig) {
        ArrayList<Resource> matchingResources = new ArrayList<Resource>();
        for (int i = 0 ; i < resources.size(); i++) {
            Resource res = resources.get(i);
            if (res.getConfiguration().isMatchFor(referenceConfig)) {
                matchingResources.add(res);
            }
        }
        if (matchingResources.size() == 1) {
            return matchingResources.get(0);
        } else if (matchingResources.size() == 0) {
            return null;
        }
        final int count = FolderConfiguration.getQualifierCount();
        for (int q = 0 ; q < count ; q++) {
            ResourceQualifier referenceQualifier = referenceConfig.getQualifier(q);
            boolean found = false;
            ResourceQualifier bestMatch = null; 
            for (Resource res : matchingResources) {
                ResourceQualifier qualifier = res.getConfiguration().getQualifier(q);
                if (qualifier != null) {
                    found = true;
                    if (referenceQualifier != null) {
                        if (qualifier.isBetterMatchThan(bestMatch, referenceQualifier)) {
                            bestMatch = qualifier;
                        }
                    }
                }
            }
            if (found) {
                for (int i = 0 ; i < matchingResources.size(); ) {
                    Resource res = matchingResources.get(i);
                    ResourceQualifier qualifier = res.getConfiguration().getQualifier(q);
                    if (qualifier == null) {
                        matchingResources.remove(res);
                    } else if (referenceQualifier != null && bestMatch != null &&
                            bestMatch.equals(qualifier) == false) {
                        matchingResources.remove(res);
                    } else {
                        i++;
                    }
                }
                if (matchingResources.size() < 2) {
                    break;
                }
            }
        }
        if (matchingResources.size() == 0) {
            return null;
        }
        return matchingResources.get(0);
    }
    private void checkAndUpdate(ResourceType type) {
        ResourceFolderType[] folderTypes = FolderTypeRelationship.getRelatedFolders(type);
        for (ResourceFolderType folderType : folderTypes) {
            List<ResourceFolder> folders = mFolderMap.get(folderType);
            if (folders != null) {
                for (ResourceFolder folder : folders) {
                    if (folder.isTouched()) {
                        ResourceType[] resTypes = FolderTypeRelationship.getRelatedResourceTypes(
                                folderType);
                        for (ResourceType resType : resTypes) {
                            update(resType);
                        }
                        return;
                    }
                }
            }
        }
    }
    private void update(ResourceType type) {
        List<ProjectResourceItem> items = mResourceMap.get(type);
        List<ProjectResourceItem> backup = new ArrayList<ProjectResourceItem>();
        if (items == null) {
            items = new ArrayList<ProjectResourceItem>();
            mResourceMap.put(type, items);
        } else {
            backup.addAll(items);
            items.clear();
        }
        ResourceFolderType[] folderTypes = FolderTypeRelationship.getRelatedFolders(type);
        for (ResourceFolderType folderType : folderTypes) {
            List<ResourceFolder> folders = mFolderMap.get(folderType);
            if (folders != null) {
                for (ResourceFolder folder : folders) {
                    items.addAll(folder.getResources(type, this));
                    folder.resetTouch();
                }
            }
        }
        if (backup.size() > 0) {
            int count = items.size();
            for (int i = 0 ; i < count;) {
                ProjectResourceItem item = items.get(i);
                ProjectResourceItem foundOldItem = null;
                for (ProjectResourceItem oldItem : backup) {
                    if (oldItem.getName().equals(item.getName())) {
                        foundOldItem = oldItem;
                        break;
                    }
                }
                if (foundOldItem != null) {
                    foundOldItem.replaceWith(item);
                    items.remove(i);
                    backup.remove(foundOldItem);
                    items.add(foundOldItem);
                } else {
                    i++;
                }
            }
        }
        if (type == ResourceType.ID) {
            mergeIdResources();
        } else {
            Collections.sort(items);
        }
    }
    private Integer getDynamicId(String name) {
        synchronized (mDynamicIds) {
            Integer value = mDynamicIds.get(name);
            if (value == null) {
                value = new Integer(++mDynamicSeed);
                mDynamicIds.put(name, value);
            }
            return value;
        }
    }
    protected ProjectResourceItem findResourceItem(ResourceType type, String name) {
        List<ProjectResourceItem> list = mResourceMap.get(type);
        for (ProjectResourceItem item : list) {
            if (name.equals(item.getName())) {
                return item;
            }
        }
        return null;
    }
    void setCompiledResources(Map<Integer, String[]> resIdValueToNameMap,
            Map<IntArrayWrapper, String> styleableValueMap,
            Map<String, Map<String, Integer>> resourceValueMap) {
        mResourceValueMap = resourceValueMap;
        mResIdValueToNameMap = resIdValueToNameMap;
        mStyleableValueToNameMap = styleableValueMap;
        mergeIdResources();
    }
    void mergeIdResources() {
        List<ProjectResourceItem> xmlIdResources = mResourceMap.get(ResourceType.ID);
        synchronized (mIdResourceList) {
            ArrayList<IdResourceItem> oldItems = new ArrayList<IdResourceItem>();
            oldItems.addAll(mIdResourceList);
            mIdResourceList.clear();
            Map<String, Integer> idMap = null;
            if (mResourceValueMap != null) {
                idMap = mResourceValueMap.get(ResourceType.ID.getName());
            }
            if (idMap == null) {
                if (xmlIdResources != null) {
                    for (ProjectResourceItem resourceItem : xmlIdResources) {
                        if (resourceItem instanceof IdResourceItem) {
                            mIdResourceList.add((IdResourceItem)resourceItem);
                        }
                    }
                }
            } else {
                Set<String> idSet = idMap.keySet();
                idLoop: for (String idResource : idSet) {
                    if (xmlIdResources != null) {
                        for (ProjectResourceItem resourceItem : xmlIdResources) {
                            if (resourceItem instanceof IdResourceItem &&
                                    resourceItem.getName().equals(idResource)) {
                                mIdResourceList.add((IdResourceItem)resourceItem);
                                continue idLoop;
                            }
                        }
                    }
                    int count = oldItems.size();
                    for (int i = 0 ; i < count ; i++) {
                        IdResourceItem resourceItem = oldItems.get(i);
                        if (resourceItem.getName().equals(idResource)) {
                            oldItems.remove(i);
                            mIdResourceList.add(resourceItem);
                            continue idLoop;
                        }
                    }
                    mIdResourceList.add(new IdResourceItem(idResource,
                            true ));
                }
            }
            Collections.sort(mIdResourceList);
        }
    }
}
