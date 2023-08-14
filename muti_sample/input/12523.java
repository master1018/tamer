public class AnnotationProcessors {
    static class NoOpAP implements AnnotationProcessor {
        NoOpAP() {}
        public void process(){}
    }
    static class CompositeAnnotationProcessor implements AnnotationProcessor {
        private List<AnnotationProcessor> aps =
            new LinkedList<AnnotationProcessor>();
        public CompositeAnnotationProcessor(Collection<AnnotationProcessor> aps) {
            this.aps.addAll(aps);
        }
        public CompositeAnnotationProcessor(AnnotationProcessor... aps) {
            for(AnnotationProcessor ap: aps)
                this.aps.add(ap);
        }
        public void process() {
            for(AnnotationProcessor ap: aps)
                ap.process();
        }
    }
    public final static AnnotationProcessor NO_OP = new NoOpAP();
    public static AnnotationProcessor getCompositeAnnotationProcessor(AnnotationProcessor... aps) {
        return new CompositeAnnotationProcessor(aps);
    }
    public static AnnotationProcessor getCompositeAnnotationProcessor(Collection<AnnotationProcessor> aps) {
        return new CompositeAnnotationProcessor(aps);
    }
}
