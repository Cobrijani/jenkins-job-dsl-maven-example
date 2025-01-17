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
            } //logRotator
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
                buildName('${POM_VERSION}.${DOCKER_TAG}')
            }//wrappers
            
            scm {
                git {
                    remote {
                        name('origin')
                        url(this.scmGitUrl)
                        credentials(this.credentialKeyId)
                    }
                    branch(this.branchName)
                }
            }//scm

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
            }//preBuildSteps

            blockOnDownstreamProjects()
            goals(this.goals + ' -Drevision=${DOCKER_TAG}')

            publishers {
                downstreamParameterized {
                    trigger(this.deployJob) {
                        condition('SUCCESS')
                        parameters {
                            predefinedProp('TAG', 'v${POM_VERSION}.${DOCKER_TAG}')
                        }
                    }//trigger(this.deployJob)
                }//downstreamParameterized
            }//publishers
            
            postBuildSteps {
                shell("""
                    if [ -f "src/main/docker/test.yml" ]; then
                        docker-compose -f src/main/docker/test.yml stop
                        docker-compose -f src/main/docker/test.yml rm -f
                    fi
                """)
            }//postBuildSteps
    
            }// dslFactory.mavenJob(jobName)    
        }//Job build(DslFactory dslFactory)
    }//class MavenCiBuilder