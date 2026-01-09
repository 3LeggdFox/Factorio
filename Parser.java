import java.util.ArrayList;
import java.util.HashMap;

public class Parser
{
    int position;
    String line;

    private Parser(String line)
    {
        this(0, line);
    }

    private Parser(int position, String line)
    {
        this.position = position;
        this.line = line;
    }

    public static Recipe parseRecipe(String line) throws ParsingException
    {
        Recipe recipe = new Recipe();
        Parser parser = new Parser(line);
        char nextChar = parser.nextChar();

        // Collect Outputs
        ArrayList<Material> outputs = new ArrayList<Material>();
        String material;
        double quantity;
        String altName = "default";
        while (true)        
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
        if (outputs.isEmpty()) // Check for syntax errors
        {
            throw new ParsingException("Error: No outputs found. Impossible Recipe.", line, parser.position);
        }

        // Collect Inputs
        parser.trim();
        nextChar = parser.increment();
        if (nextChar != '=')
        {
            throw new ParsingException("Error: Recipe format does not have \'=\' where expected.", line, parser.position-1);
        }
        ArrayList<Material> inputs = new ArrayList<Material>();
        while (true)        
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
        if (inputs.isEmpty()) // Check for syntax errors
        {
            throw new ParsingException("Error: No inputs found. Impossible Recipe.", line, parser.position);
        }

        // Collect Required Stations
        parser.trim();
        nextChar = parser.increment();
        ArrayList<String> stations = new ArrayList<String>();
        String station;
        boolean hasReq = false;
        if (nextChar == '|')
        {
            while (true)        
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

        // Collect Alternate Stations
        if (nextChar == '*')
        {
            while (true)        
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
        
        // Check if can use production modules
        if (nextChar == '\\') 
        {
            recipe.canProd = false;
        } else 
        {
            recipe.canProd = true;
        }

        if (!hasReq)
        {
            stations.add("Assembly1");
            stations.add("Assembly2");
            stations.add("Assembly3");
        }

        recipe.output = outputs;
        recipe.altName = altName;
        recipe.input = inputs;
        recipe.stations = stations;
        recipe.hasReq = hasReq;
        return recipe;
    }

    public static Setting parseSettings(String line) throws ParsingException
    {
        Parser parser = new Parser(line);
        String topic = parser.getWord();
        parser.trim();
        char charAt = parser.increment();
        if (charAt != '=')
        {
            throw new ParsingException("Error: Recipe format does not have \'=\' where expected.", line, parser.position-1);
        }
        String setting = parser.getNext();
        return new Setting(topic, setting);
    }

    public static Station parseStations(String line) throws ParsingException
    {
        Parser parser = new Parser(line);
        String station_name = parser.getWord();
        parser.eat(':');
        int modules = (int) parser.getNumber();
        parser.eatWord("modules");
        parser.eat(',');
        double productivity_bonus = parser.getNumber();
        parser.eat('%');
        parser.eat(',');
        int priority = (int) parser.getNumber();
        parser.eatWord("prio");
        return new Station(station_name, modules, productivity_bonus, priority);
    }

    public static Query parseQuery(String line, HashMap<String, Integer> allMaterials) throws ParsingException
    {
        Parser parser = new Parser(line);
        String firstWord = parser.getWord();
        boolean verbose = firstWord.equals("verbose");
        if (verbose)
        {
            firstWord = parser.getWord();
        }
        switch (firstWord)
        {
            case "get":
                String input = parser.getWord();
                parser.eatWord("in");
                String output = parser.getWord();
                return new QuantInQuery(input, output, verbose);
            case "list":
                String material = parser.getWord();
                return new ListQuery(material, verbose);
            case "help":
                return new HelpQuery(verbose);
            case "exit":
                System.out.println("Cheers.");
                System.exit(0);
            default:
                throw new ParsingException("Error: Command not recognised.", line, parser.position-1);
        }
    }

    private boolean isWord()
    {
        if (line.length() <= position)
        {
            return false;
        }
        char character = line.charAt(position);
        return (character >= 'a' && character <= 'z') || (character >= 'A' && character <= 'Z') || character == '_' || isNumber();
    }

    private boolean isNumber()
    {
        if (line.length() <= position)
        {
            return false;
        }
        char character = line.charAt(position);
        return (character >= '0' && character <= '9') || character == '.';
    }

    private String getWord() throws ParsingException
    {
        trim();
        int initPos = position;
        while (isWord())
        {
            position++;
        }
        if (initPos == position)
        {
            throw new ParsingException("Error: Either finding word at end of String, or finding word on non-word char.", line, position);
        }
        return line.substring(initPos, position);
    }

    private double getNumber() throws ParsingException
    {
        trim();
        int initPos = position;
        int decimals = 0;
        while (isNumber())
        {
            if (line.charAt(position) == '.')
            {
                decimals++;
                if (decimals == 2)
                {
                    throw new ParsingException("Error: Multiple decimal points in single number.", line, position);
                }
            }
            position++;
        }
        if (initPos == position)
        {
            throw new ParsingException("Error: Either finding number at end of String, or finding number on non-number char.", line, position);
        }
        return Double.parseDouble(line.substring(initPos, position));
    }

    private String getNext() throws ParsingException
    {
        trim();
        int initPos = position;
        while (isWord() || isNumber())
        {
            position++;
        }
        if (initPos == position)
        {
            throw new ParsingException("Error: Either finding word/number at end of String, or finding word/number on non-word/number char.", line, position);
        }
        return line.substring(initPos, position);
    }

    private void trim()
    {
        while (line.length() > position && line.charAt(position) == ' ')
        {
            position++;
        }
        return;
    }

    private char increment()
    {
        if (line.length() <= position)
        {
            return 0;
        }
        return line.charAt(position++);
    }

    private void eat(char expected) throws ParsingException
    {
        if (line.length() <= position)
        {
            throw new ParsingException("Error: Trying to eat past end of string.", line, position);
        }
        if (expected != line.charAt(position++))
        {
            throw new ParsingException("Error: Expected '" + expected + "', found '" + line.charAt(position-1) + "'.", line, position-1);
        }
        return;
    }

    private void eatWord(String expected) throws ParsingException
    {
        String word = getWord();
        if (!expected.equals(word))
        {
            throw new ParsingException("Error: Expected '" + expected + "' found '" + word + "'.", line, position-1);
        }
    }

    private char nextChar()
    {
        if (line.length() <= position)
        {
            return 0;
        }
        return line.charAt(position);
    }
}

class ParsingException extends Exception
{
    String offender;
    int position; 

    public ParsingException(String message, String offender, int position)
    {
        super(message);
        this.offender = offender;
        this.position = position;
    }

    public String getMessage()
    {
        StringBuilder builder = new StringBuilder(super.getMessage());
        builder.append("\n" + offender + "\n");
        char[] positionString = " ".repeat(offender.length()).toCharArray();
        if (position < offender.length())
        {
            positionString[position] = '^'; 
        }
        builder.append(positionString);
        return builder.toString();
    }
}