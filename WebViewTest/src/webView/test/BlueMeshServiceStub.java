package webView.test;

public class BlueMeshServiceStub{

	public BlueMeshServiceStub() {
		
	}
	
	public byte[] pull(){
		return "Pulled from BlueMesh".getBytes();	
	}

}
