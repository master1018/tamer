class CatchData {
     Object type;
     Label label;
     CatchData(Object type) {
         this.type = type;
         this.label = new Label();
     }
     public Label getLabel() {
         return label;
     }
     public Object getType() {
         return type;
     }
}
