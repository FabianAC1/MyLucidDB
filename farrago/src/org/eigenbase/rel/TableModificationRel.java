/*
// Licensed to DynamoBI Corporation (DynamoBI) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  DynamoBI licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at

//   http://www.apache.org/licenses/LICENSE-2.0

// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.
*/
package org.eigenbase.rel;

import java.util.*;

import org.eigenbase.relopt.*;


/**
 * TableModificationRel is like TableAccessRel, but represents a request to
 * modify a table rather than read from it. It takes one child which produces
 * the modified rows. (For INSERT, the new values; for DELETE, the old values;
 * for UPDATE, all old values plus updated new values.)
 *
 * @version $Id$
 */
public final class TableModificationRel
    extends TableModificationRelBase
{
    //~ Constructors -----------------------------------------------------------

    public TableModificationRel(
        RelOptCluster cluster,
        RelOptTable table,
        RelOptConnection connection,
        RelNode child,
        Operation operation,
        List<String> updateColumnList,
        boolean flattened)
    {
        super(
            cluster,
            new RelTraitSet(CallingConvention.NONE),
            table,
            connection,
            child,
            operation,
            updateColumnList,
            flattened);
    }

    //~ Methods ----------------------------------------------------------------

    // implement Cloneable
    public TableModificationRel clone()
    {
        TableModificationRel clone =
            new TableModificationRel(
                getCluster(),
                table,
                connection,
                getChild().clone(),
                getOperation(),
                getUpdateColumnList(),
                isFlattened());
        clone.inheritTraitsFrom(this);
        return clone;
    }
}

// End TableModificationRel.java
