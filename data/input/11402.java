public final class  Misc<T> implements Marker2, Marker3 {
    private static final long longConstant = Long.MAX_VALUE;
    private static final String asciispecials = "\t\n\u0007";
    public void covar(Collection<? extends T> s) {return;}
    public void contravar(Collection<? super T> s) {return;}
    public <S> S varUse(int i) {return null;}
    Object o = (new Object() {});       
}
interface Marker1 {}
interface Marker2 extends Marker1 {}
interface Marker3 {}
