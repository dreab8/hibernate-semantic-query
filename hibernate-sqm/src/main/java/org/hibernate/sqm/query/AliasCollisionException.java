/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query;

import org.hibernate.sqm.query.SemanticException;

/**
 * @author Andrea Boriero
 */
public class AliasCollisionException extends SemanticException {
	public AliasCollisionException(String message) {
		super( message );
	}

	public AliasCollisionException(String message, Throwable cause) {
		super( message, cause );
	}
}
