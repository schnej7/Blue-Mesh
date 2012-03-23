package cld.CommandlineLikeDisplay;

import android.util.Log;
import blue.mesh.BlueMeshService;

public class FastestBounce {
	
	private static final String TAG = "FastestBounce";
	private CLDMessage myCLDMessage;
	private BlueMeshService bms;
	
	//example service constructor
	public FastestBounce( CLDMessage a_CLDMessage ){
		///////////////////////////////////////////////
		//TODO
		//
		//Create a CLDMessage with the parameter as CLDMessage
		//which will be your object for io
		///////////////////////////////////////////////	
		myCLDMessage = a_CLDMessage;
	}
	
	//////////////////////////////////////////////////////////
	//Example Functions
	//////////////////////////////////////////////////////////
	//Example that using CLDMessage is thread safe :)
	/*
	public void start(){
		myCLDMessage.print_normal("Starting testThread!");
		testThread a_testThread = new testThread();
		a_testThread.start();
		myCLDMessage.print_normal("It's thread safe!");
	}
	
	class testThread extends Thread{
		public void run(){
			myCLDMessage.print_normal("What is your name?");
			String in = myCLDMessage.getLine();
			myCLDMessage.print_normal("Your name is " + in); //what?!
		    // chica chica chica (slim shady!)
		}
	}
	*/
	public void stop(){
		bms.disconnect();
		myCLDMessage.notifyGetLine();
	}
	
	public void start(){
		myCLDMessage.print_normal("Starting BlueMesh");
		try{
			bms = new BlueMeshService();
		}
		catch(NullPointerException e){
			Log.e(TAG, "BlueMeshService Constructor failed");
			myCLDMessage.print_normal("Bluetooth not enabled");
			return;
		}
		myCLDMessage.print_normal("Launching BlueMesh");
		bms.launch();
		
		while (true){
			myCLDMessage.print_normal("Choose Operation (g)et or (t)hrow");
			String input = myCLDMessage.getLine();
			if (input == "t"){
				input = "herp"; //Arbitrary send string
				for(int i=0; i<10; i++){
					long StartTime = System.nanoTime();
					bms.write(input.getBytes());
					while (true){
						byte bytes[] = bms.pull();
						if (bytes != null){
							long EndTime = System.nanoTime();
							String output = String.valueOf(EndTime-StartTime);
							myCLDMessage.print_normal(output);
						}
					}
				}
			}
			else if (input == "g"){
				input = "derp";
				int i=0;
				while (i<10){
					byte bytes[] = bms.pull();
					if( bytes != null){
						bms.write(input.getBytes());
						i++;
					}
				}
			}
			else if (input == "quit") break;
			//Include a "press any key to exit"?	
		}
		
		this.stop();
		return;
	}
	//////////////////////////////////////////////////////////

}