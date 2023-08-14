class Animal {
    int category;
    int age;
}
class Pet extends Animal {
    String owner;
    String name;
    String vet;
    String records;
    String address;
    Pet(String name) { this.name = name; }
}
class Dog extends Pet {
    int breed;
    int barks;
    Dog(String name) { super(name); }
}
class Cat extends Pet {
    int breed;
    int claws;
    Cat(String name) { super(name); }
}
public class HeapUser {
    private static Dog dogs[];
    private static Cat cats[];
    public static void main(String args[]) {
        System.out.println("HeapUser start, 101 dogs, 1000 cats");
        dogs = new Dog[101];
        for(int i=0; i<101; i++) {
            dogs[i] = new Dog("fido " + i);
        }
        cats = new Cat[1000];
        for(int i=0; i<1000; i++) {
            cats[i] = new Cat("feefee " + i);
        }
        System.out.println("HeapUser end");
    }
}
