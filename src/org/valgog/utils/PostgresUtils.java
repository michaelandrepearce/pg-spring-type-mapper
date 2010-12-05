/**
 * 
 */
package org.valgog.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.valgog.utils.exceptions.ArrayParserException;
import org.valgog.utils.exceptions.RowParserException;

/**
 * @author valgog
 * 
 */
public class PostgresUtils {

	public static final List<String> postgresArray2StringList(String value)
			throws ArrayParserException {
		return postgresArray2StringList(value, 16);
	}

	public static final List<String> postgresArray2StringList(String value,
			int appendStringSize) throws ArrayParserException {
		if (!(value.startsWith("{") && value.endsWith("}")))
			throw new ArrayParserException(
					String
							.format(
									"postgresArray2StringList() ARRAY must begin with '{' and ends with '}': %s",
									value));
		if (value.length() == 2) {
			return Collections.emptyList();
		}
		// This is a simple copy-paste from the ROW processing code, and
		// strictly speaking is not quite correct for PostgreSQL ARRAYs
		List<String> result = new ArrayList<String>();

		char[] c = value.toCharArray();

		StringBuilder element = new StringBuilder(appendStringSize);
		// this processor will fail if value has spaces between ',' and '"' or
		// ')'
		int i = 1;
		while (c[i] != '}') {
			if (c[i] == ',') {
				char nextChar = c[i + 1];
				if (nextChar == ',' || nextChar == '}') {
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
						if (nextChar == ',' || nextChar == '}') {
							result.add(element.toString());
							element = new StringBuilder(appendStringSize);
							insideQuote = false;
						} else if (nextChar == '\"') {
							i++;
							element.append(c[i]);
						} else {
							throw new ArrayParserException(
									"postgresArray2StringList() char after \" is not valid");
						}
					} else if (c[i] == '\\') {
						if (nextChar == '\\' || nextChar == '\"') {
							i++;
							element.append(c[i]);
						} else {
							throw new ArrayParserException(
									"postgresArray2StringList() char after \\ is not valid");
						}
					} else {
						element.append(c[i]);
					}
					i++;
				}
			} else {
				while (!(c[i] == ',' || c[i] == '}')) {
					element.append(c[i]);
					i++;
				}
				// if the element was not quoted and was empty, it is supposed
				// to be NULL
				result.add(element.length() > 0 ? element.toString() : null);
				element = new StringBuilder(appendStringSize);
			}
		}
		return result;
	}

	public static final List<String> postgresROW2StringList(String value)
			throws RowParserException {
		return postgresROW2StringList(value, 16);
	}

	public static final List<String> postgresROW2StringList(String value,
			int appendStringSize) throws RowParserException {
		if (!(value.startsWith("(") && value.endsWith(")")))
			throw new RowParserException(
					"postgresROW2StringList() ROW must begin with '(' and ends with ')': "
							+ value);

		List<String> result = new ArrayList<String>();

		char[] c = value.toCharArray();

		StringBuilder element = new StringBuilder(appendStringSize);
		// this processor will fail if value has spaces between ',' and '"' or
		// ')'
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
							throw new RowParserException(
									"postgresROW2StringList() char after \" is not valid");
						}
					} else if (c[i] == '\\') {
						if (nextChar == '\\' || nextChar == '\"') {
							i++;
							element.append(c[i]);
						} else {
							throw new RowParserException(
									"postgresROW2StringList() char after \\ is not valid");
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
				// if the element was not quoted and was empty, it is supposed
				// to be NULL
				result.add(element.length() > 0 ? element.toString() : null);
				element = new StringBuilder(appendStringSize);
			}
		}
		return result;
	}
	
}
