/* 
 * polymap.org
 * Copyright (C) 2014, Falko Bräutigam. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3.0 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.model2;

import org.polymap.model2.runtime.ValueInitializer;

/**
 * Provides no-op implementations for all methods.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class PropertyConcernAdapter<T>
        extends PropertyConcernBase<T>
        implements PropertyConcern<T> {

    protected Property<T> delegate() {
        return (Property<T>)delegate;
    }

    @Override
    public T get() {
        return delegate().get();
    }

    @Override
    public <U extends T> U createValue( ValueInitializer<U> initializer ) {
        return delegate().createValue( initializer );
    }

    @Override
    public void set( T value ) {
        delegate().set( value );
    }

    @Override
    public String toString() {
        return delegate().toString();
    }

}
