/*
    interface:
        function1(), function2()
    class
        function1(){}, function2(){}
    abstract class
        function1(){}, function2()
*/
/*

switch(expression) {
    case x:
        // code block
    case y:
        // code block
    default:
        // code block
}

logger.log()

class Logger {
    function log(level){
        if level == curLevel{
            log it
        } else{
            successor.log(level)
        }
    } 
}


class infoLogger:Logger {
    Level curLevel
    Logger successor = warningLogger
}
warning
error
fatal


Logger{

log(level){
    switch(level){
        case INFO:
            // log info
        case WARNING:
            // log warning
        case ERROR:
            // log error
        case FATAL:
            // log fatal
    }
}



*/

/*

Class DBConnection {
	String str = “lock”;
	private static dbConnection: DBConnection;
	private DBConnection(){}
	
	public getDBConnection() { // t1 t2
		// T1  T2

		if(!this.dbConnection) { // t1 t2
			// t2 
			lock(str);
			if(!this.dbConnection){
		this.dbConnection = new DBConnection();
}
	release(str); //t1
}

return this.dbConnection;
}
}


Class connection{
	DBConnection dbConnection = DBConnection.getDBConnection();
}


*/