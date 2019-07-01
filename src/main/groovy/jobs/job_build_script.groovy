
import utilities.configuration.ReadYaml
import utilities.job.builder.MavenCiBuilder
import utilities.job.builder.ShellCiBuilder

final YAML_FILE_CONFIG_PATH = "/var/jenkins_home/job_dsl_script/jenkins_swarm.yaml"
final BASE_PATH = "pipelines"

def createBuildJobs(projectConfig, basePath, branchName, goals) {
    new MavenCiBuilder (
        jobName: "${basePath}/${projectConfig.projectName}-build-${branchName}",
        description: 'Build ${projectConfig.projectName}',
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
        description: 'Deploy ${projectConfig.projectName}',
        numToKeep: 10,
        daysToKeep: 90,
        scriptsToRun: ["${WORKSPACE}/src/main/resources/test1.sh"
            , "${WORKSPACE}/src/main/resources/test2.sh"],
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
        
        if (parts.length == 2){
            goals = parts[1]
        }

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


