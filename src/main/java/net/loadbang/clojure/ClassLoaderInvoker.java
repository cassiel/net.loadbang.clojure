//	$Id: 6d666d951a45a6043ad8fdad26d36f0e3878e50c $
//	$Source$

package net.loadbang.clojure;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

/**	A utility package for handling the class loader environment based
 	on notional "base" directories for loading Clojure code.
 	
 	<P>This machinery is dynamic: a single thread might pass through
 	multiple MXJ objects and each one is responsible for setting its
 	"search path" for Clojure code if it is asked to do anything
 	related to that directory (such as {@link Engine#runScript} or
 	{@link Engine#runUsingPlaceHolder}).
 	
 	<P>Actually, the behaviour will be slightly surprising: we push
 	successive root directories if we call through MXJ instances,
 	so inner callees will still have access to the classpaths of
 	callers. Is that really a show-stopper? Is this face bothered?

	@author Nick Rothwell, nick@cassiel.com / nick@loadbang.net
 */

abstract public class ClassLoaderInvoker<T> {
	abstract public T invoke();
	
	public T doit(File rootDirectory) throws MalformedURLException {
		ClassLoader curr = Thread.currentThread().getContextClassLoader();
		
		URLClassLoader urlClassLoader =
			new URLClassLoader(new URL[] { rootDirectory.toURI().toURL() }, curr);
		
		Thread.currentThread().setContextClassLoader(urlClassLoader);
		
		try {
			return invoke();
		} finally {
			Thread.currentThread().setContextClassLoader(curr);
		}
	}
}
