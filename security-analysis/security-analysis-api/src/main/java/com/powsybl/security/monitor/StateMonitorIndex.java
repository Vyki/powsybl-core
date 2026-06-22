/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.monitor;

import com.powsybl.contingency.ContingencyContext;
import com.powsybl.contingency.ContingencyContextType;

import java.util.*;

/**
 * @author Etienne Lesot {@literal <etienne.lesot at rte-france.com>}
 * index all state monitors according to their ContingencyContextType
 *
 */
public class StateMonitorIndex {
    private final StateMonitor allStateMonitor;
    private final ResultModes allStateMonitorResultModes = new ResultModes();
    private final StateMonitor noneStateMonitor;
    private final ResultModes noneStateMonitorResultModes = new ResultModes();
    private final Map<String, StateMonitor> specificStateMonitors = new HashMap<>();
    private final Map<String, ResultModes> specificStateMonitorResultModes = new HashMap<>();

    public StateMonitorIndex(List<StateMonitor> stateMonitors) {
        allStateMonitor = new StateMonitor(new ContingencyContext(null, ContingencyContextType.ALL),
                new HashSet<>(), new HashSet<>(), new HashSet<>());
        noneStateMonitor = new StateMonitor(new ContingencyContext(null, ContingencyContextType.NONE),
                new HashSet<>(), new HashSet<>(), new HashSet<>());
        stateMonitors.forEach(stateMonitor -> {
            String id = stateMonitor.getContingencyContext().getContingencyId();
            if (id != null) {
                this.specificStateMonitors.merge(id, stateMonitor, StateMonitor::merge);
                this.specificStateMonitorResultModes.computeIfAbsent(id, key -> new ResultModes()).add(stateMonitor);
            } else if (stateMonitor.getContingencyContext().getContextType() == ContingencyContextType.ALL) {
                allStateMonitor.merge(stateMonitor);
                allStateMonitorResultModes.add(stateMonitor);
            } else if (stateMonitor.getContingencyContext().getContextType() == ContingencyContextType.NONE) {
                noneStateMonitor.merge(stateMonitor);
                noneStateMonitorResultModes.add(stateMonitor);
            }
        });
    }

    public StateMonitor getAllStateMonitor() {
        return allStateMonitor;
    }

    public StateMonitorResultMode getBranchResultMode(StateMonitor monitor, String branchId) {
        return getResultModes(monitor).getBranchResultMode(branchId);
    }

    public StateMonitorResultMode getVoltageLevelResultMode(StateMonitor monitor, String voltageLevelId) {
        return getResultModes(monitor).getVoltageLevelResultMode(voltageLevelId);
    }

    public StateMonitorResultMode getThreeWindingsTransformerResultMode(StateMonitor monitor, String threeWindingsTransformerId) {
        return getResultModes(monitor).getThreeWindingsTransformerResultMode(threeWindingsTransformerId);
    }

    public StateMonitor getNoneStateMonitor() {
        return noneStateMonitor;
    }

    public Map<String, StateMonitor> getSpecificStateMonitors() {
        return specificStateMonitors;
    }

    private ResultModes getResultModes(StateMonitor monitor) {
        if (monitor == allStateMonitor) {
            return allStateMonitorResultModes;
        }
        if (monitor == noneStateMonitor) {
            return noneStateMonitorResultModes;
        }
        return specificStateMonitors.entrySet().stream()
            .filter(entry -> entry.getValue() == monitor)
            .map(entry -> specificStateMonitorResultModes.get(entry.getKey()))
            .findFirst()
            .orElse(ResultModes.ALL);
    }

    private static final class ResultModes {
        private static final ResultModes ALL = new ResultModes();

        private final Map<String, StateMonitorResultMode> branchResultModes = new HashMap<>();
        private final Map<String, StateMonitorResultMode> voltageLevelResultModes = new HashMap<>();
        private final Map<String, StateMonitorResultMode> threeWindingsTransformerResultModes = new HashMap<>();

        private void add(StateMonitor stateMonitor) {
            stateMonitor.getBranchIds().forEach(branchId -> add(branchResultModes, branchId, stateMonitor.getResultMode()));
            stateMonitor.getVoltageLevelIds().forEach(voltageLevelId -> add(voltageLevelResultModes, voltageLevelId, stateMonitor.getResultMode()));
            stateMonitor.getThreeWindingsTransformerIds().forEach(threeWindingsTransformerId -> add(threeWindingsTransformerResultModes, threeWindingsTransformerId, stateMonitor.getResultMode()));
        }

        private StateMonitorResultMode getBranchResultMode(String branchId) {
            return branchResultModes.getOrDefault(branchId, StateMonitorResultMode.ALL);
        }

        private StateMonitorResultMode getVoltageLevelResultMode(String voltageLevelId) {
            return voltageLevelResultModes.getOrDefault(voltageLevelId, StateMonitorResultMode.ALL);
        }

        private StateMonitorResultMode getThreeWindingsTransformerResultMode(String threeWindingsTransformerId) {
            return threeWindingsTransformerResultModes.getOrDefault(threeWindingsTransformerId, StateMonitorResultMode.ALL);
        }

        private static void add(Map<String, StateMonitorResultMode> resultModes, String elementId, StateMonitorResultMode resultMode) {
            resultModes.merge(elementId, resultMode, ResultModes::merge);
        }

        private static StateMonitorResultMode merge(StateMonitorResultMode resultMode1, StateMonitorResultMode resultMode2) {
            return resultMode1 == StateMonitorResultMode.ALL || resultMode2 == StateMonitorResultMode.ALL ? StateMonitorResultMode.ALL : StateMonitorResultMode.VIOLATIONS_ONLY;
        }
    }
}
