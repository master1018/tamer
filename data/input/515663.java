public class DmTraceReader extends TraceReader {
    private int mVersionNumber = 0;
    private boolean mDebug = false;
    private static final int TRACE_MAGIC = 0x574f4c53;
    private boolean mRegression;
    private ProfileProvider mProfileProvider;
    private String mTraceFileName;
    private MethodData mTopLevel;
    private ArrayList<Call> mCallList;
    private ArrayList<Call> mSwitchList;
    private HashMap<Integer, MethodData> mMethodMap;
    private HashMap<Integer, ThreadData> mThreadMap;
    private ThreadData[] mSortedThreads;
    private MethodData[] mSortedMethods;
    private long mGlobalEndTime;
    private MethodData mContextSwitch;
    private int mOffsetToData;
    private byte[] mBytes = new byte[8];
    private static final Pattern mIdNamePattern = Pattern.compile("(\\d+)\t(.*)");  
    DmTraceReader(String traceFileName, boolean regression) {
        mTraceFileName = traceFileName;
        mRegression = regression;
        mMethodMap = new HashMap<Integer, MethodData>();
        mThreadMap = new HashMap<Integer, ThreadData>();
        mTopLevel = new MethodData(0, "(toplevel)");
        mContextSwitch = new MethodData(-1, "(context switch)");
        mMethodMap.put(0, mTopLevel);
        generateTrees();
    }
    void generateTrees() {
        try {
            long offset = parseKeys();
            parseData(offset);
            analyzeData();
        } catch (IOException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }
    @Override
    public ProfileProvider getProfileProvider() {
        if (mProfileProvider == null)
            mProfileProvider = new ProfileProvider(this);
        return mProfileProvider;
    }
    Call readCall(MappedByteBuffer buffer, Call call) {
        int threadId;
        int methodId;
        long time;
        try {
            if (mVersionNumber == 1)
                threadId = buffer.get();
            else
                threadId = buffer.getShort();
            methodId = buffer.getInt();
            time = buffer.getInt();
        } catch (BufferUnderflowException ex) {
            return null;
        }
        int methodAction = methodId & 0x03;
        methodId = methodId & ~0x03;
        MethodData methodData = mMethodMap.get(methodId);
        if (methodData == null) {
            String name = String.format("(0x%1$x)", methodId);  
            methodData = new MethodData(methodId, name);
        }
        if (call != null) {
            call.set(threadId, methodData, time, methodAction);
        } else {
            call = new Call(threadId, methodData, time, methodAction);
        }
        return call;
    }
    private MappedByteBuffer mapFile(String filename, long offset) {
        MappedByteBuffer buffer = null;
        try {
            FileInputStream dataFile = new FileInputStream(filename);
            File file = new File(filename);
            FileChannel fc = dataFile.getChannel();
            buffer = fc.map(FileChannel.MapMode.READ_ONLY, offset, file.length() - offset);
            buffer.order(ByteOrder.LITTLE_ENDIAN);
        } catch (FileNotFoundException ex) {
            System.err.println(ex.getMessage());
            System.exit(1);
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
            System.exit(1);
        }
        return buffer;
    }
    private void readDataFileHeader(MappedByteBuffer buffer) {
        int magic = buffer.getInt();
        if (magic != TRACE_MAGIC) {
            System.err.printf(
                    "Error: magic number mismatch; got 0x%x, expected 0x%x\n",
                    magic, TRACE_MAGIC);
            throw new RuntimeException();
        }
        int version = buffer.getShort();
        mOffsetToData = buffer.getShort() - 16;
        buffer.getLong();
        for (int ii = 0; ii < mOffsetToData; ii++) {
            buffer.get();
        }
        buffer.mark();
    }
    private void parseData(long offset) {
        MappedByteBuffer buffer = mapFile(mTraceFileName, offset);
        readDataFileHeader(buffer);
        parseDataPass1(buffer);
        buffer.reset();
        parseDataPass2(buffer);
    }
    private void parseDataPass1(MappedByteBuffer buffer) {
        mSwitchList = new ArrayList<Call>();
        Call call = new Call();
        call = readCall(buffer, call);
        if (call == null)
            return;
        long callTime = call.mThreadStartTime;
        long prevCallTime = 0;
        ThreadData threadData = mThreadMap.get(call.getThreadId());
        if (threadData == null) {
            String name = String.format("[%1$d]", call.getThreadId());  
            threadData = new ThreadData(call.getThreadId(), name, mTopLevel);
            mThreadMap.put(call.getThreadId(), threadData);
        }
        ThreadData prevThreadData = threadData;
        while (true) {
            if (prevThreadData != threadData) {
                Call switchEnter = new Call(prevThreadData.getId(),
                        mContextSwitch, prevCallTime, 0);
                prevThreadData.setLastContextSwitch(switchEnter);
                mSwitchList.add(switchEnter);
                Call contextSwitch = threadData.getLastContextSwitch();
                if (contextSwitch != null) {
                    long prevStartTime = contextSwitch.mThreadStartTime;
                    long elapsed = callTime - prevStartTime;
                    long beforeSwitch = elapsed / 2;
                    long afterSwitch = elapsed - beforeSwitch;
                    long exitTime = callTime - afterSwitch;
                    contextSwitch.mThreadStartTime = prevStartTime + beforeSwitch;
                    Call switchExit = new Call(threadData.getId(),
                            mContextSwitch, exitTime, 1);
                    mSwitchList.add(switchExit);
                }
                prevThreadData = threadData;
            }
            call = readCall(buffer, call);
            if (call == null) {
                break;
            }
            prevCallTime = callTime;
            callTime = call.mThreadStartTime;
            threadData = mThreadMap.get(call.getThreadId());
            if (threadData == null) {
                String name = String.format("[%d]", call.getThreadId());
                threadData = new ThreadData(call.getThreadId(), name, mTopLevel);
                mThreadMap.put(call.getThreadId(), threadData);
            }
        }
    }
    void parseDataPass2(MappedByteBuffer buffer) {
        mCallList = new ArrayList<Call>();
        Call call = readCall(buffer, null);
        long callTime = call.mThreadStartTime;
        long prevCallTime = callTime;
        ThreadData threadData = mThreadMap.get(call.getThreadId());
        ThreadData prevThreadData = threadData;
        threadData.setGlobalStartTime(0);
        int nthContextSwitch = 0;
        long globalTime = 0;
        while (true) {
            long elapsed = callTime - prevCallTime;
            if (threadData != prevThreadData) {
                Call contextSwitch = mSwitchList.get(nthContextSwitch++);
                mCallList.add(contextSwitch);
                elapsed = contextSwitch.mThreadStartTime - prevCallTime;
                globalTime += elapsed;
                elapsed = 0;
                contextSwitch.mGlobalStartTime = globalTime;
                prevThreadData.handleCall(contextSwitch, globalTime);
                if (!threadData.isEmpty()) {
                    contextSwitch = mSwitchList.get(nthContextSwitch++);
                    mCallList.add(contextSwitch);
                    contextSwitch.mGlobalStartTime = globalTime;
                    elapsed = callTime - contextSwitch.mThreadStartTime;
                    threadData.handleCall(contextSwitch, globalTime);
                }
                if (threadData.getGlobalStartTime() == -1)
                    threadData.setGlobalStartTime(globalTime);
                prevThreadData = threadData;
            }
            globalTime += elapsed;
            call.mGlobalStartTime = globalTime;
            threadData.handleCall(call, globalTime);
            mCallList.add(call);
            call = readCall(buffer, null);
            if (call == null) {
                break;
            }
            prevCallTime = callTime;
            callTime = call.mThreadStartTime;
            threadData = mThreadMap.get(call.getThreadId());
        }
        for (int id : mThreadMap.keySet()) {
            threadData = mThreadMap.get(id);
            long endTime = threadData.endTrace();
            if (endTime > 0)
                mTopLevel.addElapsedInclusive(endTime, false, null);
        }
        mGlobalEndTime = globalTime;
        if (mRegression) {
            dumpCallTimes();
        }
    }
    static final int PARSE_VERSION = 0;
    static final int PARSE_THREADS = 1;
    static final int PARSE_METHODS = 2;
    static final int PARSE_OPTIONS = 4;
    long parseKeys() throws IOException {
        BufferedReader in = null;
        try {
            in = new BufferedReader(new FileReader(mTraceFileName));
        } catch (FileNotFoundException ex) {
            System.err.println(ex.getMessage());
        }
        long offset = 0;
        int mode = PARSE_VERSION;
        String line = null;
        while (true) {
            line = in.readLine();
            if (line == null) {
                throw new IOException("Key section does not have an *end marker");
            }
            offset += line.length() + 1;
            if (line.startsWith("*")) {
                if (line.equals("*version")) {
                    mode = PARSE_VERSION;
                    continue;
                }
                if (line.equals("*threads")) {
                    mode = PARSE_THREADS;
                    continue;
                }
                if (line.equals("*methods")) {
                    mode = PARSE_METHODS;
                    continue;
                }
                if (line.equals("*end")) {
                    return offset;
                }
            }
            switch (mode) {
            case PARSE_VERSION:
                mVersionNumber = Integer.decode(line);
                mode = PARSE_OPTIONS;
                break;
            case PARSE_THREADS:
                parseThread(line);
                break;
            case PARSE_METHODS:
                parseMethod(line);
                break;
            case PARSE_OPTIONS:
                break;
            }
        }
    }
    void parseThread(String line) {
        String idStr = null;
        String name = null;
        Matcher matcher = mIdNamePattern.matcher(line);
        if (matcher.find()) {
            idStr = matcher.group(1);
            name = matcher.group(2);
        }
        if (idStr == null) return;
        if (name == null) name = "(unknown)";
        int id = Integer.decode(idStr);
        mThreadMap.put(id, new ThreadData(id, name, mTopLevel));
    }
    void parseMethod(String line) {
        String[] tokens = line.split("\t");
        int id = Long.decode(tokens[0]).intValue();
        String className = tokens[1];
        String methodName = null;
        String signature = null;
        String pathname = null;
        int lineNumber = -1;
        if (tokens.length == 6) {
            methodName = tokens[2];
            signature = tokens[3];
            pathname = tokens[4];
            lineNumber = Integer.decode(tokens[5]);
            pathname = constructPathname(className, pathname);
        } else if (tokens.length > 2) {
            if (tokens[3].startsWith("(")) {
                methodName = tokens[2];
                signature = tokens[3];
            } else {
                pathname = tokens[2];
                lineNumber = Integer.decode(tokens[3]);
            }
        }
        mMethodMap.put(id, new MethodData(id, className, methodName, signature,
                pathname, lineNumber));
    }
    private String constructPathname(String className, String pathname) {
        int index = className.lastIndexOf('/');
        if (index > 0 && index < className.length() - 1
                && pathname.endsWith(".java"))
            pathname = className.substring(0, index + 1) + pathname;
        return pathname;
    }
    private void analyzeData() {
        Collection<ThreadData> tv = mThreadMap.values();
        mSortedThreads = tv.toArray(new ThreadData[tv.size()]);
        Arrays.sort(mSortedThreads, new Comparator<ThreadData>() {
            public int compare(ThreadData td1, ThreadData td2) {
                if (td2.getCpuTime() > td1.getCpuTime())
                    return 1;
                if (td2.getCpuTime() < td1.getCpuTime())
                    return -1;
                return td2.getName().compareTo(td1.getName());
            }
        });
        long sum = 0;
        for (ThreadData t : mSortedThreads) {
            if (t.isEmpty() == false) {
                Call root = t.getCalltreeRoot();
                root.mGlobalStartTime = t.getGlobalStartTime();
            }
        }
        Collection<MethodData> mv = mMethodMap.values();
        MethodData[] methods;
        methods = mv.toArray(new MethodData[mv.size()]);
        Arrays.sort(methods, new Comparator<MethodData>() {
            public int compare(MethodData md1, MethodData md2) {
                if (md2.getElapsedInclusive() > md1.getElapsedInclusive())
                    return 1;
                if (md2.getElapsedInclusive() < md1.getElapsedInclusive())
                    return -1;
                return md1.getName().compareTo(md2.getName());
            }
        });
        int nonZero = 0;
        for (MethodData md : methods) {
            if (md.getElapsedInclusive() == 0)
                break;
            nonZero += 1;
        }
        mSortedMethods = new MethodData[nonZero];
        int ii = 0;
        for (MethodData md : methods) {
            if (md.getElapsedInclusive() == 0)
                break;
            md.setRank(ii);
            mSortedMethods[ii++] = md;
        }
        for (MethodData md : mSortedMethods) {
            md.analyzeData();
        }
        for (Call call : mCallList) {
            call.updateName();
        }
        if (mRegression) {
            dumpMethodStats();
        }
    }
    @Override
    public ArrayList<TimeLineView.Record> getThreadTimeRecords() {
        TimeLineView.Record record;
        ArrayList<TimeLineView.Record> timeRecs;
        timeRecs = new ArrayList<TimeLineView.Record>();
        for (ThreadData threadData : mSortedThreads) {
            if (!threadData.isEmpty() && threadData.getId() != 0) {
                Call call = new Call(threadData.getId(), mTopLevel,
                        threadData.getGlobalStartTime(), 0);
                call.mGlobalStartTime = threadData.getGlobalStartTime();
                call.mGlobalEndTime = threadData.getGlobalEndTime();
                record = new TimeLineView.Record(threadData, call);
                timeRecs.add(record);
            }
        }
        for (Call call : mCallList) {
            if (call.getMethodAction() != 0 || call.getThreadId() == 0)
                continue;
            ThreadData threadData = mThreadMap.get(call.getThreadId());
            record = new TimeLineView.Record(threadData, call);
            timeRecs.add(record);
        }
        if (mRegression) {
            dumpTimeRecs(timeRecs);
            System.exit(0);
        }
        return timeRecs;
    }
    private void dumpCallTimes() {
        String action;
        System.out.format("id thread  global start,end   method\n");
        for (Call call : mCallList) {
            if (call.getMethodAction() == 0) {
                action = "+";
            } else {
                action = " ";
            }
            long callTime = call.mThreadStartTime;
            System.out.format("%2d %6d %8d %8d %s %s\n",
                    call.getThreadId(), callTime, call.mGlobalStartTime,
                    call.mGlobalEndTime, action, call.getMethodData().getName());
        }
    }
    private void dumpMethodStats() {
        System.out.format("\nExclusive Inclusive     Calls  Method\n");
        for (MethodData md : mSortedMethods) {
            System.out.format("%9d %9d %9s  %s\n",
                    md.getElapsedExclusive(), md.getElapsedInclusive(),
                    md.getCalls(), md.getProfileName());
        }
    }
    private void dumpTimeRecs(ArrayList<TimeLineView.Record> timeRecs) {
        System.out.format("\nid thread  global start,end  method\n");
        for (TimeLineView.Record record : timeRecs) {
            Call call = (Call) record.block;
            long callTime = call.mThreadStartTime;
            System.out.format("%2d %6d %8d %8d  %s\n",
                    call.getThreadId(), callTime,
                    call.mGlobalStartTime, call.mGlobalEndTime,
                    call.getMethodData().getName());
        }
    }
    @Override
    public HashMap<Integer, String> getThreadLabels() {
        HashMap<Integer, String> labels = new HashMap<Integer, String>();
        for (ThreadData t : mThreadMap.values()) {
            labels.put(t.getId(), t.getName());
        }
        return labels;
    }
    @Override
    public MethodData[] getMethods() {
        return mSortedMethods;
    }
    @Override
    public ThreadData[] getThreads() {
        return mSortedThreads;
    }
    @Override
    public long getEndTime() {
        return mGlobalEndTime;
    }
}
