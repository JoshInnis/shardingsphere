/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.infra.metadata.database.resource.storage;

import com.google.common.base.Objects;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Storage node.
 */
@RequiredArgsConstructor
@Getter
public final class StorageNode {
    
    private final String name;
    
    @Override
    public boolean equals(final Object obj) {
        return obj instanceof StorageNode && ((StorageNode) obj).name.equalsIgnoreCase(name);
    }
    
    @Override
    public int hashCode() {
        return Objects.hashCode(name.toUpperCase());
    }
    
    @Override
    public String toString() {
        return name;
    }
}
