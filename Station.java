public class Station {
    String name;
    int modules;
    double productivity_bonus;
    int priority;
    double crafting_speed;

    public Station() {
        this("Station Name", 0, 0, 1, 1);
    }

    public Station(String name, int modules, double productivity_bonus, double crafting_speed, int priority) {
        this.name = name;
        this.modules = modules;
        this.productivity_bonus = productivity_bonus;
        this.crafting_speed = crafting_speed;
        this.priority = priority;
    }

    public double getProd(int prod_mod_level) {
        int[] percentages = { 0, 4, 6, 10 };
        int percentage = percentages[prod_mod_level] * modules;
        return 1 + (percentage + productivity_bonus) / 100;
    }

    public double getSpeed(int prod_mod_level) {
        int[] percentages = { 0, -5, -10, -15 };
        double percentage = percentages[prod_mod_level] * modules;
        return crafting_speed * (1 + (percentage / 100));
    }
}