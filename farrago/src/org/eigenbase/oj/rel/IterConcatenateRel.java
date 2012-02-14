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
package org.eigenbase.oj.rel;

import openjava.mop.*;

import openjava.ptree.*;

import org.eigenbase.oj.util.*;
import org.eigenbase.rel.*;
import org.eigenbase.rel.metadata.*;
import org.eigenbase.relopt.*;


/**
 * <code>IterConcatenateRel</code> concatenates several iterators. It is an
 * iterator implementation of {@link UnionRel}.
 */
public class IterConcatenateRel
    extends UnionRelBase
    implements JavaRel
{
    //~ Constructors -----------------------------------------------------------

    public IterConcatenateRel(
        RelOptCluster cluster,
        RelNode [] inputs)
    {
        super(
            cluster,
            new RelTraitSet(CallingConvention.ITERATOR),
            inputs,
            true /*all*/);
    }

    //~ Methods ----------------------------------------------------------------

    public IterConcatenateRel clone()
    {
        // REVIEW jvs 13-Nov-2005:  shouldn't we be cloning the inputs too?
        IterConcatenateRel clone =
            new IterConcatenateRel(
                getCluster(),
                inputs);
        clone.inheritTraitsFrom(this);
        return clone;
    }

    public IterConcatenateRel clone(RelNode [] inputs, boolean all)
    {
        assert all;
        IterConcatenateRel clone =
            new IterConcatenateRel(
                getCluster(),
                inputs);
        clone.inheritTraitsFrom(this);
        return clone;
    }

    public RelOptCost computeSelfCost(RelOptPlanner planner)
    {
        double dRows = RelMetadataQuery.getRowCount(this);

        // favor a Nexus over a CompoundIterator, due to hassles of
        // java/c++/java data transfer
        double dCpu = 1000;
        double dIo = 1000;
        return planner.makeCost(dRows, dCpu, dIo);
    }

    protected OJClass getCompoundIteratorClass()
    {
        return OJClass.forClass(
            org.eigenbase.runtime.CompoundTupleIter.class);
    }

    public ParseTree implement(JavaRelImplementor implementor)
    {
        // Generate
        //   new CompoundTupleIter(
        //     new TupleIter[] {<<input0>>, ...})
        // If any input is infinite, should instead generate
        //   new CompoundParallelTupleIter(
        //     new TupleIter[] {<<input0>>, ...})
        // but there's no way to tell, so we can't.

        // REVIEW: mb 9-Sep-2005: add a predicate RelNode.isInfinite().
        ExpressionList exps = new ExpressionList();
        for (int i = 0; i < inputs.length; i++) {
            Expression exp =
                implementor.visitJavaChild(this, i, (JavaRel) inputs[i]);
            exps.add(exp);
        }
        return new AllocationExpression(
            getCompoundIteratorClass(),
            new ExpressionList(
                new ArrayAllocationExpression(
                    OJUtil.clazzTupleIter,
                    new ExpressionList(null),
                    new ArrayInitializer(exps))));
    }
}

// End IterConcatenateRel.java
