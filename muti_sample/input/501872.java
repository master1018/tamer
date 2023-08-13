public class ZoneCompactor {
    Map<String,String> links = new HashMap<String,String>();
    Map<String,Integer> starts = new HashMap<String,Integer>();
    Map<String,Integer> lengths = new HashMap<String,Integer>();
    Map<String,Integer> offsets = new HashMap<String,Integer>();
    int start = 0;
    private static final int MAXNAME = 40;
    private static byte[] copyFile(File inFile, OutputStream out)
        throws Exception {
        byte[] ret = new byte[0];
        InputStream in = new FileInputStream(inFile);
        byte[] buf = new byte[8192];
        while (true) {
            int nbytes = in.read(buf);
            if (nbytes == -1) {
                break;
            }
            out.write(buf, 0, nbytes);
            byte[] nret = new byte[ret.length + nbytes];
            System.arraycopy(ret, 0, nret, 0, ret.length);
            System.arraycopy(buf, 0, nret, ret.length, nbytes);
            ret = nret;
        }
        out.flush();
        return ret;
    }
    private void writeInt(OutputStream os, int x) throws IOException {
        os.write((x >> 24) & 0xff);
        os.write((x >> 16) & 0xff);
        os.write((x >>  8) & 0xff);
        os.write( x        & 0xff);
    }
    public ZoneCompactor(String setupFilename, String dirName)
        throws Exception {
        File zoneInfoFile = new File("zoneinfo.dat");
        zoneInfoFile.delete();
        OutputStream zoneInfo = new FileOutputStream(zoneInfoFile);
        BufferedReader rdr = new BufferedReader(new FileReader(setupFilename));
        String s;
        while ((s = rdr.readLine()) != null) {
            s = s.trim();
            if (s.startsWith("Link")) {
                StringTokenizer st = new StringTokenizer(s);
                st.nextToken();
                String to = st.nextToken();
                String from = st.nextToken();
                links.put(from, to);
            } else {
                String link = links.get(s);
                if (link == null) {
                    File f = new File(dirName, s);
                    long length = f.length();
                    starts.put(s, new Integer(start));
                    lengths.put(s, new Integer((int)length));
                    start += length;
                    byte[] data = copyFile(f, zoneInfo);
                    TimeZone tz = ZoneInfo.make(s, data);
                    int gmtOffset = tz.getRawOffset();
                    offsets.put(s, new Integer(gmtOffset));
                }
            }
        }
        zoneInfo.close();
        Iterator<String> iter = links.keySet().iterator();
        while (iter.hasNext()) {
            String from = iter.next();
            String to = links.get(from);
            starts.put(from, starts.get(to));
            lengths.put(from, lengths.get(to));
            offsets.put(from, offsets.get(to));
        }
        File idxFile = new File("zoneinfo.idx");
        idxFile.delete();
        FileOutputStream idx = new FileOutputStream(idxFile);
        ArrayList l = new ArrayList();
        l.addAll(starts.keySet());
        Collections.sort(l);
        Iterator<String> ziter = l.iterator();
        while (ziter.hasNext()) {
            String zname = ziter.next();
            if (zname.length() >= MAXNAME) {
                System.err.println("Error - zone filename exceeds " +
                                   (MAXNAME - 1) + " characters!");
            }
            byte[] znameBuf = new byte[MAXNAME];
            for (int i = 0; i < zname.length(); i++) {
                znameBuf[i] = (byte)zname.charAt(i);
            }
            idx.write(znameBuf);
            writeInt(idx, starts.get(zname).intValue());
            writeInt(idx, lengths.get(zname).intValue());
            writeInt(idx, offsets.get(zname).intValue());
        }
        idx.close();
    }
    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.println("usage: java ZoneCompactor <setup> <data dir>");
            System.exit(0);
        }
        new ZoneCompactor(args[0], args[1]);
    }
}
