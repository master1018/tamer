public class NativeCallBack
{
    public native int computeAnswer();
    @Keep
    public int getAnswer()
    {
        return 42;
    }
    public static void main(String[] args)
    {
        int answer = new NativeCallBack().computeAnswer();
        System.out.println("The answer is " + answer);
    }
}
