public class LayoutParamsParser {
    final static class ExtViewClassInfo extends ViewClassInfo {
        private boolean mIsInstantiable;
        ExtViewClassInfo(boolean instantiable, boolean isLayout, String canonicalClassName,
                String shortClassName) {
            super(isLayout, canonicalClassName, shortClassName);
            mIsInstantiable = instantiable;
        }
        boolean isInstantiable() {
            return mIsInstantiable;
        }
    }
    protected IClassDescriptor mTopViewClass;
    protected IClassDescriptor mTopGroupClass;
    protected IClassDescriptor mTopLayoutParamsClass;
    protected ArrayList<IClassDescriptor> mViewList;
    protected ArrayList<IClassDescriptor> mGroupList;
    protected TreeMap<String, ExtViewClassInfo> mViewMap;
    protected TreeMap<String, ExtViewClassInfo> mGroupMap;
    protected HashMap<String, LayoutParamsInfo> mLayoutParamsMap;
    protected AttrsXmlParser mAttrsXmlParser;
    protected IAndroidClassLoader mClassLoader;
    public LayoutParamsParser(IAndroidClassLoader classLoader,
            AttrsXmlParser attrsXmlParser) {
        mClassLoader = classLoader;
        mAttrsXmlParser = attrsXmlParser;
    }
    public List<ViewClassInfo> getViews() {
        return getInstantiables(mViewMap);
    }
    public List<ViewClassInfo> getGroups() {
        return getInstantiables(mGroupMap);
    }
    public void parseLayoutClasses(IProgressMonitor monitor) {
        parseClasses(monitor,
                AndroidConstants.CLASS_VIEW,
                AndroidConstants.CLASS_VIEWGROUP,
                AndroidConstants.CLASS_VIEWGROUP_LAYOUTPARAMS);
    }
    public void parsePreferencesClasses(IProgressMonitor monitor) {
        parseClasses(monitor,
                AndroidConstants.CLASS_PREFERENCE,
                AndroidConstants.CLASS_PREFERENCEGROUP,
                null  );
    }
    private void parseClasses(IProgressMonitor monitor,
            String rootClassName,
            String groupClassName,
            String paramsClassName) {
        try {
            SubMonitor progress = SubMonitor.convert(monitor, 100);
            String[] superClasses = new String[2 + (paramsClassName == null ? 0 : 1)];
            superClasses[0] = groupClassName;
            superClasses[1] = rootClassName;
            if (paramsClassName != null) {
                superClasses[2] = paramsClassName;
            }
            HashMap<String, ArrayList<IClassDescriptor>> found =
                    mClassLoader.findClassesDerivingFrom("android.", superClasses);  
            mTopViewClass = mClassLoader.getClass(rootClassName);
            mTopGroupClass = mClassLoader.getClass(groupClassName);
            if (paramsClassName != null) {
                mTopLayoutParamsClass = mClassLoader.getClass(paramsClassName);
            }
            mViewList = found.get(rootClassName);
            mGroupList = found.get(groupClassName);
            mViewMap = new TreeMap<String, ExtViewClassInfo>();
            mGroupMap = new TreeMap<String, ExtViewClassInfo>();
            if (mTopLayoutParamsClass != null) {
                mLayoutParamsMap = new HashMap<String, LayoutParamsInfo>();
            }
            if (mTopGroupClass != null) {
                addGroup(mTopGroupClass);
            }
            if (mTopViewClass != null) {
                addView(mTopViewClass);
            }
            ExtViewClassInfo vg = mGroupMap.get(groupClassName);
            if (vg != null) {
                vg.setSuperClass(mViewMap.get(rootClassName));
            }
            progress.setWorkRemaining(mGroupList.size() + mViewList.size());
            for (IClassDescriptor groupChild : mGroupList) {
                addGroup(groupChild);
                progress.worked(1);
            }
            for (IClassDescriptor viewChild : mViewList) {
                if (viewChild != mTopGroupClass) {
                    addView(viewChild);
                }
                progress.worked(1);
            }
        } catch (ClassNotFoundException e) {
            AdtPlugin.log(e, "Problem loading class %1$s or %2$s",  
                    rootClassName, groupClassName);
        } catch (InvalidAttributeValueException e) {
            AdtPlugin.log(e, "Problem loading classes"); 
        } catch (ClassFormatError e) {
            AdtPlugin.log(e, "Problem loading classes"); 
        } catch (IOException e) {
            AdtPlugin.log(e, "Problem loading classes"); 
        }
    }
    private ExtViewClassInfo addView(IClassDescriptor viewClass) {
        String fqcn = viewClass.getFullClassName();
        if (mViewMap.containsKey(fqcn)) {
            return mViewMap.get(fqcn);
        } else if (mGroupMap.containsKey(fqcn)) {
            return mGroupMap.get(fqcn);
        }
        ExtViewClassInfo info = new ExtViewClassInfo(viewClass.isInstantiable(),
                false , fqcn, viewClass.getSimpleName());
        mViewMap.put(fqcn, info);
        if (viewClass.equals(mTopViewClass) == false) {
            IClassDescriptor superClass = viewClass.getSuperclass();
            ExtViewClassInfo superClassInfo = addView(superClass);
            info.setSuperClass(superClassInfo);
        }
        mAttrsXmlParser.loadViewAttributes(info);
        return info;
    }
    private ExtViewClassInfo addGroup(IClassDescriptor groupClass) {
        String fqcn = groupClass.getFullClassName();
        if (mGroupMap.containsKey(fqcn)) {
            return mGroupMap.get(fqcn);
        }
        ExtViewClassInfo info = new ExtViewClassInfo(groupClass.isInstantiable(),
                true , fqcn, groupClass.getSimpleName());
        mGroupMap.put(fqcn, info);
        IClassDescriptor superClass = groupClass.getSuperclass();
        if (superClass != null && superClass.equals(mTopViewClass) == false) {
            ExtViewClassInfo superClassInfo = addGroup(superClass);
            if (superClassInfo != null && superClassInfo != info) {
                info.setSuperClass(superClassInfo);
            }
        }
        mAttrsXmlParser.loadViewAttributes(info);
        if (mTopLayoutParamsClass != null) {
            info.setLayoutParams(addLayoutParams(groupClass));
        }
        return info;
    }
    private LayoutParamsInfo addLayoutParams(IClassDescriptor groupClass) {
        IClassDescriptor layoutParamsClass = findLayoutParams(groupClass);
        if (layoutParamsClass == null) {
            for (IClassDescriptor superClass = groupClass.getSuperclass();
                    layoutParamsClass == null &&
                        superClass != null &&
                        superClass.equals(mTopViewClass) == false;
                    superClass = superClass.getSuperclass()) {
                layoutParamsClass = findLayoutParams(superClass);
            }
        }
        if (layoutParamsClass != null) {
            return getLayoutParamsInfo(layoutParamsClass);
        }
        return null;
    }
    private LayoutParamsInfo getLayoutParamsInfo(IClassDescriptor layoutParamsClass) {
        String fqcn = layoutParamsClass.getFullClassName();
        LayoutParamsInfo layoutParamsInfo = mLayoutParamsMap.get(fqcn);
        if (layoutParamsInfo != null) {
            return layoutParamsInfo;
        }
        LayoutParamsInfo superClassInfo = null;
        if (layoutParamsClass.equals(mTopLayoutParamsClass) == false) {
            IClassDescriptor superClass = layoutParamsClass.getSuperclass();
            superClassInfo = getLayoutParamsInfo(superClass);
        }
        ExtViewClassInfo enclosingGroupInfo = addGroup(layoutParamsClass.getEnclosingClass());
        layoutParamsInfo = new ExtViewClassInfo.LayoutParamsInfo(
                enclosingGroupInfo, layoutParamsClass.getSimpleName(), superClassInfo);
        mLayoutParamsMap.put(fqcn, layoutParamsInfo);
        mAttrsXmlParser.loadLayoutParamsAttributes(layoutParamsInfo);
        return layoutParamsInfo;
    }
    private IClassDescriptor findLayoutParams(IClassDescriptor groupClass) {
        IClassDescriptor[] innerClasses = groupClass.getDeclaredClasses();
        for (IClassDescriptor innerClass : innerClasses) {
            if (innerClass.getSimpleName().equals(AndroidConstants.CLASS_NAME_LAYOUTPARAMS)) {
                return innerClass;
            }
        }
        return null;
    }
    private List<ViewClassInfo> getInstantiables(SortedMap<String, ExtViewClassInfo> map) {
        Collection<ExtViewClassInfo> values = map.values();
        ArrayList<ViewClassInfo> list = new ArrayList<ViewClassInfo>();
        for (ExtViewClassInfo info : values) {
            if (info.isInstantiable()) {
                list.add(info);
            }
        }
        return list;
    }
}
