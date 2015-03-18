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
package org.polymap.model2.store.recordstore;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.search.Query;

import org.polymap.model2.query.grammar.BooleanExpression;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
class LuceneSpatialHandler
        extends LuceneExpressionHandler {

    private static Log log = LogFactory.getLog( LuceneSpatialHandler.class );

    @Override
    public Query handle( BooleanExpression expression ) {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }
    
//  protected Query processBBOX( SpatialPredicate.BBOX bbox ) {
//  PropertyReference<Envelope> property = bbox.getPropertyReference();
//  String fieldName = property2Fieldname( property ).toString();
//  
//  Envelope envelope = (Envelope)bbox.getValueExpression().value();
//
//  return store.getValueCoders().searchQuery( 
//          new QueryExpression.BBox( fieldName, envelope.getMinX(), envelope.getMinY(), envelope.getMaxX(), envelope.getMaxY() ) );
//}
//
//
//protected Query processSpatial( SpatialPredicate predicate ) {
//  PropertyReference<Envelope> property = predicate.getPropertyReference();
//  String fieldName = property2Fieldname( property ).toString();
//
//  Geometry value = (Geometry)predicate.getValueExpression().value();
//  
//  Envelope bounds = value.getEnvelopeInternal();
//  
//  postProcess.add( predicate );
//  
//  return store.getValueCoders().searchQuery( 
//          new QueryExpression.BBox( fieldName, bounds.getMinX(), bounds.getMinY(), bounds.getMaxX(), bounds.getMaxY() ) );
//}

}

