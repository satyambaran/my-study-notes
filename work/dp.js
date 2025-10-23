// /*
//     // Dynamic programming = Recursion + Memoization
//     // https://www.youtube.com/watch?v=Hdr64lKQ3e4
//     /*

//     define subproblem
//     base case
//     recursive formula
//     Memoization

//     greedy not working, try dp
//     */

//     /*
//         Reservoir Sampling
//             1. Choose the random number from a array where length of array not known
//             2. Choose a random node from a linked list in one pass
//         Algo:
//             int randomNum=-1, i=0, randIdx;
//             while(num:stream){
//                 ++i;
//                 randIdx = rand()%i;
//                 if(randIdx == i-1){
//                     randomNum = num
//                 }
//             }
//             return randomNum;

//             P(i getting chosen) = P(i getting choosen at ith step)*P(not getting replaced by next steps) = (1/i)*((1-1/i+1)*(1-1/i+2)*(1-1/i+3)*(1-1/i+4)*....*(1-1/n-1)*(1-1/n))
//                 = 1/n

//             Now, let’s generalize this to selecting k items.

//             1.	Initialize: Fill the reservoir with the first k items of the stream.
//             2.	Iterate through the stream, starting from the k+1-th item:
//                 •	For the i-th item, generate a random number j between 1 and i.
//                 •	If j <= k, replace the j-th item in the reservoir with the i-th item.
//             3.	Continue until you reach the end of the stream.
//         .

//     */

//     // class Clause {
//     // 	constructor(column, operation, value, endValue = "", logicalOperation = null) {
//     // 		this.column = column;
//     // 		this.operation = operation;
//     // 		this.value = value;
//     // 		this.endValue = endValue;
//     // 		this.logicalOperation = logicalOperation;
//     // 	}
//     // }
//     // class Condition {
//     // 	constructor(clauses, logicalOperation = null) {
//     // 		this.clauses = clauses; // Array of Clause objects
//     // 		this.logicalOperation = logicalOperation;
//     // 	}
//     // }
//     // class SearchCriteria {
//     // 	constructor(conditions) {
//     // 		this.conditions = conditions; // Array of Condition objects
//     // 	}
//     // }
//     // class Query {
//     // 	constructor(searchCriteria) {
//     // 		this.searchCriteria = searchCriteria; // A SearchCriteria object
//     // 	}
//     // }

//     // // Create Clause objects
//     // const clause1 = new Clause("a", "=", "a");
//     // const clause2 = new Clause("b", "=", "b", "", "and");
//     // const clause3 = new Clause("c", "between", "ca", "cb", "and");

//     // const clause4 = new Clause("d", "=", "d");

//     // const clause5 = new Clause("e", "=", "e");
//     // const clause6 = new Clause("f", "=", "f", "", "and");

//     // // Create Condition objects
//     // const condition1 = new Condition([clause1, clause2, clause3]);
//     // const condition2 = new Condition([clause4], "or");
//     // const condition3 = new Condition([clause5, clause6]);

//     // function searchCriteriaToQuery() {
//     // 	const query = new Query(
//     // 		new SearchCriteria([
//     // 			new Condition([
//     // 				new Clause("a", "=", "a"),
//     // 				new Clause("b", "=", "b", "", "and"),
//     // 				new Clause("c", "between", "ca", "cb", "and"),
//     // 			]),
//     // 			new Condition([new Clause("d", "=", "d")], "or"),
//     // 			new Condition([new Clause("e", "=", "e"), new Clause("f", "=", "f", "", "and")], "and"),
//     // 		])
//     // 	);
//     // 	console.log(JSON.stringify(query, null, 4));
//     //     let str = ""
//     //     for(let i=0;i<query.searchCriteria)
//     // }
//     // searchCriteriaToQuery();

query = [
	{
		clauses: [
			{ column: "a", operation: "=", value: "a", endValue: "", logicalOperation: null },
			{ column: "b", operation: "=", value: "b", endValue: "", logicalOperation: "and" },
		],
		logicalOperation: "and",
	},
	{
		clauses: [{ column: "c", operation: "=", value: "c", endValue: "", logicalOperation: null }],
		logicalOperation: "or",
	},
	{
		clauses: [{ column: "d", operation: "=", value: "d", endValue: "", logicalOperation: null }],
		logicalOperation: "and",
	},
	{
		clauses: [
			{ column: "e", operation: "=", value: "e", endValue: "", logicalOperation: "and" },
			{ column: "f", operation: "=", value: "f", endValue: "", logicalOperation: "and" },
		],
		logicalOperation: "or",
	},
	{
		clauses: [
			{ column: "g", operation: "=", value: "g", endValue: "", logicalOperation: null },
			{ column: "h", operation: "=", value: "h", endValue: "", logicalOperation: "and" },
		],
		logicalOperation: "and",
	},
];
//     // /*
//     // import org.json.JSONArray;
//     // import org.json.JSONObject;

//     // import java.util.Stack;

//     // public class QueryConverter {

//     //     // Convert a string query to JSON query
//     //     public static JSONObject convertStringToJsonQuery(String query) {
//     //         Stack<JSONObject> stack = new Stack<>();
//     //         String[] tokens = query.split(" ");
//     //         JSONObject current = new JSONObject();
//     //         JSONArray clauses = new JSONArray();
//     //         JSONObject clause = new JSONObject();

//     //         for (String token : tokens) {
//     //             if (token.equalsIgnoreCase("(")) {
//     //                 // Push the current state onto the stack and start a new group
//     //                 stack.push(current);
//     //                 current = new JSONObject();
//     //                 clauses = new JSONArray();
//     //             } else if (token.equalsIgnoreCase(")")) {
//     //                 // Finalize the current group and pop the previous group from the stack
//     //                 current.put("clauses", clauses);
//     //                 if (!stack.isEmpty()) {
//     //                     JSONObject parent = stack.pop();
//     //                     JSONArray parentClauses = parent.getJSONArray("clauses");
//     //                     parentClauses.put(current);
//     //                     current = parent;
//     //                     clauses = parentClauses;
//     //                 }
//     //             } else if (token.equalsIgnoreCase("AND") || token.equalsIgnoreCase("OR")) {
//     //                 current.put("clauses", clauses);
//     //                 current.put("logicalOperation", token.equalsIgnoreCase("AND") ? "and" : "or");

//     //                 if (!stack.isEmpty() && token.equalsIgnoreCase("AND")) {
//     //                     JSONObject parent = stack.peek();
//     //                     JSONArray parentClauses = parent.getJSONArray("clauses");
//     //                     parentClauses.put(current);
//     //                     clauses = new JSONArray();
//     //                     current = new JSONObject();
//     //                     continue;
//     //                 }

//     //                 if (!stack.isEmpty() && token.equalsIgnoreCase("OR")) {
//     //                     stack.push(current);
//     //                     current = new JSONObject();
//     //                     clauses = new JSONArray();
//     //                 }

//     //             } else if (token.contains("=")) {
//     //                 String[] condition = token.split("=");
//     //                 clause = new JSONObject();
//     //                 clause.put("column", condition[0].replace("(", "").replace(")", ""));
//     //                 clause.put("operation", "=");
//     //                 clause.put("value", condition[1].replace("(", "").replace(")", ""));
//     //                 clause.put("endValue", "");
//     //                 clause.put("logicalOperation", clauses.length() > 0 ? "and" : null);
//     //                 clauses.put(clause);
//     //             }
//     //         }

//     //         current.put("clauses", clauses);
//     //         while (!stack.isEmpty()) {
//     //             JSONObject parent = stack.pop();
//     //             JSONArray parentClauses = parent.getJSONArray("clauses");
//     //             parentClauses.put(current);
//     //             current = parent;
//     //         }

//     //         return current;
//     //     }

//     //     // Convert a JSON query back to string query
//     //     public static String convertJsonToStringQuery(JSONObject jsonQuery) {
//     //         JSONArray clauses = jsonQuery.getJSONArray("clauses");
//     //         StringBuilder queryBuilder = new StringBuilder();

//     //         for (int i = 0; i < clauses.length(); i++) {
//     //             JSONObject clause = clauses.getJSONObject(i);
//     //             queryBuilder.append(clause.getString("column"))
//     //                     .append(" ")
//     //                     .append(clause.getString("operation"))
//     //                     .append(" ")
//     //                     .append(clause.getString("value"));

//     //             if (clause.has("logicalOperation") && !clause.isNull("logicalOperation")) {
//     //                 queryBuilder.append(" ").append(clause.getString("logicalOperation").toUpperCase()).append(" ");
//     //             }
//     //         }

//     //         if (jsonQuery.has("logicalOperation") && !jsonQuery.isNull("logicalOperation")) {
//     //             queryBuilder.append(jsonQuery.getString("logicalOperation").toUpperCase());
//     //         }

//     //         return queryBuilder.toString().trim();
//     //     }

//     //     public static void main(String[] args) {
//     //         // Example string query with brackets
//     //         String query = "a='a' AND b='b' AND (c='c' OR d='d')";

//     //         // Convert to JSON
//     //         JSONObject jsonQuery = convertStringToJsonQuery(query);
//     //         System.out.println("JSON Query: " + jsonQuery.toString(2));

//     //         // Convert back to string query
//     //         String stringQuery = convertJsonToStringQuery(jsonQuery);
//     //         System.out.println("String Query: " + stringQuery);
//     //     }
//     // }
//     // */
// */
function inQuotes(str) {
	return "'" + str + "'";
}
function func(clause) {
	var clause = { column: "b", operation: "=", value: "b", endValue: "", logicalOperation: "and" };
	// clause = { column: "c", operation: "between", value: "ca", endValue: "cb", logicalOperation: "and" };
	// clause = { column: "c", operation: "between", value: "ca", endValue: "cb", logicalOperation: null };
	let query = "";
	// console.log(clause);
	if (clause.logicalOperation == "and") {
		query = query + " and ";
	}
	query = query + "(" + clause.column + " " + clause.operation + " ";
	if (clause.operation == "=") {
		// query = query + inQuotes(clause.value);
		query = query + `'${clause.value}'`;
	} else if (clause.operation == "between") {
		query = query + `'${clause.value}'` + " and " + `'${clause.endValue}'`;
	} else {
	}
	query = query + ")";
	return query;
}
function subFun(clauses) {
	// var clauses = [
	// 	{ column: "a", operation: "=", value: "a", endValue: "", logicalOperation: null },
	// 	{ column: "b", operation: "=", value: "b", endValue: "", logicalOperation: "and" },
	// 	{ column: "c", operation: "between", value: "ca", endValue: "cb", logicalOperation: "and" },
	// 	{ column: "a", operation: "=", value: "a", endValue: "", logicalOperation: "or" },
	// 	{ column: "a", operation: "=", value: "a", endValue: "", logicalOperation: "or" },
	// 	{ column: "b", operation: "=", value: "b", endValue: "", logicalOperation: "and" },
	// 	{ column: "c", operation: "between", value: "ca", endValue: "cb", logicalOperation: "and" },
	// 	{ column: "a", operation: "=", value: "a", endValue: "", logicalOperation: "or" },
	// 	{ column: "a", operation: "=", value: "a", endValue: "", logicalOperation: "or" },
	// 	{ column: "b", operation: "=", value: "b", endValue: "", logicalOperation: "and" },
	// 	{ column: "c", operation: "between", value: "ca", endValue: "cb", logicalOperation: "and" },
	// ];
	let query = "";
	for (let i = 0; i < clauses.length; i++) {
		if (clauses[i].logicalOperation == "or") {
			query = query + " or ";
		}
		if (clauses[i].logicalOperation == null || clauses[i].logicalOperation == "or") {
			query = query + "(" + func(clauses[i]);
			while (i + 1 < clauses.length && clauses[i + 1].logicalOperation == "and") {
				i++;
				query = query + func(clauses[i]);
			}
			query = query + ")";
		}
	}
	return query;
}
// console.log(subFun());
function fn() {
	var qq = [
		{
			clauses: [
				{ column: "a", operation: "=", value: "a", endValue: "", logicalOperation: null },
				{ column: "b", operation: "=", value: "b", endValue: "", logicalOperation: "and" },
				{ column: "c", operation: "between", value: "ca", endValue: "cb", logicalOperation: "and" },
				{ column: "d", operation: "=", value: "d", endValue: "", logicalOperation: "or" },
				{ column: "e", operation: "=", value: "e", endValue: "", logicalOperation: "or" },
				{ column: "f", operation: "=", value: "f", endValue: "", logicalOperation: "and" },
				{ column: "g", operation: "between", value: "ga", endValue: "gb", logicalOperation: "and" },
				{ column: "h", operation: "=", value: "h", endValue: "", logicalOperation: "or" },
				{ column: "i", operation: "=", value: "i", endValue: "", logicalOperation: "or" },
				{ column: "j", operation: "=", value: "j", endValue: "", logicalOperation: "and" },
				{ column: "k", operation: "between", value: "ka", endValue: "kb", logicalOperation: "and" },
			],
			logicalOperation: null,
		},
		{
			clauses: [
				{ column: "aa", operation: "=", value: "aa", endValue: "", logicalOperation: null },
				{ column: "bb", operation: "=", value: "bb", endValue: "", logicalOperation: "and" },
				{ column: "cc", operation: "between", value: "ca", endValue: "cb", logicalOperation: "and" },
				{ column: "dd", operation: "=", value: "dd", endValue: "", logicalOperation: "or" },
				{ column: "ee", operation: "=", value: "ee", endValue: "", logicalOperation: "or" },
				{ column: "ff", operation: "=", value: "ff", endValue: "", logicalOperation: "and" },
				{ column: "gg", operation: "between", value: "ga", endValue: "gb", logicalOperation: "and" },
				{ column: "hh", operation: "=", value: "hh", endValue: "", logicalOperation: "or" },
				{ column: "ii", operation: "=", value: "ii", endValue: "", logicalOperation: "or" },
				{ column: "jj", operation: "=", value: "jj", endValue: "", logicalOperation: "and" },
				{ column: "kk", operation: "between", value: "ka", endValue: "kb", logicalOperation: "and" },
			],
			logicalOperation: "or",
		},
		{
			clauses: [{ column: "d", operation: "=", value: "d", endValue: "", logicalOperation: null }],
			logicalOperation: "or",
		},
		{
			clauses: [{ column: "z", operation: "=", value: "z", endValue: "", logicalOperation: null }],
			logicalOperation: "or",
		},
		{
			clauses: [
				{ column: "e", operation: "=", value: "e", endValue: "", logicalOperation: null },
				{ column: "f", operation: "=", value: "f", endValue: "", logicalOperation: "and" },
			],
			logicalOperation: "and",
		},
		{
			clauses: [
				{ column: "h", operation: "=", value: "h", endValue: "", logicalOperation: null },
				{ column: "g", operation: "=", value: "g", endValue: "", logicalOperation: "and" },
			],
			logicalOperation: "and",
		},
		{
			clauses: [
				{ column: "i", operation: "=", value: "i", endValue: "", logicalOperation: null },
				{ column: "j", operation: "=", value: "j", endValue: "", logicalOperation: "and" },
			],
			logicalOperation: "and",
		},
		{
			clauses: [{ column: "k", operation: "=", value: "k", endValue: "", logicalOperation: null }],
			logicalOperation: "or",
		},
		{
			clauses: [{ column: "l", operation: "=", value: "l", endValue: "", logicalOperation: null }],
			logicalOperation: "or",
		},
		{
			clauses: [
				{ column: "m", operation: "=", value: "m", endValue: "", logicalOperation: null },
				{ column: "n", operation: "=", value: "n", endValue: "", logicalOperation: "or" },
			],
			logicalOperation: "or",
		},
		{
			clauses: [
				{ column: "o", operation: "=", value: "o", endValue: "", logicalOperation: null },
				{ column: "p", operation: "=", value: "p", endValue: "", logicalOperation: "or" },
			],
			logicalOperation: "and",
		},
		{
			clauses: [
				{ column: "q", operation: "=", value: "q", endValue: "", logicalOperation: null },
				{ column: "r", operation: "=", value: "r", endValue: "", logicalOperation: "and" },
			],
			logicalOperation: "and",
		},
		{
			clauses: [{ column: "s", operation: "=", value: "t", endValue: "", logicalOperation: null }],
			logicalOperation: "or",
		},
		{
			clauses: [{ column: "u", operation: "=", value: "u", endValue: "", logicalOperation: null }],
			logicalOperation: "or",
		},
		{
			clauses: [
				{ column: "v", operation: "=", value: "v", endValue: "", logicalOperation: null },
				{ column: "x", operation: "=", value: "x", endValue: "", logicalOperation: "and" },
			],
			logicalOperation: "and",
		},
	];
	var qq = [
		{
			clauses: [
				{ column: "a", operation: "=", value: "a", endValue: "", logicalOperation: null },
				{ column: "b", operation: "=", value: "b", endValue: "", logicalOperation: "and" },
			],
			logicalOperation: null,
		},
		{
			clauses: [{ column: "c", operation: "=", value: "c", endValue: "", logicalOperation: null }],
			logicalOperation: "or",
		},
		{
			clauses: [{ column: "d", operation: "=", value: "d", endValue: "", logicalOperation: null }],
			logicalOperation: "and",
		},
		{
			clauses: [
				{ column: "e", operation: "=", value: "e", endValue: "", logicalOperation: null },
				{ column: "f", operation: "=", value: "f", endValue: "", logicalOperation: "and" },
			],
			logicalOperation: "or",
		},
		{
			clauses: [
				{ column: "g", operation: "=", value: "g", endValue: "", logicalOperation: null },
				{ column: "h", operation: "=", value: "h", endValue: "", logicalOperation: "and" },
			],
			logicalOperation: "and",
		},
	];
	let query = "";
	for (let i = 0; i < qq.length; i++) {
		if (qq[i].logicalOperation == "or") {
			// query = query + " or ";
			query = query + " or ";
		}
		if (qq[i].logicalOperation == null || qq[i].logicalOperation == "or") {
			// console.log(subFun(qq[i].clauses));
			query = query + "(" + subFun(qq[i].clauses);
			while (i + 1 < qq.length && qq[i + 1].logicalOperation == "and") {
				i++;
				// console.log(subFun(qq[i].clauses));
				query = query + " and " + subFun(qq[i].clauses);
			}
			query = query + ")";
		}
	}
	return query;
}
let query = fn();
console.log(query);
function fnn() {
	let query =
		"(((a = 'a') and (b = 'b') and (c between 'ca' and 'cb')) or ((d = 'd')) or ((e = 'e') and (f = 'f') and (g between 'ga' and 'gb')) or ((h = 'h')) or ((i = 'i') and (j = 'j') and (k between 'ka' and 'kb'))) or (((aa = 'aa') and (bb = 'bb') and (cc between 'ca' and 'cb')) or ((dd = 'dd')) or ((ee = 'ee') and (ff = 'ff') and (gg between 'ga' and 'gb')) or ((hh = 'hh')) or ((ii = 'ii') and (jj = 'jj') and (kk between 'ka' and 'kb'))) or (((d = 'd'))) or (((z = 'z')) and ((e = 'e') and (f = 'f')) and ((h = 'h') and (g = 'g')) and ((i = 'i') and (j = 'j'))) or (((k = 'k'))) or (((l = 'l'))) or (((m = 'm')) or ((n = 'n')) and ((o = 'o')) or ((p = 'p')) and ((q = 'q') and (r = 'r'))) or (((s = 't'))) or (((u = 'u')) and ((v = 'v') and (x = 'x')))";
}

/*

(((a = 'a') and (b = 'b') and (c between 'ca' and 'cb')) or ((d = 'd')) or ((e = 'e') and (f = 'f') and (g between 'ga' and 'gb')) or ((h = 'h')) or ((i = 'i') and (j = 'j') and (k between 'ka' and 'kb'))) 
or
 (((aa = 'aa') and (bb = 'bb') and (cc between 'ca' and 'cb')) or ((dd = 'dd')) or ((ee = 'ee') and (ff = 'ff') and (gg between 'ga' and 'gb')) or ((hh = 'hh')) or ((ii = 'ii') and (jj = 'jj') and (kk between 'ka' and 'kb'))) 
or
 (((d = 'd'))) 
or
 (((z = 'z')) and ((e = 'e') and (f = 'f')) and ((h = 'h') and (g = 'g')) and ((i = 'i') and (j = 'j'))) 
or
 (((k = 'k'))) 
or
 (((l = 'l'))) 
or
 (((m = 'm')) or ((n = 'n')) and ((o = 'o')) or ((p = 'p')) and ((q = 'q') and (r = 'r'))) 
or
 (((s = 't'))) 
or
 (((u = 'u')) and ((v = 'v') and (x = 'x')))
*/
