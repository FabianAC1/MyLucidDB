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
import org.eigenbase.sql.type.*;


/**
 * <code>OneRowRel</code> always returns one row, one column (containing the
 * value 0).
 *
 * @author jhyde
 * @version $Id$
 * @since 23 September, 2001
 */
public final class OneRowRel
    extends OneRowRelBase
{
    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a <code>OneRowRel</code>.
     *
     * @param cluster {@link RelOptCluster}  this relational expression belongs
     * to
     */
    public OneRowRel(RelOptCluster cluster)
    {
        super(
            cluster,
            new RelTraitSet(CallingConvention.NONE));
    }
}

// End OneRowRel.java
