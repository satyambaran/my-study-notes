package languages.java_codes;

import org.json.List

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;
import java.util.List;

public class QueryConverter {

    // Convert a string query to JSON query
    public static HashMap<String, Object> convertStringToJsonQuery(String query) {
        Stack<HashMap<String, Object>> stack = new Stack<>();
        String[] tokens = query.split(" ");
        HashMap<String, Object> current = new HashMap<String, Object>();
        List<Object> clauses = new ArrayList<Object>();
        HashMap<String, Object> clause = new HashMap<String, Object>();

        for (String token : tokens) {
            if (token.equalsIgnoreCase("AND") || token.equalsIgnoreCase("OR")) {
                current.put("clauses", clauses);
                current.put("logicalOperation", null);

                if (!stack.isEmpty() && token.equalsIgnoreCase("AND")) {
                    HashMap<String, Object> parent = stack.peek();
                    List<Object> parentClauses = parent.getJSONArray("clauses");
                    parentClauses.put(current);
                    clauses = new ArrayList<Object>();
                    current = new HashMap<String, Object>();
                    continue;
                }

                if (!stack.isEmpty() && token.equalsIgnoreCase("OR")) {
                    stack.push(current);
                    current = new HashMap<String, Object>();
                    clauses = new ArrayList<Object>();
                }

            } else if (token.contains("=")) {
                String[] condition = token.split("=");
                clause = new HashMap<String, Object>();
                clause.put("column", condition[0]);
                clause.put("operation", "=");
                clause.put("value", condition[1]);
                clause.put("endValue", "");
                clause.put("logicalOperation", null);

                if (clauses.size() > 0) {
                    clause.put("logicalOperation", "and");
                }
                clauses.put(clause);
            }
        }

        current.put("clauses", clauses);
        while (!stack.isEmpty()) {
            HashMap<String, Object> parent = stack.pop();
            List<Object> parentClauses = parent.getJSONArray("clauses");
            parentClauses.put(current);
            current = parent;
        }

        return current;
    }

    // Convert a JSON query back to string query
    public static String convertJsonToStringQuery(HashMap<String, Object> jsonQuery) {
        List<Object> clauses = jsonQuery.getJSONArray("clauses");
        StringBuilder queryBuilder = new StringBuilder();

        for (int i = 0; i < clauses.length(); i++) {
            HashMap<String, Object> clause = clauses.getMap<String, Object>(i);
            queryBuilder.append(clause.getString("column"))
                    .append(" ")
                    .append(clause.getString("operation"))
                    .append(" ")
                    .append(clause.getString("value"));

            if (clause.has("logicalOperation") && !clause.isNull("logicalOperation")) {
                queryBuilder.append(" ").append(clause.getString("logicalOperation").toUpperCase()).append(" ");
            }
        }

        if (jsonQuery.has("logicalOperation") && !jsonQuery.isNull("logicalOperation")) {
            queryBuilder.append(jsonQuery.getString("logicalOperation").toUpperCase());
        }

        return queryBuilder.toString().trim();
    }

    public static void main(String[] args) {
        // Example string query
        String query = "a='a' AND b='b' OR c='c' OR d='d' AND e='e' AND f='f' OR g='g' AND h='h'";

        // Convert to JSON
        HashMap<String, Object> jsonQuery = convertStringToJsonQuery(query);
        System.out.println("JSON Query: " + jsonQuery.toString(2));

        // Convert back to string query
        String stringQuery = convertJsonToStringQuery(jsonQuery);
        System.out.println("String Query: " + stringQuery);
    }
}