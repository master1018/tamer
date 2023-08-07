package cs224n.classify;
public interface LabeledDatum<F, L> extends Datum<F> {
    L getLabel();
}
