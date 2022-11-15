import Vue from 'vue';
import VueRouter, { Route, RouteConfig } from 'vue-router';
import Command from '../views/Command.vue';
import { DEFAULT_COMMAND, DEFAULT_GROUP } from '@/types';

Vue.use(VueRouter);

function getCommandRouteParams(route: Route) {
    return {
        commandGroup: route.params.commandGroup ? route.params.__commandGroup : DEFAULT_GROUP,
        commandName: route.params.commandName ? route.params.commandName : DEFAULT_COMMAND,
    };
}

const routes: Array<RouteConfig> = [
    {
        path: '/',
        redirect: { name: 'Command' }
    },
    {
        path: '/help/:commandGroup?/:commandName?',
        name: 'Command',
        component: Command,
        props: getCommandRouteParams,
    },
];

const router = new VueRouter({
    routes,
});

export default router;
