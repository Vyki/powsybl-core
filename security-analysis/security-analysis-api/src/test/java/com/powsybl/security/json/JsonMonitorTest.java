/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.json;

import com.powsybl.commons.test.AbstractSerDeTest;
import com.powsybl.contingency.ContingencyContext;
import com.powsybl.contingency.ContingencyContextType;
import com.powsybl.security.monitor.StateMonitor;
import com.powsybl.security.monitor.StateMonitorResultMode;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Etienne Lesot {@literal <etienne.lesot at rte-france.com>}
 */
class JsonMonitorTest extends AbstractSerDeTest {
    @Test
    void roundTrip() throws IOException {
        List<StateMonitor> monitors = new ArrayList<>();
        monitors.add(new StateMonitor(new ContingencyContext("contingency1", ContingencyContextType.SPECIFIC),
            Collections.singleton("Branch1"), Collections.singleton("Bus1"), Collections.singleton("ThreeWindingsTransformer1"),
            StateMonitorResultMode.VIOLATIONS_ONLY));
        roundTripTest(monitors, StateMonitor::write, StateMonitor::read, "/MonitoringFileTest.json");
    }

    @Test
    void readWithoutResultMode() throws IOException {
        Path jsonFile = tmpDir.resolve("monitoring-file-without-result-mode.json");
        try (InputStream input = getClass().getResourceAsStream("/MonitoringFileWithoutResultMode.json")) {
            Files.copy(Objects.requireNonNull(input), jsonFile);
        }
        List<StateMonitor> monitors = StateMonitor.read(jsonFile);

        assertEquals(1, monitors.size());
        assertEquals(StateMonitorResultMode.ALL, monitors.getFirst().getResultMode());
    }
}
