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

import org.eigenbase.rel.*;
import org.eigenbase.rel.convert.*;
import org.eigenbase.relopt.*;
import org.eigenbase.rex.*;


/**
 * A collection of optimizer rules related to the {@link
 * CallingConvention#ITERATOR iterator calling convention}.
 *
 * @version $Id$
 */
public abstract class IterRules
{
    //~ Inner Classes ----------------------------------------------------------

    /**
     * Rule to convert a {@link UnionRel} to {@link CallingConvention#ITERATOR
     * iterator calling convention}.
     */
    public static class UnionToIteratorRule
        extends ConverterRule
    {
        public UnionToIteratorRule()
        {
            this("UnionToIteratorRule");
        }

        protected UnionToIteratorRule(String description)
        {
            super(
                UnionRel.class,
                CallingConvention.NONE,
                CallingConvention.ITERATOR,
                description);
        }

        // factory method
        protected RelNode newIterConcatenateRel(
            RelOptCluster cluster,
            RelNode [] inputs)
        {
            return new IterConcatenateRel(cluster, inputs);
        }

        public RelNode convert(RelNode rel)
        {
            final UnionRel union = (UnionRel) rel;
            if (union.isDistinct()) {
                return null; // can only convert non-distinct Union
            }
            RelNode [] newInputs = new RelNode[union.getInputs().length];
            for (int i = 0; i < newInputs.length; i++) {
                // Stubborn, because inputs don't appear as operands.
                newInputs[i] =
                    mergeTraitsAndConvert(
                        union.getTraits(),
                        CallingConvention.ITERATOR,
                        union.getInput(i));
                if (newInputs[i] == null) {
                    return null; // cannot convert this input
                }
            }
            return newIterConcatenateRel(
                union.getCluster(),
                newInputs);
        }
    }

    /**
     * Refinement of {@link UnionToIteratorRule} which only applies to a {@link
     * UnionRel} all of whose input rows are the same type as its output row.
     * Luckily, a {@link org.eigenbase.rel.rules.CoerceInputsRule} will have
     * made that happen.
     */
    public static class HomogeneousUnionToIteratorRule
        extends UnionToIteratorRule
    {
        public static final HomogeneousUnionToIteratorRule instance =
            new HomogeneousUnionToIteratorRule();

        /**
         * Creates a HomogeneousUnionToIteratorRule.
         */
        private HomogeneousUnionToIteratorRule()
        {
            this("HomogeneousUnionToIteratorRule");
        }

        protected HomogeneousUnionToIteratorRule(String description)
        {
            super(description);
        }

        public RelNode convert(RelNode rel)
        {
            final UnionRel unionRel = (UnionRel) rel;
            if (!unionRel.isHomogeneous()) {
                return null;
            }
            return super.convert(rel);
        }
    }

    public static class OneRowToIteratorRule
        extends ConverterRule
    {
        public static final OneRowToIteratorRule instance =
            new OneRowToIteratorRule();

        /**
         * Creates a OneRowToIteratorRule.
         */
        private OneRowToIteratorRule()
        {
            super(
                OneRowRel.class,
                CallingConvention.NONE,
                CallingConvention.ITERATOR,
                "OneRowToIteratorRule");
        }

        public RelNode convert(RelNode rel)
        {
            final OneRowRel oneRow = (OneRowRel) rel;
            return new IterOneRowRel(oneRow.getCluster());
        }
    }

    /**
     * Rule to convert a {@link CalcRel} to an {@link IterCalcRel}.
     */
    public static class IterCalcRule
        extends ConverterRule
    {
        public static final IterCalcRule instance = new IterCalcRule();

        private IterCalcRule()
        {
            super(
                CalcRel.class,
                CallingConvention.NONE,
                CallingConvention.ITERATOR,
                "IterCalcRule");
        }

        public RelNode convert(RelNode rel)
        {
            final CalcRel calc = (CalcRel) rel;
            final RelNode convertedChild =
                mergeTraitsAndConvert(
                    calc.getTraits(),
                    CallingConvention.ITERATOR,
                    calc.getChild());
            if (convertedChild == null) {
                // We can't convert the child, so we can't convert rel.
                return null;
            }

            // If there's a multiset, let FarragoMultisetSplitter work on it
            // first.
            if (RexMultisetUtil.containsMultiset(calc.getProgram())) {
                return null;
            }

            // REVIEW: want to move canTranslate into RelImplementor
            // and implement it for Java & C++ calcs.
            final JavaRelImplementor relImplementor =
                rel.getCluster().getPlanner().getJavaRelImplementor(rel);
            if (!relImplementor.canTranslate(
                    convertedChild,
                    calc.getProgram()))
            {
                // Some of the expressions cannot be translated into Java
                return null;
            }

            return new IterCalcRel(
                rel.getCluster(),
                convertedChild,
                calc.getProgram(),
                ProjectRelBase.Flags.Boxed);
        }
    }
}

// End IterRules.java
