package utilities.job.builder

import javaposse.jobdsl.dsl.DslFactory
import javaposse.jobdsl.dsl.Job
import hudson.model.*

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
    String deployJob
    
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

            preBuildSteps {
                shell("echo DOCKER_TAG=`git rev-parse --short HEAD` > env.properties")
                shell("""
                    if [ -f "src/main/docker/test.yml" ]; then
                        docker-compose -f src/main/docker/test.yml stop
                        docker-compose -f src/main/docker/test.yml rm -f
                        docker-compose -f src/main/docker/test.yml up -d
                    fi
                """)
                environmentVariables {
                    propertiesFile('env.properties')
                }
            }

            blockOnDownstreamProjects()
            goals(this.goals + ' -Drevision=${DOCKER_TAG}')

            downstreamParameterized {
                trigger(this.deployJob) {
                    condition('SUCCESS'){
                        parameters {
                            predefinedProp('TAG', 'v${POM_VERSION}.${DOCKER_TAG}')
                        }
                    }
            }//downstreamParameterized
            postBuildSteps {
                shell("""
                    if [ -f "src/main/docker/test.yml" ]; then
                        docker-compose -f src/main/docker/test.yml stop
                        docker-compose -f src/main/docker/test.yml rm -f
                    fi
                """)
            }  //postBuildSteps
    
            }// dslFactory.mavenJob(jobName)    
        }
    }
}