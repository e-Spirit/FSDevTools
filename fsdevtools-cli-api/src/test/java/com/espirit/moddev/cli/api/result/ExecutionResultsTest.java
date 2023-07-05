/*
 *
 * *********************************************************************
 * fsdevtools
 * %%
 * Copyright (C) 2022 Crownpeak Technology GmbH
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * *********************************************************************
 *
 */

package com.espirit.moddev.cli.api.result;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.Objects;
import java.util.StringJoiner;

import static org.assertj.core.api.Assertions.assertThat;

public class ExecutionResultsTest {

	public static class TestResult implements ExecutionResult {

		private final int _value;

		public TestResult(final int value) {
			_value = value;
		}

		@Override
		public boolean equals(final Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			final TestResult intResult = (TestResult) o;
			return _value == intResult._value;
		}

		@Override
		public int hashCode() {
			return Objects.hash(_value);
		}

		@Override
		public String toString() {
			return new StringJoiner(", ", TestResult.class.getSimpleName() + "[", "]")
					.add("value=" + _value)
					.toString();
		}
	}

	public static class TestErrorResult implements ExecutionErrorResult<IllegalStateException> {

		private final int _value;
		private final IllegalStateException _exception;

		public TestErrorResult(final int value) {
			_value = value;
			_exception = new IllegalStateException(String.valueOf(_value));
		}

		@NotNull
		@Override
		public IllegalStateException getThrowable() {
			return _exception;
		}

		@Override
		public String toString() {
			return new StringJoiner(", ", TestErrorResult.class.getSimpleName() + "[", "]")
					.add("value=" + _value)
					.add("exception=" + _exception)
					.toString();
		}

		@Override
		public boolean equals(final Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			final TestErrorResult that = (TestErrorResult) o;
			return _value == that._value &&
					Objects.equals(getThrowable(), that.getThrowable());
		}

		@Override
		public int hashCode() {
			return Objects.hash(_value, getThrowable());
		}

	}

	@Test
	public void defaultValues() {
		final ExecutionResults results = new ExecutionResults();
		assertThat(results.isEmpty()).isTrue();
		assertThat(results.stream().count()).isZero();
		assertThat(results.hasError()).isFalse();
	}

	@Test
	public void mixedValues() {
		// setup
		final ExecutionResults results = new ExecutionResults();
		final TestResult result1 = new TestResult(1);
		final TestErrorResult result2 = new TestErrorResult(3);
		final TestResult result3 = new TestResult(3);

		// test
		results.add(result1);
		results.add(result2);
		results.add(result3);

		// verify
		assertThat(results.size()).isEqualTo(3);
		assertThat(results.stream().count()).isEqualTo(3);
		assertThat(results.get(0)).isEqualTo(result1);
		assertThat(results.get(1)).isEqualTo(result2);
		assertThat(results.get(2)).isEqualTo(result3);
		assertThat(results.hasError()).isTrue();
	}

	@Test
	public void avoid_duplicate_entries() {
		// setup
		final ExecutionResults results = new ExecutionResults();
		final TestResult result1 = new TestResult(1);
		final TestResult result2 = new TestResult(2);

		// test
		results.add(result1);
		results.add(result2);
		results.add(new TestResult(1));
		results.add(result1);

		// verify
		assertThat(results.size()).isEqualTo(2);
		assertThat(results.stream().count()).isEqualTo(2);
		assertThat(results.get(0)).isSameAs(result1);
		assertThat(results.get(1)).isEqualTo(result2);
	}

	@Test
	public void nested_add() {
		// setup
		final TestResult result1 = new TestResult(1);
		final TestResult result2 = new TestResult(2);
		final TestResult result3 = new TestResult(3);
		final TestResult result4 = new TestResult(4);
		final ExecutionResults results1 = new ExecutionResults();
		results1.add(result1);
		results1.add(result2);
		final ExecutionResults results2 = new ExecutionResults();
		results2.add(result3);
		results2.add(result1);
		results2.add(result4);

		// test --> nest results2 into results1
		results1.add(results2);

		// verify
		assertThat(results1.size()).isEqualTo(4);
		assertThat(results1.stream().count()).isEqualTo(4);
		assertThat(results1.get(0)).isSameAs(result1);
		assertThat(results1.get(1)).isEqualTo(result2);
		assertThat(results1.get(2)).isEqualTo(result3);
		assertThat(results1.get(3)).isEqualTo(result4);
		assertThat(results1.hasError()).isFalse();

		assertThat(results2.size()).isEqualTo(3);
		assertThat(results2.stream().count()).isEqualTo(3);
		assertThat(results2.get(0)).isSameAs(result3);
		assertThat(results2.get(1)).isEqualTo(result1);
		assertThat(results2.get(2)).isEqualTo(result4);
		assertThat(results2.hasError()).isFalse();
	}

	@Test
	public void add_results_with_exception() {
		// setup
		final TestResult result1 = new TestResult(1);
		final TestErrorResult result2 = new TestErrorResult(2);
		final ExecutionResults results1 = new ExecutionResults();
		results1.add(result1);
		final ExecutionResults results2 = new ExecutionResults();
		results2.add(result2);

		// pre-verify
		assertThat(results1.size()).isEqualTo(1);
		assertThat(results1.stream().count()).isEqualTo(1);
		assertThat(results1.get(0)).isSameAs(result1);
		assertThat(results1.hasError()).isFalse();

		assertThat(results2.size()).isEqualTo(1);
		assertThat(results2.stream().count()).isEqualTo(1);
		assertThat(results2.get(0)).isSameAs(result2);
		assertThat(results2.hasError()).isTrue();

		// test --> nest results2 into results1
		results1.add(results2);

		// verify
		assertThat(results1.size()).isEqualTo(2);
		assertThat(results1.stream().count()).isEqualTo(2);
		assertThat(results1.get(0)).isSameAs(result1);
		assertThat(results1.get(1)).isSameAs(result2);
		assertThat(results1.hasError()).isTrue();

		assertThat(results2.size()).isEqualTo(1);
		assertThat(results2.stream().count()).isEqualTo(1);
		assertThat(results2.get(0)).isSameAs(result2);
		assertThat(results2.hasError()).isTrue();
	}

	@Test
	public void add_results_to_results_with_exception() {
		// setup
		final TestResult result1 = new TestResult(1);
		final TestErrorResult result2 = new TestErrorResult(2);
		final ExecutionResults results1 = new ExecutionResults();
		results1.add(result1);
		final ExecutionResults results2 = new ExecutionResults();
		results2.add(result2);

		// pre-verify
		assertThat(results1.size()).isEqualTo(1);
		assertThat(results1.stream().count()).isEqualTo(1);
		assertThat(results1.get(0)).isSameAs(result1);
		assertThat(results1.hasError()).isFalse();

		assertThat(results2.size()).isEqualTo(1);
		assertThat(results2.stream().count()).isEqualTo(1);
		assertThat(results2.get(0)).isSameAs(result2);
		assertThat(results2.hasError()).isTrue();

		// test --> nest results2 into results1
		results2.add(results1);

		// verify
		assertThat(results1.size()).isEqualTo(1);
		assertThat(results1.stream().count()).isEqualTo(1);
		assertThat(results1.get(0)).isSameAs(result1);
		assertThat(results1.hasError()).isFalse();

		assertThat(results2.size()).isEqualTo(2);
		assertThat(results2.stream().count()).isEqualTo(2);
		assertThat(results2.get(0)).isSameAs(result2);
		assertThat(results2.get(1)).isSameAs(result1);
		assertThat(results2.hasError()).isTrue();
	}

	@Test
	public void nested_add_avoid_stackOverflow_self_add() {
		// setup
		final TestResult result = new TestResult(1);
		final ExecutionResults results = new ExecutionResults();
		results.add(result);

		// test
		results.add(results);

		// verify
		assertThat(results.size()).isEqualTo(1);
		assertThat(results.stream().count()).isEqualTo(1);
		assertThat(results.get(0)).isSameAs(result);
	}

	@Test
	public void nested_add_avoid_stackOverflow_in_circular_add() {
		// setup
		final TestResult result1 = new TestResult(1);
		final TestResult result2 = new TestResult(2);
		final TestResult result3 = new TestResult(3);
		final ExecutionResults results1 = new ExecutionResults();
		results1.add(result1);
		final ExecutionResults results2 = new ExecutionResults();
		results2.add(result2);
		final ExecutionResults results3 = new ExecutionResults();
		results3.add(result3);

		// test --> build circular dependencies
		results1.add(results2);
		results2.add(results3);
		results3.add(results1);

		// verify
		assertThat(results1.size()).isEqualTo(2);
		assertThat(results1.stream().count()).isEqualTo(2);
		assertThat(results1.get(0)).isSameAs(result1);
		assertThat(results1.get(1)).isSameAs(result2);

		assertThat(results2.size()).isEqualTo(2);
		assertThat(results2.stream().count()).isEqualTo(2);
		assertThat(results2.get(0)).isSameAs(result2);
		assertThat(results2.get(1)).isSameAs(result3);

		assertThat(results3.size()).isEqualTo(3);
		assertThat(results3.stream().count()).isEqualTo(3);
		assertThat(results3.get(0)).isSameAs(result3);
		assertThat(results3.get(1)).isSameAs(result1);
		assertThat(results3.get(2)).isSameAs(result2);
	}

}
