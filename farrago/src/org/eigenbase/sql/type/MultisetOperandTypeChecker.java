/*
// $Id$
// Package org.eigenbase is a class library of data management components.
// Copyright (C) 2005-2005 The Eigenbase Project
// Copyright (C) 2005-2005 Disruptive Tech
// Copyright (C) 2005-2005 LucidEra, Inc.
//
// This program is free software; you can redistribute it and/or modify it
// under the terms of the GNU General Public License as published by the Free
// Software Foundation; either version 2 of the License, or (at your option)
// any later version approved by The Eigenbase Project.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/
package org.eigenbase.sql.type;

import org.eigenbase.resource.*;
import org.eigenbase.sql.*;
import org.eigenbase.sql.validate.*;
import org.eigenbase.reltype.*;
import org.eigenbase.util.*;

import java.util.*;

/**
 * Parameter type-checking strategy
 * types must be
 * [nullable] Multiset, [nullable] Multiset
 * and the two types must have the same element type
 * @see {@link MultisetSqlType#getComponentType}
 *
 * @author Wael Chatila
 * @version $Id$
 */
public class MultisetOperandTypeChecker implements SqlOperandTypeChecker
{
    public boolean check(
        SqlCall call,
        SqlValidator validator,
        SqlValidatorScope scope,
        SqlNode node,
        int ruleOrdinal,
        boolean throwOnFailure)
    {
        throw Util.needToImplement(this);
    }

    public boolean check(
        SqlValidator validator,
        SqlValidatorScope scope,
        SqlCall call,
        boolean throwOnFailure)
    {
        SqlNode op0 = call.operands[0];
        if(!SqlTypeStrategies.otcNullableMultiset.check(
               call, validator, scope,
               op0, 0, throwOnFailure)) {
            return false;
        }

        SqlNode op1 = call.operands[1];
        if (!SqlTypeStrategies.otcNullableMultiset.check(
                call, validator, scope,
                op1, 0, throwOnFailure)) {
            return false;
        }

        RelDataType[] argTypes = new RelDataType[2];
        argTypes[0] = validator.deriveType(scope, op0).getComponentType();
        argTypes[1] = validator.deriveType(scope, op1).getComponentType();
        //TODO this wont work if element types are of ROW types and there is a
        //mismatch.
        RelDataType biggest = SqlTypeUtil.getNullableBiggest(
            validator.getTypeFactory(), argTypes);
        if (null==biggest) {
            if (throwOnFailure) {
                throw EigenbaseResource.instance().newTypeNotComparable(
                    call.operands[0].getParserPosition().toString(),
                    call.operands[1].getParserPosition().toString());
            }

            return false;
        }
        return true;
    }

    public int getArgCount()
    {
        return 2;
    }

    public String getAllowedSignatures(SqlOperator op)
    {
        return "<MULTISET> "+op.getName()+" <MULTISET>";
    }
}

// End MultisetOperandTypeChecker.java