public final class XmlDescriptors implements IDescriptorProvider {
    public static final String PREF_KEY_ATTR = "key"; 
    private DocumentDescriptor mDescriptor = new DocumentDescriptor("xml_doc", null ); 
    private DocumentDescriptor mSearchDescriptor = new DocumentDescriptor("xml_doc", null ); 
    private DocumentDescriptor mPrefDescriptor = new DocumentDescriptor("xml_doc", null ); 
    private DocumentDescriptor mAppWidgetDescriptor = new DocumentDescriptor("xml_doc", null ); 
    public DocumentDescriptor getDescriptor() {
        return mDescriptor;
    }
    public ElementDescriptor[] getRootElementDescriptors() {
        return mDescriptor.getChildren();
    }
    public DocumentDescriptor getSearchableDescriptor() {
        return mSearchDescriptor;
    }
    public DocumentDescriptor getPreferencesDescriptor() {
        return mPrefDescriptor;
    }
    public DocumentDescriptor getAppWidgetDescriptor() {
        return mAppWidgetDescriptor;
    }
    public IDescriptorProvider getSearchableProvider() {
        return new IDescriptorProvider() {
            public ElementDescriptor getDescriptor() {
                return mSearchDescriptor;
            }
            public ElementDescriptor[] getRootElementDescriptors() {
                return mSearchDescriptor.getChildren();
            }
        };
    }
    public IDescriptorProvider getPreferencesProvider() {
        return new IDescriptorProvider() {
            public ElementDescriptor getDescriptor() {
                return mPrefDescriptor;
            }
            public ElementDescriptor[] getRootElementDescriptors() {
                return mPrefDescriptor.getChildren();
            }
        };
    }
    public IDescriptorProvider getAppWidgetProvider() {
        return new IDescriptorProvider() {
            public ElementDescriptor getDescriptor() {
                return mAppWidgetDescriptor;
            }
            public ElementDescriptor[] getRootElementDescriptors() {
                return mAppWidgetDescriptor.getChildren();
            }
        };
    }
    public synchronized void updateDescriptors(
            Map<String, DeclareStyleableInfo> searchableStyleMap,
            Map<String, DeclareStyleableInfo> appWidgetStyleMap,
            ViewClassInfo[] prefs, ViewClassInfo[] prefGroups) {
        XmlnsAttributeDescriptor xmlns = new XmlnsAttributeDescriptor(
                "android", 
                SdkConstants.NS_RESOURCES); 
        ElementDescriptor searchable = createSearchable(searchableStyleMap, xmlns);
        ElementDescriptor appWidget = createAppWidgetProviderInfo(appWidgetStyleMap, xmlns);
        ElementDescriptor preferences = createPreference(prefs, prefGroups, xmlns);
        ArrayList<ElementDescriptor> list =  new ArrayList<ElementDescriptor>();
        if (searchable != null) {
            list.add(searchable);
            mSearchDescriptor.setChildren(new ElementDescriptor[]{ searchable });
        }
        if (appWidget != null) {
            list.add(appWidget);
            mAppWidgetDescriptor.setChildren(new ElementDescriptor[]{ appWidget });
        }
        if (preferences != null) {
            list.add(preferences);
            mPrefDescriptor.setChildren(new ElementDescriptor[]{ preferences });
        }
        if (list.size() > 0) {
            mDescriptor.setChildren(list.toArray(new ElementDescriptor[list.size()]));
        }
    }
    private ElementDescriptor createSearchable(
            Map<String, DeclareStyleableInfo> searchableStyleMap,
            XmlnsAttributeDescriptor xmlns) {
        ElementDescriptor action_key = createElement(searchableStyleMap,
                "SearchableActionKey", 
                "actionkey", 
                "Action Key", 
                null, 
                null, 
                null, 
                false  );
        ElementDescriptor searchable = createElement(searchableStyleMap,
                "Searchable", 
                "searchable", 
                "Searchable", 
                null, 
                xmlns, 
                new ElementDescriptor[] { action_key }, 
                false  );
        return searchable;
    }
    private ElementDescriptor createAppWidgetProviderInfo(
            Map<String, DeclareStyleableInfo> appWidgetStyleMap,
            XmlnsAttributeDescriptor xmlns) {
        if (appWidgetStyleMap == null) {
            return null;
        }
        ElementDescriptor appWidget = createElement(appWidgetStyleMap,
                "AppWidgetProviderInfo", 
                "appwidget-provider", 
                "AppWidget Provider", 
                null, 
                xmlns, 
                null, 
                false  );
        return appWidget;
    }
    private ElementDescriptor createElement(
            Map<String, DeclareStyleableInfo> styleMap, String styleName,
            String xmlName, String uiName, String sdkUrl,
            AttributeDescriptor extraAttribute,
            ElementDescriptor[] childrenElements, boolean mandatory) {
        ElementDescriptor element = new ElementDescriptor(xmlName, uiName, null, sdkUrl,
                null, childrenElements, mandatory);
        return updateElement(element, styleMap, styleName, extraAttribute);
    }
    private ElementDescriptor updateElement(ElementDescriptor element,
            Map<String, DeclareStyleableInfo> styleMap,
            String styleName,
            AttributeDescriptor extraAttribute) {
        ArrayList<AttributeDescriptor> descs = new ArrayList<AttributeDescriptor>();
        DeclareStyleableInfo style = styleMap != null ? styleMap.get(styleName) : null;
        if (style != null) {
            DescriptorsUtils.appendAttributes(descs,
                    null,   
                    SdkConstants.NS_RESOURCES,
                    style.getAttributes(),
                    null,   
                    null);  
            element.setTooltip(style.getJavaDoc());
        }
        if (extraAttribute != null) {
            descs.add(extraAttribute);
        }
        element.setAttributes(descs.toArray(new AttributeDescriptor[descs.size()]));
        return element;
    }
    private ElementDescriptor createPreference(ViewClassInfo[] prefs,
            ViewClassInfo[] prefGroups, XmlnsAttributeDescriptor xmlns) {
        ArrayList<ElementDescriptor> newPrefs = new ArrayList<ElementDescriptor>();
        if (prefs != null) {
            for (ViewClassInfo info : prefs) {
                ElementDescriptor desc = convertPref(info);
                newPrefs.add(desc);
            }
        }
        ElementDescriptor topPreferences = null;
        ArrayList<ElementDescriptor> newGroups = new ArrayList<ElementDescriptor>();
        if (prefGroups != null) {
            for (ViewClassInfo info : prefGroups) {
                ElementDescriptor desc = convertPref(info);
                newGroups.add(desc);
                if (info.getFullClassName() == AndroidConstants.CLASS_PREFERENCES) {
                    topPreferences = desc;
                }
            }
        }
        ArrayList<ElementDescriptor> everything = new ArrayList<ElementDescriptor>();
        everything.addAll(newGroups);
        everything.addAll(newPrefs);
        ElementDescriptor[] newArray = everything.toArray(new ElementDescriptor[everything.size()]);
        for (ElementDescriptor layoutDesc : newGroups) {
            layoutDesc.setChildren(newArray);
        }
        if (topPreferences != null) {
            AttributeDescriptor[] attrs = topPreferences.getAttributes();
            AttributeDescriptor[] newAttrs = new AttributeDescriptor[attrs.length + 1];
            System.arraycopy(attrs, 0, newAttrs, 0, attrs.length);
            newAttrs[attrs.length] = xmlns;
            return new ElementDescriptor(
                    topPreferences.getXmlName(),
                    topPreferences.getUiName(),
                    topPreferences.getTooltip(),
                    topPreferences.getSdkUrl(),
                    newAttrs,
                    topPreferences.getChildren(),
                    false );
        } else {
            return null;
        }
    }
    private ElementDescriptor convertPref(ViewClassInfo info) {
        String xml_name = info.getShortClassName();
        String tooltip = info.getJavaDoc();
        ArrayList<AttributeDescriptor> attributes = new ArrayList<AttributeDescriptor>();
        DescriptorsUtils.appendAttributes(attributes,
                null,   
                SdkConstants.NS_RESOURCES,
                info.getAttributes(),
                null,   
                null);  
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
                        null);  
            }
        }
        return new ViewElementDescriptor(xml_name,
                xml_name, 
                info.getFullClassName(),
                tooltip,
                null, 
                attributes.toArray(new AttributeDescriptor[attributes.size()]),
                null,
                null, 
                false );
    }
}
