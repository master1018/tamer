public class test {
                public void transfer(Pipe pipe, Input input, Output output, boolean repeated) throws IOException {
                    output.writeDouble(number, input.readDouble(), repeated);
                }
}
