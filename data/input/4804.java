public class ZipInfo {
    public static void main(String[] args) throws Throwable {
        if (args.length < 1) {
            print("Usage: java ZipInfo zfname");
        } else {
            Map<String, ?> env = Collections.emptyMap();
            ZipFileSystem zfs = (ZipFileSystem)(new ZipFileSystemProvider()
                                    .newFileSystem(Paths.get(args[0]), env));
            byte[] cen = zfs.cen;
            if (cen == null) {
                print("zip file is empty%n");
                return;
            }
            int    pos = 0;
            byte[] buf = new byte[1024];
            int    no = 1;
            while (pos + CENHDR < cen.length) {
                print("----------------#%d--------------------%n", no++);
                printCEN(cen, pos);
                long len = LOCHDR + CENNAM(cen, pos) + CENEXT(cen, pos) + CENHDR;
                if (zfs.readFullyAt(buf, 0, len, locoff(cen, pos)) != len)
                    zfs.zerror("read loc header failed");
                if (LOCEXT(buf) > CENEXT(cen, pos) + CENHDR) {
                    len = LOCHDR + LOCNAM(buf) + LOCEXT(buf);
                    if (zfs.readFullyAt(buf, 0, len, locoff(cen, pos)) != len)
                        zfs.zerror("read loc header failed");
                }
                printLOC(buf);
                pos += CENHDR + CENNAM(cen, pos) + CENEXT(cen, pos) + CENCOM(cen, pos);
            }
            zfs.close();
        }
    }
    static void print(String fmt, Object... objs) {
        System.out.printf(fmt, objs);
    }
    static void printLOC(byte[] loc) {
        print("%n");
        print("[Local File Header]%n");
        print("    Signature   :   %#010x%n", LOCSIG(loc));
        if (LOCSIG(loc) != LOCSIG) {
           print("    Wrong signature!");
           return;
        }
        print("    Version     :       %#6x    [%d.%d]%n",
                  LOCVER(loc), LOCVER(loc) / 10, LOCVER(loc) % 10);
        print("    Flag        :       %#6x%n", LOCFLG(loc));
        print("    Method      :       %#6x%n", LOCHOW(loc));
        print("    LastMTime   :   %#10x    [%tc]%n",
              LOCTIM(loc), dosToJavaTime(LOCTIM(loc)));
        print("    CRC         :   %#10x%n", LOCCRC(loc));
        print("    CSize       :   %#10x%n", LOCSIZ(loc));
        print("    Size        :   %#10x%n", LOCLEN(loc));
        print("    NameLength  :       %#6x    [%s]%n",
                  LOCNAM(loc), new String(loc, LOCHDR, LOCNAM(loc)));
        print("    ExtraLength :       %#6x%n", LOCEXT(loc));
        if (LOCEXT(loc) != 0)
            printExtra(loc, LOCHDR + LOCNAM(loc), LOCEXT(loc));
    }
    static void printCEN(byte[] cen, int off) {
        print("[Central Directory Header]%n");
        print("    Signature   :   %#010x%n", CENSIG(cen, off));
        if (CENSIG(cen, off) != CENSIG) {
           print("    Wrong signature!");
           return;
        }
        print("    VerMadeby   :       %#6x    [%d, %d.%d]%n",
              CENVEM(cen, off), (CENVEM(cen, off) >> 8),
              (CENVEM(cen, off) & 0xff) / 10,
              (CENVEM(cen, off) & 0xff) % 10);
        print("    VerExtract  :       %#6x    [%d.%d]%n",
              CENVER(cen, off), CENVER(cen, off) / 10, CENVER(cen, off) % 10);
        print("    Flag        :       %#6x%n", CENFLG(cen, off));
        print("    Method      :       %#6x%n", CENHOW(cen, off));
        print("    LastMTime   :   %#10x    [%tc]%n",
              CENTIM(cen, off), dosToJavaTime(CENTIM(cen, off)));
        print("    CRC         :   %#10x%n", CENCRC(cen, off));
        print("    CSize       :   %#10x%n", CENSIZ(cen, off));
        print("    Size        :   %#10x%n", CENLEN(cen, off));
        print("    NameLen     :       %#6x    [%s]%n",
              CENNAM(cen, off), new String(cen, off + CENHDR, CENNAM(cen, off)));
        print("    ExtraLen    :       %#6x%n", CENEXT(cen, off));
        if (CENEXT(cen, off) != 0)
            printExtra(cen, off + CENHDR + CENNAM(cen, off), CENEXT(cen, off));
        print("    CommentLen  :       %#6x%n", CENCOM(cen, off));
        print("    DiskStart   :       %#6x%n", CENDSK(cen, off));
        print("    Attrs       :       %#6x%n", CENATT(cen, off));
        print("    AttrsEx     :   %#10x%n", CENATX(cen, off));
        print("    LocOff      :   %#10x%n", CENOFF(cen, off));
    }
    static long locoff(byte[] cen, int pos) {
        long locoff = CENOFF(cen, pos);
        if (locoff == ZIP64_MINVAL) {    
            int off = pos + CENHDR + CENNAM(cen, pos);
            int end = off + CENEXT(cen, pos);
            while (off + 4 < end) {
                int tag = SH(cen, off);
                int sz = SH(cen, off + 2);
                if (tag != EXTID_ZIP64) {
                    off += 4 + sz;
                    continue;
                }
                off += 4;
                if (CENLEN(cen, pos) == ZIP64_MINVAL)
                    off += 8;
                if (CENSIZ(cen, pos) == ZIP64_MINVAL)
                    off += 8;
                return LL(cen, off);
            }
        }
        return locoff;
    }
    static void printExtra(byte[] extra, int off, int len) {
        int end = off + len;
        while (off + 4 <= end) {
            int tag = SH(extra, off);
            int sz = SH(extra, off + 2);
            print("        [tag=0x%04x, sz=%d, data= ", tag, sz);
            if (off + sz > end) {
                print("    Error: Invalid extra data, beyond extra length");
                break;
            }
            off += 4;
            for (int i = 0; i < sz; i++)
                print("%02x ", extra[off + i]);
            print("]%n");
            switch (tag) {
            case EXTID_ZIP64 :
                print("         ->ZIP64: ");
                int pos = off;
                while (pos + 8 <= off + sz) {
                    print(" *0x%x ", LL(extra, pos));
                    pos += 8;
                }
                print("%n");
                break;
            case EXTID_NTFS:
                print("         ->PKWare NTFS%n");
                if (SH(extra, off + 4) !=  0x0001 || SH(extra, off + 6) !=  24)
                    print("    Error: Invalid NTFS sub-tag or subsz");
                print("            mtime:%tc%n",
                      winToJavaTime(LL(extra, off + 8)));
                print("            atime:%tc%n",
                      winToJavaTime(LL(extra, off + 16)));
                print("            ctime:%tc%n",
                      winToJavaTime(LL(extra, off + 24)));
                break;
            case EXTID_EXTT:
                print("         ->Inof-ZIP Extended Timestamp: flag=%x%n",extra[off]);
                pos = off + 1 ;
                while (pos + 4 <= off + sz) {
                    print("            *%tc%n",
                          unixToJavaTime(LG(extra, pos)));
                    pos += 4;
                }
                break;
            default:
            }
            off += sz;
        }
    }
}
