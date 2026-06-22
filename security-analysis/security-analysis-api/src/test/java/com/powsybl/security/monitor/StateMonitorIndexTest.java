/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.monitor;

import com.powsybl.contingency.ContingencyContext;
import com.powsybl.contingency.ContingencyContextType;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static com.powsybl.security.monitor.StateMonitorResultMode.ALL;
import static com.powsybl.security.monitor.StateMonitorResultMode.VIOLATIONS_ONLY;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Roman Vykuka {@literal <roman.vykuka at unicorn.com>}
 */
class StateMonitorIndexTest {
    @Test
    void resultModesAreMergedPerElement() {
        StateMonitorIndex index = new StateMonitorIndex(List.of(
            new StateMonitor(new ContingencyContext(null, ContingencyContextType.ALL),
                Set.of("branch1", "branch2"),
                Set.of("vl1", "vl2"),
                Set.of("threeWindingsTransformer1", "threeWindingsTransformer2"),
                VIOLATIONS_ONLY),
            new StateMonitor(new ContingencyContext(null, ContingencyContextType.ALL),
                Set.of("branch1"),
                Set.of("vl1"),
                Set.of("threeWindingsTransformer1"),
                ALL)));

        StateMonitor monitor = index.getAllStateMonitor();

        assertEquals(ALL, index.getBranchResultMode(monitor, "branch1"));
        assertEquals(VIOLATIONS_ONLY, index.getBranchResultMode(monitor, "branch2"));
        assertEquals(ALL, index.getVoltageLevelResultMode(monitor, "vl1"));
        assertEquals(VIOLATIONS_ONLY, index.getVoltageLevelResultMode(monitor, "vl2"));
        assertEquals(ALL, index.getThreeWindingsTransformerResultMode(monitor, "threeWindingsTransformer1"));
        assertEquals(VIOLATIONS_ONLY, index.getThreeWindingsTransformerResultMode(monitor, "threeWindingsTransformer2"));
    }

    @Test
    void resultModesAreIndexedForSpecificStateMonitors() {
        StateMonitorIndex index = new StateMonitorIndex(List.of(
            new StateMonitor(new ContingencyContext("contingency1", ContingencyContextType.SPECIFIC),
                Set.of("branch1", "branch2"),
                Set.of("vl1", "vl2"),
                Set.of("threeWindingsTransformer1", "threeWindingsTransformer2"),
                VIOLATIONS_ONLY),
            new StateMonitor(new ContingencyContext("contingency1", ContingencyContextType.SPECIFIC),
                Set.of("branch1"),
                Set.of("vl1"),
                Set.of("threeWindingsTransformer1"),
                ALL)));

        StateMonitor monitor = index.getSpecificStateMonitors().get("contingency1");

        assertEquals(ALL, index.getBranchResultMode(monitor, "branch1"));
        assertEquals(VIOLATIONS_ONLY, index.getBranchResultMode(monitor, "branch2"));
        assertEquals(VIOLATIONS_ONLY, index.getVoltageLevelResultMode(monitor, "vl2"));
        assertEquals(VIOLATIONS_ONLY, index.getThreeWindingsTransformerResultMode(monitor, "threeWindingsTransformer2"));
    }
}
