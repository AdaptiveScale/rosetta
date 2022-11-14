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

package integration;

import com.adaptivescale.rosetta.cli.ConfigYmlConverter;
import com.adaptivescale.rosetta.cli.model.Config;
import org.junit.Rule;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetEnvironmentVariable;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;


public class EnvVariableTest {

    private final String BOB_PASSWORD = "test1";
    private final String PASSWORD = "test2";

    @Rule
    public final EnvironmentVariables environmentVariables
            = new EnvironmentVariables();
    @Test
    @DisplayName("Test env variable processing")
    @SetEnvironmentVariable(key = "BOB_PASSWORD", value = BOB_PASSWORD)
    @SetEnvironmentVariable(key = "PASSWORD", value = PASSWORD)
    public void passEnvVariable() throws Exception {
        Path resourceDirectory = Paths.get("src", "test", "resources");
        File file = resourceDirectory.resolve("env_main.conf").toFile();
        ConfigYmlConverter configYmlConverter = new ConfigYmlConverter();

        Config config = configYmlConverter.convert(file.getAbsolutePath());
        Assertions.assertEquals(config.getConnections().get(0).getPassword(), BOB_PASSWORD);
        Assertions.assertEquals(config.getConnections().get(1).getPassword(), PASSWORD);
    }
}
