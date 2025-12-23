public class Material
{
    double quantity;
    String material;

    public Material(double quantity, String material)
    {
        this.quantity = quantity;
        this.material = material;
    }

    public String toString()
    {
        return this.quantity + " " + this.material;
    }
}