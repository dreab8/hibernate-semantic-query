/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.query.parser.internal.hql.path;

import org.hibernate.query.parser.internal.hql.antlr.HqlParser;
import org.hibernate.sqm.path.AttributePathPart;
import org.hibernate.sqm.query.SemanticException;
import org.hibernate.query.parser.internal.FromClauseIndex;
import org.hibernate.query.parser.internal.FromElementBuilder;
import org.hibernate.query.parser.internal.ParsingContext;
import org.hibernate.query.parser.internal.hql.phase1.FromClauseStackNode;
import org.hibernate.sqm.domain.AttributeDescriptor;
import org.hibernate.sqm.domain.CollectionTypeDescriptor;
import org.hibernate.sqm.domain.EntityTypeDescriptor;
import org.hibernate.sqm.query.from.FromElement;
import org.hibernate.sqm.query.from.QualifiedJoinedFromElement;

/**
 * @author Steve Ebersole
 */
public class JoinPredicatePathResolverImpl extends BasicAttributePathResolverImpl {
	private final QualifiedJoinedFromElement joinRhs;
	private FromElement joinLhs;

	public JoinPredicatePathResolverImpl(
			FromElementBuilder fromElementBuilder,
			FromClauseIndex fromClauseIndex,
			ParsingContext parsingContext,
			FromClauseStackNode fromClause,
			QualifiedJoinedFromElement joinRhs) {
		super( fromElementBuilder, fromClauseIndex, parsingContext, fromClause );
		this.joinRhs = joinRhs;
	}

	@Override
	@SuppressWarnings("StatementWithEmptyBody")
	protected void validatePathRoot(FromElement root) {
		if ( root == joinRhs ) {
			// nothing to do
		}
		else if ( joinLhs == null ) {
			// assume root is LHS
			joinLhs = root;
		}
		else {
			if ( joinLhs != root ) {
				throw new SemanticException( "Qualified join predicate referred to more than 2 FromElements" );
			}
		}
	}

	@Override
	protected void validateIntermediateAttributeJoin(
			FromElement lhs,
			AttributeDescriptor joinedAttributeDescriptor) {
		if ( joinedAttributeDescriptor.getType() instanceof EntityTypeDescriptor ) {
			throw new SemanticException(
					"On-clause predicate of a qualified join cannot contain implicit entity joins : " +
							joinedAttributeDescriptor.getName()
			);
		}
		else if ( joinedAttributeDescriptor.getType() instanceof CollectionTypeDescriptor ) {
			throw new SemanticException(
					"On-clause predicate of a qualified join cannot contain implicit collection joins : " +
							joinedAttributeDescriptor.getName()
			);
		}

		super.validateIntermediateAttributeJoin( lhs, joinedAttributeDescriptor );
	}
}
