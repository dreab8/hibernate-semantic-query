/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.test.query.parser.hql.dml;

import org.hibernate.sqm.SemanticQueryInterpreter;
import org.hibernate.sqm.domain.DomainMetamodel;
import org.hibernate.sqm.domain.SingularAttribute;
import org.hibernate.sqm.query.DeleteStatement;
import org.hibernate.sqm.query.Statement;
import org.hibernate.sqm.query.expression.AttributeReferenceExpression;
import org.hibernate.sqm.query.expression.NamedParameterExpression;
import org.hibernate.sqm.query.predicate.RelationalPredicate;

import org.hibernate.test.query.parser.ConsumerContextImpl;
import org.hibernate.test.sqm.domain.EntityTypeImpl;
import org.hibernate.test.sqm.domain.ExplicitDomainMetamodel;
import org.hibernate.test.sqm.domain.StandardBasicTypeDescriptors;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertSame;

/**
 * @author Steve Ebersole
 */
public class BasicDeleteTests {
	@Test
	public void basicDeleteTest() {
		ConsumerContextImpl consumerContext = new ConsumerContextImpl( buildMetamodel() );

		basicDeleteAssertions( "delete Entity1 where basic1 = :something", consumerContext );
		basicDeleteAssertions( "delete from Entity1 where basic1 = :something", consumerContext );

		basicDeleteAssertions( "delete Entity1 e where e.basic1 = :something", consumerContext );
		basicDeleteAssertions( "delete from Entity1 e where e.basic1 = :something", consumerContext );

		basicDeleteAssertions( "delete Entity1 e where basic1 = :something", consumerContext );
		basicDeleteAssertions( "delete from Entity1 e where basic1 = :something", consumerContext );
	}

	private void basicDeleteAssertions(String query, ConsumerContextImpl consumerContext) {
		final Statement statement = SemanticQueryInterpreter.interpret( query, consumerContext );

		assertThat( statement, instanceOf( DeleteStatement.class ) );
		DeleteStatement deleteStatement = (DeleteStatement) statement;

		assertThat( deleteStatement.getEntityFromElement().getEntityName(), equalTo( "com.acme.Entity1" ) );

		assertThat( deleteStatement.getWhereClause().getPredicate(), instanceOf( RelationalPredicate.class ) );
		RelationalPredicate predicate = (RelationalPredicate) deleteStatement.getWhereClause().getPredicate();

		assertThat( predicate.getLeftHandExpression(), instanceOf( AttributeReferenceExpression.class ) );
		AttributeReferenceExpression attributeReferenceExpression = (AttributeReferenceExpression) predicate.getLeftHandExpression();
		assertSame( attributeReferenceExpression.getBoundFromElementBinding().getFromElement(), deleteStatement.getEntityFromElement() );

		assertThat( predicate.getRightHandExpression(), instanceOf( NamedParameterExpression.class ) );
	}

	private DomainMetamodel buildMetamodel() {
		ExplicitDomainMetamodel metamodel = new ExplicitDomainMetamodel();
		EntityTypeImpl entityType = metamodel.makeEntityType( "com.acme.Entity1" );
		entityType.makeSingularAttribute(
				"basic1",
				SingularAttribute.Classification.BASIC,
				StandardBasicTypeDescriptors.INSTANCE.LONG
		);
		return metamodel;
	}
}
