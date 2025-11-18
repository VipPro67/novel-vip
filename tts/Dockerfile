FROM python:3.12-slim

ARG INSTALL_FFMPEG=false
WORKDIR /app

# Install SSL certificates + optionally ffmpeg + curl
RUN apt-get update && \
    apt-get install -y ca-certificates && \
    update-ca-certificates && \
    if [ "$INSTALL_FFMPEG" = "true" ]; then apt-get install -y ffmpeg; fi && \
    # Add curl installation here - this comment line is okay now
    apt-get install -y curl && \
    rm -rf /var/lib/apt/lists/*

COPY requirements.txt /app
RUN pip install --no-cache-dir -r requirements.txt

COPY app/ /app

CMD ["python", "/app/server.py"]