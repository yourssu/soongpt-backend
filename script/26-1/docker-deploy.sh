#!/bin/bash
set -e

# Install Docker if not present
if ! command -v docker &> /dev/null; then
  echo 'Installing Docker...'
  sudo apt-get update
  sudo apt-get install -y ca-certificates curl gnupg
  sudo install -m 0755 -d /etc/apt/keyrings

  # Force overwrite GPG key to avoid prompts
  curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor --yes -o /etc/apt/keyrings/docker.gpg
  sudo chmod a+r /etc/apt/keyrings/docker.gpg

  # Add repository if not already added
  if [ ! -f /etc/apt/sources.list.d/docker.list ]; then
    echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu $(. /etc/os-release && echo "$VERSION_CODENAME") stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
  fi

  sudo apt-get update
  sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
  sudo usermod -aG docker ubuntu
  newgrp docker
else
  echo 'Docker is already installed.'
fi

# Get PROJECT_NAME from current directory or passed as argument
if [ -z "$PROJECT_NAME" ]; then
  PROJECT_NAME=$(basename $(dirname $(pwd)) | sed 's/-api$//')
  if [ -z "$PROJECT_NAME" ]; then
    echo "Error: PROJECT_NAME not set and cannot be determined"
    exit 1
  fi
fi

DEPLOY_DIR="/home/ubuntu/$PROJECT_NAME-api"

# Load environment variables
source "$DEPLOY_DIR/.env"

# Clean up disk space before deployment
echo "Cleaning up disk space..."
docker system prune -af --volumes || true
sudo apt-get clean || true
sudo rm -rf /var/lib/apt/lists/* || true

# Check disk space
echo "Disk usage after cleanup:"
df -h

# Stop existing standalone container (migration from docker run)
docker stop "$PROJECT_NAME-container" 2>/dev/null || true
docker rm "$PROJECT_NAME-container" 2>/dev/null || true

# Stop existing compose services
docker compose -f "$DEPLOY_DIR/docker-compose.deploy.yml" down 2>/dev/null || true

# Check if port is already in use and kill the process
if lsof -Pi :"$SERVER_PORT" -sTCP:LISTEN -t >/dev/null 2>&1; then
    echo "Warning: Port $SERVER_PORT is already in use. Finding and stopping the process..."
    sudo fuser -k "$SERVER_PORT/tcp" || true
    sleep 2
fi

# Pull latest images
echo "Pulling images..."
docker pull "$ECR_REGISTRY/yourssu/$PROJECT_NAME:${IMAGE_TAG:-latest}"
docker pull "$ECR_REGISTRY/yourssu/$PROJECT_NAME/rusaint-service:${IMAGE_TAG:-latest}"

# Start services with docker compose
echo "Starting services with docker compose..."
IMAGE_TAG="${IMAGE_TAG:-latest}" docker compose -f "$DEPLOY_DIR/docker-compose.deploy.yml" up -d

# Clean up old images
docker image prune -f

echo 'Deployment completed successfully!'
echo "Service status:"
docker compose -f "$DEPLOY_DIR/docker-compose.deploy.yml" ps
