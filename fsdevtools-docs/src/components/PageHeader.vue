<template>
    <div>
        <div class="background-image">
            <div id="title"><span><b>FS-CLI</b> Commands</span></div>
        </div>
    </div>
</template>

<script lang="ts">
import Vue from 'vue';

export default Vue.extend({
    name: 'PageHeader',
});
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped lang="scss">

.background-image {
    top: 0;
    left: 0;
    right: 0;
    min-width: 1200px;
    background: no-repeat center center #04162A;
    height: 248px;
    z-index: 0;
    background-image: url("../assets/img/background.jpg")
}

#title {
    font-size: 3em;
    font-family: Arial, sans-serif;
    top: 0;
    height: 248px;
    left: 0;
    right: 0;
    min-width: 1200px;
    width: auto;
    z-index: 2;
    text-align: center;
    display: flex;
    flex-direction: row;
    align-items: center;
    justify-content: center;
}

#title span {
    position: relative;
    display: inline-block;
    color: #FFFFFF;
    white-space: nowrap;
}
</style>
