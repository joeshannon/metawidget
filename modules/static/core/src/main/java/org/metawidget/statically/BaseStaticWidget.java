// Metawidget
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

package org.metawidget.statically;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

import org.metawidget.util.CollectionUtils;

/**
 * @author Richard Kennard
 */

public abstract class BaseStaticWidget
	implements StaticWidget {

	//
	// Private methods
	//

	private Map<Object, Object>	mClientProperties;

	//
	// Methods
	//

	public abstract void write( Writer writer )
		throws IOException;

	/**
	 * General storage area, like <code>JComponent.putClientProperty</code>.
	 */

	public void putClientProperty( Object key, Object value ) {

		if ( mClientProperties == null ) {
			mClientProperties = CollectionUtils.newHashMap();
		}

		mClientProperties.put( key, value );
	}

	/**
	 * General storage area, like <code>JComponent.putClientProperty</code>.
	 */

	@SuppressWarnings( "unchecked" )
	public <T> T getClientProperty( Object key ) {

		if ( mClientProperties == null ) {
			return null;
		}

		return (T) mClientProperties.get( key );
	}
}
