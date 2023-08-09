class JarFileCompare {
    private static VerifyTreeSet getVerifyTreeSet(String jarPath) {
        VerifyTreeSet vts = new VerifyTreeSet();
        try {
            JarFile j = new JarFile(jarPath);
            for (JarEntry je : Collections.list((Enumeration<JarEntry>) j.entries())) {
                if (!je.isDirectory()) { 
                    vts.add(je.getName());
                }
            }
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
        return vts;
    }
    private static LinkedList getListOfClasses(String jarPath) {
        LinkedList l = new LinkedList();
        try {
            JarFile j = new JarFile(jarPath);
            for (JarEntry je : Collections.list((Enumeration<JarEntry>) j.entries())) {
                if (!je.isDirectory() && je.getName().endsWith(".class")) {
                    l.add(je.getName());
                }
            }
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
        return l;
    }
    private static void jarDirectoryCompare(String jarPath1, String jarPath2) {
        VerifyTreeSet vts1 = getVerifyTreeSet(jarPath1);
        VerifyTreeSet vts2 = getVerifyTreeSet(jarPath2);
        TreeSet diff1 = vts1.diff(vts2);
        if (diff1.size() > 0) {
            Globals.log("Left has the following entries that right does not have");
            Globals.log(diff1.toString());
        }
        TreeSet diff2 = vts2.diff(vts1);
        if (diff2.size() > 0) {
            Globals.log("Right has the following entries that left does not have");
            Globals.log(diff2.toString());
        }
        if (Globals.checkJarClassOrdering()) {
            boolean error = false;
            Globals.println("Checking Class Ordering");
            LinkedList l1 = getListOfClasses(jarPath1);
            LinkedList l2 = getListOfClasses(jarPath2);
            if (l1.size() != l2.size()) {
                error = true;
                Globals.log("The number of classes differs");
                Globals.log("\t" + l1.size() + "<>" + l2.size());
            }
            for (int i = 0; i < l1.size(); i++) {
                String s1 = (String) l1.get(i);
                String s2 = (String) l2.get(i);
                if (s1.compareTo(s2) != 0) {
                    error = true;
                    Globals.log("Ordering differs at[" + i + "] = " + s1);
                    Globals.log("\t" + s2);
                }
            }
        }
    }
    static boolean compareStreams(InputStream is1, InputStream is2) {
        BufferedInputStream bis1 = new BufferedInputStream(is1, 8192);
        BufferedInputStream bis2 = new BufferedInputStream(is2, 8192);
        try {
            int i1, i2;
            int count = 0;
            while ((i1 = bis1.read()) == (i2 = bis2.read())) {
                count++;
                if (i1 < 0) {
                    return true;  
                }
            }
            return false;  
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }
    private static void checkEntry(JarFile jf1, JarFile jf2, JarEntry je) throws IOException {
        InputStream is1 = jf1.getInputStream(je);
        InputStream is2 = jf2.getInputStream(je);
        if (is1 != null && is2 != null) {
            if (!compareStreams(jf1.getInputStream(je), jf2.getInputStream(je))) {
                Globals.println("+++" + je.getName() + "+++");
                Globals.log("Error: File:" + je.getName()
                        + " differs, use a diff util for further diagnostics");
            }
        } else {
            Globals.println("+++" + je.getName() + "+++");
            Globals.log("Error: File:" + je.getName() + " not found in " + jf2.getName());
        }
    }
    static void jarCompare(String jarPath1, String jarPath2) {
        jarDirectoryCompare(jarPath1, jarPath2);
        try {
            JarFile jf1 = new JarFile(jarPath1);
            JarFile jf2 = new JarFile(jarPath2);
            int nclasses = 0;
            int nentries = 0;
            int entries_checked = 0;
            int classes_checked = 0;
            for (JarEntry je : Collections.list((Enumeration<JarEntry>) jf1.entries())) {
                if (!je.isDirectory() && !je.getName().endsWith(".class")) {
                    nentries++;
                } else if (je.getName().endsWith(".class")) {
                    nclasses++;
                }
            }
            for (JarEntry je : Collections.list((Enumeration<JarEntry>) jf1.entries())) {
                if (je.isDirectory()) {
                    continue;  
                }
                if (!je.getName().endsWith(".class")) {
                    entries_checked++;
                    if (je.getName().compareTo("META-INF/MANIFEST.MF") == 0) {
                        Manifest mf1 = new Manifest(jf1.getInputStream(je));
                        Manifest mf2 = new Manifest(jf2.getInputStream(je));
                        if (!mf1.equals(mf2)) {
                            Globals.log("Error: Manifests differ");
                            Globals.log("Manifest1");
                            Globals.log(mf1.getMainAttributes().entrySet().toString());
                            Globals.log("Manifest2");
                            Globals.log(mf2.getMainAttributes().entrySet().toString());
                        }
                    } else {
                        checkEntry(jf1, jf2, je);
                    }
                } else if (Globals.bitWiseClassCompare() == true) {
                    checkEntry(jf1, jf2, je);
                    classes_checked++;
                }
            }
            if (Globals.bitWiseClassCompare()) {
                Globals.println("Class entries checked (byte wise)/Total Class entries = "
                        + classes_checked + "/" + nclasses);
            }
            Globals.println("Non-class entries checked/Total non-class entries = "
                    + entries_checked + "/" + nentries);
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }
}
