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
package org.eigenbase.rel.rules;

import java.util.*;

import org.eigenbase.rel.*;
import org.eigenbase.relopt.*;
import org.eigenbase.rex.*;


/**
 * PushProjectPastSetOpRule implements the rule for pushing a {@link ProjectRel}
 * past a {@link SetOpRel}. The children of the {@link SetOpRel} will project
 * only the {@link RexInputRef}s referenced in the original {@link ProjectRel}.
 *
 * @author Zelaine Fong
 * @version $Id$
 */
public class PushProjectPastSetOpRule
    extends RelOptRule
{
    public static final PushProjectPastSetOpRule instance =
        new PushProjectPastSetOpRule();

    //~ Instance fields --------------------------------------------------------

    /**
     * Expressions that should be preserved in the projection
     */
    private PushProjector.ExprCondition preserveExprCondition;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a PushProjectPastSetOpRule.
     */
    private PushProjectPastSetOpRule()
    {
        super(
            new RelOptRuleOperand(
                ProjectRel.class,
                new RelOptRuleOperand(SetOpRel.class, ANY)));
        this.preserveExprCondition = PushProjector.ExprCondition.FALSE;
    }

    /**
     * Creates a PushProjectPastSetOpRule with an explicit condition whether
     * to preserve expressions.
     *
     * @param preserveExprCondition Condition whether to preserve expressions
     */
    public PushProjectPastSetOpRule(
        PushProjector.ExprCondition preserveExprCondition)
    {
        super(
            new RelOptRuleOperand(
                ProjectRel.class,
                new RelOptRuleOperand(SetOpRel.class, ANY)));
        this.preserveExprCondition = preserveExprCondition;
    }

    //~ Methods ----------------------------------------------------------------

    // implement RelOptRule
    public void onMatch(RelOptRuleCall call)
    {
        ProjectRel origProj = (ProjectRel) call.rels[0];
        SetOpRel setOpRel = (SetOpRel) call.rels[1];

        // cannot push project past a distinct
        if (setOpRel.isDistinct()) {
            return;
        }

        // locate all fields referenced in the projection
        PushProjector pushProject =
            new PushProjector(origProj, null, setOpRel, preserveExprCondition);
        pushProject.locateAllRefs();

        RelNode [] setOpInputs = setOpRel.getInputs();
        int nSetOpInputs = setOpInputs.length;
        RelNode [] newSetOpInputs = new RelNode[nSetOpInputs];
        int [] adjustments = pushProject.getAdjustments();

        // push the projects completely below the setop; this
        // is different from pushing below a join, where we decompose
        // to try to keep expensive expressions above the join,
        // because UNION ALL does not have any filtering effect,
        // and it is the only operator this rule currently acts on
        for (int i = 0; i < nSetOpInputs; i++) {
            // be lazy:  produce two ProjectRels, and let another rule
            // merge them (could probably just clone origProj instead?)
            newSetOpInputs[i] =
                pushProject.createProjectRefsAndExprs(
                    setOpInputs[i],
                    true,
                    false);
            newSetOpInputs[i] =
                pushProject.createNewProject(newSetOpInputs[i], adjustments);
        }

        // create a new setop whose children are the ProjectRels created above
        SetOpRel newSetOpRel =
            RelOptUtil.createNewSetOpRel(setOpRel, newSetOpInputs);

        call.transformTo(newSetOpRel);
    }
}

// End PushProjectPastSetOpRule.java
