<template>
    <div style="max-width: 100%;" class="singleParameter">
        <div class="header clickable headline-2">
            <div class="topic" @click="toggleVisibility('parameters', 'toggleButton')">
                <div ref="toggleButton" class="noselect button toggleButton expand-icon"/>
                <div class="noselect text">{{ this.headline }}</div>
            </div>
        </div>
        <ul ref="parameters" style="display: none; max-height: 0; opacity: 0;" class="parameter">
            <li class="parameter" v-for="(parameter, parameterIndex) in this.parameters" :key="'global_' + parameterIndex">
                <div class="clickable headline-3">
                    <div class="topic" @click="toggleVisibility('parameter_' + parameterIndex, 'toggleButton_parameter_' + parameterIndex)">
                        <div :ref="'toggleButton_parameter_' + parameterIndex" class="noselect button toggleButton expand-icon"/>
                        <div class="noselect text" style="cursor:pointer;" @click="toggleVisibility('parameter_' + parameterIndex, 'toggleButton_parameter_' + parameterIndex)">{{ getParameterNames(parameter) }} <code v-if="parameter.required">REQUIRED</code></div>
                    </div>
                </div>
                <div>
                    <p style="font-style: italic; padding-left: 1.4rem">{{ parameter.description }}</p>
                    <div :ref="'parameter_' + parameterIndex" style="display: none; max-height: 0; opacity: 0; padding-left: 1.3rem;">
                        <ul class="parameterInfo" style="overflow: hidden;">
                            <li>Type: <code>{{ parameter.className }}</code></li>
                            <li>
                                Default value:
                                <code v-if="parameter.defaultValue">{{ parameter.defaultValue }}</code>
                                <code v-else>&lt;none&gt;</code>
                            </li>
                            <li>Possible values: <code>{{ parameter.possibleValues }}</code></li>
                        </ul>
                        <div v-if="parameter.examples.length > 0" ref="examples" style="overflow: hidden; margin-top: 1rem;">
                            <div class="clickable">
                                <div class="topic">
                                    <div :ref="'toggleButton_examples_' + parameterIndex" class="noselect button toggleButton expand-icon" @click="toggleVisibility('parameter_examples_' + parameterIndex, 'toggleButton_examples_' + parameterIndex)"/>
                                    <div class="noselect text headline-4" @click="toggleVisibility('parameter_examples_' + parameterIndex, 'toggleButton_examples_' + parameterIndex)">Examples</div>
                                </div>
                            </div>
                            <ul class="parameter-examples" :ref="'parameter_examples_' + parameterIndex" style="display: none; max-height: 0; opacity: 0; overflow: hidden;">
                                <li v-for="(example, exampleIndex) in parameter.examples" :key="exampleIndex" class="example">
                                    <div class="clickable">
                                        <div class="topic">
                                            <div :ref="'toggleButton_example_' + parameterIndex + '_' + exampleIndex" style="transform: rotate(0deg);" class="noselect button toggleButton expand-icon" @click="toggleVisibility('parameter_example_' + parameterIndex + '_' + exampleIndex, 'toggleButton_example_' + parameterIndex + '_' + exampleIndex)"/>
                                            <p class="noselect text" style="font-style: italic; font-size: 14px;" @click="toggleVisibility('parameter_example_' + parameterIndex + '_' + exampleIndex, 'toggleButton_example_' + parameterIndex + '_' + exampleIndex)">{{ example.description }}</p>
                                        </div>
                                    </div>
                                    <pre :ref="'parameter_example_' + parameterIndex + '_' + exampleIndex" style="display: block; max-height: 9999px; opacity: 1; margin-bottom: 0;"><code style="white-space: unset;">{{ example.text }}</code></pre>
                                </li>
                            </ul>
                        </div>
                    </div>
                </div>
            </li>
        </ul>
    </div>
</template>

<script lang="ts">
import Vue from 'vue'
import {CommandParameterElement, getParameterNames, toggleVisibility} from "@/types";

export default Vue.extend({
    name: 'CommandParameters',
    props: {
        headline: {
            type: String,
            required: true
        },
        parameters: {
            type: Array as () => Array<CommandParameterElement>,
            required: true
        },
    },
    methods: {
        getParameterNames(parameter: CommandParameterElement): string {
            return getParameterNames(parameter);
        },
        toggleVisibility(elementName: string, buttonName: string): void {
            toggleVisibility(this, elementName, buttonName, 0.3);
        }
    }
});
</script>

<style lang="css" scoped>
@import "../../assets/css/styles.scss";

ul {
    padding-inline-start: 1.4rem;
    list-style-type: none;
    margin-block-start: 0;
    margin-block-end: 0;
}

li.parameter {
    margin-bottom: 2rem;
}

li.parameter:first-child {
    margin-top: 2rem;
}

ul.parameter {
    overflow: hidden;
    padding-left: 1.3rem;
}

.parameterInfo {
    list-style-type: circle;
}

ul.parameter-examples {
    padding-inline-start: 0;
    margin-block-start: 0;
    margin-block-end: 0;
}

</style>

<style lang="scss" scoped>
@import "../../assets/css/header.scss";
</style>

