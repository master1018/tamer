final class HandleHeap extends ChunkHandler {
    public static final int CHUNK_HPIF = type("HPIF");
    public static final int CHUNK_HPST = type("HPST");
    public static final int CHUNK_HPEN = type("HPEN");
    public static final int CHUNK_HPSG = type("HPSG");
    public static final int CHUNK_HPGC = type("HPGC");
    public static final int CHUNK_HPDU = type("HPDU");
    public static final int CHUNK_HPDS = type("HPDS");
    public static final int CHUNK_REAE = type("REAE");
    public static final int CHUNK_REAQ = type("REAQ");
    public static final int CHUNK_REAL = type("REAL");
    public static final int WHEN_DISABLE = 0;
    public static final int WHEN_GC = 1;
    public static final int WHAT_MERGE = 0; 
    public static final int WHAT_OBJ = 1;   
    public static final int HPIF_WHEN_NEVER = 0;
    public static final int HPIF_WHEN_NOW = 1;
    public static final int HPIF_WHEN_NEXT_GC = 2;
    public static final int HPIF_WHEN_EVERY_GC = 3;
    private static final HandleHeap mInst = new HandleHeap();
    private HandleHeap() {}
    public static void register(MonitorThread mt) {
        mt.registerChunkHandler(CHUNK_HPIF, mInst);
        mt.registerChunkHandler(CHUNK_HPST, mInst);
        mt.registerChunkHandler(CHUNK_HPEN, mInst);
        mt.registerChunkHandler(CHUNK_HPSG, mInst);
        mt.registerChunkHandler(CHUNK_HPDS, mInst);
        mt.registerChunkHandler(CHUNK_REAQ, mInst);
        mt.registerChunkHandler(CHUNK_REAL, mInst);
    }
    @Override
    public void clientReady(Client client) throws IOException {
        if (client.isHeapUpdateEnabled()) {
            sendHPIF(client, HPIF_WHEN_EVERY_GC);
        }
    }
    @Override
    public void clientDisconnected(Client client) {}
    @Override
    public void handleChunk(Client client, int type, ByteBuffer data, boolean isReply, int msgId) {
        Log.d("ddm-heap", "handling " + ChunkHandler.name(type));
        if (type == CHUNK_HPIF) {
            handleHPIF(client, data);
        } else if (type == CHUNK_HPST) {
            handleHPST(client, data);
        } else if (type == CHUNK_HPEN) {
            handleHPEN(client, data);
        } else if (type == CHUNK_HPSG) {
            handleHPSG(client, data);
        } else if (type == CHUNK_HPDU) {
            handleHPDU(client, data);
        } else if (type == CHUNK_HPDS) {
            handleHPDS(client, data);
        } else if (type == CHUNK_REAQ) {
            handleREAQ(client, data);
        } else if (type == CHUNK_REAL) {
            handleREAL(client, data);
        } else {
            handleUnknownChunk(client, type, data, isReply, msgId);
        }
    }
    private void handleHPIF(Client client, ByteBuffer data) {
        Log.d("ddm-heap", "HPIF!");
        try {
            int numHeaps = data.getInt();
            for (int i = 0; i < numHeaps; i++) {
                int heapId = data.getInt();
                @SuppressWarnings("unused")
                long timeStamp = data.getLong();
                @SuppressWarnings("unused")
                byte reason = data.get();
                long maxHeapSize = (long)data.getInt() & 0x00ffffffff;
                long heapSize = (long)data.getInt() & 0x00ffffffff;
                long bytesAllocated = (long)data.getInt() & 0x00ffffffff;
                long objectsAllocated = (long)data.getInt() & 0x00ffffffff;
                client.getClientData().setHeapInfo(heapId, maxHeapSize,
                        heapSize, bytesAllocated, objectsAllocated);
                client.update(Client.CHANGE_HEAP_DATA);
            }
        } catch (BufferUnderflowException ex) {
            Log.w("ddm-heap", "malformed HPIF chunk from client");
        }
    }
    public static void sendHPIF(Client client, int when) throws IOException {
        ByteBuffer rawBuf = allocBuffer(1);
        JdwpPacket packet = new JdwpPacket(rawBuf);
        ByteBuffer buf = getChunkDataBuf(rawBuf);
        buf.put((byte)when);
        finishChunkPacket(packet, CHUNK_HPIF, buf.position());
        Log.d("ddm-heap", "Sending " + name(CHUNK_HPIF) + ": when=" + when);
        client.sendAndConsume(packet, mInst);
    }
    private void handleHPST(Client client, ByteBuffer data) {
        client.getClientData().getVmHeapData().clearHeapData();
    }
    private void handleHPEN(Client client, ByteBuffer data) {
        client.getClientData().getVmHeapData().sealHeapData();
        client.update(Client.CHANGE_HEAP_DATA);
    }
    private void handleHPSG(Client client, ByteBuffer data) {
        byte dataCopy[] = new byte[data.limit()];
        data.rewind();
        data.get(dataCopy);
        data = ByteBuffer.wrap(dataCopy);
        client.getClientData().getVmHeapData().addHeapData(data);
    }
    public static void sendHPSG(Client client, int when, int what)
        throws IOException {
        ByteBuffer rawBuf = allocBuffer(2);
        JdwpPacket packet = new JdwpPacket(rawBuf);
        ByteBuffer buf = getChunkDataBuf(rawBuf);
        buf.put((byte)when);
        buf.put((byte)what);
        finishChunkPacket(packet, CHUNK_HPSG, buf.position());
        Log.d("ddm-heap", "Sending " + name(CHUNK_HPSG) + ": when="
            + when + ", what=" + what);
        client.sendAndConsume(packet, mInst);
    }
    public static void sendHPGC(Client client)
        throws IOException {
        ByteBuffer rawBuf = allocBuffer(0);
        JdwpPacket packet = new JdwpPacket(rawBuf);
        ByteBuffer buf = getChunkDataBuf(rawBuf);
        finishChunkPacket(packet, CHUNK_HPGC, buf.position());
        Log.d("ddm-heap", "Sending " + name(CHUNK_HPGC));
        client.sendAndConsume(packet, mInst);
    }
    public static void sendHPDU(Client client, String fileName)
        throws IOException {
        ByteBuffer rawBuf = allocBuffer(4 + fileName.length() * 2);
        JdwpPacket packet = new JdwpPacket(rawBuf);
        ByteBuffer buf = getChunkDataBuf(rawBuf);
        buf.putInt(fileName.length());
        putString(buf, fileName);
        finishChunkPacket(packet, CHUNK_HPDU, buf.position());
        Log.d("ddm-heap", "Sending " + name(CHUNK_HPDU) + " '" + fileName +"'");
        client.sendAndConsume(packet, mInst);
        client.getClientData().setPendingHprofDump(fileName);
    }
    public static void sendHPDS(Client client)
        throws IOException {
        ByteBuffer rawBuf = allocBuffer(0);
        JdwpPacket packet = new JdwpPacket(rawBuf);
        ByteBuffer buf = getChunkDataBuf(rawBuf);
        finishChunkPacket(packet, CHUNK_HPDS, buf.position());
        Log.d("ddm-heap", "Sending " + name(CHUNK_HPDS));
        client.sendAndConsume(packet, mInst);
    }
    private void handleHPDU(Client client, ByteBuffer data) {
        byte result;
        String filename = client.getClientData().getPendingHprofDump();
        client.getClientData().setPendingHprofDump(null);
        result = data.get();
        IHprofDumpHandler handler = ClientData.getHprofDumpHandler();
        if (handler != null) {
            if (result == 0) {
                handler.onSuccess(filename, client);
                Log.d("ddm-heap", "Heap dump request has finished");
            } else {
                handler.onEndFailure(client, null);
                Log.w("ddm-heap", "Heap dump request failed (check device log)");
            }
        }
    }
    private void handleHPDS(Client client, ByteBuffer data) {
        IHprofDumpHandler handler = ClientData.getHprofDumpHandler();
        if (handler != null) {
            byte[] stuff = new byte[data.capacity()];
            data.get(stuff, 0, stuff.length);
            Log.d("ddm-hprof", "got hprof file, size: " + data.capacity() + " bytes");
            handler.onSuccess(stuff, client);
        }
    }
    public static void sendREAE(Client client, boolean enable)
        throws IOException {
        ByteBuffer rawBuf = allocBuffer(1);
        JdwpPacket packet = new JdwpPacket(rawBuf);
        ByteBuffer buf = getChunkDataBuf(rawBuf);
        buf.put((byte) (enable ? 1 : 0));
        finishChunkPacket(packet, CHUNK_REAE, buf.position());
        Log.d("ddm-heap", "Sending " + name(CHUNK_REAE) + ": " + enable);
        client.sendAndConsume(packet, mInst);
    }
    public static void sendREAQ(Client client)
        throws IOException {
        ByteBuffer rawBuf = allocBuffer(0);
        JdwpPacket packet = new JdwpPacket(rawBuf);
        ByteBuffer buf = getChunkDataBuf(rawBuf);
        finishChunkPacket(packet, CHUNK_REAQ, buf.position());
        Log.d("ddm-heap", "Sending " + name(CHUNK_REAQ));
        client.sendAndConsume(packet, mInst);
    }
    public static void sendREAL(Client client)
        throws IOException {
        ByteBuffer rawBuf = allocBuffer(0);
        JdwpPacket packet = new JdwpPacket(rawBuf);
        ByteBuffer buf = getChunkDataBuf(rawBuf);
        finishChunkPacket(packet, CHUNK_REAL, buf.position());
        Log.d("ddm-heap", "Sending " + name(CHUNK_REAL));
        client.sendAndConsume(packet, mInst);
    }
    private void handleREAQ(Client client, ByteBuffer data) {
        boolean enabled;
        enabled = (data.get() != 0);
        Log.d("ddm-heap", "REAQ says: enabled=" + enabled);
        client.getClientData().setAllocationStatus(enabled ?
                AllocationTrackingStatus.ON : AllocationTrackingStatus.OFF);
        client.update(Client.CHANGE_HEAP_ALLOCATION_STATUS);
    }
    private String descriptorToDot(String str) {
        int array = 0;
        while (str.startsWith("[")) {
            str = str.substring(1);
            array++;
        }
        int len = str.length();
        if (len >= 2 && str.charAt(0) == 'L' && str.charAt(len - 1) == ';') {
            str = str.substring(1, len-1);
            str = str.replace('/', '.');
        } else {
            if ("C".equals(str)) {
                str = "char";
            } else if ("B".equals(str)) {
                str = "byte";
            } else if ("Z".equals(str)) {
                str = "boolean";
            } else if ("S".equals(str)) {
                str = "short";
            } else if ("I".equals(str)) {
                str = "int";
            } else if ("J".equals(str)) {
                str = "long";
            } else if ("F".equals(str)) {
                str = "float";
            } else if ("D".equals(str)) {
                str = "double";
            }
        }
        for (int a = 0 ; a < array; a++) {
            str = str + "[]";
        }
        return str;
    }
    private void readStringTable(ByteBuffer data, String[] strings) {
        int count = strings.length;
        int i;
        for (i = 0; i < count; i++) {
            int nameLen = data.getInt();
            String descriptor = getString(data, nameLen);
            strings[i] = descriptorToDot(descriptor);
        }
    }
    private void handleREAL(Client client, ByteBuffer data) {
        Log.e("ddm-heap", "*** Received " + name(CHUNK_REAL));
        int messageHdrLen, entryHdrLen, stackFrameLen;
        int numEntries, offsetToStrings;
        int numClassNames, numMethodNames, numFileNames;
        messageHdrLen = (data.get() & 0xff);
        entryHdrLen = (data.get() & 0xff);
        stackFrameLen = (data.get() & 0xff);
        numEntries = (data.getShort() & 0xffff);
        offsetToStrings = data.getInt();
        numClassNames = (data.getShort() & 0xffff);
        numMethodNames = (data.getShort() & 0xffff);
        numFileNames = (data.getShort() & 0xffff);
        data.position(offsetToStrings);
        String[] classNames = new String[numClassNames];
        String[] methodNames = new String[numMethodNames];
        String[] fileNames = new String[numFileNames];
        readStringTable(data, classNames);
        readStringTable(data, methodNames);
        readStringTable(data, fileNames);
        data.position(messageHdrLen);
        ArrayList<AllocationInfo> list = new ArrayList<AllocationInfo>(numEntries);
        for (int i = 0; i < numEntries; i++) {
            int totalSize;
            int threadId, classNameIndex, stackDepth;
            totalSize = data.getInt();
            threadId = (data.getShort() & 0xffff);
            classNameIndex = (data.getShort() & 0xffff);
            stackDepth = (data.get() & 0xff);
            for (int skip = 9; skip < entryHdrLen; skip++)
                data.get();
            StackTraceElement[] steArray = new StackTraceElement[stackDepth];
            for (int sti = 0; sti < stackDepth; sti++) {
                int methodClassNameIndex, methodNameIndex;
                int methodSourceFileIndex;
                short lineNumber;
                String methodClassName, methodName, methodSourceFile;
                methodClassNameIndex = (data.getShort() & 0xffff);
                methodNameIndex = (data.getShort() & 0xffff);
                methodSourceFileIndex = (data.getShort() & 0xffff);
                lineNumber = data.getShort();
                methodClassName = classNames[methodClassNameIndex];
                methodName = methodNames[methodNameIndex];
                methodSourceFile = fileNames[methodSourceFileIndex];
                steArray[sti] = new StackTraceElement(methodClassName,
                    methodName, methodSourceFile, lineNumber);
                for (int skip = 9; skip < stackFrameLen; skip++)
                    data.get();
            }
            list.add(new AllocationInfo(classNames[classNameIndex],
                totalSize, (short) threadId, steArray));
        }
        Collections.sort(list);
        client.getClientData().setAllocations(list.toArray(new AllocationInfo[numEntries]));
        client.update(Client.CHANGE_HEAP_ALLOCATIONS);
    }
    @SuppressWarnings("unused")
    private static void dumpRecords(AllocationInfo[] records) {
        System.out.println("Found " + records.length + " records:");
        for (AllocationInfo rec: records) {
            System.out.println("tid=" + rec.getThreadId() + " "
                + rec.getAllocatedClass() + " (" + rec.getSize() + " bytes)");
            for (StackTraceElement ste: rec.getStackTrace()) {
                if (ste.isNativeMethod()) {
                    System.out.println("    " + ste.getClassName()
                        + "." + ste.getMethodName()
                        + " (Native method)");
                } else {
                    System.out.println("    " + ste.getClassName()
                        + "." + ste.getMethodName()
                        + " (" + ste.getFileName()
                        + ":" + ste.getLineNumber() + ")");
                }
            }
        }
    }
}
