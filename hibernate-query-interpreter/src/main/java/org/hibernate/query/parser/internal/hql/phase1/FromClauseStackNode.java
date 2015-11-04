/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.query.parser.internal.hql.phase1;

import org.hibernate.query.parser.internal.FromClauseIndex;
import org.hibernate.sqm.query.from.FromClause;

/**
 * @author Andrea Boriero
 * @author Steve Ebersole
 */
public class FromClauseStackNode {
	private FromClause fromClause;
	private FromClauseIndex fromClauseIndex;

	private FromClauseStackNode parentNode;

	public FromClauseStackNode(FromClause fromClause, FromClauseIndex fromClauseIndex) {
		this.fromClause = fromClause;
		this.fromClauseIndex = fromClauseIndex;
	}

	public FromClauseStackNode(FromClause fromClause, FromClauseStackNode parentNode) {
		this.fromClauseIndex = new FromClauseIndex( parentNode.getFromClauseIndex() );
		this.fromClause = fromClause;
		this.parentNode = parentNode;
	}

	public FromClause getFromClause() {
		return fromClause;
	}

	public FromClauseStackNode getParentNode() {
		return parentNode;
	}

	public boolean hasParent() {
		return parentNode != null;
	}

	public FromClauseIndex getFromClauseIndex(){
		return fromClauseIndex;
	}
}
