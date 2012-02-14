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
package org.eigenbase.sql.fun;

import openjava.mop.*;

import org.eigenbase.reltype.*;
import org.eigenbase.sql.*;
import org.eigenbase.sql.type.*;


/**
 * <code>FIRST_VALUE</code> and <code>LAST_VALUE</code> aggregate functions
 * return the first or the last value in a list of values that are input to the
 * function.
 */
public class SqlFirstLastValueAggFunction
    extends SqlAggFunction
{
    //~ Static fields/initializers ---------------------------------------------

    public static final RelDataType type = null; // TODO:

    //~ Constructors -----------------------------------------------------------

    public SqlFirstLastValueAggFunction(boolean firstFlag)
    {
        super(
            (firstFlag) ? "FIRST_VALUE" : "LAST_VALUE",
            SqlKind.OTHER_FUNCTION,
            SqlTypeStrategies.rtiFirstArgType,
            null,
            SqlTypeStrategies.otcAny,
            SqlFunctionCategory.Numeric);
    }

    //~ Methods ----------------------------------------------------------------

    public RelDataType [] getParameterTypes(RelDataTypeFactory typeFactory)
    {
        return new RelDataType[] { type };
    }

    public RelDataType getReturnType(RelDataTypeFactory typeFactory)
    {
        return type;
    }

    public OJClass [] getStartParameterTypes()
    {
        return new OJClass[0];
    }
}

// End SqlFirstLastValueAggFunction.java
