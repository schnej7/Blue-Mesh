package blueMesh.display;

import android.os.Handler;

/**
 * Used to send messages to the user
 * @author schnej7
 *
 */
public class MessageSenderThread extends Thread{

	Handler mHandler;
	String outString;
	int mType;
	MessageSenderThread(
			Handler aHandler, 
			String aString,
			int aMessageType){
		
		mHandler = aHandler;
		outString = aString;
		mType = aMessageType;
	}
	
	public void run(){

		if (mType != Constants.MSG_DEBUG || Constants.DEBUG){
			// Create buffer for string to be converted to bytes to be
			// displayed by the UI thread
			byte[] buffer = new byte[1024];
			int bytes;
	
			buffer = outString.getBytes();
	
			// Check size of input string
			if (buffer.length > 1024)
				return;
	
			bytes = buffer.length;
			mHandler.obtainMessage(Constants.MSG_DEBUG, bytes, -1, buffer)
					.sendToTarget();
	
			return;
		}
	}
	
}
