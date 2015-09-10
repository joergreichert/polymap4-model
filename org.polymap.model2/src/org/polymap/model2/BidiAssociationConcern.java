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

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.model2.runtime.EntityRuntimeContext;
import org.polymap.model2.runtime.PropertyInfo;

/**
 * This {@link Association} concern maintains the back reference of a bidirectional
 * association. The back reference can be an {@link Association} or a
 * {@link ManyAssociation}. If multiple possible back references exists then
 * {@link BidiAssociationName} annotation can be used to choose the one to use.
 *
 * @see BidiAssociationName
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class BidiAssociationConcern<T extends Entity>
        extends PropertyConcernBase<T> 
        implements AssociationConcern<T> {

    private static Log log = LogFactory.getLog( BidiAssociationConcern.class );

    
    @Override
    public T get() {
        return ((Association<T>)delegate).get();
    }

    
    @Override
    public void set( T value ) {
        // value == null signals that association is removed, so we use current value as target
        T target = value != null ? value : get();

        // delegate
        ((Association<T>)delegate).set( value );
        
        // find back association
        PropertyBase backAssoc = findBackAssociation( context, info(), target );

        // find my host entity
        Class hostType = context.getInfo().getType();
        Entity hostEntity = (Entity)context.getCompositePart( hostType );

        // set back reference
        // Association
        if (backAssoc instanceof Association) {
            ((Association)backAssoc).set( value != null ? hostEntity : null );
        }
        // ManyAssocation
        else if (backAssoc instanceof ManyAssociation) {
            if (value != null) {
                ((ManyAssociation)backAssoc).add( hostEntity );
            }
            else {
                ((ManyAssociation)backAssoc).remove( hostEntity );                
            }
        }
        else {
            throw new IllegalStateException( "Unknown association type: " + backAssoc.getClass().getSimpleName() );            
        }
    }

    
    /**
     * 
     *
     * @param context The runtime context of the host entity.
     * @param propInfo The property of the host {@link Association}.
     * @param target The target {@link Entity}.
     * @return The {@link Association} or {@link ManyAssociation} that is the back
     *         association in the target.
     */
    public static <T extends Entity> PropertyBase<T> findBackAssociation( 
            EntityRuntimeContext context, PropertyInfo propInfo, T target ) {
        // find my host entity
        Class hostType = context.getInfo().getType();
        
        // find back association
        Collection<PropertyInfo> propInfos = target.info().getProperties();
        List<PropertyInfo> candidates = propInfos.stream()
                .filter( info -> info.getType().isAssignableFrom( hostType ) )
                .collect( Collectors.toList() );

        PropertyInfo backAssocInfo = null;
        // nothing found
        if (candidates.isEmpty()) {
            throw new IllegalStateException( "No back assocation found for: " + hostType.getSimpleName() + " in: " + target.getClass().getSimpleName() );
        }
        // multiple
        else if (candidates.size() > 1) {
            BidiAssociationName assocName = (BidiAssociationName)propInfo.getAnnotation( BidiAssociationName.class );
            if (assocName != null) {
                backAssocInfo = candidates.stream().filter( i -> i.getName().equals( assocName.value() ) ).findAny()
                        .orElseThrow( () -> new IllegalStateException( "No back assocation found for name: " + assocName.value() ) );
            }
            else {
                throw new IllegalStateException( "Multiple back assocation found for: " + hostType.getSimpleName() 
                        + " in: " + target.getClass().getSimpleName() 
                        + " as: " + candidates.stream().map( i -> i.getName() ).collect( Collectors.toList() ) + "."
                        + "Use @BidiAssocationName to specify which one to use.");
            }
        }
        // just one
        else {
            backAssocInfo = candidates.get( 0 );
        }

        return backAssocInfo.get( target );
    }
    
}
