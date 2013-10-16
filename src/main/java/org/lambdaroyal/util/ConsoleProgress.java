package org.lambdaroyal.util;

/**
 * stellt eine progressbar in der console dar. sinnvoll für langlaufende tasks
 * in consolenanwendung.
 * 
 * @author Christian Meichsner
 * 
 */
public final class ConsoleProgress {
	
	/**
	 * gibt an das der progress schon mal bei 100 war - keine weiteren
	 * zeilenumbrüche
	 */
	private volatile boolean finished = false;
	
	/**
	 * enthält zuletzt dargestellte prozent - wird benötigt um zu prüfen, ob die darstellung aktualisiert werden muss
	 */
	private volatile int currentProgress;
	
	/**
	 * enthält zuletzt dargestellte task - wird benötigt um zu prüfen, ob die darstellung aktualisiert werden muss
	 */
	private volatile String currentTask = "";

	/**
	 * whole progress indication (bar/percentage/task)
	 */
	private volatile String currentString = "";
	
	private volatile long start;
	
        private final char progressIndicator;

        public ConsoleProgress(){
            progressIndicator = (char) 178;
        }

        public ConsoleProgress(char progressIndicator) {
            this.progressIndicator = progressIndicator;
        }

	/**
	 * 
	 * @param task
	 *            description
	 * @param progress
	 *            in percent
	 */
	public void showProgress(String task, int progress) {
		if (!finished) {
			// global gequeued
			synchronized (ConsoleProgress.class) {
				if(start == 0) {
					start = System.currentTimeMillis();
				}
				
				//check whether something changed
				if(currentTask.equals(task) && currentProgress == progress || progress > 100) {
					return;
				}
				// jump back
				System.out.print('\r');

				//we delete everything when the task changes since carriage return is always to the full extend
				if(!currentTask.equals(task)) {
					StringBuilder b = new StringBuilder(); 
					for(int i = 0; i < currentString.length(); i++) {
						b.append(' ');
					}
					System.out.print(b.toString());
					System.out.print('\r');										
				}

				//calculate output up to percentage
				StringBuilder b = new StringBuilder();
				b.append(" [");
				int i = 0;
				for (; i < Math.floor(progress / 10.0); i++) {
					b.append(progressIndicator);
				}
				for (; i < 10; i++) {
					b.append(' ');
				}
				b.append("] ").append(String.format("%3d", progress)).append("% ");

				
				//check whether we just have to update percentage, progress rule is due to necessity of printing time taken by the process after the task
				if(currentProgress != progress || !currentTask.equals(task) || progress >= 100) {
					System.out.print(b.toString());		
					currentProgress = progress;
				}
				//check whether we just have to update task, progress rule is due to necessity of printing time taken by the process after the task
				if(!currentTask.equals(task) || progress >= 100) {
					System.out.print(task);
					currentTask = task;
				}
				b.append(task);				
				currentString = b.toString();
				
				if (progress >= 100) {
					finished = true;
					System.out.println(String.format(" %dms", (System.currentTimeMillis() - start)));
				}
			}
		}
	}

	public static void main(String[] args) throws InterruptedException {
		ConsoleProgress cp = new ConsoleProgress();
		for(int i = 0; i < 1000; i++) {
			cp.showProgress("Lade Pfade vsch20130318.csv" + i / 100, (int) Math.ceil(i / 10.0));
			Thread.sleep(5);
		}
		Thread.sleep(2000);
	}
}
