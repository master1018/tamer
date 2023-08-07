public abstract class OldComputerBuilderAbstractFactory {
    public static OldComputerBuilderAbstractFactory getFactory() {
        return new SpectrumConcreteFactory();
    }
    public abstract OldComputerDirector getComputerDirector();
}
