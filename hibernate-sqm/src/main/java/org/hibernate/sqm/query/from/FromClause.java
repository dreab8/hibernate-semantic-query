/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.from;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.sqm.query.AliasCollisionException;
import org.hibernate.sqm.query.expression.Expression;
import org.hibernate.sqm.query.select.SelectClause;
import org.hibernate.sqm.query.select.Selection;

/**
 * Contract representing a from clause.
 * <p>
 * The parent/child bit represents sub-queries.  The child from clauses are only used for test assertions,
 * but are left here as it is most convenient to maintain them here versus another structure.
 *
 * @author Steve Ebersole
 */
public class FromClause {
	private List<FromElementSpace> fromElementSpaces = new ArrayList<FromElementSpace>();

	public List<FromElementSpace> getFromElementSpaces() {
		return fromElementSpaces;
	}

	public void addFromElementSpace(FromElementSpace space) {

	}

	public FromElementSpace makeFromElementSpace() {
		final FromElementSpace space = new FromElementSpace( this );
		fromElementSpaces.add( space );
		return space;
	}

	public void checkResultVariableConflict(SelectClause selectClause) {
		for ( Selection selection : selectClause.getSelections() ) {
			checkResultVariableConflict( selection );
		}
	}

	private void checkResultVariableConflict(Selection selectClause) {
		final String alias = selectClause.getAlias();
		for ( FromElementSpace fromElementSpace : fromElementSpaces ) {
			if ( alias != null && alias.equals( fromElementSpace.getRoot().getAlias() ) ) {
				if ( !selectClause.getExpression().getTypeDescriptor().equals(
						fromElementSpace.getRoot()
								.getTypeDescriptor()
				) ) {
					throw new AliasCollisionException(
							"In Select clause is used the alias " + alias + " defined in From clause but referring a different element"
					);
				}
			}
		}
	}
}
