/* 
 * polymap.org
 * Copyright (C) 2012-2015, Falko Bräutigam. All rights reserved.
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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies the mixins of an {@link Entity}. An Entity can be cast to one of its
 * mixins via {@link Entity#as(Class)}.
 * <p/>
 * An Entity inherits the mixins of its super classes. Care must be taken if the same
 * mixin is applied to different levels of the type hierarchie. It is a good idea to
 * avoid this.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
@Retention( RetentionPolicy.RUNTIME )
@Target( { ElementType.TYPE } )
@Documented
public @interface Mixins {

    public Class<? extends Composite>[] value();
    
}
