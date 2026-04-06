const { spawn } = require("child_process");
const child = spawn("node", ["child.js"]);

child.stdout.on("data", (data) => {
	console.log(`Child process stdout: ${data}`);
});

child.on("close", (code) => {
	console.log(`Child process exited with code ${code}`);
});
