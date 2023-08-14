final class ReportDataModel implements IReportDataModel
{
    public synchronized IReportDataView getView (final int viewType)
    {
        if (viewType >= m_views.length) throw new IllegalArgumentException ("invalid viewType: " + viewType);
        IReportDataView view = m_views [viewType];
        if (view != null)
            return view;
        else
        {
            final boolean srcView = viewType == IReportDataView.HIER_SRC_VIEW;
            if (srcView && ! m_mdata.hasSrcFileData ())
                throw new IllegalStateException ("source file data view requested for metadata with incomplete SourceFile debug info");
            final AllItem root = new AllItem ();
            final Map  packageMap = new HashMap ();
            final Map  srcfileMap = new HashMap ();
            for (Iterator  descriptors = m_mdata.iterator (); descriptors.hasNext (); )
            {    
                final ClassDescriptor cls = (ClassDescriptor) descriptors.next ();
                String packageVMName = cls.getPackageVMName ();
                PackageItem packageItem = (PackageItem) packageMap.get (packageVMName);
                if (packageItem == null)
                {
                    final String packageName = packageVMName.length () == 0 ? "default package" : Descriptors.vmNameToJavaName (packageVMName); 
                    packageItem = new PackageItem (root, packageName, packageVMName);
                    packageMap.put (packageVMName, packageItem);
                    root.addChild (packageItem);
                }
                SrcFileItem srcfileItem = null;
                if (srcView)
                {                
                    final String srcFileName = cls.getSrcFileName ();
                    if ($assert.ENABLED) $assert.ASSERT (srcFileName != null, "src file name = null");
                    final String fullSrcFileName = Descriptors.combineVMName (packageVMName, srcFileName);
                    srcfileItem = (SrcFileItem) srcfileMap.get (fullSrcFileName);
                    if (srcfileItem == null)
                    {
                        srcfileItem = new SrcFileItem (packageItem, srcFileName, fullSrcFileName);
                        srcfileMap.put (fullSrcFileName, srcfileItem);
                        packageItem.addChild (srcfileItem);
                    }
                }
                final ICoverageData.DataHolder data = m_cdata.getCoverage (cls);
                if (data != null)
                {
                    if (data.m_stamp != cls.getStamp ())
                        throw new EMMARuntimeException (IAppErrorCodes.CLASS_STAMP_MISMATCH,
                                                        new Object [] { Descriptors.vmNameToJavaName (cls.getClassVMName ()) }); 
                }
                final boolean [][] coverage = data != null ? data.m_coverage : null;
                if ($assert.ENABLED) $assert.ASSERT (! srcView || srcfileItem != null, "null srcfileItem");
                final ClassItem classItem = srcView ? new ClassItem (srcfileItem, cls, coverage) : new ClassItem (packageItem, cls, coverage);
                final MethodDescriptor [] methods = cls.getMethods ();
                for (int m = 0; m < methods.length; ++ m)
                {
                    final MethodDescriptor method = methods [m];
                    if ((method.getStatus () & IMetadataConstants.METHOD_NO_BLOCK_DATA) != 0) continue;
                    final MethodItem methodItem = new MethodItem (classItem, m, method.getName (), method.getDescriptor (), method.getFirstLine ());                    
                    classItem.addChild (methodItem);
                }
                if (srcView)
                    srcfileItem.addChild (classItem);
                else
                    packageItem.addChild (classItem);
            }
            view = new ReportDataView (root);
            m_views [viewType] = view;
            return view;
        }
    }
    ReportDataModel (final IMetaData mdata, final ICoverageData cdata)
    {
        if (mdata == null) throw new IllegalArgumentException ("null input: mdata");
        if (cdata == null) throw new IllegalArgumentException ("null input: cdata");
        m_views = new IReportDataView [2];
        m_mdata = mdata;
        m_cdata = cdata;
    }
    private static final class ReportDataView implements IReportDataView
    {
        public IItem getRoot()
        {
            return m_root;
        }
        ReportDataView (final IItem root)
        {
            m_root = root;
        }
        private final IItem m_root;
    } 
    private final IMetaData m_mdata;
    private final ICoverageData m_cdata;
    private final IReportDataView [] m_views;
} 
