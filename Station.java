public class Station 
{
    String name;
    int modules;
    double productivity_bonus;

    public Station()
    {
        this("Station Name", 0, 0);
    }

    public Station(String name, int modules, double productivity_bonus)
    {
        this.name = name;
        this.modules = modules;
        this.productivity_bonus = productivity_bonus;
    }
}
