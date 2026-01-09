import java.util.ArrayList;
import java.lang.StringBuilder;

public class Recipe
{
    ArrayList<Material> inputs = new ArrayList<Material>();
    ArrayList<Material> outputs = new ArrayList<Material>();
    ArrayList<String> stations = new ArrayList<String>();
    double crafting_time;
    boolean has_req;
    boolean can_prod;
    String alt_name;
    String station = null;

    public Recipe(ArrayList<Material> inputs, ArrayList<Material> outputs, ArrayList<String> stations, double crafting_time, boolean has_req, boolean can_prod, String alt_name)
    {
        this.inputs = inputs;
        this.outputs = outputs;
        this.stations = stations;
        this.crafting_time = crafting_time;
        this.has_req = has_req;
        this.can_prod = can_prod;
        this.alt_name = alt_name;
    }

    public double amountOutput(String material)
    {
        for (Material output : this.outputs)
        {
            if (output.name.equals(material))
            {
                return output.quantity;
            }
        }
        return 0;
    }
    
    public boolean hasOutput(String material)
    {
        return amountOutput(material) != 0;
    }

    public double amountInput(String material)
    {
        for (Material input : this.inputs)
        {
            if (input.name.equals(material))
            {
                return input.quantity;
            }
        }
        return 0;
    }

    public boolean hasInput(String material)
    {
        return amountInput(material) != 0;
    }

    public boolean hasStation(String station)
    {
        for (String stat : stations)
        {
            if (stat.equals(station))
            {
                return true;
            }
        }
        return false;
    }

    public String toString()
    {
        // Inputs and Outputs
        StringBuilder string = new StringBuilder(buildInputOutputString());

        // Crafting Time
        string.append(". Takes ");
        string.append(crafting_time);
        string.append(" seconds");

        // Station Requirements/Options
        if (this.stations.size() > 0)
        {
            string.append(", ");
            if (has_req)
            {  
                string.append("needs ");
            } else
            {
                string.append("can use ");
            }
        }

        // All Usable Stations
        boolean first = true;
        for (String station : this.stations)
        {
            if (!station.equals("Assembly1") && !station.equals("Assembly2") && !station.equals("Assembly3"))
            {
                if (first)
                {
                    string.append(station);
                    first = false;
                } else
                {
                    string.append(", " + station);
                }
            }
        }

        // Productivity tag
        if (!can_prod)
        {
            string.append(", no productivity modules");
        }
        return string.toString();
    }

    private String buildInputOutputString()
    {
        StringBuilder string = new StringBuilder();
        boolean first = true;
        for (Material output : this.outputs)
        {
            if (first)
            {
                string.append(output.toString());
                first = false;
            } else
            {
                string.append(", " + output.toString());
            }
            if (!alt_name.equals("default"))
            {
                string.append("(" + alt_name + ")");
            }
        }

        string.append(" = ");

        first = true;
        for (Material input : this.inputs)
        {
            if (first)
            {
                string.append(input.toString());
                first = false;
            } else
            {
                string.append(", " + input.toString());
            }
        }
        return string.toString();
    }

    public String toStringSpecific(Station station, int prod_mod_level)
    {
        double speed = station.getSpeed(prod_mod_level);
        StringBuilder string = new StringBuilder("Using Recipe: ");
        string.append(buildInputOutputString());
        int percentage;
        double productivity = station.getProd(prod_mod_level);
        if (can_prod)
        {
            percentage = (int) (productivity * 100 - 100 + 0.5);
        } else 
        {
            percentage = 0;
        }
        string.append(" at " + station.name);
        string.append(".\nProductivity: " + percentage + "%");
        string.append(String.format(", Crafting Time: %.3f seconds.", crafting_time/speed));
        return string.toString();
    }
}
