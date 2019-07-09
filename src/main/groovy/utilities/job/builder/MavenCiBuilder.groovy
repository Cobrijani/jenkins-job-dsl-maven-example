package utilities.job.builder

import javaposse.jobdsl.dsl.DslFactory
import javaposse.jobdsl.dsl.Job

/**
 *
 * @author mwolf
 */
class MavenCiBuilder {
    
    String jobName
    String description
    Integer numToKeep
    Integer daysToKeep
    String scmGitUrl
    String branchName
    String credentialKeyId
    String goals
    
    Job build(DslFactory dslFactory) {
        dslFactory.mavenJob(jobName) {
            description(this.description)
            
            logRotator {
                numToKeep = this.numToKeep
                daysToKeep = this.daysToKeep
            }
            wrappers {
                if(this.branchName == "master"){
                    mavenRelease {
                        releaseGoals('-Dresume=false release:prepare release:perform')
                        dryRunGoals('-Dresume=false -DdryRun=true release:prepare')
                        selectCustomScmCommentPrefix()
                        selectAppendJenkinsUsername()
                        selectScmCredentials()
                        numberOfReleaseBuildsToKeep(10)
                    }
                }
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
            goals(this.goals)

            preBuildSteps {
                shell("export SHORT_COMMIT=$(git rev-parse --short HEAD)")
            }

            postBuildSteps('SUCCESS') {
                shell("printenv SHORT_COMMIT")
            }   
        }
    }
}

