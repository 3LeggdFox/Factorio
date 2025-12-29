public class Station 
{
    String name;
    int modules;
    double productivity_bonus;
    int priority;

    public Station()
    {
        this("Station Name", 0, 0, 1);
    }

    public Station(String name, int modules, double productivity_bonus, int priority)
    {
        this.name = name;
        this.modules = modules;
        this.productivity_bonus = productivity_bonus;
        this.priority = priority;
    }

    public double getProd(int moduleLevel)
    {
        int[] percentages = {0, 4, 6, 10};
        int percentage = percentages[moduleLevel] * modules;
        return 1 + (percentage + productivity_bonus)/100;
    }
}