package core;

public class SearchQuery extends Query {
    String search_string1;
    boolean logic_is_and;
    String search_string2;

    public SearchQuery(String search_string1, String logic_operator, String search_string2) {
        this.search_string1 = search_string1;
        if (logic_operator == null) {
            logic_is_and = false;
        } else if (logic_operator.equals("or")) {
            this.logic_is_and = false;
        } else if (logic_operator.equals("and")) {
            this.logic_is_and = true;
        } 
        this.search_string2 = search_string2;
    }

    public void query(RecipeBrowser browser) {
        String searching_string = search_string1;
        boolean has_second_argument = search_string2 != null;
        if (has_second_argument) {
            if (logic_is_and) {
                searching_string += "' and '";
            } else {
                searching_string += "' or '";
            }
            searching_string += search_string2;
        }
        System.out.println("Materials matching '" + searching_string + "':");
        browser.searchQuery(search_string1, logic_is_and, search_string2, has_second_argument, browser.all_materials.keySet(), false);
        System.out.println("\nSettings matching '" + searching_string + "':");
        browser.searchQuery(search_string1, logic_is_and, search_string2, has_second_argument, browser.settings.keySet(), true);
    }
}
