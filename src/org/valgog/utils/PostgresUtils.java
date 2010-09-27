/**
 * 
 */
package org.valgog.utils;

import java.util.ArrayList;
import java.util.List;

import org.valgog.utils.RowParserException;

/**
 * @author valgog
 *
 */
public class PostgresUtils {
	
	public static final List<String> postgresROW2StringList(String value, int appendStringSize) 
	throws RowParserException
	{
		if (!(value.startsWith("(") && value.endsWith(")")))
			throw new RowParserException("postgresROW2StringList() ROW must begin with '(' and ends with ')': " + value);
		
		List<String> result = new ArrayList<String>();
		
		char[] c = value.toCharArray();
		
		StringBuilder element = new StringBuilder(appendStringSize);
		int i = 1;
		while (c[i] != ')')
		{
			if (c[i] == ',')
			{
				if (c[i+1] == ',')
				{
					result.add(new String());
				}else if (c[i+1] == ')')
				{
					result.add(new String());
				}
				i++;
			}else if (c[i] == '\"')
			{
				i++;
				boolean insideQuote = true;
				while(insideQuote)
				{
					char nextChar = c[i + 1];
					if(c[i] == '\"')
					{
						if (nextChar == ',' || nextChar == ')')
						{
							result.add(element.toString());
							element = new StringBuilder(appendStringSize);
							insideQuote = false;
						}else if(nextChar == '\"')
						{
							i++;
							element.append(c[i]);
						}else
						{
							throw new RowParserException("postgresROW2StringList() char after \" is not valid");
						}
					}else if (c[i] == '\\')
					{
						if(nextChar == '\\' || nextChar == '\"')
						{
							i++;
							element.append(c[i]);
						}else
						{
							throw new RowParserException("postgresROW2StringList() char after \\ is not valid");
						}
					}else
					{
						element.append(c[i]);
					}
					i++;
				}
			}else
			{
				while(!(c[i] == ',' || c[i] == ')'))
				{
					element.append(c[i]);
					i++;
					
				}
				result.add(element.toString());
				element = new StringBuilder(appendStringSize);
			}
		}
		return result;
	}

}
