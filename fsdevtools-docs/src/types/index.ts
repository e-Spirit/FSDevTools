import { Dictionary } from 'vue-router/types/router';
import { gsap } from 'gsap';

export const DEFAULT_GROUP = 'GLOBAL';
export const DEFAULT_COMMAND = 'export';

export interface ArtifactInfo {
    version: string;
    branch: string;
    commit: string;
}

export function toggleVisibility(component: Vue, elementName: string, buttonName: string, duration: number): void {
    let element = component.$refs[elementName] as HTMLElement;
    if ((element as any).length) {
        element = (element as any)[0];
    }
    let button = component.$refs[buttonName] as HTMLElement;
    if ((button as any).length) {
        button = (button as any)[0];
    }
    const timeLine = gsap.timeline();
    if (element.style.display === 'none') {
        timeLine.set(element, {
            display: 'block'
        });
        timeLine.to(element, duration, {
            maxHeight: 9999,
            opacity: 1,
        });
        timeLine.to(button, duration, {
            rotation: 0
        }, 0);
    } else {
        timeLine.to(element, duration, {
            maxHeight: 0,
            opacity: 0,
        });
        timeLine.set(element, {
            display: 'none'
        });
        timeLine.to(button, duration, {
            rotation: -90
        }, 0);
    }
    timeLine.play();
}

export function getCurrentCommandGroupName(params: Dictionary<string>): string {
    const group = params.commandGroup;
    return group ? group : DEFAULT_GROUP;
}

export function getCurrentCommandName(params: Dictionary<string>): string {
    const command = params.commandName;
    return command ? command : DEFAULT_COMMAND;
}

export function getParameterNames(parameter: CommandParameterElement): string {
    let names = parameter.names.toString();
    names = names.replace(',', ', ');
    return names;
}

export interface CommandGroup {
    name: string;
    commands: Array<Command>;
}

export interface Command {
    name: string;
    group: string;
    elements: Array<CommandElement>;
}

export interface CommandElement {
    type: string;
}

export interface CommandTextElement extends CommandElement {
    text: string;
}

export interface CommandExampleElement extends CommandElement {
    text: string;
    description: string;
}

export interface ParameterExampleElement {
    text: string;
    description: string;
}

export interface CommandParameterElement extends CommandElement {
    names: Array<string>;
    className: string;
    global: boolean;
    examples: Array<ParameterExampleElement>;
    description: string;
    required: boolean;
    defaultValue: string;
    possibleValues: string;
}
