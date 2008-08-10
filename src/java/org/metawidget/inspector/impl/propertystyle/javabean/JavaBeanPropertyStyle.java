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

package org.metawidget.inspector.impl.propertystyle.javabean;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Map;

import org.metawidget.inspector.iface.InspectorException;
import org.metawidget.inspector.impl.propertystyle.BaseProperty;
import org.metawidget.inspector.impl.propertystyle.Property;
import org.metawidget.inspector.impl.propertystyle.PropertyStyle;
import org.metawidget.util.ClassUtils;
import org.metawidget.util.CollectionUtils;
import org.metawidget.util.simple.StringUtils;

/**
 * PropertyStyle for JavaBean-style properties.
 * <p>
 * This PropertyStyle recognizes both getters and setters and public member fields.
 *
 * @author Richard Kennard
 */

public class JavaBeanPropertyStyle
	implements PropertyStyle
{
	//
	//
	// Private members
	//
	//

	/**
	 * Cache of property lookups.
	 * <p>
	 * Property lookups are potentially expensive, so we cache them. The cache itself is a member
	 * variable, not a static, because we rely on <code>BaseObjectInspector</code> to only create
	 * one instance of <code>PropertyStyle</code> for all <code>Inspectors</code>.
	 * <p>
	 * This also stops problems with subclasses of <code>JavaBeanPropertyStyle</code> sharing the
	 * same static cache.
	 */

	private Map<Class<?>, Map<String, Property>>	mPropertiesCache	= CollectionUtils.newHashMap();

	//
	//
	// Public methods
	//
	//

	/**
	 * Returns properties sorted by name.
	 */

	public Map<String, Property> getProperties( Class<?> clazz )
	{
		synchronized ( mPropertiesCache )
		{
			Map<String, Property> properties = getCachedProperties( clazz );

			if ( properties == null )
			{
				// If the class is not a proxy...

				if ( !isProxy( clazz ) )
				{
					// ...inspect it normally

					properties = inspectProperties( clazz );
				}
				else
				{
					// ...otherwise, if the superclass is not just java.lang.Object...

					Class<?> superclass = clazz.getSuperclass();

					if ( !superclass.equals( Object.class ) )
					{
						// ...inspect the superclass

						properties = getCachedProperties( superclass );

						if ( properties == null )
						{
							properties = inspectProperties( superclass );
							cacheProperties( superclass, properties );
						}
					}
					else
					{
						// ...otherwise, inspect each interface and merge

						// TODO: Android Action
						// TODO: Groovy version of this

						properties = inspectProperties( clazz.getInterfaces() );
					}
				}

				// Cache the result

				cacheProperties( clazz, properties );
			}

			return properties;
		}
	}

	//
	//
	// Protected methods
	//
	//

	/**
	 * Whether to exclude the given property, of the given type, in the given class, when searching
	 * for properties.
	 * <p>
	 * This can be useful when the convention or base class define properties that are
	 * framework-specific, and should be filtered out from 'real' business model properties.
	 * <p>
	 * By default, calls <code>isExcludedBaseType</code>, <code>isExcludedReturnType</code> and
	 * <code>isExcludedName</code> and returns true if any of them return true. Returns false
	 * otherwise.
	 *
	 * @return true if the property should be excluded, false otherwise
	 */

	protected boolean isExcluded( Class<?> clazz, String propertyName, Class<?> propertyType )
	{
		if ( isExcludedBaseType( clazz ) )
			return true;

		if ( isExcludedReturnType( propertyType ) )
			return true;

		if ( isExcludedName( propertyName ) )
			return true;

		return false;
	}

	/**
	 * Whether to exclude the given property name when searching for properties.
	 * <p>
	 * This can be useful when the convention or base class define properties that are
	 * framework-specific, and should be filtered out from 'real' business model properties.
	 * <p>
	 * By default, excludes 'propertyChangeListeners' and 'vetoableChangeListeners'.
	 *
	 * @return true if the property should be excluded, false otherwise
	 */

	protected boolean isExcludedName( String name )
	{
		if ( "propertyChangeListeners".equals( name ) )
			return true;

		if ( "vetoableChangeListeners".equals( name ) )
			return true;

		return false;
	}

	/**
	 * Whether to exclude the given property return type when searching for properties.
	 * <p>
	 * This can be useful when the convention or base class define properties that are
	 * framework-specific, and should be filtered out from 'real' business model properties.
	 * <p>
	 * By default, does not exclude any return types.
	 *
	 * @return true if the property should be excluded, false otherwise
	 */

	protected boolean isExcludedReturnType( Class<?> clazz )
	{
		return false;
	}

	/**
	 * Whether to exclude the given base type when searching up the model inheritance chain.
	 * <p>
	 * This can be useful when the convention or base class define properties that are
	 * framework-specific, and should be filtered out from 'real' business model properties.
	 * <p>
	 * By default, excludes any base types from the <code>java.*</code> or <code>javax.*</code>
	 * packages.
	 *
	 * @return true if the property should be excluded, false otherwise
	 */

	protected boolean isExcludedBaseType( Class<?> clazz )
	{
		Package pkg = clazz.getPackage();

		if ( pkg == null )
			return false;

		String pkgName = pkg.getName();

		if ( pkgName.startsWith( "java." ) || pkgName.startsWith( "javax." ) )
			return true;

		return false;
	}

	/**
	 * Returns true if the given class is a 'proxy' of its original self.
	 * <p>
	 * Proxied classes generally don't carry annotations, so it is important to traverse away from
	 * the proxied class back to the original class before inspection.
	 * <p>
	 * By default, returns true for classes with <code>_$$_javassist_</code> or
	 * <code>ByCGLIB$$</code> in their name.
	 */

	protected boolean isProxy( Class<?> clazz )
	{
		// (don't use .getSimpleName or .contains, for J2SE 1.4 compatibility)

		String name = clazz.getName();

		if ( name.indexOf( "_$$_javassist_" ) != -1 )
			return true;

		if ( name.indexOf( "ByCGLIB$$" ) != -1 )
			return true;

		return false;
	}

	/**
	 * Inspect the given Classes and merge their results.
	 * <p>
	 * This version of <code>inspectProperties</code> is used when inspecting
	 * the interfaces of a proxied class.
	 */

	protected Map<String, Property> inspectProperties( Class<?>[] classes )
	{
		Map<String, Property> propertiesToReturn = CollectionUtils.newTreeMap();

		for ( Class<?> clazz : classes )
		{
			Map<String, Property> properties = getCachedProperties( clazz );

			if ( properties == null )
			{
				properties = inspectProperties( clazz );
				cacheProperties( clazz, properties );
			}

			propertiesToReturn.putAll( properties );
		}

		return propertiesToReturn;
	}

	/**
	 * @return the properties of the given class. Never null.
	 */

	protected Map<String, Property> inspectProperties( Class<?> clazz )
	{
		// TreeMap so that returns alphabetically sorted properties

		Map<String, Property> properties = CollectionUtils.newTreeMap();

		//
		// Public fields
		//
		// Note: we must use clazz.getFields(), not clazz.getDeclaredFields(), in order
		// to avoid Applet SecurityExceptions
		//

		for ( Field field : clazz.getFields() )
		{
			// Exclude static fields

			int modifiers = field.getModifiers();

			if ( Modifier.isStatic( modifiers ) )
				continue;

			// Get name and type

			String fieldName = field.getName();
			Class<?> type = field.getType();

			// Exclude based on other criteria

			if ( isExcluded( field.getDeclaringClass(), fieldName, type ) )
				continue;

			properties.put( fieldName, new FieldProperty( fieldName, field ) );
		}

		//
		// Public getter methods
		//
		// Note: we must use clazz.getMethods(), not clazz.getDeclaredMethods(), in order
		// to avoid Applet SecurityExceptions
		//

		for ( Method methodRead : clazz.getMethods() )
		{
			// Get type

			Class<?>[] parameters = methodRead.getParameterTypes();

			if ( parameters.length != 0 )
				continue;

			Class<?> type = methodRead.getReturnType();

			if ( void.class.equals( type ) )
				continue;

			// Get name

			String methodName = methodRead.getName();
			String propertyName = null;

			if ( methodName.startsWith( ClassUtils.JAVABEAN_GET_PREFIX ) )
				propertyName = methodName.substring( ClassUtils.JAVABEAN_GET_PREFIX.length() );
			else if ( methodName.startsWith( ClassUtils.JAVABEAN_IS_PREFIX ) )
				propertyName = methodName.substring( ClassUtils.JAVABEAN_IS_PREFIX.length() );
			else
				continue;

			if ( !StringUtils.isFirstLetterUppercase( propertyName ) )
				continue;

			String lowercasedPropertyName = StringUtils.lowercaseFirstLetter( propertyName );

			// Exclude based on other criteria

			if ( isExcluded( methodRead.getDeclaringClass(), lowercasedPropertyName, type ) )
				continue;

			// Already found via its field?

			Property existingProperty = properties.get( lowercasedPropertyName );

			if ( existingProperty instanceof FieldProperty )
				continue;

			// Already found via another getter?

			if ( existingProperty instanceof JavaBeanProperty )
			{
				JavaBeanProperty existingJavaBeanProperty = (JavaBeanProperty) existingProperty;

				// Beware covariant return types: always prefer the
				// subclass

				if ( type.isAssignableFrom( existingJavaBeanProperty.getType() ) )
					continue;
			}

			properties.put( lowercasedPropertyName, new JavaBeanProperty( lowercasedPropertyName, type, methodRead, null ) );
		}

		//
		// Public setter methods
		//
		// Note: we must use clazz.getMethods(), not clazz.getDeclaredMethods(), in order
		// to avoid Applet SecurityExceptions
		//

		for ( Method methodWrite : clazz.getMethods() )
		{
			// Get type

			Class<?>[] parameters = methodWrite.getParameterTypes();

			if ( parameters.length != 1 )
				continue;

			Class<?> type = parameters[0];

			// Get name

			String methodName = methodWrite.getName();

			if ( !methodName.startsWith( ClassUtils.JAVABEAN_SET_PREFIX ) )
				continue;

			String propertyName = methodName.substring( ClassUtils.JAVABEAN_SET_PREFIX.length() );

			if ( !StringUtils.isFirstLetterUppercase( propertyName ) )
				continue;

			String lowercasedPropertyName = StringUtils.lowercaseFirstLetter( propertyName );

			// Exclude based on other criteria

			if ( isExcluded( methodWrite.getDeclaringClass(), lowercasedPropertyName, type ) )
				continue;

			// Already found via its field?

			Property existingProperty = properties.get( lowercasedPropertyName );

			if ( existingProperty instanceof FieldProperty )
				continue;

			// Already found via its getter?

			if ( existingProperty instanceof JavaBeanProperty )
			{
				JavaBeanProperty existingJavaBeanProperty = (JavaBeanProperty) existingProperty;
				properties.put( lowercasedPropertyName, new JavaBeanProperty( lowercasedPropertyName, type, existingJavaBeanProperty.getReadMethod(), methodWrite ) );
				continue;
			}

			properties.put( lowercasedPropertyName, new JavaBeanProperty( lowercasedPropertyName, type, null, methodWrite ) );
		}

		return properties;
	}

	protected Map<String, Property> getCachedProperties( Class<?> clazz )
	{
		return mPropertiesCache.get( clazz );
	}

	protected void cacheProperties( Class<?> clazz, Map<String, Property> properties )
	{
		mPropertiesCache.put( clazz, Collections.unmodifiableMap( properties ));
	}

	//
	//
	// Inner classes
	//
	//

	/**
	 * Public member field-based property.
	 */

	protected static class FieldProperty
		extends BaseProperty
	{
		//
		//
		// Private methods
		//
		//

		private Field	mField;

		//
		//
		// Constructor
		//
		//

		public FieldProperty( String name, Field field )
		{
			super( name, field.getType() );

			mField = field;

			if ( mField == null )
				throw new NullPointerException( "field" );
		}

		//
		//
		// Public methods
		//
		//

		public boolean isReadable()
		{
			return true;
		}

		public Object read( Object obj )
		{
			try
			{
				return mField.get( obj );
			}
			catch ( Exception e )
			{
				throw InspectorException.newException( e );
			}
		}

		public boolean isWritable()
		{
			return true;
		}

		public <T extends Annotation> T getAnnotation( Class<T> annotation )
		{
			return mField.getAnnotation( annotation );
		}

		public Type getGenericType()
		{
			return mField.getGenericType();
		}

		/**
		 * Exposed for JavassistPropertyStyle.
		 */

		public Field getField()
		{
			return mField;
		}
	}

	/**
	 * JavaBean-convention-based property.
	 */

	protected static class JavaBeanProperty
		extends BaseProperty
	{
		//
		//
		// Private methods
		//
		//

		private Method	mReadMethod;

		private Method	mWriteMethod;

		//
		//
		// Constructor
		//
		//

		public JavaBeanProperty( String name, Class<?> clazz, Method readMethod, Method writeMethod )
		{
			super( name, clazz );

			mReadMethod = readMethod;
			mWriteMethod = writeMethod;

			// Must have either a getter or a setter (or both)

			if ( mReadMethod == null && mWriteMethod == null )
				throw InspectorException.newException( "JavaBeanProperty '" + name + "' has no getter and no setter" );
		}

		//
		//
		// Public methods
		//
		//

		public boolean isReadable()
		{
			return ( mReadMethod != null );
		}

		public Object read( Object obj )
		{
			try
			{
				return mReadMethod.invoke( obj );
			}
			catch ( Exception e )
			{
				throw InspectorException.newException( e );
			}
		}

		public boolean isWritable()
		{
			return ( mWriteMethod != null );
		}

		public <T extends Annotation> T getAnnotation( Class<T> annotationClass )
		{
			if ( mReadMethod != null )
			{
				T annotation = mReadMethod.getAnnotation( annotationClass );

				if ( annotation != null )
					return annotation;
			}

			if ( mWriteMethod != null )
			{
				T annotation = mWriteMethod.getAnnotation( annotationClass );

				if ( annotation != null )
					return annotation;

				return null;
			}

			return null;
		}

		public Type getGenericType()
		{
			if ( mReadMethod != null )
				return mReadMethod.getGenericReturnType();

			return mWriteMethod.getGenericParameterTypes()[0];
		}

		/**
		 * Exposed for JavassistPropertyStyle.
		 */

		public Method getReadMethod()
		{
			return mReadMethod;
		}

		/**
		 * Exposed for JavassistPropertyStyle.
		 */

		public Method getWriteMethod()
		{
			return mWriteMethod;
		}
	}
}
