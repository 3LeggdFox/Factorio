package core;

public class Material {
    double quantity;
    String name;

    public Material(double quantity, String material) {
        this.quantity = quantity;
        this.name = material;
    }

    public String toString() {
        return this.quantity + " " + this.name;
    }
}