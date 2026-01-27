package core;

/**
 * QueryException
 * Exception parent class for all exceptions brought about by queries
 * 
 * @version 1.0
 */
public class QueryException extends RuntimeException {

    public QueryException() {
        super();
    }

    public QueryException(String message) {
        super(message);
    }

    public String getMessage() {
        return super.getMessage();
    }
}

/**
 * InvalidModuleLevelException
 * Exception for invalid module level input by user
 * 
 * @version 1.0
 */
class InvalidModuleLevelException extends QueryException {
    int level;

    public InvalidModuleLevelException(int level) {
        super();
        this.level = level;
    }

    public String getMessage() {
        return "Productivity module level '" + level + "' is out of range [0, 3].";
    }
}