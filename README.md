clojure-util
============

General helpful aspects for clojure projects (caching, I/O, ...)

##ConsoleProgress##

tired of logging the progress in consecutive lines wasting console space. do you want to implementing logging schemes implying only small I/O load. This class gives you a progress bar in the console that is only updated when necessary and does a time tracing as well. Line feed is done after the progress went to 100 percent.

* Clojure example

<pre>
(import '[org.lambdaroyal.util ConsoleProgress])

(def p (ConsoleProgress. \-))

(loop [i 0]
    (if
        (> i 100) nil
        (do
            (.showProgress p "I'm waitin" i)
            (Thread/sleep 5)
            (recur (inc i)))))
</pre>

gives you (in the end)

 [----------] 100% I'm waitin 579ms

* Java Example

<pre>
ConsoleProgress progress = new org.lambdaroyal.util.ConsoleProgress("*");
progress.showProgress("task1", 0);
progress.showProgress("task2", 0);
progress.showProgress("task12",11);
progress.showProgress("task12", 100);
</pre>

