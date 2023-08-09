final class ClassItem extends Item
{
    public ClassItem (final IItem parent, final ClassDescriptor cls, final boolean [][] coverage)
    {
        super (parent);
        m_cls = cls;
        m_coverage = coverage;
    }
    public String getName ()
    {
        return m_cls.getName ();
    }
    public String getSrcFileName ()
    {
        return m_cls.getSrcFileName ();
    }
    public int getFirstLine ()
    {
        if (m_firstLine == 0)
        {
            final MethodDescriptor [] methods = m_cls.getMethods ();
            int firstLine = Integer.MAX_VALUE;
            for (int m = 0, mLimit = methods.length; m < mLimit; ++ m)
            {
                final int mFirstLine = methods [m].getFirstLine ();
                if ((mFirstLine > 0) && (mFirstLine < firstLine))
                    firstLine = mFirstLine;
            }
            m_firstLine = firstLine;
            return firstLine;
        }
        return m_firstLine;
    }
    public ClassDescriptor getClassDescriptor ()
    {
        return m_cls;
    }
    public boolean [][] getCoverage ()
    {
        return m_coverage;
    }
    public boolean loaded ()
    {
        return m_coverage != null;
    }
    public int getAggregate (final int type)
    {
        final int [] aggregates = m_aggregates;
        int value = aggregates [type];
        if (value < 0)
        {
            switch (type)
            {
                case COVERAGE_CLASS_COUNT:
                case    TOTAL_CLASS_COUNT:
                {
                    aggregates [TOTAL_CLASS_COUNT] = 1;
                    aggregates [COVERAGE_CLASS_COUNT] = m_coverage != null ? 1 : 0;
                    return aggregates [type];
                }
                case COVERAGE_LINE_COUNT:
                case    TOTAL_LINE_COUNT:
                case COVERAGE_LINE_INSTR:
                {
                    final boolean [][] ccoverage = m_coverage; 
                    final IntObjectMap  cldata = new IntObjectMap ();
                    final MethodDescriptor [] methoddescs = m_cls.getMethods ();
                    for (Iterator methods = getChildren (); methods.hasNext (); )
                    {
                        final MethodItem method = (MethodItem) methods.next ();
                        final int methodID = method.getID ();
                        final boolean [] mcoverage = ccoverage == null ? null : ccoverage [methodID];
                        final MethodDescriptor methoddesc = methoddescs [methodID];                        
                        final int [] mbsizes = methoddesc.getBlockSizes ();
                        final IntObjectMap mlineMap = methoddesc.getLineMap ();
                        if ($assert.ENABLED) $assert.ASSERT (mlineMap != null);
                        final int [] mlines = mlineMap.keys ();
                        for (int ml = 0, mlLimit = mlines.length; ml < mlLimit; ++ ml)
                        {
                            final int mline = mlines [ml];
                            int [] data = (int []) cldata.get (mline);
                            if (data == null)
                            {
                                data = new int [4]; 
                                cldata.put (mline, data);
                            }
                            final int [] lblocks = (int []) mlineMap.get (mline);
                            final int bCount = lblocks.length; 
                            data [0] += bCount;
                            for (int bID = 0; bID < bCount; ++ bID)
                            {
                                final int block = lblocks [bID];
                                final boolean bcovered = mcoverage != null && mcoverage [block];
                                final int instr = mbsizes [block];
                                data [1] += instr;
                                if (bcovered)
                                {
                                    ++ data [2];
                                    data [3] += instr;
                                }
                            }
                        }
                    }
                    aggregates [TOTAL_LINE_COUNT] = cldata.size ();
                    int coverageLineCount = 0;
                    int coverageLineInstr = 0;
                    final int [] clines = cldata.keys ();
                    for (int cl = 0, clLimit = clines.length; cl < clLimit; ++ cl)
                    {
                        final int cline = clines [cl];
                        final int [] data = (int []) cldata.get (cline);
                        final int ltotalCount = data [0];
                        final int ltotalInstr = data [1];
                        final int lcoverageCount = data [2];
                        final int lcoverageInstr = data [3];
                        if (lcoverageInstr > 0)
                        {
                            coverageLineCount += (PRECISION * lcoverageCount) / ltotalCount;
                            coverageLineInstr += (PRECISION * lcoverageInstr) / ltotalInstr;
                        }
                    }
                    aggregates [COVERAGE_LINE_COUNT] = coverageLineCount;
                    aggregates [COVERAGE_LINE_INSTR] = coverageLineInstr;
                    return aggregates [type];
                }
                default: return super.getAggregate (type);
            }
        }
        return value;
    }
    public void accept (final IItemVisitor visitor, final Object ctx)
    {
        visitor.visit (this, ctx);
    }
    public final IItemMetadata getMetadata ()
    {
        return METADATA;
    }
    public static IItemMetadata getTypeMetadata ()
    {
        return METADATA;
    }
    final ClassDescriptor m_cls;
    final boolean [][] m_coverage;
    private int m_firstLine;
    private static final Item.ItemMetadata METADATA; 
    static
    {
        METADATA = new Item.ItemMetadata (IItemMetadata.TYPE_ID_CLASS, "class",
            1 << IItemAttribute.ATTRIBUTE_NAME_ID |
            1 << IItemAttribute.ATTRIBUTE_CLASS_COVERAGE_ID |
            1 << IItemAttribute.ATTRIBUTE_METHOD_COVERAGE_ID |
            1 << IItemAttribute.ATTRIBUTE_BLOCK_COVERAGE_ID |
            1 << IItemAttribute.ATTRIBUTE_LINE_COVERAGE_ID);
    }
} 
