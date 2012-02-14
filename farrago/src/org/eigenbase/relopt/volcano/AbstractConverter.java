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
package org.eigenbase.relopt.volcano;

import org.eigenbase.rel.*;
import org.eigenbase.rel.convert.*;
import org.eigenbase.relopt.*;


/**
 * Converts a relational expression to any given output convention.
 *
 * <p>Unlike most {@link ConverterRel}s, an abstract converter is always
 * abstract. You would typically create an <code>AbstractConverter</code> when
 * it is necessary to transform a relational expression immediately; later,
 * rules will transform it into relational expressions which can be implemented.
 * </p>
 *
 * <p>If an abstract converter cannot be satisfied immediately (because the
 * source subset is abstract), the set is flagged, so this converter will be
 * expanded as soon as a non-abstract relexp is added to the set.</p>
 */
public class AbstractConverter
    extends ConverterRelImpl
{
    //~ Constructors -----------------------------------------------------------

    public AbstractConverter(
        RelOptCluster cluster,
        RelNode rel,
        CallingConvention outConvention)
    {
        this(
            cluster,
            rel,
            outConvention.getTraitDef(),
            new RelTraitSet(outConvention));
    }

    public AbstractConverter(
        RelOptCluster cluster,
        RelNode rel,
        RelTraitDef traitDef,
        RelTraitSet traits)
    {
        super(cluster, traitDef, traits, rel);
    }

    //~ Methods ----------------------------------------------------------------

    public AbstractConverter clone()
    {
        return new AbstractConverter(
            getCluster(),
            getChild(),
            traitDef,
            cloneTraits());
    }

    public RelOptCost computeSelfCost(RelOptPlanner planner)
    {
        return planner.makeInfiniteCost();
    }

    public void explain(RelOptPlanWriter pw)
    {
        String [] terms = new String[traits.size() + 1];
        Object [] values = new Object[traits.size()];
        terms[0] = "child";
        for (int i = 0; i < traits.size(); i++) {
            terms[i + 1] = traits.getTrait(i).getTraitDef().getSimpleName();
            values[i] = traits.getTrait(i);
        }

        pw.explain(this, terms, values);
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * Rule which converts an {@link AbstractConverter} into a chain of
     * converters from the source relation to the target traits.
     *
     * <p>The chain produced is mimimal: we have previously built the transitive
     * closure of the graph of conversions, so we choose the shortest chain.</p>
     *
     * <p>Unlike the {@link AbstractConverter} they are replacing, these
     * converters are guaranteed to be able to convert any relation of their
     * calling convention. Furthermore, because they introduce subsets of other
     * calling conventions along the way, these subsets may spawn more efficient
     * conversions which are not generally applicable.</p>
     *
     * <p>AbstractConverters can be messy, so they restrain themselves: they
     * don't fire if the target subset already has an implementation (with less
     * than infinite cost).</p>
     */
    public static class ExpandConversionRule
        extends RelOptRule
    {
        public static final ExpandConversionRule instance =
            new ExpandConversionRule();

        /**
         * Creates an ExpandConversionRule.
         */
        private ExpandConversionRule()
        {
            super(
                new RelOptRuleOperand(
                    AbstractConverter.class,
                    ANY));
        }

        public void onMatch(RelOptRuleCall call)
        {
            final VolcanoPlanner planner = (VolcanoPlanner) call.getPlanner();
            AbstractConverter converter = (AbstractConverter) call.rels[0];
            final RelSubset converterSubset = planner.getSubset(converter);
            final RelNode child = converter.getChild();
            RelNode converted =
                planner.changeTraitsUsingConverters(
                    child,
                    converter.traits);
            if (converted != null) {
                call.transformTo(converted);
                return;
            }
            if (true) {
                return;
            }

            // Since we couldn't convert directly, create abstract converters to
            // all sibling subsets. This will cause them to be important, and
            // hence rules will fire which may generate the conversion we need.
            final RelSet set = planner.getSet(child);
            for (RelSubset subset : set.subsets) {
                if (subset.getTraits().equals(child.getTraits())
                    || subset.getTraits().equals(converter.traits))
                {
                    continue;
                }
                final AbstractConverter newConverter =
                    new AbstractConverter(
                        child.getCluster(),
                        subset,
                        converter.traitDef,
                        converter.traits);
                call.transformTo(newConverter);
            }
        }
    }
}

// End AbstractConverter.java
