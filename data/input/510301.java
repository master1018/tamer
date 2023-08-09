class RTCoverageDataPersister
{
    static void dumpCoverageData (final ICoverageData cdata, final boolean useSnapshot,
                                  final File outFile, final boolean merge)
    {
        try
        {
            if (cdata != null)
            {
                final Logger log = Logger.getLogger ();
                final boolean info = log.atINFO ();
                final long start = info ? System.currentTimeMillis () : 0;
                {
                    final ICoverageData cdataView = useSnapshot ? cdata.shallowCopy () : cdata;
                    synchronized (Object.class) 
                    {
                        DataFactory.persist (cdataView, outFile, merge);
                    }
                }
                if (info)
                {
                    final long end = System.currentTimeMillis ();
                    log.info ("runtime coverage data " + (merge ? "merged into" : "written to") + " [" + outFile.getAbsolutePath () + "] {in " + (end - start) + " ms}");
                }
            }
        }
        catch (Throwable t)
        {
            t.printStackTrace ();
            throw new RuntimeException (IAppConstants.APP_NAME + " failed to dump coverage data: " + t.toString ());
        }
    }
} 
