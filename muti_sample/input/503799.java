public class SingleResourceFile extends ResourceFile {
    private final static SAXParserFactory sParserFactory = SAXParserFactory.newInstance();
    static {
        sParserFactory.setNamespaceAware(true);
    }
    private final static Pattern sXmlPattern = Pattern.compile("^(.+)\\.xml", 
            Pattern.CASE_INSENSITIVE);
    private final static Pattern[] sDrawablePattern = new Pattern[] {
        Pattern.compile("^(.+)\\.9\\.png", Pattern.CASE_INSENSITIVE), 
        Pattern.compile("^(.+)\\.png", Pattern.CASE_INSENSITIVE), 
        Pattern.compile("^(.+)\\.jpg", Pattern.CASE_INSENSITIVE), 
        Pattern.compile("^(.+)\\.gif", Pattern.CASE_INSENSITIVE), 
    };
    private String mResourceName;
    private ResourceType mType;
    private IResourceValue mValue;
    public SingleResourceFile(IAbstractFile file, ResourceFolder folder) {
        super(file, folder);
        ResourceType[] types = FolderTypeRelationship.getRelatedResourceTypes(folder.getType());
        mType = types[0];
        mResourceName = getResourceName(mType);
        PixelDensityQualifier qualifier = folder.getConfiguration().getPixelDensityQualifier();
        if (qualifier == null) {
            mValue = new ResourceValue(mType.getName(), getResourceName(mType),
                    file.getOsLocation(), isFramework());
        } else {
            mValue = new DensityBasedResourceValue(mType.getName(), getResourceName(mType),
                    file.getOsLocation(), qualifier.getValue().getDensity(), isFramework());
        }
    }
    @Override
    public ResourceType[] getResourceTypes() {
        return FolderTypeRelationship.getRelatedResourceTypes(getFolder().getType());
    }
    @Override
    public boolean hasResources(ResourceType type) {
        return FolderTypeRelationship.match(type, getFolder().getType());
    }
    @Override
    public Collection<ProjectResourceItem> getResources(ResourceType type,
            ProjectResources projectResources) {
        ProjectResourceItem item = projectResources.findResourceItem(type, mResourceName);
        ArrayList<ProjectResourceItem> items = new ArrayList<ProjectResourceItem>();
        if (item == null) {
            item = new ConfigurableResourceItem(mResourceName);
            items.add(item);
        }
        item.add(this);
        return items;
    }
    @Override
    public IResourceValue getValue(ResourceType type, String name) {
        return mValue;
    }
    private String getResourceName(ResourceType type) {
        String name = getFile().getName();
        if (type == ResourceType.ANIM || type == ResourceType.LAYOUT || type == ResourceType.MENU ||
                type == ResourceType.COLOR || type == ResourceType.XML) {
            Matcher m = sXmlPattern.matcher(name);
            if (m.matches()) {
                return m.group(1);
            }
        } else if (type == ResourceType.DRAWABLE) {
            for (Pattern p : sDrawablePattern) {
                Matcher m = p.matcher(name);
                if (m.matches()) {
                    return m.group(1);
                }
            }
            Matcher m = sXmlPattern.matcher(name);
            if (m.matches()) {
                return m.group(1);
            }
        }
        return name;
    }
}
