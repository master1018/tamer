abstract class AbstractReportGenerator extends AbstractItemVisitor
                                       implements IReportGenerator
{
    public static IReportGenerator create (final String type)
    {
        if ((type == null) || (type.length () == 0))
            throw new IllegalArgumentException ("null/empty input: type");
        if ("html".equals (type))
            return new com.vladium.emma.report.html.ReportGenerator ();
        else if ("lcov".equals (type))
            return new com.vladium.emma.report.lcov.ReportGenerator ();
        else if ("txt".equals (type))
            return new com.vladium.emma.report.txt.ReportGenerator ();
        else if ("xml".equals (type))
            return new com.vladium.emma.report.xml.ReportGenerator ();
        else 
            throw new EMMARuntimeException ("no report generator class found for type [" + type + "]");
    }
    public void initialize (final IMetaData mdata, final ICoverageData cdata,
                            final SourcePathCache cache, final IProperties properties)
        throws EMMARuntimeException
    {
        m_log = Logger.getLogger ();
        m_verbose = m_log.atVERBOSE ();
        m_settings = ReportProperties.parseProperties (properties, getType ());
        m_cache = cache;
        m_hasSrcFileInfo = mdata.hasSrcFileData ();
        m_hasLineNumberInfo = mdata.hasLineNumberData ();
        boolean debugInfoWarning = false;
        boolean bailOut = false;
        if (! mdata.hasSrcFileData () && (m_settings.getViewType () == IReportDataView.HIER_SRC_VIEW))
        {
            debugInfoWarning = true;
            m_log.warning ("not all instrumented classes were compiled with source file");
            m_log.warning ("debug data: no sources will be embedded in the report.");
            m_settings.setViewType (IReportDataView.HIER_CLS_VIEW);
        }
        if (! m_hasLineNumberInfo)
        {
            final int [] userColumnIDs = m_settings.getColumnOrder ();
            final IntVector columnIDs = new IntVector ();
            boolean removed = false;
            for (int c = 0; c < userColumnIDs.length; ++ c)
            {
                if (userColumnIDs [c] == IItemAttribute.ATTRIBUTE_LINE_COVERAGE_ID)
                    removed = true;
                else
                    columnIDs.add (userColumnIDs [c]);
            }
            if (removed)
            {
                debugInfoWarning = true;
                if (columnIDs.size () == 0)
                {
                    m_log.warning ("line coverage requested in a report of type [" + getType () + "] but");
                    m_log.warning ("not all instrumented classes were compiled with line number");
                    m_log.warning ("debug data: since this was the only requested column, no report will be generated.");
                    bailOut = true;
                }
                else
                {
                    m_log.warning ("line coverage requested in a report of type [" + getType () + "] but");
                    m_log.warning ("not all instrumented classes were compiled with line number");
                    m_log.warning ("debug data: this column will be removed from the report.");
                    m_settings.setColumnOrder (columnIDs.values ());
                    final int [] userSort = m_settings.getSortOrder ();
                    final IntVector sort = new IntVector ();
                    for (int c = 0; c < userSort.length; c += 2)
                    {
                        if (Math.abs (userSort [c]) != IItemAttribute.ATTRIBUTE_LINE_COVERAGE_ID)
                        {
                            sort.add (userSort [c]);
                            sort.add (userSort [c + 1]);
                        }
                    }
                    m_settings.setSortOrder (sort.values ());
                }
            }
        }
        if (debugInfoWarning && m_log.atINFO ())
        {
            final Set  sampleClassNames = new TreeSet ();
            final ObjectIntMap  countMap = new ObjectIntMap ();
            final int [] _count = new int [1];
            for (Iterator  descriptors = mdata.iterator (); descriptors.hasNext (); )
            {    
                final ClassDescriptor cls = (ClassDescriptor) descriptors.next ();
                if (! cls.hasCompleteLineNumberInfo () || ! cls.hasSrcFileInfo ())
                {
                    final String packageVMName = cls.getPackageVMName ();
                    final int count = countMap.get (packageVMName, _count)
                        ? _count [0]
                        : 0;
                    if (count < MAX_DEBUG_INFO_WARNING_COUNT)
                    {
                        sampleClassNames.add (Descriptors.vmNameToJavaName (cls.getClassVMName ()));
                        countMap.put (packageVMName, count + 1);
                    }
                }
            }
            m_log.info ("showing up to " + MAX_DEBUG_INFO_WARNING_COUNT + " classes without full debug info per package:");
            for (Iterator  names = sampleClassNames.iterator (); names.hasNext (); )
            {
                m_log.info ("  " + names.next ());
            }
        }
        if (bailOut)
        {
            throw new EMMARuntimeException ("BAILED OUT");
        }
        final IItemMetadata [] allTypes = IItemMetadata.Factory.getAllTypes ();
        m_typeSortComparators = new ItemComparator [allTypes.length];
        for (int t = 0; t < allTypes.length; ++ t)
        {
            final IntVector orderedAttrIDsWithDir = new IntVector ();
            final long typeAttrIDSet = allTypes [t].getAttributeIDs ();
            for (int s = 0; s < m_settings.getSortOrder ().length; s += 2)
            {
                final int attrID = m_settings.getSortOrder () [s];
                if ((typeAttrIDSet & (1 << attrID)) != 0)
                {
                    orderedAttrIDsWithDir.add (attrID);
                    final int dir = m_settings.getSortOrder () [s + 1];
                    orderedAttrIDsWithDir.add (dir);
                }
            }
            m_typeSortComparators [t] = ItemComparator.Factory.create (orderedAttrIDsWithDir.values (), m_settings.getUnitsType ());
        }
        m_metrics = new int [allTypes.length];
        final IntIntMap metrics = m_settings.getMetrics ();
        for (int t = 0; t < m_metrics.length; ++ t)
        {
            m_metrics [t] = -1;
            metrics.get (t, m_metrics, t);
        }
        final IReportDataModel model = IReportDataModel.Factory.create (mdata, cdata);
        m_view = model.getView (m_settings.getViewType ());
        m_srcView = (m_settings.getViewType () == IReportDataView.HIER_SRC_VIEW);
    }
    public void cleanup ()
    {
        reset ();
    }
    protected void reset ()
    {
        m_settings = null;
        m_cache = null;
        m_view = null;
        m_srcView = false;
        m_typeSortComparators = null;
        m_metrics = null;
        m_log = null;
    }
    protected ReportProperties.ParsedProperties m_settings;
    protected SourcePathCache m_cache;
    protected IReportDataView m_view;
    protected boolean m_srcView;
    protected boolean m_hasSrcFileInfo, m_hasLineNumberInfo;
    protected ItemComparator [] m_typeSortComparators; 
    protected int [] m_metrics; 
    protected Logger m_log; 
    protected boolean m_verbose;
    private static final int MAX_DEBUG_INFO_WARNING_COUNT = 3; 
} 
