public class IsLightweightCrash {
    public static int ITERATIONS = 20;
    public static void main(String []s)
    {
        for (int i = 0; i < ITERATIONS; i++){
            showFrame(i);
        }
    }
    private static void showFrame(int i){
        System.out.println("iteration = "+i);
        Frame f = new Frame();
        f.add(new AHeavyweightComponent());
        f.setVisible(true);
        f.setVisible(false);
    }
}
class AHeavyweightComponent extends Component {
    public boolean isLightweight() { return false; }
}
