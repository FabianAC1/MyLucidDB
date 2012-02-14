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
import org.eigenbase.reltype.*;


/**
 * CoerceInputsRule precasts inputs to a particular type. This can be used to
 * assist operator implementations which impose requirements on their input
 * types.
 *
 * @author John V. Sichi
 * @version $Id$
 */
public class CoerceInputsRule
    extends RelOptRule
{
    //~ Instance fields --------------------------------------------------------

    private final Class consumerRelClass;

    private final boolean coerceNames;

    //~ Constructors -----------------------------------------------------------

    /**
     * Constructs the rule.
     *
     * @param consumerRelClass the RelNode class which will consume the inputs
     * @param coerceNames if true, coerce names and types; if false, coerce type
     * only
     */
    public CoerceInputsRule(
        Class<? extends RelNode> consumerRelClass,
        boolean coerceNames)
    {
        super(
            new RelOptRuleOperand(consumerRelClass, ANY),
            "CoerceInputsRule:" + consumerRelClass.getName());
        this.consumerRelClass = consumerRelClass;
        this.coerceNames = coerceNames;
    }

    //~ Methods ----------------------------------------------------------------

    // implement RelOptRule
    public CallingConvention getOutConvention()
    {
        return CallingConvention.NONE;
    }

    // implement RelOptRule
    public void onMatch(RelOptRuleCall call)
    {
        RelNode consumerRel = call.rels[0];
        if (consumerRel.getClass() != consumerRelClass) {
            // require exact match on type
            return;
        }
        RelNode [] inputs = consumerRel.getInputs();
        RelNode [] newInputs = new RelNode[inputs.length];
        boolean coerce = false;
        for (int i = 0; i < inputs.length; ++i) {
            RelDataType expectedType = consumerRel.getExpectedInputRowType(i);
            RelNode input = inputs[i];
            newInputs[i] =
                RelOptUtil.createCastRel(
                    input,
                    expectedType,
                    coerceNames);
            if (newInputs[i] != input) {
                coerce = true;
            }
            assert (RelOptUtil.areRowTypesEqual(
                newInputs[i].getRowType(),
                expectedType,
                coerceNames));
        }
        if (!coerce) {
            return;
        }
        RelNode newConsumerRel = consumerRel.clone();
        for (int i = 0; i < newInputs.length; ++i) {
            newConsumerRel.replaceInput(i, newInputs[i]);
        }
        call.transformTo(newConsumerRel);
    }
}

// End CoerceInputsRule.java
