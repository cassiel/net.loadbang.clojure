//	$Id: 91221998950b2ac1be8843e0fddeff0d3f05a906 $
//	$Source$

package net.loadbang.clojure;

/**	Something which keeps hold of the current name of the MaxObject's
	namespace. This is actually the MXJ object (since namespace is
	an attribute).

	@author Nick Rothwell, nick@cassiel.com / nick@loadbang.net
 */

public interface NSOwner {
	public String getNS();
}
