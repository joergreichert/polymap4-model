/* 
 * polymap.org
 * Copyright (C) 2014-2015, Falko Bräutigam. All rights reserved.
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

import org.polymap.model2.Entity;
import org.polymap.model2.PropertyBase;
import org.polymap.model2.engine.TemplateProperty;
import org.polymap.model2.query.grammar.BooleanExpression;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
abstract class LuceneExpressionHandler<T extends BooleanExpression> {

    private static final Log log = LogFactory.getLog( LuceneExpressionHandler.class );

    protected LuceneQueryBuilder        builder;
    
    protected Class<? extends Entity>   resultType;
    

    public abstract Query handle( T expression );
    
    
    /**
     * @see LuceneQueryBuilder#simpleFieldname(TemplateProperty)
     */
    public String simpleFieldname( PropertyBase property ) {
        return LuceneQueryBuilder.simpleFieldname( (TemplateProperty)property );
    }

    /**
     * @see LuceneQueryBuilder#prefixedFieldname(TemplateProperty)
     */
    public FieldnameBuilder prefixedFieldname( PropertyBase property ) {
        return builder.prefixedFieldname( (TemplateProperty)property );
    }

    public void log( String op, Object... params ) {
        StringBuilder buf = new StringBuilder( 256 );
        for (Object param : params) {
            buf.append( buf.length() > 0 ? ", " : "" );
            buf.append( param.toString() );
        }
        log.debug( builder.logIndent + op + " : " + buf.toString() );    
    }

}
