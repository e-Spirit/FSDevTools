<template>
  <div style="max-width: 100%;">
    <div class="header clickable headline-2">
      <div class="topic" @click="toggleVisibility('examples', 'toggleButton')">
        <div ref="toggleButton" class="noselect button toggleButton expand-icon"/>
        <div class="noselect text" style="cursor:pointer;">Examples</div>
      </div>
    </div>
    <ul class="examples" ref="examples" style="max-height: 0; opacity: 0; display: none; overflow: hidden;">
      <li v-for="(example, index) in this.examples" :key="index" class="example">
        <div class="clickable">
          <div class="topic" @click="toggleVisibility('example_' + index, 'toggleButton_example_' + index)">
            <div :ref="'toggleButton_example_' + index" class="noselect button toggleButton expand-icon"/>
            <p class="noselect text" style="cursor:pointer; font-style: italic;">{{
                example.description
              }}</p>
          </div>
        </div>
        <pre :ref="'example_' + index" style="max-height: 0; opacity: 0; display: none; margin-bottom: 0;"><div
          :ref="'copy_example_' + index" style="float: right;" class="noselect copyButton copy-icon"
          title="Copy" @click="copyToClipboard('copy_example_' + index, example.text, 1000)"/><code
          style="white-space: unset;">{{ example.text }}</code></pre>
      </li>
    </ul>
  </div>
</template>

<script lang="ts">
import Vue from 'vue';
import { CommandExampleElement, toggleVisibility } from '@/types';

export default Vue.extend({
  name: 'CommandExamples',
  props: {
    examples: {
      type: Array as () => Array<CommandExampleElement>,
      required: true
    },
  },
  data: () => {
    return {
      copyTimeout: 0,
    };
  },
  methods: {
    toggleVisibility(elementName: string, buttonName: string): void {
      toggleVisibility(this, elementName, buttonName, 0.3);
    },
    copyToClipboard(buttonName: string, text: string, duration: number): void {
      document.addEventListener('copy', (clipboardEvent: any) => {
        clipboardEvent.clipboardData.setData('text/plain', text);
        clipboardEvent.preventDefault();
        document.removeEventListener('copy', clipboardEvent);
      });
      document.execCommand('copy');
      let button = this.$refs[buttonName] as HTMLElement;
      if ((button as any).length) {
        button = (button as any)[0];
      }
      button.classList.add('check-icon');
      if (this.copyTimeout != 0) {
        clearTimeout(this.copyTimeout);
        this.copyTimeout = 0;
      }
      this.copyTimeout = setTimeout(() => {
        button.classList.remove('check-icon');
      }, duration);
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

.example {
  margin-top: 1rem;
}

</style>

<style lang="scss" scoped>
@import "../../assets/css/header.scss";
</style>
