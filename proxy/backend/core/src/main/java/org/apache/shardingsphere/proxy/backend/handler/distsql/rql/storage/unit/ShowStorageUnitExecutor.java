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

package org.apache.shardingsphere.proxy.backend.handler.distsql.rql.storage.unit;

import com.google.gson.Gson;
import org.apache.shardingsphere.distsql.handler.query.RQLExecutor;
import org.apache.shardingsphere.distsql.parser.statement.rql.show.ShowStorageUnitsStatement;
import org.apache.shardingsphere.infra.database.core.connector.ConnectionProperties;
import org.apache.shardingsphere.infra.database.core.metadata.database.DialectDatabaseMetaData;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.database.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.datasource.pool.CatalogSwitchableDataSource;
import org.apache.shardingsphere.infra.datasource.pool.props.creator.DataSourcePoolPropertiesCreator;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.resource.storage.StorageUnit;
import org.apache.shardingsphere.proxy.backend.util.StorageUnitUtils;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Show storage unit executor.
 */
public final class ShowStorageUnitExecutor implements RQLExecutor<ShowStorageUnitsStatement> {
    
    private static final String CONNECTION_TIMEOUT_MILLISECONDS = "connectionTimeoutMilliseconds";
    
    private static final String IDLE_TIMEOUT_MILLISECONDS = "idleTimeoutMilliseconds";
    
    private static final String MAX_LIFETIME_MILLISECONDS = "maxLifetimeMilliseconds";
    
    private static final String MAX_POOL_SIZE = "maxPoolSize";
    
    private static final String MIN_POOL_SIZE = "minPoolSize";
    
    private static final String READ_ONLY = "readOnly";
    
    @Override
    public Collection<String> getColumnNames() {
        return Arrays.asList("name", "type", "host", "port", "db", "connection_timeout_milliseconds", "idle_timeout_milliseconds",
                "max_lifetime_milliseconds", "max_pool_size", "min_pool_size", "read_only", "other_attributes");
    }
    
    @Override
    public Collection<LocalDataQueryResultRow> getRows(final ShardingSphereDatabase database, final ShowStorageUnitsStatement sqlStatement) {
        ResourceMetaData resourceMetaData = database.getResourceMetaData();
        Collection<LocalDataQueryResultRow> result = new LinkedList<>();
        for (Entry<String, DataSourcePoolProperties> entry : getDataSourcePoolPropertiesMap(database, sqlStatement).entrySet()) {
            String key = entry.getKey();
            DataSourcePoolProperties props = entry.getValue();
            ConnectionProperties connectionProps = resourceMetaData.getConnectionProperties(key);
            Map<String, Object> standardProps = props.getPoolPropertySynonyms().getStandardProperties();
            Map<String, Object> otherProps = props.getCustomProperties().getProperties();
            result.add(new LocalDataQueryResultRow(key,
                    resourceMetaData.getStorageType(key).getType(),
                    connectionProps.getHostname(),
                    connectionProps.getPort(),
                    connectionProps.getCatalog(),
                    getStandardProperty(standardProps, CONNECTION_TIMEOUT_MILLISECONDS),
                    getStandardProperty(standardProps, IDLE_TIMEOUT_MILLISECONDS),
                    getStandardProperty(standardProps, MAX_LIFETIME_MILLISECONDS),
                    getStandardProperty(standardProps, MAX_POOL_SIZE),
                    getStandardProperty(standardProps, MIN_POOL_SIZE),
                    getStandardProperty(standardProps, READ_ONLY),
                    otherProps.isEmpty() ? "" : new Gson().toJson(otherProps)));
        }
        return result;
    }
    
    private Map<String, DataSourcePoolProperties> getDataSourcePoolPropertiesMap(final ShardingSphereDatabase database, final ShowStorageUnitsStatement sqlStatement) {
        Map<String, DataSourcePoolProperties> result = new LinkedHashMap<>(database.getResourceMetaData().getStorageUnitMetaData().getStorageUnits().size(), 1F);
        Map<String, DataSourcePoolProperties> propsMap = database.getResourceMetaData().getStorageUnitMetaData().getStorageUnits().entrySet().stream()
                .collect(Collectors.toMap(Entry::getKey, entry -> entry.getValue().getDataSourcePoolProperties(), (oldValue, currentValue) -> currentValue, LinkedHashMap::new));
        Map<String, StorageUnit> storageUnits = database.getResourceMetaData().getStorageUnitMetaData().getStorageUnits();
        Optional<Integer> usageCount = sqlStatement.getUsageCount();
        if (usageCount.isPresent()) {
            Map<String, Collection<String>> inUsedStorageUnits = StorageUnitUtils.getInUsedStorageUnits(
                    database.getRuleMetaData(), database.getResourceMetaData().getStorageUnitMetaData().getStorageUnits().size());
            for (Entry<String, StorageUnit> entry : database.getResourceMetaData().getStorageUnitMetaData().getStorageUnits().entrySet()) {
                Integer currentUsageCount = inUsedStorageUnits.containsKey(entry.getKey()) ? inUsedStorageUnits.get(entry.getKey()).size() : 0;
                if (usageCount.get().equals(currentUsageCount)) {
                    result.put(entry.getKey(), getDataSourcePoolProperties(propsMap, entry.getKey(), storageUnits.get(entry.getKey()).getStorageType(), entry.getValue().getDataSource()));
                }
            }
        } else {
            for (Entry<String, StorageUnit> entry : database.getResourceMetaData().getStorageUnitMetaData().getStorageUnits().entrySet()) {
                result.put(entry.getKey(), getDataSourcePoolProperties(propsMap, entry.getKey(), storageUnits.get(entry.getKey()).getStorageType(), entry.getValue().getDataSource()));
            }
        }
        return result;
    }
    
    private DataSourcePoolProperties getDataSourcePoolProperties(final Map<String, DataSourcePoolProperties> propsMap, final String storageUnitName,
                                                                 final DatabaseType databaseType, final DataSource dataSource) {
        DataSourcePoolProperties result = getDataSourcePoolProperties(dataSource);
        DialectDatabaseMetaData dialectDatabaseMetaData = new DatabaseTypeRegistry(databaseType).getDialectDatabaseMetaData();
        if (dialectDatabaseMetaData.isInstanceConnectionAvailable() && propsMap.containsKey(storageUnitName)) {
            DataSourcePoolProperties unitDataSourcePoolProperties = propsMap.get(storageUnitName);
            for (Entry<String, Object> entry : unitDataSourcePoolProperties.getPoolPropertySynonyms().getStandardProperties().entrySet()) {
                if (null != entry.getValue()) {
                    result.getPoolPropertySynonyms().getStandardProperties().put(entry.getKey(), entry.getValue());
                }
            }
        }
        return result;
    }
    
    private DataSourcePoolProperties getDataSourcePoolProperties(final DataSource dataSource) {
        return dataSource instanceof CatalogSwitchableDataSource
                ? DataSourcePoolPropertiesCreator.create(((CatalogSwitchableDataSource) dataSource).getDataSource())
                : DataSourcePoolPropertiesCreator.create(dataSource);
    }
    
    private String getStandardProperty(final Map<String, Object> standardProps, final String key) {
        if (standardProps.containsKey(key) && null != standardProps.get(key)) {
            return standardProps.get(key).toString();
        }
        return "";
    }
    
    @Override
    public Class<ShowStorageUnitsStatement> getType() {
        return ShowStorageUnitsStatement.class;
    }
}
