# Artesano Cafe — Tribeca, New York

Artisan neighbourhood café & bakery platform deployed on Vercel and built with Android Native (Jetpack Compose).

## Project Overview

- **Location:** 90 Chambers St Unit B, New York, NY 10007, United States
- **Direct Line:** +1 212-372-0297
- **Operating Hours:** Mon–Fri 7:30 AM – 6:00 PM · Sat–Sun 8:00 AM – 5:00 PM (America/New_York)
- **Morning Menu:** Served Daily 8:00 AM – 11:00 AM

---

## 1. Web Deployment (Vercel)

The website is configured with `vercel.json` for static deployment on Vercel Edge.

### How to Deploy on Vercel:

1. **Log in to Vercel:** Go to [vercel.com](https://vercel.com) and sign in.
2. **Add New Project:** Click **Add New...** > **Project**.
3. **Import GitHub Repository:** Select `vermapardeep074-art/Cafe`.
4. **Project Settings:**
   - **Framework Preset:** `Other` (Static HTML)
   - **Root Directory:** `./`
   - **Output Directory:** `public` (`vercel.json` specifies `outputDirectory: "public"`)
5. **Click Deploy:** Vercel deploys the application in under 30 seconds.

### Key Routes:
- Public Customer Website: `/`
- Owner Admin Panel: `/admin`

---

## 2. Owner Administration Panel

Access the Owner CMS at `https://your-domain.vercel.app/admin`.

- **Initial Setup:** On first access on any authorized browser, the portal prompts the authorized owner to set their own confidential master password (minimum 8 characters).
- **Security Features:**
  - Salted SHA-256 client-side cryptographic hashing.
  - Rate limiting: 5 consecutive failed attempts locks out the login form for 15 minutes with a live countdown timer.
  - Automatic 60-minute inactivity session expiration.
  - Confidential password rotation via the **Security & Audit** tab.
- **Storage Mode Notice:**
  - This installation uses local browser storage (`localStorage`). To transfer menu edits or settings to another computer or smartphone, use the **Export Backup** and **Import Backup** buttons in the Backup & Restore tab.
  - For real-time multi-device cloud synchronization across multiple customer smartphones and kitchen order terminals, connect a cloud database (Firebase Firestore or Supabase).

### What the Cafe Owner Can Do Without Writing Code:
1. **Menu & Dishes:**
   - Add new dishes with photos, titles, prices, descriptions, and dietary badges (Vegan, Vegetarian, Gluten-Free).
   - In-place price editing without rebuilding the website.
   - Toggle availability ("In Stock" / "Sold Out") in real-time.
   - Reorder dishes up and down.
   - Delete dishes with confirmation dialogs.
2. **Kitchen & Orders Pipeline:**
   - Real-time customer pickup orders submitted through the website cart.
   - Status transitions: `RECEIVED` → `PREPARING` → `READY` → `COMPLETED`.
   - Refund and cancellation recording with documented reasons.
3. **Table Reservations:**
   - Customer reservation requests with dates, party sizes, and guest phone numbers.
   - Seat or cancel reservations.
4. **Branding & Hero Copy:**
   - Update cafe headline, subheadline, and morning schedule.
   - Toggle and edit the top announcement banner.
   - Upload custom brand logos and hero photos.
5. **Backup & Restore:**
   - Download a complete, clean JSON backup of all menu dishes and settings.
   - Import and validate backups on any computer or phone browser.

---

## 3. Android Native Mobile App

The project includes a native Android application built with Kotlin and Jetpack Compose Material 3:
- **Application ID:** `com.aistudio.artesanocafe.nypub`
- **Build System:** Gradle (Kotlin DSL)
- **Compilation:** Run `./gradlew assembleDebug` or export the signed APK.
- **GitHub Actions Workflow:** Automatically builds and tests the APK on git push via `.github/workflows/build-apk.yml`.
