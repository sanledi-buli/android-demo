node {
  try {
    notifyBuild('STARTED')

    stage('Checkout Source Code') {
      gitFlow()
    }

    stage('Testing') {
      //todo
    }

    stage('Build') {
      build()
    }

    stage('Deploy'){

    }
  } catch (e) {
    currentBuild.result = 'FAILED'
    throw e
  } finally {
    notifyBuild(currentBuild.result)
  }
}

def gitFlow() {
  timeout(time: 60, unit: 'SECONDS') {
    def isRepoExists = fileExists("${projectName}-${branchName}")
    if (isRepoExists) {
      echo "Repository ${projectName} already exists."
      dir("${projectName}-${branchName}") {
        sh "git checkout ${branchName}"
        sh "git pull origin ${branchName}"
      }
    } else {
      echo "Repository need to clone."
      sh "git clone ${projectURL} ${projectName}-${branchName}"
      dir("${projectName}-${branchName}") {
        sh "git fetch origin ${branchName}"
        sh "git checkout origin ${branchName}"
      }
    }
  }
}

def build() {
  dir("${projectName}-${branchName}") {
    sh "gradle clean"
    sh "gradle build"
  }
}

def notifyBuild(String buildStatus = 'STARTED') {
  buildStatus = buildStatus ?: 'SUCCESSFULL'
  def subject = "${buildStatus}: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]'"
  def summary = "${subject} (${env.BUILD_URL})"
  def colorCode = '#00FF00'
  if (buildStatus == 'STARTED') {
    colorCode = "#FFFF00"
  }
  if (buildStatus == 'FAILED') {
    colorCode = "#FF0000"
  }
  slackSend (color: colorCode, message: summary)
}
