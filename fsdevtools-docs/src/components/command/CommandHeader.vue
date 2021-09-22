<template>
    <div style="max-width: 100%; margin-bottom: 1rem;">
        <h1>{{ this.command.elements[0].text }}</h1>
        <div>{{ this.command.elements[1].text }}</div>
    </div>
</template>

<script lang="ts">
import Vue from 'vue'
import {Command} from "@/types";

export default Vue.extend({
    name: 'CommandHeader',
    props: {
        command: {
            type: Object as () => Command,
            required: true
        },
    }
});
</script>
