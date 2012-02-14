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
import org.eigenbase.reltype.RelDataTypeField;
import org.eigenbase.rex.*;


/**
 * A JoinRel represents two relational expressions joined according to some
 * condition.
 *
 * <p>Some rules:
 *
 * <ul> <li>{@link org.eigenbase.rel.rules.ExtractJoinFilterRule} converts an
 * {@link JoinRel inner join} to a {@link FilterRel filter} on top of a {@link
 * JoinRel cartesian inner join}.  <li>{@link
 * net.sf.farrago.fennel.rel.FennelCartesianJoinRule} implements a JoinRel as a
 * cartesian product.  </ul>
 *
 * @author jhyde
 * @version $Id$
 */
public final class JoinRel
    extends JoinRelBase
{
    //~ Instance fields --------------------------------------------------------

    // NOTE jvs 14-Mar-2006:  Normally we don't use state like this
    // to control rule firing, but due to the non-local nature of
    // semijoin optimizations, it's pretty much required.
    private final boolean semiJoinDone;

    private List<RelDataTypeField> systemFieldList;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a JoinRel.
     *
     * @param cluster Cluster
     * @param left Left input
     * @param right Right input
     * @param condition Join condition
     * @param joinType Join type
     * @param variablesStopped Set of names of variables which are set by the
     * LHS and used by the RHS and are not available to nodes above this JoinRel
     * in the tree
     */
    public JoinRel(
        RelOptCluster cluster,
        RelNode left,
        RelNode right,
        RexNode condition,
        JoinRelType joinType,
        Set<String> variablesStopped)
    {
        this(
            cluster,
            left,
            right,
            condition,
            joinType,
            variablesStopped,
            false,
            Collections.<RelDataTypeField>emptyList());
    }

    /**
     * Creates a JoinRel, flagged with whether it has been translated to a
     * semi-join.
     *
     * @param cluster Cluster
     * @param left Left input
     * @param right Right input
     * @param condition Join condition
     * @param joinType Join type
     * @param variablesStopped Set of names of variables which are set by the
     * LHS and used by the RHS and are not available to nodes above this JoinRel
     * in the tree
     * @param semiJoinDone Whether this join has been translated to a semi-join
     * @param systemFieldList List of system fields that will be prefixed to
     * output row type; typically empty but must not be null
     *
     * @see #isSemiJoinDone()
     */
    public JoinRel(
        RelOptCluster cluster,
        RelNode left,
        RelNode right,
        RexNode condition,
        JoinRelType joinType,
        Set<String> variablesStopped,
        boolean semiJoinDone,
        List<RelDataTypeField> systemFieldList)
    {
        super(
            cluster,
            new RelTraitSet(CallingConvention.NONE),
            left,
            right,
            condition,
            joinType,
            variablesStopped);
        assert systemFieldList != null;
        this.semiJoinDone = semiJoinDone;
        this.systemFieldList = systemFieldList;
    }

    //~ Methods ----------------------------------------------------------------

    @SuppressWarnings({"CloneDoesntCallSuperClone"})
    public JoinRel clone()
    {
        JoinRel clone =
            new JoinRel(
                getCluster(),
                left.clone(),
                right.clone(),
                condition.clone(),
                joinType,
                new HashSet<String>(variablesStopped),
                isSemiJoinDone(),
                systemFieldList);
        clone.inheritTraitsFrom(this);
        return clone;
    }

    public void explain(RelOptPlanWriter pw)
    {
        // NOTE jvs 14-Mar-2006: Do it this way so that semijoin state
        // don't clutter things up in optimizers that don't use semijoins
        if (!semiJoinDone) {
            super.explain(pw);
            return;
        }
        pw.explain(
            this,
            new String[] {
                "left", "right", "condition", "joinType", "semiJoinDone"
            },
            new Object[] {
                joinType.name().toLowerCase(), semiJoinDone
            });
    }

    /**
     * Returns whether this JoinRel has already spawned a {@link
     * org.eigenbase.rel.rules.SemiJoinRel} via {@link
     * org.eigenbase.rel.rules.AddRedundantSemiJoinRule}.
     *
     * @return whether this join has already spawned a semi join
     */
    public boolean isSemiJoinDone()
    {
        return semiJoinDone;
    }

    public List<RelDataTypeField> getSystemFieldList()
    {
        return systemFieldList;
    }
}

// End JoinRel.java
