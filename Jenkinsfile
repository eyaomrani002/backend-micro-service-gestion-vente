pipeline {
  agent any

  environment {
    DOCKER_COMPOSE_VERSION = '1.29.2'
  }

  stages {

    stage('Préparation') {
      steps {
        echo "🔧 Préparation du workspace"
        sh 'docker --version'
        sh 'docker compose version || docker-compose --version'
      }
    }

    stage('Checkout du code') {
      steps {
        echo "📥 Récupération du code source"
        checkout scm
      }
    }

    stage('Build Backend Services') {
      steps {
        echo "🏗️ Build des services Spring Boot"
        sh 'docker compose build config-service eureka-server gateway-service authentificationservice client-service produit-service factureservice reglement-service devise-service'
      }
    }

    stage('Build Frontend Angular') {
      steps {
        echo "🎨 Build du frontend Angular"
        dir('gestion-ventes') {
          sh 'npm install'
          sh 'npm run build --prod'
          sh 'docker build -t gestion-ventes:latest .'
        }
      }
    }

    stage('Lancement des services') {
      steps {
        echo "🚀 Démarrage des conteneurs"
        sh 'docker compose up -d'
      }
    }

    stage('Vérification des services') {
      steps {
        echo "🔍 Vérification des conteneurs en cours d'exécution"
        sh 'docker ps'
      }
    }

    // Optionnel
    stage('Nettoyage après build') {
      steps {
        echo "🧹 Nettoyage des images non utilisées"
        sh 'docker image prune -f'
      }
    }

  }

  post {
    failure {
      echo "❌ Build échoué. Vérifie les logs ci-dessus."
    }
    success {
      echo "✅ Build terminé avec succès !"
    }
  }
}
