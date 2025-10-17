var str = `
    (((a = ' a ') and (b = 'b') and (c between 'ca' and 'cb')) or ((d = 'd')) or ((e = 'e') and (f = 'f') and (g between 'ga' and 'gb')) or ((h = 'h')) or ((i = 'i') and (j = 'j') and (k between 'ka' and 'kb'))) 
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
		clauses: [{ column: "remove_it", operation: "=", value: "d", endValue: "", logicalOperation: null }],
		logicalOperation: null,
	},
	{ clauses: [{ column: "d", operation: "=", value: "d", endValue: "", logicalOperation: null }], logicalOperation: "or" },
	{
		clauses: [
			{ column: "z", operation: "=", value: "z", endValue: "", logicalOperation: null },
			{ column: "e", operation: "=", value: "e", endValue: "", logicalOperation: "and" },
			{ column: "f", operation: "=", value: "f", endValue: "", logicalOperation: "and" },
			{ column: "h", operation: "=", value: "h", endValue: "", logicalOperation: "and" },
			{ column: "g", operation: "=", value: "g", endValue: "", logicalOperation: "and" },
			{ column: "i", operation: "=", value: "i", endValue: "", logicalOperation: "and" },
			{ column: "j", operation: "=", value: "j", endValue: "", logicalOperation: "and" },
		],
		logicalOperation: "or",
	},
	{ clauses: [{ column: "k", operation: "=", value: "k", endValue: "", logicalOperation: null }], logicalOperation: "or" },
	{ clauses: [{ column: "l", operation: "=", value: "l", endValue: "", logicalOperation: null }], logicalOperation: "or" },
	{
		clauses: [
			{ column: "m", operation: "=", value: "m", endValue: "", logicalOperation: null },
			{ column: "n", operation: "=", value: "n", endValue: "", logicalOperation: "or" },
			{ column: "o", operation: "=", value: "o", endValue: "", logicalOperation: "and" },
			{ column: "p", operation: "=", value: "p", endValue: "", logicalOperation: "or" },
			{ column: "q", operation: "=", value: "q", endValue: "", logicalOperation: "and" },
			{ column: "r", operation: "=", value: "r", endValue: "", logicalOperation: "and" },
		],
		logicalOperation: "or",
	},
	{ clauses: [{ column: "s", operation: "=", value: "t", endValue: "", logicalOperation: null }], logicalOperation: "or" },
	{
		clauses: [
			{ column: "u", operation: "=", value: "u", endValue: "", logicalOperation: null },
			{ column: "v", operation: "=", value: "v", endValue: "", logicalOperation: "and" },
			{ column: "x", operation: "=", value: "x", endValue: "", logicalOperation: "and" },
		],
		logicalOperation: "or",
	},
];
