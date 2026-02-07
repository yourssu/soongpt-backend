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

# Load environment variables
source /home/ubuntu/$PROJECT_NAME-api/.env

# No AWS credentials or CLI needed for ECR Public (pulling only)

# Clean up disk space before deployment
echo "Cleaning up disk space..."
docker system prune -af --volumes || true
sudo apt-get clean || true
sudo rm -rf /var/lib/apt/lists/* || true

# Check disk space
echo "Disk usage after cleanup:"
df -h

# Stop and remove existing container if exists
docker stop $PROJECT_NAME-container 2>/dev/null || true
docker rm $PROJECT_NAME-container 2>/dev/null || true

# Check if port is already in use and kill the process
if lsof -Pi :$SERVER_PORT -sTCP:LISTEN -t >/dev/null ; then
    echo "Warning: Port $SERVER_PORT is already in use. Finding and stopping the process..."
    
    # Try to find Docker container using the port
    EXISTING_CONTAINER=$(docker ps --format "table {{.Names}}" | grep -v NAMES | xargs -I {} sh -c 'docker port {} 2>/dev/null | grep -q "$SERVER_PORT->" && echo {}' || true)
    if [ ! -z "$EXISTING_CONTAINER" ]; then
        echo "Stopping container using port $SERVER_PORT: $EXISTING_CONTAINER"
        docker stop $EXISTING_CONTAINER
        docker rm $EXISTING_CONTAINER
    else
        # If not a Docker container, kill the process directly
        echo "Killing process using port $SERVER_PORT"
        sudo fuser -k $SERVER_PORT/tcp || true
    fi
    
    # Wait a moment for port to be released
    sleep 2
fi

# Pull the latest image
docker pull $ECR_REGISTRY/yourssu/$PROJECT_NAME:latest

# Run the container with environment variables
docker run -d \
  --name $PROJECT_NAME-container \
  --restart unless-stopped \
  -p $SERVER_PORT:$SERVER_PORT \
  -v /home/ubuntu/$PROJECT_NAME-api/logs:/app/logs \
  --env-file /home/ubuntu/$PROJECT_NAME-api/.env \
  $ECR_REGISTRY/yourssu/$PROJECT_NAME:latest

# Clean up old images
docker image prune -f

echo 'Deployment completed successfully!'