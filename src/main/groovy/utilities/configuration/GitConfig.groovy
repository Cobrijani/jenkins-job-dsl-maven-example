package utilities.configuration

/**
 *
 * @author mwolf
 */
class GitConfig {
    
    final URL_PROPERTY_KEY = "url"
    final BRANCHES_TO_BUILD_PROPERTY_KEY = "branches_to_build"
    final BRANCHES_TO_DEPLOY_PROPERTY_KEY = "branches_to_deploy"
    final CREDENTIAL_KEY_ID_PROPERTY_KEY = "credential_key_id"
    
    def String url
    def branchesToBuild = []
    def branchesToDeploy = []
    def String credentialKeyId
    
    def void readConfigFromList(gitYamlList) {
        gitYamlList.each {
            if (it[URL_PROPERTY_KEY]) {
                url = it[URL_PROPERTY_KEY]
            } else if (it[BRANCHES_TO_BUILD_PROPERTY_KEY]) {
                it[BRANCHES_TO_BUILD_PROPERTY_KEY].each { build ->
                    if(build instanceof String){
                        branchesToBuild
                            .add(new BranchBuild(branchName: build,
                                goals: "clean package"))
                    }else{
                        branchesToBuild
                            .add(new BranchBuild(branchName: build.key, 
                            goals: build.value))
                    }
                }
            } else if (it[CREDENTIAL_KEY_ID_PROPERTY_KEY]) {
                credentialKeyId = it[CREDENTIAL_KEY_ID_PROPERTY_KEY]
            } else if (it[BRANCHES_TO_DEPLOY_PROPERTY_KEY]){
                branchesToDeploy = it[BRANCHES_TO_DEPLOY_PROPERTY_KEY]
            }
        }
    }

}

