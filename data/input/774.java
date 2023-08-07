public class JobHandlerRegistry extends SecuredDBObject {
    private static final String thisClass = JobHandlerRegistry.class.getName();
    public static final String FLD_OSNAME = "OSName";
    public static final String FLD_LOAD_AVERAGE = "LoadAverage";
    public static final String FLD_CURR_JOBNUM = "CurrentJobs";
    public static final String FLD_TIMESTAMP = "TimStamp";
    public static final String FLD_HOSTNAME = "HostName";
    public static final String FLD_STATUS = "Status";
    public static final String FLD_SERVERID = "ServerID";
    public static final String FLD_FREEMEM = "FreeMem";
    public static final String FLD_TOTALMEM = "TotalMem";
    public static final String FLD_USEDMEM = "UsedMem";
    public static final String FLD_POWERFACTOR = "PowerFactor";
    public static final String FLD_OPERATION = "ControlCommand";
    public JobHandlerRegistry() throws DBException {
        super();
    }
    public JobHandlerRegistry(ControllerRequest request) throws DBException {
        super(request);
    }
    public JobHandlerRegistry(int uid) throws DBException {
        super(uid);
    }
    protected synchronized void setupFields() throws DBException {
        setTargetTable("JOBHANDLERREGISTRY");
        setDescription("DBjobHandlerRegistry");
        addField(FLD_LOAD_AVERAGE, "int", 0, true, "jhLoadAvg");
        addField(FLD_CURR_JOBNUM, "int", 0, true, "nofJobs");
        addField(FLD_TIMESTAMP, "datetime", 0, true, "timeStamp");
        addField(FLD_STATUS, "varchar", 255, true, "jobHandlerStatus");
        addField(FLD_POWERFACTOR, "double", 0, true, "powerFactor");
        addField(FLD_TOTALMEM, "int", 0, true, "totalMemory");
        addField(FLD_FREEMEM, "int", 0, true, "freeMemory");
        addField(FLD_USEDMEM, "int", 0, true, "memoryInUse");
        addField(FLD_SERVERID, "int", 0, false, "handlingServer");
        addField(FLD_HOSTNAME, "varchar", 160, false, "jobHandlerName");
        addField(FLD_OSNAME, "varchar", 255, false, "jobHandlerOSName");
        addField(FLD_OPERATION, "varchar", 255, true, "jobHandlerControlCommand");
        addKey(FLD_SERVERID);
        addKey(FLD_HOSTNAME);
    }
}
