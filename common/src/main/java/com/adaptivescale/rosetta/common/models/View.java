/*
 *  Copyright 2022 AdaptiveScale
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *            http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.adaptivescale.rosetta.common.models;

import java.util.Optional;

public class View extends Table {
    private String code;
    private Boolean materialized;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Boolean getMaterialized() {
        return materialized;
    }

    public void setMaterialized(Boolean materialized) {
        this.materialized = materialized;
    }

    public String getMaterializedString() {
        return Optional.ofNullable(getMaterialized())
                .filter(Boolean::booleanValue)
                .map(b -> "MATERIALIZED")
                .orElse("");
    }
}
