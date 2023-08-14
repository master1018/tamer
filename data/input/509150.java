    abstract class Factory
    {
        public static IReportDataModel create (final IMetaData mdata, final ICoverageData cdata)
        {
            return new ReportDataModel (mdata, cdata);
        }
    } 
} 
