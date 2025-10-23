var con = [
	{
		clauses: [
			{ column: "a", operation: "=", value: " a     ", endValue: "", logicalOperation: null },
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
	// null c1 or c2 or c3 and c4 and c5 and c6 or c7 or c8 or c9 and c10 and c11 or c12 or c13 and c14
	// null c1 or c2 or (c3 and c4 and c5 and c6) or c7 or c8 or (c9 and c10 and c11) or c12 or (c13 and c14)
	{
		clauses: [{ column: "remove_it", operation: "=", value: "d", endValue: "", logicalOperation: null }],
		logicalOperation: null,
	},
	{
		clauses: [{ column: "d", operation: "=", value: "d", endValue: "", logicalOperation: null }],
		logicalOperation: "or",
	},
	{
		clauses: [{ column: "z", operation: "=", value: "z", endValue: "", logicalOperation: null }],
		logicalOperation: "or",
	},
	/*
		(((remove it = 'd'))) 
		or  (((d = 'd'))) 
		or  (((z = 'z')))
		and (((e = 'e') and (f = 'f'))) 
		and (((h = 'h') and (g = 'g'))) 
		and (((i = 'i') and (j = 'j'))) 
		or  (((k = 'k'))) or (((l = 'l'))) 
		or  (((m = 'm')) or ((n = 'n'))) 
		and (((o = 'o')) or ((p = 'p')))
		and (((q = 'q') and (r = 'r'))) 
		or  (((s = 't'))) 
		or  (((u = 'u')) 
		and (((v = 'v') and (x = 'x'))) 


		(((remove it = 'd'))) 
		or  (((d = 'd'))) 
		or  ( 	(((z = 'z')))
		and (((e = 'e') and (f = 'f'))) 
		and (((h = 'h') and (g = 'g'))) 
		and (((i = 'i') and (j = 'j'))) 	)
		or  (((k = 'k'))) or (((l = 'l'))) 
		or  ( 	(((m = 'm')) or ((n = 'n'))) 
		and (((o = 'o')) or ((p = 'p')))
		and (((q = 'q') and (r = 'r'))) 	)
		or  (((s = 't'))) 
		or  (	(((u = 'u')) 
		and (((v = 'v') and (x = 'x')))		)
	
	*/
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
console.log(con.length)
function clauseToString(clause) {
	let query = "";
	if (clause.logicalOperation == "and") {
		query = query + " and ";
	}
	query = query + "(" + clause.column + " " + clause.operation + " ";
	if (clause.operation == "=") {
		query = query + `'${clause.value}'`;
	} else if (clause.operation == "between") {
		query = query + `'${clause.value}'` + " and " + `'${clause.endValue}'`;
	}
	query = query + ")";
	return query;
}
function conditionToString(clauses) {
	let query = "";
	for (let i = 0; i < clauses.length; i++) {
		if (clauses[i].logicalOperation == "or") {
			query = query + " or ";
		}
		if (clauses[i].logicalOperation == null || clauses[i].logicalOperation == "or") {
			query = query + "(" + clauseToString(clauses[i]);
			while (i + 1 < clauses.length && clauses[i + 1].logicalOperation == "and") {
				i++;
				query = query + clauseToString(clauses[i]);
			}
			query = query + ")";
		}
	}
	return query;
}
function getQuery(conditions) {
	let query = "";
	for (let i = 0; i < conditions.length; i++) {
		if (conditions[i].logicalOperation == "or") {
			query = query + " or ";
		}
		if (conditions[i].logicalOperation == null || conditions[i].logicalOperation == "or") {
			query = query + "(" + conditionToString(conditions[i].clauses);
			while (i + 1 < conditions.length && conditions[i + 1].logicalOperation == "and") {
				i++;
				query = query + " and " + conditionToString(conditions[i].clauses);
			}
			query = query + ")";
		}
	}
	return query;
}
function parseClause(str) {
	const clause = {
		column: "",
		operation: "",
		value: "",
		endValue: "",
		logicalOperation: null,
	};
	let match = str.match(/(\w+)\s*([=]|between)\s*'([^']*)'(?:\s*and\s*'([^']*)')?/);
	if (match) {
		clause.column = match[1];
		clause.operation = match[2];
		clause.value = match[3];
		if (match[2] === "between") {
			clause.endValue = match[4];
		}
	}
	return clause;
}
function parseString() {
	query = query.replace(/\s+/g, " ").trim();
	// console.log(query);
	// Define the regex patterns
	let clausePattern = /\((\w+\s*[=]\s*'[^']*'|\w+\s*between\s*'[^']*'\s*and\s*'[^']*')\)/g;
	let logicalOpPattern = /\b(and|or)\b/g;
	let conditions = [];
	let clauseList = { clauses: [], logicalOperation: null };
	let clauseMatch = clausePattern.exec(query),
		logicalOpMatch,
		clauseObj;
	let lastLogicalOp = null;
	let lastClausePos = 0;
	while (clauseMatch !== null) {
		// console.log(clauseMatch);
		clauseObj = parseClause(clauseMatch[1]);
		clauseObj.logicalOperation = lastLogicalOp;
		lastClausePos = clausePattern.lastIndex - 1;
		clauseList.clauses.push(clauseObj);
		logicalOpPattern.lastIndex = clausePattern.lastIndex;
		logicalOpMatch = logicalOpPattern.exec(query);
		lastLogicalOp = logicalOpMatch?.[1] ?? null;
		if (query.substring(lastClausePos, lastClausePos + 3) === ")))") {
			conditions.push(clauseList);
			clauseList = { clauses: [], logicalOperation: lastLogicalOp };
			lastLogicalOp = null;
		}
		clauseMatch = clausePattern.exec(query);
	}

	return conditions;
}
var query = getQuery(con);
console.log(query, "\n\n");
var con2 = parseString(query);
console.log(JSON.stringify(con2));
console.log("\n\n");
var query2 = getQuery(con2);
var con3 = parseString(query2);
var query3 = getQuery(con3);
console.log(query, "\n\n", query2, "\n\n", query3, query == query2, query2 == query3);
console.log(
	JSON.stringify(con),
	"\n\n",
	JSON.stringify(con2),
	"\n\n",
	JSON.stringify(con3),
	JSON.stringify(con) == JSON.stringify(con2),
	JSON.stringify(con2) == JSON.stringify(con3)
);
