/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.query.parser.internal.hql.path;

import org.jboss.logging.Logger;

import org.hibernate.query.parser.internal.FromClauseIndex;
import org.hibernate.query.parser.internal.FromElementBuilder;
import org.hibernate.query.parser.internal.ParsingContext;
import org.hibernate.query.parser.internal.hql.phase1.FromClauseStackNode;
import org.hibernate.sqm.domain.AttributeDescriptor;
import org.hibernate.sqm.path.AttributePathPart;
import org.hibernate.sqm.query.expression.AttributeReferenceExpression;
import org.hibernate.sqm.query.expression.FromElementReferenceExpression;
import org.hibernate.sqm.query.from.FromElement;

/**
 * @author Steve Ebersole
 */
public class BasicAttributePathResolverImpl extends StandardAttributePathResolverTemplate {
	private static final Logger log = Logger.getLogger( BasicAttributePathResolverImpl.class );

	private final FromElementBuilder fromElementBuilder;
	private final FromClauseIndex fromClauseIndex;
	private final FromClauseStackNode fromClause;
	private final ParsingContext parsingContext;

	public BasicAttributePathResolverImpl(
			FromElementBuilder fromElementBuilder,
			FromClauseIndex fromClauseIndex,
			ParsingContext parsingContext,
			FromClauseStackNode fromClause) {
		this.fromElementBuilder = fromElementBuilder;
		this.fromClauseIndex = fromClauseIndex;
		this.fromClause = fromClause;
		this.parsingContext = parsingContext;
	}

	@Override
	protected FromElementBuilder fromElementBuilder() {
		return fromElementBuilder;
	}

	@Override
	protected ParsingContext parsingContext() {
		return parsingContext;
	}

	@Override
	protected FromClauseIndex getFromClauseIdeIndex() {
		return fromClauseIndex;
	}

	protected void validatePathRoot(FromElement root) {
	}

	protected FromElement findFromElementByAlias(String alias) {
		return fromClauseIndex.findFromElementByAlias( alias );
	}

	protected FromElement findFromElementWithAttribute(String attributeName) {
		return fromClauseIndex.findFromElementWithAttribute( fromClause, attributeName );
	}

	protected AttributePathPart resolveTerminalPathPart(FromElement lhs, String terminalName) {
		final AttributeDescriptor attributeDescriptor = lhs.getTypeDescriptor().getAttributeDescriptor( terminalName );
		final AttributeReferenceExpression expr = new AttributeReferenceExpression( lhs, attributeDescriptor );
		log.debugf( "Resolved terminal path-part [%s] : %s", terminalName, expr );
		return expr;
	}

	protected AttributePathPart resolveFromElementAliasAsTerminal(FromElement aliasedFromElement) {
		log.debugf( "Resolved from-element alias as terminal : %s", aliasedFromElement.getAlias() );
		return new FromElementReferenceExpression( aliasedFromElement );
	}
}
