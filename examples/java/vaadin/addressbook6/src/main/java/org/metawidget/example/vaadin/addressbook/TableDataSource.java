// Metawidget Examples (licensed under BSD License)
//
// Copyright (c) Richard Kennard
// All rights reserved
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions
// are met:
//
// * Redistributions of source code must retain the above copyright notice,
//   this list of conditions and the following disclaimer.
// * Redistributions in binary form must reproduce the above copyright notice,
//   this list of conditions and the following disclaimer in the documentation
//   and/or other materials provided with the distribution.
// * Neither the name of Richard Kennard nor the names of its contributors may
//   be used to endorse or promote products derived from this software without
//   specific prior written permission.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
// AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
// IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
// ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
// FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
// (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
// OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
// OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
// OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
// OF THE POSSIBILITY OF SUCH DAMAGE.

package org.metawidget.example.vaadin.addressbook;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.metawidget.util.ClassUtils;
import org.metawidget.util.CollectionUtils;

import com.vaadin.data.util.IndexedContainer;

/**
 * TableDataSource
 *
 * @author Loghman Barari
 */

public class TableDataSource<T extends Comparable<T>>
	extends IndexedContainer {

	//
	// Private members
	//

	private Class<T>		mClass;

	private Map<Object, T>	mDataSource;

	private String[]		mColumns;

	//
	// Constructor
	//

	public TableDataSource( Class<T> clazz, Collection<T> collection, String... columns ) {

		mClass = clazz;
		mColumns = columns;
		mDataSource = CollectionUtils.newHashMap();

		for ( String column : mColumns ) {
			addContainerProperty( column, getColumnType( column ), null );
		}

		importCollection( collection );
	}

	//
	// Public methods
	//

	public void importCollection( Collection<T> collection ) {

		removeAllItems();
		mDataSource.clear();

		if ( collection != null ) {

			List<T> list = CollectionUtils.newArrayList( collection );
			Collections.sort( list );

			for ( T item : list ) {

				Object itemId = addItem();

				for ( String column : mColumns ) {

					getItem( itemId ).getItemProperty( column ).setValue( getValue( item, column ) );
				}

				mDataSource.put( itemId, item );
			}
		}
	}

	public T getDataRow( Object itemId ) {

		return mDataSource.get( itemId );
	}

	//
	// Protected methods
	//

	protected Class<?> getColumnType( String column ) {

		return ClassUtils.getReadMethod( mClass, column ).getReturnType();
	}

	protected Object getValue( T item, String column ) {

		return ClassUtils.getProperty( item, column );
	}
}
