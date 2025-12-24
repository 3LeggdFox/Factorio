import java.util.ArrayList;
import java.lang.StringBuilder;

public class Recipe
{
    ArrayList<Material> input = new ArrayList<Material>();
    ArrayList<Material> output = new ArrayList<Material>();
    boolean canProd;
    boolean hasReq;
    ArrayList<String> stations = new ArrayList<String>();
    String station = null;
    String altName;

    public Recipe() {}

    public Recipe(String line)
    {
        // Check Productivity Applicability Tag
        String[] segments = line.split("\\\\");         // Seperate productivity ability tag
        this.canProd = segments.length == 1;            // Temp solution. Only deals with productivity

        // Check For Alternate Station(s)
        String[] stations;
        segments = segments[0].split("\\*");            // Separate alternate station
        if (segments.length > 1)
        {
            // Query factory file for preferable station. Otherwise ask for preference before continuing.
            /* Still needs to be implemented */

            stations = segments[1].split(",");          // Split into different station options
            for (String station : stations)
            {
                this.stations.add(station.trim());      // Add to list of options
            }
        }

        // Check For Required Station(s)
        segments = segments[0].split("\\|");
        if (segments.length > 1)
        {
            this.hasReq = true;
            // Query factory file for preferable station. Otherwise ask for preference before continuing. Also check if any of the stations are unlocked
            /* Still needs to be implemented */
            stations = segments[1].split(",");          // Split into different station options
            for (String station : stations)
            {
                this.stations.add(station.trim());      // Add to list of options
            }
        } else
        {
            this.hasReq = false;
        }

        // Set Station If None Set
        if (this.station == null)
        {
            // Query factory file for most advanced assembly machine, set station to this
            /* Still needs to be implemented */
        }

        // Record Inputs
        segments = segments[0].split("=");          // Find Inputs and Outputs
        String[] inputs = segments[1].split(",");   // Separate Inputs
        output = new ArrayList<Material>();         // Initialise
        for (String inp : inputs)
        {
            inp = inp.trim();
            String[] pair = inp.split(" ");         // Split into quantity and material
            input.add(new Material(Double.parseDouble(pair[0]), pair[1])); // Append new Material
        }

        // Record Alternate Recipe
        /* Still needs to be implemented */

        // Record Outputs
        String[] outputs = segments[0].split(",");  // Separate Outputs
        output = new ArrayList<Material>();         // Initialise
        for (String out : outputs)
        {
            out = out.trim();
            String[] pair = out.split(" ");         // Split into quantity and material
            output.add(new Material(Double.parseDouble(pair[0]), pair[1])); // Append new Material
        }
    }

    public double amountOutput(String material)
    {
        for (Material output : this.output)
        {
            if (output.material.equals(material))
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
        for (Material input : this.input)
        {
            if (input.material.equals(material))
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

    public String toString()
    {
        StringBuilder string = new StringBuilder();
        boolean first = true;

        for (Material output : this.output)
        {
            if (first)
            {
                string.append(output.toString());
                first = false;
            } else
            {
                string.append(", " + output.toString());
            }
            if (!altName.equals("default"))
            {
                string.append("(" + altName + ")");
            }
        }

        string.append(" = ");

        first = true;
        for (Material input : this.input)
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

        if (this.stations.size() > 0)
        {
            if (hasReq)
            {  
                string.append(" | ");
            } else
            {
                string.append(" * ");
            }
        }

        first = true;
        for (String station : this.stations)
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

        if (!canProd)
        {
            string.append(" \\ prod");
        }
        return string.toString();
    }
}
