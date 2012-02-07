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

import org.eigenbase.relopt.*;
import org.eigenbase.reltype.*;
import org.eigenbase.rex.*;


/**
 * <code>TableFunctionRel</code> represents a call to a function which returns a
 * result set. Currently, it can only appear as a leaf in a query tree, but
 * eventually we will extend it to take relational inputs.
 *
 * @author John V. Sichi
 * @version $Id$
 */
public class TableFunctionRel
    extends TableFunctionRelBase
{
    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a <code>TableFunctionRel</code>.
     *
     * @param cluster {@link RelOptCluster}  this relational expression belongs
     * to
     * @param rexCall function invocation expression
     * @param rowType row type produced by function
     * @param inputs 0 or more relational inputs
     */
    public TableFunctionRel(
        RelOptCluster cluster,
        RexNode rexCall,
        RelDataType rowType,
        RelNode [] inputs)
    {
        super(
            cluster,
            new RelTraitSet(CallingConvention.NONE),
            rexCall,
            rowType,
            inputs);
    }

    //~ Methods ----------------------------------------------------------------

    public TableFunctionRel clone()
    {
        TableFunctionRel clone =
            new TableFunctionRel(
                getCluster(),
                getCall(),
                getRowType(),
                RelOptUtil.clone(inputs));
        clone.inheritTraitsFrom(this);
        clone.setColumnMappings(getColumnMappings());
        return clone;
    }

    public RelOptCost computeSelfCost(RelOptPlanner planner)
    {
        // REVIEW jvs 8-Jan-2006:  what is supposed to be here
        // for an abstract rel?
        return planner.makeHugeCost();
    }
}

// End TableFunctionRel.java
