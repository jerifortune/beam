/*******************************************************************************
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
 ******************************************************************************/

package com.google.cloud.dataflow.sdk.util.common.worker;

import com.google.cloud.dataflow.sdk.util.common.CounterSet;

/**
 * The abstract base class for Operations that have inputs and
 * implement process().
 */
public abstract class ReceivingOperation extends Operation implements Receiver {

  public ReceivingOperation(String operationName,
                            OutputReceiver[] receivers,
                            String counterPrefix,
                            CounterSet.AddCounterMutator addCounterMutator,
                            StateSampler stateSampler) {
    super(operationName, receivers,
          counterPrefix, addCounterMutator, stateSampler);
  }

  /**
   * Adds an input to this Operation, coming from the given
   * output of the given source Operation.
   */
  public void attachInput(Operation source, int outputNum) {
    checkUnstarted();
    OutputReceiver fanOut = source.receivers[outputNum];
    fanOut.addOutput(this);
  }
}
