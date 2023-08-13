public final class LayoutDescriptors implements IDescriptorProvider {
    public static final String ID_ATTR = "id"; 
    private DocumentDescriptor mRootDescriptor =
        new DocumentDescriptor("layout_doc", null); 
    private ArrayList<ElementDescriptor> mLayoutDescriptors = new ArrayList<ElementDescriptor>();
    private List<ElementDescriptor> mROLayoutDescriptors;
    private ArrayList<ElementDescriptor> mViewDescriptors = new ArrayList<ElementDescriptor>();
    private List<ElementDescriptor> mROViewDescriptors;
    private ElementDescriptor mBaseViewDescriptor;
    public DocumentDescriptor getDescriptor() {
        return mRootDescriptor;
    }
    public List<ElementDescriptor> getLayoutDescriptors() {
        return mROLayoutDescriptors;
    }
    public List<ElementDescriptor> getViewDescriptors() {
        return mROViewDescriptors;
    }
    public ElementDescriptor[] getRootElementDescriptors() {
        return mRootDescriptor.getChildren();
    }
    public ElementDescriptor getBaseViewDescriptor() {
        if (mBaseViewDescriptor == null) {
            for (ElementDescriptor desc : mViewDescriptors) {
                if (desc instanceof ViewElementDescriptor) {
                    ViewElementDescriptor viewDesc = (ViewElementDescriptor) desc;
                    if (AndroidConstants.CLASS_VIEW.equals(viewDesc.getFullClassName())) {
                        mBaseViewDescriptor = viewDesc;
                        break;
                    }
                }
            }
        }
        return mBaseViewDescriptor;
    }
    public synchronized void updateDescriptors(ViewClassInfo[] views, ViewClassInfo[] layouts) {
        HashMap<ViewClassInfo, ViewElementDescriptor> infoDescMap =
            new HashMap<ViewClassInfo, ViewElementDescriptor>();
        ArrayList<ElementDescriptor> newViews = new ArrayList<ElementDescriptor>();
        if (views != null) {
            for (ViewClassInfo info : views) {
                ElementDescriptor desc = convertView(info, infoDescMap);
                newViews.add(desc);
            }
        }
        insertInclude(newViews);
        ArrayList<ElementDescriptor> newLayouts = new ArrayList<ElementDescriptor>();
        if (layouts != null) {
            for (ViewClassInfo info : layouts) {
                ElementDescriptor desc = convertView(info, infoDescMap);
                newLayouts.add(desc);
            }
        }
        ArrayList<ElementDescriptor> newDescriptors = new ArrayList<ElementDescriptor>();
        newDescriptors.addAll(newLayouts);
        newDescriptors.addAll(newViews);
        for (ElementDescriptor layoutDesc : newLayouts) {
            layoutDesc.setChildren(newDescriptors);
        }
        fixSuperClasses(infoDescMap);
        ElementDescriptor mergeTag = createMerge(newLayouts);
        mergeTag.setChildren(newDescriptors);  
        newDescriptors.add(mergeTag);
        newLayouts.add(mergeTag);
        mViewDescriptors = newViews;
        mLayoutDescriptors  = newLayouts;
        mRootDescriptor.setChildren(newDescriptors);
        mBaseViewDescriptor = null;
        mROLayoutDescriptors = Collections.unmodifiableList(mLayoutDescriptors);
        mROViewDescriptors = Collections.unmodifiableList(mViewDescriptors);
    }
    private ElementDescriptor convertView(
            ViewClassInfo info,
            HashMap<ViewClassInfo, ViewElementDescriptor> infoDescMap) {
        String xml_name = info.getShortClassName();
        String tooltip = info.getJavaDoc();
        ArrayList<AttributeDescriptor> attributes = new ArrayList<AttributeDescriptor>();
        AttributeInfo styleInfo = new DeclareStyleableInfo.AttributeInfo(
                "style",    
                new DeclareStyleableInfo.AttributeInfo.Format[] {
                        DeclareStyleableInfo.AttributeInfo.Format.REFERENCE
                    });
        styleInfo.setJavaDoc("A reference to a custom style"); 
        DescriptorsUtils.appendAttribute(attributes,
                "style",    
                null,       
                styleInfo,
                false,      
                null);      
        DescriptorsUtils.appendAttributes(attributes,
                null, 
                SdkConstants.NS_RESOURCES,
                info.getAttributes(),
                null, 
                null );
        for (ViewClassInfo link = info.getSuperClass();
                link != null;
                link = link.getSuperClass()) {
            AttributeInfo[] attrList = link.getAttributes();
            if (attrList.length > 0) {
                attributes.add(new SeparatorAttributeDescriptor(
                        String.format("Attributes from %1$s", link.getShortClassName())));
                DescriptorsUtils.appendAttributes(attributes,
                        null, 
                        SdkConstants.NS_RESOURCES,
                        attrList,
                        null, 
                        null );
            }
        }
        ArrayList<AttributeDescriptor> layoutAttributes = new ArrayList<AttributeDescriptor>();
        LayoutParamsInfo layoutParams = info.getLayoutData();
        for(; layoutParams != null; layoutParams = layoutParams.getSuperClass()) {
            boolean need_separator = true;
            for (AttributeInfo attr_info : layoutParams.getAttributes()) {
                if (DescriptorsUtils.containsAttribute(layoutAttributes,
                        SdkConstants.NS_RESOURCES, attr_info)) {
                    continue;
                }
                if (need_separator) {
                    String title;
                    if (layoutParams.getShortClassName().equals(
                            AndroidConstants.CLASS_NAME_LAYOUTPARAMS)) {
                        title = String.format("Layout Attributes from %1$s",
                                    layoutParams.getViewLayoutClass().getShortClassName());
                    } else {
                        title = String.format("Layout Attributes from %1$s (%2$s)",
                                layoutParams.getViewLayoutClass().getShortClassName(),
                                layoutParams.getShortClassName());
                    }
                    layoutAttributes.add(new SeparatorAttributeDescriptor(title));
                    need_separator = false;
                }
                DescriptorsUtils.appendAttribute(layoutAttributes,
                        null, 
                        SdkConstants.NS_RESOURCES,
                        attr_info,
                        false, 
                        null );
            }
        }
        ViewElementDescriptor desc = new ViewElementDescriptor(xml_name,
                xml_name, 
                info.getFullClassName(),
                tooltip,
                null, 
                attributes.toArray(new AttributeDescriptor[attributes.size()]),
                layoutAttributes.toArray(new AttributeDescriptor[layoutAttributes.size()]),
                null, 
                false );
        infoDescMap.put(info, desc);
        return desc;
    }
    private void insertInclude(ArrayList<ElementDescriptor> knownViews) {
        String xml_name = "include";  
        ArrayList<AttributeDescriptor> attributes = new ArrayList<AttributeDescriptor>();
        DescriptorsUtils.appendAttribute(attributes,
                null, 
                null, 
                new AttributeInfo(
                        "layout",       
                        new AttributeInfo.Format[] { AttributeInfo.Format.REFERENCE }
                        ),
                true,  
                null); 
        DescriptorsUtils.appendAttribute(attributes,
                null, 
                SdkConstants.NS_RESOURCES, 
                new AttributeInfo(
                        "id",           
                        new AttributeInfo.Format[] { AttributeInfo.Format.REFERENCE }
                        ),
                true,  
                null); 
        AttributeDescriptor[] viewLayoutAttribs = findViewLayoutAttributes(
                AndroidConstants.CLASS_VIEW, knownViews);
        ViewElementDescriptor desc = new ViewElementDescriptor(xml_name,  
                xml_name, 
                null,     
                "Lets you statically include XML layouts inside other XML layouts.",  
                null, 
                attributes.toArray(new AttributeDescriptor[attributes.size()]),
                viewLayoutAttribs,  
                null, 
                false );
        knownViews.add(desc);
    }
    private ElementDescriptor createMerge(ArrayList<ElementDescriptor> knownLayouts) {
        String xml_name = "merge";  
        AttributeDescriptor[] viewLayoutAttribs = findViewLayoutAttributes(
                AndroidConstants.CLASS_FRAMELAYOUT, knownLayouts);
        ViewElementDescriptor desc = new ViewElementDescriptor(xml_name,  
                xml_name, 
                null,     
                "A root tag useful for XML layouts inflated using a ViewStub.",  
                null,  
                null,  
                viewLayoutAttribs,  
                null,  
                false  );
        return desc;
    }
    private AttributeDescriptor[] findViewLayoutAttributes(
            String viewFqcn,
            ArrayList<ElementDescriptor> knownViews) {
        for (ElementDescriptor desc : knownViews) {
            if (desc instanceof ViewElementDescriptor) {
                ViewElementDescriptor viewDesc = (ViewElementDescriptor) desc;
                if (viewFqcn.equals(viewDesc.getFullClassName())) {
                    return viewDesc.getLayoutAttributes();
                }
            }
        }
        return null;
    }
    private void fixSuperClasses(HashMap<ViewClassInfo, ViewElementDescriptor> infoDescMap) {
        for (Entry<ViewClassInfo, ViewElementDescriptor> entry : infoDescMap.entrySet()) {
            ViewClassInfo info = entry.getKey();
            ViewElementDescriptor desc = entry.getValue();
            ViewClassInfo sup = info.getSuperClass();
            if (sup != null) {
                ViewElementDescriptor supDesc = infoDescMap.get(sup);
                while (supDesc == null && sup != null) {
                    sup = sup.getSuperClass();
                    if (sup != null) {
                        supDesc = infoDescMap.get(sup);
                    }
                }
                if (supDesc != null) {
                    desc.setSuperClass(supDesc);
                }
            }
        }
    }
}
