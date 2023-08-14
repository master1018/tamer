class DasmBuildStep extends BuildStep {
    boolean generate_linenum = false;
    DasmBuildStep(BuildFile inputFile, BuildFile outputFile) {
        super(inputFile, outputFile);
    }
    @Override
    boolean build() {
        if (super.build()) {
            return assemble(inputFile.fileName);
        }
        return false;
    }
    private static Reader createReader(String fname) throws IOException {
        FileInputStream fs = new FileInputStream(fname);
        InputStreamReader ir;
        ir = new InputStreamReader(fs);
        return new BufferedReader(ir);
    }
    private boolean assemble(File file) {
        DAsm dAsm = new DAsm();
        String fname = file.getAbsolutePath();
        Reader inp = null; 
        try {
            inp = createReader(fname);
            dAsm.readD(inp, new File(fname).getName(), generate_linenum);
            close(inp);
        } catch(DasmError e) {
            if(BuildDalvikSuite.DEBUG)
                e.printStackTrace();
            System.err.println("DASM Error: " + e.getMessage());
        } catch(Exception e) {
             if(BuildDalvikSuite.DEBUG)
                 e.printStackTrace();
             System.err.println("Exception <" + e.getClass().getName() + ">" + e.getMessage() + 
                         " while reading and parsing " + fname);
             return false;
        }
        finally {
            close(inp);
        }
        if(dAsm.errorCount() > 0) {
            System.err.println("Found " + dAsm.errorCount() + " errors " +
                    " while reading and parsing " + fname);
                return false;
        }
        String class_path[] = Utils.getClassFieldFromString(dAsm.getClassName());
        String class_name = class_path[1];
        String dest_dir = outputFile.folder.getAbsolutePath();
        if (class_path[0] != null) {
            String class_dir = class_path[0].replaceAll("/|\\.", Character.toString(File.separatorChar));                                           
            if (dest_dir != null) {
                dest_dir = dest_dir + File.separator + class_dir;
            } else {
                dest_dir = class_dir;
            }
        }
        File out_file = null;
        if (dest_dir == null) {
            out_file = new File(class_name + ".dex");
        } else {
            out_file = new File(dest_dir, class_name + ".dex");
            File dest = new File(dest_dir);
            if (!dest.exists()) {
                dest.mkdirs();
            }
            if (!dest.isDirectory()) {
                System.err.println("Cannot create directory " + dest_dir);
                return false;
            }
        }
        FileOutputStream outp = null;
        try {
            outp = new FileOutputStream(out_file);
            dAsm.write(outp, null);
        } catch(Exception e) {
                if(BuildDalvikSuite.DEBUG)
                e.printStackTrace();
                System.err.println("Exception <" + e.getClass().getName() + ">" + e.getMessage() + 
                        " while writing " + out_file.getPath());
                close(outp);
                out_file.delete();
                return false;
       }
       finally {
           close(outp);
       }
       return true;
    }
    private static void close(Closeable c) {
        if(c == null)
            return;
        try {
            c.close();
        } catch(IOException e) {
        }
    }
    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj)) {
            DasmBuildStep other = (DasmBuildStep) obj;
            return inputFile.equals(other.inputFile)
                    && generate_linenum == other.generate_linenum
                    && outputFile.equals(other.outputFile);
        }
        return false;
    }
    @Override
    public int hashCode() {
        return inputFile.hashCode() ^ outputFile.hashCode()
                ^ (generate_linenum ? 31 : 37);
    }
}
