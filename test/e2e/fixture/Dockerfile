#
# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
FROM alpine AS prepare

ARG APP_NAME

ADD target/${APP_NAME}.tar.gz /opt
RUN mv /opt/${APP_NAME} /opt/shardingsphere-proxy

WORKDIR /opt/shardingsphere-proxy

FROM eclipse-temurin:8-jdk

COPY --from=prepare /opt/shardingsphere-proxy /opt/shardingsphere-proxy

EXPOSE 3307
EXPOSE 3308

CMD /opt/shardingsphere-proxy/bin/start.sh
