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

package com.google.cloud.dataflow.sdk.util;

import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Key;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Utilities for working with the Dataflow distribution.
 */
public final class DataflowReleaseInfo extends GenericJson {
  private static final Logger LOG = LoggerFactory.getLogger(DataflowReleaseInfo.class);

  private static final String DATAFLOW_PROPERTIES_PATH =
      "/com/google/cloud/dataflow/sdk/sdk.properties";

  private static class LazyInit {
    private static final DataflowReleaseInfo INSTANCE =
        new DataflowReleaseInfo(DATAFLOW_PROPERTIES_PATH);
  }

  /**
   * Returns an instance of DataflowReleaseInfo.
   */
  public static DataflowReleaseInfo getReleaseInfo() {
    return LazyInit.INSTANCE;
  }

  @Key private String name = "Google Cloud Dataflow Java SDK";
  @Key private String version = "Unknown";

  /** Provides the SDK name. */
  public String getName() {
    return name;
  }

  /** Provides the SDK version. */
  public String getVersion() {
    return version;
  }

  private DataflowReleaseInfo(String resourcePath) {
    Properties properties = new Properties();

    InputStream in = DataflowReleaseInfo.class.getResourceAsStream(
        DATAFLOW_PROPERTIES_PATH);
    if (in == null) {
      LOG.warn("Dataflow properties resource not found: {}", resourcePath);
      return;
    }

    try {
      properties.load(in);
    } catch (IOException e) {
      LOG.warn("Error loading Dataflow properties resource: ", e);
    }

    for (String name : properties.stringPropertyNames()) {
      if (name.equals("name")) {
        // We don't allow the properties to override the SDK name.
        continue;
      }
      put(name, properties.getProperty(name));
    }
  }
}
