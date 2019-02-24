#!/usr/bin/env groovy


node {
        def WORKSPACE = pwd()
        env.KUBECONFIG = pwd() + "/.kubeconfig"
}



pipeline {


    agent {
        label 'docker-slave'
    }



    stages {


        stage('Checkout') {

            steps {
                script {
                    checkout scm
                }
            }


        }
        //=================

        stage('Logging see Jenkins Log') {

                    steps {

                            sh """
                                java -version
                                gradle -version
                                npm -version

                               """
                    }

          }



        stage('Gradle Build') {


            steps {



                    sh """
                             set +x
                             echo "workspace:" ${WORKSPACE}
                             echo "User:" `whoami`
                             gradle  clean --no-daemon
                             gradle  build --no-daemon


                                                         """



                script {

                    step([$class: 'ArtifactArchiver', artifacts: '**/build/libs/*.war, **/build/libs/*.jar', fingerprint: true])
                }
            }


        }

        //=====================
        stage('Create Middleware Image Builder') {
            when {
                expression {
                    openshift.withCluster() {
                        openshift.withProject() {
                            return !openshift.selector("bc", "middleware").exists();
                        }
                    }
                }
            }
            steps {
                script {
                    openshift.withCluster() {
                        openshift.withProject() {
                            openshift.newBuild("--name=middleware", "--image-stream=hisd3base:1.0.3", "--binary=true")
                        }
                    }
                }
            }
        }


        // =====================

        stage("Build Middleware") {


            steps {
                    login()


                    sh """

                        rm -rf oc-build && mkdir -p oc-build/
                        cp build/libs/hl7middleware.jar oc-build/hl7middleware.jar
                        cp Dockerfile oc-build/Dockerfile
                        oc start-build middleware --from-dir=oc-build
                    """

            }

        }

        //=======================
    }


}


def login() {
    sh """
       set +x
       oc login --certificate-authority=/var/run/secrets/kubernetes.io/serviceaccount/ca.crt --token=\$(cat /var/run/secrets/kubernetes.io/serviceaccount/token) https://kubernetes.default.svc.cluster.local >/dev/null 2>&1 || echo 'OpenShift login failed'
       """
}

def processStageResult() {

    if (currentBuild.result != null) {
        sh "exit 1"
    }
}