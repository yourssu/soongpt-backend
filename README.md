# soongpt-backend

## 역사

- 2026.01.29
    - leopold, moru, piki

## API Documentation
[API_DOCUMENTATION](./src/main/resources/api/API_DOCUMENTATION.md)


## Deployment Guide
- [DEPLOYMENT_GUIDE](./DEPLOYMENT_GUIDE.md)

## Compose Files
- Local: `docker-compose.local.yml` (or `docker-compose.yml`)
- Deploy: `docker-compose.deploy.yml`
- Env examples: `examples/local.env.example`, `examples/deploy.env.example`

## Nginx Template
- `ops/nginx/soongpt.current-host.conf.template`
- `ops/nginx/install-current-host.sh`
- `ops/nginx/enable-https-letsencrypt.sh`

## Github Actions
- Env var sync helper: `ops/github/sync-env-vars.sh`
- Manual dev-backup deploy workflow: `.github/workflows/dev-backup.yml`
[![Dev - Build and Deploy to EC2](https://github.com/yourssu/soongpt-backend/actions/workflows/dev.yml/badge.svg)](https://github.com/yourssu/soongpt-backend/actions/workflows/dev.yml)  

[![Prod - Build and Deploy to EC2](https://github.com/yourssu/soongpt-backend/actions/workflows/prod.yml/badge.svg)](https://github.com/yourssu/soongpt-backend/actions/workflows/prod.yml)    

