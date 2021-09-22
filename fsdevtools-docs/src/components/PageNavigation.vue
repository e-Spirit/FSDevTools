<template>
    <div>
        <article id="categories">
            <ul>
                <router-link tag="li"
                             v-if="commandGroup.commands.length > 0"
                             v-for="commandGroup in sortedCommandGroups"
                             :key="commandGroup.name"
                             :to="{name: 'Command', params: {commandGroup: commandGroup.name, commandName:getSortedCommands(commandGroup)[0].name}}"
                             class="noselect">
                    {{ commandGroup.name }}
                </router-link>
            </ul>
        </article>
        <article id="subcategories">
            <ul v-if="isCommandGroupActive(commandGroup.name)"
                v-for="commandGroup in sortedCommandGroups"
                :key="'commands_' + commandGroup.name">
                <router-link tag="li"
                             v-for="command in getSortedCommands(commandGroup)"
                             :key="commandGroup.name + '_' + command.name"
                             :to="{name: 'Command', params: {commandGroup: commandGroup.name, commandName: command.name}}"
                             class="noselect"
                             :class="getSubcategoryClass(commandGroup.name , command.name)">
                    {{ command.name }}
                </router-link>
            </ul>
        </article>
    </div>
</template>

<script lang="ts">
import Vue from 'vue'
import {
    Command,
    CommandGroup,
    DEFAULT_COMMAND,
    DEFAULT_GROUP,
    getCurrentCommandGroupName,
    getCurrentCommandName
} from "@/types";

export default Vue.extend({
    name: 'PageNavigation',
    props: {
        __commandGroup: {
            type: String,
            default: DEFAULT_GROUP,
            required: false
        },
        __commandName: {
            type: String,
            default: DEFAULT_COMMAND,
            required: false
        },
    },
    computed: {
        sortedCommandGroups(): Array<CommandGroup> {
            return [...this.$store.getters.data].sort((first: CommandGroup, other: CommandGroup) => {
                if (first.name === other.name) {
                    return 0;
                } else if (first.name.localeCompare(other.name) > 0) {
                    return +1;
                } else {
                    return -1;
                }
            });
        },
    },
    methods: {
        getSortedCommands(commandGroup: CommandGroup): Array<Command> {
            return [...commandGroup.commands].sort((first: Command, other: Command) => {
                if (first.name === other.name) {
                    return 0;
                } else if (first.name.localeCompare(other.name) > 0) {
                    return +1;
                } else {
                    return -1;
                }
            });
        },
        getSubcategoryClass(groupName: string, commandName: string): string | Array<string> {
            if (groupName === getCurrentCommandGroupName(this.$route.params) && commandName === getCurrentCommandName(this.$route.params)) {
                return "selected";
            }
            return [];
        },
        isCommandGroupActive(commandGroupName: string): boolean {
            return getCurrentCommandGroupName(this.$route.params) === commandGroupName;
        }
    }
});
</script>

<style scoped lang="scss">

/* ------------------------------------------------------------------------------------------------------------------ */

#categories {
    background-color: #25263A;
    top: 248px;
    min-height: 72px;
    flex-wrap: wrap;
    left: 0;
    right: 0;
    width: auto;
    z-index: 2;
    text-align: center;
    display: flex;
    flex-direction: row;
    justify-content: center;
}

#categories ul {
    display: flex;
    flex-wrap: wrap;
    margin: 0;
    padding-inline: 0;
}

#categories ul li {
    margin: 0;
    position: relative;
    display: inline-block;
    min-width: 100px;
    width: 170px;
    line-height: 70px;
    z-index: 3;
    text-decoration: none;
    border: 1px solid #51505E;
    color: #FFFFFF;
    font-size: 1.3em;
    cursor: pointer;
}

#categories ul li.selected {
    color: #25263A;
    background-color: #FFFFFF;
    border-color: #CFCFCF;
    white-space: nowrap;
}

#categories ul li.selected::before {
    position: absolute;
    content: "";
    width: 0;
    height: 0;
    border-left: 8px solid transparent;
    border-right: 8px solid transparent;
    left: 77px;
    bottom: -1px;
    border-bottom: 8px solid #FFFFFF;
    z-index: 5;
}

#categories ul li.selected::after {
    position: absolute;
    content: "";
    width: 0;
    height: 0;
    border-left: 10px solid transparent;
    border-right: 10px solid transparent;
    left: 75px;
    bottom: -1px;
    border-bottom: 10px solid #CFCFCF;
    z-index: 4;
}

/* ------------------------------------------------------------------------------------------------------------------ */

#subcategories {
    background-color: white;
    top: 320px;
    height: 51px;
    left: 0;
    right: 0;
    width: auto;
    z-index: 2;
    text-align: center;
    display: flex;
    flex-direction: row;
    justify-content: center;
    box-shadow: 0 0 8px 0 rgba(0, 0, 0, 0.1);
}

#subcategories ul {
    display: flex;
    margin: 0;
    padding-inline: 0;
}

#subcategories ul li {
    position: relative;
    display: inline-block;
    line-height: 51px;
    margin: 0 15px;
    z-index: 3;
    text-decoration: none;
    color: #7B7B9F;
    font-size: 1.2em;
    /*text-transform: uppercase;*/
    white-space: nowrap;
    cursor: pointer;
}

#subcategories ul li.selected {
    border-bottom: 5px solid #25263A;
}

/* ------------------------------------------------------------------------------------------------------------------ */

</style>
