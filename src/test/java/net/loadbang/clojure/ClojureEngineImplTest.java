//	$Id: fd82d90904e13143b39e0a4522c967f94e52b00d $
//	$Source$

package net.loadbang.clojure;

import java.util.List;

import net.loadbang.scripting.MaxObjectProxy;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.cycling74.max.Atom;

@RunWith(JMock.class)
public class ClojureEngineImplTest {
	private Mockery itsContext = new JUnit4Mockery();

	@Test
	public void canExecutePhrase() {
		final NSOwner nsOwner = itsContext.mock(NSOwner.class);
		final MaxObjectProxy proxy = itsContext.mock(MaxObjectProxy.class);
		
		itsContext.checking(new Expectations() {{
			atLeast(1).of(nsOwner).getNS();
			will(returnValue("aaa"));
			
			exactly(1).of(proxy).outlet(1, "Hello-1");
			will(returnValue(true));		//	Actual Clojure outlet call.

			exactly(1).of(proxy).outlet(0, true);
			will(returnValue(true));		//	Done to report success.
		}});
		
		ClojureEngineImpl engine = new ClojureEngineImpl(proxy, nsOwner);
		engine.eval("(.outlet max/maxObject 1 \"Hello-1\")");
	}
	
	@Test
	public void canOutputMultipleArguments() {
		final NSOwner nsOwner = itsContext.mock(NSOwner.class);
		final MaxObjectProxy proxy = itsContext.mock(MaxObjectProxy.class);
		
		itsContext.checking(new Expectations() {{
			atLeast(1).of(nsOwner).getNS();
			will(returnValue("aaa"));
			
			exactly(1).of(proxy).outlet(with(any(Integer.class)),
									    with(any(List.class)));
			will(returnValue(true));

			exactly(1).of(proxy).outlet(0, true);
			will(returnValue(true));
		}});
		
		ClojureEngineImpl engine = new ClojureEngineImpl(proxy, nsOwner);
		engine.eval("(. max/maxObject outlet 1 [\"A\" \"B\" \"C\"])");
	}
	
	@Test
	public void canInvokeFullyQualifiedFnCall() {
		final NSOwner nsOwner = itsContext.mock(NSOwner.class);
		final MaxObjectProxy proxy = itsContext.mock(MaxObjectProxy.class);
		
		itsContext.checking(new Expectations() {{
			atLeast(1).of(nsOwner).getNS();
			will(returnValue("aaa"));

			exactly(1).of(proxy).outlet(0, null);
			will(returnValue(true));
		}});
		
		ClojureEngineImpl engine = new ClojureEngineImpl(proxy, nsOwner);
		
		engine.invoke("clojure.core/println", null, new Atom [] { Atom.newAtom("Hello-2") });
	}

	@Test
	public void canInvokeAtomListAsFnCall() {
		final NSOwner nsOwner = itsContext.mock(NSOwner.class);
		final MaxObjectProxy proxy = itsContext.mock(MaxObjectProxy.class);
		
		itsContext.checking(new Expectations() {{
			atLeast(1).of(nsOwner).getNS();
			will(returnValue("aaa"));

			exactly(1).of(proxy).outlet(0, null);
			will(returnValue(true));
		}});
		
		ClojureEngineImpl engine = new ClojureEngineImpl(proxy, nsOwner);
		
		engine.invoke("clojure.core/println", null, new Atom [] { Atom.newAtom("Hello-3") });
	}
}
