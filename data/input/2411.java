public abstract class JarTest
{
        static String tmpdir = System.getProperty("java.io.tmpdir");
        static String javaCmd = System.getProperty("java.home") + File.separator +
                                "bin" + File.separator + "java";
        protected byte[] readFully(InputStream in) throws Exception {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                byte[] buffer = new byte[32];
                int count;
                while ((count = in.read(buffer)) >= 0) {
                        out.write(buffer, 0, count);
                }
                return out.toByteArray();
        }
        protected File copyResource(File dir, String resName) throws Exception {
                BufferedOutputStream buffOut = null;
                FileOutputStream fileOut;
                InputStream in = null;
                File file = null;
                byte[] buffer;
                int count;
                file = new File(dir, resName);
                try {
                    fileOut = new FileOutputStream(file);
                    buffOut = new BufferedOutputStream(fileOut);
                    in = getClass().getResourceAsStream(resName);
                    buffer = new byte[1024];
                    while ((count = in.read(buffer)) >= 0) {
                            buffOut.write(buffer, 0, count);
                    }
                    buffOut.flush();
                } finally {
                    if (buffOut != null) {
                        try {
                            buffOut.close();
                        } catch (IOException e) {
                        }
                    }
                    if (in != null) {
                        try {
                            in.close();
                        }  catch (IOException e) {
                        }
                    }
                }
                return file;
        }
        protected File createTempDir() throws Exception {
                File result = new File(tmpdir  + File.separator + getClass().getName());
                result.delete();
                result.mkdirs();
                return result;
        }
        protected  boolean deleteRecursively(File file) {
                File[] children;
                boolean result = true;
                children = file.listFiles();
                if (children != null) {
                        for (int i=0; i<children.length; i++) {
                                result = result && deleteRecursively(children[i]);
                        }
                }
                result = result && file.delete();
                return result;
        }
    static class Redirector implements Runnable
    {
        private BufferedReader reader;
        private PrintStream out;
        private boolean hasReadData;
        public Redirector(BufferedReader reader, PrintStream out) {
            this.reader = reader;
            this.out = out;
        }
        public void run() {
            String str;
            try {
                while ((str = reader.readLine()) != null) {
                    hasReadData = true;
                    out.println(str);
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
        public boolean getHasReadData() {
            return hasReadData;
        }
    }
}
