/*
 * Copyright (C) 2015 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.cloud.dataflow.sdk.transforms;

import static com.google.cloud.dataflow.sdk.TestUtils.LINES;
import static com.google.cloud.dataflow.sdk.TestUtils.LINES_ARRAY;
import static com.google.cloud.dataflow.sdk.TestUtils.NO_LINES;
import static com.google.cloud.dataflow.sdk.TestUtils.NO_LINES_ARRAY;

import com.google.cloud.dataflow.sdk.Pipeline;
import com.google.cloud.dataflow.sdk.coders.StringUtf8Coder;
import com.google.cloud.dataflow.sdk.testing.DataflowAssert;
import com.google.cloud.dataflow.sdk.testing.TestPipeline;
import com.google.cloud.dataflow.sdk.values.PCollection;
import com.google.cloud.dataflow.sdk.values.TimestampedValue;

import org.hamcrest.Matchers;
import org.joda.time.Instant;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Tests for Create.
 */
@RunWith(JUnit4.class)
@SuppressWarnings({"serial", "unchecked"})
public class CreateTest {
  @Rule public final ExpectedException thrown = ExpectedException.none();

  @Test
  @Category(com.google.cloud.dataflow.sdk.testing.RunnableOnService.class)
  public void testCreate() {
    Pipeline p = TestPipeline.create();

    PCollection<String> output =
        p.apply(Create.of(LINES));

    DataflowAssert.that(output)
        .containsInAnyOrder(LINES_ARRAY);
    p.run();
  }

  // TODO: setOrdered(true) isn't supported yet by the Dataflow service.
  @Test
  public void testCreateOrdered() {
    Pipeline p = TestPipeline.create();

    PCollection<String> output =
        p.apply(Create.of(LINES))
        .setOrdered(true);

    DataflowAssert.that(output)
        .containsInOrder(LINES_ARRAY);
    p.run();
  }

  @Test
  @Category(com.google.cloud.dataflow.sdk.testing.RunnableOnService.class)
  public void testCreateEmpty() {
    Pipeline p = TestPipeline.create();

    PCollection<String> output =
        p.apply(Create.of(NO_LINES))
        .setCoder(StringUtf8Coder.of());

    DataflowAssert.that(output)
        .containsInAnyOrder(NO_LINES_ARRAY);
    p.run();
  }

  static class Record implements Serializable {
  }

  static class Record2 extends Record {
  }

  @Test
  public void testPolymorphicType() throws Exception {
    thrown.expect(RuntimeException.class);
    thrown.expectMessage(
        Matchers.containsString("unable to infer a default Coder"));

    Pipeline p = TestPipeline.create();

    // Create won't infer a default coder in this case.
    p.apply(Create.of(new Record(), new Record2()));

    p.run();
  }

  @Test
  public void testCreateParameterizedType() throws Exception {
    Pipeline p = TestPipeline.create();

    PCollection<TimestampedValue<String>> output =
        p.apply(Create.of(
            TimestampedValue.of("a", new Instant(0)),
            TimestampedValue.of("b", new Instant(0))));

    DataflowAssert.that(output)
        .containsInAnyOrder(
            TimestampedValue.of("a", new Instant(0)),
            TimestampedValue.of("b", new Instant(0)));
  }

  private static class PrintTimestamps extends DoFn<String, String> {
    @Override
      public void processElement(ProcessContext c) {
      c.output(c.element() + ":" + c.timestamp().getMillis());
    }
  }

  @Test
  @Category(com.google.cloud.dataflow.sdk.testing.RunnableOnService.class)
  public void testCreateTimestamped() {
    Pipeline p = TestPipeline.create();

    List<TimestampedValue<String>> data = Arrays.asList(
        TimestampedValue.of("a", new Instant(1L)),
        TimestampedValue.of("b", new Instant(2L)),
        TimestampedValue.of("c", new Instant(3L)));

    PCollection<String> output =
        p.apply(Create.timestamped(data))
        .apply(ParDo.of(new PrintTimestamps()));

    DataflowAssert.that(output)
        .containsInAnyOrder("a:1", "b:2", "c:3");
    p.run();
  }

  @Test
  // This test fails when run on the service!
  // TODO: @Category(com.google.cloud.dataflow.sdk.testing.RunnableOnService.class)
  public void testCreateTimestampedEmpty() {
    Pipeline p = TestPipeline.create();

    PCollection<String> output = p
        .apply(Create.timestamped(new ArrayList<TimestampedValue<String>>()))
        .setCoder(StringUtf8Coder.of());

    DataflowAssert.that(output)
        .containsInAnyOrder();
    p.run();
  }

  @Test
  public void testCreateTimestampedPolymorphicType() throws Exception {
    thrown.expect(RuntimeException.class);
    thrown.expectMessage(
        Matchers.containsString("unable to infer a default Coder"));

    Pipeline p = TestPipeline.create();

    // Create won't infer a default coder in this case.
    PCollection<Record> c = p.apply(Create.timestamped(
        TimestampedValue.of(new Record(), new Instant(0)),
        TimestampedValue.<Record>of(new Record2(), new Instant(0))));

    p.run();


    throw new RuntimeException("Coder: " + c.getCoder());

  }
}
