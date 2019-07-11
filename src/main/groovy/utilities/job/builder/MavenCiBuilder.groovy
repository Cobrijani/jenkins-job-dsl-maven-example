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
            blockOnDownstreamProjects()
            goals(this.goals + ' -Drevision=${DOCKER_TAG}')

                preBuildSteps {
                shell("echo DOCKER_TAG=`git rev-parse --short HEAD` > env.properties")
                shell("""
FILE=src/main/docker/test.yml
if [ -f "$FILE" ]; then
    docker-compose -f $FILE stop
    docker-compose -f $FILE rm -f
    docker-compose -f $FILE up -d
fi

                """)
                environmentVariables {
                    propertiesFile('env.properties')
                }
            }
            
            postBuildSteps {
                shell("""
FILE=src/main/docker/test.yml
if [ -f "$FILE" ]; then
    docker-compose -f $FILE stop
    docker-compose -f $FILE rm -f
fi
                """)
            }
        

            downstreamParameterized {
                    trigger(this.deployJob) {
                        condition('SUCCESS')
                        parameters {
                            predefinedProp('TAG', 'v${POM_VERSION}.${DOCKER_TAG}')
                        }
                    }
                }
            }
        }
}


