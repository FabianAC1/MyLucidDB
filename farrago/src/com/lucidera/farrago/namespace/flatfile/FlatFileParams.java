/*
// $Id$
// Farrago is an extensible data management system.
// Copyright (C) 2005-2005 LucidEra, Inc.
// Copyright (C) 2005-2005 The Eigenbase Project
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
import java.sql.*;
import java.util.Properties;

import org.eigenbase.util.*;

import net.sf.farrago.namespace.impl.*;

/**
 * Decodes options used for a flat file data server. An instance 
 * of this class must be initialized with <code>decode()</code> 
 * before calling its other methods. The conventions for 
 * parameters are as follows:
 *
 * <ul>
 *   <li>Boolean and integer properties use the default behavior</li>
 *   <li>Delimiter characters are translated to canonical form</li>
 *   <li>Special characters are read as single characters</li>
 *   <li>Directories become empty string or end with the File.separator</li>
 *   <li>File name extensions become empty string or begin with a prefix</li>
 * </ul>
 *
 * @author John Pham
 * @version $Id$
 */
class FlatFileParams extends MedAbstractBase
{
    //~ Static fields/initializers --------------------------------------------

    public static final String PROP_DIRECTORY = "DIRECTORY";
    public static final String PROP_FILE_EXTENSION = "FILE_EXTENSION";
    public static final String PROP_CONTROL_FILE_EXTENSION =
        "CTRL_FILE_EXTENSION";
    public static final String PROP_FIELD_DELIMITER = "FIELD_DELIMITER";
    public static final String PROP_LINE_DELIMITER = "LINE_DELIMITER";
    public static final String PROP_QUOTE_CHAR = "QUOTE_CHAR";
    public static final String PROP_ESCAPE_CHAR = "ESCAPE_CHAR";
    public static final String PROP_WITH_HEADER = "WITH_HEADER";
    public static final String PROP_NUM_ROWS_SCAN = "NUM_ROWS_SCAN";
    public static final String PROP_WITH_LOGGING = "WITH_LOGGING";
    public static final String PROP_LOG_DIRECTORY = "LOG_DIRECTORY";

    public static final String FILE_EXTENSION_PREFIX = ".";
    public static final String LOG_FILE_EXTENSION = "err";

    private static final String DEFAULT_FILE_EXTENSION = "txt";
    private static final String DEFAULT_CONTROL_FILE_EXTENSION = "bcp";
    private static final String DEFAULT_FIELD_DELIMITER = ",";
    private static final String DEFAULT_LINE_DELIMITER = "\\n";
    private static final String DEFAULT_QUOTE_CHAR = "\"";
    private static final String DEFAULT_ESCAPE_CHAR = "\"";
    private static final boolean DEFAULT_WITH_HEADER = true;
    private static final int DEFAULT_NUM_ROWS_SCAN = 5;
    private static final boolean DEFAULT_WITH_LOGGING = true;
    
    //~ Instance fields -------------------------------------------------------

    private Properties props;
    private String directory, logDirectory;
    private String fileExtension, controlFileExtension;
    private char fieldDelimiter, lineDelimiter;
    private char quoteChar, escapeChar;
    private boolean withHeader, withLogging;
    private int numRowsScan;
    
    //~ Constructors ----------------------------------------------------------

    /**
     * Constructs an uninitialized FlatFileParams. The method 
     * <code>decode()</code> must be called before the object is used.
     * 
     * @param props foreign server parameters
     */
    public FlatFileParams(Properties props) 
    {
        this.props = props;
    }

    /**
     * The main entry point into paremter decoding. Throws an 
     * exception when there are error parsing parameters.
     * 
     * @throws SQLException
     */
    public void decode()
        throws SQLException
    {
        directory = decodeDirectory(
            props.getProperty(PROP_DIRECTORY, null));
        fileExtension = decodeExtension(props.getProperty(
            PROP_FILE_EXTENSION,
            DEFAULT_FILE_EXTENSION));
        controlFileExtension = decodeExtension(props.getProperty(
            PROP_CONTROL_FILE_EXTENSION, 
            DEFAULT_CONTROL_FILE_EXTENSION));
        fieldDelimiter = decodeDelimiter(props.getProperty(
            PROP_FIELD_DELIMITER, 
            DEFAULT_FIELD_DELIMITER));
        lineDelimiter = decodeDelimiter(props.getProperty(
            PROP_LINE_DELIMITER, 
            DEFAULT_LINE_DELIMITER));
        quoteChar = decodeSpecialChar(
            props.getProperty(PROP_QUOTE_CHAR, DEFAULT_QUOTE_CHAR),
            DEFAULT_QUOTE_CHAR);
        escapeChar = decodeSpecialChar(
            props.getProperty(PROP_ESCAPE_CHAR, DEFAULT_ESCAPE_CHAR),
            DEFAULT_ESCAPE_CHAR);
        withHeader = getBooleanProperty(
            props, PROP_WITH_HEADER, DEFAULT_WITH_HEADER);
        withLogging = getBooleanProperty(
            props, PROP_WITH_LOGGING, DEFAULT_WITH_LOGGING);
        numRowsScan = getIntProperty(
            props, PROP_NUM_ROWS_SCAN, DEFAULT_NUM_ROWS_SCAN);
        logDirectory = decodeDirectory(
            props.getProperty(PROP_LOG_DIRECTORY, null));
    }
    
    /**
     * Decodes a delimiter string into a canonical delimiter character. 
     * This method recognizes the escape sequences \t, \r, \n. 
     * Combinations of the two line characters \r and \n are all 
     * reduced to universal line character \n.
     *
     * <p>
     * 
     * This function comes from legacy code and is based on odd 
     * heuristics. 
     * 
     * <ul>
     *   <li>An initial double quote denotes no delimiter.</li>
     *   <li>A tab escape "\t" becomes a tab.</li>
     *   <li>Otherwise preference is given to the line characters 
     *         sequences "\r" and "\n". These escapes are recognized 
     *         from the 0, 1, and 2 index positions.</li>
     *   <li>The backslash character cannot escape any other 
     *         character, and itself becomes a delimiter.</li>
     *   <li>Any other character becomes a delimiter.</li>
     * </ul>
     *
     * <p>REVIEW: this behavior seems overly complex and awkward
     * 
     * @param delim delimiter string
     * 
     * @return canonical delimiter character represented by delimiter string
     */
    private char decodeDelimiter(String delim)
    {
        if (delim == null || delim.length() == 0 || delim.charAt(0) == '"') {
            return 0;
        }
        if (delim.indexOf("\\t") == 0) {
            return '\t';
        }
        int a = delim.indexOf("\\r");
        int b = delim.indexOf("\\n");
        if (a == 0 || a == 1 || a == 2 || b == 0 || b == 1 || b == 2) {
            return '\n';
        }
        return delim.charAt(0);
    }

    /**
     * Decodes a quote or escape character
     * 
     * @param specialChar string containing special character, may be empty
     * @param defaultChar default string, must not be empty
     * 
     * @return the first character of specialChar or defaultChar
     */
    private char decodeSpecialChar(String specialChar, String defaultChar)
    {
        if (specialChar == null || specialChar.length() == 0) {
            Util.pre(defaultChar.length() > 0,
                "defaultChar.length() > 0");
            return defaultChar.charAt(0);
        }
        return specialChar.charAt(0);
    }

    /**
     * Decodes a directory name into a useful format. If the name is null, 
     * returns an empty string. Otherwise, ensures the directory name ends 
     * with File.separator.
     * 
     * @param directory directory name, may be null
     * 
     * @return empty string or directory name, ending with File.separator
     */
    private String decodeDirectory(String directory) {
        if (directory == null) {
            return "";
        }
        if (directory.endsWith(File.separator)) {
            return directory;
        }
        return directory + File.separator;
    }

    private String decodeExtension(String extension)
    {
        Util.pre(extension != null, "extension != null");
        if (extension.length() == 0
            || extension.startsWith(FILE_EXTENSION_PREFIX)) 
        {
            return extension;
        }
        return FILE_EXTENSION_PREFIX + extension;
    }

    public String getDirectory() 
    {
        return directory;
    }

    public String getFileExtenstion()
    {
        return fileExtension;
    }
        
    public String getControlFileExtenstion()
    {
        return controlFileExtension;
    }

    public char getFieldDelimiter() 
    {
        return fieldDelimiter;
    }

    public char getLineDelimiter()
    {
        return lineDelimiter;
    }

    public char getQuoteChar() 
    {
        return quoteChar;
    }

    public char getEscapeChar()
    {
        return escapeChar;
    }
    
    public boolean getWithHeader() 
    {
        return withHeader;
    }

    public int getNumRowsScan()
    {
        return numRowsScan;
    }

    public boolean getWithLogging()
    {
        return withLogging;
    }

    public String getLogDirectory() 
    {
        return logDirectory;
    }
}

// End FlatFileParams.java