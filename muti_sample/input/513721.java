final class HandleNativeHeap extends ChunkHandler {
    public static final int CHUNK_NHGT = type("NHGT"); 
    public static final int CHUNK_NHSG = type("NHSG"); 
    public static final int CHUNK_NHST = type("NHST"); 
    public static final int CHUNK_NHEN = type("NHEN"); 
    private static final HandleNativeHeap mInst = new HandleNativeHeap();
    private HandleNativeHeap() {
    }
    public static void register(MonitorThread mt) {
        mt.registerChunkHandler(CHUNK_NHGT, mInst);
        mt.registerChunkHandler(CHUNK_NHSG, mInst);
        mt.registerChunkHandler(CHUNK_NHST, mInst);
        mt.registerChunkHandler(CHUNK_NHEN, mInst);
    }
    @Override
    public void clientReady(Client client) throws IOException {}
    @Override
    public void clientDisconnected(Client client) {}
    @Override
    public void handleChunk(Client client, int type, ByteBuffer data, boolean isReply, int msgId) {
        Log.d("ddm-nativeheap", "handling " + ChunkHandler.name(type));
        if (type == CHUNK_NHGT) {
            handleNHGT(client, data);
        } else if (type == CHUNK_NHST) {
            client.getClientData().getNativeHeapData().clearHeapData();
        } else if (type == CHUNK_NHEN) {
            client.getClientData().getNativeHeapData().sealHeapData();
        } else if (type == CHUNK_NHSG) {
            handleNHSG(client, data);
        } else {
            handleUnknownChunk(client, type, data, isReply, msgId);
        }
        client.update(Client.CHANGE_NATIVE_HEAP_DATA);
    }
    public static void sendNHGT(Client client) throws IOException {
        ByteBuffer rawBuf = allocBuffer(0);
        JdwpPacket packet = new JdwpPacket(rawBuf);
        ByteBuffer buf = getChunkDataBuf(rawBuf);
        finishChunkPacket(packet, CHUNK_NHGT, buf.position());
        Log.d("ddm-nativeheap", "Sending " + name(CHUNK_NHGT));
        client.sendAndConsume(packet, mInst);
        rawBuf = allocBuffer(2);
        packet = new JdwpPacket(rawBuf);
        buf = getChunkDataBuf(rawBuf);
        buf.put((byte)HandleHeap.WHEN_GC);
        buf.put((byte)HandleHeap.WHAT_OBJ);
        finishChunkPacket(packet, CHUNK_NHSG, buf.position());
        Log.d("ddm-nativeheap", "Sending " + name(CHUNK_NHSG));
        client.sendAndConsume(packet, mInst);
    }
    private void handleNHGT(Client client, ByteBuffer data) {
        ClientData cd = client.getClientData();
        Log.d("ddm-nativeheap", "NHGT: " + data.limit() + " bytes");
        byte[] copy = new byte[data.limit()];
        data.get(copy);
        cd.clearNativeAllocationInfo();
        ByteBuffer buffer = ByteBuffer.wrap(copy);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        int mapSize = buffer.getInt();
        int allocSize = buffer.getInt();
        int allocInfoSize = buffer.getInt();
        int totalMemory = buffer.getInt();
        int backtraceSize = buffer.getInt();
        Log.d("ddms", "mapSize: " + mapSize);
        Log.d("ddms", "allocSize: " + allocSize);
        Log.d("ddms", "allocInfoSize: " + allocInfoSize);
        Log.d("ddms", "totalMemory: " + totalMemory);
        cd.setTotalNativeMemory(totalMemory);
        if (allocInfoSize == 0)
          return;
        if (mapSize > 0) {
            byte[] maps = new byte[mapSize];
            buffer.get(maps, 0, mapSize);
            parseMaps(cd, maps);
        }
        int iterations = allocSize / allocInfoSize;
        for (int i = 0 ; i < iterations ; i++) {
            NativeAllocationInfo info = new NativeAllocationInfo(
                    buffer.getInt() ,
                    buffer.getInt() );
            for (int j = 0 ; j < backtraceSize ; j++) {
                long addr = ((long)buffer.getInt()) & 0x00000000ffffffffL;
                info.addStackCallAddress(addr);;
            }
            cd.addNativeAllocation(info);
        }
    }
    private void handleNHSG(Client client, ByteBuffer data) {
        byte dataCopy[] = new byte[data.limit()];
        data.rewind();
        data.get(dataCopy);
        data = ByteBuffer.wrap(dataCopy);
        client.getClientData().getNativeHeapData().addHeapData(data);
        if (true) {
            return;
        }
        byte[] copy = new byte[data.limit()];
        data.get(copy);
        ByteBuffer buffer = ByteBuffer.wrap(copy);
        buffer.order(ByteOrder.BIG_ENDIAN);
        int id = buffer.getInt();
        int unitsize = (int) buffer.get();
        long startAddress = (long) buffer.getInt() & 0x00000000ffffffffL;
        int offset = buffer.getInt();
        int allocationUnitCount = buffer.getInt();
        while (buffer.position() < buffer.limit()) {
            int eState = (int)buffer.get() & 0x000000ff;
            int eLen = ((int)buffer.get() & 0x000000ff) + 1;
        }
    }
    private void parseMaps(ClientData cd, byte[] maps) {
        InputStreamReader input = new InputStreamReader(new ByteArrayInputStream(maps));
        BufferedReader reader = new BufferedReader(input);
        String line;
        try {
            long startAddr = 0;
            long endAddr = 0;
            String library = null;
            while ((line = reader.readLine()) != null) {
                Log.d("ddms", "line: " + line);
                if (line.length() < 16) {
                    continue;
                }
                try {
                    long tmpStart = Long.parseLong(line.substring(0, 8), 16);
                    long tmpEnd = Long.parseLong(line.substring(9, 17), 16);
                    if (tmpStart >= 0x0000000080000000L && tmpStart <= 0x00000000BFFFFFFFL) {
                        int index = line.indexOf('/');
                        if (index == -1)
                            continue;
                        String tmpLib = line.substring(index);
                        if (library == null ||
                                (library != null && tmpLib.equals(library) == false)) {
                            if (library != null) {
                                cd.addNativeLibraryMapInfo(startAddr, endAddr, library);
                                Log.d("ddms", library + "(" + Long.toHexString(startAddr) +
                                        " - " + Long.toHexString(endAddr) + ")");
                            }
                            library = tmpLib;
                            startAddr = tmpStart;
                            endAddr = tmpEnd;
                        } else {
                            endAddr = tmpEnd;
                        }
                    }
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
            if (library != null) {
                cd.addNativeLibraryMapInfo(startAddr, endAddr, library);
                Log.d("ddms", library + "(" + Long.toHexString(startAddr) +
                        " - " + Long.toHexString(endAddr) + ")");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
