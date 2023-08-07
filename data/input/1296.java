public class Level1MapOfSetEntriesOperator<K, V> extends Operator implements ILevel1MapOfSetEntriesOperator<K, V> {
    public Level1MapOfSetEntriesOperator(final Target target) {
        super(target);
    }
    public ILevel0MapOfSetOperator<K, V> endFor() {
        return new Level0MapOfSetOperator<K, V>(getTarget().endIterate(Structure.MAP, null));
    }
    public ILevel2MapOfSetEntriesKeyOperator<K, V> onKey() {
        return new Level2MapOfSetEntriesKeyOperator<K, V>(getTarget().iterateIndex(0));
    }
    public ILevel2MapOfSetEntriesValueOperator<K, V> onValue() {
        return new Level2MapOfSetEntriesValueOperator<K, V>(getTarget().iterateIndex(1));
    }
    public Map<K, Set<V>> get() {
        return endFor().get();
    }
    public <X> ILevel1ListElementsOperator<X> convert(final IConverter<X, ? super Entry<K, Set<V>>> converter) {
        return new Level1ListElementsOperator<X>(getTarget().execute(converter));
    }
    public <X> ILevel1ListElementsOperator<X> eval(final IEvaluator<X, ? super Entry<K, Set<V>>> eval) {
        return new Level1ListElementsOperator<X>(getTarget().execute(eval));
    }
    public <X> ILevel1ListElementsOperator<X> exec(final IFunction<X, ? super Entry<K, Set<V>>> function) {
        return new Level1ListElementsOperator<X>(getTarget().execute(function));
    }
}
