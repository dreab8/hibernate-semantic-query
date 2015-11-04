/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.query.parser;

import org.hibernate.sqm.query.QueryException;

/**
 * The root exception for errors (potential bugs) in the query parser code itself, as opposed
 * to {@link QueryException} which indicates problems with the query.
 *
 * @author Steve Ebersole
 */
public class ParsingException extends RuntimeException {
	public ParsingException(String message) {
		super( message );
	}

	public ParsingException(String message, Throwable cause) {
		super( message, cause );
	}
}
