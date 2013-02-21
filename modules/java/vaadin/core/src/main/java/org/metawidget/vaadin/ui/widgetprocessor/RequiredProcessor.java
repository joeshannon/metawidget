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

package org.metawidget.vaadin.ui.widgetprocessor;

import static org.metawidget.inspector.InspectionResultConstants.*;

import java.text.MessageFormat;
import java.util.Map;

import org.metawidget.vaadin.ui.VaadinMetawidget;
import org.metawidget.widgetprocessor.iface.WidgetProcessor;

import com.vaadin.ui.Component;
import com.vaadin.ui.Field;

/**
 * WidgetProcessor that sets the 'required' property on a Vaadin Field.
 *
 * @author Richard Kennard
 */

public class RequiredProcessor
	implements WidgetProcessor<Component, VaadinMetawidget> {

	//
	// Public methods
	//

	public Component processWidget( Component component, String elementName, Map<String, String> attributes, VaadinMetawidget metawidget ) {

		if ( !( component instanceof Field )) {
			return component;
		}

		Field field = (Field) component;

		if ( TRUE.equals( attributes.get( REQUIRED ) ) ) {
			field.setRequired( true );
			field.setRequiredError( MessageFormat.format( "{0} is required", field.getCaption() ) );
		}

		return component;
	}
}
