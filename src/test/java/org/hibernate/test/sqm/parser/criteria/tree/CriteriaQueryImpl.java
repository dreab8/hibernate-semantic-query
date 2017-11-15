/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.test.sqm.parser.criteria.tree;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.Tuple;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;
import javax.persistence.criteria.Subquery;
import javax.persistence.metamodel.EntityType;

import org.hibernate.sqm.parser.criteria.tree.JpaCriteriaQuery;
import org.hibernate.sqm.parser.criteria.tree.JpaExpression;
import org.hibernate.sqm.parser.criteria.tree.JpaOrder;
import org.hibernate.sqm.parser.criteria.tree.JpaQuerySpec;
import org.hibernate.sqm.parser.criteria.tree.select.JpaSelection;

import org.jboss.logging.Logger;

/**
 * The Hibernate implementation of the JPA {@link CriteriaQuery} contract.  Mostly a set of delegation to its
 * internal {@link JpaQuerySpecImpl}.
 *
 * @author Steve Ebersole
 */
public class CriteriaQueryImpl<T> extends AbstractNode implements JpaCriteriaQuery<T>, Serializable {
	private static final Logger log = Logger.getLogger( CriteriaQueryImpl.class );

	private final Class<T> returnType;

	private final JpaQuerySpecImpl<T> queryStructure;


	public CriteriaQueryImpl(
			CriteriaBuilderImpl criteriaBuilder,
			Class<T> returnType) {
		super( criteriaBuilder );
		this.returnType = returnType;
		this.queryStructure = new JpaQuerySpecImpl<T>( this, criteriaBuilder );
	}

	@Override
	public Class<T> getResultType() {
		return returnType;
	}


	// SELECTION ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	@Override
	public CriteriaQuery<T> distinct(boolean applyDistinction) {
		queryStructure.setDistinct( applyDistinction );
		return this;
	}

	@Override
	public boolean isDistinct() {
		return queryStructure.isDistinct();
	}

	@Override
	@SuppressWarnings({ "unchecked" })
	public Selection<T> getSelection() {
		return ( Selection<T> ) queryStructure.getSelection();
	}

	public void applySelection(JpaSelection<? extends T> selection) {
		queryStructure.setSelection( selection );
	}

	@Override
	@SuppressWarnings("unchecked")
	public CriteriaQuery<T> select(Selection<? extends T> selection) {
		criteriaBuilder().check( selection );
		applySelection( (JpaSelection<? extends T>) selection );
		return this;
	}

	@Override
	@SuppressWarnings({ "unchecked" })
	public CriteriaQuery<T> multiselect(Selection<?>... selections) {
		return multiselect( Arrays.asList( selections ) );
	}

	@Override
	@SuppressWarnings({ "unchecked" })
	public CriteriaQuery<T> multiselect(List<Selection<?>> selections) {
		final JpaSelection<? extends T> selection;

		if ( Tuple.class.isAssignableFrom( getResultType() ) ) {
			selection = (JpaSelection<? extends T>) criteriaBuilder().tuple( selections );
		}
		else if ( getResultType().isArray() ) {
			selection = ( JpaSelection<? extends T> )  criteriaBuilder().array(
					(Class<Object[]>) getResultType(),
					selections
			);
		}
		else if ( Object.class.equals( getResultType() ) ) {
			switch ( selections.size() ) {
				case 0: {
					throw new IllegalArgumentException(
							"empty selections passed to criteria query typed as Object"
					);
				}
				case 1: {
					selection = (JpaSelection<? extends T>) selections.get( 0 );
					break;
				}
				default: {
					selection = (JpaSelection<? extends T>) criteriaBuilder().array( selections );
				}
			}
		}
		else {
			selection = criteriaBuilder().construct( getResultType(), selections );
		}
		applySelection( selection );
		return this;
	}


	// ROOTS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	@Override
	public Set<Root<?>> getRoots() {
		return queryStructure.getFromClause().getRoots().stream().collect( Collectors.toSet() );
	}

	@Override
	public <X> Root<X> from(EntityType<X> entityType) {
		return queryStructure.from( entityType );
	}

	@Override
	public <X> Root<X> from(Class<X> entityClass) {
		return queryStructure.from( entityClass );
	}

	public <X> Root<X> from(String entityName) {
		return queryStructure.from( entityName );
	}

	// RESTRICTION ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	@Override
	public Predicate getRestriction() {
		return queryStructure.getRestriction();
	}

	@Override
	public CriteriaQuery<T> where(Expression<Boolean> expression) {
		queryStructure.setRestriction( criteriaBuilder().wrap( expression ) );
		return this;
	}

	@Override
	public CriteriaQuery<T> where(Predicate... predicates) {
		// TODO : assuming this should be a conjuntion, but the spec does not say specifically...
		queryStructure.setRestriction( criteriaBuilder().and( predicates ) );
		return this;
	}


	// GROUPING ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	@Override
	public List<Expression<?>> getGroupList() {
		return queryStructure.getGroupings().stream().collect( Collectors.toList() );
	}

	@Override
	public CriteriaQuery<T> groupBy(Expression<?>... groupings) {
		queryStructure.setGroupings( (JpaExpression<?>[]) groupings );
		return this;
	}

	@Override
	@SuppressWarnings("unchecked")
	public CriteriaQuery<T> groupBy(List<Expression<?>> groupings) {
		groupings.forEach( expr -> {
			criteriaBuilder().check( expr );
			queryStructure.setGroupings( (List<JpaExpression<?>>) expr );
		} );
		return this;
	}

	@Override
	public Predicate getGroupRestriction() {
		return queryStructure.getHaving();
	}

	@Override
	public CriteriaQuery<T> having(Expression<Boolean> expression) {
		queryStructure.setHaving( criteriaBuilder().wrap( expression ) );
		return this;
	}

	@Override
	public CriteriaQuery<T> having(Predicate... predicates) {
		queryStructure.setHaving( criteriaBuilder().and( predicates ) );
		return this;
	}


	// ORDERING ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	@Override
	public List<Order> getOrderList() {
		return queryStructure.getOrderList().stream().collect( Collectors.toList() );
	}

	@Override
	public CriteriaQuery<T> orderBy(Order... orders) {
		queryStructure.clearOrderList();
		for ( Order order : orders ) {
			criteriaBuilder().check( order );
			queryStructure.addOrderBy( (JpaOrder) order );
		}
		return this;
	}

	@Override
	public CriteriaQuery<T> orderBy(List<Order> orders) {
		queryStructure.clearOrderList();
		orders.forEach( order -> {
			criteriaBuilder().check( order );
			queryStructure.addOrderBy( (JpaOrder) order );
		} );
		return this;
	}

	@Override
	public Set<ParameterExpression<?>> getParameters() {
		return queryStructure.collectParameters();
	}

	@Override
	public <U> Subquery<U> subquery(Class<U> subqueryType) {
		return queryStructure.subquery( subqueryType );
	}

	public void validate() {
		// getRoots() is explicitly supposed to return empty if none defined, no need to check for null
		if ( getRoots().isEmpty() ) {
			throw new IllegalStateException( "No criteria query roots were specified" );
		}

		// if there is not an explicit selection, there is an *implicit* selection of the root entity provided only
		// a single query root was defined.
		if ( getSelection() == null && !hasImplicitSelection() ) {
			throw new IllegalStateException( "No explicit selection and an implicit one could not be determined" );
		}
	}

	/**
	 * If no explicit selection was defined, we have a condition called an implicit selection if the query specified
	 * a single {@link Root} and the java type of that {@link Root root's} model is the same as this criteria's
	 * {@link #getResultType() result type}.
	 *
	 * @return True if there is an explicit selection; false otherwise.
	 */
	private boolean hasImplicitSelection() {
		if ( getRoots().size() != 1 ) {
			return false;
		}

		Root root = getRoots().iterator().next();
		Class<?> javaType = root.getModel().getJavaType();
		if ( javaType != null && javaType != returnType ) {
			return false;
		}

		// if we get here, the query defined no selection but defined a single root of the same type as the
		// criteria query return, so we use that as the implicit selection
		//
		// todo : should we put an implicit marker in the selection to this fact to make later processing easier?
		return true;
	}

	@Override
	public JpaQuerySpec getQuerySpec() {
		return queryStructure;
	}
}
