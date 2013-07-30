package org.lambdaroyal.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import junit.framework.Assert;

import org.junit.Test;

/**
 * Testet {@link ObjectBlobConverter} 
 * @author Christian Meichsner
 *
 */
public class ObjectBlobConverterTest {
	private static final ArrayList<Integer> LIST = new ArrayList<Integer>(Arrays.asList(1,2,3,4,5,6,7,8,9,10));
	

	/**
	 * Testet Umwandlung von Object in bytearray und zurück
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 */
	@Test	
	public void o2b2o() throws IOException, ClassNotFoundException {
		byte[] blob = ObjectBlobConverter.o2b(LIST);
		Assert.assertNotNull(blob);
		@SuppressWarnings("unchecked")
		ArrayList<Integer> demarshalled = (ArrayList<Integer>) ObjectBlobConverter.b2o(blob);
		assertEqualContent(demarshalled);
	}
	
	@Test
	public void nullToObject() throws IOException, ClassNotFoundException {
		Assert.assertNull(ObjectBlobConverter.b2o((byte[]) null));
	}

	/**
	 * prüft ob das übergebene objekt der statischen LISTE entspricht
	 * @param demarshalled
	 */
	private void assertEqualContent(ArrayList<Integer> demarshalled) {
		Assert.assertNotNull(demarshalled);
		Assert.assertNotSame(LIST,  demarshalled);
		
		//Inhalt prüfen
		HashSet<Integer> set = new HashSet<Integer>(demarshalled);
		Assert.assertEquals(LIST.size(), set.size());
		for(int i : LIST) {
			Assert.assertTrue(set.contains(i));
		}
	}
	
	/**
	 * tested umwandlung eines objectes in einen bytestream
	 * @throws IOException
	 * @throws ClassNotFoundException 
	 */
	@Test
	public void o2is() throws IOException, ClassNotFoundException {
		ObjectInputStream ois = ObjectBlobConverter.o2is(LIST);
		@SuppressWarnings("unchecked")
		ArrayList<Integer> demarshalled = (ArrayList<Integer>) ois.readObject();
		assertEqualContent(demarshalled);
	}

}
