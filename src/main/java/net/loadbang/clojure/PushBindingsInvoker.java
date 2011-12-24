//	$Id: dc16713b0892864781518e678cd44dbc7f0acf1c $
//	$Source$

package net.loadbang.clojure;

import java.io.OutputStreamWriter;
import java.io.Reader;

import net.loadbang.scripting.MaxObjectProxy;
import clojure.lang.LineNumberingPushbackReader;
import clojure.lang.Namespace;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Var;

/**	A helper class for invoking operations having pushed some
 	common bindings.
 
	@author Nick Rothwell, nick@cassiel.com / nick@loadbang.net

	@param <T> the type of value returned by the invocation
 */

abstract public class PushBindingsInvoker<T> {
	abstract public T invoke() throws Exception;
	
	static final Var IN_NS = RT.var("clojure.core", "in-ns");
	static final Namespace MAX_NS = Namespace.findOrCreate(Symbol.intern("max"));
	static final Var MAX_OBJECT = Var.intern(MAX_NS, Symbol.intern("maxObject"), null);
	static final Symbol CLOJURE_CORE = Symbol.intern("clojure.core");
	static final Symbol USER = Symbol.intern("user");
	
	private Reader itsReader;
	private MaxObjectProxy itsProxy;
	private NSOwner itsNSOwner;
	private static NamespaceTracker theNamespaceTracker = new NamespaceTracker();

	static {
		theNamespaceTracker.firstEncounter(USER);
			// USER has clojure.core referred already, so mark it as done.
	}

	public PushBindingsInvoker(MaxObjectProxy proxy,
							   NSOwner nsOwner,
							   Reader reader
							  ) {
		itsProxy = proxy;
		itsNSOwner = nsOwner;
		itsReader = reader;
	}

	public T doit() throws Exception {
		Var.pushThreadBindings(
			RT.map(MAX_OBJECT, itsProxy,
					RT.CURRENT_NS, RT.CURRENT_NS.deref(),
					RT.IN, new LineNumberingPushbackReader(itsReader),
					RT.OUT, new OutputStreamWriter(System.out),
					RT.ERR, new OutputStreamWriter(System.err))
		);

		Symbol sym = Symbol.intern(itsNSOwner.getNS());
		IN_NS.invoke(sym);
		
		if (theNamespaceTracker.firstEncounter(sym)) {
			System.out.println("First encounter: " + sym);
			Var refer = RT.var("clojure.core", "refer");
			refer.invoke(CLOJURE_CORE);
		}

		try {
			return invoke();
		} finally {
			Var.popThreadBindings();
		}
	}
	
	public Reader getReader() {
		return itsReader;
	}
}
