import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Clause {
    String column, operation, value, endValue, logicalOperation;

    Clause(String column, String operation, String value, String endValue, String logicalOperation) {
        this.column = column;
        this.operation = operation;
        this.value = value;
        this.endValue = endValue;
        this.logicalOperation = logicalOperation;
    }

    @Override
    public String toString() {
        return String.format("column: %s, operation: %s, value: %s, endValue: %s, logicalOperation: %s\n", column,
                operation, value, endValue, logicalOperation);
    }
}

class Condition {
    List<Clause> clauses = new ArrayList<>();
    String logicalOperation;

    Condition(String logicalOperation) {
        this.logicalOperation = logicalOperation;
    }

    Condition(List<Clause> clauses, String logicalOperation) {
        this.clauses = clauses;
        this.logicalOperation = logicalOperation;
    }

    @Override
    boolean equals(Condition other) {
        return true;
    }

    @Override
    public String toString() {
        return String.format("logicalOperation: %s \nclauses: %s\n", logicalOperation, clauses.toString());
    }
}

public class Main {
    public static void main(String[] args) {
        List<Condition> con = getCon();

        String query = getQuery(con);
        System.out.println(query + "\n\n");

        List<Condition> con2 = parseString(query);
        System.out.println(con2 + "\n\n");

        String query2 = getQuery(con2);
        List<Condition> con3 = parseString(query2);
        String query3 = getQuery(con3);
        System.out.println(query + "\n\n" + query2 + "\n\n" + query3 + "\n\n" + query.equals(query2) + "\n\n"
                + query2.equals(query3));
        System.out.printf("%s %s %s %b %b\n", con.toString(), con2.toString(), con3.toString(), con.toString().equals(con2.toString()),
                con2.toString().equals(con3.toString()));
    }

    private static List<Condition> getCon() {
        List<Condition> con = new ArrayList<>();

        // First condition
        List<Clause> clauses1 = new ArrayList<>();
        clauses1.add(new Clause("a", "=", "a", "", null));
        clauses1.add(new Clause("b", "=", "b", "", "and"));
        clauses1.add(new Clause("c", "between", "ca", "cb", "and"));
        clauses1.add(new Clause("d", "=", "d", "", "or"));
        clauses1.add(new Clause("e", "=", "e", "", "or"));
        clauses1.add(new Clause("f", "=", "f", "", "and"));
        clauses1.add(new Clause("g", "between", "ga", "gb", "and"));
        clauses1.add(new Clause("h", "=", "h", "", "or"));
        clauses1.add(new Clause("i", "=", "i", "", "or"));
        clauses1.add(new Clause("j", "=", "j", "", "and"));
        clauses1.add(new Clause("k", "between", "ka", "kb", "and"));
        con.add(new Condition(clauses1, null));

        // Second condition
        List<Clause> clauses2 = new ArrayList<>();
        clauses2.add(new Clause("aa", "=", "aa", "", null));
        clauses2.add(new Clause("bb", "=", "bb", "", "and"));
        clauses2.add(new Clause("cc", "between", "ca", "cb", "and"));
        clauses2.add(new Clause("dd", "=", "dd", "", "or"));
        clauses2.add(new Clause("ee", "=", "ee", "", "or"));
        clauses2.add(new Clause("ff", "=", "ff", "", "and"));
        clauses2.add(new Clause("gg", "between", "ga", "gb", "and"));
        clauses2.add(new Clause("hh", "=", "hh", "", "or"));
        clauses2.add(new Clause("ii", "=", "ii", "", "or"));
        clauses2.add(new Clause("jj", "=", "jj", "", "and"));
        clauses2.add(new Clause("kk", "between", "ka", "kb", "and"));
        con.add(new Condition(clauses2, "or"));

        // Other conditions...
        List<Clause> clauses3 = new ArrayList<>();
        clauses3.add(new Clause("remove_it", "=", "d", "", null));
        con.add(new Condition(clauses3, null));

        List<Clause> clauses4 = new ArrayList<>();
        clauses4.add(new Clause("d", "=", "d", "", null));
        con.add(new Condition(clauses4, "or"));

        List<Clause> clauses5 = new ArrayList<>();
        clauses5.add(new Clause("z", "=", "z", "", null));
        con.add(new Condition(clauses5, "or"));

        List<Clause> clauses6 = new ArrayList<>();
        clauses6.add(new Clause("e", "=", "e", "", null));
        clauses6.add(new Clause("f", "=", "f", "", "and"));
        con.add(new Condition(clauses6, "and"));

        List<Clause> clauses7 = new ArrayList<>();
        clauses7.add(new Clause("h", "=", "h", "", null));
        clauses7.add(new Clause("g", "=", "g", "", "and"));
        con.add(new Condition(clauses7, "and"));

        List<Clause> clauses8 = new ArrayList<>();
        clauses8.add(new Clause("i", "=", "i", "", null));
        clauses8.add(new Clause("j", "=", "j", "", "and"));
        con.add(new Condition(clauses8, "and"));

        List<Clause> clauses9 = new ArrayList<>();
        clauses9.add(new Clause("k", "=", "k", "", null));
        con.add(new Condition(clauses9, "or"));

        List<Clause> clauses10 = new ArrayList<>();
        clauses10.add(new Clause("l", "=", "l", "", null));
        con.add(new Condition(clauses10, "or"));

        List<Clause> clauses11 = new ArrayList<>();
        clauses11.add(new Clause("m", "=", "m", "", null));
        clauses11.add(new Clause("n", "=", "n", "", "or"));
        con.add(new Condition(clauses11, "or"));

        List<Clause> clauses12 = new ArrayList<>();
        clauses12.add(new Clause("o", "=", "o", "", null));
        clauses12.add(new Clause("p", "=", "p", "", "or"));
        con.add(new Condition(clauses12, "and"));

        List<Clause> clauses13 = new ArrayList<>();
        clauses13.add(new Clause("q", "=", "q", "", null));
        clauses13.add(new Clause("r", "=", "r", "", "and"));
        con.add(new Condition(clauses13, "and"));

        List<Clause> clauses14 = new ArrayList<>();
        clauses14.add(new Clause("s", "=", "t", "", null));
        con.add(new Condition(clauses14, "or"));

        List<Clause> clauses15 = new ArrayList<>();
        clauses15.add(new Clause("u", "=", "u", "", null));
        con.add(new Condition(clauses15, "or"));

        List<Clause> clauses16 = new ArrayList<>();
        clauses16.add(new Clause("v", "=", "v", "", null));
        clauses16.add(new Clause("x", "=", "x", "", "and"));
        con.add(new Condition(clauses16, "and"));
        return con;
    }

    public static String clauseToString(Clause clause) {
        StringBuilder query = new StringBuilder();
        if ("and".equals(clause.logicalOperation)) {
            query.append(" and ");
        }
        query.append("(").append(clause.column).append(" ").append(clause.operation).append(" ");
        if ("=".equals(clause.operation)) {
            query.append("'").append(clause.value).append("'");
        } else if ("between".equals(clause.operation)) {
            query.append("'").append(clause.value).append("' and '").append(clause.endValue).append("'");
        }
        query.append(")");
        return query.toString();
    }

    public static String conditionToString(List<Clause> clauses) {
        StringBuilder query = new StringBuilder();
        for (int i = 0; i < clauses.size(); i++) {
            if ("or".equals(clauses.get(i).logicalOperation)) {
                query.append(" or ");
            }
            if (clauses.get(i).logicalOperation == null || "or".equals(clauses.get(i).logicalOperation)) {
                query.append("(").append(clauseToString(clauses.get(i)));
                while (i + 1 < clauses.size() && "and".equals(clauses.get(i + 1).logicalOperation)) {
                    i++;
                    query.append(clauseToString(clauses.get(i)));
                }
                query.append(")");
            }
        }
        return query.toString();
    }

    public static String getQuery(List<Condition> conditions) {
        StringBuilder query = new StringBuilder();
        for (int i = 0; i < conditions.size(); i++) {
            if ("or".equals(conditions.get(i).logicalOperation)) {
                query.append(" or ");
            }
            if (conditions.get(i).logicalOperation == null || "or".equals(conditions.get(i).logicalOperation)) {
                query.append("(").append(conditionToString(conditions.get(i).clauses));
                while (i + 1 < conditions.size() && "and".equals(conditions.get(i + 1).logicalOperation)) {
                    i++;
                    query.append(" and ").append(conditionToString(conditions.get(i).clauses));
                }
                query.append(")");
            }
        }
        return query.toString();
    }

    public static Clause parseClause(String str) {
        Clause clause = new Clause("", "", "", "", null);
        Pattern pattern = Pattern.compile("(\\w+)\\s*([=]|between)\\s*'([^']*)'(?:\\s*and\\s*'([^']*)')?");
        Matcher matcher = pattern.matcher(str);
        if (matcher.find()) {
            clause.column = matcher.group(1);
            clause.operation = matcher.group(2);
            clause.value = matcher.group(3);
            if ("between".equals(matcher.group(2))) {
                clause.endValue = matcher.group(4);
            }
        }
        return clause;
    }

    public static List<Condition> parseString(String query) {
        query = query.replaceAll("\\s+", " ").trim();
        Pattern clausePattern = Pattern
                .compile("\\((\\w+\\s*[=]\\s*'[^']*'|\\w+\\s*between\\s*'[^']*'\\s*and\\s*'[^']*')\\)");
        Pattern logicalOpPattern = Pattern.compile("\\b(and|or)\\b");
        List<Condition> conditions = new ArrayList<>();
        Condition clauseList = new Condition(null);
        Matcher clauseMatch = clausePattern.matcher(query);
        Matcher logicalOpMatch;
        Clause clauseObj;
        String lastLogicalOp = null;
        int lastClausePos = 0;
        while (clauseMatch.find()) {
            clauseObj = parseClause(clauseMatch.group(1));
            clauseObj.logicalOperation = lastLogicalOp;
            lastClausePos = clauseMatch.end() - 1;
            clauseList.clauses.add(clauseObj);
            logicalOpMatch = logicalOpPattern.matcher(query.substring(clauseMatch.end()));
            if (logicalOpMatch.find()) {
                lastLogicalOp = logicalOpMatch.group(1);
            } else {
                lastLogicalOp = null;
            }
            if (query.substring(lastClausePos, lastClausePos + 3).equals(")))")) {
                conditions.add(clauseList);
                clauseList = new Condition(lastLogicalOp);
                lastLogicalOp = null;
            }
        }
        return conditions;
    }
}