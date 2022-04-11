<template>
    <div id="footer">
        <div style="float: right; margin-right: 1rem;">
            <div class="artifactInfo" ref="artifactInfo" style="opacity: 0;">{{ this.artifactInfo.version }} | {{ this.artifactInfo.branch }} | {{ this.artifactInfo.commit }}</div>
            <div class="info-button info-icon" @click="toggleArtifactInfo"></div>
        </div>
        <div>
            <span>© 2005 - 2020 e-Spirit GmbH</span>
            <span>Alle Rechte vorbehalten</span>
            <span><a href="https://www.e-spirit.com/de/datenschutzerklaerung/">Datenschutz</a></span>
            <span><a href="https://www.e-spirit.com/de/impressum/">Impressum</a></span>
            <span><a href="https://www.e-spirit.com/de/kontakt/">Kontakt</a></span>
        </div>
    </div>
</template>

<script lang="ts">
import Vue from 'vue'
import {ArtifactInfo} from "@/types";

export default Vue.extend({
    name: 'PageFooter',
    props: {
        artifactInfo: {
            type: Object as () => ArtifactInfo,
            required: true
        }
    },
    methods: {
        toggleArtifactInfo(): void {
            let element = this.$refs["artifactInfo"] as HTMLElement;
            element.style.opacity = element.style.opacity === "0" ? "1" : "0";
        }
    }
});
</script>

<style scoped lang="scss">

.artifactInfo {
    transition: opacity 0.5s;
    position: absolute;
    right: 40px;
    bottom: 9px;
}

.invisible {
    opacity: 0;
    transition: opacity 0.5s;
}

#footer:hover div.invisible {
    opacity: 1;
}

#footer {
    font-family: Arial, sans-serif;
    font-size: 12px;
    position: fixed;
    bottom: 0;
    left: 0;
    right: 0;
    background-color: #FFFFFF;
    color: #C7BFC1;
    padding: 10px 0;
    line-height: 20px;
    box-sizing: border-box;
    text-align: center;
    box-shadow: 0 -5px 5px 0 rgba(0, 0, 0, 0.05);
    min-height: 40px;
    z-index: 10;
}

#footer a {
    color: #7B7B9F;
}

#footer div span::before {
    content: " | ";
}

#footer div span:first-child::before {
    content: "";
}
</style>
