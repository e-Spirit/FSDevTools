import com.moowork.gradle.node.npm.NpmTask


plugins {
    id("com.github.node-gradle.node") version "2.2.4"
}

node {
    // Whether to download and install a specific Node.js version or not
    // If false, it will use the globally installed Node.js
    // If true, it will download node using above parameters
    // Note that npm is bundled with Node.js
    download = true

    // Version of node to download and install (only used if download is true)
    // It will be unpacked in the workDir
    version = "12.18.1"

    // Version of npm to use
    // If specified, installs it in the npmWorkDir
    // If empty, the plugin will use the npm command bundled with Node.js
    npmVersion = ""

    // Base URL for fetching node distributions
    // Only used if download is true
    // Change it if you want to use a mirror
    // Or set to null if you want to add the repository on your own.
    distBaseUrl = "https://nodejs.org/dist"

    // The npm command executed by the npmInstall task
    // By default it is install but it can be changed to ci
    npmInstallCommand = "install"

    // The directory where Node.js is unpacked (when download is true)
    workDir = file("${project.projectDir}/.gradle/nodejs")

    // The directory where npm is installed (when a specific version is defined)
    npmWorkDir = file("${project.projectDir}/.gradle/npm")

    nodeModulesDir = file("${project.projectDir}")
}

project.afterEvaluate {
    disablePublishing()
}

dependencies {
    compileOnly(project(":fsdevtools-cli"))
}

tasks {

    clean {
        delete("dist")
    }

    jar {
        enabled = true

        dependsOn(project.tasks["npmInstall"], project.tasks["buildVueApp"])

        // include fsui-images
        from("node_modules/fsui/dist/static") {
            include("**/*.*")
            into("static")
        }


        from("dist") {
            include("**/*.*")
            into("static")
        }

        manifest {
            attributes["Implementation-Title"] = "FirstSpirit MultisiteManagement Vue-Application"
            attributes["Implementation-Version"] = project.version
        }
    }
}

tasks.create<NpmTask>("buildVueApp") {
    group = "build"
    dependsOn(project.tasks["npmInstall"], rootProject.tasks["createDocumentationJson"])
    setArgs(listOf("run", "build"))
    inputs.files("package.json", "package-lock.json")
    inputs.dir("src")
    inputs.dir(fileTree("node_modules").exclude(".cache"))
}

tasks.create<NpmTask>("serveVueApp") {
    group = "application"
    dependsOn(project.tasks["npmInstall"])
    setArgs(listOf("run", "serve"))
}


