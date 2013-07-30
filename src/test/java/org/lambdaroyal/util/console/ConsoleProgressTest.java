/**
 * 
 */
package org.lambdaroyal.util.console; 

import org.junit.Test;
import org.lambdaroyal.util.ConsoleProgress;

/**
 * @see org.lambdaroyal.util.ConsoleProgress
 * @author Christian Meichsner
 *
 */
public class ConsoleProgressTest {

	/**
	 * Tested die verschiedenen Updateszenarien für den Progressanzeiger
	 */
	@Test
	public void test() {
		ConsoleProgress progress = new ConsoleProgress();
		progress.showProgress("task1", 0);
		progress.showProgress("task2", 0);
		progress.showProgress("task12",11);
		progress.showProgress("task12", 100);
		
		//wird nicht mehr angezeigt
		progress.showProgress("task12", 110); 
		progress.showProgress("task13", 110);
	}
	
	/**
	 * Für Sonar :)
	 * @throws InterruptedException 
	 */
	@Test	
	public void testMain() throws InterruptedException {
		ConsoleProgress.main(null);
	}

}
