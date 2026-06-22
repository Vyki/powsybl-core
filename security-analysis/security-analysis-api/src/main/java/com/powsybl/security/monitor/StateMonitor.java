/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.monitor;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.contingency.ContingencyContext;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 *  A state monitor allows to output in security analysis results some state variables related to branches, buses and three
 *  windings transformers. The supported state variables are active power, reactive power and current at both side for branches
 *  (see {@link com.powsybl.security.results.BranchResult}), active power, reactive power and current at voltage level side for
 *  three windings transformers (see {@link com.powsybl.security.results.ThreeWindingsTransformerResult}) and voltage angle and voltage
 *  magnitude for buses (see {@link com.powsybl.security.results.BusResult}).
 *  <p>
 *  A state monitor is defined for some contingencies through a {@link ContingencyContext}.
 *  A {@link com.powsybl.contingency.ContingencyContextType#NONE} or {@link com.powsybl.contingency.ContingencyContextType#ALL}
 *  (because it includes the base case) contingency context will output monitoring results in
 *  {@link com.powsybl.security.results.PreContingencyResult}.
 *  If a contingency has a state monitor declared (through a {@link com.powsybl.contingency.ContingencyContextType#ALL},
 *  a {@link com.powsybl.contingency.ContingencyContextType#ONLY_CONTINGENCIES} or a {@link com.powsybl.contingency.ContingencyContextType#SPECIFIC}),
 *  monitoring results are output in the dedicated {@link com.powsybl.security.results.PostContingencyResult}.
 *
 * @author Etienne Lesot {@literal <etienne.lesot at rte-france.com>}
 */
public class StateMonitor {

    /**
     * define on which situation information are needed
     */
    private final ContingencyContext contingencyContext;

    /**
     * branchs ids on which information will be collected
     */
    private final Set<String> branchIds = new LinkedHashSet<>();

    /**
     * voltageLevels ids on which information will be collected
     */
    private final Set<String> voltageLevelIds = new LinkedHashSet<>();

    /**
     * voltageLevels ids on which information will be collected
     */
    private final Set<String> threeWindingsTransformerIds = new LinkedHashSet<>();

    /**
     * Defines which monitored element results are included.
     */
    private StateMonitorResultMode resultMode;

    public ContingencyContext getContingencyContext() {
        return contingencyContext;
    }

    public Set<String> getBranchIds() {
        return branchIds;
    }

    public Set<String> getVoltageLevelIds() {
        return voltageLevelIds;
    }

    public Set<String> getThreeWindingsTransformerIds() {
        return threeWindingsTransformerIds;
    }

    public StateMonitorResultMode getResultMode() {
        return resultMode;
    }

    public StateMonitor(ContingencyContext contingencyContext, Set<String> branchIds, Set<String> voltageLevelIds,
                        Set<String> threeWindingsTransformerIds) {
        this(contingencyContext, branchIds, voltageLevelIds, threeWindingsTransformerIds, StateMonitorResultMode.ALL);
    }

    @JsonCreator
    public StateMonitor(@JsonProperty("contingencyContext") ContingencyContext contingencyContext,
                        @JsonProperty("branchIds") Set<String> branchIds,
                        @JsonProperty("voltageLevelIds") Set<String> voltageLevelIds,
                        @JsonProperty("threeWindingsTransformerIds") Set<String> threeWindingsTransformerIds,
                        @JsonProperty("resultMode") StateMonitorResultMode resultMode) {
        this.contingencyContext = Objects.requireNonNull(contingencyContext);
        this.branchIds.addAll(Objects.requireNonNull(branchIds));
        this.voltageLevelIds.addAll(Objects.requireNonNull(voltageLevelIds));
        this.threeWindingsTransformerIds.addAll(Objects.requireNonNull(threeWindingsTransformerIds));
        this.resultMode = Objects.requireNonNullElse(resultMode, StateMonitorResultMode.ALL);
    }

    public StateMonitor merge(StateMonitor monitorTobeMerged) {
        this.branchIds.addAll(monitorTobeMerged.getBranchIds());
        this.voltageLevelIds.addAll(monitorTobeMerged.getVoltageLevelIds());
        this.threeWindingsTransformerIds.addAll(monitorTobeMerged.getThreeWindingsTransformerIds());
        if (monitorTobeMerged.getResultMode() == StateMonitorResultMode.ALL) {
            this.resultMode = StateMonitorResultMode.ALL;
        }
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        StateMonitor that = (StateMonitor) o;
        return Objects.equals(contingencyContext, that.contingencyContext) &&
            Objects.equals(branchIds, that.branchIds) &&
            Objects.equals(voltageLevelIds, that.voltageLevelIds) &&
            Objects.equals(threeWindingsTransformerIds, that.threeWindingsTransformerIds) &&
            Objects.equals(resultMode, that.resultMode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(contingencyContext, branchIds, voltageLevelIds, threeWindingsTransformerIds, resultMode);
    }

    @Override
    public String toString() {
        return "StateMonitor{" +
            "contingencyContext=" + contingencyContext +
            ", branchIds=" + branchIds +
            ", voltageLevelIds=" + voltageLevelIds +
            ", threeWindingsTransformerIds=" + threeWindingsTransformerIds +
            ", resultMode=" + resultMode +
            '}';
    }

    public static void write(List<StateMonitor> monitors, Path jsonFile) {
        try {
            Files.writeString(jsonFile, JsonUtil.createObjectMapper().writer().writeValueAsString(monitors) + "\n");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static List<StateMonitor> read(Path jsonFile) {
        try {
            return JsonUtil.createObjectMapper().readerFor(new TypeReference<List<StateMonitor>>() {
            }).readValue(Files.newInputStream(jsonFile));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
