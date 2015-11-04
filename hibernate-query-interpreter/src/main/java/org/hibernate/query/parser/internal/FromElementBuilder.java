/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.query.parser.internal;

import org.jboss.logging.Logger;

import org.hibernate.query.parser.ParsingException;
import org.hibernate.sqm.domain.AttributeDescriptor;
import org.hibernate.sqm.domain.EntityTypeDescriptor;
import org.hibernate.sqm.query.JoinType;
import org.hibernate.sqm.query.from.CrossJoinedFromElement;
import org.hibernate.sqm.query.from.FromElement;
import org.hibernate.sqm.query.from.FromElementSpace;
import org.hibernate.sqm.query.from.QualifiedAttributeJoinFromElement;
import org.hibernate.sqm.query.from.RootEntityFromElement;

/**
 * @author Steve Ebersole
 */
public class FromElementBuilder {
	private static final Logger log = Logger.getLogger( FromElementBuilder.class );

	private final ParsingContext parsingContext;

	public FromElementBuilder(ParsingContext parsingContext) {
		this.parsingContext = parsingContext;
	}

	/**
	 * Make the root entity reference for the FromElementSpace
	 *
	 * @param fromElementSpace
	 * @param entityTypeDescriptor
	 * @param alias
	 *
	 * @return
	 */
	public RootEntityFromElement makeRootEntityFromElement(
			FromElementSpace fromElementSpace,
			EntityTypeDescriptor entityTypeDescriptor,
			FromClauseIndex fromClauseIndex,
			String alias) {
		if ( alias == null ) {
			alias = parsingContext.getImplicitAliasGenerator().buildUniqueImplicitAlias();
			log.debugf(
					"Generated implicit alias [%s] for root entity reference [%s]",
					alias,
					entityTypeDescriptor.getTypeName()
			);
		}
		final RootEntityFromElement root = new RootEntityFromElement( fromElementSpace, alias, entityTypeDescriptor );
		fromElementSpace.setRoot( root );
		registerAlias( root, fromClauseIndex );
		return root;
	}


	/**
	 * Make the root entity reference for the FromElementSpace
	 *
	 * @param fromElementSpace
	 * @param entityTypeDescriptor
	 * @param alias
	 *
	 * @return
	 */
	public CrossJoinedFromElement makeCrossJoinedFromElement(
			FromElementSpace fromElementSpace,
			EntityTypeDescriptor entityTypeDescriptor,
			FromClauseIndex fromClauseIndex,
			String alias) {
		if ( alias == null ) {
			alias = parsingContext.getImplicitAliasGenerator().buildUniqueImplicitAlias();
			log.debugf(
					"Generated implicit alias [%s] for cross joined entity reference [%s]",
					alias,
					entityTypeDescriptor.getTypeName()
			);
		}

		final CrossJoinedFromElement join = new CrossJoinedFromElement( fromElementSpace, alias, entityTypeDescriptor );
		fromElementSpace.addJoin( join );
		registerAlias( join, fromClauseIndex );
		return join;
	}

	public QualifiedAttributeJoinFromElement buildAttributeJoin(
			FromElementSpace fromElementSpace,
			FromElement lhs,
			AttributeDescriptor attributeDescriptor,
			FromClauseIndex fromClauseIndex,
			String alias,
			JoinType joinType,
			boolean fetched) {
		if ( attributeDescriptor == null ) {
			throw new ParsingException(
					"AttributeDescriptor was null [name unknown]; cannot build attribute join in relation to from-element [" +
							lhs.getTypeDescriptor().getTypeName() + "]"
			);
		}

		if ( alias == null ) {
			alias = parsingContext.getImplicitAliasGenerator().buildUniqueImplicitAlias();
			log.debugf(
					"Generated implicit alias [%s] for attribute join [%s.%s]",
					alias,
					lhs.getAlias(),
					attributeDescriptor.getName()
			);
		}

		final QualifiedAttributeJoinFromElement join = new QualifiedAttributeJoinFromElement(
				fromElementSpace,
				alias,
				lhs.getAlias(),
				attributeDescriptor,
				joinType,
				fetched
		);
		fromElementSpace.addJoin( join );
		registerAlias( join, fromClauseIndex );
		registerPath( join );
		return join;
	}

	private void registerAlias(FromElement fromElement, FromClauseIndex fromClauseIndex) {
		final String alias = fromElement.getAlias();

		if ( alias == null ) {
			throw new ParsingException( "FromElement alias was null" );
		}

		if ( ImplicitAliasGenerator.isImplicitAlias( alias ) ) {
			log.debug( "Alias registration for implicit FromElement alias : " + alias );
		}

		fromClauseIndex.registerAlias( fromElement );
	}

	private void registerPath(QualifiedAttributeJoinFromElement join) {
		// todo : come back to this
		// 		Be sure to disable this while processing from clauses (FromClauseProcessor).  Paths in from clause
		//		should almost never be reused.  Paths defined in other parts of the query are fine...
	}
}
