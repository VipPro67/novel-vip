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