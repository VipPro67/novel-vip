# novel-vip

A modern novel management and reading platform built with a focus on performance, scalability, and user experience. This project aims to provide a rich set of features for both readers and administrators to manage and enjoy novels efficiently.

## Features

### Core Features
- **Novel Library**: Browse, search, and filter a diverse collection of novels with advanced search capabilities powered by Elasticsearch
- **Reading Experience**: Enjoy distraction-free reading with customizable settings (font size, themes, reading modes) and support for multiple file formats (EPUB, DOCX, Markdown, TXT)
- **User Accounts**: Register and log in with email or Google OAuth, manage profiles, and track reading progress
- **Admin Dashboard**: Comprehensive management interface for novels, chapters, users, roles, comments, reviews, and system analytics
- **Responsive Design**: Fully optimized for desktop, tablet, and mobile devices with a modern, intuitive UI

### Secondary Features
- **Bookmarks & Reading History**: Save reading positions and track history across devices
- **Favorites & Ratings**: Mark novels as favorites and rate them to help other readers
- **Reviews & Comments**: Engage with the community through reviews and comments on novels and chapters
- **Correction Requests**: Submit and manage correction requests for novel content
- **Feature Requests**: Suggest new features and vote on existing requests
- **Reports**: Report inappropriate content or behavior
- **Groups & Messaging**: Create and join reading groups, send messages, and participate in discussions
- **Notifications**: Receive real-time notifications for updates, messages, and system alerts
- **Gamification**: Earn badges and track reading statistics
- **Video Management**: Upload and manage promotional videos for novels
- **File Management**: Upload and manage various file types related to novels
- **Role Approval System**: Request and approve user role changes (e.g., from reader to author)

### Notable Features
- **Text-to-Speech**: Convert novel content to audio using Google Cloud Text-to-Speech
- **Real-time Communication**: WebSocket-based real-time search, notifications, and chat functionality
- **Multiple Storage Options**: Support for Google Cloud Storage, AWS S3, and Cloudinary for file storage
- **Message Queues**: ActiveMQ and RabbitMQ for asynchronous processing of tasks like email verification and EPUB import
- **Caching**: Redis for performance optimization
- **Rate Limiting**: Bucket4j for API protection
- **Logging & Analytics**: Log4j2 with OpenTelemetry integration for Grafana Loki log shipping
- **Internationalization**: Multi-language support (English and Vietnamese)

## Technologies Used

### Frontend
- **Framework**: Next.js 16.1.0-canary (with Turbopack for fast development)
- **Language**: TypeScript 5
- **Styling**: Tailwind CSS 3.4.17 (with Tailwind Animate for animations)
- **UI Components**: Radix UI (various packages including dialog, dropdown, tabs, etc.), Lucide React icons
- **State Management**: Zustand (lightweight and fast state management)
- **Form Handling**: React Hook Form (with Zod validation)
- **Internationalization**: Next-intl
- **Theming**: Next-themes (dark/light mode support)
- **Data Visualization**: Recharts (charts and graphs for analytics)
- **Real-time Communication**: @stomp/stompjs, sockjs-client
- **Authentication**: @react-oauth/google
- **Analytics**: Vercel Analytics, Vercel Speed Insights
- **Build Tools**: PostCSS, Autoprefixer

### Backend
- **Framework**: Spring Boot 3.2.3
- **Language**: Java 17
- **Security**: Spring Security (with JWT authentication), JJWT 0.11.5
- **ORM**: Spring Data JPA (with Hibernate), MapStruct 1.5.5.Final
- **Database**: PostgreSQL (primary), Redis (caching), Elasticsearch (search)
- **Message Brokers**: ActiveMQ, RabbitMQ
- **Real-time Communication**: WebSocket (STOMP protocol)
- **File Storage**: Google Cloud Storage 2.25.0, AWS S3 2.21.32, Cloudinary 1.33.0
- **Text-to-Speech**: Google Cloud Text-to-Speech 2.25.0
- **APIs**: RESTful endpoints with OpenAPI 3.0 (Swagger UI 5.17.14)
- **Document Processing**: CommonMark 0.22.0 (Markdown), Jsoup 1.17.2 (HTML sanitization), Docx4j 11.4.11 (DOCX)
- **Rate Limiting**: Bucket4j 8.7.0
- **Logging**: Log4j2, OpenTelemetry 1.44.1 (Grafana Loki integration)
- **Email**: Spring Boot Starter Mail
- **Build Tool**: Maven 3.8.x

### Deployment
- **Backend**: https://13-213-96-98.sslip.io/swagger-ui/index.html
- **Frontend**: https://novel-vip.vercel.app/
- **Infrastructure**: Docker (compose.yml for local development), Caddy (reverse proxy)

## Contact

- Author: [VipPro67](https://github.com/VipPro67)

