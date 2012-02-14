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

import org.eigenbase.rel.*;
import org.eigenbase.relopt.*;


/**
 * <code>UnionEliminatorRule</code> checks to see if its possible to optimize a
 * Union call by eliminating the Union operator altogether in the case the call
 * consists of only one input.
 *
 * @author Wael Chatila
 * @version $Id$
 * @since Feb 4, 2005
 */
public class UnionEliminatorRule
    extends RelOptRule
{
    public static final UnionEliminatorRule instance =
        new UnionEliminatorRule();

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a UnionEliminatorRule.
     */
    private UnionEliminatorRule()
    {
        super(
            new RelOptRuleOperand(
                UnionRel.class,
                ANY));
    }

    //~ Methods ----------------------------------------------------------------

    public void onMatch(RelOptRuleCall call)
    {
        UnionRel union = (UnionRel) call.rels[0];
        if (union.getInputs().length != 1) {
            return;
        }
        if (union.isDistinct()) {
            return;
        }

        // REVIEW jvs 14-Mar-2006:  why don't we need to register
        // the equivalence here like we do in RemoveDistinctRule?
        // And is the clone actually required here?

        RelNode child = union.getInputs()[0];
        call.transformTo(child.clone());
    }
}

// End UnionEliminatorRule.java
