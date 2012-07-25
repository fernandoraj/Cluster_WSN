package projects.ids_wsn.nodeDefinitions.crypto;


/**
 * A very, very, very simple cryptographic algorithm
 * @author marvin
 *
 */
public class SimpleCrypto implements ICrypto {

	public Long decrypt(Long value) {
		return value-1;
	}

	public Long encrypt(Long value) {
		return value+1;
	}

}
