/*
 * Copyright © 2017-2020 Ocado (Ocava)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ocadotechnology.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.ocadotechnology.config.TestConfig.FirstSubConfig;

@DisplayName("A Config object")
@SuppressWarnings("InnerClassMayBeStatic") // @Nested classes cannot be static
class ConfigTest {

    @Test
    void isValueDefined_whenKeyIsMissing_thenThrowsConfigKeyNotFoundException() {
        Config<TestConfig> config = Config.empty(TestConfig.class);
        assertThatThrownBy(() -> config.isValueDefined(TestConfig.FOO)).isInstanceOf(ConfigKeyNotFoundException.class);
    }

    @Test
    void isValueDefined_whenValueIsExplicitlyDefinedEmpty_thenReturnsFalse() {
        Config<TestConfig> config = generateConfigWithEntry(TestConfig.FOO, "");
        assertThat(config.isValueDefined(TestConfig.FOO)).isFalse();
    }

    @Test
    void isValueDefined_whenValueIsExplicitlyDefinedBlank_thenReturnsFalse() {
        Config<TestConfig> config = generateConfigWithEntry(TestConfig.FOO, "  ");
        assertThat(config.isValueDefined(TestConfig.FOO)).isFalse();
    }

    @Test
    void isValueDefined_whenValueIsDefinedAndNonEmpty_thenReturnsTrue() {
        Config<TestConfig> config = generateConfigWithEntry(TestConfig.FOO, "Non empty value");
        assertThat(config.isValueDefined(TestConfig.FOO)).isTrue();
    }

    @Test
    void areKeyAndValueDefined_whenKeyIsNotDefined_thenReturnsFalse() {
        Config<TestConfig> config = Config.empty(TestConfig.class);
        assertThat(config.areKeyAndValueDefined(TestConfig.FOO)).isFalse();
    }

    @Test
    void areKeyAndValueDefined_whenValueIsExplicitlyDefinedEmpty_thenReturnsFalse() {
        Config<TestConfig> config = generateConfigWithEntry(TestConfig.FOO, "");
        assertThat(config.areKeyAndValueDefined(TestConfig.FOO)).isFalse();
    }

    @Test
    void areKeyAndValueDefined_whenValueIsExplicitlyDefinedBlank_thenReturnsFalse() {
        Config<TestConfig> config = generateConfigWithEntry(TestConfig.FOO, "  ");
        assertThat(config.areKeyAndValueDefined(TestConfig.FOO)).isFalse();
    }

    @Test
    void areKeyAndValueDefined_whenValueIsDefined_thenReturnsTrue() {
        Config<TestConfig> config = generateConfigWithEntry(TestConfig.FOO, "Non empty value");
        assertThat(config.areKeyAndValueDefined(TestConfig.FOO)).isTrue();
    }

    @Test
    void enumTypeMatches_whenEnumKeyIsInConfig_thenReturnsTrue() {
        Config<TestConfig> config = SimpleConfigBuilder.create(TestConfig.class)
                .put(TestConfig.FOO, "Non empty value")
                .put(TestConfig.BAR, "")
                .buildWrapped();

        assertThat(config.enumTypeMatches(TestConfig.FOO.getClass()))
                .as("Config enum type should contain this enum key").isTrue();
        assertThat(config.enumTypeMatches(TestConfig.BAR.getClass()))
                .as("Config enum type should contain the enum key even if its value is explicitly set to empty in the config").isTrue();
        assertThat(config.enumTypeMatches(TestConfig.BAZ.getClass()))
                .as("Config enum type should contain the enum key even if its value is not set in the config").isTrue();
        assertThat(config.enumTypeMatches(FirstSubConfig.HOO.getClass()))
                .as("Config enum type should contain sub config enum key").isTrue();
        assertThat(config.enumTypeMatches(FirstSubConfig.SubSubConfig.X.getClass()))
                .as("Config enum type should contain sub sub config enum key").isTrue();

        Config<TestConfig.FirstSubConfig> subConfig = config.getSubConfig(TestConfig.FirstSubConfig.class);
        assertThat(subConfig.enumTypeMatches(FirstSubConfig.SubSubConfig.X.getClass()))
                .as("Sub config should contain sub sub config enum key").isTrue();
        assertThat(subConfig.getSubConfig(TestConfig.FirstSubConfig.SubSubConfig.class).enumTypeMatches(FirstSubConfig.SubSubConfig.X.getClass()))
                .as("Sub sub config should contain this enum key").isTrue();
    }

    @Test
    void enumTypeMatches_whenKeyCannotBeConfigured_thenReturnsFalse() {
        Config<TestConfig> config =  SimpleConfigBuilder.create(TestConfig.class).buildWrapped();

        assertThat(config.enumTypeMatches(TestConfigDummy.FOO.getClass()))
                .as("Config enum type should not contain enum key of wrong config").isFalse();
        assertThat(config.getSubConfig(TestConfig.SecondSubConfig.class).enumTypeMatches(FirstSubConfig.SubSubConfig.X.getClass()))
                .as("Sub config enum should not contain enum key of different sub config").isFalse();
        assertThat(config.getSubConfig(TestConfig.SecondSubConfig.class).enumTypeMatches(TestConfig.FOO.getClass()))
                .as("Sub config enum should not contain enum key from parent config").isFalse();
    }

    @Test
    void enumTypeIncludes_whenEnumKeyIsInConfig_thenReturnsTrue() {
        Config<TestConfig> config = SimpleConfigBuilder.create(TestConfig.class)
                .put(TestConfig.FOO, "Non empty value")
                .put(TestConfig.BAR, "")
                .buildWrapped();

        assertThat(config.enumTypeIncludes(TestConfig.FOO))
                .as("Config enum type should contain this enum key").isTrue();
        assertThat(config.enumTypeIncludes(TestConfig.BAR))
                .as("Config enum type should contain the enum key even if its value is explicitly set to empty in the config").isTrue();
        assertThat(config.enumTypeIncludes(TestConfig.BAZ))
                .as("Config enum type should contain the enum key even if its value is not set in the config").isTrue();
        assertThat(config.enumTypeIncludes(FirstSubConfig.HOO))
                .as("Config enum type should contain sub config enum key").isTrue();
        assertThat(config.enumTypeIncludes(FirstSubConfig.SubSubConfig.X))
                .as("Config enum type should contain sub sub config enum key").isTrue();

        Config<TestConfig.FirstSubConfig> subConfig = config.getSubConfig(TestConfig.FirstSubConfig.class);
        assertThat(subConfig.enumTypeIncludes(FirstSubConfig.SubSubConfig.X))
                .as("Sub config should contain sub sub config enum key").isTrue();
        assertThat(subConfig.getSubConfig(TestConfig.FirstSubConfig.SubSubConfig.class).enumTypeIncludes(FirstSubConfig.SubSubConfig.X))
                .as("Sub sub config should contain this enum key").isTrue();
    }

    @Test
    void enumTypeIncludes_whenKeyCannotBeConfigured_thenReturnsFalse() {
        Config<TestConfig> config =  SimpleConfigBuilder.create(TestConfig.class).buildWrapped();

        assertThat(config.enumTypeIncludes(TestConfigDummy.FOO))
                .as("Config enum type should not contain enum key of wrong config").isFalse();
        assertThat(config.getSubConfig(TestConfig.SecondSubConfig.class).enumTypeIncludes(FirstSubConfig.SubSubConfig.X))
                .as("Sub config enum should not contain enum key of different sub config").isFalse();
        assertThat(config.getSubConfig(TestConfig.SecondSubConfig.class).enumTypeIncludes(TestConfig.FOO))
                .as("Sub config enum should not contain enum key from parent config").isFalse();
    }

    @Test
    void getValue_whenKeyHasValue_thenReturnsParserWithValue() {
        String testValue = "TEST_VALUE";
        Config<TestConfig> config =  SimpleConfigBuilder.create(TestConfig.class)
                .put(TestConfig.FOO, testValue)
                .buildWrapped();

        assertThat(config.getValue(TestConfig.FOO).asString()).isEqualTo(testValue);
    }

    @Test
    void getValue_whenKeyHasPaddedValue_thenReturnsParserWithTrimmedValue() {
        Config<TestConfig> config =  SimpleConfigBuilder.create(TestConfig.class)
                .put(TestConfig.FOO, " TEST_VALUE ")
                .buildWrapped();

        assertThat(config.getValue(TestConfig.FOO).asString()).isEqualTo("TEST_VALUE");
    }

    @Test
    void getValue_whenKeyHasEmptyStringValue_thenReturnsParserWithValue() {
        String testValue = "";
        Config<TestConfig> config =  SimpleConfigBuilder.create(TestConfig.class)
                .put(TestConfig.FOO, testValue)
                .buildWrapped();

        assertThat(config.getValue(TestConfig.FOO).asString()).isEqualTo(testValue);
    }

    @Test
    void getValue_whenKeyNotDefined_thenThrowsException() {
        Config<TestConfig> config =  SimpleConfigBuilder.create(TestConfig.class).buildWrapped();

        assertThatThrownBy(() -> config.getValue(TestConfig.FOO)).isInstanceOf(ConfigKeyNotFoundException.class);
    }

    @Test
    void getIfValueDefined_whenKeyHasValue_thenReturnsParserWithValue() {
        String testValue = "TEST_VALUE";
        Config<TestConfig> config =  SimpleConfigBuilder.create(TestConfig.class)
                .put(TestConfig.FOO, testValue)
                .buildWrapped();

        assertThat(config.getIfValueDefined(TestConfig.FOO).asString().get()).isEqualTo(testValue);
    }

    @Test
    void getIfValueDefined_whenKeyHasPaddedValue_thenReturnsParserTrimmedWithValue() {
        Config<TestConfig> config =  SimpleConfigBuilder.create(TestConfig.class)
                .put(TestConfig.FOO, " TEST_VALUE ")
                .buildWrapped();

        assertThat(config.getIfValueDefined(TestConfig.FOO).asString().get()).isEqualTo("TEST_VALUE");
    }

    @Test
    void getIfValueDefined_whenKeyHasEmptyStringValue_thenReturnsParserWithoutValue() {
        String testValue = "";
        Config<TestConfig> config =  SimpleConfigBuilder.create(TestConfig.class)
                .put(TestConfig.FOO, testValue)
                .buildWrapped();

        assertThat(config.getIfValueDefined(TestConfig.FOO).asString().isPresent()).isFalse();
    }

    @Test
    void getIfValueDefined_whenKeyNotDefined_thenThrowsException() {
        Config<TestConfig> config =  SimpleConfigBuilder.create(TestConfig.class).buildWrapped();

        assertThatThrownBy(() -> config.getIfValueDefined(TestConfig.FOO)).isInstanceOf(ConfigKeyNotFoundException.class);
    }

    @Test
    void getIfKeyAndValueDefined_whenKeyHasValue_thenReturnsParserWithValue() {
        String testValue = "TEST_VALUE";
        Config<TestConfig> config =  SimpleConfigBuilder.create(TestConfig.class)
                .put(TestConfig.FOO, testValue)
                .buildWrapped();

        assertThat(config.getIfKeyAndValueDefined(TestConfig.FOO).asString().get()).isEqualTo(testValue);
    }

    @Test
    void getIfKeyAndValueDefined_whenKeyHasPaddedValue_thenReturnsParserWithTrimmedValue() {
        Config<TestConfig> config =  SimpleConfigBuilder.create(TestConfig.class)
                .put(TestConfig.FOO, " TEST_VALUE ")
                .buildWrapped();

        assertThat(config.getIfKeyAndValueDefined(TestConfig.FOO).asString().get()).isEqualTo("TEST_VALUE");
    }

    @Test
    void getIfKeyAndValueDefined_whenKeyHasEmptyStringValue_thenReturnsParserWithoutValue() {
        String testValue = "";
        Config<TestConfig> config =  SimpleConfigBuilder.create(TestConfig.class)
                .put(TestConfig.FOO, testValue)
                .buildWrapped();

        assertThat(config.getIfKeyAndValueDefined(TestConfig.FOO).asString()).isEmpty();
    }

    @Test
    void getIfKeyAndValueDefined_whenKeyNotDefined_thenThrowsException() {
        Config<TestConfig> config =  SimpleConfigBuilder.create(TestConfig.class).buildWrapped();

        assertThat(config.getIfKeyAndValueDefined(TestConfig.FOO).asString().isPresent()).isFalse();
    }

    @Nested
    @DisplayName("toString() method")
    class ToStringMethodTest {

        @Test
        @DisplayName("contains type and empty list of properties for empty config")
        void emptyConfig() {
            assertThat(Config.empty(TestConfig.class).toString()).isEqualToIgnoringWhitespace("TestConfig{}");
        }

        @Test
        @DisplayName("includes the specified config entries")
        void containsConfigEntries() {
            Config<TestConfig> config = generateConfigWithEntry(TestConfig.FOO, "foo");
            assertThat(config.toString()).isEqualToIgnoringWhitespace("TestConfig{TestConfig.FOO=foo}");
        }

        @Test
        @DisplayName("provides the full key name of nested enums")
        void nestedEnums() {
            Config<TestConfig> config = SimpleConfigBuilder.create(TestConfig.class).put("TestConfig.FirstSubConfig.SubSubConfig.X", "x").buildWrapped();
            assertThat(config.toString()).contains("TestConfig.FirstSubConfig.SubSubConfig.X=x");
        }

        @Test
        @DisplayName("lists one config entry per-line")
        void listsConfigOnIndividualLines() {
            Config<TestConfig> config = SimpleConfigBuilder.create(TestConfig.class)
                    .put("TestConfig.FOO", "foo")
                    .put("TestConfig.BAR", "bar")
                    .buildWrapped();

            assertThat(config.toString())
                    .contains("\nTestConfig.FOO=foo\n")
                    .contains("\nTestConfig.BAR=bar\n")
                    .hasLineCount(4);
        }

        @Test
        @DisplayName("does not contain value of SecretConfig")
        void secretConfig() {
            Config<TestConfig> config = SimpleConfigBuilder.create(TestConfig.class)
                    .put("TestConfig.SECRET_1", "secret1")
                    .put("TestConfig.FirstSubConfig.SECRET_2", "s2")
                    .buildWrapped();

            assertThat(config.toString())
                    .doesNotContain("TestConfig.SECRET_1")
                    .doesNotContain("TestConfig.FirstSubConfig.SECRET_2");
        }

        @Test
        @DisplayName("does not contain value of SecretConfig but does contain value of non-secret config")
        void secretAndClearConfig() {
            Config<TestConfig> config = SimpleConfigBuilder.create(TestConfig.class)
                    .put("TestConfig.SECRET_1", "secret1")
                    .put("TestConfig.FOO", "foo")
                    .buildWrapped();

            assertThat(config.toString())
                    .doesNotContain("TestConfig.SECRET_1")
                    .contains("TestConfig.FOO=foo");
        }

        @Test
        @DisplayName("contains prefixed keys")
        void prefixedValues() {
            Config<TestConfig> config = SimpleConfigBuilder.create(TestConfig.class)
                    .put("prefix@TestConfig.FOO", "bar")
                    .put("TestConfig.FOO", "foo")
                    .buildWrapped();

            assertThat(config.toString())
                    .contains("prefix@TestConfig.FOO=bar")
                    .contains("TestConfig.FOO=foo");
        }
    }

    private static Config<TestConfig> generateConfigWithEntry(Enum<?> key, String value) {
        return SimpleConfigBuilder.create(TestConfig.class)
                .put(key.getClass().getSimpleName() + "." + key.name(), value)
                .buildWrapped();
    }
}
