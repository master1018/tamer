public class DFHBuildStep extends BuildStep {
    public DFHBuildStep(BuildFile inputFile, BuildFile outputFile) {
        super(inputFile, outputFile);
    }
    @Override
    boolean build() {
        if (super.build()) {
            File out_dir = outputFile.fileName.getParentFile();
            if (!out_dir.exists() && !out_dir.mkdirs()) {
                System.err.println("failed to create dir: "
                        + out_dir.getAbsolutePath());
                return false;
            }
            ClassFileAssembler cfAssembler = new ClassFileAssembler();
            Reader r;
            OutputStream os;
            try {
                r = new FileReader(inputFile.fileName);
                os = new FileOutputStream(outputFile.fileName);
            } catch (FileNotFoundException e) {
                System.err.println(e);
                return false;
            }
            try {
                cfAssembler.writeClassFile(r, os, true);
            } catch (RuntimeException e) {
                System.err.println("error in DFHBuildStep for inputfile "+inputFile.fileName+", outputfile "+outputFile.fileName);
                throw e;
            }
            return true;
        }
        return false;
    }
    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj)) {
            return inputFile.equals(((DFHBuildStep) obj).inputFile)
                    && outputFile.equals(((DFHBuildStep) obj).outputFile);
        }
        return false;
    }
    @Override
    public int hashCode() {
        return (inputFile == null ? 31 : inputFile.hashCode())
                ^ (outputFile == null ? 37 : outputFile.hashCode());
    }
}
