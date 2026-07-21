// ==========================================================================
// PORTAL DE ESPACIOS CULTURALES DE AGUASCALIENTES - LOGIC
// Colectivo Vendaval IAJU
// ==========================================================================

// --- 1. Dataset Completo de Espacios Públicos Culturales ---
const spacesData = [
  // Municipio: Aguascalientes
  {
    id: 1,
    name: "Instituto Cultural de Aguascalientes",
    address: "Venustiano Carranza 101, Centro, 20000 Aguascalientes, Ags.",
    phone: "449 910 2010",
    type: "Casa de Cultura",
    municipio: "Aguascalientes",
    lat: 21.8804,
    lng: -102.2965,
    mapQuery: "Instituto+Cultural+de+Aguascalientes",
    hasWater: true,
    waterType: "Bebederos públicos de agua potable filtrada en patio central",
    hasAC: true,
    shadeLevel: "Alta",
    climateComfort: "Patio colonial arbolado con sombra densa, vegetación y bebederos de agua fresca."
  },
  {
    id: 2,
    name: "Centro Cultural Los Arquitos",
    address: "Alameda S/N, Barrio de la Purísima, 20000 Aguascalientes, Ags.",
    phone: "449 916 9201",
    type: "Casa de Cultura",
    municipio: "Aguascalientes",
    lat: 21.8845,
    lng: -102.2818,
    mapQuery: "Centro+Cultural+Los+Arquitos+Aguascalientes",
    hasWater: true,
    waterType: "Bebederos públicos e hidrantes en corredores",
    hasAC: false,
    shadeLevel: "Alta",
    climateComfort: "Antiguos baños térmicos con frondosos jardines, fuentes de agua y sombra arbolada."
  },
  {
    id: 3,
    name: "Casa Terán",
    address: "Rivero y Gutiérrez 110, Zona Centro, 20000 Aguascalientes, Ags.",
    phone: "449 915 1741",
    type: "Casa de Cultura",
    municipio: "Aguascalientes",
    lat: 21.8827,
    lng: -102.2974,
    mapQuery: "Casa+Teran+Aguascalientes",
    hasWater: true,
    waterType: "Dispensador de agua fría en patio principal",
    hasAC: true,
    shadeLevel: "Alta",
    climateComfort: "Casona histórica sombreada con librería, cafetería y dispensador de agua potable."
  },
  {
    id: 4,
    name: "Centro Cultural Ángel",
    address: "Rivero y Gutiérrez 110, Zona Centro, 20000 Aguascalientes, Ags.",
    phone: "449 915 1741",
    type: "Casa de Cultura",
    municipio: "Aguascalientes",
    lat: 21.8828,
    lng: -102.2975,
    mapQuery: "Centro+Cultural+Angel+Aguascalientes",
    hasWater: true,
    waterType: "Garrafón de agua potable para visitantes",
    hasAC: true,
    shadeLevel: "Media",
    climateComfort: "Instalaciones de muros gruesos protegidas del sol con aire acondicionado."
  },
  {
    id: 5,
    name: "Centro de Artes Visuales",
    address: "Venustiano Carranza 111, Zona Centro, 20000 Aguascalientes, Ags.",
    phone: "449 916 1456",
    type: "Centro Cultural",
    municipio: "Aguascalientes",
    lat: 21.8805,
    lng: -102.2970,
    mapQuery: "Centro+de+Artes+Visuales+Aguascalientes",
    hasWater: true,
    waterType: "Dispensador de agua en área de talleres",
    hasAC: true,
    shadeLevel: "Media",
    climateComfort: "Galerías climatizadas y talleres protegidos del calor directo."
  },
  {
    id: 6,
    name: "Artefacto / Espacio Experimental",
    address: "Av. Francisco I. Madero 457, Zona Centro, 20000 Aguascalientes, Ags.",
    phone: "449 125 6259",
    type: "Centro Cultural",
    municipio: "Aguascalientes",
    lat: 21.8831,
    lng: -102.2928,
    mapQuery: "Artefacto+Espacio+Experimental+Aguascalientes",
    hasWater: true,
    waterType: "Dispensador de agua para asistentes",
    hasAC: false,
    shadeLevel: "Media",
    climateComfort: "Foro independiente con ventilación cruzada y refresco de agua."
  },
  {
    id: 7,
    name: "Casa Refugio Reyes",
    address: "C. Juan de Montoro 423, Zona Centro, 20000 Aguascalientes, Ags.",
    phone: "449 929 4201",
    type: "Centro Cultural",
    municipio: "Aguascalientes",
    lat: 21.8833,
    lng: -102.2920,
    mapQuery: "Casa+Refugio+Reyes+Aguascalientes",
    hasWater: true,
    waterType: "Bebedero en patio interior",
    hasAC: true,
    shadeLevel: "Alta",
    climateComfort: "Casona de cantera diseñada por Refugio Reyes con patio sombreado y fresco."
  },
  {
    id: 8,
    name: 'Galería de Arte "El Obraje"',
    address: "C. Juan de Montoro 222, Centro, 20000 Aguascalientes, Ags.",
    phone: "449 994 0074",
    type: "Galería de Arte",
    municipio: "Aguascalientes",
    lat: 21.8833,
    lng: -102.2950,
    mapQuery: "Galeria+de+Arte+El+Obraje+Aguascalientes",
    hasWater: true,
    waterType: "Dispensador de agua purificada",
    hasAC: true,
    shadeLevel: "Media",
    climateComfort: "Galería con aire acondicionado y resguardo térmico en el centro."
  },
  {
    id: 9,
    name: "Teatro Morelos",
    address: "Nieto 113, Zona Centro, 20000 Aguascalientes, Ags.",
    phone: "449 915 1941",
    type: "Teatro",
    municipio: "Aguascalientes",
    lat: 21.8806,
    lng: -102.2961,
    mapQuery: "Teatro+Morelos+Aguascalientes",
    hasWater: true,
    waterType: "Dispensadores de agua potable en vestíbulo",
    hasAC: true,
    shadeLevel: "Media",
    climateComfort: "Teatro histórico con aire acondicionado central (HVAC) en todas las salas."
  },
  {
    id: 10,
    name: "Teatro Antonio Leal y Romero",
    address: "Venustiano Carranza, Zona Centro, 20000 Aguascalientes, Ags.",
    phone: "449 910 2010",
    type: "Teatro",
    municipio: "Aguascalientes",
    lat: 21.8804,
    lng: -102.2964,
    mapQuery: "Teatro+Antonio+Leal+y+Romero+Aguascalientes",
    hasWater: true,
    waterType: "Acceso a bebederos del complejo cultural",
    hasAC: true,
    shadeLevel: "Alta",
    climateComfort: "Sala de teatro completamente climatizada y aislada del sol directo."
  },
  {
    id: 11,
    name: "Galería Benjamín Manzo",
    address: "Venustiano Carranza 101, Zona Centro, 20000 Aguascalientes, Ags.",
    phone: "449 915 1691",
    type: "Galería de Arte",
    municipio: "Aguascalientes",
    lat: 21.8804,
    lng: -102.2965,
    mapQuery: "Galeria+Benjamin+Manzo+Aguascalientes",
    hasWater: true,
    waterType: "Dispensador de agua en sala",
    hasAC: true,
    shadeLevel: "Alta",
    climateComfort: "Galería interior con control de clima y sombra del claustro."
  },
  {
    id: 12,
    name: "Teatro Aguascalientes",
    address: "Av. José María Chávez S/N, Jardines de la Asunción, 20270 Aguascalientes, Ags.",
    phone: "449 978 5414",
    type: "Teatro",
    municipio: "Aguascalientes",
    lat: 21.8601,
    lng: -102.2905,
    mapQuery: "Teatro+Aguascalientes",
    hasWater: true,
    waterType: "Estaciones de hidratación en lobby principal",
    hasAC: true,
    shadeLevel: "Alta",
    climateComfort: "Recinto de gran escala con climatización central integral y amplia explanada sombreada."
  },
  {
    id: 13,
    name: "Centro CRECER Obraje",
    address: "Prof. Enrique Olivares Santana 119, Obraje, 20230 Aguascalientes, Ags.",
    phone: "449 910 2121 ext. 4255",
    type: "Centro Comunitario",
    municipio: "Aguascalientes",
    lat: 21.8745,
    lng: -102.2910,
    mapQuery: "Centro+CRECER+Obraje+Aguascalientes",
    hasWater: true,
    waterType: "Bebedero público comunitario",
    hasAC: false,
    shadeLevel: "Alta",
    climateComfort: "Centro social comunal con patio arbolado y toma de agua potable."
  },
  // Municipio: San José de Gracia
  {
    id: 14,
    name: "Casa de la Cultura de San José de Gracia (ICA)",
    address: "Juan Domínguez 214, Col. Las Cañadas, 20500 San José de Gracia, Ags.",
    phone: "465 967 3436",
    type: "Casa de Cultura",
    municipio: "San José de Gracia",
    lat: 22.1506,
    lng: -102.4158,
    mapQuery: "Casa+de+la+Cultura+San+Jose+de+Gracia+Aguascalientes",
    hasWater: true,
    waterType: "Bebedero escolar y público",
    hasAC: false,
    shadeLevel: "Alta",
    climateComfort: "Patio sombreado cerca de la presa con brisa natural y agua fresca."
  },
  {
    id: 15,
    name: "Casa del Peregrino",
    address: "San José de Gracia, Ags.",
    phone: "No disponible",
    type: "Casa de Cultura",
    municipio: "San José de Gracia",
    lat: 22.1520,
    lng: -102.4160,
    mapQuery: "Casa+del+Peregrino+San+Jose+de+Gracia+Aguascalientes",
    hasWater: true,
    waterType: "Toma de agua potable para peregrinos",
    hasAC: false,
    shadeLevel: "Media",
    climateComfort: "Refugio techado para visitantes del Cristo Redentor."
  },
  // Municipio: San Francisco de los Romo
  {
    id: 16,
    name: "Casa de Cultura de San Francisco de los Romo",
    address: "Juárez 410, Centro, 20300 San Francisco de los Romo, Ags.",
    phone: "465 967 0427",
    type: "Casa de Cultura",
    municipio: "San Francisco de los Romo",
    lat: 22.0738,
    lng: -102.2710,
    mapQuery: "Casa+de+Cultura+San+Francisco+de+los+Romo",
    hasWater: true,
    waterType: "Bebedero de agua purificada",
    hasAC: false,
    shadeLevel: "Alta",
    climateComfort: "Espacio cultural con áreas verdes y punto de agua potable."
  },
  // Municipio: Rincón de Romos
  {
    id: 17,
    name: "Casa de la Cultura de Rincón de Romos",
    address: "Dolores Hidalgo 309, Centro Histórico, 20400 Rincón de Romos, Ags.",
    phone: "465 951 1482",
    type: "Casa de Cultura",
    municipio: "Rincón de Romos",
    lat: 22.2289,
    lng: -102.3241,
    mapQuery: "Casa+de+la+Cultura+Rincon+de+Romos",
    hasWater: true,
    waterType: "Dispensador de agua fría en recepción",
    hasAC: false,
    shadeLevel: "Alta",
    climateComfort: "Patio histórico con sombra de frondosos árboles y bebedero."
  },
  // Municipio: Jesús María
  {
    id: 18,
    name: "Casa de la Cultura Jesús María",
    address: "Ignacio Allende 707, Ayuntamiento, 20920 Jesús María, Ags.",
    phone: "449 965 0017",
    type: "Casa de Cultura",
    municipio: "Jesús María",
    lat: 21.9612,
    lng: -102.3435,
    mapQuery: "Casa+de+la+Cultura+Jesus+Maria+Aguascalientes",
    hasWater: true,
    waterType: "Dispensador de agua para estudiantes y visitantes",
    hasAC: true,
    shadeLevel: "Alta",
    climateComfort: "Aulas climatizadas y patio sombreado en la zona centro de Jesús María."
  },
  // Municipio: Calvillo
  {
    id: 19,
    name: "Casa de Cultura de Calvillo",
    address: "C. Zaragoza, Zona Centro, 20800 Calvillo, Ags.",
    phone: "495 956 0807",
    type: "Casa de Cultura",
    municipio: "Calvillo",
    lat: 21.8465,
    lng: -102.7188,
    mapQuery: "Casa+de+Cultura+Calvillo+Aguascalientes",
    hasWater: true,
    waterType: "Bebedero de agua fresca en patio",
    hasAC: false,
    shadeLevel: "Alta",
    climateComfort: "Casona colonial sombreada en Calvillo con fuente y agua fresca."
  },
  // Municipio: Pabellón de Arteaga
  {
    id: 20,
    name: "Casa de la Cultura de Pabellón de Arteaga",
    address: "Centro, Pabellón de Arteaga, Ags.",
    phone: "No disponible",
    type: "Casa de Cultura",
    municipio: "Pabellón de Arteaga",
    lat: 22.1415,
    lng: -102.2762,
    mapQuery: "Casa+de+la+Cultura+Pabellon+de+Arteaga+Aguascalientes",
    hasWater: true,
    waterType: "Bebedero de agua potable",
    hasAC: false,
    shadeLevel: "Alta",
    climateComfort: "Patio con vegetación, bebedero y sombra constante."
  },
  // Municipio: Aguascalientes (Nuevos espacios de la lista de Google Maps)
  {
    id: 21,
    name: "Biblioteca Pública Enrique Fernández Ledesma",
    address: "Calle Galeana Norte 202, Zona Centro, 20000 Aguascalientes, Ags.",
    phone: "449 910 2010",
    type: "Centro Cultural",
    municipio: "Aguascalientes",
    lat: 21.8807271,
    lng: -102.2977607,
    mapQuery: "Biblioteca+Enrique+Fernandez+Ledesma+Aguascalientes",
    hasWater: true,
    waterType: "Dispensador de agua fría para lectores",
    hasAC: true,
    shadeLevel: "Alta",
    climateComfort: "Salas de lectura climatizadas frente al Jardín de San Marcos."
  },
  {
    id: 22,
    name: "Biblioteca Jaime Torres Bodet",
    address: "Andador Benito Juárez No. 122, Zona Centro, 20000 Aguascalientes, Ags.",
    phone: "449 910 2010",
    type: "Centro Cultural",
    municipio: "Aguascalientes",
    lat: 21.8823929,
    lng: -102.2960414,
    mapQuery: "Biblioteca+Jaime+Torres+Bodet+Aguascalientes",
    hasWater: true,
    waterType: "Dispensador de agua purificada en entrada",
    hasAC: true,
    shadeLevel: "Media",
    climateComfort: "Ambiente climatizado silencioso con punto de hidratación en andador Juárez."
  },
  {
    id: 23,
    name: "Biblioteca Pública Central Centenario-Bicentenario",
    address: "Av. Manuel Gómez Morín S/N, Col. Ferronales, 20180 Aguascalientes, Ags.",
    phone: "449 910 2645",
    type: "Centro Cultural",
    municipio: "Aguascalientes",
    lat: 21.888821,
    lng: -102.279617,
    mapQuery: "Biblioteca+Centenario+Bicentenario+Aguascalientes",
    hasWater: true,
    waterType: "Estaciones de hidratación con filtro de carbono",
    hasAC: true,
    shadeLevel: "Alta",
    climateComfort: "Moderna biblioteca automatizada con climatización integral y agua purificada."
  },
  {
    id: 24,
    name: "Sala Alternativa",
    address: "Edén 205, Aguascalientes, Ags.",
    phone: "449 960 2333",
    type: "Teatro",
    municipio: "Aguascalientes",
    lat: 21.8983049,
    lng: -102.3178328,
    mapQuery: "Sala+Alternativa+Cine+Aguascalientes",
    hasWater: true,
    waterType: "Dispensador de agua en ambigú",
    hasAC: true,
    shadeLevel: "Media",
    climateComfort: "Sala de proyección y teatro climatizada con confort térmico interior."
  },
  {
    id: 25,
    name: "Aquelarre, espacio cultural",
    address: "Calle Francisco G. Hornedo No. 336, Zona Centro, 20000 Aguascalientes, Ags.",
    phone: "449 190 0616",
    type: "Centro Cultural",
    municipio: "Aguascalientes",
    lat: 21.8800881,
    lng: -102.2924548,
    mapQuery: "Aquelarre+espacio+cultural+Aguascalientes",
    hasWater: true,
    waterType: "Garrafón de agua filtrada de libre acceso",
    hasAC: false,
    shadeLevel: "Alta",
    climateComfort: "Foro independiente con patio sombreado e hidratación gratuita."
  },
  {
    id: 26,
    name: "Cafecheetoh",
    address: "Talamantes 213, Barrio de San Marcos, 20070 Aguascalientes, Ags.",
    phone: "No disponible",
    type: "Centro Cultural",
    municipio: "Aguascalientes",
    lat: 21.8804204,
    lng: -102.305894,
    mapQuery: "Cafecheetoh+Café+Cultural+Aguascalientes",
    hasWater: true,
    waterType: "Vaso de agua potable purificada de cortesía",
    hasAC: false,
    shadeLevel: "Media",
    climateComfort: "Café cultural arbolado en San Marcos con agua de cortesía para clientes y transeúntes."
  },
  {
    id: 27,
    name: "Hogar Espejo",
    address: "Gral. Francisco Villa 245, Zona Centro, 20000 Aguascalientes, Ags.",
    phone: "No disponible",
    type: "Centro Cultural",
    municipio: "Aguascalientes",
    lat: 21.8898675,
    lng: -102.2867643,
    mapQuery: "Hogar+Espejo+Centro+Cultural+Aguascalientes",
    hasWater: true,
    waterType: "Dispensador de agua purificada ambiental",
    hasAC: false,
    shadeLevel: "Alta",
    climateComfort: "Jardín sombreado comunitario con libre acceso a agua fresca."
  }
];

// Helper para atributos de confort e hidratación por espacio (respeta datos explícitos reales)
function normalizeSpaceComfort(space) {
  if (!space) return space;
  if (space.hasWater === undefined) {
    space.hasWater = true;
  }
  if (!space.waterType) {
    space.waterType = space.hasWater ? "Bebedero / Dispensador de Agua Potable" : "Sin punto público de agua";
  }
  if (space.hasAC === undefined) {
    space.hasAC = ["Teatro", "Galería de Arte", "Centro Cultural"].includes(space.type);
  }
  if (!space.shadeLevel) {
    space.shadeLevel = (space.type === "Casa de Cultura" || space.type === "Centro Comunitario") ? "Alta" : "Media";
  }
  if (!space.climateComfort) {
    space.climateComfort = space.hasWater 
      ? (space.hasAC ? "Punto de agua potable gratis y aire acondicionado." : "Acceso libre a agua potable y sombra natural.")
      : "Espacio techado adecuado para resguardo contra clima severo.";
  }
  return space;
}

// Normalizar lista estática base
spacesData.forEach(normalizeSpaceComfort);

// --- Supabase Client Initialization ---
const SUPABASE_URL = "https://ebjejctuuhvmjmevabjh.supabase.co";
const SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImViamVqY3R1dWh2bWptZXZhYmpoIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODMzMzg4NDksImV4cCI6MjA5ODkxNDg0OX0.MCVJau8KDz_xV1iwakVW4wpKD_MeMGan3aWY3cGE6g0";
let supabaseClient = null;
try {
  if (window.supabase) {
    supabaseClient = window.supabase.createClient(SUPABASE_URL, SUPABASE_KEY);
  }
} catch (err) {
  console.error("Failed to initialize Supabase client:", err);
}

// --- Load Custom Spaces from LocalStorage (as offline fallback) ---
let customSpaces = [];
try {
  const stored = localStorage.getItem("vendaval_custom_spaces");
  if (stored) {
    const parsed = JSON.parse(stored);
    if (Array.isArray(parsed)) {
      // Filter out invalid spaces to prevent map crashes
      customSpaces = parsed.filter(s => s && s.name && !isNaN(parseFloat(s.lat)) && !isNaN(parseFloat(s.lng)));
    }
  }
} catch (e) {
  console.error("Error reading custom spaces from localStorage", e);
}

let activeSpacesList = [...spacesData, ...customSpaces];

// --- Supabase Async Fetcher ---
async function loadSpacesFromSupabase() {
  if (!supabaseClient) return;
  try {
    const { data, error } = await supabaseClient
      .from("espacios_culturales")
      .select("*")
      .order("id", { ascending: true });
    
    if (error) throw error;
    
    if (data && data.length > 0) {
      // Merge: keep static spacesData as the base, add Supabase entries that are not duplicates
      const staticIds = new Set(spacesData.map(s => s.id));
      const supabaseExtras = data.filter(s => !staticIds.has(s.id));
      
      // Ensure all Supabase entries have mapQuery & comfort normalization
      supabaseExtras.forEach(s => {
        if (!s.mapQuery) {
          s.mapQuery = encodeURIComponent(s.name + " " + s.municipio + " Aguascalientes");
        }
        normalizeSpaceComfort(s);
      });
      
      activeSpacesList = [...spacesData, ...supabaseExtras];
      
      // Refresh user interface & map layers
      updateStatsTargets();
      filterData();
      if (map) {
        switchMapLayer(activeMapLayer);
        setTimeout(() => map.invalidateSize(), 200);
      }
    }
  } catch (err) {
    console.error("Error loading spaces from Supabase:", err);
  }
}

// --- Supabase Realtime Subscription ---
function subscribeToSpacesRealtime() {
  if (!supabaseClient) return;
  try {
    supabaseClient
      .channel('public:espacios_culturales')
      .on('postgres_changes', { event: 'INSERT', schema: 'public', table: 'espacios_culturales' }, payload => {
        console.log('Realtime insert received:', payload);
        const newSpace = payload.new;
        
        // Evitar duplicaciones
        const exists = activeSpacesList.some(s => s.id === newSpace.id);
        if (!exists) {
          if (!newSpace.mapQuery) {
            newSpace.mapQuery = encodeURIComponent(newSpace.name + " " + newSpace.municipio + " Aguascalientes");
          }
          normalizeSpaceComfort(newSpace);
          activeSpacesList.push(newSpace);
          updateStatsTargets();
          filterData();
          if (map) switchMapLayer(activeMapLayer);
          showSyncNotification(newSpace.name);
        }
      })
      .subscribe((status) => {
        console.log("Supabase Realtime subscription status:", status);
      });
  } catch (err) {
    console.error("Failed to set up Realtime subscription:", err);
  }
}

// --- Toast Sincronización Notificación ---
function showSyncNotification(name) {
  let container = document.getElementById("toast-container");
  if (!container) {
    container = document.createElement("div");
    container.id = "toast-container";
    container.style.position = "fixed";
    container.style.bottom = "24px";
    container.style.right = "24px";
    container.style.zIndex = "9999";
    container.style.display = "flex";
    container.style.flexDirection = "column";
    container.style.gap = "10px";
    container.style.pointerEvents = "none";
    document.body.appendChild(container);
  }
  
  const toast = document.createElement("div");
  toast.className = "sync-toast";
  toast.innerHTML = `
    <div class="sync-toast-icon">🔄</div>
    <div class="sync-toast-content">
      <div class="sync-toast-title">¡Espacio Sincronizado!</div>
      <div class="sync-toast-body"><strong>${name}</strong> se ha añadido al mapa.</div>
    </div>
  `;
  
  container.appendChild(toast);
  
  setTimeout(() => {
    toast.classList.add("fade-out");
    setTimeout(() => toast.remove(), 400);
  }, 4500);
}

// --- Google Maps Live Shared List Sync Engine ---
const OFFICIAL_GMAPS_LIST_URL = "https://www.google.com/maps/@22.0409642,-102.6739162,120170m/data=!3m1!1e3!4m2!11m1!2sGzNGWQCSTaKUCoHh621-3g?entry=ttu&g_ep=EgoyMDI2MDcxNS4wIKXMDSoASAFQAw%3D%3D";

async function syncGoogleMapsList(userInitiated = false) {
  const syncBtn1 = document.getElementById("syncGmapsBtn");
  const syncBtn2 = document.getElementById("syncGmapsBtnMap");
  
  if (syncBtn1 && userInitiated) syncBtn1.innerHTML = `⏳ Sincronizando...`;
  if (syncBtn2 && userInitiated) syncBtn2.innerHTML = `⏳ Sincronizando...`;

  try {
    const proxies = [
      `https://api.allorigins.win/raw?url=${encodeURIComponent(OFFICIAL_GMAPS_LIST_URL)}`,
      `https://corsproxy.io/?${encodeURIComponent(OFFICIAL_GMAPS_LIST_URL)}`
    ];

    let htmlText = "";
    for (const p of proxies) {
      try {
        const res = await fetch(p);
        if (res.ok) {
          htmlText = await res.text();
          if (htmlText && htmlText.length > 500) break;
        }
      } catch (e) {
        console.warn("Proxy attempt failed:", p, e);
      }
    }

    let addedCount = 0;

    if (htmlText) {
      const matches = htmlText.match(/\["([^"]+)",\[null,null,(-?\d+\.\d+),(-?\d+\.\d+)\]/g) || [];
      const extractedPlaces = [];

      matches.forEach(m => {
        const parts = m.match(/\["([^"]+)",\[null,null,(-?\d+\.\d+),(-?\d+\.\d+)\]/);
        if (parts && parts[1] && parts[2] && parts[3]) {
          extractedPlaces.push({
            name: parts[1],
            lat: parseFloat(parts[2]),
            lng: parseFloat(parts[3])
          });
        }
      });

      for (const place of extractedPlaces) {
        if (isNaN(place.lat) || isNaN(place.lng)) continue;
        
        const exists = activeSpacesList.some(s => 
          Math.abs(s.lat - place.lat) < 0.0005 && Math.abs(s.lng - place.lng) < 0.0005
        );

        if (!exists) {
          const newSpace = {
            name: place.name || "Nuevo Espacio Google Maps",
            address: "Aguascalientes",
            phone: "No disponible",
            type: "Centro Cultural",
            municipio: "Aguascalientes",
            lat: place.lat,
            lng: place.lng,
            mapQuery: encodeURIComponent(place.name + " Aguascalientes"),
            hasWater: true,
            hasAC: true,
            shadeLevel: "Alta",
            climateComfort: "Punto de confort térmico e hidratación importado en vivo desde Google Maps."
          };
          normalizeSpaceComfort(newSpace);

          if (supabaseClient) {
            try {
              const { data } = await supabaseClient.from("espacios_culturales").insert([newSpace]).select();
              if (data && data[0]) {
                activeSpacesList.push(data[0]);
              } else {
                newSpace.id = Date.now() + Math.floor(Math.random()*1000);
                activeSpacesList.push(newSpace);
              }
            } catch (err) {
              newSpace.id = Date.now() + Math.floor(Math.random()*1000);
              activeSpacesList.push(newSpace);
            }
          } else {
            newSpace.id = Date.now() + Math.floor(Math.random()*1000);
            activeSpacesList.push(newSpace);
          }

          addedCount++;
        }
      }
    }

    if (addedCount > 0) {
      updateStatsTargets();
      filterData();
      if (map) switchMapLayer(activeMapLayer);
    }

    if (userInitiated || addedCount > 0) {
      const msg = addedCount > 0 
        ? `🔄 ¡Sincronizado! ${addedCount} nueva(s) ubicación(es) de Google Maps importadas.`
        : `✅ Lista de Google Maps al día (${activeSpacesList.length} espacios activos).`;
      showSyncNotification(msg);
    }
  } catch (err) {
    console.error("Error syncing Google Maps list:", err);
  } finally {
    if (syncBtn1) syncBtn1.innerHTML = `🔄 Sincronizar Google Maps`;
    if (syncBtn2) syncBtn2.innerHTML = `🔄 Sincronizar en Vivo`;
  }
}

// --- 1.5 Clima, GPS y Motor de Recomendaciones Climáticas ---
let currentWeatherData = {
  temp: 26,
  feelsLike: 27,
  humidity: 40,
  uv: "7.2",
  wind: 12,
  description: "Cálido agradable",
  muniName: "Aguascalientes"
};

let userLocation = null; // { lat, lng }
let activeHeatMode = 'island'; // 'island' vs 'shade'
let activeWaterSubfilter = 'all'; // 'all', 'bebedero', 'fria', 'garrafon'
let waterRoutePolyline = null;

// Coordenadas centrales reales de los 11 municipios de Aguascalientes
const MUNICIPIOS_DATA = {
  "Aguascalientes": { lat: 21.8823, lng: -102.2978, weather: null },
  "Calvillo": { lat: 21.8464, lng: -102.7187, weather: null },
  "Jesús María": { lat: 21.9611, lng: -102.3433, weather: null },
  "Rincón de Romos": { lat: 22.2289, lng: -102.3231, weather: null },
  "San José de Gracia": { lat: 22.1528, lng: -102.4158, weather: null },
  "Pabellón de Arteaga": { lat: 22.1414, lng: -102.2764, weather: null },
  "San Francisco de los Romo": { lat: 22.0733, lng: -102.2703, weather: null },
  "Asientos": { lat: 22.2386, lng: -102.0889, weather: null },
  "Cosío": { lat: 22.3650, lng: -102.3000, weather: null },
  "El Llano": { lat: 21.9181, lng: -101.9647, weather: null },
  "Tepezalá": { lat: 22.2217, lng: -102.1706, weather: null }
};

// Fórmula de Haversine para distancia GPS exacta
function calculateDistance(lat1, lon1, lat2, lon2) {
  const R = 6371e3; // Metros
  const φ1 = lat1 * Math.PI / 180;
  const φ2 = lat2 * Math.PI / 180;
  const Δφ = (lat2 - lat1) * Math.PI / 180;
  const Δλ = (lon2 - lon1) * Math.PI / 180;

  const a = Math.sin(Δφ/2) * Math.sin(Δφ/2) +
            Math.cos(φ1) * Math.cos(φ2) *
            Math.sin(Δλ/2) * Math.sin(Δλ/2);
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

  return R * c;
}

function formatDistance(meters) {
  if (meters < 1000) return `${Math.round(meters)} m`;
  return `${(meters / 1000).toFixed(1)} km`;
}

// Clasificador del Índice UV
function getUVCategory(uvValue) {
  const val = parseFloat(uvValue) || 0;
  if (val < 3.0) {
    return { tag: "Bajo", class: "uv-low", tip: "Bajo riesgo solar." };
  } else if (val < 6.0) {
    return { tag: "Moderado", class: "uv-mod", tip: "Usa gafas y bloqueador." };
  } else if (val < 8.0) {
    return { tag: "Alto", class: "uv-high", tip: "Busca sombra y agua." };
  } else {
    return { tag: "Extremo", class: "uv-extreme", tip: "Protección solar obligatoria." };
  }
}

// Consulta en lote dinámica de API Open-Meteo para todos los municipios
async function fetchCurrentWeather(lat = 21.882, lng = -102.298, muniName = "Aguascalientes") {
  try {
    const mainUrl = `https://api.open-meteo.com/v1/forecast?latitude=${lat}&longitude=${lng}&current=temperature_2m,relative_humidity_2m,apparent_temperature,precipitation,weather_code,wind_speed_10m,uv_index&timezone=auto`;
    const res = await fetch(mainUrl);
    if (!res.ok) throw new Error("Status " + res.status);
    const data = await res.json();

    if (data && data.current) {
      const c = data.current;
      currentWeatherData = {
        temp: Math.round(c.temperature_2m),
        feelsLike: Math.round(c.apparent_temperature),
        humidity: Math.round(c.relative_humidity_2m),
        uv: c.uv_index !== undefined ? Number(c.uv_index).toFixed(1) : "6.5",
        wind: Math.round(c.wind_speed_10m),
        description: c.temperature_2m > 28 ? "Caluroso" : (c.temperature_2m < 15 ? "Fresco" : "Templado"),
        muniName: muniName
      };
      updateWeatherUI();
      updateComfortRecommendation();
    }

    await fetchMunicipiosWeather();
  } catch (err) {
    console.warn("Uso de datos clima locales por defecto:", err);
    currentWeatherData.muniName = muniName;
    updateWeatherUI();
    updateComfortRecommendation();
  }
}

async function fetchMunicipiosWeather() {
  const keys = Object.keys(MUNICIPIOS_DATA);
  const lats = keys.map(k => MUNICIPIOS_DATA[k].lat).join(",");
  const lngs = keys.map(k => MUNICIPIOS_DATA[k].lng).join(",");

  try {
    const multiUrl = `https://api.open-meteo.com/v1/forecast?latitude=${lats}&longitude=${lngs}&current=temperature_2m,relative_humidity_2m,apparent_temperature,wind_speed_10m,uv_index&timezone=auto`;
    const res = await fetch(multiUrl);
    if (!res.ok) throw new Error("Multi weather status " + res.status);
    const data = await res.json();

    const dataArray = Array.isArray(data) ? data : [data];
    dataArray.forEach((item, index) => {
      const name = keys[index];
      if (item && item.current && name) {
        const cur = item.current;
        MUNICIPIOS_DATA[name].weather = {
          temp: Math.round(cur.temperature_2m),
          feelsLike: Math.round(cur.apparent_temperature),
          humidity: Math.round(cur.relative_humidity_2m),
          uv: cur.uv_index !== undefined ? Number(cur.uv_index).toFixed(1) : "6.0",
          wind: Math.round(cur.wind_speed_10m)
        };
      }
    });
  } catch (err) {
    console.warn("Fallback dinámico para microclima municipal:", err);
    keys.forEach(name => {
      const baseTemp = currentWeatherData.temp;
      const diff = name === 'Calvillo' ? 2 : (name === 'San José de Gracia' ? -2 : (name === 'Cosío' ? -1 : 0));
      MUNICIPIOS_DATA[name].weather = {
        temp: baseTemp + diff,
        feelsLike: currentWeatherData.feelsLike + diff,
        humidity: currentWeatherData.humidity,
        uv: currentWeatherData.uv,
        wind: currentWeatherData.wind
      };
    });
  }
}

function updateWeatherUI() {
  const tempEl = document.getElementById("weatherTemp");
  const feelsEl = document.getElementById("weatherFeels");
  const humEl = document.getElementById("weatherHumidity");
  const humDescEl = document.getElementById("weatherHumidityDesc");
  const uvEl = document.getElementById("weatherUV");
  const uvBadgeEl = document.getElementById("weatherUVBadge");
  const windEl = document.getElementById("weatherWind");
  const airQualityEl = document.getElementById("weatherAirQuality");

  if (tempEl) tempEl.textContent = `${currentWeatherData.temp} °C`;
  if (feelsEl) feelsEl.textContent = `Sensación: ${currentWeatherData.feelsLike} °C`;
  
  if (humEl) humEl.textContent = `${currentWeatherData.humidity} %`;
  if (humDescEl) {
    const hum = currentWeatherData.humidity;
    humDescEl.textContent = hum < 30 ? "Ambiente Seco" : (hum > 65 ? "Ambiente Húmedo" : "Confort Agradable");
  }

  if (uvEl) uvEl.textContent = currentWeatherData.uv;
  if (uvBadgeEl) {
    const cat = getUVCategory(currentWeatherData.uv);
    uvBadgeEl.innerHTML = `<span class="uv-tag ${cat.class}">${cat.tag}</span>`;
  }

  if (windEl) windEl.textContent = `${currentWeatherData.wind} km/h`;
  if (airQualityEl) {
    airQualityEl.textContent = currentWeatherData.wind > 20 ? "Viento Moderado" : "Aire Limpio & Fresco";
  }
}

function updateComfortRecommendation(preferredService = null) {
  const recBadge = document.getElementById("climaRecBadge");
  const recTitle = document.getElementById("climaRecTitle");
  const recDesc = document.getElementById("climaRecDesc");
  if (!recTitle) return;

  const userLat = userLocation ? userLocation.lat : 21.882;
  const userLng = userLocation ? userLocation.lng : -102.298;

  const spacesWithDist = activeSpacesList.map(s => {
    normalizeSpaceComfort(s);
    const distMeters = calculateDistance(userLat, userLng, s.lat, s.lng);
    return { ...s, distMeters };
  }).sort((a, b) => a.distMeters - b.distMeters);

  if (spacesWithDist.length === 0) return;

  const temp = currentWeatherData.temp;
  let bestMatch = null;
  let titleText = "";
  let descText = "";

  if (preferredService === 'water') {
    bestMatch = spacesWithDist.find(s => s.hasWater) || spacesWithDist[0];
    titleText = `💧 Punto de Agua Gratis más Cercano a ${formatDistance(bestMatch.distMeters)}`;
    descText = `Te recomendamos acudir a <strong>${bestMatch.name}</strong> (${bestMatch.address}). ${bestMatch.waterType || "Cuenta con bebedero de agua potable gratuita abierta al público."}`;
  } else if (preferredService === 'shade' || temp >= 27) {
    bestMatch = spacesWithDist.find(s => s.hasAC || s.shadeLevel === 'Alta') || spacesWithDist[0];
    titleText = temp >= 27 
      ? `🔥 Clima Caluroso (${temp}°C) — Refugio Climatizado a ${formatDistance(bestMatch.distMeters)}`
      : `❄️ Espacio Fresco con Sombra a ${formatDistance(bestMatch.distMeters)}`;
    descText = `Te sugerimos resguardarte en <strong>${bestMatch.name}</strong>. ${bestMatch.climateComfort || "Excelente sombra y climatización de interiores."}`;
  } else if (preferredService === 'warm' || temp <= 16) {
    bestMatch = spacesWithDist.find(s => s.hasAC || s.type === 'Teatro' || s.type === 'Casa de Cultura') || spacesWithDist[0];
    titleText = `🌬️ Clima Fresco (${temp}°C) — Espacio Techado a ${formatDistance(bestMatch.distMeters)}`;
    descText = `Te recomendamos ingresar a <strong>${bestMatch.name}</strong>. Instalaciones acogedoras y protegidas del viento exterior.`;
  } else {
    bestMatch = spacesWithDist[0];
    titleText = `☀️ Clima Templado (${temp}°C) — Espacio Cultural Recomendado a ${formatDistance(bestMatch.distMeters)}`;
    descText = `Visita <strong>${bestMatch.name}</strong> (${bestMatch.municipio}). Condiciones ambientales óptimas para tu recorrido.`;
  }

  if (recBadge) recBadge.textContent = userLocation ? "📍 Recomendación GPS Activa" : "☀️ Alerta Climática Ciudadana";
  recTitle.innerHTML = titleText;
  recDesc.innerHTML = descText;
}

function activateGPSLocation() {
  const btn = document.getElementById("gpsLocateBtn");
  if (btn) btn.innerHTML = `⏳ Obteniendo GPS...`;

  if (!navigator.geolocation) {
    alert("Tu navegador o dispositivo no soporta geolocalización GPS.");
    if (btn) btn.innerHTML = `📍 Activar mi Ubicación GPS`;
    return;
  }

  navigator.geolocation.getCurrentPosition(
    (pos) => {
      userLocation = {
        lat: pos.coords.latitude,
        lng: pos.coords.longitude
      };

      if (btn) btn.innerHTML = `✅ GPS Activo`;
      fetchCurrentWeather(userLocation.lat, userLocation.lng);

      if (map) {
        map.setView([userLocation.lat, userLocation.lng], 14);

        if (window.userGPSMarker) {
          window.userGPSMarker.setLatLng([userLocation.lat, userLocation.lng]);
        } else {
          const userIcon = L.divIcon({
            className: "user-gps-marker",
            iconAnchor: [10, 10],
            html: `<span style="background:#00f2fe; width:20px; height:20px; border-radius:50%; display:block; border:3px solid #fff; box-shadow:0 0 15px #00f2fe;"></span>`
          });
          window.userGPSMarker = L.marker([userLocation.lat, userLocation.lng], { icon: userIcon })
            .bindPopup("<b>📍 Tu Ubicación Actual (GPS)</b>")
            .addTo(map);
        }
        window.userGPSMarker.openPopup();
      }

      updateComfortRecommendation();
    },
    (err) => {
      console.warn("GPS error:", err);
      alert("No pudimos obtener tu ubicación exacta. Usando centro de Aguascalientes.");
      if (btn) btn.innerHTML = `📍 Activar mi Ubicación GPS`;
    },
    { enableHighAccuracy: true, timeout: 8000 }
  );
}

// --- 2. Leaflet Map Initialization ---
let map;
let markersGroup;
let heatLayerGroup = null;
let waterLayerGroup = null;
let weatherLayerGroup = null;
let activeMapLayer = 'spaces';
const customMarkers = {};

function initMap() {
  try {
    const mapElement = document.getElementById("leafletMap");
    if (!mapElement) {
      console.error("Map element #leafletMap not found");
      return;
    }

    if (typeof L === 'undefined') {
      console.error("Leaflet library (L) is not loaded");
      return;
    }

    map = L.map("leafletMap", {
      scrollWheelZoom: false,
      zoomControl: true
    }).setView([22.0, -102.3], 9.5);

    L.tileLayer("https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png", {
      attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors &copy; <a href="https://carto.com/attributions">CARTO</a>',
      subdomains: "abcd",
      maxZoom: 20
    }).addTo(map);

    markersGroup = L.featureGroup().addTo(map);
    
    plotMapMarkers(activeSpacesList);
    initLayerSwitcherEvents();

    setTimeout(() => {
      if (map) map.invalidateSize();
    }, 300);
  } catch (err) {
    console.error("Error initializing map:", err);
  }
}

// Conmutador de Capas (Recintos, Calor, Agua, Clima)
function initLayerSwitcherEvents() {
  const layerChips = document.querySelectorAll(".layer-chip");
  layerChips.forEach(chip => {
    chip.addEventListener("click", () => {
      const layer = chip.getAttribute("data-layer");
      switchMapLayer(layer);
    });
  });
}

function switchMapLayer(layerName) {
  activeMapLayer = layerName;
  const chips = document.querySelectorAll(".layer-chip");
  chips.forEach(c => c.classList.toggle("active", c.getAttribute("data-layer") === layerName));

  // Toggle floating overlay panels
  const heatOverlay = document.getElementById("heatOverlayPanel");
  const waterOverlay = document.getElementById("waterOverlayPanel");
  const weatherOverlay = document.getElementById("weatherOverlayPanel");

  if (heatOverlay) heatOverlay.style.display = layerName === 'heat' ? 'block' : 'none';
  if (waterOverlay) waterOverlay.style.display = layerName === 'water' ? 'block' : 'none';
  if (weatherOverlay) weatherOverlay.style.display = layerName === 'weather' ? 'block' : 'none';

  if (!map) return;

  if (markersGroup) map.removeLayer(markersGroup);
  if (heatLayerGroup) map.removeLayer(heatLayerGroup);
  if (waterLayerGroup) map.removeLayer(waterLayerGroup);
  if (weatherLayerGroup) map.removeLayer(weatherLayerGroup);

  if (layerName === 'spaces') {
    markersGroup.addTo(map);
  } else if (layerName === 'heat') {
    buildHeatmapLayer();
    heatLayerGroup.addTo(map);
  } else if (layerName === 'water') {
    buildWaterMapLayer();
    waterLayerGroup.addTo(map);
  } else if (layerName === 'weather') {
    buildWeatherMapLayer();
    weatherLayerGroup.addTo(map);
  }
}

// Mapa de Calor dinámico con modos 'Isla de Calor' vs 'Confort & Sombra'
function buildHeatmapLayer() {
  if (heatLayerGroup && map) {
    map.removeLayer(heatLayerGroup);
  }

  heatLayerGroup = L.featureGroup();

  const isShadeMode = activeHeatMode === 'shade';
  const badgeEl = document.getElementById("heatModeBadge");
  if (badgeEl) {
    badgeEl.textContent = isShadeMode ? "🌳 Cobertura de Sombra" : "🔥 Isla de Calor Urbano";
  }

  // Visualización con círculos térmicos informativos para máxima interactividad
  activeSpacesList.forEach(s => {
    normalizeSpaceComfort(s);
    
    // Cálculo térmico dinámico en el punto exacto
    const tempBase = (MUNICIPIOS_DATA[s.municipio]?.weather?.temp) || currentWeatherData.temp || 26;
    const shadeOffset = s.shadeLevel === 'Alta' ? -3.5 : (s.shadeLevel === 'Media' ? -1.0 : +2.5);
    const acOffset = s.hasAC ? -1.5 : 0;
    const spotTemp = Math.round((tempBase + shadeOffset + acOffset) * 10) / 10;
    
    let color = "#3b82f6";
    let fillColor = "#60a5fa";
    let statusLabel = "Zona Fresca / Refugio";

    if (spotTemp >= 30) {
      color = "#ef4444";
      fillColor = "#f87171";
      statusLabel = "Isla de Calor Intenso";
    } else if (spotTemp >= 26) {
      color = "#f97316";
      fillColor = "#fb923c";
      statusLabel = "Temperatura Elevada";
    } else if (spotTemp >= 21) {
      color = "#10b981";
      fillColor = "#34d399";
      statusLabel = "Confort Térmico Óptimo";
    }

    if (isShadeMode) {
      // Invertir colores para priorizar zonas con mayor frescura y árboles
      if (s.shadeLevel === 'Alta') {
        color = "#00f2fe";
        fillColor = "#38bdf8";
        statusLabel = "Alta Sombra & Frescor";
      } else {
        color = "#64748b";
        fillColor = "#94a3b8";
        statusLabel = "Sombra Parcial";
      }
    }

    const circle = L.circle([s.lat, s.lng], {
      color: color,
      fillColor: fillColor,
      fillOpacity: 0.45,
      radius: isShadeMode ? (s.shadeLevel === 'Alta' ? 450 : 250) : 380
    });

    circle.bindPopup(`
      <div style="text-align:center; min-width:190px;">
        <div style="background:${color}; color:#fff; font-size:0.75rem; font-weight:700; padding:3px 8px; border-radius:50px; display:inline-block; margin-bottom:6px;">
          ${statusLabel}
        </div>
        <h4 style="margin-bottom:4px;">${s.name}</h4>
        <p style="font-size:0.8rem; color:#cbd5e1; margin-bottom:6px;">${s.address}</p>
        <div style="display:grid; grid-template-columns:1fr 1fr; gap:4px; font-size:0.78rem; background:rgba(255,255,255,0.06); padding:6px; border-radius:6px; margin-bottom:8px; text-align:left;">
          <div>🌡️ Temp en sitio: <strong>${spotTemp}°C</strong></div>
          <div>🌳 Sombra: <strong>${s.shadeLevel}</strong></div>
          <div>❄️ Climatización: <strong>${s.hasAC ? 'Sí (A/C)' : 'Natural'}</strong></div>
          <div>💧 Agua Gratis: <strong>${s.hasWater ? 'Sí' : 'No'}</strong></div>
        </div>
        <a href="#" class="leaflet-popup-link" onclick="window.showSpaceDetailsById(${s.id}); return false;">
          Ver Ficha Completa
        </a>
      </div>
    `);

    heatLayerGroup.addLayer(circle);
  });
}

// Mapa de Agua Potable dinámico con sub-filtros e iconos pulsantes
function buildWaterMapLayer() {
  if (waterLayerGroup && map) {
    map.removeLayer(waterLayerGroup);
  }

  waterLayerGroup = L.featureGroup();
  
  let waterSpaces = activeSpacesList.map(s => normalizeSpaceComfort(s)).filter(s => s.hasWater);

  // Aplicar sub-filtro de agua
  if (activeWaterSubfilter === 'bebedero') {
    waterSpaces = waterSpaces.filter(s => s.waterType.toLowerCase().includes('bebedero') || s.waterType.toLowerCase().includes('toma'));
  } else if (activeWaterSubfilter === 'fria') {
    waterSpaces = waterSpaces.filter(s => s.waterType.toLowerCase().includes('fría') || s.waterType.toLowerCase().includes('filtrad'));
  } else if (activeWaterSubfilter === 'garrafon') {
    waterSpaces = waterSpaces.filter(s => s.waterType.toLowerCase().includes('garraf') || s.waterType.toLowerCase().includes('dispensador'));
  }

  const countBadge = document.getElementById("waterStatsCount");
  if (countBadge) countBadge.textContent = `${waterSpaces.length} Puntos`;

  waterSpaces.forEach(space => {
    let iconEmoji = "💧";
    let iconBg = "#0284c7";
    let glowColor = "#38bdf8";

    if (space.waterType.toLowerCase().includes('bebedero')) {
      iconEmoji = "🚰";
      iconBg = "#0284c7";
    } else if (space.waterType.toLowerCase().includes('fría') || space.waterType.toLowerCase().includes('filtrad')) {
      iconEmoji = "🧊";
      iconBg = "#2563eb";
      glowColor = "#60a5fa";
    } else if (space.waterType.toLowerCase().includes('garraf')) {
      iconEmoji = "💧";
      iconBg = "#059669";
      glowColor = "#34d399";
    }

    const waterHtml = `
      <span class="water-pulse-icon" style="
        background-color: ${iconBg};
        color: #ffffff;
        width: 28px;
        height: 28px;
        display: flex;
        align-items: center;
        justify-content: center;
        border-radius: 50%;
        border: 2px solid #ffffff;
        box-shadow: 0 0 14px ${glowColor};
        font-size: 14px;
        cursor: pointer;
      ">${iconEmoji}</span>
    `;

    const icon = L.divIcon({
      className: "water-div-icon",
      iconAnchor: [14, 14],
      popupAnchor: [0, -14],
      html: waterHtml
    });

    const userLat = userLocation ? userLocation.lat : 21.882;
    const userLng = userLocation ? userLocation.lng : -102.298;
    const distMeters = calculateDistance(userLat, userLng, space.lat, space.lng);

    const marker = L.marker([space.lat, space.lng], { icon: icon });
    marker.bindPopup(`
      <div style="text-align:center; min-width:200px;">
        <div style="color:#38bdf8; font-size:0.75rem; font-weight:700; text-transform:uppercase; margin-bottom:4px;">
          ${iconEmoji} Punto de Hidratación Gratuita
        </div>
        <strong style="font-size:0.95rem; display:block;">${space.name}</strong>
        <p style="font-size:0.8rem; color:#94a3b8; margin-top:2px;">${space.address}</p>
        <div style="background:rgba(56,189,248,0.12); padding:6px 10px; border-radius:6px; font-size:0.78rem; color:#bae6fd; margin:8px 0; border:1px solid rgba(56,189,248,0.25);">
          ${space.waterType || "Bebedero / Dispensador Gratuito"}
        </div>
        <div style="font-size:0.75rem; color:#cbd5e1; margin-bottom:8px;">
          📍 Distancia aproximada: <strong>${formatDistance(distMeters)}</strong>
        </div>
        <div style="display:flex; gap:6px;">
          <button class="btn btn-secondary btn-sm" style="flex:1; font-size:0.72rem; padding:4px 8px;" onclick="window.traceRouteToWaterSpace(${space.id})">
            🚶‍♂️ Trazar Ruta
          </button>
          <button class="btn btn-glow btn-sm" style="flex:1; font-size:0.72rem; padding:4px 8px;" onclick="window.showSpaceDetailsById(${space.id})">
            Ficha
          </button>
        </div>
      </div>
    `);
    waterLayerGroup.addLayer(marker);
  });
}

// Trazar ruta visual al punto de agua potable más cercano
window.traceRouteToWaterSpace = function(spaceId) {
  const space = activeSpacesList.find(s => s.id === spaceId);
  if (!space || !map) return;

  const userLat = userLocation ? userLocation.lat : 21.882;
  const userLng = userLocation ? userLocation.lng : -102.298;

  if (waterRoutePolyline) {
    map.removeLayer(waterRoutePolyline);
  }

  const latlngs = [
    [userLat, userLng],
    [space.lat, space.lng]
  ];

  waterRoutePolyline = L.polyline(latlngs, {
    color: '#00f2fe',
    weight: 4,
    opacity: 0.85,
    dashArray: '8, 12'
  }).addTo(map);

  const bounds = L.latLngBounds(latlngs);
  map.fitBounds(bounds, { padding: [60, 60] });
};

// Función para encontrar automáticamente el punto de agua más cercano y trazar la ruta
function findNearestWaterPoints() {
  const userLat = userLocation ? userLocation.lat : 21.882;
  const userLng = userLocation ? userLocation.lng : -102.298;

  const waterSpaces = activeSpacesList.filter(s => s.hasWater).map(s => {
    const dist = calculateDistance(userLat, userLng, s.lat, s.lng);
    return { ...s, dist };
  }).sort((a, b) => a.dist - b.dist);

  if (waterSpaces.length > 0) {
    const closest = waterSpaces[0];
    switchMapLayer('water');
    window.traceRouteToWaterSpace(closest.id);
  }
}

// Mapa Climático dinámico con datos multi-municipio de Open-Meteo
function buildWeatherMapLayer() {
  if (weatherLayerGroup && map) {
    map.removeLayer(weatherLayerGroup);
  }

  weatherLayerGroup = L.featureGroup();

  Object.keys(MUNICIPIOS_DATA).forEach(muniName => {
    const info = MUNICIPIOS_DATA[muniName];
    const w = info.weather || {
      temp: currentWeatherData.temp,
      feelsLike: currentWeatherData.feelsLike,
      humidity: currentWeatherData.humidity,
      uv: currentWeatherData.uv,
      wind: currentWeatherData.wind
    };

    const weatherHtml = `
      <div style="
        background: rgba(15, 23, 42, 0.92);
        border: 1px solid #00f2fe;
        color: #ffffff;
        padding: 5px 12px;
        border-radius: 50px;
        font-size: 11px;
        font-weight: 700;
        box-shadow: 0 0 12px rgba(0, 242, 254, 0.5);
        display: flex;
        align-items: center;
        gap: 6px;
        white-space: nowrap;
        cursor: pointer;
      ">
        <span>🌤️</span>
        <span>${muniName}: ${w.temp}°C</span>
      </div>
    `;

    const icon = L.divIcon({
      className: "weather-div-icon",
      iconAnchor: [45, 14],
      popupAnchor: [0, -14],
      html: weatherHtml
    });

    const spacesInMuni = activeSpacesList.filter(s => s.municipio === muniName).length;

    const marker = L.marker([info.lat, info.lng], { icon: icon });
    marker.bindPopup(`
      <div style="text-align:center; min-width:200px;">
        <h4 style="color:#00f2fe; margin-bottom:6px;">🌤️ Estación Climática ${muniName}</h4>
        <div style="display:grid; grid-template-columns:1fr 1fr; gap:6px; font-size:0.8rem; text-align:left; background:rgba(255,255,255,0.05); padding:8px; border-radius:6px; margin-bottom:8px;">
          <div>🌡️ Temp: <strong>${w.temp}°C</strong></div>
          <div>🔥 Sensación: <strong>${w.feelsLike}°C</strong></div>
          <div>💧 Humedad: <strong>${w.humidity}%</strong></div>
          <div>☀️ UV: <strong>${w.uv}</strong></div>
        </div>
        <p style="font-size:0.75rem; color:#94a3b8; margin-bottom:8px;">${spacesInMuni} recintos registrados en este municipio.</p>
        <button class="btn btn-glow btn-sm" style="padding:4px 10px; font-size:0.75rem; width:100%;" onclick="window.filterByMunicipio('${muniName}')">
          Filtrar Recintos de ${muniName}
        </button>
      </div>
    `);
    weatherLayerGroup.addLayer(marker);
  });
}

// Generate map colors by space type
function getMarkerColor(type) {
  const colors = {
    "Casa de Cultura": "#f97316", // orange
    "Teatro": "#ec4899",         // pink
    "Centro Cultural": "#3b82f6", // blue
    "Galería de Arte": "#a855f7", // purple
    "Centro Comunitario": "#10b981" // green
  };
  return colors[type] || "#ffffff";
}

function plotMapMarkers(spaces) {
  // Clear existing markers
  if (markersGroup) {
    markersGroup.clearLayers();
  }

  // Filter out invalid coordinates to prevent Leaflet map crashes
  const validSpaces = spaces.filter(space => space && !isNaN(parseFloat(space.lat)) && !isNaN(parseFloat(space.lng)));

  validSpaces.forEach(space => {
    const markerHtmlStyles = `
      background-color: ${getMarkerColor(space.type)};
      width: 14px;
      height: 14px;
      display: block;
      left: -2px;
      top: -2px;
      position: relative;
      border-radius: 50px 50px 0;
      transform: rotate(-45deg);
      border: 1.5px solid #030712;
      box-shadow: 0 0 10px rgba(0,242,254,0.4);
    `;

    const icon = L.divIcon({
      className: "custom-div-icon",
      iconAnchor: [0, 14],
      popupAnchor: [7, -14],
      html: `<span style="${markerHtmlStyles}"></span>`
    });

    const marker = L.marker([space.lat, space.lng], { icon: icon });
    
    // Popup content
    const mapsLink = `https://www.google.com/maps/search/${space.mapQuery}`;
    const popupContent = `
      <div>
        <h4>${space.name}</h4>
        <p>${space.address}</p>
        <a href="#" class="leaflet-popup-link" onclick="window.showSpaceDetailsById(${space.id}); return false;">
          Ver Ficha Completa
          <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M5 12h14M12 5l7 7-7 7"/></svg>
        </a>
      </div>
    `;
    
    marker.bindPopup(popupContent);
    markersGroup.addLayer(marker);
    
    // Guardar referencia
    customMarkers[space.id] = marker;
  });

  // Fit bounds if markers exist
  if (validSpaces.length > 0 && map) {
    map.fitBounds(markersGroup.getBounds(), { padding: [30, 30] });
  }
}

// --- 3. Directory Rendering and Searching ---
const searchInput = document.getElementById("searchInput");
const clearSearchBtn = document.getElementById("clearSearch");
const typeChips = document.querySelectorAll("#typeChips .chip");
const municipioChips = document.querySelectorAll("#municipioChips .chip");
const spacesGrid = document.getElementById("spacesGrid");
const resultsCount = document.getElementById("resultsCount");
const emptyState = document.getElementById("emptyState");
const resetFiltersBtn = document.getElementById("resetFiltersBtn");

let activeFilters = {
  search: "",
  type: "all",
  municipio: "all",
  comfort: "all"
};

function getBadgeClass(type) {
  const badgeClasses = {
    "Casa de Cultura": "badge-casa",
    "Teatro": "badge-teatro",
    "Centro Cultural": "badge-centro",
    "Galería de Arte": "badge-galeria",
    "Centro Comunitario": "badge-comunitario"
  };
  return badgeClasses[type] || "badge-casa";
}

function renderDirectory(spaces) {
  if (!spacesGrid) return;
  spacesGrid.innerHTML = "";

  if (spaces.length === 0) {
    emptyState.style.display = "block";
    spacesGrid.style.display = "none";
    resultsCount.textContent = "0 espacios encontrados";
    return;
  }

  emptyState.style.display = "none";
  spacesGrid.style.display = "grid";
  resultsCount.textContent = `${spaces.length} de ${activeSpacesList.length} espacios disponibles`;

  spaces.forEach(space => {
    normalizeSpaceComfort(space);

    const card = document.createElement("div");
    card.className = "space-card";
    card.setAttribute("data-id", space.id);
    
    card.innerHTML = `
      <div>
        <div class="space-card-top">
          <span class="space-badge ${getBadgeClass(space.type)}">${space.type}</span>
          <span class="space-municipio">${space.municipio}</span>
        </div>
        <h3>${space.name}</h3>
        <p class="space-address">${space.address}</p>
        
        <div class="card-comfort-row">
          ${space.hasWater ? '<span class="badge-comfort badge-water">💧 Agua Gratis</span>' : ''}
          ${space.hasAC ? '<span class="badge-comfort badge-ac">❄️ A/C</span>' : ''}
          ${space.shadeLevel === 'Alta' ? '<span class="badge-comfort badge-shade">🌳 Sombra Alta</span>' : ''}
        </div>
      </div>
      <div class="space-card-footer">
        <span>Tel: ${space.phone}</span>
        <span class="space-link">
          Detalles 
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M5 12h14M12 5l7 7-7 7"/></svg>
        </span>
      </div>
    `;

    card.addEventListener("click", () => {
      openSpaceDrawer(space);
      
      if (map && customMarkers[space.id]) {
        map.setView([space.lat, space.lng], 14);
        customMarkers[space.id].openPopup();
        
        if (window.innerWidth < 1024) {
          document.getElementById("mapa-section").scrollIntoView({ behavior: "smooth" });
        }
      }
    });

    spacesGrid.appendChild(card);
  });
}

function filterData() {
  let filtered = activeSpacesList;

  // Filtrado por buscador
  if (activeFilters.search) {
    const term = activeFilters.search.toLowerCase();
    filtered = filtered.filter(space => 
      space.name.toLowerCase().includes(term) ||
      space.address.toLowerCase().includes(term) ||
      space.municipio.toLowerCase().includes(term)
    );
  }

  // Filtrado por tipo de recinto
  if (activeFilters.type !== "all") {
    filtered = filtered.filter(space => space.type === activeFilters.type);
  }

  // Filtrado por municipio
  if (activeFilters.municipio !== "all") {
    filtered = filtered.filter(space => space.municipio === activeFilters.municipio);
  }

  // Filtrado por servicios de confort e hidratación
  if (activeFilters.comfort !== "all") {
    if (activeFilters.comfort === "water") {
      filtered = filtered.filter(space => space.hasWater);
    } else if (activeFilters.comfort === "ac") {
      filtered = filtered.filter(space => space.hasAC);
    } else if (activeFilters.comfort === "shade") {
      filtered = filtered.filter(space => space.shadeLevel === "Alta");
    }
  }

  renderDirectory(filtered);
  plotMapMarkers(filtered);
}

// Bind search events
if (searchInput) {
  searchInput.addEventListener("input", (e) => {
    activeFilters.search = e.target.value;
    clearSearchBtn.style.display = e.target.value ? "block" : "none";
    filterData();
  });
}

if (clearSearchBtn) {
  clearSearchBtn.addEventListener("click", () => {
    searchInput.value = "";
    activeFilters.search = "";
    clearSearchBtn.style.display = "none";
    filterData();
  });
}

// Bind chip selection (type)
typeChips.forEach(chip => {
  chip.addEventListener("click", () => {
    typeChips.forEach(c => c.classList.remove("active"));
    chip.classList.add("active");
    activeFilters.type = chip.getAttribute("data-type");
    filterData();
  });
});

// Bind chip selection (municipio)
municipioChips.forEach(chip => {
  chip.addEventListener("click", () => {
    municipioChips.forEach(c => c.classList.remove("active"));
    chip.classList.add("active");
    activeFilters.municipio = chip.getAttribute("data-municipio");
    filterData();
  });
});

// Bind chip selection (comfort)
const comfortChips = document.querySelectorAll("#comfortChips .chip");
comfortChips.forEach(chip => {
  chip.addEventListener("click", () => {
    comfortChips.forEach(c => c.classList.remove("active"));
    chip.classList.add("active");
    activeFilters.comfort = chip.getAttribute("data-comfort");
    filterData();
  });
});

if (resetFiltersBtn) {
  resetFiltersBtn.addEventListener("click", () => {
    activeFilters.search = "";
    activeFilters.type = "all";
    activeFilters.municipio = "all";
    activeFilters.comfort = "all";
    if (searchInput) searchInput.value = "";
    if (clearSearchBtn) clearSearchBtn.style.display = "none";
    
    typeChips.forEach(c => c.classList.remove("active"));
    if (typeChips[0]) typeChips[0].classList.add("active");
    
    municipioChips.forEach(c => c.classList.remove("active"));
    if (municipioChips[0]) municipioChips[0].classList.add("active");

    comfortChips.forEach(c => c.classList.remove("active"));
    if (comfortChips[0]) comfortChips[0].classList.add("active");
    
    filterData();
  });
}

// --- 4. Interactive Detail Drawer (Ficha Informativa) ---
const drawerBackdrop = document.getElementById("drawerBackdrop");
const spaceDrawer = document.getElementById("spaceDrawer");
const drawerClose = document.getElementById("drawerClose");
const drawerBadge = document.getElementById("drawerBadge");
const drawerTitle = document.getElementById("drawerTitle");
const drawerAddress = document.getElementById("drawerAddress");
const drawerPhone = document.getElementById("drawerPhone");
const drawerMunicipio = document.getElementById("drawerMunicipio");
const drawerMapsLink = document.getElementById("drawerMapsLink");

function openSpaceDrawer(space) {
  if (!drawerBackdrop || !spaceDrawer) return;

  normalizeSpaceComfort(space);

  // Set Content
  drawerBadge.textContent = space.type;
  drawerBadge.className = `space-badge ${getBadgeClass(space.type)}`;
  drawerTitle.textContent = space.name;
  drawerAddress.textContent = space.address;
  drawerPhone.textContent = space.phone;
  drawerMunicipio.textContent = `${space.municipio} · Aguascalientes`;
  drawerMapsLink.href = `https://www.google.com/maps/search/${space.mapQuery}`;

  // Atributos climáticos en el Drawer
  const drawerWaterChip = document.getElementById("drawerWaterChip");
  const drawerAcChip = document.getElementById("drawerAcChip");
  const drawerShadeChip = document.getElementById("drawerShadeChip");
  const drawerComfortDesc = document.getElementById("drawerComfortDesc");

  if (drawerWaterChip) drawerWaterChip.style.display = space.hasWater ? "inline-flex" : "none";
  if (drawerAcChip) drawerAcChip.style.display = space.hasAC ? "inline-flex" : "none";
  if (drawerShadeChip) drawerShadeChip.textContent = `🌳 Sombra ${space.shadeLevel || 'Alta'}`;
  if (drawerComfortDesc) drawerComfortDesc.textContent = space.climateComfort || "Espacio preparado para confort climático y resguardo de la ciudadanía.";

  // Show
  drawerBackdrop.classList.add("active");
}

function closeSpaceDrawer() {
  if (drawerBackdrop) {
    drawerBackdrop.classList.remove("active");
  }
}

if (drawerClose) {
  drawerClose.addEventListener("click", closeSpaceDrawer);
}

if (drawerBackdrop) {
  drawerBackdrop.addEventListener("click", (e) => {
    if (e.target === drawerBackdrop) closeSpaceDrawer();
  });
}

// Global hook so Leaflet popup click calls it
window.showSpaceDetailsById = function(id) {
  const space = activeSpacesList.find(s => s.id === id);
  if (space) {
    openSpaceDrawer(space);
  }
};

window.filterByMunicipio = function(muniName) {
  activeFilters.municipio = muniName;
  const municipioChips = document.querySelectorAll("#municipioChips .chip");
  municipioChips.forEach(c => c.classList.toggle("active", c.getAttribute("data-municipio") === muniName));
  filterData();
  const dirSec = document.getElementById("espacios");
  if (dirSec) dirSec.scrollIntoView({ behavior: "smooth" });
};



// --- 6. Mobile Navigation Drawer ---
const navToggle = document.getElementById("navToggle");
const navLinksWrapper = document.querySelector(".nav-links-wrapper");

if (navToggle && navLinksWrapper) {
  navToggle.addEventListener("click", () => {
    navToggle.classList.toggle("active");
    navLinksWrapper.classList.toggle("active");
    
    // Evitar scroll en body al estar activo
    document.body.style.overflow = navLinksWrapper.classList.contains("active") ? "hidden" : "";
  });

  // Cerrar menú al hacer clic en enlace
  navLinksWrapper.querySelectorAll("a").forEach(link => {
    link.addEventListener("click", () => {
      navToggle.classList.remove("active");
      navLinksWrapper.classList.remove("active");
      document.body.style.overflow = "";
    });
  });
}

// --- 7. Navbar Scroll Effect ---
const navbar = document.getElementById("navbar");

window.addEventListener("scroll", () => {
  if (window.scrollY > 40) {
    navbar.classList.add("scrolled");
  } else {
    navbar.classList.remove("scrolled");
  }
});

// --- 8. Stats Counters Animation ---
function startStatsCounter() {
  const statsNumbers = document.querySelectorAll(".stat-inline-num, .stat-number");
  
  const animate = (el) => {
    const target = parseInt(el.getAttribute("data-target"), 10);
    const duration = 1200; // ms
    const startTime = performance.now();
    
    const update = (currentTime) => {
      const elapsed = currentTime - startTime;
      const progress = Math.min(elapsed / duration, 1);
      const ease = 1 - Math.pow(1 - progress, 4); // Cubic ease out
      
      el.textContent = Math.floor(ease * target);
      
      if (progress < 1) {
        requestAnimationFrame(update);
      } else {
        el.textContent = target;
      }
    };
    
    requestAnimationFrame(update);
  };

  // Intersection Observer to trigger when visible
  const observer = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
      if (entry.isIntersecting) {
        animate(entry.target);
        observer.unobserve(entry.target);
      }
    });
  }, { threshold: 0.5 });

  statsNumbers.forEach(num => observer.observe(num));
}

// --- Dynamic Stats Updater ---
function updateStatsTargets() {
  const totalSpaces = activeSpacesList.length;
  const totalMunicipios = new Set(activeSpacesList.map(s => s.municipio)).size;
  const totalTypes = new Set(activeSpacesList.map(s => s.type)).size;
  
  const spacesEl = document.querySelector('.stat-inline-box:nth-child(1) .stat-inline-num') || document.querySelector('[data-target="20"]');
  const muniEl = document.querySelector('.stat-inline-box:nth-child(2) .stat-inline-num') || document.querySelector('[data-target="7"]');
  const typesEl = document.querySelector('.stat-inline-box:nth-child(3) .stat-inline-num') || document.querySelector('[data-target="5"]');
  
  if (spacesEl) { spacesEl.setAttribute('data-target', totalSpaces); spacesEl.textContent = totalSpaces; }
  if (muniEl) { muniEl.setAttribute('data-target', totalMunicipios); muniEl.textContent = totalMunicipios; }
  if (typesEl) { typesEl.setAttribute('data-target', totalTypes); typesEl.textContent = totalTypes; }
}

// --- 9. Add Space Modal Logic ---
const openAddSpaceBtn = document.getElementById("openAddSpaceBtn");
const addSpaceModalOverlay = document.getElementById("addSpaceModalOverlay");
const closeAddSpaceModal = document.getElementById("closeAddSpaceModal");
const addSpaceForm = document.getElementById("addSpaceForm");

function openAddSpaceModalFunc() {
  if (addSpaceModalOverlay) {
    addSpaceModalOverlay.classList.add("active");
  }
}

function closeAddSpaceModalFunc() {
  if (addSpaceModalOverlay) {
    addSpaceModalOverlay.classList.remove("active");
    if (addSpaceForm) addSpaceForm.reset();
  }
}

if (openAddSpaceBtn) {
  openAddSpaceBtn.addEventListener("click", openAddSpaceModalFunc);
}
if (closeAddSpaceModal) {
  closeAddSpaceModal.addEventListener("click", closeAddSpaceModalFunc);
}
if (addSpaceModalOverlay) {
  addSpaceModalOverlay.addEventListener("click", (e) => {
    if (e.target === addSpaceModalOverlay) closeAddSpaceModalFunc();
  });
}

// --- Helper para extraer latitud y longitud automáticamente de URLs de Google Maps ---
function extractCoordsFromGmapsUrl(urlStr) {
  if (!urlStr || typeof urlStr !== 'string') return null;
  // Coordenadas estilo @lat,lng
  const atMatch = urlStr.match(/@(-?\d+\.\d+),(-?\d+\.\d+)/);
  if (atMatch) {
    return { lat: parseFloat(atMatch[1]), lng: parseFloat(atMatch[2]) };
  }
  // Coordenadas estilo q=lat,lng o ll=lat,lng
  const qMatch = urlStr.match(/[?&](?:q|ll)=(-?\d+\.\d+),(-?\d+\.\d+)/);
  if (qMatch) {
    return { lat: parseFloat(qMatch[1]), lng: parseFloat(qMatch[2]) };
  }
  // Coordenadas de inserción !3d(lat)!4d(lng)
  const dMatch = urlStr.match(/!3d(-?\d+\.\d+)!4d(-?\d+\.\d+)/);
  if (dMatch) {
    return { lat: parseFloat(dMatch[1]), lng: parseFloat(dMatch[2]) };
  }
  // Coordenadas simples en texto lat, lng
  const plainMatch = urlStr.match(/(-?\d+\.\d+)\s*,\s*(-?\d+\.\d+)/);
  if (plainMatch) {
    return { lat: parseFloat(plainMatch[1]), lng: parseFloat(plainMatch[2]) };
  }
  return null;
}

// Auto-detector de URL en el input del modal
const spaceGmapsUrlInput = document.getElementById("spaceGmapsUrlInput");
if (spaceGmapsUrlInput) {
  spaceGmapsUrlInput.addEventListener("input", (e) => {
    const parsed = extractCoordsFromGmapsUrl(e.target.value);
    if (parsed) {
      const latEl = document.getElementById("spaceLatInput");
      const lngEl = document.getElementById("spaceLngInput");
      if (latEl) latEl.value = parsed.lat;
      if (lngEl) lngEl.value = parsed.lng;
    }
  });
}

// Form Submission & Auto-saving
if (addSpaceForm) {
  addSpaceForm.addEventListener("submit", async (e) => {
    e.preventDefault();

    const name = document.getElementById("spaceNameInput").value.trim();
    const address = document.getElementById("spaceAddressInput").value.trim();
    const type = document.getElementById("spaceTypeInput").value;
    const municipio = document.getElementById("spaceMunicipioInput").value;
    const phone = document.getElementById("spacePhoneInput").value.trim() || "No disponible";
    const mapQueryInput = document.getElementById("spaceMapQueryInput").value.trim();
    const gmapsUrlVal = document.getElementById("spaceGmapsUrlInput") ? document.getElementById("spaceGmapsUrlInput").value.trim() : "";

    // Parse coordinates from URL if inputs are empty
    let parsedFromUrl = extractCoordsFromGmapsUrl(gmapsUrlVal);

    // Default coordinates center for municipios if empty
    const muniCoords = {
      "Aguascalientes": [21.882, -102.298],
      "San José de Gracia": [22.153, -102.416],
      "San Francisco de los Romo": [22.073, -102.270],
      "Rincón de Romos": [22.229, -102.323],
      "Jesús María": [21.961, -102.343],
      "Calvillo": [21.846, -102.718],
      "Pabellón de Arteaga": [22.141, -102.276]
    };

    const center = muniCoords[municipio] || [21.9, -102.3];
    const offsetLat = (Math.random() - 0.5) * 0.008;
    const offsetLng = (Math.random() - 0.5) * 0.008;

    const rawLat = parseFloat(document.getElementById("spaceLatInput").value);
    const rawLng = parseFloat(document.getElementById("spaceLngInput").value);

    let lat = !isNaN(rawLat) ? rawLat : (parsedFromUrl ? parsedFromUrl.lat : (center[0] + offsetLat));
    let lng = !isNaN(rawLng) ? rawLng : (parsedFromUrl ? parsedFromUrl.lng : (center[1] + offsetLng));

    const mapQuery = mapQueryInput || (gmapsUrlVal ? encodeURIComponent(gmapsUrlVal) : encodeURIComponent(name + " " + municipio + " Aguascalientes"));

    const hasWater = document.getElementById("spaceWaterInput") ? document.getElementById("spaceWaterInput").checked : true;
    const hasAC = document.getElementById("spaceAcInput") ? document.getElementById("spaceAcInput").checked : true;
    const shadeLevel = document.getElementById("spaceShadeInput") ? document.getElementById("spaceShadeInput").value : "Alta";

    const newSpace = {
      name: name,
      address: address,
      phone: phone,
      type: type,
      municipio: municipio,
      lat: lat,
      lng: lng,
      mapQuery: mapQuery,
      hasWater: hasWater,
      hasAC: hasAC,
      shadeLevel: shadeLevel,
      climateComfort: hasWater ? "Punto de agua potable gratis y área de resguardo." : "Espacio techado con climatización de interiores."
    };

    let savedSpace = null;

    if (supabaseClient) {
      try {
        const { data, error } = await supabaseClient
          .from("espacios_culturales")
          .insert([newSpace])
          .select();
        
        if (error) throw error;
        if (data && data[0]) {
          savedSpace = data[0];
          console.log("Successfully saved to Supabase:", savedSpace);
        }
      } catch (err) {
        console.error("Error saving new space to Supabase:", err);
      }
    }

    // Fallback to local storage if Supabase is offline or fails
    if (!savedSpace) {
      newSpace.id = Date.now();
      customSpaces.push(newSpace);
      try {
        localStorage.setItem("vendaval_custom_spaces", JSON.stringify(customSpaces));
      } catch (err) {
        console.error("Error saving new space to localStorage", err);
      }
      activeSpacesList = [...spacesData, ...customSpaces];
    } else {
      activeSpacesList.push(savedSpace);
    }

    // Reload interface & update map layers
    updateStatsTargets();
    filterData();
    if (map) switchMapLayer(activeMapLayer);
    showSyncNotification(name);

    // Close
    closeAddSpaceModalFunc();
  });
}

// --- 10. Initialization ---
document.addEventListener("DOMContentLoaded", () => {
  updateStatsTargets();
  initMap();
  renderDirectory(activeSpacesList);
  startStatsCounter();
  fetchCurrentWeather();

  // Bind GPS Locate Button
  const gpsLocateBtn = document.getElementById("gpsLocateBtn");
  if (gpsLocateBtn) gpsLocateBtn.addEventListener("click", activateGPSLocation);

  // Bind Quick Actions
  const btnFindWater = document.getElementById("btnFindWater");
  if (btnFindWater) {
    btnFindWater.addEventListener("click", () => {
      updateComfortRecommendation('water');
      switchMapLayer('water');
      document.getElementById("mapa-section")?.scrollIntoView({ behavior: 'smooth' });
    });
  }

  const btnFindShade = document.getElementById("btnFindShade");
  if (btnFindShade) {
    btnFindShade.addEventListener("click", () => {
      updateComfortRecommendation('shade');
      switchMapLayer('heat');
      document.getElementById("mapa-section")?.scrollIntoView({ behavior: 'smooth' });
    });
  }

  const btnFindWarm = document.getElementById("btnFindWarm");
  if (btnFindWarm) {
    btnFindWarm.addEventListener("click", () => {
      updateComfortRecommendation('warm');
      switchMapLayer('weather');
      document.getElementById("mapa-section")?.scrollIntoView({ behavior: 'smooth' });
    });
  }

  // Bind Municipality Weather Chips
  const muniChips = document.querySelectorAll("#muniWeatherChips .muni-chip");
  muniChips.forEach(chip => {
    chip.addEventListener("click", () => {
      muniChips.forEach(c => c.classList.remove("active"));
      chip.classList.add("active");
      const muni = chip.getAttribute("data-muni");
      const coords = MUNICIPIOS_DATA[muni];
      if (coords) {
        fetchCurrentWeather(coords.lat, coords.lng, muni);
        if (map) {
          map.setView([coords.lat, coords.lng], 12);
        }
      }
    });
  });

  // Bind Heat Map Overlay Mode Controls
  const btnHeatModeIsland = document.getElementById("btnHeatModeIsland");
  const btnHeatModeShade = document.getElementById("btnHeatModeShade");
  if (btnHeatModeIsland && btnHeatModeShade) {
    btnHeatModeIsland.addEventListener("click", () => {
      activeHeatMode = 'island';
      btnHeatModeIsland.classList.add("active");
      btnHeatModeShade.classList.remove("active");
      if (activeMapLayer === 'heat') buildHeatmapLayer();
    });

    btnHeatModeShade.addEventListener("click", () => {
      activeHeatMode = 'shade';
      btnHeatModeShade.classList.add("active");
      btnHeatModeIsland.classList.remove("active");
      if (activeMapLayer === 'heat') buildHeatmapLayer();
    });
  }

  // Bind Water Subfilters
  const waterSubchips = document.querySelectorAll("#waterSubfilters .water-chip");
  waterSubchips.forEach(chip => {
    chip.addEventListener("click", () => {
      waterSubchips.forEach(c => c.classList.remove("active"));
      chip.classList.add("active");
      activeWaterSubfilter = chip.getAttribute("data-watersub");
      if (activeMapLayer === 'water') buildWaterMapLayer();
    });
  });

  // Bind Nearest Water Route Button
  const btnFindNearestWater = document.getElementById("btnFindNearestWater");
  if (btnFindNearestWater) {
    btnFindNearestWater.addEventListener("click", () => {
      findNearestWaterPoints();
    });
  }

  // Bind Google Maps Sync Buttons
  const syncBtn1 = document.getElementById("syncGmapsBtn");
  if (syncBtn1) {
    syncBtn1.addEventListener("click", () => syncGoogleMapsList(true));
  }

  const syncBtn2 = document.getElementById("syncGmapsBtnMap");
  if (syncBtn2) {
    syncBtn2.addEventListener("click", () => syncGoogleMapsList(true));
  }

  // Periodic Auto-Sync every 30 seconds
  setInterval(() => {
    syncGoogleMapsList(false);
  }, 30000);

  if (supabaseClient) {
    loadSpacesFromSupabase();
    subscribeToSpacesRealtime();
  }
});
