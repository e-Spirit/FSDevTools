import Vue from 'vue';
import Vuex from 'vuex';
import data from "@build/assets/data.json";


Vue.use(Vuex);

export default new Vuex.Store({
    state: {},
    mutations: {},
    actions: {},
    modules: {},
    getters: {
        data: state => {
            return data;
        }
    }
});
