<!---
 Licensed to the Apache Software Foundation (ASF) under one or more
 contributor license agreements.  See the NOTICE file distributed with
 this work for additional information regarding copyright ownership.
 The ASF licenses this file to You under the Apache License, Version 2.0
 (the "License"); you may not use this file except in compliance with
 the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
-->

Apache [Maven Distribution Tool][report] Plugin
============

[![ASF Jira](https://img.shields.io/endpoint?url=https%3A%2F%2Fmaven.apache.org%2Fbadges%2Fasf_jira-MNGSITE.json)][jira]
[![Apache License, Version 2.0, January 2004](https://img.shields.io/github/license/apache/maven.svg?label=License)][license]
[![Jenkins Status](https://img.shields.io/jenkins/s/https/builds.apache.org/job/maven-box/job/maven/job/master.svg)][build]


Maven Distribution Tool plugin is a tool executed daily on Maven CI server to produce [a report][report]
of the different checks done on our releases.

Quick Build
-------
```
mvn clean install site
```

[report]: https://builds.apache.org/view/M-R/view/Maven/job/dist-tool-plugin/site/
[jira]: https://issues.apache.org/jira/projects/MNGSITE/
[license]: https://www.apache.org/licenses/LICENSE-2.0
[build]: https://builds.apache.org/job/dist-tool-plugin/

