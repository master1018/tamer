public class DexBuildStep extends BuildStep {
    private final boolean deleteInputFileAfterBuild;
    DexBuildStep(BuildFile inputFile, BuildFile outputFile,
            boolean deleteInputFileAfterBuild) {
        super(inputFile, outputFile);
        this.deleteInputFileAfterBuild = deleteInputFileAfterBuild;
    }
    @Override
    boolean build() {
        if (super.build()) {
            Main.Arguments args = new Main.Arguments();
            args.jarOutput = true;
            args.fileNames = new String[] {inputFile.fileName.getAbsolutePath()};
            args.outName = outputFile.fileName.getAbsolutePath();
            int result = Main.run(args);
            if (result == 0) {
                if (deleteInputFileAfterBuild) {
                    inputFile.fileName.delete();
                }
                return true;
            } else {
                System.err.println("exception while dexing "
                        + inputFile.fileName.getAbsolutePath() + " to "
                        + args.outName);
                return false;
            }
        }
        return false;
    }
    @Override
    public int hashCode() {
        return inputFile.hashCode() ^ outputFile.hashCode();
    }
    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj)) {
            DexBuildStep other = (DexBuildStep) obj;
            return inputFile.equals(other.inputFile)
                    && outputFile.equals(other.outputFile);
        }
        return false;
    }
}
