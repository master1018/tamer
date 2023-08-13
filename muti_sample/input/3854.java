public class BigFont extends Applet {
   static private class SizedInputStream extends InputStream {
       int size;
       int cnt = 0;
       SizedInputStream(int size) {
           this.size = size;
       }
       public int read() {
           if (cnt < size) {
              cnt++;
              return 0;
           } else {
              return -1;
           }
       }
       public int getCurrentSize() {
           return cnt;
       }
   }
    String id;
    String fileName;
    public void init() {
        id = getParameter("number");
        fileName = getParameter("font");
        System.out.println("Applet " + id + " "+
                           Thread.currentThread().getThreadGroup());
        int fontSize = 64 * 1000 * 1000;
        SizedInputStream sis = new SizedInputStream(fontSize);
        try {
             Font font = Font.createFont(Font.TRUETYPE_FONT, sis);
        } catch (Throwable t) {
            if (t instanceof FontFormatException ||
                fontSize <= sis.getCurrentSize())
            {
                System.out.println(sis.getCurrentSize());
                System.out.println(t);
                throw new RuntimeException("Allowed file to be too large.");
            }
        }
        System.out.println("Applet " + id + " finished.");
    }
    int getFileSize(String fileName) {
        try {
            URL url = new URL(getCodeBase(), fileName);
            InputStream inStream = url.openStream();
            BufferedInputStream fontStream = new BufferedInputStream(inStream);
            int size = 0;
            while (fontStream.read() != -1) {
                size++;
            }
            fontStream.close();
            return size;
        } catch (IOException e) {
            return 0;
        }
    }
    void loadMany(int oneFont, int fontCnt, String fileName) {
        System.out.println("fontcnt= " + fontCnt);
        Font[] fonts = new Font[fontCnt];
        int totalSize = 0;
        boolean gotException = false;
        for (int i=0; i<fontCnt; i++) {
            try {
                URL url = new URL(getCodeBase(), fileName);
                InputStream inStream = url.openStream();
                BufferedInputStream fontStream =
                    new BufferedInputStream(inStream);
                fonts[i] = Font.createFont(Font.TRUETYPE_FONT, fontStream);
                totalSize += oneFont;
                fontStream.close();
            } catch (Throwable t) {
                gotException = true;
                System.out.println("Applet " + id + " " + t);
            }
        }
        if (!gotException) {
          throw new RuntimeException("No expected exception");
        }
    }
}
