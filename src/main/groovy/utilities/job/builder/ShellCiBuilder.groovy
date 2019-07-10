package utilities.job.builder

import javaposse.jobdsl.dsl.DslFactory
import javaposse.jobdsl.dsl.Job

/**
 *
 * @author mwolf
 */
class ShellCiBuilder {
    
    String jobName
    String description
    Integer numToKeep
    Integer daysToKeep
    List<String> scriptsToRun
    String scmGitUrl
    String branchName
    String credentialKeyId
    
    Job build(DslFactory dslFactory) {
        dslFactory.freeStyleJob(jobName) {
            description(this.description)

            parameters {
                stringParam('TAG', 'latest', 'Tag used to deploying')
            }

            preBuildSteps {
                environmentVariables {
                    env('TAG', '$TAG')
                }
            }

            logRotator {
                numToKeep = this.numToKeep
                daysToKeep = this.daysToKeep
            }
            scm {
                git {
                    remote {
                        name('origin')
                        url(this.scmGitUrl)
                        credentials(this.credentialKeyId)
                    }
                    branch(this.branchName)
                }
            }
            blockOnDownstreamProjects()
            steps {
                scriptsToRun.each { script ->
                    shell new File(script).getText('UTF-8')
                }
            }
        }
    }
}

