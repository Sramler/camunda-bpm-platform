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

import com.jayway.jsonpath.InvalidJsonException;
import com.jayway.jsonpath.spi.json.AbstractJsonProvider;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.ObjectReader;

/**
 * JSONPath's built-in Jackson provider is hard-wired to Jackson 2 packages.
 * This copy keeps Spin on JSONPath while switching the runtime to Jackson 3.
 */
public class JacksonJsonPathJsonProvider extends AbstractJsonProvider {

  private static final ObjectMapper DEFAULT_OBJECT_MAPPER = new ObjectMapper();
  private static final ObjectReader DEFAULT_OBJECT_READER = DEFAULT_OBJECT_MAPPER.reader().forType(Object.class);

  protected ObjectMapper objectMapper;
  protected ObjectReader objectReader;

  public JacksonJsonPathJsonProvider() {
    this(DEFAULT_OBJECT_MAPPER, DEFAULT_OBJECT_READER);
  }

  public JacksonJsonPathJsonProvider(ObjectMapper objectMapper) {
    this(objectMapper, objectMapper.reader().forType(Object.class));
  }

  public JacksonJsonPathJsonProvider(ObjectMapper objectMapper, ObjectReader objectReader) {
    this.objectMapper = objectMapper;
    this.objectReader = objectReader;
  }

  public ObjectMapper getObjectMapper() {
    return objectMapper;
  }

  @Override
  public Object parse(String json) throws InvalidJsonException {
    try {
      return objectReader.readValue(json);
    } catch (JacksonException e) {
      throw new InvalidJsonException(e, json);
    }
  }

  @Override
  public Object parse(byte[] json) throws InvalidJsonException {
    try {
      return objectReader.readValue(json);
    } catch (JacksonException e) {
      throw new InvalidJsonException(e, new String(json, StandardCharsets.UTF_8));
    }
  }

  @Override
  public Object parse(InputStream jsonStream, String charset) throws InvalidJsonException {
    try {
      return objectReader.readValue(new InputStreamReader(jsonStream, charset));
    } catch (IOException e) {
      throw new InvalidJsonException(e);
    }
  }

  @Override
  public String toJson(Object object) {
    StringWriter writer = new StringWriter();
    try {
      JsonGenerator generator = objectMapper.createGenerator(writer);
      objectMapper.writeValue(generator, object);
      writer.flush();
      writer.close();
      generator.close();
      return writer.getBuffer().toString();
    } catch (IOException e) {
      throw new InvalidJsonException(e);
    }
  }

  @Override
  public List<Object> createArray() {
    return new LinkedList<>();
  }

  @Override
  public Object createMap() {
    return new LinkedHashMap<String, Object>();
  }
}
