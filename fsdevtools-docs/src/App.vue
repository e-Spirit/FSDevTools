<template>
    <div id="app">
        <PageHeader></PageHeader>
        <PageNavigation></PageNavigation>
        <router-view></router-view>
        <PageFooter :artifact-info="this.artifactInfo"></PageFooter>
    </div>
</template>

<script lang="ts">
import Vue from "vue";
import PageHeader from "@/components/PageHeader.vue";
import PageNavigation from "@/components/PageNavigation.vue";
import PageFooter from "@/components/PageFooter.vue";
import ApplicationInfo from "@build/assets/application.json";
import {ArtifactInfo} from "@/types";

export default Vue.extend({
    components: {PageHeader, PageNavigation, PageFooter},
    data: () => {
        return {
            artifactInfo: ApplicationInfo as ArtifactInfo,
        };
    }
});
</script>
