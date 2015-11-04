/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.select;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.sqm.query.AliasCollisionException;

/**
 * The semantic select clause.  Defined as a list of individual selections.
 *
 * @author Steve Ebersole
 */
public class SelectClause {
	private final boolean distinct;
	private List<Selection> selections;
	private Set<String> aliases;

	public SelectClause(boolean distinct) {
		this.distinct = distinct;
	}

	public boolean isDistinct() {
		return distinct;
	}

	public List<Selection> getSelections() {
		if ( selections == null ) {
			return Collections.emptyList();
		}
		else {
			return Collections.unmodifiableList( selections );
		}
	}

	public void addSelection(Selection selection) {
		if ( selections == null ) {
			selections = new ArrayList<Selection>();
		}
		registerAlias( selection );
		selections.add( selection );
	}

	private void registerAlias(Selection selection) {
		final String alias = selection.getAlias();
		if(alias != null) {
			if ( aliases == null ) {
				aliases = new HashSet<String>();
			}
			if ( aliases.contains( alias ) ) {
				throw new AliasCollisionException( "Alias collision, alias " + alias + " is already used" );
			}
			aliases.add( alias );
		}
	}
}
