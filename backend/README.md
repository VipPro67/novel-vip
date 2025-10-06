# Novel VIP Backend

## Supabase Auth Webhook Setup

To sync users from Supabase Auth to your database, follow these steps:

1. Set the `SUPABASE_WEBHOOK_SECRET` environment variable in your deployment environment.

2. In your Supabase dashboard, go to Database > Webhooks.

3. Create a new webhook with the following settings:
   - Name: User Creation Webhook
   - Table: `auth.users`
   - Events: INSERT
   - HTTP Method: POST
   - URL: `https://your-api-url/api/webhooks/supabase/auth`
   - Headers: Add a header with key `X-Supabase-Webhook-Secret` and value matching your `SUPABASE_WEBHOOK_SECRET` environment variable.

4. Save the webhook.

Now, whenever a new user is created in Supabase Auth, a webhook will be triggered to create a corresponding user in your database.

## Environment Variables

Make sure to set the following environment variables:

```
DB_URL=your_database_url
DB_USERNAME=your_database_username
DB_PASSWORD=your_database_password
JWT_SECRET=your_jwt_secret
CLOUDINARY_API_KEY=your_cloudinary_api_key
CLOUDINARY_API_SECRET=your_cloudinary_api_secret
SUPABASE_URL=your_supabase_url
SUPABASE_SERVICE_ROLE_KEY=your_supabase_service_role_key
SUPABASE_ANON_KEY=your_supabase_anon_key
SUPABASE_WEBHOOK_SECRET=your_supabase_webhook_secret
```

## Search & Media Configuration

The service can now be switched between different infrastructure providers without touching the code:

- `SEARCH_PROVIDER` – choose `elasticsearch` or `opensearch`. The backend wires the matching search client automatically and Docker Compose uses the same value as its profile.
- `SEARCH_BASE_URI` / `SEARCH_INDEX` – point at the running search cluster and optionally change the index name.
- `COMPOSE_PROFILES` – should match `SEARCH_PROVIDER` so only the relevant search stack (Elasticsearch + Kibana + Filebeat or OpenSearch + Dashboards) is started.
- `STORAGE_PROVIDER` – toggle between `s3` and `cloudinary` file storage backends.
- `TTS_PROVIDER` – choose the text-to-speech provider (`openai-edge`, `elevenlabs`, or `gcp`).

When using Docker Compose you can switch stacks with:

```bash
# Elastic Stack
SEARCH_PROVIDER=elasticsearch COMPOSE_PROFILES=elasticsearch docker compose up

# OpenSearch Stack
SEARCH_PROVIDER=opensearch COMPOSE_PROFILES=opensearch docker compose up
```

Make sure the matching `.env` values are set before starting the stack so the backend and infrastructure stay in sync.

## Log Shipping with Filebeat

Filebeat now ships the backend log file (`logs/app.log`) into the active search cluster (OpenSearch or Elasticsearch). The Docker Compose stack mounts a shared `app_logs` volume between the backend and Filebeat containers and enables the Filebeat service automatically when you run one of the search profiles (`opensearch` or `elasticsearch`).

- Tune the destination by updating `FILEBEAT_OUTPUT_HOST`, `FILEBEAT_USERNAME`, and `FILEBEAT_PASSWORD` in `.env` / `.env.local`.
- Default indexing uses `novel-backend-logs`; adjust `FILEBEAT_INDEX` (and the related template vars) if you want a different index pattern.
- To verify ingestion locally, launch the stack with `docker compose --profile opensearch up backend opensearch opensearch-dashboards filebeat` and watch documents arrive in the `novel-backend-logs` index.

With Filebeat in place, Logstash is no longer required for routine development setups.

