package org.lambdaroyal.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Blob;
import java.sql.SQLException;

/**
 * stateless converter from pojo to rdbms space
 * @author Christian Meichsner
 *
 */
public final class ObjectBlobConverter {
	static final int BUFFER_SIZE = 2 << 9;
	
	/**
	 * Codecoverage++
	 */
	static {
		new ObjectBlobConverter();
	}

	/**
	 * Instanz ist Zustandsfrei
	 */
	private ObjectBlobConverter() {
		
	}	
	
	/**
	 * Object -> Blob Umwandlung
	 */
	public static byte[] o2b(Object o) throws IOException {
			ByteArrayOutputStream bos = new ByteArrayOutputStream(BUFFER_SIZE);
			try(ObjectOutputStream oos = new ObjectOutputStream(bos)) {
				oos.writeObject(o);
				return bos.toByteArray();
			}
		
	}
	
	/**
	 * Gibt einen Inputstream zurück, aus welchem ein Object deserialisiert werden kann 
	 */
	public static ObjectInputStream o2is(Object o) throws IOException {
		ByteArrayInputStream bis = new ByteArrayInputStream(o2b(o));
		return new ObjectInputStream(bis);		
	}
	
	/**
	 * Byte Array -> Object Umwandlung
	 */
	public static Object b2o(byte[] o) throws IOException, ClassNotFoundException {
		if(o == null) {
			return null;
		} else {
			ByteArrayInputStream bis = new ByteArrayInputStream(o);
			try (ObjectInputStream ois = new ObjectInputStream(bis)){
				return ois.readObject();
			}
		}
	}
	
	/**
	 * Blob -> Object Umwandlung
	 */
	public static Object b2o(Blob o) throws IOException, ClassNotFoundException, SQLException {
		if(o == null) {
			return null;
		} else {
			try (ObjectInputStream ois = new ObjectInputStream(o.getBinaryStream())){
				return ois.readObject();
			}
		}
	}
	
}
