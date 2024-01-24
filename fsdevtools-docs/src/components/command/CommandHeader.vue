<template>
    <div style="max-width: 100%; margin-bottom: 1rem;">
        <h1>{{ this.command.elements[0].text }}</h1>
        <div>
            <vue-markdown :source="this.command.elements[1].text"></vue-markdown>
        </div>
    </div>
</template>

<script lang="ts">
import Vue from 'vue';
import { Command } from '@/types';
import VueMarkdown from 'vue-markdown';

export default Vue.extend({
    name: 'CommandHeader',
    props: {
        command: {
            type: Object as () => Command,
            required: true
        },
    },
    components: {
        VueMarkdown
    },
});
</script>
