# Runtime stage only - JAR is built externally
FROM --platform=linux/amd64 openjdk:21-jdk-slim
WORKDIR /app

# Install Python and required packages for observer script
RUN apt-get update && apt-get install -y \
    python3 \
    python3-pip \
    python3-venv \
    && rm -rf /var/lib/apt/lists/*

# Create Python virtual environment and install dependencies
RUN python3 -m venv /app/venv
RUN /app/venv/bin/pip install --upgrade pip
RUN /app/venv/bin/pip install watchdog python-dotenv requests pytz

# Copy pre-built JAR (built in GitHub Actions)
COPY build/libs/*-SNAPSHOT.jar app.jar

# Copy observer scripts
COPY script/ /app/script/

# Create logs directory
RUN mkdir -p /app/logs

# Create startup script
RUN echo '#!/bin/bash\n\
set -e\n\
\n\
# Start observer script first\n\
cd /app\n\
/app/venv/bin/python /app/script/observer.py &\n\
OBSERVER_PID=$!\n\
\n\
# Wait a moment for observer to initialize\n\
sleep 2\n\
\n\
# Start Spring Boot application\n\
java -jar /app/app.jar --spring.profiles.active=${ENVIRONMENT:-dev} --server.port=${SERVER_PORT:-8080} &\n\
SPRING_PID=$!\n\
\n\
# Wait for either process to exit\n\
wait $SPRING_PID $OBSERVER_PID\n\
' > /app/start.sh && chmod +x /app/start.sh

# Expose port (default 8080, can be overridden by SERVER_PORT)
EXPOSE ${SERVER_PORT:-8080}

# Use the startup script as entrypoint
ENTRYPOINT ["/app/start.sh"]