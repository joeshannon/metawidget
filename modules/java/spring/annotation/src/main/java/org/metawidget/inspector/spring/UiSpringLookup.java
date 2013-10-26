// Metawidget
//
// This library is dual licensed under both LGPL and a commercial
// license.
//
// LGPL: this library is free software; you can redistribute it and/or
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
//
// Commercial License: See http://metawidget.org for details

package org.metawidget.inspector.spring;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotates the value returned by the field should belong to the set returned by the given EL
 * expression.
 * <p>
 * Because Spring's <code>s:options</code> tag takes a JSP EL expression, not a 'Spring beans aware'
 * expression, this annotation is best used in conjunction with
 * <code>InternalResourceViewResolver.setExposeContextBeansAsAttributes</code> (available in Spring
 * 2.5+).
 *
 * @author <a href="http://kennardconsulting.com">Richard Kennard</a>
 */

@Retention( RetentionPolicy.RUNTIME )
@Target( { ElementType.FIELD, ElementType.METHOD } )
public @interface UiSpringLookup {

	/**
	 * Value of the lookup. Equivalent to
	 * <code>org.springframework.web.servlet.tags.form.OptionsTag.setItems</code>
	 */

	String value();

	/**
	 * Name of the property mapped to the label (inner text) of the 'option' tag. Equivalent to
	 * <code>org.springframework.web.servlet.tags.form.OptionsTag.setItemValue</code>
	 */

	String itemValue() default "";

	/**
	 * Name of the property mapped to the 'value' attribute of the 'option' tag. Equivalent to
	 * <code>org.springframework.web.servlet.tags.form.OptionsTag.setItemLabel</code>
	 */

	String itemLabel() default "";
}
