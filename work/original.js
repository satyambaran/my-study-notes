var str = `
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
    (((m = 'm')) or ((n = 'n')) and ((o = 'o')) or ((p = 'p')) and ((q = 'q') and (r = 'r'))) or 
    (((s = 't'))) 
    or 
    (((u = 'u')) and ((v = 'v') and (x = 'x')))
`;
var conditions = [
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
