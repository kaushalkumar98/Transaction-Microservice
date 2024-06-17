pipeline {
    agent any

    tools {
        // Install the Maven version configured as "M3" and the JDK version configured as "jdk11"
        maven 'M3'
        jdk 'jdk17'
    }

    environment {
        // Define SonarQube environment variables
        SONARQUBE_URL = 'http://localhost:9000'
        SONARQUBE_TOKEN = credentials('Krasv_bank')
    }

    stages {
        stage('Checkout') {
            steps {
                // Checkout code from GitHub
                git branch: 'master', url: 'https://github.com/kaushalkumar98/Transaction-Microservice.git'
            }
        }

        stage('Build') {
            steps {
                // Run Maven build
                bat 'mvn clean install'
            }
        }

        stage('SonarQube Analysis') {
            steps {
                // Run SonarQube analysis
                withSonarQubeEnv('server sonar') {
                    bat 'mvn sonar:sonar -Dsonar.projectKey=Transaction-Microservice -Dsonar.projectName=Transaction-Microservice -Dsonar.host.url=http://localhost:9000 -Dsonar.token=sqp_1c4f5f779420fd6956b2a2422491805f39172115'
                }
            }
        }
    }

    post {
        always {
            // Clean up workspace
            cleanWs()
        }
    }
}
