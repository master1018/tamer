package dk.aau.imi.med4.ooadp2009.javaintro.class05;
public class TemperatureTest {
    public static void main(String[] args) {
        Temperature t = new Temperature();
        t.setCelsius(0);
        System.out.println("Temperature of t is " + t.getFahrenheit() + " degrees fahrenheit and " + t.getCelsius() + " degrees celsius");
        t.setFahrenheit(0);
        System.out.println("Temperature of t is " + t.getFahrenheit() + " degrees fahrenheit and " + t.getCelsius() + " degrees celsius");
        t.setCelsius(100);
        System.out.println("Temperature of t is " + t.getFahrenheit() + " degrees fahrenheit and " + t.getCelsius() + " degrees celsius");
        t.setFahrenheit(100);
        System.out.println("Temperature of t is " + t.getFahrenheit() + " degrees fahrenheit and " + t.getCelsius() + " degrees celsius");
        t.setCelsius(-40);
        System.out.println("Temperature of t is " + t.getFahrenheit() + " degrees fahrenheit and " + t.getCelsius() + " degrees celsius");
        t.setFahrenheit(-40);
        System.out.println("Temperature of t is " + t.getFahrenheit() + " degrees fahrenheit and " + t.getCelsius() + " degrees celsius");
    }
}
