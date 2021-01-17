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

package org.seaborne.jena.rules.exec.sld;

import java.util.Iterator;
import java.util.Objects;
import java.util.stream.Stream;

import migrate.binding.Binding;
import migrate.binding.Sub;
import org.apache.jena.atlas.iterator.Iter;
import org.seaborne.jena.rules.*;
import org.seaborne.jena.rules.store.RelStoreBuilder;

/** Non-recursive SLR Resolution */
public class RulesEngineSLD_NR extends RulesEngineBkd {

    public static RulesEngine.Factory factory = RulesEngineSLD_NR::new;

    protected RulesEngineSLD_NR(RelStore data, RuleSet rules) {
        super(data, rules);
    }

    @Override
    public EngineType engineType() { return EngineType.BKD_NON_RECURSIVE_SLD; }

    @Override
    public Stream<Rel> stream() {
        return rules.rules().stream().flatMap(r->{
            Rel query = r.getHead();
            Stream<Binding> matches = solve(query);
            return matches.map(binding->Sub.substitute(binding, query)).filter(Objects::nonNull);
        });
    }

    @Override
    public RelStore materialize() {
        RelStoreBuilder builder = RelStoreFactory.create();
        Stream.concat(data.stream(), stream()).forEach(builder::add);
        return builder.build();
    }

    @Override
    public Stream<Binding> solve(Rel query) {
        Iterator<Binding> iter = BkdSolver.solver(query, rules, data);
        return Iter.asStream(iter);
    }
}

