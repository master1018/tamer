import java.util.Scanner;

public class SimpleCalculator {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("简单计算器");
        System.out.print("请输入第一个数字: ");
        double num1 = scanner.nextDouble();
        
        System.out.print("请输入操作符 (+/-): ");
        char operator = scanner.next().charAt(0);
        
        System.out.print("请输入第二个数字: ");
        double num2 = scanner.nextDouble();
        
        double result = 0.0;
        
        if (operator == '+') {
            result = num1 + num2;
        } else if (operator == '-') {
            result = num1 - num2;
        } else {
            System.out.println("无效的操作符");
            return;
        }
        
        System.out.println("结果: " + result);
        
        scanner.close();
    }
}
