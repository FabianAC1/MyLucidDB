/*
// $Id$
// Farrago is an extensible data management system.
// Copyright (C) 2005-2005 LucidEra, Inc.
// Copyright (C) 2005-2005 The Eigenbase Project
// Portions Copyright (C) 2003-2005 John V. Sichi
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
package com.lucidera.farrago.namespace.flatfile;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

import net.sf.farrago.namespace.*;
import net.sf.farrago.namespace.impl.*;

import org.eigenbase.rel.*;
import org.eigenbase.relopt.*;
import org.eigenbase.reltype.*;
import org.eigenbase.util.*;


/**
 * FlatFileColumnSet provides a flatfile implementation of the {@link
 * FarragoMedColumnSet} interface.
 *
 * @author John Pham
 * @version $Id$
 */
class FlatFileColumnSet extends MedAbstractColumnSet
{
    public static final String PROP_FILENAME = "FILENAME";
    public static final String PROP_LOG_FILENAME = "LOG_FILENAME";
    
    private static final String TIMESTAMP_PREFIX = "_";
    private static final String TIMESTAMP_FORMAT = "yyyyMMddHHmmss";
    
    //~ Instance fields -------------------------------------------------------

    FlatFileParams params;
    String filePath;
    String logFilePath;

    //~ Constructors ----------------------------------------------------------

    FlatFileColumnSet(
        String [] localName,
        RelDataType rowType,
        FlatFileParams params,
        Properties tableProps)
    {
        super(localName, null, rowType, null, null);
        this.params = params;
        filePath = makeFilePath(
            localName,
            tableProps.getProperty(PROP_FILENAME, null));
        logFilePath = makeLogFilePath(
            tableProps.getProperty(PROP_LOG_FILENAME, null));
    }

    //~ Methods ---------------------------------------------------------------

    public FlatFileParams getParams() 
    {
        return params;
    }

    public String getFilePath()
    {
        return filePath;
    }
        
    public String getLogFilePath()
    {
        return logFilePath;
    }
    
    // implement RelOptTable
    public RelNode toRel(
        RelOptCluster cluster,
        RelOptConnection connection)
    {
        return new FlatFileFennelRel(this, cluster, connection);
    }

    /**
     * Constructs the full path to the file for a table, based 
     * upon the server directory, filename option (if specified),
     * and the server data file extension. If the filename is not 
     * specified, the local table name is used instead.
     * 
     * @param localName name of the table within the catalog
     * @param filename name of the file, specified in parameters
     * @return full path to the data file for the table
     */
    private String makeFilePath(String[] localName, String filename)
    {
        String name = filename;
        if (name == null) {
            name = localName[localName.length-1];
        }
        String extension = params.getFileExtenstion();
        return (params.getDirectory() + name + extension);
    }
    
    /**
     * Constructs the full path to the log file for a table.
     * The path is constructed from the server's log directory option,
     * and the table's log filename option. If the log directory is not 
     * specified, then the current directory is used. If the log filename 
     * is not specified, then the log filename will be based upon the 
     * table's filename.
     * 
     * <p>
     * 
     * Log files names are appended with a timestamp and have a .ERR 
     * extension rather than the data file extension.
     */
    private String makeLogFilePath(String logFilename) 
    {
        String name = logFilename;
        if (name == null) {
            // NOTE: file path must be set before calling this function
            Util.pre(filePath != null, "filePath != null");
            File file = new File(filePath);                // DIR/FILE.EXT
            String root = file.getName();                  // FILE.EXT
            int dot = root.lastIndexOf(FlatFileParams.FILE_EXTENSION_PREFIX);
            if (dot > 0) {
                root = root.substring(0, dot);             // FILE
            }
            SimpleDateFormat formatter = new SimpleDateFormat(TIMESTAMP_FORMAT);
            String timeStamp = formatter.format(new java.util.Date());
            name = (root + TIMESTAMP_PREFIX + timeStamp 
                + FlatFileParams.FILE_EXTENSION_PREFIX 
                + FlatFileParams.LOG_FILE_EXTENSION);
        }
        return params.getLogDirectory() + name;
    }
}


// End FlatFileColumnSet.java