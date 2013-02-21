// Metawidget (licensed under LGPL)
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

package org.metawidget.inspector.faces;

/**
 * Faces-specific element and attribute names appearing in DOMs conforming to
 * inspection-result-1.0.xsd.
 * 
 * @author Richard Kennard
 */

public final class FacesInspectionResultConstants {

	//
	// Public statics
	//

	public static final String	FACES_LOOKUP			= "faces-lookup";

	public static final String	FACES_LOOKUP_VAR		= "faces-lookup-var";

	public static final String	FACES_LOOKUP_ITEM_VALUE	= "faces-lookup-item-value";

	public static final String	FACES_LOOKUP_ITEM_LABEL	= "faces-lookup-item-label";

	public static final String	FACES_SUGGEST			= "faces-suggest";

	public static final String	FACES_EXPRESSION		= "faces-expression";

	public static final String	FACES_COMPONENT			= "faces-component";

	public static final String	FACES_AJAX_EVENT		= "faces-ajax-event";

	public static final String	FACES_AJAX_ACTION		= "faces-ajax-action";

	/**
	 * The Faces Converter to use for this component. Can be an id or an EL expression that
	 * evaluates to a <code>javax.faces.convert.Converter</code> instance.
	 */

	public static final String	FACES_CONVERTER			= "faces-converter";

	/**
	 * Attribute indicated the UIComponent should have its <code>immediate</code> flag set.
	 * <p>
	 * Should be used in conjunction with <code>ImmediateAttributeWidgetProcessor</code>.
	 */

	public static final String	FACES_IMMEDIATE			= "faces-immediate";

	//
	// Private constructor
	//

	private FacesInspectionResultConstants() {

		// Can never be called
	}
}
