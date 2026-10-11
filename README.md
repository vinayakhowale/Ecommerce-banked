# Social Commerce — Backend (Spring Boot)

Admin-controlled influencer product discovery platform. Influencers never log in or upload;
all content is created and published by admins. Public users browse a social feed, add
products to a cart, and use "Buy Now" to be redirected (securely) to the original product page.

## Tech Stack

- Java 17, Spring Boot 3.3.4
- Spring Security + JWT (stateless)
- Spring Data JPA / Hibernate
- H2 (dev profile) / PostgreSQL (prod profile)
- Bean Validation, springdoc-openapi (Swagger UI)
- Lombok, MapStruct-ready

## Getting Started (Local / Dev)

Requires Java 17+ and Maven 3.9+.

```bash
cd backend
mvn spring-boot:run
```

This runs with the `dev` profile by default (file-based H2 database, stored at `./data/socommerce.mv.db`
relative to wherever you run the app — **your data now survives backend restarts**. Delete that file
any time you want a completely clean slate, e.g.:

```bash
# Stop the backend first, then:
rm -rf data/
```

- API base URL: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- H2 console: `http://localhost:8080/h2-console` (JDBC URL `jdbc:h2:file:./data/socommerce`, user `sa`, blank password)

A default SUPER_ADMIN is seeded on first boot:
- Email: `admin@socialcommerce.com`
- Password: `Admin@12345`

**Change these via env vars before deploying** (`ADMIN_EMAIL`, `ADMIN_PASSWORD`), and change `JWT_SECRET`.

## Running with Postgres (prod profile)

```bash
export SPRING_PROFILES_ACTIVE=prod
export DB_URL=jdbc:postgresql://localhost:5432/socommerce
export DB_USERNAME=postgres
export DB_PASSWORD=postgres
export JWT_SECRET=$(openssl rand -base64 48)
export ADMIN_EMAIL=you@yourcompany.com
export ADMIN_PASSWORD='ChangeMe!2024'
mvn clean package -DskipTests
java -jar target/app.jar
```

## Docker

```bash
docker build -t socommerce-backend .
docker run -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e DB_URL=jdbc:postgresql://host.docker.internal:5432/socommerce \
  -e DB_USERNAME=postgres -e DB_PASSWORD=postgres \
  -e JWT_SECRET=supersecret \
  -v $(pwd)/uploads:/app/uploads \
  socommerce-backend
```

## Key Architecture Decisions

### Buy Now / Redirect Security
`POST /api/buy-now` is the **only** way the frontend can obtain a redirect URL. The client sends
just a `productId` (+ optional `postId` for attribution). The backend:
1. Loads the product from the DB (never trusts anything from the client about price or URL).
2. Confirms the product is `active` and `urlApproved`.
3. Runs the stored `productUrl` / `affiliateUrl` through `RedirectUrlValidator`, which blocks
   `javascript:`, `data:`, `file:` schemes, non-http(s) schemes, loopback/internal/metadata hosts,
   and malformed URLs — defense in depth against open-redirect and SSRF-style abuse.
4. Records a `ProductClick` row (used for admin analytics and can back affiliate tracking/payout
   reconciliation later).
5. Returns only the validated absolute URL; the Angular app performs the actual navigation.

### Cart pricing integrity
`CartService` always re-fetches `Product.price` from the database when computing subtotals/totals.
The client never supplies a price. Inactive products remain visible in the cart (so the user isn't
confused by a vanishing line item) but are excluded from the total and flagged `productActive:false`
so the frontend can block checkout for that line.

### Admin delete semantics
Admin deletes are unconditional and immediate — a genuine SQL `DELETE`, not a soft-delete flag.
Deleting a `Product` that's currently tagged in a post or sitting in someone's cart does **not**
fail: `ProductService.delete()` first removes that product's tags from any posts (the post itself
is unaffected — it just loses that one tagged product) and removes it from any carts it's in, then
cleans up its click/add-to-cart analytics, before deleting the product row itself. Deleting a `Post`
follows the same pattern for its likes, saves, and click-attribution records. This trades "block the
delete with a warning" for "always honor the delete and clean up around it" — the more common
expectation for an admin "Delete" action.

### Roles
`USER`, `ADMIN`, `SUPER_ADMIN`. All `/api/admin/**` endpoints require `ADMIN` or `SUPER_ADMIN`.
Role re-assignment (`PATCH /api/admin/users/{id}/role`) is restricted to `SUPER_ADMIN` only.

### Error responses
Every rejection — whether thrown inside a controller/service (`ResourceNotFoundException`,
`BadRequestException`, validation failures, malformed JSON, oversized uploads, missing multipart
parts, bad path/query param types) or rejected earlier by the security filter chain itself
(missing/invalid JWT, insufficient role) — returns the same consistent JSON `ApiError` shape with
a proper HTTP status. This matters in particular for 401 vs 403: a request with no/expired token
returns 401 (`RestAuthenticationEntryPoint`), and a request with a valid token but the wrong role
returns 403 (`RestAccessDeniedHandler`) — frontends commonly branch on exactly this distinction
(e.g. auto-logout on 401 vs. just showing "not allowed" on 403).

### Content model
`Post` (influencer content) has many `PostProduct` join rows, each pointing at a `Product` — this
is how a single post can tag multiple products (e.g. an outfit with a T-shirt, jeans, shoes).
Only posts with `status = PUBLISHED` are ever returned by public endpoints.

## Main Endpoints

**Public**
- `POST /api/auth/register`, `POST /api/auth/login`, `GET /api/auth/me`
- `GET /api/public/feed`, `GET /api/public/explore`, `GET /api/public/search?q=`
- `GET /api/public/posts/{id}`, `POST /api/public/posts/{id}/view` (works for guests too)
- `GET /api/public/products`, `GET /api/public/products/{id}`, `GET /api/public/products/search?q=`
- `GET /api/public/products/{id}/posts`
- `POST /api/buy-now` (explicitly permitted for guests — see Buy Now section above)

**Authenticated**
- `GET/POST/PATCH/DELETE /api/cart`, `/api/cart/items/{id}`
- `POST /api/posts/{id}/like`, `POST /api/posts/{id}/save`

**Admin (`ADMIN`/`SUPER_ADMIN`)**
- Full CRUD: `/api/admin/products`, `/api/admin/posts` (+ publish/unpublish/archive)
- Media upload: `POST /api/admin/products/upload-image`, `POST /api/admin/posts/upload-media`
- `GET /api/admin/dashboard` — totals, most viewed posts, most clicked products, recent uploads
- `GET/PATCH /api/admin/users` — activate/deactivate, `PATCH /.../role` (SUPER_ADMIN only)

## Notes / Next Steps for Production Hardening

- Add Flyway/Liquibase migrations instead of `ddl-auto: update` for controlled schema changes.
- Move uploaded media to S3/Cloud Storage + CDN instead of local disk for horizontal scaling.
- Add rate limiting (e.g. bucket4j) on `/api/auth/**` and `/api/buy-now`.
- Add refresh tokens / token revocation list if longer-lived sessions are needed.
- Wire real affiliate network callbacks/postbacks into `ProductClick` for conversion tracking.
