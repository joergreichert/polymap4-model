/*
 * polymap.org Copyright (C) 2011-2015, Polymap GmbH. All rights reserved.
 * 
 * This is free software; you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software
 * Foundation; either version 2.1 of the License, or (at your option) any later
 * version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 */
package org.polymap.model2.store.recordstore;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

import org.polymap.model2.Entity;
import org.polymap.model2.engine.TemplateProperty;
import org.polymap.model2.query.Expressions;
import org.polymap.model2.query.grammar.BooleanExpression;
import org.polymap.recordstore.RecordQuery;
import org.polymap.recordstore.lucene.LuceneRecordQuery;
import org.polymap.recordstore.lucene.LuceneRecordStore;
import org.polymap.recordstore.lucene.ValueCoders;

/**
 * Converts {@link BooleanExpression} into Lucene queries.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
class LuceneQueryBuilder {

    private static Log                                            log             = LogFactory
                                                                                          .getLog( LuceneQueryBuilder.class );

    static final Query                                            ALL             = new MatchAllDocsQuery();

    private static List<Class<? extends LuceneExpressionHandler>> handlers;

    static {
        // more frequently used first
        handlers = new ArrayList();
        handlers.add( LuceneComparisonHandler.class );
        handlers.add( LuceneEqualsAnyHandler.class );
        handlers.add( LuceneJunctionHandler.class );
        handlers.add( LuceneAssociationHandler.class );
        handlers.add( LuceneIdHandler.class );
        handlers.add( LuceneQuantifierHandler.class );
    }

    // instance *******************************************

    protected LuceneRecordStore                                   store;

    protected List<BooleanExpression>                             postProcess     = new ArrayList();

    protected ValueCoders                                         valueCoders;

    /**
     * The current fieldname prefix. This accumulates the names of the traversed
     * Quantifier expressions.
     */
    protected FieldnameBuilder                                    traversedPrefix = FieldnameBuilder.EMPTY;

    public String                                                 logIndent       = "";


    public LuceneQueryBuilder( LuceneRecordStore store ) {
        this.store = store;
        this.valueCoders = store.getValueCoders();
    }


    public LuceneQueryBuilder( LuceneRecordStore store, String logIndent ) {
        this( store );
        this.logIndent = logIndent;
    }


    public List<BooleanExpression> getPostProcess() {
        return postProcess;
    }


    public RecordQuery createQuery( Class<? extends Entity> resultType, final BooleanExpression whereClause ) {
        assert postProcess.isEmpty();

        Query filterQuery = processExpression( null, whereClause, resultType );

        Query typeQuery = new TermQuery( new Term( RecordCompositeState.TYPE_KEY, resultType.getName() ) );
        Query result = null;
        if (!filterQuery.equals( ALL )) {
            result = new BooleanQuery();
            ((BooleanQuery)result).add( typeQuery, BooleanClause.Occur.MUST );
            ((BooleanQuery)result).add( filterQuery, BooleanClause.Occur.MUST );
        }
        else {
            result = typeQuery;
        }
        // log.debug( "    LUCENE: [" + StringUtils.abbreviate( result.toString(),
        // 256 ) + "]" );
        return new LuceneRecordQuery( store, result );
    }


    /**
     * 
     *
     * @param prefix The next composite prefix to add to {@link #traversedPrefix}
     *        while processing the given expression.
     * @param expression
     * @param resultType
     * @return
     */
    protected Query processExpression( String prefix, final BooleanExpression expression,
            Class<? extends Entity> resultType ) {
        if (expression == null || expression == Expressions.TRUE) {
            return ALL;
        }
        else if (expression == Expressions.FALSE) {
            BooleanQuery result = new BooleanQuery();
            result.add( ALL, BooleanClause.Occur.MUST_NOT );
            return result;
        }
        for (Class<? extends LuceneExpressionHandler> handlerClass : handlers) {

            // update traversed prefix
            FieldnameBuilder currentPrefix = traversedPrefix;
            traversedPrefix = (prefix != null) ? traversedPrefix.composite( prefix ) : traversedPrefix;

            String currentLogIndent = logIndent;
            logIndent += "    ";
            // find expression handler
            try {
                LuceneExpressionHandler handler = handlerClass.newInstance();
                handler.builder = this;
                handler.resultType = resultType;

                Query result = handler.handle( expression );

                if (result != null) {
                    return result;
                }
            }
            catch (ClassCastException e) {
                // handler's type parameter does not match
            }
            catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException( e );
            }
            finally {
                traversedPrefix = currentPrefix;
                logIndent = currentLogIndent;
            }
        }
        throw new UnsupportedOperationException( "Expression not supported: " + expression.getClass().getName() );
    }


    /**
     * Recursivly build the field name for the given Property.
     */
    protected FieldnameBuilder prefixedFieldname( TemplateProperty prop ) {
        return traversedPrefix.composite( simpleFieldname( prop ) );
    }


    /**
     *
     */
    protected static String simpleFieldname( TemplateProperty property ) {
        return property.getInfo().getNameInStore();
    }

}
