pipeline {
    agent any

    tools {
        jdk 'JDK21'
    }

    stages {
        stage('Build') {
            steps {
                echo "Building..."
                sh './gradlew clean build'
            }
        }
    }
}
