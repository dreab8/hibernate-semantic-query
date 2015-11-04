/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.query.parser.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.jboss.logging.Logger;

import org.hibernate.query.parser.AliasCollisionException;
import org.hibernate.query.parser.internal.hql.phase1.FromClauseStackNode;
import org.hibernate.sqm.query.from.FromElement;
import org.hibernate.sqm.query.from.FromElementSpace;
import org.hibernate.sqm.query.from.JoinedFromElement;
import org.hibernate.sqm.query.select.SelectClause;
import org.hibernate.sqm.query.select.Selection;

/**
 * Maintains numerous indexes over information and state determined during the Phase 1 processing of
 * a queries from clauses.
 *
 * @author Steve Ebersole
 */
public class FromClauseIndex {
	private static final Logger log = Logger.getLogger( FromClauseIndex.class );

	private List<FromClauseStackNode> roots;

	protected Map<String, FromElement> fromElementsByPath = new HashMap<String, FromElement>();
	protected Map<String, FromElement> fromElementsByAlias = new HashMap<String, FromElement>();

	protected Map<String, Selection> selectionByAlias = new HashMap<String, Selection>();
	private FromClauseIndex parent;

	public FromClauseIndex() {
	}

	public FromClauseIndex(FromClauseIndex parentNode) {
		this.parent = parentNode;
		fromElementsByAlias = new HashMap<String, FromElement>();
	}

	public void registerAlias(Selection selection) {
		if(selection.getAlias() != null) {
			checkResultVariable( selection );
			selectionByAlias.put( selection.getAlias(), selection );
		}
	}

	public void registerAlias(FromElement fromElement) {
		final FromElement old = fromElementsByAlias.put( fromElement.getAlias(), fromElement );
		if ( old != null ) {
			throw new IllegalStateException(
					String.format(
							Locale.ENGLISH,
							"Alias [%s] used for multiple from-clause-elements : %s, %s",
							fromElement.getAlias(),
							old,
							fromElement
					)
			);
		}
	}

	public Selection findSelectionByAlias(String alias) {
		return selectionByAlias.get( alias );
	}

	public FromElement findFromElementByAlias(String alias) {
		if(fromElementsByAlias.containsKey( alias )) {
			return fromElementsByAlias.get( alias );
		}else if( parent != null){
			return parent.findFromElementByAlias( alias );
		}
		return null;
	}

	public FromElement findFromElementWithAttribute(FromClauseStackNode fromClause, String name) {
		FromElement found = null;
		for ( FromElementSpace space : fromClause.getFromClause().getFromElementSpaces() ) {
			if ( space.getRoot().getTypeDescriptor().getAttributeDescriptor( name ) != null ) {
				if ( found != null ) {
					throw new IllegalStateException( "Multiple from-elements expose unqualified attribute : " + name );
				}
				found = space.getRoot();
			}

			for ( JoinedFromElement join : space.getJoins() ) {
				if ( join.getTypeDescriptor().getAttributeDescriptor( name ) != null ) {
					if ( found != null ) {
						throw new IllegalStateException( "Multiple from-elements expose unqualified attribute : " + name );
					}
					found = join;
				}
			}
		}

		if ( found == null ) {
			if ( fromClause.hasParent() ) {
				log.debugf( "Unable to resolve unqualified attribute [%s] in local FromClause; checking parent" );
				found = findFromElementWithAttribute( fromClause.getParentNode(), name );
			}
		}

		return found;
	}

	public void registerRootFromClauseStackNode(FromClauseStackNode root) {
		if ( roots == null ) {
			roots = new ArrayList<FromClauseStackNode>();
		}
		roots.add( root );
	}

	public List<FromClauseStackNode> getFromClauseStackNodeList() {
		if ( roots == null ) {
			return Collections.emptyList();
		}
		else {
			return Collections.unmodifiableList( roots );
		}
	}

	public FromClauseIndex getParent() {
		return parent;
	}

	private void checkResultVariable(Selection selection) {
		final String alias = selection.getAlias();
		if ( selectionByAlias.containsKey( alias ) ) {
			throw new AliasCollisionException( "Alias collision, alias " + alias + " is already used" );
		}
		if ( fromElementsByAlias.containsKey( alias ) ) {
			if ( !selection.getExpression().getTypeDescriptor().equals(
					fromElementsByAlias.get( alias ).getTypeDescriptor()
			) ) {
				throw new AliasCollisionException(
						"In Select clause is used the alias " + alias + " defined in From clause but referring a different element"
				);
			}
		}
	}
}
