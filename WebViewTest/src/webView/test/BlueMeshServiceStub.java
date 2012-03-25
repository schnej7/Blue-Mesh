package webView.test;

import android.content.Context;

public class BlueMeshServiceStub{

	public BlueMeshServiceStub(Context a_context) {
		
	}
	
	public byte[] pull(){
		return "Pulled from BlueMesh".getBytes();	
	}

}
