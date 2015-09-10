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
package org.polymap.model2;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.model2.query.Expressions;
import org.polymap.model2.query.ResultSet;
import org.polymap.model2.runtime.EntityRepository;
import org.polymap.model2.runtime.UnitOfWork;

/**
 * Provides a computed back reference of a bidirectional {@link Association} or
 * {@link ManyAssociation}.
 * 
 * @see BidiAssociationName
 * @see BidiAssociationConcern
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ComputedBidiAssocation<T extends Entity>
        extends ComputedProperty<T> {

    private static Log log = LogFactory.getLog( ComputedBidiAssocation.class );

    
    @Override
    public T get() {
        EntityRepository repo = composite.context.getRepository();
        Entity template = (Entity)Expressions.template( info.getType(), repo );
        PropertyBase<Entity> backAssoc = BidiAssociationConcern.findBackAssociation( composite.context, info, template );
        
        UnitOfWork uow = composite.context.getUnitOfWork();
        // ManyAssociation
        if (backAssoc instanceof ManyAssociation) {
            ResultSet<T> results = uow.query( (Class<T>)info.getType() )
                    .where( Expressions.anyOf( ((ManyAssociation)backAssoc), Expressions.id( (Entity)composite ) ) )
                    .execute();
            assert results.size() <= 1;
            return results.stream().findAny().get();
        }
        // Association
        else if (backAssoc instanceof Association) {
            ResultSet<T> results = uow.query( (Class<T>)info.getType() )
                    .where( Expressions.the( ((Association)backAssoc), Expressions.id( (Entity)composite ) ) )
                    .execute();
            assert results.size() <= 1;
            return results.stream().findAny().get();
        }
        else {
            throw new IllegalStateException( "Unknown backAssoc type: " + backAssoc.getClass().getSimpleName() );
        }
    }
    
}
