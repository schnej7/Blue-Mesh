package blueMesh.display;

import java.util.Iterator;
import java.util.List;
import java.util.zip.CRC32;
import java.util.Collections;

//Class used to calculate CRCs over a list of strings
public class CrcCalculations {

	static long CalcCRC( List < String > deviceIDs ){
		Collections.sort(deviceIDs);
		
		CRC32 idCRC = new CRC32();
		idCRC.reset();
		
		Iterator <String> i = deviceIDs.iterator();
		
		int bytesRead = 0;
		
		while( i.hasNext()){
			byte[] idBytes = i.next().getBytes();
			idCRC.update(idBytes, bytesRead, idBytes.length);
			bytesRead += idBytes.length;
		}
		
		return idCRC.getValue();
	}
}
