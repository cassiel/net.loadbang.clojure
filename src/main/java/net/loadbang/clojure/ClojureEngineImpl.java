//	$Id: 520becb867943ef9278fab412a4a006c89ae7ee5 $
//	$Source$

package net.loadbang.clojure;

import java.io.File;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Stack;

import net.loadbang.scripting.EngineImpl;
import net.loadbang.scripting.MaxObjectProxy;
import net.loadbang.scripting.util.Converters;
import clojure.lang.Compiler;
import clojure.lang.IFn;
import clojure.lang.Namespace;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Var;

import com.cycling74.max.Atom;
import com.cycling74.max.MaxObject;

public class ClojureEngineImpl extends EngineImpl {
	/**	The directory for any place-holder. */
	private File itsPlaceHolderDirectory00;
	private NSOwner itsNSOwner;
	
	private ClojureConverters itsConverters;
	
	/**	A stack of callback functions to call on disposal or "clear". */
	private Stack<IFn> itsCleanupCallbacks00 = null;

	/**	Create an instance of a Clojure engine.

		@param proxy a proxy for the owning {@link MaxObject}.
	 */

	public ClojureEngineImpl(MaxObjectProxy proxy, NSOwner nsOwner) {
		super(proxy);
		itsNSOwner = nsOwner;
		itsConverters = new ClojureConverters();
		clear();
	}

	/**	Clear the environment. Since we only have one Clojure
	 	interpreter it's not clear that this means anything,
	 	but we want to clear any on-delete functions.

	 	@see net.loadbang.scripting.Engine#clear()
	 */
	
	@Override
	public void clear() {
		unwindCallbacks();
	}
	
	/**	Run a script file in a given directory. The directory is
	 	(temporarily) made the root of a classpath entry so that
	 	libraries can be found relative to the directory using
	 	'require and friends.
	 	
	 	<P>We also set up some bindings (including max/maxObject)
	 	for the invocation.
	 	
		@param directory the directory containing the file, used
			as a classpath root
			
		@param filename the name of the script file in that
			directory to run
	 */

	private void run1(final File directory, final String filename) {
		final ClojureEngineImpl x = this;

		try {
			//	Add directory to classpath and go:
			new ClassLoaderInvoker<Object>() {
				private void bindAndGo() throws Exception {
					try {
						//	Push bindings and go:
						new PushBindingsInvoker<Object>(x, getProxy(), itsNSOwner,
														new StringReader("")
								  					   ) {
							@Override
							public Object invoke() {
								try {
									Compiler.loadFile(new File(directory, filename)
											  		  .getCanonicalPath()
											 		 );
								} catch (Exception exn) {
									getProxy().error(exn.getMessage());
									exn.printStackTrace();
								}
								return null;
							}
						}.doit();
					} catch (Exception exn) {
						getProxy().error(exn.getMessage());
						exn.printStackTrace();
					}
				}

				@Override
				public Object invoke() {
					try {
						bindAndGo();
					} catch (Exception exn) {
						getProxy().error(exn.getMessage());
						exn.printStackTrace();
					}
					
					return null;
				}
			}.doit(directory);
		} catch (MalformedURLException exn) {
			getProxy().error(exn.getMessage());
			exn.printStackTrace();
		}
	}

	/**	Run a script from a file in a given directory,
	 	the latter provided for a search path. This
	 	search path should be considered transient;
	 	the "real" search path is set by the place-holder.
	 	
	 	@see net.loadbang.scripting.Engine#runScript(java.lang.String, java.lang.String)
	 */

	@Override
	public void runScript(final String directory, final String filename) {
		try {
			run1(new File(directory), filename);
		} catch (Exception exn) {
			getProxy().error(exn.getMessage());
			exn.printStackTrace();
		}
	}

	/**	Run a script file with a path rooted on the
	 	current place-holder.
	 	
	 	@see net.loadbang.scripting.Engine#runUsingPlaceHolder(java.lang.String)
	 */

	@Override
	public void runUsingPlaceHolder(String name) {
		if (itsPlaceHolderDirectory00 == null) {
			getProxy().error("engine not loaded: place-holder problem?");
		} else {
			run1(itsPlaceHolderDirectory00, name);
		}
	}

	/**	Add (stack) a cleanup function.

	 	@see net.loadbang.scripting.Engine#addCleanup(java.lang.Object)
	 */

	@Override
	public void addCleanup(Object obj) {
		if (obj instanceof IFn) {
			if (itsCleanupCallbacks00 == null) {
				itsCleanupCallbacks00 = new Stack<IFn>();
			}

			itsCleanupCallbacks00.push((IFn) obj);
		} else {
			getProxy().error("addCleanup: not a function");
		}
	}

	/**	Data converters. Generally universal, but (probably) with
	  	some Clojure-specific routines.
	  	
	 	@see net.loadbang.scripting.EngineImpl#getConverters()
	 */

	@Override
	protected Converters getConverters() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setupEngineOnPlaceHolder(String directory) {
		itsPlaceHolderDirectory00 = new File(directory);
	}

	@Override
	public void setVar(String id, Object args) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected Object getVar00(String id) {
		// TODO Auto-generated method stub
		return null;
	}
	
	/**	Raw evaluation. We could establish the classpath for the
	 	current root before running this, but the encumbent
	 	Groovy and Python systems don't do this.
	 	
		@param statement the Clojure statement to evaluate
		@return the result of the evaluation
		@throws Exception if evaluation throws an exception
	 */

	private Object evaluate(String statement) throws Exception {
		return new PushBindingsInvoker<Object>(this, getProxy(), itsNSOwner,
											   new StringReader(statement)
											  ) {
			@Override
			public Object invoke() throws Exception {
				return Compiler.load(getReader());
			}
		}.doit();
	}

	@Override
	public void exec(String statement) {
		try {
			/*ignore*/ evaluate(statement);
		} catch (Exception exn) {
			getProxy().error(exn.toString());
		}
	}

	@Override
	public void eval(String statement) {
		try {
			Object obj00 = evaluate(statement);
			//System.out.println(obj00.getClass());
			getProxy().outlet(0, obj00);
		} catch (Exception exn) {
			getProxy().error(exn.toString());
		}
	}
	
	private Var retrieveFnAsVar(String fn) throws Exception {
		String format = "Function %s not found in namespace %s";
		String ns, rawFn;

		if (fn.indexOf('/') == -1) {
			ns = itsNSOwner.getNS();
			rawFn = fn;
		} else {
			String[] names = fn.split("/");
			ns = names[0];
			rawFn = names[1];
		}
	
		//Var var = RT.var(ns, rawFn);
		Namespace namespace = Namespace.find(Symbol.intern(ns));
		
		if (namespace == null) {
			throw new IllegalArgumentException("No such namespace: " + ns);
		}
		
		Var var = namespace.findInternedVar(Symbol.intern(rawFn));

		if (var == null) {
			String msg = String.format(format, rawFn, ns);
			throw new NoSuchMethodException(msg);
		} else {
			return var;
		}
	}

	/** Invoke a function followed by a set of arguments (Atoms).
	 	We frig this slightly to allow a special form: if the "fn" begins
	 	with "(", "@" or "[" we assume the entire input is a Clojure form and do
	 	an eval(), ignoring the inlet number. (This will be slow, so
	 	should be avoided: try to drop into the applyTo() instead.)
	 	
	 	<P>While "run" and "script" temporarily extend the classpath
	 	in order to load libraries (using the place-holder and the location
	 	of the script respectively), we don't do that here, partly due to
	 	a possibly misconceived notion of performance: we want one-liners
	 	to be fast (especially the Max-style unbracketted ones).
	 	
	 	TODO: for function names we should pick out ":xxxx" (but only
	 	if with args: I don't know what we should do with an isolated
	 	":xxxx"), and possibly "#'name". For arguments we should
	 	pick out and convert these as well.

	 	@see net.loadbang.scripting.EngineImpl#invoke(java.lang.String, java.lang.Integer, com.cycling74.max.Atom[])
	 */

	@Override
	public void invoke(String fn, Integer inlet00, Atom[] args) {
		//	Pick common initial characters meaning "not a function".
		if (fn.startsWith("(") || fn.startsWith("[") || fn.startsWith("@")) {
			eval(fn + " " + Atom.toOneString(args));
		} else {		//	Complicated: function invocation. Args are int/float/string,
						//	but we should identify :foo and 'foo (possibly even in
						//	namespaces).
			final String fn2 = fn;
			
			try {
				final List<Object> argObjects = itsConverters.atomsToObjects(args, 0);
					
				Object result =
					new PushBindingsInvoker<Object>(this, getProxy(), itsNSOwner,
												    new StringReader("")
												   ) {
					@Override
					public Object invoke() throws Exception {
						//	We have to retrieve once we've set up bindings and namespace:
						Var var = retrieveFnAsVar(fn2);
						return var.applyTo(RT.seq(argObjects));
					}
				}.doit();
	
				getProxy().outlet(0, result);
			} catch (Exception exn) {
				getProxy().error(exn.toString());
			}
		}
	}

	@Override
	public void unwindCallbacks() {
		if (itsCleanupCallbacks00 != null) {
			while (!itsCleanupCallbacks00.empty()) {
				try {
					new PushBindingsInvoker<Object>(this, getProxy(), itsNSOwner,
													new StringReader("")
												   ) {
						@Override
						public Object invoke() throws Exception {
							itsCleanupCallbacks00.pop().call();
							return null;
						}
					}.doit();
				} catch (Exception exn) {
					getProxy().error(exn.toString());
				}
			}
		}
	}
}
