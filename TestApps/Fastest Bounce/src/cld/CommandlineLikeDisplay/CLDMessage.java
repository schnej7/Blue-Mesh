package cld.CommandlineLikeDisplay;

import java.util.LinkedList;
import java.util.Queue;

import android.os.Handler;
import android.util.Log;

public class CLDMessage {
	
	private static final String TAG = "CLDMessage";
	private static Handler myHandler;
	private static Queue <String> input_queue;
	
	//Constructor
	CLDMessage(Handler a_Handler){
		myHandler = a_Handler;
		input_queue = new LinkedList <String>();
	}
	
	public synchronized void clearQueue(){
		input_queue.clear();
	}

	//Get a line of text from the input
	public synchronized void getLineFromInput(String input){
		input_queue.add(input);
		this.notifyAll();
	}
	
	//GetLine is a blocking call, we notify so it will wake up
	//from sleep
	public synchronized void notifyGetLine()
	{
		this.notifyAll();
	}
	
	//Return the next element of the input queue
	//or wait for user input
	public String getLine(){
		while(input_queue.size() == 0){
			synchronized(this){
				try {
					wait();
				} catch (InterruptedException e) {
					return null;
				}
			}
		}
		return input_queue.remove();
	}
	
	//Print a debug message
	public void print_debug(String outString) {
		String debugString = "(Debug) " + outString;
		byte[] buffer = debugString.getBytes();

		myHandler.obtainMessage(Constants.MSG_DEBUG, buffer.length, -1, buffer)
				.sendToTarget();
	}

	//Print a normal message
	public void print_normal(String outString) {
		byte[] buffer = outString.getBytes();

		myHandler.obtainMessage(Constants.MSG_NORMAL, buffer.length, -1, buffer)
				.sendToTarget();
	}
}
