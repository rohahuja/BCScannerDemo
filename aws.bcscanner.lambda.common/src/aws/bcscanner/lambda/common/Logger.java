/**
 * 
 */
package aws.bcscanner.lambda.common;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;

/**
 * Logger class that will enable logging extensibility. 
 * Also eliminates context issues when debugging locally.
 */
public class Logger {
	private static LambdaLogger logger;
	
	public Logger(Context context){
		if (context != null) {
			logger = context.getLogger();
		}
	}
	
	public void write(String message) {
		if (logger == null) {
			return;
		}
		
		logger.log(message);
	}
}
