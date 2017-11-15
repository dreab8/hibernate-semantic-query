/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package benchmark;

import org.hibernate.sqm.domain.DomainMetamodel;

import org.hibernate.test.sqm.ConsumerContextImpl;
import org.hibernate.test.sqm.domain.EntityTypeImpl;
import org.hibernate.test.sqm.domain.ExplicitDomainMetamodel;
import org.hibernate.test.sqm.domain.StandardBasicTypeDescriptors;

import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

/**
 * @author Andrea Boriero
 */
@State(Scope.Thread)
public class TestScope {
	public ConsumerContextImpl consumerContext;

	@Setup
	public void setup(){
		consumerContext = new ConsumerContextImpl( buildMetamodel() );
	}

	public ConsumerContextImpl getConsumerContext() {
		return consumerContext;
	}

	private DomainMetamodel buildMetamodel() {
		ExplicitDomainMetamodel metamodel = new ExplicitDomainMetamodel();

		EntityTypeImpl relatedType = metamodel.makeEntityType( "com.acme.Related" );
		relatedType.makeSingularAttribute(
				"basic1",
				StandardBasicTypeDescriptors.INSTANCE.LONG
		);
		relatedType.makeSingularAttribute(
				"basic2",
				StandardBasicTypeDescriptors.INSTANCE.LONG
		);
		relatedType.makeSingularAttribute(
				"entity",
				relatedType
		);

		EntityTypeImpl somethingType = metamodel.makeEntityType( "com.acme.Something" );
		somethingType.makeSingularAttribute(
				"b",
				StandardBasicTypeDescriptors.INSTANCE.STRING
		);
		somethingType.makeSingularAttribute(
				"c",
				StandardBasicTypeDescriptors.INSTANCE.STRING
		);
		somethingType.makeSingularAttribute(
				"basic",
				StandardBasicTypeDescriptors.INSTANCE.LONG
		);
		somethingType.makeSingularAttribute(
				"basic1",
				StandardBasicTypeDescriptors.INSTANCE.LONG
		);
		somethingType.makeSingularAttribute(
				"basic2",
				StandardBasicTypeDescriptors.INSTANCE.LONG
		);
		somethingType.makeSingularAttribute(
				"entity",
				relatedType
		);

		EntityTypeImpl somethingElseType = metamodel.makeEntityType( "com.acme.SomethingElse" );
		somethingElseType.makeSingularAttribute(
				"basic1",
				StandardBasicTypeDescriptors.INSTANCE.LONG
		);
		somethingElseType.makeSingularAttribute(
				"related1",
				relatedType
		);
		somethingElseType.makeSingularAttribute(
				"related2",
				relatedType
		);

		EntityTypeImpl somethingElse2Type = metamodel.makeEntityType( "com.acme.SomethingElse2" );
		somethingElse2Type.makeSingularAttribute(
				"basic1",
				StandardBasicTypeDescriptors.INSTANCE.LONG
		);
		somethingElse2Type.makeSingularAttribute(
				"basic2",
				StandardBasicTypeDescriptors.INSTANCE.LONG
		);

		return metamodel;
	}
}
