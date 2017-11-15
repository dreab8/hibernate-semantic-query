/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package benchmark;

import org.hibernate.sqm.SemanticQueryInterpreter;
import org.hibernate.sqm.query.SqmStatement;

import org.hibernate.test.sqm.ConsumerContextImpl;

import org.openjdk.jmh.annotations.Benchmark;


/**
 * @author Andrea Boriero
 */
public class SimpleHQLQueryBenchmarkTest {

	@Benchmark
	public void testIt(TestScope state) {
		SqmStatement selectStatement = interpret( "select a.basic from Something a where 1=2",
												  state.getConsumerContext() );
	}

	private SqmStatement interpret(String query, ConsumerContextImpl consumerContext) {
		return SemanticQueryInterpreter.interpret( query, consumerContext );
	}

}
