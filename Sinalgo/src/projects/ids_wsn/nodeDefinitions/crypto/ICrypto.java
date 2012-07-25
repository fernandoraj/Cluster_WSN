package projects.ids_wsn.nodeDefinitions.crypto;

public interface ICrypto {
	
	public Long encrypt(Long value);
	public Long decrypt(Long value);
}
