public final class MultiResourceFile extends ResourceFile implements IValueResourceRepository {
    private final static SAXParserFactory sParserFactory = SAXParserFactory.newInstance();
    private final HashMap<ResourceType, HashMap<String, ResourceValue>> mResourceItems =
        new HashMap<ResourceType, HashMap<String, ResourceValue>>();
    public MultiResourceFile(IAbstractFile file, ResourceFolder folder) {
        super(file, folder);
    }
    @Override
    public ResourceType[] getResourceTypes() {
        update();
        Set<ResourceType> keys = mResourceItems.keySet();
        return keys.toArray(new ResourceType[keys.size()]);
    }
    @Override
    public boolean hasResources(ResourceType type) {
        update();
        HashMap<String, ResourceValue> list = mResourceItems.get(type);
        return (list != null && list.size() > 0);
    }
    @Override
    public Collection<ProjectResourceItem> getResources(ResourceType type,
            ProjectResources projectResources) {
        update();
        HashMap<String, ResourceValue> list = mResourceItems.get(type);
        ArrayList<ProjectResourceItem> items = new ArrayList<ProjectResourceItem>();
        if (list != null) {
            Collection<ResourceValue> values = list.values();
            for (ResourceValue res : values) {
                ProjectResourceItem item = projectResources.findResourceItem(type, res.getName());
                if (item == null) {
                    if (type == ResourceType.ID) {
                        item = new IdResourceItem(res.getName(), false );
                    } else {
                        item = new ConfigurableResourceItem(res.getName());
                    }
                    items.add(item);
                }
                item.add(this);
            }
        }
        return items;
    }
    private void update() {
        if (isTouched() == true) {
            mResourceItems.clear();
            parseFile();
            resetTouch();
        }
    }
    private void parseFile() {
        try {
            SAXParser parser = sParserFactory.newSAXParser();
            parser.parse(getFile().getContents(), new ValueResourceParser(this, isFramework()));
        } catch (ParserConfigurationException e) {
        } catch (SAXException e) {
        } catch (IOException e) {
        } catch (StreamException e) {
        }
    }
    public void addResourceValue(String resType, ResourceValue value) {
        ResourceType type = ResourceType.getEnum(resType);
        if (type != null) {
            HashMap<String, ResourceValue> list = mResourceItems.get(type);
            if (list == null) {
                list = new HashMap<String, ResourceValue>();
                mResourceItems.put(type, list);
            } else {
                ResourceValue oldValue = list.get(value.getName());
                if (oldValue != null) {
                    oldValue.replaceWith(value);
                    return;
                }
            }
            list.put(value.getName(), value);
        }
    }
    @Override
    public IResourceValue getValue(ResourceType type, String name) {
        update();
        HashMap<String, ResourceValue> list = mResourceItems.get(type);
        if (list != null) {
            return list.get(name);
        }
        return null;
    }
}
