# Stage 1: Build the Angular app
FROM node:22-alpine AS build

# Set the working directory inside the container
WORKDIR /app

# Install build dependencies (if needed for native modules)
RUN apk add --no-cache python3 make g++ && ln -sf python3 /usr/bin/python

# Copy package.json and package-lock.json to install dependencies
COPY package.json package-lock.json ./

# Install dependencies
RUN npm install

# Copy the rest of the frontend code
COPY . .

# Build the Angular app for production
RUN npm run build -- --configuration production

# Stage 2: Serve the app with Nginx
FROM nginx:alpine

# Copy the built Angular app from the previous stage
COPY --from=build /app/dist/novel-fe /usr/share/nginx/html

# Expose port 80 for the Nginx server
EXPOSE 80

# Start Nginx
CMD ["nginx", "-g", "daemon off;"]