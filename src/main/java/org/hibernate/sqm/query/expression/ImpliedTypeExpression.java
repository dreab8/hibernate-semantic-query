/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.expression;

import org.hibernate.sqm.domain.Type;

/**
 * Extension for Expressions whose Type can be implied from their surroundings.
 *
 * @author Steve Ebersole
 */
public interface ImpliedTypeExpression extends Expression {
	/**
	 * Used to inject the Type implied by the expression's context.
	 *
	 * @param type The implied type.
	 */
	void impliedType(Type type);
}
