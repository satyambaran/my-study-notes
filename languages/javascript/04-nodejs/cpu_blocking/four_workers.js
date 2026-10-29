const THREAD_COUNT = 4;

function createWorker() {
	return new Promise(function (resolve, reject) {
		const worker = new Worker("./four_workers.js", {
			workerData: { thread_count: THREAD_COUNT },
		});
	});
}
module.exports = {
	createWorker,
};
