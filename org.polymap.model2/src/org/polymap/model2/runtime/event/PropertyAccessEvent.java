/* 
 * polymap.org
 * Copyright (C) 2015, Falko Bräutigam. All rights reserved.
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
package org.polymap.model2.runtime.event;

import java.util.EventObject;

import org.polymap.model2.Entity;
import org.polymap.model2.runtime.PropertyInfo;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class PropertyAccessEvent
        extends EventObject {

    private PropertyInfo        propInfo;
    
    private Object              value;

    
    public PropertyAccessEvent( Entity source, PropertyInfo propInfo, Object value ) {
        super( source );
        this.propInfo = propInfo;
        this.value = value;
    }

    public PropertyInfo getPropertyInfo() {
        return propInfo;
    }

    public String getPropertyName() {
        return propInfo.getName();
    }
    
    /**
     * The value of the property at the time it was accessed as the source of this
     * event.
     */
    public Object getPropertyValue() {
        return value;
    }
    
}
