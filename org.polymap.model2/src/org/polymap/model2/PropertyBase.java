/* 
 * polymap.org
 * Copyright (C) 2014, Falko Bräutigam. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.model2;

import org.polymap.model2.runtime.PropertyInfo;

/**
 * Abstract base for all property types.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract interface PropertyBase<T> {

    public PropertyInfo info();
    
}
