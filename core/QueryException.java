package core;

public abstract class QueryException extends RuntimeException {

    public QueryException()
    {
        super();
    }

    public QueryException(String message)
    {
        super(message);
    }
}

class InvalidModuleLevelException extends QueryException
{
    int level;

    public InvalidModuleLevelException(int level)
    {
        super();
        this.level = level;
    }

    public String getMessage() {
        return "Productivity module level '" + level + "' is out of range [0, 3].";
    }
}