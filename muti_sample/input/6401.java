public class Common {
    static void copyFile (String src, String dst) {
        copyFile (new File(src), new File(dst));
    }
    static void copyDir (String src, String dst) {
        copyDir (new File(src), new File(dst));
    }
    static void copyFile (File src, File dst) {
        try {
            if (!src.isFile()) {
                throw new RuntimeException ("File not found: " + src.toString());
            }
            dst.delete();
            dst.createNewFile();
            FileInputStream i = new FileInputStream (src);
            FileOutputStream o = new FileOutputStream (dst);
            byte[] buf = new byte [1024];
            int count;
            while ((count=i.read(buf)) >= 0) {
                o.write (buf, 0, count);
            }
            i.close();
            o.close();
        } catch (IOException e) {
            throw new RuntimeException (e);
        }
    }
    static void rm_minus_rf (File path) {
        if (!path.exists()) {
            return;
        }
        if (path.isFile()) {
            if (!path.delete()) {
                throw new RuntimeException ("Could not delete " + path);
            }
        } else if (path.isDirectory ()) {
            String[] names = path.list();
            File[] files = path.listFiles();
            for (int i=0; i<files.length; i++) {
                rm_minus_rf (new File(path, names[i]));
            }
            if (!path.delete()) {
                throw new RuntimeException ("Could not delete " + path);
            }
        } else {
            throw new RuntimeException ("Trying to delete something that isn't a file or a directory");
        }
    }
    static void copyDir (File src, File dst) {
        if (!src.isDirectory()) {
            throw new RuntimeException ("Dir not found: " + src.toString());
        }
        if (dst.exists()) {
            throw new RuntimeException ("Dir exists: " + dst.toString());
        }
        dst.mkdir();
        String[] names = src.list();
        File[] files = src.listFiles();
        for (int i=0; i<files.length; i++) {
            String f = names[i];
            if (files[i].isDirectory()) {
                copyDir (files[i], new File (dst, f));
            } else {
                copyFile (new File (src, f), new File (dst, f));
            }
        }
    }
    static Class loadClass (String name, URLClassLoader loader, boolean expect){
        try {
            Class clazz = Class.forName (name, true, loader);
            if (!expect) {
                throw new RuntimeException ("loadClass: "+name+" unexpected");
            }
            return clazz;
        } catch (ClassNotFoundException e) {
            if (expect) {
                throw new RuntimeException ("loadClass: " +name + " not found");
            }
        }
        return null;
    }
}
