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
    mapQuery: "Instituto+Cultural+de+Aguascalientes"
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
    mapQuery: "Centro+Cultural+Los+Arquitos+Aguascalientes"
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
    mapQuery: "Casa+Teran+Aguascalientes"
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
    mapQuery: "Centro+Cultural+Angel+Aguascalientes"
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
    mapQuery: "Centro+de+Artes+Visuales+Aguascalientes"
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
    mapQuery: "Artefacto+Espacio+Experimental+Aguascalientes"
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
    mapQuery: "Casa+Refugio+Reyes+Aguascalientes"
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
    mapQuery: "Galeria+de+Arte+El+Obraje+Aguascalientes"
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
    mapQuery: "Teatro+Morelos+Aguascalientes"
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
    mapQuery: "Teatro+Antonio+Leal+y+Romero+Aguascalientes"
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
    mapQuery: "Galeria+Benjamin+Manzo+Aguascalientes"
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
    mapQuery: "Teatro+Aguascalientes"
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
    mapQuery: "Centro+CRECER+Obraje+Aguascalientes"
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
    mapQuery: "Casa+de+la+Cultura+San+Jose+de+Gracia+Aguascalientes"
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
    mapQuery: "Casa+del+Peregrino+San+Jose+de+Gracia+Aguascalientes"
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
    mapQuery: "Casa+de+Cultura+San+Francisco+de+los+Romo"
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
    mapQuery: "Casa+de+la+Cultura+Rincon+de+Romos"
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
    mapQuery: "Casa+de+la+Cultura+Jesus+Maria+Aguascalientes"
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
    mapQuery: "Casa+de+Cultura+Calvillo+Aguascalientes"
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
    mapQuery: "Casa+de+la+Cultura+Pabellon+de+Arteaga+Aguascalientes"
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
    mapQuery: "Biblioteca+Enrique+Fernandez+Ledesma+Aguascalientes"
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
    mapQuery: "Biblioteca+Jaime+Torres+Bodet+Aguascalientes"
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
    mapQuery: "Biblioteca+Centenario+Bicentenario+Aguascalientes"
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
    mapQuery: "Sala+Alternativa+Cine+Aguascalientes"
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
    mapQuery: "Aquelarre+espacio+cultural+Aguascalientes"
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
    mapQuery: "Cafecheetoh+Café+Cultural+Aguascalientes"
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
    mapQuery: "Hogar+Espejo+Centro+Cultural+Aguascalientes"
  }
];

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
      
      // Ensure all Supabase entries have mapQuery
      supabaseExtras.forEach(s => {
        if (!s.mapQuery) {
          s.mapQuery = encodeURIComponent(s.name + " " + s.municipio + " Aguascalientes");
        }
      });
      
      activeSpacesList = [...spacesData, ...supabaseExtras];
      
      // Refresh user interface
      updateStatsTargets();
      filterData();
      
      // Ensure map tiles render correctly after data load
      if (map) {
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
          activeSpacesList.push(newSpace);
          updateStatsTargets();
          filterData();
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

// --- 2. Leaflet Map Initialization ---
let map;
let markersGroup;
const customMarkers = {};

function initMap() {
  try {
    const mapElement = document.getElementById("leafletMap");
    if (!mapElement) {
      console.error("Map element #leafletMap not found");
      return;
    }

    // Check if Leaflet is loaded
    if (typeof L === 'undefined') {
      console.error("Leaflet library (L) is not loaded");
      return;
    }

    // Centro de Aguascalientes
    map = L.map("leafletMap", {
      scrollWheelZoom: false,
      zoomControl: true
    }).setView([22.0, -102.3], 9.5);

    // CartoDB Dark Matter tile layer
    L.tileLayer("https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png", {
      attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors &copy; <a href="https://carto.com/attributions">CARTO</a>',
      subdomains: "abcd",
      maxZoom: 20
    }).addTo(map);

    markersGroup = L.featureGroup().addTo(map);
    
    // Rellenar marcadores
    plotMapMarkers(activeSpacesList);

    // Force map to recalculate size after initial render
    setTimeout(() => {
      if (map) map.invalidateSize();
    }, 300);
  } catch (err) {
    console.error("Error initializing map:", err);
  }
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
  municipio: "all"
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
      // Abrir drawer
      openSpaceDrawer(space);
      
      // Enfocar en mapa
      if (map && customMarkers[space.id]) {
        map.setView([space.lat, space.lng], 14);
        customMarkers[space.id].openPopup();
        
        // Scroll suave al mapa en móviles
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

if (resetFiltersBtn) {
  resetFiltersBtn.addEventListener("click", () => {
    activeFilters.search = "";
    activeFilters.type = "all";
    activeFilters.municipio = "all";
    if (searchInput) searchInput.value = "";
    if (clearSearchBtn) clearSearchBtn.style.display = "none";
    
    typeChips.forEach(c => c.classList.remove("active"));
    typeChips[0].classList.add("active");
    
    municipioChips.forEach(c => c.classList.remove("active"));
    municipioChips[0].classList.add("active");
    
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

  // Set Content
  drawerBadge.textContent = space.type;
  drawerBadge.className = `space-badge ${getBadgeClass(space.type)}`;
  drawerTitle.textContent = space.name;
  drawerAddress.textContent = space.address;
  drawerPhone.textContent = space.phone;
  drawerMunicipio.textContent = `${space.municipio} · Aguascalientes`;
  drawerMapsLink.href = `https://www.google.com/maps/search/${space.mapQuery}`;

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

// --- 5. Log Tab System (Bitácora de Campo) ---
const logTabButtons = document.querySelectorAll(".log-tab-btn");
const logTabPanes = document.querySelectorAll(".log-tab-pane");

logTabButtons.forEach(btn => {
  btn.addEventListener("click", () => {
    const tabName = btn.getAttribute("data-tab");
    
    logTabButtons.forEach(b => b.classList.remove("active"));
    logTabPanes.forEach(p => p.classList.remove("active"));
    
    btn.classList.add("active");
    const activePane = document.getElementById(`tab-${tabName}`);
    if (activePane) {
      activePane.classList.add("active");
    }
  });
});

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
    // Add a tiny random offset so overlapping markers don't align exactly
    const offsetLat = (Math.random() - 0.5) * 0.008;
    const offsetLng = (Math.random() - 0.5) * 0.008;

    const latValue = parseFloat(document.getElementById("spaceLatInput").value);
    const lngValue = parseFloat(document.getElementById("spaceLngInput").value);
    const lat = isNaN(latValue) ? (center[0] + offsetLat) : latValue;
    const lng = isNaN(lngValue) ? (center[1] + offsetLng) : lngValue;

    const mapQuery = mapQueryInput || encodeURIComponent(name + " " + municipio + " Aguascalientes");

    const newSpace = {
      name: name,
      address: address,
      phone: phone,
      type: type,
      municipio: municipio,
      lat: lat,
      lng: lng,
      mapQuery: mapQuery
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

    // Reload interface
    updateStatsTargets();
    filterData();

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

  if (supabaseClient) {
    loadSpacesFromSupabase();
    subscribeToSpacesRealtime();
  }
});
