<template>
  <div class="home">
    <section id="commandDescription">
      <CommandHeader :command="this.getCommand()"></CommandHeader>
      <CommandParameters v-if="this.getParameters(true).length > 0"
                         headline="Global parameters"
                         :parameters="this.getParameters(true)"></CommandParameters>
      <CommandParameters v-if="this.getParameters(false).length > 0"
                         headline="Command parameters"
                         :parameters="this.getParameters(false)"></CommandParameters>
      <CommandExamples v-if="this.getExamples().length > 0"
                       :examples="this.getExamples()"></CommandExamples>
    </section>
  </div>
</template>

<script lang="ts">
import Vue from 'vue';
import VueMarkdown from 'vue-markdown';
import {
  Command,
  CommandElement,
  CommandExampleElement,
  CommandGroup,
  CommandParameterElement,
  getCurrentCommandGroupName,
  getCurrentCommandName,
  getParameterNames
} from '@/types';
import CommandExamples from '@/components/command/CommandExamples.vue';
import CommandHeader from '@/components/command/CommandHeader.vue';
import CommandParameters from '@/components/command/CommandParameters.vue';

export default Vue.extend({
  name: 'Command',
  components: {
    CommandParameters,
    CommandHeader,
    CommandExamples,
    VueMarkdown
  },
  methods: {
    getCommand(): Command | undefined {
      let routeCommandGroup = getCurrentCommandGroupName(this.$route.params);
      let routeCommandName = getCurrentCommandName(this.$route.params);
      let commandGroup = this.$store.getters.data.find((commandGroup: CommandGroup) => {
        return routeCommandGroup === commandGroup.name;
      });
      if (!commandGroup) {
        return undefined;
      }
      let command = commandGroup.commands.find((command: Command) => {
        return routeCommandName === command.name;
      });
      if (!command) {
        return undefined;
      }
      return command;
    },
    getParameters(global: boolean): Array<CommandParameterElement> {
      let command = this.getCommand();
      if (!command) {
        return [];
      }
      // get all parameter types
      let result = command.elements.filter((element: CommandElement) => {
        if (element.type === 'PARAMETER') {
          return (element as CommandParameterElement).global === global;
        }
        return false;
      }) as Array<CommandParameterElement>;
      // sort parameters by required-attribute & alphabetical
      result = [...result].sort((first: CommandParameterElement, other: CommandParameterElement) => {
        if (first.required && !other.required) {
          return -1;
        }
        if (!first.required && other.required) {
          return +1;
        }
        return getParameterNames(first)
          .localeCompare(getParameterNames(other));
      });
      return result;
    },
    getExamples(): Array<CommandExampleElement> {
      let command = this.getCommand();
      if (!command) {
        return [];
      }
      return command.elements.filter((element: CommandElement) => {
        return element.type === 'EXAMPLE';
      }) as Array<CommandExampleElement>;
    }
  }
});
</script>
