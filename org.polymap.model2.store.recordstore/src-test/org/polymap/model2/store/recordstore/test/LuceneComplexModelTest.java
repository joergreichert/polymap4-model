/* 
 * polymap.org
 * Copyright (C) 2014, Falko Bräutigam. All rights reserved.
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
package org.polymap.model2.store.recordstore.test;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.model2.runtime.EntityRepository;
import org.polymap.model2.store.recordstore.RecordStoreAdapter;
import org.polymap.model2.test.Company;
import org.polymap.model2.test.ComplexModelTest;
import org.polymap.model2.test.Employee;
import org.polymap.model2.test.Female;
import org.polymap.model2.test.Male;
import org.polymap.recordstore.IRecordStore;
import org.polymap.recordstore.lucene.LuceneRecordStore;

/**
 * The {@link ComplexModelTest} with {@link IRecordStore}/Lucene backend.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class LuceneComplexModelTest
        extends ComplexModelTest {

    private static final Log log = LogFactory.getLog( LuceneComplexModelTest.class );

    protected IRecordStore          store;

    
    public LuceneComplexModelTest( String name ) {
        super( name );
    }


    protected void setUp() throws Exception {
        super.setUp();
        store = new LuceneRecordStore();
        repo = EntityRepository.newConfiguration()
                .store.set( new RecordStoreAdapter( store ) )
                .entities.set( new Class[] {Employee.class, Company.class, Male.class, Female.class} )
                .create();
        uow = repo.newUnitOfWork();
    }
    
}
