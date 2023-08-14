class CommentNode extends AbstractSimpleNode {
    String text;
    CommentNode(String text) {
        this.text = text;
    }
    String text() {
        return text;
    }
}
