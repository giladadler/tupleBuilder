import java.text.SimpleDateFormat

node ('docker') {

    deleteDir()

    stage 'get artifacts'

    // get source code
    checkout scm

    // create the third-party 'lib' folder
    def mvnHome = tool 'M3'
    sh "${mvnHome}/bin/mvn -B clean package"

    stage 'build docker'

    def localRepositoryUrl = "https://${env.DOCKER_LOCAL_REGISTRY}"

    sh "shopt -s dotglob && cp docker/* ."

    def dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm")
    def date = new Date()
    String buildTime = dateFormat.format(date)

    String tupleBuilder = "tuple-builder"
    String buildNumber = "1.0." + currentBuild.id
    currentBuild.displayName = buildNumber
    String tagName = "TupleBuilder_${buildNumber}"

    sh "echo build.version=${buildNumber} > build.properties"
    sh "echo build.timestamp=${buildTime} >> build.properties"


    String fullLocalDockerUri = "docker-registry.lab.aternity.com/${tupleBuilder}:${tagName}"
    sh "docker login -u admin -p admin ${localRepositoryUrl}"
    sh "docker build -t ${fullLocalDockerUri} ."
    sh "docker push ${fullLocalDockerUri}"

    stage 'run tupleBuilder app'

        node('automation-services') {

            sh "docker pull docker-registry.lab.aternity.com/${tupleBuilder}:${tagName}"

            String result = runShell("docker ps -a | grep ${tupleBuilder}")

            if (result.contains("${tupleBuilder}")) { // if service exists remove it
                sh "docker rm -f ${tupleBuilder}"
            }

            // start new servcie
            sh script: """ \
            docker run -d --name ${tupleBuilder} \
            --restart=unless-stopped \
            -e TZ=Asia/Jerusalem \
            -t --publish 9300:9300 \
            docker-registry.lab.aternity.com/${tupleBuilder}:${tagName} \
            """

            String listServiceOutput = sh(returnStdout: true, script: "docker ps | grep ${tupleBuilder} | grep \"Up \"")
            if (!listServiceOutput.contains(tupleBuilder))
                error "failed to run ${tupleBuilder} service. see service ls command output."
    }

    deleteDir()

}

def runShell(String command) {
    return sh (
            script: command + " || true",
            returnStdout: true
    ).trim()
}
