public final class FolderTypeRelationship {
    private final static HashMap<ResourceType, ResourceFolderType[]> mTypeToFolderMap =
        new HashMap<ResourceType, ResourceFolderType[]>();
    private final static HashMap<ResourceFolderType, ResourceType[]> mFolderToTypeMap =
        new HashMap<ResourceFolderType, ResourceType[]>();
    static {
        HashMap<ResourceType, List<ResourceFolderType>> typeToFolderMap =
            new HashMap<ResourceType, List<ResourceFolderType>>();
        HashMap<ResourceFolderType, List<ResourceType>> folderToTypeMap =
            new HashMap<ResourceFolderType, List<ResourceType>>();
        add(ResourceType.ANIM, ResourceFolderType.ANIM, typeToFolderMap, folderToTypeMap);
        add(ResourceType.ARRAY, ResourceFolderType.VALUES, typeToFolderMap, folderToTypeMap);
        add(ResourceType.COLOR, ResourceFolderType.VALUES, typeToFolderMap, folderToTypeMap);
        add(ResourceType.COLOR, ResourceFolderType.COLOR, typeToFolderMap, folderToTypeMap);
        add(ResourceType.DIMEN, ResourceFolderType.VALUES, typeToFolderMap, folderToTypeMap);
        add(ResourceType.DRAWABLE, ResourceFolderType.VALUES, typeToFolderMap, folderToTypeMap);
        add(ResourceType.DRAWABLE, ResourceFolderType.DRAWABLE, typeToFolderMap, folderToTypeMap);
        add(ResourceType.ID, ResourceFolderType.VALUES, typeToFolderMap, folderToTypeMap);
        add(ResourceType.LAYOUT, ResourceFolderType.LAYOUT, typeToFolderMap, folderToTypeMap);
        add(ResourceType.MENU, ResourceFolderType.MENU, typeToFolderMap, folderToTypeMap);
        add(ResourceType.RAW, ResourceFolderType.RAW, typeToFolderMap, folderToTypeMap);
        add(ResourceType.STRING, ResourceFolderType.VALUES, typeToFolderMap, folderToTypeMap);
        add(ResourceType.STYLE, ResourceFolderType.VALUES, typeToFolderMap, folderToTypeMap);
        add(ResourceType.XML, ResourceFolderType.XML, typeToFolderMap, folderToTypeMap);
        optimize(typeToFolderMap, folderToTypeMap);
    }
    public static ResourceType[] getRelatedResourceTypes(ResourceFolderType folderType) {
        ResourceType[] array = mFolderToTypeMap.get(folderType);
        if (array != null) {
            return array;
        }
        return new ResourceType[0];
    }
    public static ResourceFolderType[] getRelatedFolders(ResourceType resType) {
        ResourceFolderType[] array = mTypeToFolderMap.get(resType);
        if (array != null) {
            return array;
        }
        return new ResourceFolderType[0];
    }
    public static boolean match(ResourceType resType, ResourceFolderType folderType) {
        ResourceFolderType[] array = mTypeToFolderMap.get(resType);
        if (array != null && array.length > 0) {
            for (ResourceFolderType fType : array) {
                if (fType == folderType) {
                    return true;
                }
            }
        }
        return false;
    }
    private static void add(ResourceType type, ResourceFolderType folder,
            HashMap<ResourceType, List<ResourceFolderType>> typeToFolderMap,
            HashMap<ResourceFolderType, List<ResourceType>> folderToTypeMap) {
        List<ResourceFolderType> folderList = typeToFolderMap.get(type);
        if (folderList == null) {
            folderList = new ArrayList<ResourceFolderType>();
            typeToFolderMap.put(type, folderList);
        }
        if (folderList.indexOf(folder) == -1) {
            folderList.add(folder);
        }
        List<ResourceType> typeList = folderToTypeMap.get(folder);
        if (typeList == null) {
            typeList = new ArrayList<ResourceType>();
            folderToTypeMap.put(folder, typeList);
        }
        if (typeList.indexOf(type) == -1) {
            typeList.add(type);
        }
    }
    private static void optimize(HashMap<ResourceType, List<ResourceFolderType>> typeToFolderMap,
            HashMap<ResourceFolderType, List<ResourceType>> folderToTypeMap) {
        Set<ResourceType> types = typeToFolderMap.keySet();
        for (ResourceType type : types) {
            List<ResourceFolderType> list = typeToFolderMap.get(type);
            mTypeToFolderMap.put(type, list.toArray(new ResourceFolderType[list.size()]));
        }
        Set<ResourceFolderType> folders = folderToTypeMap.keySet();
        for (ResourceFolderType folder : folders) {
            List<ResourceType> list = folderToTypeMap.get(folder);
            mFolderToTypeMap.put(folder, list.toArray(new ResourceType[list.size()]));
        }
    }
}
