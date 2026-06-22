/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.monitor;

/**
 * Defines which monitored element results are included for a state monitor.
 *
 * @author Roman Vykuka {@literal <roman.vykuka at unicorn.com>}
 */
public enum StateMonitorResultMode {
    /**
     * Include results for all monitored elements.
     */
    ALL,

    /**
     * Include results only for monitored elements having at least one limit violation.
     */
    VIOLATIONS_ONLY
}
