    final class DataHolder
    {
        public DataHolder (final boolean [][] coverage, final long stamp)
        {
            m_coverage = coverage;
            m_stamp = stamp;
        }
        public final boolean [][] m_coverage;
        public final long m_stamp;
    } 
    Object lock ();
    ICoverageData shallowCopy ();
    int size ();
    DataHolder getCoverage (ClassDescriptor cls);
    void addClass (boolean [][] coverage, String classVMName, long stamp);
} 
