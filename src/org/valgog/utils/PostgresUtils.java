/**
 * 
 */
package org.valgog.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.valgog.utils.RowParserException;

/**
 * @author valgog
 * 
 */
public class PostgresUtils {

	public static final List<String> postgresArray2StringList(String value) throws ArrayParserException {
		if (!(value.startsWith("{") && value.endsWith("}")))
			throw new ArrayParserException(String.format("postgresArray2StringList() ARRAY must begin with '{' and ends with '}': %s", value));
		if (value.length() == 2) {
			return Collections.emptyList();
		}
		throw new ArrayParserException("Array parser is not yet implemented");
	}

	public static final List<String> postgresROW2StringList(String value, int appendStringSize) throws RowParserException {
		if (!(value.startsWith("(") && value.endsWith(")")))
			throw new RowParserException("postgresROW2StringList() ROW must begin with '(' and ends with ')': " + value);

		List<String> result = new ArrayList<String>();

		char[] c = value.toCharArray();

		StringBuilder element = new StringBuilder(appendStringSize);
		// this processor will fail if value has spaces between ',' and '"' or ')'
		int i = 1;
		while (c[i] != ')') {
			if (c[i] == ',') {
				char nextChar = c[i + 1];
				if (nextChar == ',' || nextChar == ')') {
					// we have an empty position, that is we have a NULL value
					result.add(null);
				}
				i++;
			} else if (c[i] == '\"') {
				i++;
				boolean insideQuote = true;
				while (insideQuote) {
					char nextChar = c[i + 1];
					if (c[i] == '\"') {
						if (nextChar == ',' || nextChar == ')') {
							result.add(element.toString());
							element = new StringBuilder(appendStringSize);
							insideQuote = false;
						} else if (nextChar == '\"') {
							i++;
							element.append(c[i]);
						} else {
							throw new RowParserException("postgresROW2StringList() char after \" is not valid");
						}
					} else if (c[i] == '\\') {
						if (nextChar == '\\' || nextChar == '\"') {
							i++;
							element.append(c[i]);
						} else {
							throw new RowParserException("postgresROW2StringList() char after \\ is not valid");
						}
					} else {
						element.append(c[i]);
					}
					i++;
				}
			} else {
				while (!(c[i] == ',' || c[i] == ')')) {
					element.append(c[i]);
					i++;
				}
				// if the element was not quoted and was empty, it is supposed to be NULL
				result.add(element.length() > 0 ? element.toString() : null );
				element = new StringBuilder(appendStringSize);
			}
		}
		return result;
	}
	
    public static List<String> getStringList(final String value) throws RowParserException {
        if (value == null) {
            return null;
        }

        String myValue = null;
        if (value.length() > 0) {
            if (value.startsWith("{") && value.endsWith("}")) {
                if (value.length() == 2) {
                    // special case for empty postgres array ("{}")
                    return new ArrayList<String>(0);
                }
                myValue = "(" + value.substring(1, value.length() - 1) + ")";
            } else {
                myValue = value;
            }
        } else {
            return null;
        }
        return postgresROW2StringList(myValue, 0);
    }	
    
    public static List<String> getArrayElements(final String serializedArray) {

        final List<String> elements = new ArrayList<String>();

        int currentPositionInString = 0;

        while (currentPositionInString != -1) {
            currentPositionInString = fetchElement(currentPositionInString, serializedArray,
                    elements);
        }

        return elements;
    }
    
    private static int fetchElement(final int fromIndex, final String serializedArray, final List<String> elements) {

        int n = serializedArray.indexOf('(', fromIndex) + 1;

        // we did not find something...
        if (n == 0) {
            return -1;
        }

        int depth = 0;

        final StringBuffer elementBuffer = new StringBuffer().append('(');

        while (n < serializedArray.length()) {

            final char currentChar = serializedArray.charAt(n);

            elementBuffer.append(currentChar);

            if (currentChar == '(') {
                depth++;
            }
            if (currentChar == ')' && depth == 0) {
                break;
            }
            if (currentChar == ')' && depth != 0) {
                depth--;
            }

            n++;
        }

        elements.add(elementBuffer.toString());

        return n;
    }    

}