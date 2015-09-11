/* 
 * polymap.org
 * Copyright 2012, Falko Bräutigam. All rights reserved.
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
package org.polymap.model2.test;

import org.polymap.model2.BidiManyAssociationConcern;
import org.polymap.model2.Concerns;
import org.polymap.model2.Entity;
import org.polymap.model2.ManyAssociation;
import org.polymap.model2.NameInStore;
import org.polymap.model2.runtime.CompositeInfo;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
@NameInStore("Male")
public class Female
        extends Entity {
    
    public static CompositeInfo         INFO;

    @Concerns(BidiManyAssociationConcern.class)
    public ManyAssociation<Male>        friends;
    
}
