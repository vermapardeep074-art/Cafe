/**
 * ARTESANO CAFE — Production Supabase & Security Client Library
 * Handles:
 * 1. Supabase Auth (Official JWT-based authentication)
 * 2. PostgreSQL Row Level Security (RLS) interaction
 * 3. Realtime WebSockets for kitchen order alerts
 * 4. Zero-Trust Input Sanitization & Anti-XSS Protection
 */

(function (window) {
  'use strict';

  // 1. SECURITY & ANTI-XSS UTILITIES
  const Security = {
    /**
     * Encodes untrusted input to safe HTML entities.
     * Prevents Stored and Reflected XSS (e.g. <img src=x onerror=alert(1)>).
     */
    escapeHtml: function (str) {
      if (str === null || str === undefined) return '';
      return String(str)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#x27;')
        .replace(/\//g, '&#x2F;');
    },

    /**
     * Sanitizes URL protocols to prevent javascript: or data: script execution.
     */
    sanitizeUrl: function (url) {
      if (!url) return '';
      const trimmed = String(url).trim();
      if (/^(https?:\/\/|\/|data:image\/(png|jpeg|jpg|webp|gif|avif);base64,)/i.test(trimmed)) {
        return trimmed;
      }
      return 'https://images.unsplash.com/photo-1501339847302-ac426a4a7cbb?w=600&auto=format&fit=crop';
    },

    /**
     * Validates image uploads strictly by MIME type and size limit.
     */
    validateImageFile: function (file, maxBytes = 2 * 1024 * 1024) {
      if (!file) return { valid: false, error: 'No file selected.' };
      const allowedTypes = ['image/jpeg', 'image/png', 'image/webp', 'image/gif', 'image/avif'];
      if (!allowedTypes.includes(file.type)) {
        return { valid: false, error: 'Invalid file format. Allowed formats: JPEG, PNG, WebP, GIF, AVIF.' };
      }
      if (file.size > maxBytes) {
        return { valid: false, error: 'File size exceeds the 2MB limit.' };
      }
      return { valid: true };
    },

    /**
     * Validates imported JSON schema to prevent injection of malicious structures.
     */
    validateBackupSchema: function (data) {
      if (!data || typeof data !== 'object') {
        return { valid: false, error: 'Backup is not a valid JSON object.' };
      }
      const menu = Array.isArray(data.menu) ? data.menu : (Array.isArray(data) ? data : null);
      if (!menu) {
        return { valid: false, error: 'Backup does not contain a valid menu array.' };
      }

      // Validate each dish structure
      const sanitizedMenu = [];
      for (let i = 0; i < menu.length; i++) {
        const item = menu[i];
        if (!item || typeof item !== 'object') continue;
        const price = parseFloat(item.price);
        if (isNaN(price) || price < 0) continue;

        sanitizedMenu.push({
          id: String(item.id || 'dish_' + Date.now() + '_' + i).slice(0, 64),
          name: Security.escapeHtml(String(item.name || 'Unnamed Dish').slice(0, 100)),
          price: price,
          desc: Security.escapeHtml(String(item.desc || '').slice(0, 500)),
          cat: Security.escapeHtml(String(item.cat || 'Morning').slice(0, 50)),
          badge: Security.escapeHtml(String(item.badge || '').slice(0, 30)),
          inStock: item.inStock !== false,
          img: Security.sanitizeUrl(item.img)
        });
      }

      if (sanitizedMenu.length === 0) {
        return { valid: false, error: 'No valid dish records found in backup.' };
      }

      return { valid: true, menu: sanitizedMenu, settings: data.settings || null };
    }
  };

  // 2. SUPABASE BACKEND MANAGER
  const ArtesanoBackend = {
    client: null,
    isCloudActive: false,

    // Default Seed Data
    defaultMenu: [
      { id: "sw_1", name: "Avocado Toast", price: 9.95, desc: "Sourdough toast topped with smashed avocado, lime, chili flakes, and microgreens.", cat: "Morning", badge: "VEGAN", inStock: true, img: "https://images.unsplash.com/photo-1588137378633-dea1336ce1e2?w=600&auto=format&fit=crop" },
      { id: "sw_6", name: "Pan con Chicharrón", price: 10.95, desc: "Crispy pork belly, fried sweet potato, and salsa criolla on fresh artisan bread.", cat: "Sandwich", badge: "POPULAR", inStock: true, img: "https://images.unsplash.com/photo-1509722747041-616f39b57569?w=600&auto=format&fit=crop" },
      { id: "sw_3", name: "Classic Omelette", price: 13.95, desc: "Folded golden omelette served with seasoned home fries and herb salad.", cat: "Morning", badge: "CHEF SPECIAL", inStock: true, img: "https://images.unsplash.com/photo-1510693206972-df098062cb71?w=600&auto=format&fit=crop" },
      { id: "p_1", name: "Peruvian Alfajores", price: 4.95, desc: "Handmade cornstarch shortbread cookies sandwiched with rich dulce de leche.", cat: "Bakery", badge: "HOMEMADE", inStock: true, img: "https://images.unsplash.com/photo-1558961363-fa8fdf82db35?w=600&auto=format&fit=crop" },
      { id: "c_4", name: "Cortado", price: 4.00, desc: "Equal parts rich espresso and silky warm milk in a black ceramic glass.", cat: "Coffee", badge: "SPECIALTY", inStock: true, img: "https://images.unsplash.com/photo-1514432324607-a09d9b4aefdd?w=600&auto=format&fit=crop" },
      { id: "p_2", name: "Almond Croissant", price: 5.50, desc: "Flaky twice-baked croissant filled with rich almond frangipane cream.", cat: "Bakery", badge: "FRESH DAILY", inStock: true, img: "https://images.unsplash.com/photo-1555507036-ab1f4038808a?w=600&auto=format&fit=crop" },
      { id: "sw_5", name: "Burrata Caprese", price: 14.95, desc: "Creamy fresh burrata with heirloom tomatoes, basil pesto, and aged balsamic reduction.", cat: "Sandwich", badge: "VEGETARIAN", inStock: true, img: "https://images.unsplash.com/photo-1592417817098-8f3d69109853?w=600&auto=format&fit=crop" },
      { id: "c_1", name: "Iced Caramel Macchiato", price: 5.75, desc: "Chilled espresso poured over creamy oat milk with vanilla syrup and house caramel drizzle.", cat: "Beverages", badge: "COLD BREW", inStock: true, img: "https://images.unsplash.com/photo-1517701604599-bb29b565090c?w=600&auto=format&fit=crop" }
    ],

    defaultSettings: {
      status: "OPEN",
      headline: "Fresh · Bright · Plant-Filled · Homemade · Daily",
      subheadline: "Artisan neighbourhood café & bakery with Italian influence in Tribeca, New York City.",
      morningWindow: "Morning Menu Served Daily 8:00 AM – 11:00 AM",
      announcementActive: false,
      announcement: "Special Morning Pastry Tasting this week in Tribeca!",
      phone: "+1 212-372-0297",
      address: "90 Chambers St Unit B, New York, NY 10007, United States",
      hours: "Monday – Friday: 7:30 AM – 6:00 PM\nSaturday – Sunday: 8:00 AM – 5:00 PM"
    },

    /**
     * Initializes Supabase connection.
     */
    init: function () {
      const storedUrl = localStorage.getItem('artesano_supabase_url') || window.ARTESANO_SUPABASE_URL;
      const storedKey = localStorage.getItem('artesano_supabase_anon_key') || window.ARTESANO_SUPABASE_ANON_KEY;

      if (storedUrl && storedKey && window.supabase && typeof window.supabase.createClient === 'function') {
        try {
          this.client = window.supabase.createClient(storedUrl, storedKey, {
            auth: {
              persistSession: true,
              autoRefreshToken: true,
              detectSessionInUrl: true
            }
          });
          this.isCloudActive = true;
          console.log('[Artesano] Connected to Supabase Cloud.');
        } catch (err) {
          console.warn('[Artesano] Failed to initialize Supabase client:', err);
          this.isCloudActive = false;
        }
      } else {
        this.isCloudActive = false;
      }
      return this.isCloudActive;
    },

    // --------------------------------------------------------------------------
    // AUTHENTICATION (SUPABASE AUTH)
    // --------------------------------------------------------------------------
    signIn: async function (email, password) {
      if (!this.isCloudActive || !this.client) {
        return { error: { message: 'Cloud database not configured. Please connect Supabase in Cloud Settings.' } };
      }
      return await this.client.auth.signInWithPassword({ email, password });
    },

    signOut: async function () {
      if (this.isCloudActive && this.client) {
        await this.client.auth.signOut();
      }
      sessionStorage.removeItem('admin_auth_token');
      sessionStorage.removeItem('admin_auth_time');
      return { success: true };
    },

    getSession: async function () {
      if (!this.isCloudActive || !this.client) {
        return { data: { session: null } };
      }
      return await this.client.auth.getSession();
    },

    onAuthStateChange: function (callback) {
      if (this.isCloudActive && this.client) {
        return this.client.auth.onAuthStateChange(callback);
      }
      return null;
    },

    // --------------------------------------------------------------------------
    // MENU DATA ACCESS (RLS PROTECTED)
    // --------------------------------------------------------------------------
    getMenu: async function () {
      if (this.isCloudActive && this.client) {
        try {
          const { data, error } = await this.client
            .from('menu_items')
            .select('*')
            .order('sort_order', { ascending: true });

          if (!error && Array.isArray(data) && data.length > 0) {
            const mapped = data.map(d => ({
              id: d.id,
              name: d.name,
              price: parseFloat(d.price),
              desc: d.description || '',
              cat: d.category || 'Morning',
              badge: d.badge || '',
              inStock: d.in_stock !== false,
              img: d.image_url
            }));
            localStorage.setItem('artesano_menu', JSON.stringify(mapped));
            return mapped;
          }
        } catch (e) {
          console.error('[Artesano] Error reading cloud menu, falling back to local cache:', e);
        }
      }

      // Local fallback
      try {
        const local = localStorage.getItem('artesano_menu');
        return local ? JSON.parse(local) : this.defaultMenu;
      } catch (e) {
        return this.defaultMenu;
      }
    },

    saveMenuItem: async function (dish) {
      // Local cache update
      let menu = await this.getMenu();
      const existingIdx = menu.findIndex(d => d.id === dish.id);
      if (existingIdx >= 0) {
        menu[existingIdx] = dish;
      } else {
        menu.unshift(dish);
      }
      localStorage.setItem('artesano_menu', JSON.stringify(menu));

      // Cloud persistence
      if (this.isCloudActive && this.client) {
        const payload = {
          id: dish.id,
          name: dish.name,
          price: dish.price,
          description: dish.desc,
          category: dish.cat,
          badge: dish.badge,
          in_stock: dish.inStock,
          image_url: dish.img,
          updated_at: new Date().toISOString()
        };
        const { error } = await this.client.from('menu_items').upsert(payload);
        if (error) throw error;
      }
      return dish;
    },

    deleteMenuItem: async function (dishId) {
      let menu = await this.getMenu();
      menu = menu.filter(d => d.id !== dishId);
      localStorage.setItem('artesano_menu', JSON.stringify(menu));

      if (this.isCloudActive && this.client) {
        const { error } = await this.client.from('menu_items').delete().eq('id', dishId);
        if (error) throw error;
      }
      return true;
    },

    // --------------------------------------------------------------------------
    // STORE SETTINGS & BRANDING
    // --------------------------------------------------------------------------
    getSettings: async function () {
      if (this.isCloudActive && this.client) {
        try {
          const { data, error } = await this.client
            .from('store_settings')
            .select('*')
            .eq('id', 'default')
            .single();

          if (!error && data) {
            const mapped = {
              status: data.status,
              headline: data.headline,
              subheadline: data.subheadline,
              morningWindow: data.morning_window,
              announcementActive: data.announcement_active,
              announcement: data.announcement,
              phone: data.phone,
              address: data.address,
              hours: data.hours
            };
            localStorage.setItem('artesano_settings', JSON.stringify(mapped));
            return mapped;
          }
        } catch (e) {
          console.warn('[Artesano] Error loading cloud settings, falling back to local cache:', e);
        }
      }

      try {
        const local = localStorage.getItem('artesano_settings');
        return local ? JSON.parse(local) : this.defaultSettings;
      } catch (e) {
        return this.defaultSettings;
      }
    },

    saveSettings: async function (settings) {
      localStorage.setItem('artesano_settings', JSON.stringify(settings));

      if (this.isCloudActive && this.client) {
        const payload = {
          id: 'default',
          headline: settings.headline,
          subheadline: settings.subheadline,
          morning_window: settings.morningWindow,
          announcement_active: settings.announcementActive,
          announcement: settings.announcement,
          phone: settings.phone,
          address: settings.address,
          hours: settings.hours,
          status: settings.status,
          updated_at: new Date().toISOString()
        };
        const { error } = await this.client.from('store_settings').upsert(payload);
        if (error) throw error;
      }
      return settings;
    },

    // --------------------------------------------------------------------------
    // CUSTOMER ORDERS (CLOUD PERSISTENCE & SECURE RLS SUBMISSION)
    // --------------------------------------------------------------------------
    submitOrder: async function (orderData) {
      // Zero-trust client-side validation
      if (!orderData.customer_name || orderData.customer_name.trim().length < 2) {
        throw new Error('Please enter a valid guest name.');
      }
      if (!orderData.items_summary) {
        throw new Error('Order cart cannot be empty.');
      }

      const orderPayload = {
        id: 'ORD-' + Math.floor(100 + Math.random() * 900),
        customer_name: Security.escapeHtml(orderData.customer_name.trim()),
        service_mode: Security.escapeHtml(orderData.service_mode || 'Takeaway / Counter Pickup'),
        items_summary: Security.escapeHtml(orderData.items_summary),
        subtotal: parseFloat(orderData.subtotal || 0),
        tax: parseFloat(orderData.tax || 0),
        total: parseFloat(orderData.total || 0),
        status: 'RECEIVED'
      };

      if (this.isCloudActive && this.client) {
        const { data, error } = await this.client.from('orders').insert([orderPayload]).select();
        if (error) throw error;
        return { ...orderPayload, isCloud: true };
      } else {
        // Local preview fallback
        const orders = JSON.parse(localStorage.getItem('artesano_orders') || '[]');
        orders.unshift({
          ...orderPayload,
          customer: orderPayload.customer_name,
          mode: orderPayload.service_mode,
          items: orderPayload.items_summary,
          time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
        });
        localStorage.setItem('artesano_orders', JSON.stringify(orders));
        return { ...orderPayload, isCloud: false };
      }
    },

    getOrders: async function () {
      if (this.isCloudActive && this.client) {
        const { data, error } = await this.client
          .from('orders')
          .select('*')
          .order('created_at', { ascending: false });

        if (!error && Array.isArray(data)) {
          return data.map(o => ({
            id: o.id,
            customer: o.customer_name,
            mode: o.service_mode,
            items: o.items_summary,
            total: parseFloat(o.total).toFixed(2),
            status: o.status,
            time: new Date(o.created_at).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
            createdAt: o.created_at
          }));
        }
      }

      return JSON.parse(localStorage.getItem('artesano_orders') || '[]');
    },

    updateOrderStatus: async function (orderId, newStatus) {
      if (this.isCloudActive && this.client) {
        const { error } = await this.client
          .from('orders')
          .update({ status: newStatus, updated_at: new Date().toISOString() })
          .eq('id', orderId);
        if (error) throw error;
      }

      // Update local storage representation
      const orders = JSON.parse(localStorage.getItem('artesano_orders') || '[]');
      const ord = orders.find(o => o.id === orderId);
      if (ord) {
        ord.status = newStatus;
        localStorage.setItem('artesano_orders', JSON.stringify(orders));
      }
      return true;
    },

    // --------------------------------------------------------------------------
    // TABLE RESERVATIONS
    // --------------------------------------------------------------------------
    submitReservation: async function (resData) {
      if (!resData.name || resData.name.trim().length < 2) {
        throw new Error('Please enter a valid guest name.');
      }
      if (!resData.phone || resData.phone.trim().length < 7) {
        throw new Error('Please enter a valid contact phone number.');
      }
      if (!resData.date) {
        throw new Error('Please choose a valid reservation date.');
      }
      const todayStr = new Date().toISOString().split('T')[0];
      if (resData.date < todayStr) {
        throw new Error('Reservation date cannot be in the past.');
      }

      const resPayload = {
        id: 'RES-' + Math.floor(100 + Math.random() * 900),
        name: Security.escapeHtml(resData.name.trim()),
        phone: Security.escapeHtml(resData.phone.trim()),
        date: resData.date,
        time: resData.time || '10:00 AM',
        guests: Math.min(20, Math.max(1, parseInt(resData.guests, 10) || 2)),
        notes: Security.escapeHtml((resData.notes || '').trim()),
        status: 'PENDING'
      };

      if (this.isCloudActive && this.client) {
        const { error } = await this.client.from('reservations').insert([resPayload]);
        if (error) throw error;
        return { ...resPayload, isCloud: true };
      } else {
        const reservations = JSON.parse(localStorage.getItem('artesano_reservations') || '[]');
        reservations.unshift({
          ...resPayload,
          createdAt: new Date().toLocaleTimeString()
        });
        localStorage.setItem('artesano_reservations', JSON.stringify(reservations));
        return { ...resPayload, isCloud: false };
      }
    },

    getReservations: async function () {
      if (this.isCloudActive && this.client) {
        const { data, error } = await this.client
          .from('reservations')
          .select('*')
          .order('created_at', { ascending: false });

        if (!error && Array.isArray(data)) {
          return data.map(r => ({
            id: r.id,
            name: r.name,
            phone: r.phone,
            date: r.date,
            time: r.time,
            guests: r.guests,
            notes: r.notes,
            status: r.status,
            createdAt: new Date(r.created_at).toLocaleTimeString()
          }));
        }
      }

      return JSON.parse(localStorage.getItem('artesano_reservations') || '[]');
    },

    updateReservationStatus: async function (resId, newStatus) {
      if (this.isCloudActive && this.client) {
        const { error } = await this.client
          .from('reservations')
          .update({ status: newStatus, updated_at: new Date().toISOString() })
          .eq('id', resId);
        if (error) throw error;
      }

      const reservations = JSON.parse(localStorage.getItem('artesano_reservations') || '[]');
      const r = reservations.find(res => res.id === resId);
      if (r) {
        r.status = newStatus;
        localStorage.setItem('artesano_reservations', JSON.stringify(reservations));
      }
      return true;
    },

    // --------------------------------------------------------------------------
    // REALTIME LIVE NOTIFICATIONS (SUPABASE WEBSOCKETS)
    // --------------------------------------------------------------------------
    subscribeRealtime: function (onOrderChange, onReservationChange, onMenuChange) {
      if (!this.isCloudActive || !this.client) return null;

      try {
        const channel = this.client
          .channel('artesano_live_updates')
          .on('postgres_changes', { event: '*', schema: 'public', table: 'orders' }, payload => {
            console.log('[Artesano Realtime] Order event received:', payload);
            if (typeof onOrderChange === 'function') onOrderChange(payload);
          })
          .on('postgres_changes', { event: '*', schema: 'public', table: 'reservations' }, payload => {
            console.log('[Artesano Realtime] Reservation event received:', payload);
            if (typeof onReservationChange === 'function') onReservationChange(payload);
          })
          .on('postgres_changes', { event: '*', schema: 'public', table: 'menu_items' }, payload => {
            console.log('[Artesano Realtime] Menu event received:', payload);
            if (typeof onMenuChange === 'function') onMenuChange(payload);
          })
          .subscribe();

        return channel;
      } catch (e) {
        console.warn('[Artesano] Realtime subscription error:', e);
        return null;
      }
    }
  };

  // Expose to window
  window.ArtesanoSecurity = Security;
  window.ArtesanoBackend = ArtesanoBackend;

  // Auto-init in browser environments
  if (typeof document !== 'undefined') {
    document.addEventListener('DOMContentLoaded', () => {
      ArtesanoBackend.init();
    });
  }

})(window);
