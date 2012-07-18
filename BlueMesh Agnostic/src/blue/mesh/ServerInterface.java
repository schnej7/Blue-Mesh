package blue.mesh;

public interface ServerInterface {
	public abstract BluetoothObject[] ReturnObjects();
	//ReturnObject must return a number of correct instances of BluetoothObject once opened.
	//It may be zero, one, or many objects. It may contain null pointers.
	//It does not have to maintain order. It must be able to be called multiple times.
	
	//Should it return a BluetoothObject or an array of BluetoothObjects?
	//If it's one, how does one make sure that it doesn't return the same one over and over?
	//If it's all of them, what's the point of having a thread or a RouterObject?
}
