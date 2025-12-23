import java.util.ArrayList;

public class Recipe 
{
    ArrayList<Material> input = new ArrayList<Material>();
    ArrayList<Material> output = new ArrayList<Material>();
    boolean canProd;
    boolean hasReq;
    ArrayList<String> stations = new ArrayList<String>();
    String station = null;

    public Recipe(String line)
    {
        // Check Productivity Applicability Tag
        String[] segments = line.split("\\\\");         // Seperate productivity ability tag
        this.canProd = segments.length == 1;              // Temp solution. Only deals with productivity

        // Check For Alternate Station(s)
        String[] stations;
        segments = segments[0].split("\\*");          // Separate alternate station
        if (segments.length > 1)
        {
            // Query factory file for preferable station. Otherwise ask for preference before continuing.
            /* Still needs to be implemented */

            stations = segments[1].split(",");     // Split into different station options
            for (String station : stations)
            {
                this.stations.add(station.trim()); // Add to list of options
            }
        }

        // Check For Required Station(s)
        segments = segments[0].split("\\|");
        if (segments.length > 1)
        {
            this.hasReq = true;
            // Query factory file for preferable station. Otherwise ask for preference before continuing. Also check if any of the stations are unlocked
            /* Still needs to be implemented */
            stations = segments[1].split(",");     // Split into different station options
            for (String station : stations)
            {
                this.stations.add(station.trim()); // Add to list of options
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
}
