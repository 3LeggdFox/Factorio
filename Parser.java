import java.util.ArrayList;

public class Parser
{
    int position;
    String line;

    public Parser(String line)
    {
        this(0, line);
    }

    public Parser(int position, String line)
    {
        this.position = position;
        this.line = line;
    }

    public static Recipe parseRecipe(String line)
    {
        Recipe recipe = new Recipe();
        Parser parser = new Parser(line);
        char nextChar = parser.nextChar();

        ArrayList<Material> outputs = new ArrayList<Material>();
        String material;
        double quantity;
        String altName = "default";
        while (true)        // Collect Outputs
        {
            quantity = parser.getNumber();
            material = parser.getWord();
            outputs.add(new Material(quantity, material));
            nextChar = parser.increment();
            if (nextChar == '(')
            {
                altName = parser.getWord();
                parser.increment();
                nextChar = parser.nextChar();
            }
            if (nextChar != ',')
            {
                break;
            }
        }

        parser.trim();
        nextChar = parser.increment();
        if (nextChar != '=')
        {
            System.out.println("Error: Recipe format does not have \'=\' where expected.");
            return null;
        }
        ArrayList<Material> inputs = new ArrayList<Material>();
        while (true)        // Collect Inputs
        {
            quantity = parser.getNumber();
            material = parser.getWord();
            inputs.add(new Material(quantity, material));
            nextChar = parser.increment();
            if (nextChar != ',')
            {
                break;
            }
        }

        parser.trim();
        nextChar = parser.increment();
        ArrayList<String> stations = new ArrayList<String>();
        String station;
        boolean hasReq = false;
        if (nextChar == '|')
        {
            while (true)        // Collect Required Stations
            {
                station = parser.getWord();
                stations.add(station);
                nextChar = parser.nextChar();
                if (nextChar != ',')
                {
                    break;
                }
            }
            hasReq = true;
            parser.trim();
            nextChar = parser.nextChar();
        }

        if (nextChar == '*')
        {
            while (true)        // Collect Alternate Stations
            {
                station = parser.getWord();
                stations.add(station);
                nextChar = parser.nextChar();
                if (nextChar != ',')
                {
                    break;
                }
            }
            parser.trim();
            nextChar = parser.nextChar();
        }
        
        if (nextChar == '\\') // Check if can use production modules
        {
            recipe.canProd = false;
        } else 
        {
            recipe.canProd = true;
        }

        recipe.output = outputs;
        recipe.altName = altName;
        recipe.input = inputs;
        recipe.stations = stations;
        recipe.hasReq = hasReq;
        return recipe;
    }

    public static Setting parseFactory(String line)
    {
        Parser parser = new Parser(line);
        String topic = parser.getWord();
        parser.trim();
        char charAt = parser.increment();
        if (charAt != '=')
        {
            System.out.println("Error: Recipe format does not have \'=\' where expected.");
            return null;
        }
        String setting = parser.getNext();
        return new Setting(topic, setting);
    }

    public boolean isWord()
    {
        if (line.length() <= position)
        {
            return false;
        }
        char character = line.charAt(position);
        return (character >= 'a' && character <= 'z') || (character >= 'A' && character <= 'Z') || character == '_';
    }

    public boolean isNumber()
    {
        if (line.length() <= position)
        {
            return false;
        }
        char character = line.charAt(position);
        return (character >= '0' && character <= '9');
    }

    public String getWord()
    {
        trim();
        int initPos = position;
        while (isWord())
        {
            position++;
        }
        if (initPos == position)
        {
            System.out.println("Error: Either finding word at end of String, or finding word on non-word char.");
        }
        return line.substring(initPos, position);
    }

    public double getNumber()
    {
        trim();
        int initPos = position;
        while (isNumber())
        {
            position++;
        }
        if (initPos == position)
        {
            System.out.println("Error: Either finding number at end of String, or finding number on non-number char.");
        }
        return Double.parseDouble(line.substring(initPos, position));
    }

    public String getNext()
    {
        trim();
        int initPos = position;
        while (isWord() || isNumber())
        {
            position++;
        }
        if (initPos == position)
        {
            System.out.println("Error: Either finding word/number at end of String, or finding word/number on non-word/number char.");
        }
        return line.substring(initPos, position);
    }

    public void trim()
    {
        if (line.length() <= position)
        {
            return;
        }
        while (line.charAt(position) == ' ')
        {
            position++;
            if (line.length() <= position)
            {
                System.out.println("Error: Trying to trim end of String.");
                return;
            }
        }
        return;
    }

    public char increment()
    {
        if (line.length() <= position)
        {
            return 0;
        }
        return line.charAt(position++);
    }

    public void gotoChar(char character)
    {
        while(line.charAt(position) != character)
        {
            position++;
            if (line.length() <= position)
            {
                System.out.println("Error: Trying to trim end of String.");
                return;
            }
        }
        return;
    }

    public char nextChar()
    {
        if (line.length() <= position)
        {
            return 0;
        }
        return line.charAt(position);
    }
}
