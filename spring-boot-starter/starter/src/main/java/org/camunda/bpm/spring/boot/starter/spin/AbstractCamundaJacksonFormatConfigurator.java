/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.spring.boot.starter.spin;

import org.camunda.spin.impl.json.jackson.format.JacksonJsonDataFormat;
import org.camunda.spin.impl.json.jackson.format.JacksonJsonPathJsonProvider;
import org.camunda.spin.impl.json.jackson.format.JacksonJsonPathMappingProvider;
import org.camunda.spin.spi.DataFormatConfigurator;

import com.jayway.jsonpath.Configuration.ConfigurationBuilder;

import tools.jackson.databind.JacksonModule;
import tools.jackson.databind.ObjectMapper;

abstract class AbstractCamundaJacksonFormatConfigurator implements DataFormatConfigurator<JacksonJsonDataFormat> {

  private final String moduleClassName;

  protected AbstractCamundaJacksonFormatConfigurator(String moduleClassName) {
    this.moduleClassName = moduleClassName;
  }

  @Override
  public Class<JacksonJsonDataFormat> getDataFormatClass() {
    return JacksonJsonDataFormat.class;
  }

  @Override
  public void configure(JacksonJsonDataFormat dataFormat) {
    ObjectMapper mapper = dataFormat.getObjectMapper();
    ObjectMapper configuredMapper = mapper.rebuild()
        .addModule(instantiateModule())
        .build();

    dataFormat.setObjectMapper(configuredMapper);
    dataFormat.setJsonPathConfiguration(new ConfigurationBuilder()
        .jsonProvider(new JacksonJsonPathJsonProvider(configuredMapper))
        .mappingProvider(new JacksonJsonPathMappingProvider(configuredMapper))
        .build());
  }

  protected JacksonModule instantiateModule() {
    try {
      Class<?> moduleClass = Class.forName(moduleClassName);
      return (JacksonModule) moduleClass.getDeclaredConstructor().newInstance();
    } catch (ReflectiveOperationException e) {
      throw new IllegalStateException("Unable to initialize Jackson module " + moduleClassName, e);
    }
  }
}
