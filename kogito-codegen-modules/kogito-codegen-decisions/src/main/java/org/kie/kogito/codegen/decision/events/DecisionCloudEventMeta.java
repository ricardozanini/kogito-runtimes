/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kie.kogito.codegen.decision.events;

import java.util.Objects;

import org.kie.kogito.event.CloudEventMeta;
import org.kie.kogito.event.EventKind;

/**
 * Decisions representation of {@link CloudEventMeta} with inner information about the generated DMN model.
 */
public class DecisionCloudEventMeta extends CloudEventMeta {

    final String methodNameChunk;

    public DecisionCloudEventMeta(String type, String source, EventKind kind, String methodNameChunk) {
        super(type, source, kind);
        this.methodNameChunk = methodNameChunk;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DecisionCloudEventMeta that = (DecisionCloudEventMeta) o;
        return getType().equals(that.getType()) && getSource().equals(that.getSource()) && getKind() == that.getKind();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getType(), getSource(), getKind());
    }
}