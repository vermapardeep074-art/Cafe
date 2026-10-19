-- ==============================================================================
-- ARTESANO CAFE — PRODUCTION DATABASE SCHEMA & ROW LEVEL SECURITY (RLS)
-- Database Engine: PostgreSQL / Supabase
-- Target Environment: Vercel + Supabase
-- Security Model: Zero-Trust Strict Role-Based Access Control (RBAC)
-- ==============================================================================

-- 1. EXTENSIONS
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ==============================================================================
-- 2. STAFF & OWNER AUTHORIZATION TABLE & FUNCTIONS
-- CRITICAL SECURITY: Never trust "auth.uid() IS NOT NULL" alone for administrative
-- operations. Any user who signs up via Supabase Auth is an authenticated user.
-- Only accounts verified in public.staff_members or with admin metadata may
-- modify store settings, dishes, prices, orders, or view private guest reservations.
-- ==============================================================================

CREATE TABLE IF NOT EXISTS public.staff_members (
    user_id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    email TEXT UNIQUE NOT NULL,
    role TEXT NOT NULL DEFAULT 'staff' CHECK (role IN ('owner', 'manager', 'staff')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

ALTER TABLE public.staff_members ENABLE ROW LEVEL SECURITY;

-- Helper function: Check if current authenticated caller is authorized staff/owner
CREATE OR REPLACE FUNCTION public.is_staff()
RETURNS BOOLEAN
LANGUAGE sql
SECURITY DEFINER
STABLE
SET search_path = public
AS $$
    SELECT (
        -- 1. Explicit membership in staff table
        EXISTS (
            SELECT 1 FROM public.staff_members 
            WHERE user_id = auth.uid()
        )
        OR
        -- 2. Staff/Owner role injected in JWT app_metadata
        ((auth.jwt() -> 'app_metadata' ->> 'role') IN ('owner', 'manager', 'staff', 'admin'))
        OR
        -- 3. Verified primary owner email
        ((auth.jwt() ->> 'email') = 'owner@artesanocafe.com')
    );
$$;

-- Helper function: Check if caller is cafe owner
CREATE OR REPLACE FUNCTION public.is_owner()
RETURNS BOOLEAN
LANGUAGE sql
SECURITY DEFINER
STABLE
SET search_path = public
AS $$
    SELECT (
        EXISTS (
            SELECT 1 FROM public.staff_members 
            WHERE user_id = auth.uid() AND role = 'owner'
        )
        OR
        ((auth.jwt() -> 'app_metadata' ->> 'role') IN ('owner', 'admin'))
        OR
        ((auth.jwt() ->> 'email') = 'owner@artesanocafe.com')
    );
$$;

-- RLS for staff_members table
DROP POLICY IF EXISTS "Staff can view staff roster" ON public.staff_members;
CREATE POLICY "Staff can view staff roster"
ON public.staff_members FOR SELECT
TO authenticated
USING (public.is_staff());

DROP POLICY IF EXISTS "Only owner can modify staff roster" ON public.staff_members;
CREATE POLICY "Only owner can modify staff roster"
ON public.staff_members FOR ALL
TO authenticated
USING (public.is_owner())
WITH CHECK (public.is_owner());

-- Auto-enroll owner@artesanocafe.com trigger
CREATE OR REPLACE FUNCTION public.handle_new_staff_user()
RETURNS TRIGGER
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public
AS $$
BEGIN
    IF NEW.email = 'owner@artesanocafe.com' THEN
        INSERT INTO public.staff_members (user_id, email, role)
        VALUES (NEW.id, NEW.email, 'owner')
        ON CONFLICT (user_id) DO NOTHING;
    END IF;
    RETURN NEW;
END;
$$;

DO $$
BEGIN
    DROP TRIGGER IF EXISTS on_auth_user_created_staff ON auth.users;
    CREATE TRIGGER on_auth_user_created_staff
        AFTER INSERT ON auth.users
        FOR EACH ROW EXECUTE FUNCTION public.handle_new_staff_user();
EXCEPTION WHEN OTHERS THEN
    NULL;
END $$;

-- ==============================================================================
-- 3. APPLICATION TABLES
-- ==============================================================================

-- 3.1 Store Settings & Branding
CREATE TABLE IF NOT EXISTS public.store_settings (
    id TEXT PRIMARY KEY DEFAULT 'default',
    headline TEXT NOT NULL DEFAULT 'Fresh · Bright · Plant-Filled · Homemade · Daily',
    subheadline TEXT NOT NULL DEFAULT 'Artisan neighbourhood café & bakery with Italian influence in Tribeca, New York City.',
    morning_window TEXT NOT NULL DEFAULT 'Morning Menu Served Daily 8:00 AM – 11:00 AM',
    announcement_active BOOLEAN NOT NULL DEFAULT false,
    announcement TEXT NOT NULL DEFAULT 'Special Morning Pastry Tasting this week in Tribeca!',
    phone TEXT NOT NULL DEFAULT '+1 212-372-0297',
    address TEXT NOT NULL DEFAULT '90 Chambers St Unit B, New York, NY 10007, United States',
    hours TEXT NOT NULL DEFAULT 'Monday – Friday: 7:30 AM – 6:00 PM\nSaturday – Sunday: 8:00 AM – 5:00 PM',
    status TEXT NOT NULL DEFAULT 'OPEN',
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Seed default settings
INSERT INTO public.store_settings (id, headline, subheadline, morning_window, announcement_active, announcement, phone, address, hours, status)
VALUES (
    'default',
    'Fresh · Bright · Plant-Filled · Homemade · Daily',
    'Artisan neighbourhood café & bakery with Italian influence in Tribeca, New York City.',
    'Morning Menu Served Daily 8:00 AM – 11:00 AM',
    false,
    'Special Morning Pastry Tasting this week in Tribeca!',
    '+1 212-372-0297',
    '90 Chambers St Unit B, New York, NY 10007, United States',
    'Monday – Friday: 7:30 AM – 6:00 PM\nSaturday – Sunday: 8:00 AM – 5:00 PM',
    'OPEN'
) ON CONFLICT (id) DO NOTHING;

-- 3.2 Menu Items
CREATE TABLE IF NOT EXISTS public.menu_items (
    id TEXT PRIMARY KEY DEFAULT 'dish_' || gen_random_uuid(),
    name TEXT NOT NULL,
    price NUMERIC(10, 2) NOT NULL CHECK (price >= 0),
    description TEXT DEFAULT '',
    category TEXT NOT NULL DEFAULT 'Morning',
    badge TEXT DEFAULT '',
    in_stock BOOLEAN NOT NULL DEFAULT true,
    image_url TEXT NOT NULL DEFAULT 'https://images.unsplash.com/photo-1501339847302-ac426a4a7cbb?w=600&auto=format&fit=crop',
    sort_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 3.3 Categories
CREATE TABLE IF NOT EXISTS public.categories (
    id TEXT PRIMARY KEY DEFAULT 'cat_' || gen_random_uuid(),
    name TEXT UNIQUE NOT NULL,
    sort_order INT NOT NULL DEFAULT 0
);

-- Seed initial categories
INSERT INTO public.categories (name, sort_order) VALUES
    ('Morning', 1),
    ('Sandwich', 2),
    ('Bakery', 3),
    ('Coffee', 4),
    ('Beverages', 5)
ON CONFLICT (name) DO NOTHING;

-- Seed default menu items
INSERT INTO public.menu_items (id, name, price, description, category, badge, in_stock, image_url, sort_order) VALUES
    ('sw_1', 'Avocado Toast', 9.95, 'Sourdough toast topped with smashed avocado, lime, chili flakes, and microgreens.', 'Morning', 'VEGAN', true, 'https://images.unsplash.com/photo-1588137378633-dea1336ce1e2?w=600&auto=format&fit=crop', 1),
    ('sw_6', 'Pan con Chicharrón', 10.95, 'Crispy pork belly, fried sweet potato, and salsa criolla on fresh artisan bread.', 'Sandwich', 'POPULAR', true, 'https://images.unsplash.com/photo-1509722747041-616f39b57569?w=600&auto=format&fit=crop', 2),
    ('sw_3', 'Classic Omelette', 13.95, 'Folded golden omelette served with seasoned home fries and herb salad.', 'Morning', 'CHEF SPECIAL', true, 'https://images.unsplash.com/photo-1510693206972-df098062cb71?w=600&auto=format&fit=crop', 3),
    ('p_1', 'Peruvian Alfajores', 4.95, 'Handmade cornstarch shortbread cookies sandwiched with rich dulce de leche.', 'Bakery', 'HOMEMADE', true, 'https://images.unsplash.com/photo-1558961363-fa8fdf82db35?w=600&auto=format&fit=crop', 4),
    ('c_4', 'Cortado', 4.00, 'Equal parts rich espresso and silky warm milk in a black ceramic glass.', 'Coffee', 'SPECIALTY', true, 'https://images.unsplash.com/photo-1514432324607-a09d9b4aefdd?w=600&auto=format&fit=crop', 5),
    ('p_2', 'Almond Croissant', 5.50, 'Flaky twice-baked croissant filled with rich almond frangipane cream.', 'Bakery', 'FRESH DAILY', true, 'https://images.unsplash.com/photo-1555507036-ab1f4038808a?w=600&auto=format&fit=crop', 6),
    ('sw_5', 'Burrata Caprese', 14.95, 'Creamy fresh burrata with heirloom tomatoes, basil pesto, and aged balsamic reduction.', 'Sandwich', 'VEGETARIAN', true, 'https://images.unsplash.com/photo-1592417817098-8f3d69109853?w=600&auto=format&fit=crop', 7),
    ('c_1', 'Iced Caramel Macchiato', 5.75, 'Chilled espresso poured over creamy oat milk with vanilla syrup and house caramel drizzle.', 'Beverages', 'COLD BREW', true, 'https://images.unsplash.com/photo-1517701604599-bb29b565090c?w=600&auto=format&fit=crop', 8)
ON CONFLICT (id) DO NOTHING;

-- 3.4 Customer Orders (Counter Pickup & Dine-In)
CREATE TABLE IF NOT EXISTS public.orders (
    id TEXT PRIMARY KEY DEFAULT 'ORD-' || FLOOR(100 + RANDOM() * 900)::TEXT,
    customer_name TEXT NOT NULL,
    service_mode TEXT NOT NULL DEFAULT 'Takeaway / Counter Pickup',
    items_summary TEXT NOT NULL,
    subtotal NUMERIC(10, 2) NOT NULL DEFAULT 0.00 CHECK (subtotal >= 0 AND subtotal <= 2500),
    tax NUMERIC(10, 2) NOT NULL DEFAULT 0.00 CHECK (tax >= 0 AND tax <= 500),
    total NUMERIC(10, 2) NOT NULL DEFAULT 0.00 CHECK (total >= 0 AND total <= 3000),
    status TEXT NOT NULL DEFAULT 'RECEIVED' CHECK (status IN ('RECEIVED', 'PREPARING', 'READY', 'COMPLETED', 'CANCELLED') OR status LIKE 'REFUNDED%'),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 3.5 Table Reservations
CREATE TABLE IF NOT EXISTS public.reservations (
    id TEXT PRIMARY KEY DEFAULT 'RES-' || FLOOR(100 + RANDOM() * 900)::TEXT,
    name TEXT NOT NULL,
    phone TEXT NOT NULL,
    date DATE NOT NULL,
    time TEXT NOT NULL,
    guests INT NOT NULL CHECK (guests BETWEEN 1 AND 20),
    notes TEXT DEFAULT '',
    status TEXT NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'CONFIRMED', 'SEATED', 'CANCELLED')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 3.6 Admin Audit Log (Immutable)
CREATE TABLE IF NOT EXISTS public.audit_logs (
    id BIGSERIAL PRIMARY KEY,
    action_text TEXT NOT NULL,
    actor_email TEXT DEFAULT 'owner@artesanocafe.com',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Indexes for performance & query speed
CREATE INDEX IF NOT EXISTS idx_menu_category ON public.menu_items(category);
CREATE INDEX IF NOT EXISTS idx_menu_sort ON public.menu_items(sort_order);
CREATE INDEX IF NOT EXISTS idx_orders_created ON public.orders(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_orders_status ON public.orders(status);
CREATE INDEX IF NOT EXISTS idx_reservations_date ON public.reservations(date, time);
CREATE INDEX IF NOT EXISTS idx_audit_created ON public.audit_logs(created_at DESC);

-- ==============================================================================
-- 4. ROW LEVEL SECURITY (RLS) POLICIES — ZERO TRUST ARCHITECTURE
-- ==============================================================================

ALTER TABLE public.store_settings ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.menu_items ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.categories ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.orders ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.reservations ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.audit_logs ENABLE ROW LEVEL SECURITY;

-- 4.1 Store Settings
-- Public can view cafe branding, hours, and status
DROP POLICY IF EXISTS "Public can view store settings" ON public.store_settings;
CREATE POLICY "Public can view store settings" 
ON public.store_settings FOR SELECT 
TO anon, authenticated 
USING (true);

-- ONLY authorized staff can update store settings
DROP POLICY IF EXISTS "Only staff can update store settings" ON public.store_settings;
CREATE POLICY "Only staff can update store settings" 
ON public.store_settings FOR UPDATE 
TO authenticated 
USING (public.is_staff())
WITH CHECK (public.is_staff());

-- 4.2 Menu Items
-- Public can view menu items
DROP POLICY IF EXISTS "Public can view active menu items" ON public.menu_items;
CREATE POLICY "Public can view active menu items" 
ON public.menu_items FOR SELECT 
TO anon, authenticated 
USING (true);

-- ONLY authorized staff can modify menu items
DROP POLICY IF EXISTS "Staff can insert menu items" ON public.menu_items;
CREATE POLICY "Staff can insert menu items" 
ON public.menu_items FOR INSERT 
TO authenticated 
WITH CHECK (public.is_staff());

DROP POLICY IF EXISTS "Staff can update menu items" ON public.menu_items;
CREATE POLICY "Staff can update menu items" 
ON public.menu_items FOR UPDATE 
TO authenticated 
USING (public.is_staff())
WITH CHECK (public.is_staff());

DROP POLICY IF EXISTS "Staff can delete menu items" ON public.menu_items;
CREATE POLICY "Staff can delete menu items" 
ON public.menu_items FOR DELETE 
TO authenticated 
USING (public.is_staff());

-- 4.3 Categories
DROP POLICY IF EXISTS "Public can view categories" ON public.categories;
CREATE POLICY "Public can view categories" 
ON public.categories FOR SELECT 
TO anon, authenticated 
USING (true);

DROP POLICY IF EXISTS "Staff can manage categories" ON public.categories;
CREATE POLICY "Staff can manage categories" 
ON public.categories FOR ALL 
TO authenticated 
USING (public.is_staff())
WITH CHECK (public.is_staff());

-- 4.4 Customer Orders
-- Public (anonymous customers) can INSERT orders with strict payload & status validation:
-- 1. Status MUST be 'RECEIVED' (cannot insert pre-completed or fraudulent orders)
-- 2. Customer name length must be between 2 and 100 characters
-- 3. Items summary length must be between 3 and 2000 characters
-- 4. Subtotal, tax, and total must be non-negative and within reasonable cafe bounds
-- 5. CANNOT read back other customers' orders
DROP POLICY IF EXISTS "Public customers can create orders" ON public.orders;
CREATE POLICY "Public customers can create orders" 
ON public.orders FOR INSERT 
TO anon, authenticated 
WITH CHECK (
    customer_name IS NOT NULL AND 
    LENGTH(TRIM(customer_name)) >= 2 AND 
    LENGTH(customer_name) <= 100 AND
    items_summary IS NOT NULL AND 
    LENGTH(TRIM(items_summary)) >= 3 AND 
    LENGTH(items_summary) <= 2000 AND
    subtotal >= 0 AND subtotal <= 2000 AND
    tax >= 0 AND tax <= 500 AND
    total >= 0 AND total <= 2500 AND
    service_mode IN ('Takeaway / Counter Pickup', 'Dine-In', 'Takeaway') AND
    status = 'RECEIVED'
);

-- ONLY authorized staff can SELECT, UPDATE, or DELETE orders
DROP POLICY IF EXISTS "Staff can view all customer orders" ON public.orders;
CREATE POLICY "Staff can view all customer orders" 
ON public.orders FOR SELECT 
TO authenticated 
USING (public.is_staff());

DROP POLICY IF EXISTS "Staff can update order status" ON public.orders;
CREATE POLICY "Staff can update order status" 
ON public.orders FOR UPDATE 
TO authenticated 
USING (public.is_staff())
WITH CHECK (public.is_staff());

DROP POLICY IF EXISTS "Owner can delete orders" ON public.orders;
CREATE POLICY "Owner can delete orders" 
ON public.orders FOR DELETE 
TO authenticated 
USING (public.is_owner());

-- 4.5 Table Reservations
-- Public customers can submit table requests with strict validation:
-- 1. Status MUST be 'PENDING' (cannot self-confirm or seat)
-- 2. Guest count must be between 1 and 20
-- 3. Date must be on or after current date (cannot book in the past)
-- 4. CANNOT read back phone numbers or private notes of other guests
DROP POLICY IF EXISTS "Public customers can submit reservations" ON public.reservations;
CREATE POLICY "Public customers can submit reservations" 
ON public.reservations FOR INSERT 
TO anon, authenticated 
WITH CHECK (
    name IS NOT NULL AND 
    LENGTH(TRIM(name)) >= 2 AND 
    LENGTH(name) <= 100 AND
    phone IS NOT NULL AND 
    LENGTH(TRIM(phone)) >= 7 AND 
    LENGTH(phone) <= 30 AND
    guests >= 1 AND guests <= 20 AND
    date >= CURRENT_DATE AND
    status = 'PENDING'
);

-- ONLY authorized staff can view reservations (protecting guest PII: phone, date, notes)
DROP POLICY IF EXISTS "Staff can view all table reservations" ON public.reservations;
CREATE POLICY "Staff can view all table reservations" 
ON public.reservations FOR SELECT 
TO authenticated 
USING (public.is_staff());

DROP POLICY IF EXISTS "Staff can manage table reservations" ON public.reservations;
CREATE POLICY "Staff can manage table reservations" 
ON public.reservations FOR UPDATE 
TO authenticated 
USING (public.is_staff())
WITH CHECK (public.is_staff());

DROP POLICY IF EXISTS "Staff can delete table reservations" ON public.reservations;
CREATE POLICY "Staff can delete table reservations" 
ON public.reservations FOR DELETE 
TO authenticated 
USING (public.is_staff());

-- 4.6 Audit Logs (Immutable)
DROP POLICY IF EXISTS "Staff can view audit logs" ON public.audit_logs;
CREATE POLICY "Staff can view audit logs" 
ON public.audit_logs FOR SELECT 
TO authenticated 
USING (public.is_staff());

DROP POLICY IF EXISTS "Staff can write audit logs" ON public.audit_logs;
CREATE POLICY "Staff can write audit logs" 
ON public.audit_logs FOR INSERT 
TO authenticated 
WITH CHECK (public.is_staff());

-- ==============================================================================
-- 5. IDEMPOTENT REALTIME NOTIFICATIONS (SUPABASE REALTIME)
-- Enable realtime broadcasting so the kitchen and admin display update live.
-- Safe for repeated execution without throwing duplicate object errors.
-- ==============================================================================
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_publication_tables WHERE pubname = 'supabase_realtime' AND schemaname = 'public' AND tablename = 'orders') THEN
        ALTER PUBLICATION supabase_realtime ADD TABLE public.orders;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_publication_tables WHERE pubname = 'supabase_realtime' AND schemaname = 'public' AND tablename = 'reservations') THEN
        ALTER PUBLICATION supabase_realtime ADD TABLE public.reservations;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_publication_tables WHERE pubname = 'supabase_realtime' AND schemaname = 'public' AND tablename = 'menu_items') THEN
        ALTER PUBLICATION supabase_realtime ADD TABLE public.menu_items;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_publication_tables WHERE pubname = 'supabase_realtime' AND schemaname = 'public' AND tablename = 'store_settings') THEN
        ALTER PUBLICATION supabase_realtime ADD TABLE public.store_settings;
    END IF;
END $$;
