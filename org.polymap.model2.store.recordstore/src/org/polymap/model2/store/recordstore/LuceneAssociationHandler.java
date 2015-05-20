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

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;

import org.polymap.model2.engine.TemplateProperty;
import org.polymap.model2.query.grammar.AssociationEquals;
import org.polymap.model2.query.grammar.IdPredicate;
import org.polymap.model2.runtime.ModelRuntimeException;
import org.polymap.recordstore.QueryExpression;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
class LuceneAssociationHandler
        extends LuceneExpressionHandler<AssociationEquals> {

    private static Log log = LogFactory.getLog( LuceneAssociationHandler.class );

    @Override
    public Query handle( AssociationEquals expression ) {
        // Id
        if (expression.children[0] instanceof IdPredicate) {
            IdPredicate predicate = (IdPredicate)expression.children[0];

            TemplateProperty assoc = expression.assoc;
            String fieldname = prefixedFieldname( assoc ).get();
            
            log( "ID", fieldname + " is/in " + ArrayUtils.toString( predicate.ids ) );
            //assert predicate.ids.length == 1 : "Ids != 1 for Association: " + expression.assoc.getInfo().getName() + ", ids:" + ArrayUtils.toString( predicate.ids );
            
            if (predicate.ids.length == 0) {
                throw new ModelRuntimeException( "Ids == 0 for Association: " + expression.assoc.info().getName() );                
            }
            else if (predicate.ids.length == 1) {
                Object id = predicate.ids[0];
                return builder.valueCoders.searchQuery( new QueryExpression.Equal( fieldname, id ) );
            }
            else {
                BooleanQuery result = new BooleanQuery();
                for (Object id : predicate.ids) {
                    Query sub = builder.valueCoders.searchQuery( new QueryExpression.Equal( fieldname, id ) );
                    result.add( sub, BooleanClause.Occur.SHOULD );
                }
                return result;
            }
        }
        // sub-expression
        else {
            throw new RuntimeException( "Sub-queries are not supported yet." );
        }
    }
    
}
