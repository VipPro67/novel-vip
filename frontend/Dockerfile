# =========================
# Stage 1: Builder
# =========================
FROM node:22-alpine AS builder

WORKDIR /app

# Copy dependency files
COPY package*.json ./

# Install dependencies
RUN npm ci --omit=dev

# Copy all source files
COPY . .

# Build Next.js app
RUN npm run build


# =========================
# Stage 2: Runner
# =========================
FROM node:22-alpine AS runner

WORKDIR /app

# Environment variables for runtime
ENV NODE_ENV=production
ENV PORT=3000

# Copy necessary files from builder
COPY --from=builder /app/public ./public
COPY --from=builder /app/.next ./.next
COPY --from=builder /app/node_modules ./node_modules
COPY --from=builder /app/package.json ./package.json
COPY --from=builder /app/next.config.mjs ./next.config.mjs

EXPOSE 3000

# Start Next.js production server
CMD ["npm", "start"]
