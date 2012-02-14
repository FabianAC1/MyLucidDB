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
import org.eigenbase.sql.*;


/**
 * <code>EmptyRel</code> represents a relational expression with zero rows.
 *
 * <p>EmptyRel can not be implemented, but serves as a token for rules to match
 * so that empty sections of queries can be eliminated.
 *
 * <p>Rules:
 *
 * <ul>
 * <li>Created by {@link net.sf.farrago.query.FarragoReduceValuesRule}</li>
 * <li>Triggers {@link org.eigenbase.rel.rules.RemoveEmptyRule}</li>
 * </ul>
 *
 * @author jhyde
 * @version $Id$
 * @see org.eigenbase.rel.ValuesRel
 */
public class EmptyRel
    extends AbstractRelNode
{
    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new EmptyRel.
     *
     * @param cluster Cluster
     * @param rowType row type for tuples which would be produced by this rel if
     * it actually produced any, but it doesn't (see, philosophy is good for
     * something after all!)
     */
    public EmptyRel(
        RelOptCluster cluster,
        RelDataType rowType)
    {
        super(
            cluster,
            new RelTraitSet(CallingConvention.NONE));
        this.rowType = rowType;
    }

    //~ Methods ----------------------------------------------------------------

    // override Object
    public EmptyRel clone()
    {
        // immutable with no children
        return this;
    }

    // implement RelNode
    protected RelDataType deriveRowType()
    {
        return rowType;
    }

    // implement RelNode
    public RelOptCost computeSelfCost(RelOptPlanner planner)
    {
        return planner.makeZeroCost();
    }

    // implement RelNode
    public double getRows()
    {
        return 0.0;
    }

    // implement RelNode
    public void explain(RelOptPlanWriter pw)
    {
        if (pw.getDetailLevel() == SqlExplainLevel.DIGEST_ATTRIBUTES) {
            // For rel digest, include the row type to discriminate
            // this from other empties with different row types.
            pw.explain(
                this,
                new String[] { "type", },
                new Object[] { rowType });
        } else {
            // For normal EXPLAIN PLAN, omit the type.
            super.explain(pw);
        }
    }
}

// End EmptyRel.java
