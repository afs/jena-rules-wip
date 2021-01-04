/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.seaborne.jena.rules.naive;

import java.util.stream.Stream;

import migrate.binding.Binding;
import org.seaborne.jena.rules.*;

public abstract class RulesEngineFwd implements RulesEngine {

    protected abstract RelStore doForReal();

    protected final RelStore data;
    protected final RelStore materialized;
    protected final RuleSet rules;

    protected RulesEngineFwd(RelStore data, RuleSet rules) {
        this.data = data;
        this.rules = rules;
        materialized = doForReal();
    }

    @Override
    public Stream<Binding> solve(Rel query) {
        Stream<Rel> matches = stream().filter(r -> RuleOps.provides(r, query));
        return RulesLib.bindings(matches, query);
    }

    @Override
    public Stream<Rel> stream() {
        return materialized.all();
    }

    @Override
    public RelStore materialize() {
        return materialized;
    }
}

