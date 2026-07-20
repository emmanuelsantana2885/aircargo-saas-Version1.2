# Aircargo — agent notes

## Structure

Monorepo with three directories:

| Dir | Stack | Entrypoint / notes |
|-----|-------|-------------------|
| `frontend/` | Vue 3 + Vite + Pinia + Vue Router + Tailwind + TS | Vite dev on port 5173, proxy `/api` → `localhost:9091` |
| `backend/aircargo-api/` | Spring Boot 3.3 + Java 21 + Maven + JPA + Flyway + PostgreSQL | `com.aircargo.AircargoApiApplication`, port 9091 |
| `database/migrations/` | PostgreSQL Flyway migrations | Root copy — see "Migrations" below |

## Commands

```sh
# Frontend
npm install          # in frontend/
npm run dev          # Vite dev server (port 5173)
npm run type-check   # vue-tsc --build
npm run build        # type-check + vite build (via npm-run-all2)

# Backend
mvn test                               # unit + integration tests (H2, Flyway disabled)
mvn spring-boot:run                    # starts on port 9091 (needs Postgres)
mvn clean compile spring-boot:run      # full rebuild + run

# Database
docker compose up -d                   # PostgreSQL 16 alpine on :5432
```

## Hard-coded constants

- UPS airline UUID `00000000-0000-0000-0000-000000000001` is hard-coded in every frontend API file and seeded in `V1__init.sql` (backend copy only). Never change it without updating both sides.
- Vite proxy in `frontend/vite.config.ts` assumes backend is on `localhost:9091`.

## Migrations

Flyway migrations live in **two places** with **different content**:
- **Source of truth (Flyway picks up):** `backend/aircargo-api/src/main/resources/db/migration/`
- **Root copy:** `database/migrations/` — differs from the backend copy (has full Postgres schema with functions, triggers, permissions; backend copy is Hibernate-compressed)

The backend copy also seeds the UPS airline row; the root copy does not. Keep both in sync.

## Recent session changes (July 20, 2026 — ULD Barcode Scanning + Dashboard Commodity Fix)

| File | Change |
|------|--------|
| `frontend/src/views/DashboardView.vue` | **FIX**: `mawbDispatchedWeightLbs(mawb, flightId)` now filters ULD-AWB links by flight (`uldIds.has(l.uldId)`), preventing cross-flight piece inflation. Added `flightUldIdSet()` with cache + `watch` invalidation. All callers updated to pass `flightId`. |
| `frontend/src/components/FlightDetail.vue` | **FIX**: Same `mawbDispatchedWeightLbs` and `mawbDispatchedPieces` fix — filter by `flightUlds` set. |
| `frontend/src/stores/app.js` | **FIX**: `dispatchUld()` now uses `mawb.awbNumber` instead of `mawb.id` for ULD-AWB link creation. |
| `.gitignore` | Added `backend/**/target/` to ignore all microservice build artifacts. |
| `backend/.../entity/UldPiece.java` | **NEW** — per-piece tracking entity (uld_id, mawb_id, awb_number, hawb_number, piece_number, source [BARCODE/MANUAL], scanned_by, scanned_at). |
| `backend/.../entity/PieceSource.java` | **NEW** — enum: BARCODE, MANUAL. |
| `backend/.../entity/Uld.java` | Added `@OneToMany(mappedBy="uld", cascade=ALL, orphanRemoval=true)` to `UldPiece`. |
| `backend/.../repository/UldPieceRepository.java` | **NEW** — findByUldId, findByUldIdAndMawbId, countByUldIdAndMawbId, deleteByUldIdAndMawbId, etc. |
| `backend/.../repository/MawbRepository.java` | Added `findByAwbNumber(String)` for scan lookup. |
| `backend/.../repository/HawbRepository.java` | Added `findByHawbNumber(String)` for HAWB scan resolution. |
| `backend/.../repository/UldAwbRepository.java` | Added `findByUldIdAndMawbId(UUID, UUID)`. |
| `backend/.../service/ScanService.java` | **NEW** — lookup (MAWB/HAWB/ULD resolution), registerPiece (creates UldPiece + upserts UldAwb + auto-advance status), undoLastPiece. |
| `backend/.../controller/ScanController.java` | **NEW** — GET `/api/scan/lookup`, POST `/api/scan/piece`, DELETE `/api/scan/piece/last`. |
| `backend/.../dto/ScanLookupDTO.java` | **NEW** — response for lookup (type, awbNumber, pieces info, ULD info). |
| `backend/.../dto/ScanPieceRequest.java` | **NEW** — request for registering a piece (uldId, awbNumber, hawbNumber, source). |
| `backend/.../dto/ScanPieceResult.java` | **NEW** — response for piece registration (success, pieceNumber, totalOnUld, availablePieces). |
| `backend/.../config/SecurityConfig.java` | Added `/api/scan/**` for OPERATIONS, TRAFFIC, LOAD_PLANNER, WAREHOUSE_ASSISTANT, ADMIN, SUPER_USER. |
| `backend/resources/db/migration/V32__create_uld_piece_table.sql` | **NEW** — creates `uld_piece` table with indexes, `piece_source` enum type. |
| `database/migrations/V32__create_uld_piece_table.sql` | Synced from backend. |
| `frontend/src/api/scan.js` | **NEW** — lookup, piece, undoLast API calls. |
| `frontend/src/components/ScanPanel.vue` | **NEW** — scan mode panel: auto-focus input, barcode capture, scan history, undo, ULD number detection, audio/visual feedback, camera placeholder. |
| `frontend/src/views/UldsView.vue` | Integrated ScanPanel: scan toggle button in action bar, scan mode state, `onScanPieceAdded` (auto-creates MAWB row + updates pieces), `onScanPieceRemoved`. |

## Recent session changes (July 3, 2026 — Sites + SuperUser role)

| File | Change |
|------|--------|
| `backend/.../entity/Site.java` | **NEW** — entity with id, code, name, country, isActive |
| `backend/.../dto/SiteDTO.java` | **NEW** — DTO with fromEntity/toEntity mappers |
| `backend/.../repository/SiteRepository.java` | **NEW** — CRUD + findByCode + findByIsActiveTrue |
| `backend/.../service/SiteService.java` | **NEW** — CRUD for site management |
| `backend/.../controller/SiteController.java` | **NEW** — CRUD endpoints, audit logging, SuperUser-only |
| `backend/.../entity/UserRole.java` | Renamed `SUPERVISOR` → `SUPER_USER` |
| `backend/.../entity/AppUser.java` | Added `@ManyToMany(fetch = EAGER)` relationship to `Site` via `user_sites` join table |
| `backend/.../dto/LoginResponse.java` | Added `List<SiteDTO> sites` field |
| `backend/.../dto/AppUserDTO.java` | Added `List<UUID> siteIds` field |
| `backend/.../controller/AuthController.java` | Login now returns user's assigned sites |
| `backend/.../service/AppUserServiceImpl.java` | Handles site assignment on create/update |
| `backend/.../controller/AppUserController.java` | Validates siteIds on create (non-empty) |
| `backend/.../config/SecurityConfig.java` | Renamed `SUPERVISOR` → `SUPER_USER` in all matchers; added `.requestMatchers("/api/sites/**").hasAuthority("SUPER_USER")` |
| `backend/.../service/PermissionService.java` | `SUPERVISOR` → `SUPER_USER` |
| `backend/resources/db/migration/V15__create_sites_table.sql` | **NEW** — creates `site` table, seeds SDQ/STI/PUJ/MIA |
| `backend/resources/db/migration/V16__create_user_sites_table.sql` | **NEW** — creates `user_sites` join table, assigns all users to SDQ |
| `backend/resources/db/migration/V17__rename_supervisor_to_super_user.sql` | **NEW** — updates existing rows from SUPERVISOR → SUPER_USER |
| `database/migrations/V15__create_sites_table.sql` | Synced from backend |
| `database/migrations/V16__create_user_sites_table.sql` | Synced from backend |
| `database/migrations/V17__rename_supervisor_to_super_user.sql` | Synced from backend |
| `backend/resources/db/migration/V8__update_user_role_to_varchar.sql` | Updated seed to use SUPER_USER |
| `backend/resources/db/migration/V10__add_password_hash_and_seed_real_users.sql` | Updated seed to use SUPER_USER |
| `database/migrations/V8__update_user_role_to_varchar.sql` | Synced from backend |
| `database/migrations/V10__add_password_hash_and_seed_real_users.sql` | Synced from backend |
| `frontend/src/api/sites.js` | **NEW** — CRUD API calls for sites |
| `frontend/src/stores/auth.js` | Added `sites`, `selectedSiteId`, `selectedSite`, `confirmSite()`; `isAuthenticated` requires token + site; renamed SUPERVISOR → SUPER_USER |
| `frontend/src/views/LoginView.vue` | **Two-step login**: step 1 credentials, step 2 site selection dropdown; site is mandatory |
| `frontend/src/router/index.js` | Renamed SUPERVISOR → SUPER_USER; added site check in beforeEach guard |
| `frontend/src/components/layout/Sidebar.vue` | Shows selected site code in logo area + site indicator bar |
| `frontend/src/views/SettingsView.vue` | **Redesigned with tabs**: Users tab shows site assignment per user (checkbox list of sites); Sites tab (SuperUser only) for CRUD site management |
| `frontend/src/views/UsersView.vue` | Renamed `SUPERVISOR` → `SUPER_USER` label |

## Recent session changes (June 30, 2026 — READ_ONLY role + ERP redesign)

| File | Change |
|------|--------|
| `backend/.../entity/UserRole.java` | Added `READ_ONLY` enum value |
| `backend/.../config/SecurityConfig.java` | Added `.requestMatchers(HttpMethod.GET, "/api/**")` for READ_ONLY; READ_ONLY has GET-only access to all APIs |
| `backend/.../service/PermissionService.java` | Added `READ_ONLY` case → all views visible |
| `backend/resources/db/migration/V13__add_read_only_role.sql` | **NEW** — no DDL (varchar column already supports new values) |
| `database/migrations/V13__add_read_only_role.sql` | Synced from backend |
| `frontend/src/stores/auth.js` | Added `READ_ONLY` → returns `true` for all views in `canView()` |
| `frontend/src/router/index.js` | Added `READ_ONLY` → returns `true` for all views in `hasPermission()` |
| `frontend/src/views/SettingsView.vue` | **Complete redesign**: modal-based editing (replaced inline edit row), search/filter input, role list includes READ_ONLY, removed `roleColor()` function (no more colored role badges — all roles use neutral `bg-slate-100 text-slate-600`), toolbar with user count |
| `frontend/src/views/UsersView.vue` | **Complete redesign**: connected users now rendered as proper table (not cards), audit log uses monochrome action badges (removed `actionStyle()` colors), added READ_ONLY role label |
| `frontend/src/api/flights.js` | Removed UPS UUID hardcode — `getAll()` no longer filters by `airlineId: UPS`, create/update accept DTO as-is |

## Architecture (June 2026)

### RBAC (Role-Based Access Control)

Seven roles with the following view permissions (enforced on both frontend routes and backend endpoints):

| Role | Dashboard | Bookings | Receipts | Flights | MAWBs | Load Planning | ULDs | Users | Settings |
|------|-----------|----------|----------|---------|-------|---------------|------|-------|----------|
| **Read Only** | ✅ | ✅* | ✅* | ✅* | ✅* | ✅* | ✅* | ❌ | ❌ |
| **Warehouse Assistant** | ✅ | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **Operations** | ✅ | ❌ | ❌ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ |
| **Traffic** | ✅ | ✅ | ❌ | ❌ | ✅ | ✅ | ✅ | ❌ | ❌ |
| **Load Planner** | ✅ | ❌ | ❌ | ✅ | ❌ | ✅ | ✅ | ❌ | ❌ |
| **Admin** | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ |
| **SuperUser** | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |


\* READ_ONLY can see all views (GET-only) but cannot create/update/delete — enforced at backend SecurityConfig via `HttpMethod.GET` matcher.

### Authentication Flow
1. User POSTs email to `/api/auth/login` → backend returns JWT + user info
2. Token stored in `localStorage` under key `aircargo_auth` (persisted across refreshes)
3. Axios interceptor attaches `Authorization: Bearer <token>` to every request
4. Spring Security `JwtAuthFilter` validates token on every request (except `/api/auth/**`)
5. 401 auto-clears localStorage and redirects to `/login`

### Test Users (seeded by V9 migration)

| Email | Role |
|-------|------|
| `readonly@aircargo.com` | READ_ONLY |
| `warehouse@aircargo.com` | WAREHOUSE_ASSISTANT |
| `operations@aircargo.com` | OPERATIONS |
| `traffic@aircargo.com` | TRAFFIC |
| `loadplanner@aircargo.com` | LOAD_PLANNER |
| `admin@aircargo.com` | ADMIN |
| `supervisor@aircargo.com` | SUPER_USER |

### Caching
- Spring Cache with Caffeine (in-process, configurable via `spring.cache.caffeine.spec=maximumSize=500,expireAfterWrite=300s`)
- **Flights** (`getAll` + `getById`): `@Cacheable(value = "flights")`, evicted on create/update/delete
- **Airlines** (`getAll` + `getById`): `@Cacheable(value = "airlines")`, evicted on create/update/delete

### Async / Event-Driven
- `@EnableAsync` + `@EnableCaching` on main application class
- `ReceiptCreatedEvent` published after receipt creation in `WarehouseService`
- `ReceiptEventListener` (async) processes background tasks post-receipt
- Template for future: export PDFs, send notifications, cache warming

### Authentication Implementation
- **Backend**: `JwtUtil` (jjwt 0.12.6), `JwtAuthFilter` (OncePerRequestFilter), `SecurityConfig` (filter chain with URL-based role checks), `AuthController` (POST `/api/auth/login`)
- **Frontend**: `stores/auth.js` (Pinia store + localStorage persistence), `views/LoginView.vue` (email-only login), `router/index.ts` (navigation guards with role-based permission checks), `api/client.js` (token injection interceptor + 401 auto-redirect)
- Tests use `@Profile("test")` security config that permits all requests

## Backend quirks

- **Spring Security** (`spring-boot-starter-security`, `jjwt-api/impl/jackson 0.12.6`) — protects all `/api/*` endpoints by role via `SecurityConfig.filterChain()`.
- **Test pattern:** All 50 tests pass (`mvn test`): service-layer tests use Mockito (no Spring context); integration tests use `@SpringBootTest` + `@AutoConfigureMockMvc` + `@Transactional` with H2 (test profile disables security via `TestSecurityConfig`). Flyway disabled in tests.
- **Apache POI** for Excel ramp manifest parsing (`LoadPlanningImportService`).
- **Fixes applied:**
  - Deleted duplicate `com.aircargo.WebConfig` (root package) — kept `com.aircargo.config.WebConfig` (ports 5173 + 5174) since both beans named `webConfig` caused `ConflictingBeanDefinitionException`.
  - Removed `@CrossOrigin(origins = "*")` from 5 controllers (Hawb, Warehouse, Mawb, FlightManifest, LoadPlanning) — conflicted with `WebConfig.allowCredentials(true)` causing CORS rejection.
  - Added `= BigDecimal.ZERO` default to `Booking.reservedKg` and null guard in `BookingDTO.toEntity()` — field was `nullable = false` but had no default, causing 500 on booking create.

## Frontend quirks

- **Stale files (removed):** `src/stores/ulds.js` was deleted — superseded by `src/stores/ulds.ts`. `src/stores/counter.ts` is a Pinia template leftover. The API layer (`src/api/*.js`) is plain JS while stores are mixed JS/TS.
- **No frontend tests** exist (no vitest/jest config found).
- `README.md` is the default Vite template — ignore it.
- **Pre-existing type-check errors:** missing `env.d.ts` for `.vue` module declarations, `ulds.ts` strict mode issues. These do not block the Vite build.

## Recent session changes (June 2026)

| File | Change |
|------|--------|
| `frontend/src/views/WarehouseReceiptsView.vue` | Major redesign: Step 1 two-column layout (left: data fields, right: HAWB table + checkboxes group); shipper/consignee preloaded from MAWB, editable, sync'd to backend on blur; 5 checkboxes (Cash Only, Booked in ACOMS, Documents Provided, Export Customs Completed, Pre-built) grouped in bordered card; Step 2 professional dark-header table with bordered inputs, proper spacing; multi-HAWB pieces section with per-HAWB tables; Step 5 compact signatures (grid 2-col, smaller pads); added PDF + HTML download buttons for supporting evidence; added MAWB-level evidence manager modal (folder icon in status column); **NEW**: HAWB count input with `syncHawbCount()` to add/remove HAWB rows dynamically; fully editable HAWB table (hawbNumber, consignee, pieces, weightKg, destination); filename read from `Content-Disposition` header |
| `frontend/src/api/mawbs.js` | Added `update(mawbId, dto)`, `getSupportingDocs(id)`, `updateSupportingDocs(id, docs)`, `getSupportingDocsPdf(id)` |
| `frontend/src/api/receipts.js` | Added `getSupportingDocsJson(id)`, `getSupportingDocsHtml(id)`, `getSupportingDocsPdf(id)` |
| `backend/aircargo-api/src/main/java/com/aircargo/controller/MawbController.java` | Added `PUT /{mawbId}`, `GET /{mawbId}/supporting-docs`, `PUT /{mawbId}/supporting-docs`, `GET /{mawbId}/supporting-docs/pdf` — MAWB evidence CRUD + PDF generation |
| `backend/aircargo-api/src/main/java/com/aircargo/controller/WarehouseController.java` | Added `ReceiptPayload.supportingDocs` field; `GET /{receiptId}/supporting-docs` (JSON), `GET /{receiptId}/supporting-docs/html`, `GET /{receiptId}/supporting-docs/pdf`; updated `emit` to pass supportingDocs to service; fixed Excel filename to `RECIBO_DE_BODEGA_AWB {mawbNumber}.xlsx` |
| `backend/aircargo-api/src/main/java/com/aircargo/entity/WarehouseReceipt.java` | Added `supportingDocs` text column (default `"[]"`) |
| `backend/aircargo-api/src/main/java/com/aircargo/entity/Mawb.java` | Added `supportingDocs` text column (default `"[]"`) |
| `backend/aircargo-api/src/main/java/com/aircargo/service/WarehouseService.java` | Overloaded `processWarehouseReceipt` to accept `List<Map<String,String>> supportingDocs`; stores as JSON; added `generateSupportingDocsHtml()` + `generateSupportingDocsPdf()` |
| `backend/aircargo-api/src/main/java/com/aircargo/service/PdfGenerationService.java` | New — uses openhtmltopdf to convert HTML+CSS (with embedded base64 images) to PDF |
| `backend/aircargo-api/src/main/java/com/aircargo/repository/WarehouseReceiptRepository.java` | Added `findByMawbId(UUID)` |
| `backend/aircargo-api/pom.xml` | Added `openhtmltopdf-pdfbox:1.0.10` dependency |
| `backend/aircargo-api/src/main/resources/db/migration/V6__add_supporting_docs_to_receipt.sql` | New — `ALTER TABLE warehouse_receipt ADD COLUMN supporting_docs text` |
| `backend/aircargo-api/src/main/resources/db/migration/V7__add_supporting_docs_to_mawb.sql` | New — `ALTER TABLE mawb ADD COLUMN supporting_docs text` |
| `database/migrations/V6__add_supporting_docs_to_receipt.sql` | Synced from backend |
| `database/migrations/V7__add_supporting_docs_to_mawb.sql` | Synced from backend |
| `backend/aircargo-api/src/main/java/com/aircargo/service/ReceiptExportService.java` | **Rewritten** — matches reference template `RECIBO_DE_BODEGA_AWB.xlsx`: same merged cells layout (B-C labels, D-E values, F-G checkboxes), same column widths (A-L), 25 data rows, `Dim Weight` = vol/366 (KGS), `Dim LBS` = vol/194; Tahoma font |
| `frontend/src/views/WarehouseReceiptsView.vue` | **HAWB section redesigned**: bigger (text-[10px] inputs), dark green border-2 border-emerald-800, emerald header bg, emerald-800 accent checkboxes group with shadow |
| `frontend/src/views/BookingsView.vue` | **MAWB status column** added (col-span-2): shows real MAWB status per booking with colored square indicator; stats now use MAWB status for Received %; flujograma reduced to col-span-2 with `h-1.5 w-1.5` squares (no rounded-full) |
| `backend/aircargo-api/src/main/java/com/aircargo/service/WarehouseService.java` | **PDF evidence rewritten**: replaced CSS grid + object-fit with table layout; max image size reduced to 150KB; removed unsupported CSS properties for openhtmltopdf compatibility; added `xmlEscape()` helper |
| `backend/aircargo-api/src/main/java/com/aircargo/controller/MawbController.java` | **MAWB PDF evidence rewritten**: same table layout as receipt PDF; added `xmlEscape()` helper; replaced flex/grid with table; reduced image max to 150KB; replaced `→` unicode with `&#8594;` |
| `backend/aircargo-api/src/main/java/com/aircargo/controller/MawbController.java` | Fixed XML/HTML entities for openhtmltopdf: self-closing `<meta/>`, `&mdash;` → `&#8212;`; added `xmlEscape()` helper for all dynamic content to prevent SAXParseException on `&` in user data |
| `backend/aircargo-api/src/main/java/com/aircargo/service/WarehouseService.java` | Fixed XML/HTML entities for openhtmltopdf: self-closing `<meta/>`, `&mdash;` → `&#8212;`, `&middot;` → `&#183;`; added `xmlEscape()` helper for all dynamic content |
| `backend/aircargo-api/src/main/java/com/aircargo/service/PdfGenerationService.java` | **Rewritten** — decodes base64 data URIs in HTML to temp files with `file:///` paths for reliable PDF image rendering; auto-cleanup in `finally` block |
| `backend/aircargo-api/src/main/java/com/aircargo/service/WarehouseService.java` | Simplified `generateSupportingDocsPdf()` — removed temp file handling (now delegated to `PdfGenerationService`) |
| `frontend/src/views/WarehouseReceiptsView.vue` | **HAWB inputs enlarged**: `text-[10px]` → `text-xs`, wider columns (w-20→w-24, w-14→w-16), bigger padding; **Labels darkened**: all form `<label>` elements `text-slate-400` → `text-slate-700`; secondary text darkened to `text-slate-500`; step headers darkened; evidence upload/camera labels darkened; pieza count text darkened |
| `backend/aircargo-api/src/main/java/com/aircargo/service/ReceiptExportService.java` | **Rewritten** — matches reference template `RECIBO_DE_BODEGA_AWB.xlsx`: same merged cells layout (B-C labels, D-E values, F-G checkboxes), same column widths (A-L), 25 data rows, `Dim Weight` = vol/366 (KGS), `Dim LBS` = vol/194; Tahoma font; **Added Evidencias sheet**: signatures (dock/deliveredBy/broker) + supporting docs embedded as images; **Sheet protection**: main sheet protected with password `aircargo2024`, value cells unlocked; uses `ObjectMapper` to parse `supportingDocs` JSON |
| `frontend/src/views/MawbsView.vue` | **Complete redesign**: 6 frozen columns (MAWB/Shipper/Pzas/Kg/Dest/Pcs Disp) with sticky positioning; date headers enlarged `text-[6px]` → `text-[11px]` bold; all table content `text-[10px]` → `text-xs`; piece cells use **chalkboard texture** (dark green gradient + line noise + `radial-gradient`), white chalk-like text with `text-shadow` glow; **SVG mini arc** showing piece distribution per flight; **pop-in animation** on cell entrance with staggered delays; **column hover glow** via `ring-1 ring-inset ring-white/20`; **minimap widget** (teleported) with viewport overlay and `@scroll` tracking; MAWB click navigates to `/receipts?mawbId=xxx` using router push + query params; `WarehouseReceiptsView` reads `route.query.mawbId` on mount to auto-expand the target MAWB; `float button` toggle for minimap visibility |
| `frontend/src/views/MawbsView.vue` | **June 22 updates**: Removed St column; added Pcs Dispatched column (right-aligned, amber ⚠ when > received); Shipper + Consignee stacked vertically in same column; MAWB cell gets status-based background + left border color (gray=BOOKED, blue=RECEIVED, amber=MANIFESTED, emerald=DEPARTED) with Spanish status text subtitle; `totalPieces` capped at warehouse received quantity (`Math.min(dispatched, received)`); per-ULD breakdown in tooltip + "N ULDs" subtitle when >1 ULD per flight; stats bar includes Despachadas count with exceso indicator |
| `frontend/src/views/UldsView.vue` | **Autocomplete MAWB**: replaced `<select>` with text input + filtered suggestions dropdown; keyboard navigation (arrows + enter + escape); MAWB search filters by awbNumber/shipperName; pending pieces logic (receipt > booking) |
| `frontend/src/views/DashboardView.vue` | **Fixed tare formulas**: `netLbs` = gross - realTare (was gross - bellyTare); `payloadLbs` = netLbs (was netLbs + 5 dummy) |
| `frontend/src/components/layout/Sidebar.vue` | **Icon swap**: `IconPackage` → `IconPackageExport` for ULDs; all nav icons increased 16→18 (+10%) |
| `backend/aircargo-api/src/main/java/com/aircargo/service/WarehouseService.java` | **Piece accumulation fix + Booking MAWB sync**: `processWarehouseReceipt()` now deletes existing receipt+pieces for a MAWB before re-emitting; after saving, updates linked Booking's `awbNumber` from MAWB |
| `backend/aircargo-api/src/main/java/com/aircargo/repository/BookingRepository.java` | Added `findByMawbId(UUID)` for booking lookup by MAWB |
| `backend/aircargo-api/src/main/java/com/aircargo/service/ReceiptExportService.java` | Fixed `Workbook` → `XSSFWorkbook` cast error in `createEvidenceSheet` call |
| All 10 `src/views/*View.vue` + `WarehouseForm.vue` | **Font-size standardisation**: table data → `text-[10px]`, table headers → `text-[11px]`, titles → `text-[12px]` |
| `frontend/src/views/WarehouseReceiptsView.vue` | **Pieces loaded from existing receipt**: when editing a receipt, existing pieces are now loaded via `receiptsApi.getPieces()` and displayed in the form; `editReceipt` rewritten to send pieces+evidence via POST `/emit` (backend replaces old receipt entirely); signatures (dock/deliveredBy/broker) from existing receipt shown as images in MAWB evidence section; **Signature evidence enriched**: each signature image now has a companion text card showing the person's name and ID (printName, deliveredByName+ID, brokerName+ID) in the MAWB evidence grid |
| `frontend/src/api/receipts.js` | Added `getPieces(id)` — calls `GET /warehouse/receipts/{id}/pieces` |
| `frontend/src/views/MawbsView.vue` | **Pcs Reserved from Booking**: now reads from `Booking.skids` (via `store.bookings`) instead of `Mawb.pieces`; falls back to `Mawb.pieces` if no booking linked. **Cap removed**: per-flight pieces are now the raw total from ULD-AWB links (no capping by received/dispatched ratio). **ULD count always shown**: each flight cell always shows `N ULDs` below the piece count. **Tooltip changed**: shows "X UPS REPARTIDAS ENTRE Y ULDs" summary. **Bookings loaded**: added `store.loadBookings()` in `onMounted` and `onFlightChange` so booking data is available for matrix building. |

## Recent session changes (June 29, 2026 — Password + Real Users)

| File | Change |
|------|--------|
| `backend/.../entity/AppUser.java` | Added `passwordHash` varchar(255) field (nullable) |
| `backend/.../dto/LoginRequest.java` | Added optional `password` field |
| `backend/.../dto/LoginResponse.java` | Added `hasPasswordSet` boolean field |
| `backend/.../dto/SetPasswordRequest.java` | **NEW** — record with email, newPassword, currentPassword |
| `backend/.../controller/AuthController.java` | Login now verifies password via BCrypt if `passwordHash` is set; returns 428 if password required but missing; added `POST /api/auth/set-password` (sets or changes password, returns JWT) |
| `backend/resources/db/migration/V10__add_password_hash_and_seed_real_users.sql` | **NEW** — adds `password_hash` column, seeds 6 real users: jsantos@rannik.com (ADMIN), esantana@rannik.com (SUPERVISOR → SUPER_USER), dchestaro@rannik.com (OPERATIONS), ilsantana@rannik.com (WAREHOUSE_ASSISTANT), earellano@ups.com (TRAFFIC), jcastrolopez@ups.com (LOAD_PLANNER) — all with null password_hash |
| `database/migrations/V10__add_password_hash_and_seed_real_users.sql` | Synced from backend |
| `frontend/src/api/auth.js` | Added `setPassword(email, newPassword, currentPassword)` |
| `frontend/src/stores/auth.js` | Added `hasPasswordSet` ref (persisted); `login()` now accepts optional password parameter |
| `frontend/src/views/LoginView.vue` | Added password input (shown when backend returns 428); "Establecer contraseña" link redirects to /set-password |
| `frontend/src/views/SetPasswordView.vue` | **NEW** — first-time password setup page; fields: email (readonly from query), currentPassword (only if has existing password), newPassword, confirm; validates match + min 6 chars; auto-redirects to /login after success |
| `frontend/src/router/index.ts` | Added `/set-password` route; both `/login` and `/set-password` marked as `publicPaths` (no auth required) |

## Recent session changes (June 29, 2026 — Settings + Users Views + Audit)

| File | Change |
|------|--------|
| `backend/.../AircargoApiApplication.java` | Added `@EnableScheduling` for `ActiveSessionTracker` purge |
| `backend/.../auth/UserPrincipal.java` | Added `fullName` field to record |
| `backend/.../auth/JwtUtil.java` | `generateToken()` now accepts `fullName` parameter, stores as JWT claim |
| `backend/.../auth/JwtAuthFilter.java` | Extracts `fullName` from JWT claims when building `UserPrincipal` |
| `backend/.../entity/AuditLog.java` | **NEW** — entity with userId, email, fullName, action, entityType, entityId, details, ipAddress, createdAt |
| `backend/.../dto/AuditLogDTO.java` | **NEW** — DTO with `fromEntity()` mapper |
| `backend/.../dto/ConnectedUserDTO.java` | **NEW** — DTO for connected users (userId, email, fullName, role, lastHeartbeat, lastLogin) |
| `backend/.../repository/AuditLogRepository.java` | **NEW** — findByUserId, findAllByOrderByCreatedAtDesc, findByAction |
| `backend/.../service/AuditService.java` | **NEW** — helper methods: log, logLogin, logUserCreate, logUserUpdate, logUserDelete, logPasswordReset |
| `backend/.../service/ActiveSessionTracker.java` | **NEW** — in-memory `ConcurrentHashMap` tracking user heartbeats; `@Scheduled` purge every 60s; 5min timeout |
| `backend/.../controller/AuditLogController.java` | **NEW** — `GET /api/audit-logs` with optional `userId` filter |
| `backend/.../controller/AuthController.java` | Added `auditService.logLogin()` on login; added `auditService.log()` on set-password; added `GET /api/auth/heartbeat` for session tracking |
| `backend/.../controller/AppUserController.java` | Added audit logging on create/update/delete; added `POST /api/users/{id}/reset-password` (clears hash); added `GET /api/users/connected`; prevent self-delete |
| `backend/.../service/AppUserService.java` | Added `resetPassword(UUID id)` to interface |
| `backend/.../service/AppUserServiceImpl.java` | `create()` sets null password_hash; `update()` preserves existing password_hash; `resetPassword()` clears it |
| `backend/.../config/SecurityConfig.java` | Added `.requestMatchers("/api/audit-logs/**").hasAnyAuthority("ADMIN", "SUPERVISOR")` (later renamed to `SUPER_USER`) |
| `backend/.../resources/db/migration/V11__create_audit_log.sql` | **NEW** — CREATE TABLE `audit_log` with indexes on userId, action, createdAt, entity |
| `database/migrations/V11__create_audit_log.sql` | Synced from backend |
| `frontend/src/api/users.js` | **NEW** — all user CRUD + resetPassword + getConnected + getAuditLogs + heartbeat |
| `frontend/src/views/SettingsView.vue` | **NEW** — user management: list/create/edit/delete users, reset passwords, role assignment, active toggle |
| `frontend/src/views/UsersView.vue` | **NEW** — connected users (live cards with green dot) + audit log table with user filter + action colors |
| `frontend/src/router/index.ts` | Added `/users` → UsersView + `/settings` → SettingsView routes |
| `frontend/src/components/layout/Header.vue` | Added `/settings`: 'Configuración' title |
| `frontend/src/App.vue` | Heartbeat interval (60s) calls `GET /api/auth/heartbeat` when authenticated |

## Recent session changes (June 29, 2026 — Audit extendido a todos los controladores)

| File | Change |
|------|--------|
| `frontend/src/App.vue` | Heartbeat interval (60s) calls `GET /api/auth/heartbeat` when authenticated |
| `backend/.../controller/FlightController.java` | Added `AuditService` injection + `@AuthenticationPrincipal` + `HttpServletRequest`; audit logging on create/update/delete with null-safe principal check |
| `backend/.../controller/BookingController.java` | Added `AuditService` injection + `@AuthenticationPrincipal` + `HttpServletRequest`; audit logging on create/update/delete/updateAwb with null-safe principal check |
| `backend/.../controller/MawbController.java` | Added `AuditService` injection + `@AuthenticationPrincipal` + `HttpServletRequest`; audit logging on createMawb, updateMawb, updateMawbStatus, updateSupportingDocs with null-safe principal check |
| `backend/.../controller/UldController.java` | Added `AuditService` injection + `@AuthenticationPrincipal` + `HttpServletRequest`; audit logging on create, update, assignFlight, transferUld, delete with null-safe principal check |
| `backend/.../controller/WarehouseController.java` | Added `AuditService` injection + `@AuthenticationPrincipal` + `HttpServletRequest`; audit logging on emitWarehouseReceipt, updateWarehouseReceipt with null-safe principal check |
| `backend/.../test/.../BookingControllerTest.java` | Updated: replaced `@Mock AuditService` with `@Mock AuditLogRepository` + real `AuditService` (bytebuddy/Java 25 compat) |
| `backend/.../test/.../BookingControllerIntegrationTest.java` | Updated: passes null principal to skip audit (filter chain clears SecurityContext) |

Entity types auditadas: `FLIGHT`, `BOOKING`, `MAWB`, `ULD`, `RECEIPT`. Cada create/update/delete queda registrado en `audit_log` con usuario, acción, entidad, detalles JSON e IP. El principal se verifica con null-safety para compatibilidad con tests y edge cases.

## Build

```sh
npm run build         # Vite build succeeds (type-check has pre-existing errors)
npm run type-check    # Fails on pre-existing .vue module declarations + ulds.ts strict mode
```

## Recent session changes (June 29, 2026 — RBAC + Cache + Async)

| File | Change |
|------|--------|
| `backend/pom.xml` | Added `spring-boot-starter-security`, `jjwt-api/impl/jackson 0.12.6`, `spring-boot-starter-cache`, `caffeine` |
| `backend/.../entity/UserRole.java` | Replaced 4 old roles with 6 new roles: `WAREHOUSE_ASSISTANT`, `OPERATIONS`, `TRAFFIC`, `LOAD_PLANNER`, `ADMIN`, `SUPERVISOR` (later renamed to `SUPER_USER`) |
| `backend/.../entity/AppUser.java` | Removed `columnDefinition = "user_role"` (now varchar), default `WAREHOUSE_ASSISTANT` |
| `backend/.../auth/JwtUtil.java` | **NEW** — HMAC-SHA512 JWT generation/validation via jjwt 0.12.6 |
| `backend/.../auth/UserPrincipal.java` | **NEW** — `record` carrying userId, role, airlineId, email |
| `backend/.../auth/JwtAuthFilter.java` | **NEW** — `OncePerRequestFilter` extracts JWT from Bearer header, sets SecurityContext |
| `backend/.../config/SecurityConfig.java` | **NEW** — URL-based role checks per view (`@Profile("!test")`) |
| `backend/.../controller/AuthController.java` | **NEW** — `POST /api/auth/login` returns JWT + user info |
| `backend/.../dto/LoginRequest.java` | **NEW** — `record` with email |
| `backend/.../dto/LoginResponse.java` | **NEW** — `record` with token, user info |
| `backend/.../service/PermissionService.java` | **NEW** — role→view mapping for programmatic checks |
| `backend/.../event/ReceiptCreatedEvent.java` | **NEW** — record published after receipt creation |
| `backend/.../event/ReceiptEventListener.java` | **NEW** — `@Async` + `@EventListener` for background processing |
| `backend/.../AircargoApiApplication.java` | Added `@EnableCaching`, `@EnableAsync` |
| `backend/.../service/FlightServiceImpl.java` | Added `@Cacheable("flights")` on getAll, `@CacheEvict` on create/update/delete |
| `backend/.../service/AirlineServiceImpl.java` | Added `@Cacheable("airlines")` on getAll/getById, `@CacheEvict` on create/update/delete |
| `backend/.../service/WarehouseService.java` | Publishes `ReceiptCreatedEvent` after receipt save; added `pdfService` + `eventPublisher` fields |
| `backend/.../config/WebConfig.java` | Added `CorsConfigurationSource` bean for Spring Security compatibility |
| `backend/.../repository/AppUserRepository.java` | Added `findByEmail(String)` + `existsByEmail(String)` |
| `backend/resources/db/migration/V8__update_user_role_to_varchar.sql` | **NEW** — converts `role` column from enum to varchar(50), drops type, seeds 3 users |
| `backend/resources/db/migration/V9__seed_more_users.sql` | **NEW** — seeds warehouse, operations, admin users |
| `database/migrations/V8__update_user_role_to_varchar.sql` | Synced from backend |
| `database/migrations/V9__seed_more_users.sql` | Synced from backend |
| `backend/.../config/TestSecurityConfig.java` | **NEW** — `@Profile("test")` permits all requests for integration tests |
| `frontend/src/api/client.js` | Added request interceptor (Bearer token from localStorage) + 401 auto-redirect |
| `frontend/src/api/auth.js` | **NEW** — `authApi.login(email)` |
| `frontend/src/stores/auth.js` | **NEW** — Pinia auth store with localStorage persistence, `canView()` permission check |
| `frontend/src/views/LoginView.vue` | **NEW** — email-only login form |
| `frontend/src/router/index.ts` | Added `/login` route, `beforeEach` guards (auth check + role permission check) |
| `frontend/src/App.vue` | Renders `<Sidebar>` only when authenticated; login view when not |
| `frontend/src/components/layout/Sidebar.vue` | Dynamic nav items by `canView()`, real user info from auth store, logout button |

## Import paths

Frontend uses `@/` → `./src/` (configured in `vite.config.ts` and `tsconfig.app.json`).
