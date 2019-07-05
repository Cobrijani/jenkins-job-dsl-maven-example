
import utilities.configuration.ReadYaml
import utilities.job.builder.MavenCiBuilder
import utilities.job.builder.ShellCiBuilder
import javaposse.jobdsl.dsl.DslFactory

final YAML_FILE_CONFIG_PATH = "/var/jenkins_home/job_dsl_script/jenkins_swarm.yaml"
final BASE_PATH = "pipelines"

DslFactory factory = this

def createBuildJobs(projectConfig, basePath, branchName, goals) {
    new MavenCiBuilder (
        jobName: "${basePath}/${projectConfig.projectName}-build-${branchName}",
        description: "Build ${projectConfig.projectName}",
        numToKeep: 10,
        daysToKeep: 90,
        scmGitUrl: projectConfig.gitConfig.url,
        branchName: branchName,
        credentialKeyId: projectConfig.gitConfig.credentialKeyId,
        goals: goals
    ).build(this)
    
    
}

def createDeployJobs(projectConfig, basePath, branchName){
    new ShellCiBuilder(
        jobName: "${basePath}/${projectConfig.projectName}-deploy-${branchName}",
        description: "Deploy ${projectConfig.projectName}",
        numToKeep: 10,
        daysToKeep: 90,
        scriptsToRun: ["${WORKSPACE}/src/main/resources/deploy.sh"],
        scmGitUrl: projectConfig.gitConfig.url,
        branchName: branchName,
        credentialKeyId: projectConfig.gitConfig.credentialKeyId
    ).build(this)
}

ReadYaml readYaml = new ReadYaml()
def projectConfigList = readYaml.readJenkinsYaml(YAML_FILE_CONFIG_PATH)

folder(BASE_PATH) {
    description 'All jobs that are created with the seed job'
}

projectConfigList.each { projectConfig ->
    def projectBasePath = "${BASE_PATH}/${projectConfig.projectName}"
    folder(projectBasePath) {
        description 'All branch pipelines'
    }
    projectConfig.gitConfig.branchesToBuild.each { branchDefinition ->
        
        def branchName = branchDefinition.branchName
        def goals = branchDefinition.goals

        def branchPath = "${projectBasePath}/${branchName}"
        folder(branchPath) {
            description 'All jobs for the pipeline'
        }
        def buildJobNames = createBuildJobs(
            projectConfig, branchPath, branchName, goals)
    }

    projectConfig.gitConfig.branchesToDeploy.each { branchName ->
        def branchPath = "${projectBasePath}/${branchName}"
        folder(branchPath) {
            description 'All jobs for the pipeline'
        }
        def buildJobNames = createDeployJobs(
            projectConfig, branchPath, branchName)
    }
}

factory.listView('Build master') {
    description('Build master of the projects')
    filterBuildQueue()
    filterExecutors()
    jobs {
        regex(/.*-build-master/)
    }
    columns {
        status()
        weather()
        name()
        lastSuccess()
        lastFailure()
        lastDuration()
        buildButton()
    }
}

factory.listView('Build staging') {
    description('Build staging of the projects')
    filterBuildQueue()
    filterExecutors()
    jobs {
        regex(/.*-build-staging/)
    }
    columns {
        status()
        weather()
        name()
        lastSuccess()
        lastFailure()
        lastDuration()
        buildButton()
    }
}

factory.listView('Deploy master') {
    description('Deploy master of the projects')
    filterBuildQueue()
    filterExecutors()
    jobs {
        regex(/.*-deploy-master/)
    }
    columns {
        status()
        weather()
        name()
        lastSuccess()
        lastFailure()
        lastDuration()
        buildButton()
    }
}

factory.listView('Deploy staging') {
    description('Deploy staging of the projects')
    filterBuildQueue()
    filterExecutors()
    jobs {
        regex(/.*-deploy-staging/)
    }
    columns {
        status()
        weather()
        name()
        lastSuccess()
        lastFailure()
        lastDuration()
        buildButton()
    }
}




