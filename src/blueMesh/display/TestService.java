package blueMesh.display;

import android.content.Context;
import android.os.Handler;

public class TestService{
	
	private final Handler mHandler;
	
	
	public TestService (Context context, Handler handler){
		mHandler = handler;
	}
	
	public synchronized void start(){
		messageSender mSender = new messageSender();
		mSender.start();		
	}
	
	
	//Class to send messages to be displayed
	private class messageSender extends Thread {
		
		public void run(){
			count();
		}
		
		public void count(){
			byte[] buffer = new byte[1024];
			int bytes;
			
			for(int i = 0; i < 10; i++){
				String sendString = "From Service " + String.valueOf(i);
				buffer = sendString.getBytes();
				bytes = buffer.length;
				mHandler.obtainMessage(BlueMeshDisplayActivity.MSG_DEBUG,
						bytes, -1, buffer).sendToTarget();
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	

}
