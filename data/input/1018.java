public class DifferenceDescriptor extends Component implements IDifferenceDescriptor {
    public DifferenceDescriptor() {
        super(ComponentType.DIFFERENCE);
    }
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append(ComponentType.DIFFERENCE);
        buffer.append("{");
        for (int i = 0; i < this.getVariables().size(); i++) {
            buffer.append(this.getVariables().get(i).toString());
            buffer.append(", ");
        }
        buffer.delete(buffer.length() - 2, buffer.length());
        buffer.append("}");
        for (int i = 0; i < this.getChildren().size(); i++) {
            buffer.append("\n{(");
            buffer.append(this.getChildren().get(i));
            buffer.append(", ");
        }
        buffer.delete(buffer.length() - 2, buffer.length());
        buffer.append(")}");
        return buffer.toString();
    }
}
