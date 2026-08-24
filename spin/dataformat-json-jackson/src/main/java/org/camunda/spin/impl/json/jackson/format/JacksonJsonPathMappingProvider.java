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
package org.camunda.spin.impl.json.jackson.format;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.TypeRef;
import com.jayway.jsonpath.spi.mapper.MappingException;
import com.jayway.jsonpath.spi.mapper.MappingProvider;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.ObjectMapper;

/**
 * JSONPath's default mapping provider depends on Jackson 2 package names.
 * Spin uses this replacement so JSONPath can map against Jackson 3 types.
 */
public class JacksonJsonPathMappingProvider implements MappingProvider {

  protected final ObjectMapper objectMapper;

  public JacksonJsonPathMappingProvider() {
    this(new ObjectMapper());
  }

  public JacksonJsonPathMappingProvider(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  public <T> T map(Object source, Class<T> targetType, Configuration configuration) {
    if (source == null) {
      return null;
    }

    try {
      return objectMapper.convertValue(source, targetType);
    } catch (Exception e) {
      throw new MappingException(e);
    }
  }

  @Override
  public <T> T map(Object source, TypeRef<T> targetType, Configuration configuration) {
    if (source == null) {
      return null;
    }

    try {
      JavaType javaType = objectMapper.getTypeFactory().constructType(targetType.getType());
      return objectMapper.convertValue(source, javaType);
    } catch (Exception e) {
      throw new MappingException(e);
    }
  }
}
