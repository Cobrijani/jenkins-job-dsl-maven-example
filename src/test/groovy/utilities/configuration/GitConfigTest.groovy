package utilities.configuration

import org.yaml.snakeyaml.Yaml
import utilities.configuration.ReadYaml
import utilities.configuration.GitConfig
import javaposse.jobdsl.dsl.Job
import javaposse.jobdsl.dsl.JobParent
import javaposse.jobdsl.dsl.jobs.FreeStyleJob
import org.junit.*
import static groovy.test.GroovyAssert.*

/**
 *
 * @author mwolf
 */
class GitConfigTest {
    
    
    
    @Before
    public void setUp() {
        
    }
    
    @After
    public void tearDown() {}
    
    @Test
    void configWithBranchesBuildTest() {
        String content = """
        - branches_to_build:
            - master: "clean -Pprod deploy"
            - staging
        """

        Yaml yaml = new Yaml()
        def obj = yaml.load(content)
        
        GitConfig config = new GitConfig()

        config.readConfigFromList(obj)

        assertEquals("master", config.branchesToBuild[0].branchName)
        assertEquals("clean -Pprod deploy", config.branchesToBuild[0].goals)
        assertEquals("staging", config.branchesToBuild[1].branchName)
        assertEquals("clean package", config.branchesToBuild[1].goals)
    }
    
    @Test
    void configWithBranchesDeployTest() {

        String content = """
        - branches_to_deploy:
            - master
            - staging
        """

        Yaml yaml = new Yaml()
        def obj = yaml.load(content)
        
        GitConfig config = new GitConfig()

        config.readConfigFromList(obj)

        assertEquals("master", config.branchesToDeploy[0])
        assertEquals("staging", config.branchesToDeploy[1])
        
    }
}

