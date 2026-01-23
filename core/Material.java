package core;

/**
 * Material
 * Class which pairs a material name and quantity
 * 
 * @version 1.0
 */
public class Material {
    double quantity;
    String name;

    /**
     * Constructor
     * 
     * @param quantity Amount of the material
     * @param material Name of the material
     */
    public Material(double quantity, String material) {
        this.quantity = quantity;
        this.name = material;
    }

    /**
     * Method returning a String representation of the object
     * 
     * @return String representation of the object, compatible with recipe format
     */
    public String toString() {
        return this.quantity + " " + this.name;
    }
}