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
package org.polymap.model2.store.recordstore;

import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;

import org.polymap.model2.engine.TemplateProperty;
import org.polymap.model2.query.grammar.PropertyEqualsAny;
import org.polymap.recordstore.QueryExpression;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
class LuceneEqualsAnyHandler
        extends LuceneExpressionHandler<PropertyEqualsAny> {

    @Override
    public Query handle( PropertyEqualsAny predicate ) {
        TemplateProperty prop = predicate.prop;
        Object[] values = predicate.values;
        
        // support enums
        for (int i=0; i<values.length; i++) {
            if (values[i] instanceof Enum) {
                values[i] = values[i].toString();
            }
        }
        String fieldname = prefixedFieldname( prop ).get();

        // create query
        if (predicate instanceof PropertyEqualsAny) {
            BooleanQuery result = new BooleanQuery();
            for (Object value : values) {
                result.add( 
                        builder.valueCoders.searchQuery( new QueryExpression.Equal( fieldname, value ) ),
                        BooleanClause.Occur.SHOULD );                
            }
            return result;
        }
        else {
            throw new UnsupportedOperationException( "Predicate type not supported in comparison: " + predicate );
        }
    }

}
