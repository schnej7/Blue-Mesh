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
			byte[] buffer = outString.getBytes();
	
			mHandler.obtainMessage(Constants.MSG_DEBUG, buffer.length, -1, buffer)
					.sendToTarget();
	
			return;
		}
	}
	
}
