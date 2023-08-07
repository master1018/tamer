public class BpDemo3 extends BackProp {
    public BpDemo3(int input_population, int middle_population, int output_population, double learning_rate, double momentum) {
        super(input_population, middle_population, output_population, learning_rate, momentum);
    }
    public BpDemo3(File file) throws IOException, FileNotFoundException, ClassNotFoundException {
        super(file);
    }
}
