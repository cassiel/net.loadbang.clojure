//	$Id: 0806a5af55bf59b6663e37dcde88b1ee5daff9e0 $
//	$Source$

package net.loadbang.clojure;

import clojure.lang.Symbol;
import java.util.HashSet;
import java.util.Set;

public class NamespaceTracker {
	private Set<Symbol> itsEncounteredSymbols = new HashSet<Symbol>();

	public synchronized boolean firstEncounter(Symbol sym) {
		if (itsEncounteredSymbols.contains(sym)) {
			return false;
		} else {
			itsEncounteredSymbols.add(sym);
			return true;
		}
	}
}
