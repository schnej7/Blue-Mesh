package blue.mesh;

public abstract class Message {
	private String m_destination;
	private Object m_data;
	private Class<? extends Object>  m_dataType;
	private int m_id;
	
	public String getDestination(){
		return m_destination;
	}
	
	public void setDestination(String a_destination){
		m_destination = a_destination;
	}
	
	public Object getData(){
		return m_data;
	}
	
	public void setData(Object a_data){
		m_data = a_data;
		m_dataType = a_data.getClass();
	}
	
	public Class<? extends Object> getDataType(){
		return m_dataType;
	}
	
	protected void setId(int a_id){
		m_id = a_id;
	}
	
	protected int getId(){
		return m_id;
	}
}
