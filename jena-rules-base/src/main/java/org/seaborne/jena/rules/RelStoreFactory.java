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

package org.seaborne.jena.rules;

import org.seaborne.jena.rules.impl.RelStore2;
import org.seaborne.jena.rules.impl.RelStoreSimple;

public class RelStoreFactory {
    private RelStoreFactory() {}

    public static RelStore create() {
        return new RelStoreSimple();
    }
    
    /** Create a {@link RelStore} that is suitable for temporary working data. */
    public static RelStore createMem() {
        return new RelStoreSimple();
    }

    /** Merge two {@link RelStore}s into one. */ 
    public static RelStore combine(RelStore data, RelStore acc) {
        //XXX Do better!
        RelStore rs = createMem();
        rs.add(data);
        rs.add(acc);
        return rs;
    }
    
    public static RelStore union(RelStore rs1, RelStore rs2) {
        return new RelStore2(rs1, rs2);
    }
}

