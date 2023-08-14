public class CreateBadJar {
public static void main(String [] arguments) {
        if (arguments.length != 2) {
            throw new RuntimeException("Arguments: jarfilename entryname");
        }
        String outFile = arguments[0];
        String entryName = arguments[1];
        try {
        if (!new File(outFile).exists()) {
          System.out.println("Creating file " + outFile);
          ZipOutputStream zos = null;
          zos = new ZipOutputStream(
            new FileOutputStream(outFile));
          ZipEntry e = new ZipEntry(entryName);
          zos.putNextEntry(e);
          for (int j=0; j<50000; j++) {
            zos.write((int)'a');
          }
          zos.closeEntry();
          zos.close();
          zos = null;
        }
        int len = (int)(new File(outFile).length());
        byte[] good = new byte[len];
        FileInputStream fis = new FileInputStream(outFile);
        fis.read(good);
        fis.close();
        fis = null;
        int endpos = len - ENDHDR;
        int cenpos = u16(good, endpos+ENDOFF);
        if (u32(good, cenpos) != CENSIG) throw new RuntimeException("Where's CENSIG?");
        byte[] bad;
        bad = good.clone();
        int pos = findInCEN(bad, cenpos, entryName);
        bad[pos+0x18]=(byte)0xff;
        bad[pos+0x19]=(byte)0xff;
        bad[pos+0x1a]=(byte)0xff;
        bad[pos+0x1b]=(byte)0xff;
        new File(outFile).delete();
        FileOutputStream fos = new FileOutputStream(outFile);
        fos.write(bad);
        fos.close();
        fos = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
}
    static int findInCEN(byte[] bytes, int cenpos, String entryName) {
        int pos = cenpos;
        int nextPos = 0;
        String filename = null;
        do {
            if (nextPos != 0) {
                pos = nextPos;
            }
            System.out.println("entry at pos = " + pos);
            if (u32(bytes, pos) != CENSIG) throw new RuntimeException ("entry not found in CEN or premature end...");
            int csize = u32(bytes, pos+0x14);          
            int uncompsize = u32(bytes, pos+0x18);     
            int filenameLength = u16(bytes, pos+0x1c); 
            int extraLength = u16(bytes, pos+0x1e);    
            int commentLength = u16(bytes, pos+0x20);  
            filename = new String(bytes, pos+0x2e, filenameLength); 
            int offset = u32(bytes, pos+0x2a);         
            System.out.println("filename = " + filename + "\ncsize = " + csize +
                               " uncomp.size = " + uncompsize +" file offset = " + offset);
            nextPos =  pos + 0x2e + filenameLength + extraLength + commentLength;
        } while (!filename.equals(entryName));
        System.out.println("entry found at pos = " + pos);
        return pos;
    }
    static int u8(byte[] data, int offset) {
        return data[offset]&0xff;
    }
    static int u16(byte[] data, int offset) {
        return u8(data,offset) + (u8(data,offset+1)<<8);
    }
    static int u32(byte[] data, int offset) {
        return u16(data,offset) + (u16(data,offset+2)<<16);
    }
}
