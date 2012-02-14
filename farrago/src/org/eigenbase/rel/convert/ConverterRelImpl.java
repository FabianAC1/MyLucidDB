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
package org.eigenbase.rel.convert;

import org.eigenbase.rel.*;
import org.eigenbase.rel.metadata.*;
import org.eigenbase.relopt.*;
import org.eigenbase.util.*;


/**
 * Abstract implementation of {@link org.eigenbase.rel.convert.ConverterRel}.
 *
 * @author jhyde
 * @version $Id$
 */
public abstract class ConverterRelImpl
    extends SingleRel
    implements ConverterRel
{
    //~ Instance fields --------------------------------------------------------

    protected RelTraitSet inTraits;
    protected final RelTraitDef traitDef;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a ConverterRelImpl.
     *
     * @param cluster planner's cluster
     * @param traitDef the RelTraitDef this converter converts
     * @param traits the output traits of this converter
     * @param child child rel (provides input traits)
     */
    protected ConverterRelImpl(
        RelOptCluster cluster,
        RelTraitDef traitDef,
        RelTraitSet traits,
        RelNode child)
    {
        super(cluster, traits, child);
        this.inTraits = child.getTraits();
        this.traitDef = traitDef;
    }

    //~ Methods ----------------------------------------------------------------

    // implement RelNode
    public RelOptCost computeSelfCost(RelOptPlanner planner)
    {
        double dRows = RelMetadataQuery.getRowCount(getChild());
        double dCpu = dRows;
        double dIo = 0;
        return planner.makeCost(dRows, dCpu, dIo);
    }

    protected Error cannotImplement()
    {
        return Util.newInternal(
            getClass() + " cannot convert from "
            + inTraits + " traits");
    }

    public boolean isDistinct()
    {
        return getChild().isDistinct();
    }

    protected CallingConvention getInputConvention()
    {
        return (CallingConvention) inTraits.getTrait(
            CallingConventionTraitDef.instance);
    }

    public RelTraitSet getInputTraits()
    {
        return inTraits;
    }

    public RelTraitDef getTraitDef()
    {
        return traitDef;
    }

    /**
     * Returns a new trait set based on <code>traits</code>, with a different
     * trait for a given type of trait. Clones <code>traits</code>, and then
     * replaces the existing trait matching <code>trait.getTraitDef()</code>
     * with <code>trait</code>.
     *
     * @param traits the set of traits to convert
     * @param trait the converted trait
     *
     * @return a new RelTraitSet
     */
    protected static RelTraitSet convertTraits(
        RelTraitSet traits,
        RelTrait trait)
    {
        RelTraitSet converted = RelOptUtil.clone(traits);

        converted.setTrait(
            trait.getTraitDef(),
            trait);

        return converted;
    }
}

// End ConverterRelImpl.java
